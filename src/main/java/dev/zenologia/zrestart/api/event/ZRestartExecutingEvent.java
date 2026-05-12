package dev.zenologia.zrestart.api.event;

import dev.zenologia.zrestart.api.RestartCountdown;
import java.util.Objects;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when ZRestart begins final restart execution.
 *
 * <p>This event is notification-only. It is not cancellable and only fires after
 * pre-restart commands have allowed the restart to continue.</p>
 */
public final class ZRestartExecutingEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final RestartCountdown countdown;

    /**
     * Creates a restart-executing event.
     *
     * @param countdown countdown that reached execution
     */
    public ZRestartExecutingEvent(RestartCountdown countdown) {
        this.countdown = Objects.requireNonNull(countdown, "countdown");
    }

    /**
     * Returns the countdown that reached execution.
     *
     * @return executing countdown snapshot
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
