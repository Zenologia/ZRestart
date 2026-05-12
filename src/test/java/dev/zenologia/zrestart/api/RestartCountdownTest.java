package dev.zenologia.zrestart.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RestartCountdownTest {
    private static final Instant NOW = Instant.parse("2026-05-02T12:00:00Z");

    @Test
    void calculatesRemainingTimeFromSuppliedInstant() {
        RestartCountdown countdown = countdownAt(NOW.plus(Duration.ofMinutes(5)));

        assertEquals(Duration.ofMinutes(5), countdown.remainingFrom(NOW));
    }

    @Test
    void reportsRestartWithinFutureWindow() {
        RestartCountdown countdown = countdownAt(NOW.plus(Duration.ofMinutes(5)));

        assertTrue(countdown.isWithin(Duration.ofMinutes(10), NOW));
        assertTrue(countdown.isWithin(Duration.ofMinutes(5), NOW));
        assertFalse(countdown.isWithin(Duration.ofMinutes(4), NOW));
    }

    @Test
    void doesNotTreatPastRestartAsWithinWindow() {
        RestartCountdown countdown = countdownAt(NOW.minus(Duration.ofSeconds(1)));

        assertFalse(countdown.isWithin(Duration.ofMinutes(10), NOW));
    }

    @Test
    void rejectsNegativeWindow() {
        RestartCountdown countdown = countdownAt(NOW.plus(Duration.ofMinutes(5)));

        assertThrows(IllegalArgumentException.class, () -> countdown.isWithin(Duration.ofSeconds(-1), NOW));
    }

    @Test
    void normalizesNullableTextFieldsToEmptyStrings() {
        RestartCountdown countdown = new RestartCountdown(
            1L,
            RestartKind.MANUAL,
            NOW.plus(Duration.ofMinutes(5)),
            Duration.ofMinutes(5),
            null,
            null
        );

        assertEquals("", countdown.reason());
        assertEquals("", countdown.scheduleEntryRaw());
    }

    private static RestartCountdown countdownAt(Instant targetTime) {
        return new RestartCountdown(
            1L,
            RestartKind.MANUAL,
            targetTime,
            Duration.ofMinutes(5),
            "Maintenance",
            ""
        );
    }
}
