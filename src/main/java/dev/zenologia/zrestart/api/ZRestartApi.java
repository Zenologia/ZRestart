package dev.zenologia.zrestart.api;

import java.time.Duration;
import java.util.Optional;

/**
 * Public service API exposed by ZRestart through Bukkit's services manager.
 *
 * <p>Consumers should load and query this service during their own plugin enable
 * phase, then listen to ZRestart events for runtime changes. Load this service with
 * {@code Bukkit.getServicesManager().load(ZRestartApi.class)} and declare ZRestart
 * as an optional or required server dependency in {@code paper-plugin.yml}.</p>
 */
public interface ZRestartApi {
    /**
     * Returns the active countdown, if ZRestart currently has one.
     *
     * @return active countdown snapshot
     */
    Optional<RestartCountdown> currentCountdown();

    /**
     * Returns the next restart ZRestart currently knows about.
     *
     * <p>If a countdown is active, this returns the active countdown. Otherwise it
     * returns the next scheduled restart projection when one is available. Projection
     * snapshots have {@code RestartCountdown.id() == 0}.</p>
     *
     * @return next known restart snapshot
     */
    Optional<RestartCountdown> nextRestart();

    /**
     * Checks whether the next known restart is pending within the supplied window.
     *
     * @param window maximum allowed time until restart
     * @return {@code true} when a restart is known and close enough
     */
    boolean isRestartWithin(Duration window);

    /**
     * Returns whether the active countdown was manually triggered.
     *
     * @return {@code true} when a manual restart countdown is active
     */
    boolean isManualRestartPending();

    /**
     * Returns whether ZRestart has started final restart execution.
     *
     * @return {@code true} after countdown completion while restart execution is in progress
     */
    boolean isRestartExecuting();
}
