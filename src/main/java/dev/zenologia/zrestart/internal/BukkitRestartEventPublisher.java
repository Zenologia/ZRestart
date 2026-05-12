package dev.zenologia.zrestart.internal;

import dev.zenologia.zrestart.api.event.ZRestartCountdownCancelledEvent;
import dev.zenologia.zrestart.api.event.ZRestartCountdownStartedEvent;
import dev.zenologia.zrestart.api.event.ZRestartExecutingEvent;
import dev.zenologia.zrestart.countdown.CountdownState;
import org.bukkit.Bukkit;

public final class BukkitRestartEventPublisher implements RestartEventPublisher {
    @Override
    public void countdownStarted(CountdownState state) {
        Bukkit.getPluginManager().callEvent(new ZRestartCountdownStartedEvent(RestartSnapshots.fromState(state)));
    }

    @Override
    public void countdownCancelled(CountdownState state) {
        Bukkit.getPluginManager().callEvent(new ZRestartCountdownCancelledEvent(RestartSnapshots.fromState(state)));
    }

    @Override
    public void restartExecuting(CountdownState state) {
        Bukkit.getPluginManager().callEvent(new ZRestartExecutingEvent(RestartSnapshots.fromState(state)));
    }
}
