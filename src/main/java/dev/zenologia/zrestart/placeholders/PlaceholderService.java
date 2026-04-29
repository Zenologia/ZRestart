package dev.zenologia.zrestart.placeholders;

import dev.zenologia.zrestart.ZRestartPlugin;
import dev.zenologia.zrestart.config.RestartConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlaceholderService {
    private final ZRestartPlugin plugin;
    private boolean available;
    private boolean enabled;

    public PlaceholderService(ZRestartPlugin plugin) {
        this.plugin = plugin;
    }

    public void refresh(RestartConfig config) {
        this.enabled = config.settings().papiPlaceholders();
        this.available = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public boolean active() {
        return this.enabled && this.available;
    }

    public boolean available() {
        return this.available;
    }

    public String apply(Player player, String input) {
        if (!active() || player == null || input == null || input.isEmpty()) {
            return input;
        }
        try {
            return PlaceholderAPI.setPlaceholders(player, input);
        } catch (NoClassDefFoundError ex) {
            this.available = false;
            this.plugin.getLogger().warning("PlaceholderAPI was expected but could not be linked. Internal placeholders will be used.");
            return input;
        } catch (RuntimeException ex) {
            this.plugin.getLogger().warning("PlaceholderAPI failed to parse a ZRestart message: " + ex.getMessage());
            return input;
        }
    }
}
