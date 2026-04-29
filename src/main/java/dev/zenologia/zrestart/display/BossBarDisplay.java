package dev.zenologia.zrestart.display;

import dev.zenologia.zrestart.config.MessageManager;
import dev.zenologia.zrestart.config.RestartConfig;
import dev.zenologia.zrestart.countdown.CountdownState;
import dev.zenologia.zrestart.placeholders.PlaceholderContext;
import dev.zenologia.zrestart.time.TimeFormatter;
import dev.zenologia.zrestart.util.TextRenderer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class BossBarDisplay implements DisplayChannel, Listener {
    private final MessageManager messages;
    private final TextRenderer renderer;
    private final Supplier<RestartConfig> configSupplier;
    private final TimeFormatter timeFormatter;
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private CountdownState visibleState;
    private Duration visibleRemaining = Duration.ZERO;

    public BossBarDisplay(
        MessageManager messages,
        TextRenderer renderer,
        Supplier<RestartConfig> configSupplier,
        TimeFormatter timeFormatter
    ) {
        this.messages = messages;
        this.renderer = renderer;
        this.configSupplier = configSupplier;
        this.timeFormatter = timeFormatter;
    }

    @Override
    public void showWarning(CountdownState state, Duration remaining) {
        tick(state, remaining);
    }

    @Override
    public void tick(CountdownState state, Duration remaining) {
        RestartConfig config = this.configSupplier.get();
        RestartConfig.BossBarSettings settings = config.countdown().bossBar();
        if (!settings.enabled() || remaining.compareTo(settings.showFrom()) > 0 || remaining.isZero() || remaining.isNegative()) {
            clear();
            return;
        }

        PlaceholderContext placeholders = DisplayPlaceholders.countdown(state, remaining, config, this.timeFormatter);
        float progress = settings.progress()
            ? (float) Math.max(0.0D, Math.min(1.0D, remaining.toSeconds() / (double) Math.max(1L, state.totalSeconds())))
            : 1.0F;

        if (this.visibleState == null || this.visibleState.id() != state.id()) {
            clear();
            this.visibleState = state;
        }
        this.visibleRemaining = remaining;
        removeOfflineBars();
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerBar(player, placeholders, progress, settings);
        }
    }

    @Override
    public void clear() {
        for (Map.Entry<UUID, BossBar> entry : this.bossBars.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                player.hideBossBar(entry.getValue());
            }
        }
        this.bossBars.clear();
        this.visibleState = null;
        this.visibleRemaining = Duration.ZERO;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.visibleState != null && !this.visibleRemaining.isZero() && !this.visibleRemaining.isNegative()) {
            RestartConfig config = this.configSupplier.get();
            RestartConfig.BossBarSettings settings = config.countdown().bossBar();
            PlaceholderContext placeholders = DisplayPlaceholders.countdown(this.visibleState, this.visibleRemaining, config, this.timeFormatter);
            float progress = settings.progress()
                ? (float) Math.max(0.0D, Math.min(1.0D, this.visibleRemaining.toSeconds() / (double) Math.max(1L, this.visibleState.totalSeconds())))
                : 1.0F;
            updatePlayerBar(event.getPlayer(), placeholders, progress, settings);
        }
    }

    private void updatePlayerBar(Player player, PlaceholderContext placeholders, float progress, RestartConfig.BossBarSettings settings) {
        BossBar bar = this.bossBars.get(player.getUniqueId());
        if (bar == null) {
            bar = BossBar.bossBar(
                this.renderer.component(player, this.messages.string("countdown.boss-bar"), placeholders),
                progress,
                settings.color(),
                settings.overlay()
            );
            this.bossBars.put(player.getUniqueId(), bar);
            player.showBossBar(bar);
            return;
        }

        bar.name(this.renderer.component(player, this.messages.string("countdown.boss-bar"), placeholders));
        bar.progress(progress);
        bar.color(settings.color());
        bar.overlay(settings.overlay());
    }

    private void removeOfflineBars() {
        Iterator<Map.Entry<UUID, BossBar>> iterator = this.bossBars.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, BossBar> entry = iterator.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                iterator.remove();
            }
        }
    }
}
