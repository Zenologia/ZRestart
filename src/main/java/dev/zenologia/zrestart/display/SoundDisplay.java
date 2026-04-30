package dev.zenologia.zrestart.display;

import dev.zenologia.zrestart.config.RestartConfig;
import dev.zenologia.zrestart.config.SoundResolver;
import dev.zenologia.zrestart.countdown.CountdownState;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class SoundDisplay implements DisplayChannel {
    private final Supplier<RestartConfig> configSupplier;

    public SoundDisplay(Supplier<RestartConfig> configSupplier) {
        this.configSupplier = configSupplier;
    }

    @Override
    public void showWarning(CountdownState state, Duration remaining) {
        showWarning(state, remaining, remaining);
    }

    @Override
    public void showWarning(CountdownState state, Duration warningTime, Duration remaining) {
        RestartConfig.SoundSettings settings = this.configSupplier.get().countdown().sounds();
        if (!settings.enabled() || settings.entries().isEmpty()) {
            return;
        }

        List<RestartConfig.SoundEntry> entries = soundsBySecond(settings).get(warningTime.toSeconds());
        if (entries == null || entries.isEmpty()) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (RestartConfig.SoundEntry entry : entries) {
                play(player, settings, entry);
            }
        }
    }

    private Map<Long, List<RestartConfig.SoundEntry>> soundsBySecond(RestartConfig.SoundSettings settings) {
        Map<Long, List<RestartConfig.SoundEntry>> entriesBySecond = new LinkedHashMap<>();
        for (RestartConfig.SoundEntry entry : settings.entries()) {
            entriesBySecond.computeIfAbsent(entry.time().toSeconds(), ignored -> new ArrayList<>()).add(entry);
        }
        return entriesBySecond;
    }

    private void play(Player player, RestartConfig.SoundSettings settings, RestartConfig.SoundEntry entry) {
        RestartConfig.SoundReference sound = entry.sound();
        if (sound.usesBukkitSound()) {
            SoundResolver.bukkitSound(sound.bukkitSoundName())
                .ifPresent(resolved -> playBukkitSound(player, settings, entry, resolved));
            return;
        }
        player.playSound(player, sound.namespacedKey(), settings.category(), entry.volume(), entry.pitch());
    }

    private void playBukkitSound(
        Player player,
        RestartConfig.SoundSettings settings,
        RestartConfig.SoundEntry entry,
        Sound sound
    ) {
        player.playSound(player, sound, settings.category(), entry.volume(), entry.pitch());
    }
}
