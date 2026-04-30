package dev.zenologia.zrestart.countdown;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.zenologia.zrestart.config.RestartConfig;
import dev.zenologia.zrestart.display.DisplayChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class WarningDispatcherTest {
    @Test
    void delayAllowsPreviouslyFiredThresholdToFireAgain() {
        WarningDispatcher dispatcher = new WarningDispatcher();
        RecordingDisplay display = new RecordingDisplay();
        RestartConfig config = new RestartConfig(
            null,
            null,
            new RestartConfig.Countdown(List.of(Duration.ofMinutes(5)), null, null, null, null),
            null,
            null
        );
        dispatcher.configure(config, List.of(display));

        Instant now = Instant.parse("2026-04-29T12:00:00Z");
        CountdownState state = new CountdownState(
            CountdownType.MANUAL,
            now.plus(Duration.ofMinutes(10)),
            Duration.ofMinutes(10),
            "Maintenance",
            null
        );

        dispatcher.dispatchDue(state, Duration.ofMinutes(5).toSeconds());
        assertEquals(1, display.warnings);

        state.delay(Duration.ofMinutes(10), now.plus(Duration.ofMinutes(5)));
        dispatcher.resetAfterDelay(state, Duration.ofMinutes(15).toSeconds());
        dispatcher.dispatchDue(state, Duration.ofMinutes(5).toSeconds());

        assertEquals(2, display.warnings);
    }

    @Test
    void dispatchPassesConfiguredThresholdWhenTickRunsLate() {
        WarningDispatcher dispatcher = new WarningDispatcher();
        RecordingDisplay display = new RecordingDisplay();
        RestartConfig config = new RestartConfig(
            null,
            null,
            new RestartConfig.Countdown(List.of(Duration.ofMinutes(5)), null, null, null, null),
            null,
            null
        );
        dispatcher.configure(config, List.of(display));

        Instant now = Instant.parse("2026-04-29T12:00:00Z");
        CountdownState state = new CountdownState(
            CountdownType.MANUAL,
            now.plus(Duration.ofMinutes(10)),
            Duration.ofMinutes(10),
            "Maintenance",
            null
        );

        dispatcher.dispatchDue(state, Duration.ofMinutes(5).minusSeconds(1).toSeconds());

        assertEquals(Duration.ofMinutes(5), display.warningTime);
        assertEquals(Duration.ofMinutes(5).minusSeconds(1), display.remaining);
    }

    private static final class RecordingDisplay implements DisplayChannel {
        private int warnings;
        private Duration warningTime;
        private Duration remaining;

        @Override
        public void showWarning(CountdownState state, Duration remaining) {
            this.warnings++;
        }

        @Override
        public void showWarning(CountdownState state, Duration warningTime, Duration remaining) {
            this.warningTime = warningTime;
            this.remaining = remaining;
            showWarning(state, remaining);
        }
    }
}
