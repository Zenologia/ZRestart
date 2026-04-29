package dev.zenologia.zrestart.schedule;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class NextRestartCalculator {
    public Optional<NextRestart> calculate(List<ScheduleEntry> entries, Instant now, ZoneId zoneId) {
        return calculateAfter(entries, now, now, zoneId);
    }

    public Optional<NextRestart> calculateAfter(List<ScheduleEntry> entries, Instant now, Instant after, ZoneId zoneId) {
        if (entries.isEmpty()) {
            return Optional.empty();
        }

        ZonedDateTime zonedNow = now.atZone(zoneId);
        List<Candidate> candidates = new ArrayList<>();
        for (ScheduleEntry entry : entries) {
            candidates.add(nextCandidate(entry, zonedNow, after, zoneId));
        }

        return candidates.stream()
            .min(Comparator.comparing(candidate -> candidate.restart.time().toInstant()))
            .map(candidate -> candidate.restart);
    }

    private Candidate nextCandidate(ScheduleEntry entry, ZonedDateTime zonedNow, Instant after, ZoneId zoneId) {
        LocalDate date = entry.day().daily()
            ? zonedNow.toLocalDate()
            : nextWeekdayDate(entry, zonedNow.toLocalDate(), zonedNow.getDayOfWeek().getValue());

        ResolvedTime resolved = resolve(entry, date, zoneId);
        while (!resolved.time().toInstant().isAfter(after)) {
            date = entry.day().daily() ? date.plusDays(1) : date.plusWeeks(1);
            resolved = resolve(entry, date, zoneId);
        }

        return new Candidate(new NextRestart(entry, resolved.time(), resolved.adjustments()));
    }

    private LocalDate nextWeekdayDate(ScheduleEntry entry, LocalDate currentDate, int currentDayValue) {
        int targetDayValue = entry.day().dayOfWeek().getValue();
        int daysAhead = (targetDayValue - currentDayValue + 7) % 7;
        return currentDate.plusDays(daysAhead);
    }

    private ResolvedTime resolve(ScheduleEntry entry, LocalDate date, ZoneId zoneId) {
        LocalDateTime localDateTime = LocalDateTime.of(date, entry.localTime());
        ZoneRules rules = zoneId.getRules();
        List<ZoneOffset> offsets = rules.getValidOffsets(localDateTime);

        if (offsets.size() == 1) {
            return new ResolvedTime(ZonedDateTime.ofLocal(localDateTime, zoneId, offsets.get(0)), List.of());
        }

        if (offsets.isEmpty()) {
            ZoneOffsetTransition transition = rules.getTransition(localDateTime);
            ZonedDateTime adjusted = ZonedDateTime.of(transition.getDateTimeAfter(), zoneId);
            DstAdjustment warning = new DstAdjustment(
                entry,
                adjusted,
                "The local time falls in a daylight saving gap and was moved to the next valid time."
            );
            return new ResolvedTime(adjusted, List.of(warning));
        }

        ZonedDateTime first = ZonedDateTime.ofLocal(localDateTime, zoneId, offsets.get(0));
        ZonedDateTime second = ZonedDateTime.ofLocal(localDateTime, zoneId, offsets.get(1));
        ZonedDateTime later = first.toInstant().isAfter(second.toInstant()) ? first : second;
        DstAdjustment warning = new DstAdjustment(
            entry,
            later,
            "The local time is ambiguous during a daylight saving overlap and the later occurrence was selected."
        );
        return new ResolvedTime(later, List.of(warning));
    }

    private record Candidate(NextRestart restart) {
    }

    private record ResolvedTime(ZonedDateTime time, List<DstAdjustment> adjustments) {
    }
}
