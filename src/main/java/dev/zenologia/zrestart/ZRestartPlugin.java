package dev.zenologia.zrestart;

import dev.zenologia.zrestart.config.ConfigLoadResult;
import dev.zenologia.zrestart.config.ConfigManager;
import dev.zenologia.zrestart.config.ConfigWarning;
import dev.zenologia.zrestart.config.MessageManager;
import dev.zenologia.zrestart.config.ReloadDecision;
import dev.zenologia.zrestart.config.ReloadDecider;
import dev.zenologia.zrestart.config.RestartConfig;
import dev.zenologia.zrestart.countdown.CountdownManager;
import dev.zenologia.zrestart.countdown.WarningDispatcher;
import dev.zenologia.zrestart.display.BossBarDisplay;
import dev.zenologia.zrestart.display.ChatDisplay;
import dev.zenologia.zrestart.display.DisplayChannel;
import dev.zenologia.zrestart.display.DisplayPlaceholders;
import dev.zenologia.zrestart.display.SoundDisplay;
import dev.zenologia.zrestart.display.TitleDisplay;
import dev.zenologia.zrestart.placeholders.PlaceholderContext;
import dev.zenologia.zrestart.placeholders.PlaceholderService;
import dev.zenologia.zrestart.restart.RestartExecutor;
import dev.zenologia.zrestart.restart.RestartScriptChecker;
import dev.zenologia.zrestart.schedule.NextRestart;
import dev.zenologia.zrestart.schedule.NextRestartCalculator;
import dev.zenologia.zrestart.schedule.ScheduleParser;
import dev.zenologia.zrestart.schedule.ScheduleService;
import dev.zenologia.zrestart.time.DurationParser;
import dev.zenologia.zrestart.time.TimeFormatter;
import dev.zenologia.zrestart.util.TextRenderer;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZRestartPlugin extends JavaPlugin {
    private static final DateTimeFormatter RESTART_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static ZRestartPlugin instance;

    private final DurationParser durationParser = new DurationParser();
    private ConfigManager configManager;
    private MessageManager messageManager;
    private PlaceholderService placeholderService;
    private TextRenderer renderer;
    private TimeFormatter timeFormatter;
    private WarningDispatcher warningDispatcher;
    private CountdownManager countdownManager;
    private ScheduleService scheduleService;
    private RestartScriptChecker restartScriptChecker;
    private List<DisplayChannel> displayChannels = List.of();
    private RestartConfig currentConfig;
    private boolean enabled;

    public static ZRestartPlugin instance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            this.configManager = new ConfigManager(this, this.durationParser);
            this.configManager.ensureDefaults();

            this.messageManager = new MessageManager(this);
            this.messageManager.loadInitial();

            ConfigLoadResult configLoad = this.configManager.loadCandidate();
            this.currentConfig = configLoad.config();
            this.configManager.apply(this.currentConfig);

            this.placeholderService = new PlaceholderService(this);
            this.placeholderService.refresh(this.currentConfig);
            this.timeFormatter = new TimeFormatter(this::currentConfig);
            this.renderer = new TextRenderer(this, this.messageManager, this.placeholderService, this::currentConfig);

            if (!configLoad.successful()) {
                logConfigWarnings(configLoad.warnings());
                getLogger().severe("ZRestart configuration is not safe to start: " + configLoad.errorSummary());
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            initializeRuntime(configLoad.warnings());
            this.enabled = true;
        } catch (Exception ex) {
            getLogger().severe("ZRestart failed to start: " + ex.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        this.enabled = false;
        if (this.countdownManager != null) {
            this.countdownManager.stopTicker();
        }
        if (this.renderer != null) {
            this.renderer.console("console.disabled", PlaceholderContext.empty());
        }
        if (instance == this) {
            instance = null;
        }
    }

    private void initializeRuntime(List<ConfigWarning> configWarnings) {
        this.warningDispatcher = new WarningDispatcher();

        ChatDisplay chatDisplay = new ChatDisplay(this.renderer, this::currentConfig, this.timeFormatter);
        TitleDisplay titleDisplay = new TitleDisplay(this.messageManager, this.renderer, this::currentConfig, this.timeFormatter);
        BossBarDisplay bossBarDisplay = new BossBarDisplay(this.messageManager, this.renderer, this::currentConfig, this.timeFormatter);
        SoundDisplay soundDisplay = new SoundDisplay(this::currentConfig);
        Bukkit.getPluginManager().registerEvents(bossBarDisplay, this);

        this.displayChannels = List.of(chatDisplay, titleDisplay, bossBarDisplay, soundDisplay);
        this.warningDispatcher.configure(this.currentConfig, this.displayChannels);

        RestartExecutor restartExecutor = new RestartExecutor(this, this.renderer, this.timeFormatter);
        this.countdownManager = new CountdownManager(this, this.warningDispatcher, restartExecutor);
        this.scheduleService = new ScheduleService(new ScheduleParser(), new NextRestartCalculator(), this.renderer);
        this.restartScriptChecker = new RestartScriptChecker(this, this.renderer);

        logConfigWarnings(configWarnings);
        logPlaceholderStatus();

        this.scheduleService.reload(this.currentConfig);
        this.scheduleService.scheduleNext(this.countdownManager, Instant.now());
        this.countdownManager.startTicker();

        if (this.currentConfig.settings().checkRestartScript()) {
            this.restartScriptChecker.check();
        }

        logLoadedMessage();
    }

    public void reloadFromCommand(CommandSender sender) {
        try {
            YamlConfiguration candidateMessages = this.messageManager.loadCandidate();
            ConfigLoadResult configLoad = this.configManager.loadCandidate();
            ReloadDecision decision = new ReloadDecider().decide(this.currentConfig, configLoad);
            if (!decision.accepted()) {
                throw new InvalidConfigurationException(decision.error());
            }

            this.messageManager.apply(candidateMessages);
            this.currentConfig = decision.activeConfig();
            this.configManager.apply(this.currentConfig);
            this.placeholderService.refresh(this.currentConfig);

            this.warningDispatcher.clear();
            logConfigWarnings(configLoad.warnings());
            logPlaceholderStatus();
            this.warningDispatcher.configure(this.currentConfig, this.displayChannels);
            this.scheduleService.reload(this.currentConfig);
            Instant now = Instant.now();
            this.scheduleService.recalculate(now);

            if (this.countdownManager.manualActive()) {
                this.countdownManager.reapplyWarningThresholds();
            } else {
                this.countdownManager.clear();
                this.scheduleService.nextRestart().ifPresent(this.countdownManager::startScheduled);
            }

            if (this.currentConfig.settings().checkRestartScript()) {
                this.restartScriptChecker.check();
            }

            this.renderer.send(sender, "reload.success", PlaceholderContext.empty());
        } catch (Exception ex) {
            String error = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            PlaceholderContext placeholders = PlaceholderContext.builder().put("error", error).build();
            this.renderer.send(sender, "reload.kept-old-config", placeholders);
            this.renderer.console("console.reload-kept-old-config", placeholders);
        }
    }

    private void logConfigWarnings(List<ConfigWarning> warnings) {
        for (ConfigWarning warning : warnings) {
            this.renderer.console(warning.messagePath(), warning.placeholders());
        }
    }

    private void logPlaceholderStatus() {
        if (!this.currentConfig.settings().papiPlaceholders()) {
            return;
        }
        this.renderer.console(
            this.placeholderService.available() ? "console.papi-hooked" : "console.papi-missing",
            PlaceholderContext.empty()
        );
    }

    private void logLoadedMessage() {
        Instant now = Instant.now();
        this.scheduleService.nextRestart()
            .ifPresentOrElse(
                next -> this.renderer.console("console.loaded", nextRestartPlaceholders(next, now)),
                () -> this.renderer.console("console.loaded-no-schedule", PlaceholderContext.empty())
            );
    }

    public PlaceholderContext nextRestartPlaceholders(NextRestart next, Instant now) {
        Duration remaining = next.remainingFrom(now);
        String formatted = this.timeFormatter.format(remaining);
        return PlaceholderContext.builder()
            .put("time", formatted)
            .put("time_formatted", formatted)
            .put("seconds", Math.max(0L, remaining.toSeconds()))
            .put("reason", DisplayPlaceholders.normalizeReason(next.entry().reason(), this.currentConfig))
            .put("restart_time", RESTART_TIME_FORMAT.format(next.time()))
            .put("restart_day", prettyDay(next.time().getDayOfWeek().toString()))
            .put("timezone", this.currentConfig.settings().timezone())
            .build();
    }

    private static String prettyDay(String day) {
        String lower = day.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    public boolean enabled() {
        return this.enabled;
    }

    public RestartConfig currentConfig() {
        return this.currentConfig;
    }

    public TextRenderer renderer() {
        return this.renderer;
    }

    public TimeFormatter timeFormatter() {
        return this.timeFormatter;
    }

    public DurationParser durationParser() {
        return this.durationParser;
    }

    public CountdownManager countdownManager() {
        return this.countdownManager;
    }

    public ScheduleService scheduleService() {
        return this.scheduleService;
    }
}
