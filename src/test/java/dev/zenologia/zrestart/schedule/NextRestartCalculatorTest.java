package dev.zenologia.zrestart.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class NextRestartCalculatorTest {
    private final NextRestartCalculator calculator = new NextRestartCalculator();
    private final ZoneId zone = ZoneId.of("America/New_York");

    @Test
    void selectsEarliestFutureRestart() {
        List<ScheduleEntry> entries = List.of(
            new ScheduleEntry(ScheduleDay.DAILY, 5, 0, "Daily", "Daily;05;00;Daily"),
            new ScheduleEntry(ScheduleDay.MONDAY, 4, 30, "Weekly", "Monday;04;30;Weekly")
        );
        Instant now = Instant.parse("2026-04-27T07:00:00Z");

        NextRestart next = this.calculator.calculate(entries, now, this.zone).orElseThrow();

        assertEquals("Weekly", next.entry().reason());
        assertEquals(LocalTime.of(4, 30), next.time().toLocalTime());
    }

    @Test
    void movesPassedCurrentDayEntryToFollowingWeek() {
        ScheduleEntry mondayMorning = new ScheduleEntry(ScheduleDay.MONDAY, 11, 0, "", "Monday;11;00");
        Instant now = Instant.parse("2026-04-27T16:00:00Z");

        NextRestart next = this.calculator.calculate(List.of(mondayMorning), now, this.zone).orElseThrow();

        assertEquals("2026-05-04T15:00:00Z", next.instant().toString());
    }

    @Test
    void adjustsDstGapToNextValidLaterTime() {
        ScheduleEntry gapEntry = new ScheduleEntry(ScheduleDay.SUNDAY, 2, 30, "", "Sunday;02;30");
        Instant now = Instant.parse("2026-03-07T12:00:00Z");

        NextRestart next = this.calculator.calculate(List.of(gapEntry), now, this.zone).orElseThrow();

        assertFalse(next.adjustments().isEmpty());
        assertEquals(LocalTime.of(3, 0), next.time().toLocalTime());
    }

    @Test
    void adjustsDstOverlapToLaterOccurrence() {
        ScheduleEntry overlapEntry = new ScheduleEntry(ScheduleDay.SUNDAY, 1, 30, "", "Sunday;01;30");
        Instant now = Instant.parse("2026-10-31T12:00:00Z");

        NextRestart next = this.calculator.calculate(List.of(overlapEntry), now, this.zone).orElseThrow();

        assertFalse(next.adjustments().isEmpty());
        assertEquals(ZoneOffset.ofHours(-5), next.time().getOffset());
    }
}
