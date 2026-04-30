package dev.zenologia.zrestart.countdown;

import dev.zenologia.zrestart.config.RestartConfig;
import dev.zenologia.zrestart.display.DisplayChannel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class WarningDispatcher {
    private List<Long> thresholds = List.of();
    private List<DisplayChannel> channels = List.of();

    public void configure(RestartConfig config, List<DisplayChannel> channels) {
        this.thresholds = config.countdown().warningTimes().stream()
            .map(Duration::toSeconds)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .toList();
        this.channels = List.copyOf(channels);
    }

    public void dispatchDue(CountdownState state, long remainingSeconds) {
        for (Long threshold : this.thresholds) {
            if (threshold > state.totalSeconds() || remainingSeconds > threshold) {
                continue;
            }
            if (!state.markWarningFired(threshold)) {
                continue;
            }
            Duration warningTime = Duration.ofSeconds(threshold);
            Duration remaining = Duration.ofSeconds(remainingSeconds);
            for (DisplayChannel channel : this.channels) {
                channel.showWarning(state, warningTime, remaining);
            }
        }
    }

    public void tick(CountdownState state, Duration remaining) {
        for (DisplayChannel channel : this.channels) {
            channel.tick(state, remaining);
        }
    }

    public void resetAfterDelay(CountdownState state, long remainingSeconds) {
        state.resetFutureWarnings(this.thresholds, remainingSeconds);
    }

    public void clear() {
        for (DisplayChannel channel : new ArrayList<>(this.channels)) {
            channel.clear();
        }
    }

    public List<Long> thresholds() {
        return this.thresholds;
    }
}
