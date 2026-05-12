package dev.zenologia.zrestart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.zenologia.zrestart.api.RestartCountdown;
import dev.zenologia.zrestart.api.RestartKind;
import dev.zenologia.zrestart.countdown.CountdownState;
import dev.zenologia.zrestart.countdown.CountdownType;
import dev.zenologia.zrestart.internal.RestartSnapshots;
import dev.zenologia.zrestart.schedule.NextRestart;
import dev.zenologia.zrestart.schedule.ScheduleDay;
import dev.zenologia.zrestart.schedule.ScheduleEntry;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

class ZRestartApiProviderTest {
    private static final Instant NOW = Instant.parse("2026-05-02T12:00:00Z");
    private static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    void mapsManualCountdownStateToPublicSnapshot() {
        CountdownState state = new CountdownState(
            CountdownType.MANUAL,
            NOW.plus(Duration.ofMinutes(10)),
            Duration.ofMinutes(10),
            "Manual maintenance",
            null
        );

        RestartCountdown snapshot = RestartSnapshots.fromState(state);

        assertTrue(snapshot.id() > 0L);
        assertEquals(RestartKind.MANUAL, snapshot.kind());
        assertTrue(snapshot.manual());
        assertEquals(NOW.plus(Duration.ofMinutes(10)), snapshot.targetTime());
        assertEquals(Duration.ofMinutes(10), snapshot.totalDuration());
        assertEquals("Manual maintenance", snapshot.reason());
        assertEquals("", snapshot.scheduleEntryRaw());
    }

    @Test
    void mapsScheduledCountdownStateToPublicSnapshot() {
        ScheduleEntry entry = new ScheduleEntry(
            ScheduleDay.DAILY,
            5,
            0,
            "Daily maintenance",
            "Daily;05;00;Daily maintenance"
        );
        CountdownState state = new CountdownState(
            CountdownType.SCHEDULED,
            NOW.plus(Duration.ofHours(1)),
            Duration.ofHours(1),
            entry.reason(),
            entry
        );

        RestartCountdown snapshot = RestartSnapshots.fromState(state);

        assertEquals(RestartKind.SCHEDULED, snapshot.kind());
        assertEquals("Daily maintenance", snapshot.reason());
        assertEquals("Daily;05;00;Daily maintenance", snapshot.scheduleEntryRaw());
    }

    @Test
    void mapsNextRestartProjectionWithZeroId() {
        ScheduleEntry entry = new ScheduleEntry(
            ScheduleDay.MONDAY,
            11,
            0,
            "Weekly maintenance",
            "Monday;11;00;Weekly maintenance"
        );
        NextRestart nextRestart = new NextRestart(
            entry,
            NOW.plus(Duration.ofMinutes(15)).atZone(UTC),
            List.of()
        );

        RestartCountdown snapshot = RestartSnapshots.fromNextRestart(nextRestart, NOW);

        assertEquals(0L, snapshot.id());
        assertEquals(RestartKind.SCHEDULED, snapshot.kind());
        assertEquals(Duration.ofMinutes(15), snapshot.totalDuration());
        assertEquals("Weekly maintenance", snapshot.reason());
        assertEquals("Monday;11;00;Weekly maintenance", snapshot.scheduleEntryRaw());
    }
}
