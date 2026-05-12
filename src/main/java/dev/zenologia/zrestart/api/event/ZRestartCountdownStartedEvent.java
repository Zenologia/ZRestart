package dev.zenologia.zrestart.api.event;

import dev.zenologia.zrestart.api.RestartCountdown;
import java.util.Objects;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called after ZRestart starts a scheduled or manual restart countdown.
 *
 * <p>Consumers should still query {@code ZRestartApi} during their own enable
 * phase for already-active startup countdowns, because dependent plugins may load
 * after ZRestart starts its initial scheduled countdown.</p>
 */
public final class ZRestartCountdownStartedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final RestartCountdown countdown;

    /**
     * Creates a countdown-started event.
     *
     * @param countdown countdown that was started
     */
    public ZRestartCountdownStartedEvent(RestartCountdown countdown) {
        this.countdown = Objects.requireNonNull(countdown, "countdown");
    }

    /**
     * Returns the countdown that was started.
     *
     * @return countdown snapshot
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
