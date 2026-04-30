package dev.zenologia.zrestart.config;

import dev.zenologia.zrestart.ZRestartPlugin;
import dev.zenologia.zrestart.placeholders.PlaceholderContext;
import dev.zenologia.zrestart.time.DurationParser;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ConfigManager {
    private static final Duration DEFAULT_BOSS_BAR_SHOW_FROM = Duration.ofMinutes(5);
    private static final BossBar.Color DEFAULT_BOSS_BAR_COLOR = BossBar.Color.RED;
    private static final BossBar.Overlay DEFAULT_BOSS_BAR_OVERLAY = BossBar.Overlay.PROGRESS;
    private static final SoundCategory DEFAULT_SOUND_CATEGORY = SoundCategory.MASTER;
    private static final float DEFAULT_SOUND_VOLUME = 1.0F;
    private static final float DEFAULT_SOUND_PITCH = 1.0F;

    private final ZRestartPlugin plugin;
    private final DurationParser durationParser;
    private RestartConfig currentConfig;

    public ConfigManager(ZRestartPlugin plugin, DurationParser durationParser) {
        this.plugin = plugin;
        this.durationParser = durationParser;
    }

    public void ensureDefaults() {
        if (!this.plugin.getDataFolder().exists() && !this.plugin.getDataFolder().mkdirs()) {
            throw new IllegalStateException("Could not create plugin data folder.");
        }
        YamlFileMigrator migrator = new YamlFileMigrator(this.plugin);
        migrateResource(migrator, "config.yml", "config-version");
        migrateResource(migrator, "messages.yml", "messages-version");
    }

    public ConfigLoadResult loadCandidate() throws IOException, InvalidConfigurationException {
        File file = new File(this.plugin.getDataFolder(), "config.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.load(file);
        return buildSnapshot(config);
    }

    public void apply(RestartConfig config) {
        this.currentConfig = config;
    }

    public RestartConfig currentConfig() {
        return this.currentConfig;
    }

    private ConfigLoadResult buildSnapshot(YamlConfiguration yaml) {
        List<ConfigWarning> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        ZoneResult zone = resolveZone(
            yaml.getString("settings.timezone", "America/New_York"),
            yaml.getString("settings.fallback-timezone", "UTC"),
            warnings,
            errors
        );

        List<Duration> warningTimes = parseWarningTimes(yaml.getStringList("countdown.warning-times"), warnings);
        RestartConfig.Chat chat = new RestartConfig.Chat(yaml.getBoolean("countdown.chat.enabled", true));
        RestartConfig.Title title = new RestartConfig.Title(
            yaml.getBoolean("countdown.title.enabled", false),
            Math.max(0, yaml.getInt("countdown.title.fade-in", 10)),
            Math.max(0, yaml.getInt("countdown.title.stay", 40)),
            Math.max(0, yaml.getInt("countdown.title.fade-out", 10))
        );
        RestartConfig.BossBarSettings bossBar = parseBossBar(yaml, warnings);
        RestartConfig.SoundSettings sounds = parseSounds(yaml, warningTimes, warnings);

        if (!chat.enabled() && !title.enabled() && !bossBar.enabled() && !soundChannelActive(sounds)) {
            errors.add("At least one countdown display channel must be enabled.");
        }

        RestartConfig config = new RestartConfig(
            new RestartConfig.Settings(
                zone.requestedTimezone(),
                yaml.getString("settings.fallback-timezone", "UTC"),
                zone.zoneId(),
                yaml.getBoolean("settings.check-restart-script", true),
                yaml.getBoolean("settings.papi-placeholders", true)
            ),
            new RestartConfig.Schedule(
                yaml.getBoolean("schedule.enabled", true),
                List.copyOf(yaml.getStringList("schedule.entries"))
            ),
            new RestartConfig.Countdown(warningTimes, chat, title, bossBar, sounds),
            new RestartConfig.Formatting(
                yaml.getBoolean("formatting.minimessage", true),
                yaml.getBoolean("formatting.legacy-ampersand-colors", true),
                yaml.getString("formatting.empty-reason", "No reason provided"),
                new RestartConfig.TimeFormat(
                    yaml.getBoolean("formatting.time-format.include-days", true),
                    yaml.getBoolean("formatting.time-format.include-hours", true),
                    yaml.getBoolean("formatting.time-format.include-minutes", true),
                    yaml.getBoolean("formatting.time-format.include-seconds", true),
                    yaml.getBoolean("formatting.time-format.compact", false)
                )
            ),
            new RestartConfig.PreRestartCommands(
                yaml.getBoolean("pre-restart-commands.enabled", false),
                parseFailureBehavior(yaml.getString("pre-restart-commands.failure-behavior", "CONTINUE"), warnings),
                List.copyOf(yaml.getStringList("pre-restart-commands.commands"))
            )
        );

        return new ConfigLoadResult(config, List.copyOf(warnings), List.copyOf(errors));
    }

    private List<Duration> parseWarningTimes(List<String> rawTimes, List<ConfigWarning> warnings) {
        Set<Long> seconds = new TreeSet<>(Comparator.reverseOrder());
        for (String raw : rawTimes) {
            DurationParser.ParseResult parsed = this.durationParser.parse(raw);
            if (parsed.successful()) {
                seconds.add(parsed.duration().toSeconds());
            } else {
                warnings.add(new ConfigWarning(
                    "console.invalid-warning-time",
                    PlaceholderContext.builder()
                        .put("entry", raw)
                        .put("error", parsed.error())
                        .build()
                ));
            }
        }
        return seconds.stream()
            .map(Duration::ofSeconds)
            .toList();
    }

    private RestartConfig.BossBarSettings parseBossBar(YamlConfiguration yaml, List<ConfigWarning> warnings) {
        BossBar.Color color = parseBossBarColor(yaml.getString("countdown.boss-bar.color", "RED"), warnings);
        BossBar.Overlay overlay = parseBossBarOverlay(yaml.getString("countdown.boss-bar.overlay", "PROGRESS"), warnings);
        Duration showFrom = parseBossBarShowFrom(yaml.getString("countdown.boss-bar.show-from", "5m"), warnings);
        return new RestartConfig.BossBarSettings(
            yaml.getBoolean("countdown.boss-bar.enabled", false),
            color,
            overlay,
            showFrom,
            yaml.getBoolean("countdown.boss-bar.progress", true)
        );
    }

    private BossBar.Color parseBossBarColor(String raw, List<ConfigWarning> warnings) {
        try {
            return BossBar.Color.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            warnings.add(new ConfigWarning(
                "console.invalid-boss-bar-color",
                PlaceholderContext.builder()
                    .put("entry", raw)
                    .put("error", DEFAULT_BOSS_BAR_COLOR.name())
                    .build()
            ));
            return DEFAULT_BOSS_BAR_COLOR;
        }
    }

    private BossBar.Overlay parseBossBarOverlay(String raw, List<ConfigWarning> warnings) {
        try {
            return BossBar.Overlay.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            warnings.add(new ConfigWarning(
                "console.invalid-boss-bar-overlay",
                PlaceholderContext.builder()
                    .put("entry", raw)
                    .put("error", DEFAULT_BOSS_BAR_OVERLAY.name())
                    .build()
            ));
            return DEFAULT_BOSS_BAR_OVERLAY;
        }
    }

    private Duration parseBossBarShowFrom(String raw, List<ConfigWarning> warnings) {
        DurationParser.ParseResult parsed = this.durationParser.parse(raw);
        if (parsed.successful()) {
            return parsed.duration();
        }
        warnings.add(new ConfigWarning(
            "console.invalid-warning-time",
            PlaceholderContext.builder()
                .put("entry", raw)
                .put("error", parsed.error())
                .build()
        ));
        return DEFAULT_BOSS_BAR_SHOW_FROM;
    }

    private RestartConfig.SoundSettings parseSounds(
        YamlConfiguration yaml,
        List<Duration> warningTimes,
        List<ConfigWarning> warnings
    ) {
        boolean enabled = yaml.getBoolean("countdown.sounds.enabled", false);
        SoundCategory category = parseSoundCategory(yaml.getString("countdown.sounds.category", "MASTER"), warnings);
        Set<Long> configuredWarningSeconds = warningSeconds(warningTimes);
        List<RestartConfig.SoundEntry> entries = parseSoundEntries(yaml.getMapList("countdown.sounds.entries"), configuredWarningSeconds, warnings);

        if (enabled && entries.isEmpty()) {
            warnings.add(new ConfigWarning("console.no-valid-sound-entries", PlaceholderContext.empty()));
        }

        return new RestartConfig.SoundSettings(enabled, category, List.copyOf(entries));
    }

    private SoundCategory parseSoundCategory(String raw, List<ConfigWarning> warnings) {
        try {
            return SoundCategory.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            warnings.add(new ConfigWarning(
                "console.invalid-sound-category",
                PlaceholderContext.builder()
                    .put("entry", raw)
                    .put("error", DEFAULT_SOUND_CATEGORY.name())
                    .build()
            ));
            return DEFAULT_SOUND_CATEGORY;
        }
    }

    private List<RestartConfig.SoundEntry> parseSoundEntries(
        List<Map<?, ?>> rawEntries,
        Set<Long> configuredWarningSeconds,
        List<ConfigWarning> warnings
    ) {
        List<RestartConfig.SoundEntry> entries = new ArrayList<>();
        for (Map<?, ?> rawEntry : rawEntries) {
            String entryText = String.valueOf(rawEntry);
            String rawTime = stringValue(rawEntry, "time");
            String rawSound = stringValue(rawEntry, "sound");

            if (rawTime == null || rawTime.isBlank() || rawSound == null || rawSound.isBlank()) {
                warnings.add(new ConfigWarning(
                    "console.invalid-sound-entry",
                    PlaceholderContext.builder()
                        .put("entry", entryText)
                        .put("error", "Each sound entry needs time and sound.")
                        .build()
                ));
                continue;
            }

            DurationParser.ParseResult parsedTime = this.durationParser.parse(rawTime);
            if (!parsedTime.successful()) {
                warnings.add(new ConfigWarning(
                    "console.invalid-sound-entry",
                    PlaceholderContext.builder()
                        .put("entry", entryText)
                        .put("error", parsedTime.error())
                        .build()
                ));
                continue;
            }

            long seconds = parsedTime.duration().toSeconds();
            if (!configuredWarningSeconds.contains(seconds)) {
                warnings.add(new ConfigWarning(
                    "console.sound-time-not-configured",
                    PlaceholderContext.builder()
                        .put("entry", rawTime)
                        .build()
                ));
                continue;
            }

            RestartConfig.SoundReference sound = SoundResolver.resolve(rawSound).orElse(null);
            if (sound == null) {
                warnings.add(new ConfigWarning(
                    "console.invalid-sound-entry",
                    PlaceholderContext.builder()
                        .put("entry", entryText)
                        .put("error", "Unknown Bukkit sound name or invalid namespaced key.")
                        .build()
                ));
                continue;
            }

            entries.add(new RestartConfig.SoundEntry(
                parsedTime.duration(),
                sound,
                parseSoundFloat(rawEntry, "volume", DEFAULT_SOUND_VOLUME, warnings),
                parseSoundFloat(rawEntry, "pitch", DEFAULT_SOUND_PITCH, warnings)
            ));
        }
        return entries;
    }

    private Set<Long> warningSeconds(List<Duration> warningTimes) {
        Set<Long> seconds = new TreeSet<>();
        for (Duration warningTime : warningTimes) {
            seconds.add(warningTime.toSeconds());
        }
        return seconds;
    }

    private String stringValue(Map<?, ?> entry, String key) {
        Object value = entry.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private float parseSoundFloat(Map<?, ?> entry, String key, float fallback, List<ConfigWarning> warnings) {
        Object raw = entry.get(key);
        if (raw == null || String.valueOf(raw).isBlank()) {
            return fallback;
        }

        float parsed;
        if (raw instanceof Number number) {
            parsed = number.floatValue();
        } else {
            try {
                parsed = Float.parseFloat(String.valueOf(raw));
            } catch (NumberFormatException ex) {
                return warnAndFallbackSoundFloat(raw, key, fallback, warnings);
            }
        }

        if (Float.isNaN(parsed) || Float.isInfinite(parsed) || parsed < 0.0F || ("pitch".equals(key) && parsed <= 0.0F)) {
            return warnAndFallbackSoundFloat(raw, key, fallback, warnings);
        }
        return parsed;
    }

    private float warnAndFallbackSoundFloat(Object raw, String key, float fallback, List<ConfigWarning> warnings) {
        warnings.add(new ConfigWarning(
            "console.invalid-sound-number",
            PlaceholderContext.builder()
                .put("field", key)
                .put("entry", raw)
                .put("error", fallback)
                .build()
        ));
        return fallback;
    }

    private boolean soundChannelActive(RestartConfig.SoundSettings sounds) {
        return sounds.enabled() && !sounds.entries().isEmpty();
    }

    private RestartConfig.FailureBehavior parseFailureBehavior(String raw, List<ConfigWarning> warnings) {
        try {
            return RestartConfig.FailureBehavior.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            warnings.add(new ConfigWarning(
                "console.invalid-pre-command-behavior",
                PlaceholderContext.builder()
                    .put("entry", raw)
                    .put("error", RestartConfig.FailureBehavior.CONTINUE.name())
                    .build()
            ));
            return RestartConfig.FailureBehavior.CONTINUE;
        }
    }

    private ZoneResult resolveZone(String requestedTimezone, String fallbackTimezone, List<ConfigWarning> warnings, List<String> errors) {
        try {
            ZoneId zoneId = ZoneId.of(requestedTimezone);
            return new ZoneResult(requestedTimezone, zoneId);
        } catch (ZoneRulesException ex) {
            try {
                ZoneId fallback = ZoneId.of(fallbackTimezone);
                warnings.add(new ConfigWarning(
                    "console.invalid-timezone",
                    PlaceholderContext.builder()
                        .put("entry", requestedTimezone)
                        .put("timezone", fallback.getId())
                        .build()
                ));
                return new ZoneResult(fallback.getId(), fallback);
            } catch (ZoneRulesException fallbackError) {
                errors.add("Configured timezone and fallback timezone are both invalid.");
                return new ZoneResult("UTC", ZoneId.of("UTC"));
            }
        }
    }

    private void migrateResource(YamlFileMigrator migrator, String resource, String versionPath) {
        try {
            YamlMigrationResult result = migrator.migrateResource(resource, versionPath);
            if (result.migrated()) {
                this.plugin.getLogger().info(
                    "Updated " + resource + " from version " + result.previousVersion()
                        + " to " + result.currentVersion() + ". Backup: " + result.backupFile().getName()
                );
            }
        } catch (IOException | InvalidConfigurationException ex) {
            this.plugin.getLogger().warning("Could not auto-update " + resource + ": " + ex.getMessage());
        }
    }

    private record ZoneResult(String requestedTimezone, ZoneId zoneId) {
    }
}
