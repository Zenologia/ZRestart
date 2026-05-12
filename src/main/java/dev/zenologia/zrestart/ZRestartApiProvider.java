package dev.zenologia.zrestart;

import dev.zenologia.zrestart.api.RestartCountdown;
import dev.zenologia.zrestart.api.ZRestartApi;
import dev.zenologia.zrestart.internal.RestartSnapshots;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

final class ZRestartApiProvider implements ZRestartApi {
    private final ZRestartPlugin plugin;

    ZRestartApiProvider(ZRestartPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public Optional<RestartCountdown> currentCountdown() {
        if (this.plugin.countdownManager() == null) {
            return Optional.empty();
        }
        return this.plugin.countdownManager().activeState().map(RestartSnapshots::fromState);
    }

    @Override
    public Optional<RestartCountdown> nextRestart() {
        Optional<RestartCountdown> active = currentCountdown();
        if (active.isPresent() || this.plugin.scheduleService() == null) {
            return active;
        }
        Instant now = Instant.now();
        return this.plugin.scheduleService().nextRestart().map(next -> RestartSnapshots.fromNextRestart(next, now));
    }

    @Override
    public boolean isRestartWithin(Duration window) {
        Objects.requireNonNull(window, "window");
        if (window.isNegative()) {
            throw new IllegalArgumentException("window cannot be negative.");
        }
        Instant now = Instant.now();
        return nextRestart().map(countdown -> countdown.isWithin(window, now)).orElse(false);
    }

    @Override
    public boolean isManualRestartPending() {
        return currentCountdown().map(RestartCountdown::manual).orElse(false);
    }

    @Override
    public boolean isRestartExecuting() {
        return this.plugin.restartExecuting();
    }
}
