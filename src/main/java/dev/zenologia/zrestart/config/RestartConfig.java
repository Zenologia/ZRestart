package dev.zenologia.zrestart.config;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.SoundCategory;

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
        BossBarSettings bossBar,
        SoundSettings sounds
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

    public record SoundSettings(
        boolean enabled,
        SoundCategory category,
        List<SoundEntry> entries
    ) {
    }

    public record SoundEntry(
        Duration time,
        SoundReference sound,
        float volume,
        float pitch
    ) {
    }

    public record SoundReference(
        String input,
        String bukkitSoundName,
        String namespacedKey
    ) {
        public boolean usesBukkitSound() {
            return this.bukkitSoundName != null;
        }
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
