package dev.zenologia.zrestart.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class YamlFileMigrator {
    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final JavaPlugin plugin;

    public YamlFileMigrator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public YamlMigrationResult migrateResource(String resourceName, String versionPath) throws IOException, InvalidConfigurationException {
        File target = new File(this.plugin.getDataFolder(), resourceName);
        if (!target.exists()) {
            this.plugin.saveResource(resourceName, false);
            YamlConfiguration created = loadFile(target);
            return YamlMigrationResult.created(created.getInt(versionPath, 0));
        }

        YamlConfiguration latest = loadResource(resourceName);
        return migrate(target, latest, versionPath);
    }

    public YamlMigrationResult migrate(File target, YamlConfiguration latest, String versionPath) throws IOException, InvalidConfigurationException {
        YamlConfiguration installed = loadFile(target);
        int installedVersion = installed.getInt(versionPath, 0);
        int latestVersion = latest.getInt(versionPath, 0);

        if (latestVersion <= 0 || installedVersion >= latestVersion) {
            return YamlMigrationResult.unchanged(installedVersion);
        }

        File backup = createBackup(target);
        List<String> rawHeader = latest.options().getHeader();
        List<String> header = rawHeader == null ? List.of() : List.copyOf(rawHeader);
        Map<String, List<String>> comments = collectComments(latest, false);
        Map<String, List<String>> inlineComments = collectComments(latest, true);

        copyExistingValues(installed, latest, versionPath);
        latest.set(versionPath, latestVersion);
        latest.options().setHeader(header);
        restoreComments(latest, comments, false);
        restoreComments(latest, inlineComments, true);
        latest.save(target);

        return YamlMigrationResult.migrated(installedVersion, latestVersion, backup);
    }

    private YamlConfiguration loadResource(String resourceName) throws IOException, InvalidConfigurationException {
        try (InputStream stream = this.plugin.getResource(resourceName)) {
            if (stream == null) {
                throw new IOException("Bundled resource is missing: " + resourceName);
            }
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.options().parseComments(true);
            yaml.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return yaml;
        }
    }

    private static YamlConfiguration loadFile(File file) throws IOException, InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().parseComments(true);
        yaml.load(file);
        return yaml;
    }

    private static File createBackup(File target) throws IOException {
        String timestamp = LocalDateTime.now().format(BACKUP_TIMESTAMP);
        File backup = new File(target.getParentFile(), target.getName() + ".bak-" + timestamp);
        int suffix = 2;
        while (backup.exists()) {
            backup = new File(target.getParentFile(), target.getName() + ".bak-" + timestamp + "-" + suffix);
            suffix++;
        }
        Files.copy(target.toPath(), backup.toPath());
        return backup;
    }

    private static void copyExistingValues(YamlConfiguration installed, YamlConfiguration latest, String versionPath) {
        for (String path : installed.getKeys(true)) {
            if (path.equals(versionPath) || installed.isConfigurationSection(path) || latest.isConfigurationSection(path)) {
                continue;
            }
            if (latest.contains(path)) {
                latest.set(path, installed.get(path));
            }
        }
    }

    private static Map<String, List<String>> collectComments(YamlConfiguration yaml, boolean inline) {
        Map<String, List<String>> comments = new LinkedHashMap<>();
        for (String path : yaml.getKeys(true)) {
            List<String> pathComments = inline ? yaml.getInlineComments(path) : yaml.getComments(path);
            if (!pathComments.isEmpty()) {
                comments.put(path, List.copyOf(pathComments));
            }
        }
        return comments;
    }

    private static void restoreComments(YamlConfiguration yaml, Map<String, List<String>> comments, boolean inline) {
        for (Map.Entry<String, List<String>> entry : comments.entrySet()) {
            if (inline) {
                yaml.setInlineComments(entry.getKey(), entry.getValue());
            } else {
                yaml.setComments(entry.getKey(), entry.getValue());
            }
        }
    }
}
