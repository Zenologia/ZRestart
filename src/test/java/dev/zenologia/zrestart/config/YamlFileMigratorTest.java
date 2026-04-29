package dev.zenologia.zrestart.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class YamlFileMigratorTest {
    @TempDir
    private Path tempDir;

    @Test
    void migratesMissingVersionToLatestTemplateAndKeepsAdminValues() throws Exception {
        Path file = this.tempDir.resolve("config.yml");
        Files.writeString(
            file,
            """
            settings:
              timezone: "UTC"
            old-path: "remove me"
            """
        );

        YamlConfiguration latest = latestTemplate(2);
        latest.setComments("settings.timezone", List.of("Use an IANA timezone ID."));

        YamlMigrationResult result = new YamlFileMigrator(null).migrate(file.toFile(), latest, "config-version");

        YamlConfiguration migrated = load(file);
        assertTrue(result.migrated());
        assertEquals(0, result.previousVersion());
        assertEquals(2, result.currentVersion());
        assertNotNull(result.backupFile());
        assertTrue(result.backupFile().exists());
        assertEquals(2, migrated.getInt("config-version"));
        assertEquals("UTC", migrated.getString("settings.timezone"));
        assertEquals("default", migrated.getString("new-path"));
        assertFalse(migrated.contains("old-path"));
        assertEquals(List.of("Use an IANA timezone ID."), migrated.getComments("settings.timezone"));
    }

    @Test
    void leavesSameVersionFileAlone() throws Exception {
        Path file = this.tempDir.resolve("config.yml");
        Files.writeString(
            file,
            """
            config-version: 2
            settings:
              timezone: "UTC"
            """
        );

        YamlMigrationResult result = new YamlFileMigrator(null).migrate(file.toFile(), latestTemplate(2), "config-version");

        assertFalse(result.migrated());
        assertEquals(2, result.currentVersion());
        try (Stream<Path> files = Files.list(this.tempDir)) {
            assertEquals(0, files.filter(path -> path.getFileName().toString().contains(".bak-")).count());
        }
    }

    @Test
    void doesNotDowngradeHigherVersionFile() throws Exception {
        Path file = this.tempDir.resolve("config.yml");
        Files.writeString(
            file,
            """
            config-version: 5
            settings:
              timezone: "UTC"
            """
        );

        YamlMigrationResult result = new YamlFileMigrator(null).migrate(file.toFile(), latestTemplate(2), "config-version");

        assertFalse(result.migrated());
        assertEquals(5, load(file).getInt("config-version"));
    }

    private static YamlConfiguration latestTemplate(int version) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().parseComments(true);
        yaml.set("config-version", version);
        yaml.set("settings.timezone", "America/New_York");
        yaml.set("new-path", "default");
        return yaml;
    }

    private static YamlConfiguration load(Path file) throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().parseComments(true);
        yaml.load(file.toFile());
        return yaml;
    }
}
