package dev.zenologia.zrestart.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable public snapshot of a ZRestart restart countdown.
 *
 * @param id stable countdown id while the countdown is active; {@code 0} for a non-active schedule projection
 * @param kind whether the countdown came from the automatic schedule or a manual command
 * @param targetTime instant when the restart is expected to execute
 * @param totalDuration original active countdown duration, or remaining duration at snapshot creation for projections
 * @param reason configured or manually supplied restart reason, or an empty string
 * @param scheduleEntryRaw raw schedule entry for scheduled restarts, or an empty string
 */
public record RestartCountdown(
    long id,
    RestartKind kind,
    Instant targetTime,
    Duration totalDuration,
    String reason,
    String scheduleEntryRaw
) {
    /**
     * Creates a restart countdown snapshot.
     */
    public RestartCountdown {
        if (id < 0L) {
            throw new IllegalArgumentException("id cannot be negative.");
        }
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(targetTime, "targetTime");
        Objects.requireNonNull(totalDuration, "totalDuration");
        if (totalDuration.isNegative()) {
            throw new IllegalArgumentException("totalDuration cannot be negative.");
        }
        reason = reason == null ? "" : reason;
        scheduleEntryRaw = scheduleEntryRaw == null ? "" : scheduleEntryRaw;
    }

    /**
     * Returns the time remaining until {@link #targetTime()} from the supplied instant.
     * The returned duration is negative when the target time has already passed.
     *
     * @param now instant to compare against
     * @return remaining duration from {@code now}
     */
    public Duration remainingFrom(Instant now) {
        return Duration.between(Objects.requireNonNull(now, "now"), this.targetTime);
    }

    /**
     * Checks whether this restart is in the future and no farther away than the supplied window.
     *
     * @param window maximum allowed time until restart
     * @param now instant to compare against
     * @return {@code true} when the restart is pending within {@code window}
     */
    public boolean isWithin(Duration window, Instant now) {
        Objects.requireNonNull(window, "window");
        if (window.isNegative()) {
            throw new IllegalArgumentException("window cannot be negative.");
        }
        Duration remaining = remainingFrom(now);
        return !remaining.isNegative() && remaining.compareTo(window) <= 0;
    }

    /**
     * Returns whether this snapshot represents a manual restart countdown.
     * Schedule projections are never manual and always have {@code id() == 0}.
     *
     * @return {@code true} for manual restart countdowns
     */
    public boolean manual() {
        return this.kind == RestartKind.MANUAL;
    }
}
