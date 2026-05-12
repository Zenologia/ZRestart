package dev.zenologia.zrestart.internal;

import dev.zenologia.zrestart.api.RestartCountdown;
import dev.zenologia.zrestart.api.RestartKind;
import dev.zenologia.zrestart.countdown.CountdownState;
import dev.zenologia.zrestart.countdown.CountdownType;
import dev.zenologia.zrestart.schedule.NextRestart;
import java.time.Duration;
import java.time.Instant;

public final class RestartSnapshots {
    private RestartSnapshots() {
    }

    public static RestartCountdown fromState(CountdownState state) {
        String scheduleEntryRaw = state.scheduleEntry()
            .map(entry -> entry.raw() == null ? "" : entry.raw())
            .orElse("");
        return new RestartCountdown(
            state.id(),
            fromType(state.type()),
            state.target(),
            Duration.ofSeconds(state.totalSeconds()),
            state.reason(),
            scheduleEntryRaw
        );
    }

    public static RestartCountdown fromNextRestart(NextRestart nextRestart, Instant now) {
        Duration remaining = Duration.between(now, nextRestart.instant());
        if (remaining.isNegative()) {
            remaining = Duration.ZERO;
        }
        return new RestartCountdown(
            0L,
            RestartKind.SCHEDULED,
            nextRestart.instant(),
            remaining,
            nextRestart.entry().reason(),
            nextRestart.entry().raw()
        );
    }

    private static RestartKind fromType(CountdownType type) {
        return switch (type) {
            case SCHEDULED -> RestartKind.SCHEDULED;
            case MANUAL -> RestartKind.MANUAL;
        };
    }
}
