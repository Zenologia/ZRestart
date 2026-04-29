package dev.zenologia.zrestart.config;

import dev.zenologia.zrestart.ZRestartPlugin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MessageManager {
    private final ZRestartPlugin plugin;
    private YamlConfiguration messages;
    private YamlConfiguration defaults;

    public MessageManager(ZRestartPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadInitial() throws IOException, InvalidConfigurationException {
        this.defaults = loadDefaults();
        this.messages = loadCandidate();
    }

    public YamlConfiguration loadCandidate() throws IOException, InvalidConfigurationException {
        File file = new File(this.plugin.getDataFolder(), "messages.yml");
        YamlConfiguration loaded = new YamlConfiguration();
        loaded.load(file);
        return loaded;
    }

    public void apply(YamlConfiguration candidate) {
        this.messages = candidate;
    }

    public String prefix() {
        return string("prefix");
    }

    public String string(String path) {
        String value = this.messages.getString(path);
        if (value != null) {
            return value;
        }
        String fallback = this.defaults.getString(path);
        if (fallback != null) {
            this.plugin.getLogger().warning("Missing messages.yml path '" + path + "'. Using built-in default.");
            return fallback;
        }
        this.plugin.getLogger().warning("Missing messages.yml path '" + path + "' and no built-in default exists.");
        return "";
    }

    public List<String> stringList(String path) {
        if (this.messages.isList(path)) {
            return this.messages.getStringList(path);
        }
        if (this.defaults.isList(path)) {
            this.plugin.getLogger().warning("Missing messages.yml list '" + path + "'. Using built-in default.");
            return this.defaults.getStringList(path);
        }
        String single = string(path);
        return single.isEmpty() ? List.of() : List.of(single);
    }

    private YamlConfiguration loadDefaults() throws IOException, InvalidConfigurationException {
        try (InputStream stream = this.plugin.getResource("messages.yml")) {
            if (stream == null) {
                throw new IOException("Bundled messages.yml resource is missing.");
            }
            YamlConfiguration loaded = new YamlConfiguration();
            loaded.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return loaded;
        }
    }
}
