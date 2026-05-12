package dev.zenologia.zrestart.api.event;

import dev.zenologia.zrestart.api.RestartCountdown;
import java.util.Objects;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called after ZRestart successfully cancels an active restart countdown.
 *
 * <p>This includes direct cancellations from {@code /zrestart stop}, manual
 * restart replacement, and reloads that clear or replace an automatic scheduled
 * countdown. Manual countdowns continue across reloads.</p>
 */
public final class ZRestartCountdownCancelledEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final RestartCountdown countdown;

    /**
     * Creates a countdown-cancelled event.
     *
     * @param countdown countdown that was cancelled
     */
    public ZRestartCountdownCancelledEvent(RestartCountdown countdown) {
        this.countdown = Objects.requireNonNull(countdown, "countdown");
    }

    /**
     * Returns the countdown that was cancelled.
     *
     * @return cancelled countdown snapshot
     */
    public RestartCountdown countdown() {
        return this.countdown;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Returns Bukkit's handler list for this event type.
     *
     * @return event handler list
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
