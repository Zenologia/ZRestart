package dev.zenologia.zrestart.config;

import java.io.File;

public record YamlMigrationResult(boolean migrated, int previousVersion, int currentVersion, File backupFile) {
    public static YamlMigrationResult unchanged(int currentVersion) {
        return new YamlMigrationResult(false, currentVersion, currentVersion, null);
    }

    public static YamlMigrationResult created(int currentVersion) {
        return new YamlMigrationResult(false, currentVersion, currentVersion, null);
    }

    public static YamlMigrationResult migrated(int previousVersion, int currentVersion, File backupFile) {
        return new YamlMigrationResult(true, previousVersion, currentVersion, backupFile);
    }
}
