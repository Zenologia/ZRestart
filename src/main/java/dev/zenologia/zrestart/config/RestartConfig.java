package dev.zenologia.zrestart.config;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import net.kyori.adventure.bossbar.BossBar;

public record RestartConfig(
    Settings settings,
    Schedule schedule,
    Countdown countdown,
    Formatting formatting,
    PreRestartCommands preRestartCommands
) {
    public record Settings(
        String timezone,
        String fallbackTimezone,
        ZoneId zoneId,
        boolean checkRestartScript,
        boolean papiPlaceholders
    ) {
    }

    public record Schedule(boolean enabled, List<String> entries) {
    }

    public record Countdown(
        List<Duration> warningTimes,
        Chat chat,
        Title title,
        BossBarSettings bossBar
    ) {
    }

    public record Chat(boolean enabled) {
    }

    public record Title(boolean enabled, int fadeInTicks, int stayTicks, int fadeOutTicks) {
    }

    public record BossBarSettings(
        boolean enabled,
        BossBar.Color color,
        BossBar.Overlay overlay,
        Duration showFrom,
        boolean progress
    ) {
    }

    public record Formatting(
        boolean miniMessage,
        boolean legacyAmpersandColors,
        String emptyReason,
        TimeFormat timeFormat
    ) {
    }

    public record TimeFormat(
        boolean includeDays,
        boolean includeHours,
        boolean includeMinutes,
        boolean includeSeconds,
        boolean compact
    ) {
    }

    public record PreRestartCommands(
        boolean enabled,
        FailureBehavior failureBehavior,
        List<String> commands
    ) {
    }

    public enum FailureBehavior {
        CONTINUE,
        ABORT
    }
}
