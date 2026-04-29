package dev.zenologia.zrestart.restart;

import dev.zenologia.zrestart.ZRestartPlugin;
import dev.zenologia.zrestart.config.RestartConfig;
import dev.zenologia.zrestart.countdown.CountdownState;
import dev.zenologia.zrestart.display.DisplayPlaceholders;
import dev.zenologia.zrestart.placeholders.PlaceholderContext;
import dev.zenologia.zrestart.time.TimeFormatter;
import dev.zenologia.zrestart.util.TextRenderer;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.World;

public final class RestartExecutor {
    private final ZRestartPlugin plugin;
    private final TextRenderer renderer;
    private final TimeFormatter timeFormatter;
    private final AtomicBoolean executing = new AtomicBoolean(false);

    public RestartExecutor(ZRestartPlugin plugin, TextRenderer renderer, TimeFormatter timeFormatter) {
        this.plugin = plugin;
        this.renderer = renderer;
        this.timeFormatter = timeFormatter;
    }

    public boolean execute(CountdownState state) {
        if (!this.executing.compareAndSet(false, true)) {
            return false;
        }

        RestartConfig config = this.plugin.currentConfig();
        PlaceholderContext placeholders = DisplayPlaceholders.countdown(
            state,
            Duration.ZERO,
            config,
            this.timeFormatter
        );

        if (!runPreRestartCommands(config, placeholders)) {
            this.executing.set(false);
            return false;
        }

        Bukkit.savePlayers();
        for (World world : Bukkit.getWorlds()) {
            world.save();
        }

        this.renderer.console("restart.executing-console", placeholders);
        this.renderer.broadcast("restart.executing-broadcast", placeholders);
        Bukkit.getScheduler().runTask(this.plugin, () -> Bukkit.restart());
        return true;
    }

    private boolean runPreRestartCommands(RestartConfig config, PlaceholderContext basePlaceholders) {
        RestartConfig.PreRestartCommands preCommands = config.preRestartCommands();
        if (!preCommands.enabled()) {
            return true;
        }

        for (String command : preCommands.commands()) {
            if (command == null || command.isBlank()) {
                continue;
            }
            try {
                boolean dispatched = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                if (!dispatched && !handleCommandFailure(config, basePlaceholders, command, "Command returned false.")) {
                    return false;
                }
            } catch (RuntimeException ex) {
                if (!handleCommandFailure(config, basePlaceholders, command, ex.getMessage())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean handleCommandFailure(RestartConfig config, PlaceholderContext basePlaceholders, String command, String error) {
        PlaceholderContext placeholders = basePlaceholders
            .with("command", command)
            .with("error", error == null || error.isBlank() ? "Unknown error" : error);
        this.renderer.console("restart.pre-command-failed", placeholders);

        if (config.preRestartCommands().failureBehavior() == RestartConfig.FailureBehavior.CONTINUE) {
            return true;
        }

        this.renderer.broadcast("restart.pre-command-aborted", placeholders);
        return false;
    }

    public boolean executing() {
        return this.executing.get();
    }
}
