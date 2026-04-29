package dev.zenologia.zrestart.countdown;

import dev.zenologia.zrestart.schedule.ScheduleEntry;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public final class CountdownState {
    private static final AtomicLong IDS = new AtomicLong();

    private final long id;
    private final CountdownType type;
    private final String reason;
    private final ScheduleEntry scheduleEntry;
    private final Set<Long> firedWarnings = new HashSet<>();
    private Instant target;
    private long totalSeconds;
    private boolean executing;

    public CountdownState(CountdownType type, Instant target, Duration totalDuration, String reason, ScheduleEntry scheduleEntry) {
        this.id = IDS.incrementAndGet();
        this.type = type;
        this.target = target;
        this.totalSeconds = Math.max(1L, totalDuration.toSeconds());
        this.reason = reason;
        this.scheduleEntry = scheduleEntry;
    }

    public long id() {
        return this.id;
    }

    public CountdownType type() {
        return this.type;
    }

    public Instant target() {
        return this.target;
    }

    public long totalSeconds() {
        return this.totalSeconds;
    }

    public String reason() {
        return this.reason;
    }

    public Optional<ScheduleEntry> scheduleEntry() {
        return Optional.ofNullable(this.scheduleEntry);
    }

    public boolean executing() {
        return this.executing;
    }

    public void markExecuting() {
        this.executing = true;
    }

    public Duration remaining(Instant now) {
        return Duration.between(now, this.target);
    }

    public long remainingSecondsCeil(Instant now) {
        long millis = Math.max(0L, Duration.between(now, this.target).toMillis());
        return (millis + 999L) / 1000L;
    }

    public void delay(Duration duration, Instant now) {
        this.target = this.target.plus(duration);
        long remaining = Math.max(1L, remainingSecondsCeil(now));
        this.totalSeconds = Math.max(this.totalSeconds, remaining);
    }

    public boolean markWarningFired(long thresholdSeconds) {
        return this.firedWarnings.add(thresholdSeconds);
    }

    public void resetFutureWarnings(Iterable<Long> thresholds, long currentRemainingSeconds) {
        for (Long threshold : thresholds) {
            if (threshold < currentRemainingSeconds) {
                this.firedWarnings.remove(threshold);
            }
        }
    }
}
