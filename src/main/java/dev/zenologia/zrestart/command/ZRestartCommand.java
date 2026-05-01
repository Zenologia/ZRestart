package dev.zenologia.zrestart.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.zenologia.zrestart.ZRestartPlugin;
import dev.zenologia.zrestart.config.RestartConfig;
import dev.zenologia.zrestart.countdown.CountdownState;
import dev.zenologia.zrestart.countdown.CountdownType;
import dev.zenologia.zrestart.display.DisplayPlaceholders;
import dev.zenologia.zrestart.placeholders.PlaceholderContext;
import dev.zenologia.zrestart.schedule.NextRestart;
import dev.zenologia.zrestart.time.DurationParser;
import dev.zenologia.zrestart.util.PermissionNodes;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;

public final class ZRestartCommand {
    private static final long TICK_MILLIS = 50L;
    private static final DateTimeFormatter RESTART_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<String> SUBCOMMANDS = List.of("time", "now", "delay", "stop", "reload");
    private static final List<String> INTERVAL_EXAMPLES = List.of("30m", "15m", "5m", "1m", "30s", "1:30", "0:30", "3600", "1h30m", "1h 30m");
    private static final List<String> REASON_EXAMPLES = List.of("Maintenance", "Daily maintenance", "Weekly maintenance");

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("zrestart")
            .executes(context -> execute(context, ""))
            .then(Commands.argument("input", StringArgumentType.greedyString())
                .suggests(this::suggest)
                .executes(context -> execute(context, StringArgumentType.getString(context, "input"))))
            .build();
    }

    private int execute(CommandContext<CommandSourceStack> context, String rawInput) {
        CommandSender sender = context.getSource().getSender();
        ZRestartPlugin plugin = ZRestartPlugin.instance();
        if (plugin == null || !plugin.enabled()) {
            sender.sendMessage(Component.text("ZRestart is still loading."));
            return Command.SINGLE_SUCCESS;
        }

        String input = rawInput == null ? "" : rawInput.trim();
        if (input.isEmpty()) {
            showHelp(plugin, sender);
            return Command.SINGLE_SUCCESS;
        }

        String[] split = input.split("\\s+", 2);
        String subcommand = split[0].toLowerCase(Locale.ROOT);
        String tail = split.length > 1 ? split[1].trim() : "";

        switch (subcommand) {
            case "time" -> handleTime(plugin, sender, tail);
            case "now" -> handleNow(plugin, sender, tail);
            case "delay" -> handleDelay(plugin, sender, tail);
            case "stop" -> handleStop(plugin, sender, tail);
            case "reload" -> handleReload(plugin, sender, tail);
            default -> plugin.renderer().send(sender, "commands.unknown-subcommand", PlaceholderContext.empty());
        }
        return Command.SINGLE_SUCCESS;
    }

    private void showHelp(ZRestartPlugin plugin, CommandSender sender) {
        if (!requirePermission(plugin, sender, PermissionNodes.TIME)) {
            return;
        }
        plugin.renderer().send(sender, "help", PlaceholderContext.empty());
    }

    private void handleTime(ZRestartPlugin plugin, CommandSender sender, String tail) {
        if (!requirePermission(plugin, sender, PermissionNodes.TIME)) {
            return;
        }
        if (!tail.isBlank()) {
            invalidUsage(plugin, sender, "/zrestart time");
            return;
        }

        Instant now = Instant.now();
        Optional<CountdownState> active = plugin.countdownManager().activeState();
        if (active.isPresent()) {
            CountdownState state = active.get();
            Duration remaining = Duration.ofSeconds(state.remainingSecondsCeil(now));
            plugin.renderer().send(
                sender,
                "time.active-countdown",
                DisplayPlaceholders.countdown(state, remaining, plugin.currentConfig(), plugin.timeFormatter())
            );
            return;
        }

        Optional<NextRestart> next = plugin.scheduleService().nextRestart()
            .or(() -> plugin.scheduleService().recalculate(now));
        if (next.isEmpty()) {
            plugin.renderer().send(sender, "time.no-restart-scheduled", PlaceholderContext.empty());
            return;
        }

        plugin.renderer().send(sender, "time.next-restart", nextRestartPlaceholders(plugin, next.get(), now));
    }

    private void handleNow(ZRestartPlugin plugin, CommandSender sender, String tail) {
        if (!requirePermission(plugin, sender, PermissionNodes.ADMIN)) {
            return;
        }
        if (tail.isBlank()) {
            invalidUsage(plugin, sender, "/zrestart now <interval> [reason]");
            return;
        }

        DurationParser.LeadingParseResult parsed = plugin.durationParser().parseLeading(tail);
        if (!parsed.successful()) {
            plugin.renderer().send(
                sender,
                "now.invalid-interval",
                PlaceholderContext.builder().put("error", parsed.error()).build()
            );
            return;
        }

        String reason = parsed.remainder().isBlank()
            ? plugin.currentConfig().formatting().emptyReason()
            : parsed.remainder().trim();
        plugin.countdownManager().startManual(parsed.duration(), reason);
        CountdownState state = plugin.countdownManager().activeState().orElseThrow();
        PlaceholderContext placeholders = DisplayPlaceholders.countdown(
            state,
            parsed.duration(),
            plugin.currentConfig(),
            plugin.timeFormatter()
        );
        plugin.renderer().send(
            sender,
            "now.started",
            placeholders
        );
        showCommandPopup(plugin, "now.popup-title", "now.popup-subtitle", placeholders);
    }

    private void handleDelay(ZRestartPlugin plugin, CommandSender sender, String tail) {
        if (!requirePermission(plugin, sender, PermissionNodes.ADMIN)) {
            return;
        }
        if (tail.isBlank()) {
            invalidUsage(plugin, sender, "/zrestart delay <interval>");
            return;
        }

        DurationParser.ParseResult parsed = plugin.durationParser().parse(tail);
        if (!parsed.successful()) {
            plugin.renderer().send(
                sender,
                "delay.invalid-interval",
                PlaceholderContext.builder().put("error", parsed.error()).build()
            );
            return;
        }

        if (!plugin.countdownManager().delay(parsed.duration())) {
            plugin.renderer().send(sender, "delay.no-active-countdown", PlaceholderContext.empty());
            return;
        }

        String formatted = plugin.timeFormatter().format(parsed.duration());
        plugin.renderer().send(
            sender,
            "delay.delayed",
            PlaceholderContext.builder()
                .put("time", formatted)
                .put("time_formatted", formatted)
                .put("seconds", parsed.duration().toSeconds())
                .build()
        );
    }

    private void handleStop(ZRestartPlugin plugin, CommandSender sender, String tail) {
        if (!requirePermission(plugin, sender, PermissionNodes.ADMIN)) {
            return;
        }
        if (!tail.isBlank()) {
            invalidUsage(plugin, sender, "/zrestart stop");
            return;
        }

        Optional<CountdownState> cancelled = plugin.countdownManager().cancelActive();
        if (cancelled.isEmpty()) {
            plugin.renderer().send(sender, "stop.no-active-countdown", PlaceholderContext.empty());
            return;
        }

        Instant now = Instant.now();
        CountdownState cancelledState = cancelled.get();
        Duration remaining = Duration.ofSeconds(cancelledState.remainingSecondsCeil(now));
        PlaceholderContext placeholders = DisplayPlaceholders.countdown(
            cancelledState,
            remaining,
            plugin.currentConfig(),
            plugin.timeFormatter()
        );
        plugin.renderer().send(sender, "stop.cancelled", PlaceholderContext.empty());
        showCommandPopup(plugin, "stop.popup-title", "stop.popup-subtitle", placeholders);

        Instant after = cancelledState.type() == CountdownType.SCHEDULED ? cancelledState.target() : now;
        plugin.scheduleService()
            .recalculateAfter(now, after)
            .ifPresent(plugin.countdownManager()::startScheduled);
    }

    private void handleReload(ZRestartPlugin plugin, CommandSender sender, String tail) {
        if (!requirePermission(plugin, sender, PermissionNodes.RELOAD)) {
            return;
        }
        if (!tail.isBlank()) {
            invalidUsage(plugin, sender, "/zrestart reload");
            return;
        }
        plugin.reloadFromCommand(sender);
    }

    private boolean requirePermission(ZRestartPlugin plugin, CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        plugin.renderer().send(
            sender,
            "commands.no-permission",
            PlaceholderContext.builder().put("permission", permission).build()
        );
        return false;
    }

    private void invalidUsage(ZRestartPlugin plugin, CommandSender sender, String usage) {
        plugin.renderer().send(
            sender,
            "commands.invalid-usage",
            PlaceholderContext.builder().put("command", usage).build()
        );
    }

    private void showCommandPopup(ZRestartPlugin plugin, String titlePath, String subtitlePath, PlaceholderContext placeholders) {
        RestartConfig.Title titleConfig = plugin.currentConfig().countdown().title();
        Title.Times times = Title.Times.times(
            Duration.ofMillis(titleConfig.fadeInTicks() * TICK_MILLIS),
            Duration.ofMillis(titleConfig.stayTicks() * TICK_MILLIS),
            Duration.ofMillis(titleConfig.fadeOutTicks() * TICK_MILLIS)
        );
        plugin.renderer().showTitleToPlayers(titlePath, subtitlePath, placeholders, times);
    }

    private PlaceholderContext nextRestartPlaceholders(ZRestartPlugin plugin, NextRestart next, Instant now) {
        RestartConfig config = plugin.currentConfig();
        Duration remaining = next.remainingFrom(now);
        String formatted = plugin.timeFormatter().format(remaining);
        var restartTime = next.time();
        return PlaceholderContext.builder()
            .put("time", formatted)
            .put("time_formatted", formatted)
            .put("seconds", Math.max(0L, remaining.toSeconds()))
            .put("reason", DisplayPlaceholders.normalizeReason(next.entry().reason(), config))
            .put("restart_time", RESTART_TIME_FORMAT.format(restartTime))
            .put("restart_day", prettyDay(restartTime.getDayOfWeek().toString()))
            .put("timezone", config.settings().timezone())
            .build();
    }

    private CompletableFuture<Suggestions> suggest(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ZRestartPlugin plugin = ZRestartPlugin.instance();
        String remaining = builder.getRemaining();
        String lower = remaining.toLowerCase(Locale.ROOT);

        if (!remaining.contains(" ")) {
            suggestMatching(builder, SUBCOMMANDS, lower);
            return builder.buildFuture();
        }

        String[] split = remaining.split("\\s+", 2);
        String subcommand = split[0].toLowerCase(Locale.ROOT);
        String tail = split.length > 1 ? split[1] : "";
        if (subcommand.equals("now") || subcommand.equals("delay")) {
            List<String> suggestions = new ArrayList<>();
            for (String example : INTERVAL_EXAMPLES) {
                suggestions.add(subcommand + " " + example);
            }
            if (subcommand.equals("now") && plugin != null) {
                for (String reason : configuredReasonSuggestions(plugin)) {
                    suggestions.add("now " + firstIntervalOrDefault(tail) + " " + reason);
                }
            }
            suggestMatching(builder, suggestions, lower);
        }
        return builder.buildFuture();
    }

    private List<String> configuredReasonSuggestions(ZRestartPlugin plugin) {
        List<String> suggestions = new ArrayList<>(REASON_EXAMPLES);
        for (String raw : plugin.currentConfig().schedule().entries()) {
            String[] parts = raw.split(";", 4);
            if (parts.length == 4 && !parts[3].isBlank()) {
                suggestions.add(parts[3].trim());
            }
        }
        return suggestions.stream().distinct().toList();
    }

    private static String firstIntervalOrDefault(String tail) {
        String trimmed = tail.trim();
        if (trimmed.isEmpty()) {
            return "30m";
        }
        return trimmed.split("\\s+", 2)[0];
    }

    private static void suggestMatching(SuggestionsBuilder builder, List<String> suggestions, String lowerRemaining) {
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase(Locale.ROOT).startsWith(lowerRemaining)) {
                builder.suggest(suggestion);
            }
        }
    }

    private static String prettyDay(String day) {
        String lower = day.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
