package dev.zenologia.zrestart.countdown;

import dev.zenologia.zrestart.ZRestartPlugin;
import dev.zenologia.zrestart.restart.RestartExecutor;
import dev.zenologia.zrestart.schedule.NextRestart;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public final class CountdownManager {
    private final ZRestartPlugin plugin;
    private final WarningDispatcher warningDispatcher;
    private final RestartExecutor restartExecutor;
    private BukkitTask task;
    private CountdownState activeState;

    public CountdownManager(ZRestartPlugin plugin, WarningDispatcher warningDispatcher, RestartExecutor restartExecutor) {
        this.plugin = plugin;
        this.warningDispatcher = warningDispatcher;
        this.restartExecutor = restartExecutor;
    }

    public void startTicker() {
        if (this.task != null) {
            this.task.cancel();
        }
        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 20L, 20L);
    }

    public void stopTicker() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        clear();
    }

    public void startScheduled(NextRestart nextRestart) {
        Instant now = Instant.now();
        Duration total = Duration.between(now, nextRestart.instant());
        this.activeState = new CountdownState(
            CountdownType.SCHEDULED,
            nextRestart.instant(),
            total,
            nextRestart.entry().reason(),
            nextRestart.entry()
        );
        this.warningDispatcher.clear();
    }

    public void startManual(Duration duration, String reason) {
        Instant now = Instant.now();
        this.activeState = new CountdownState(
            CountdownType.MANUAL,
            now.plus(duration),
            duration,
            reason,
            null
        );
        this.warningDispatcher.clear();
    }

    public boolean delay(Duration duration) {
        if (this.activeState == null || this.activeState.executing()) {
            return false;
        }
        Instant now = Instant.now();
        this.activeState.delay(duration, now);
        this.warningDispatcher.resetAfterDelay(this.activeState, this.activeState.remainingSecondsCeil(now));
        return true;
    }

    public Optional<CountdownState> cancelActive() {
        if (this.activeState == null || this.activeState.executing()) {
            return Optional.empty();
        }
        CountdownState cancelled = this.activeState;
        this.activeState = null;
        this.warningDispatcher.clear();
        return Optional.of(cancelled);
    }

    public Optional<CountdownState> activeState() {
        return Optional.ofNullable(this.activeState);
    }

    public boolean manualActive() {
        return this.activeState != null && this.activeState.type() == CountdownType.MANUAL;
    }

    public void reapplyWarningThresholds() {
        if (this.activeState != null) {
            long remaining = this.activeState.remainingSecondsCeil(Instant.now());
            this.warningDispatcher.resetAfterDelay(this.activeState, remaining);
        }
    }

    public void clear() {
        this.activeState = null;
        this.warningDispatcher.clear();
    }

    private void tick() {
        CountdownState state = this.activeState;
        if (state == null) {
            return;
        }

        Instant now = Instant.now();
        Duration remaining = state.remaining(now);
        if (remaining.compareTo(Duration.ZERO) <= 0) {
            state.markExecuting();
            this.warningDispatcher.clear();
            this.activeState = null;
            this.restartExecutor.execute(state);
            return;
        }

        long remainingSeconds = state.remainingSecondsCeil(now);
        this.warningDispatcher.dispatchDue(state, remainingSeconds);
        this.warningDispatcher.tick(state, Duration.ofSeconds(remainingSeconds));
    }
}
