package dev.zenologia.zrestart.display;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class BossBarDisplayTest {
    @Test
    void scheduledCountdownProgressUsesVisibleWindow() {
        float progress = BossBarDisplay.progress(
            Duration.ofMinutes(5),
            Duration.ofHours(12).toSeconds(),
            Duration.ofMinutes(5),
            true
        );

        assertEquals(1.0F, progress, 0.0001F);
    }

    @Test
    void scheduledCountdownProgressShrinksAcrossVisibleWindow() {
        float progress = BossBarDisplay.progress(
            Duration.ofMinutes(2).plusSeconds(30),
            Duration.ofHours(12).toSeconds(),
            Duration.ofMinutes(5),
            true
        );

        assertEquals(0.5F, progress, 0.0001F);
    }

    @Test
    void shortManualCountdownProgressUsesTotalDuration() {
        float progress = BossBarDisplay.progress(
            Duration.ofMinutes(1),
            Duration.ofMinutes(2).toSeconds(),
            Duration.ofMinutes(5),
            true
        );

        assertEquals(0.5F, progress, 0.0001F);
    }

    @Test
    void disabledProgressStaysFull() {
        float progress = BossBarDisplay.progress(
            Duration.ofSeconds(1),
            Duration.ofMinutes(5).toSeconds(),
            Duration.ofMinutes(5),
            false
        );

        assertEquals(1.0F, progress, 0.0001F);
    }
}
