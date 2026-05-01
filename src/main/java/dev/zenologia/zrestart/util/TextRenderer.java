package dev.zenologia.zrestart.util;

import dev.zenologia.zrestart.ZRestartPlugin;
import dev.zenologia.zrestart.config.MessageManager;
import dev.zenologia.zrestart.config.RestartConfig;
import dev.zenologia.zrestart.placeholders.PlaceholderContext;
import dev.zenologia.zrestart.placeholders.PlaceholderService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TextRenderer {
    private static final Pattern LEGACY_COLOR = Pattern.compile("(?i)&([0-9a-fk-or])");

    private final ZRestartPlugin plugin;
    private final MessageManager messages;
    private final PlaceholderService placeholderService;
    private final Supplier<RestartConfig> configSupplier;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public TextRenderer(
        ZRestartPlugin plugin,
        MessageManager messages,
        PlaceholderService placeholderService,
        Supplier<RestartConfig> configSupplier
    ) {
        this.plugin = plugin;
        this.messages = messages;
        this.placeholderService = placeholderService;
        this.configSupplier = configSupplier;
    }

    public void send(CommandSender sender, String path, PlaceholderContext context) {
        List<String> list = this.messages.stringList(path);
        if (!list.isEmpty()) {
            for (String line : list) {
                sender.sendMessage(component(sender, line, context));
            }
            return;
        }
        sender.sendMessage(component(sender, this.messages.string(path), context));
    }

    public void sendRaw(CommandSender sender, String raw, PlaceholderContext context) {
        sender.sendMessage(component(sender, raw, context));
    }

    public void broadcast(String path, PlaceholderContext context) {
        String raw = this.messages.string(path);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(component(player, raw, context));
        }
        Bukkit.getConsoleSender().sendMessage(component(null, raw, context));
    }

    public void sendToPlayers(String path, PlaceholderContext context) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player, path, context);
        }
    }

    public void showTitleToPlayers(String titlePath, String subtitlePath, PlaceholderContext context, Title.Times times) {
        String rawTitle = this.messages.string(titlePath);
        String rawSubtitle = this.messages.string(subtitlePath);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(Title.title(
                component(player, rawTitle, context),
                component(player, rawSubtitle, context),
                times
            ));
        }
    }

    public void console(String path, PlaceholderContext context) {
        String raw = this.messages.string(path);
        String plain = plain(raw, context);
        if (!plain.isBlank()) {
            this.plugin.getLogger().info(plain);
        }
    }

    public Component component(CommandSender recipient, String raw, PlaceholderContext context) {
        String replaced = replaceInternalPlaceholders(raw, context);
        if (recipient instanceof Player player) {
            replaced = this.placeholderService.apply(player, replaced);
        }

        RestartConfig.Formatting formatting = this.configSupplier.get().formatting();
        if (formatting.legacyAmpersandColors() && formatting.miniMessage()) {
            replaced = legacyToMiniMessage(replaced);
        }

        if (formatting.miniMessage()) {
            try {
                return this.miniMessage.deserialize(replaced);
            } catch (RuntimeException ex) {
                this.plugin.getLogger().warning("Invalid MiniMessage text in messages.yml. Falling back to plain text: " + ex.getMessage());
                return Component.text(stripMiniMessage(replaced));
            }
        }

        if (formatting.legacyAmpersandColors()) {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(replaced);
        }

        return Component.text(replaced);
    }

    public String plain(String raw, PlaceholderContext context) {
        Component rendered = component(null, raw, context);
        return PlainTextComponentSerializer.plainText().serialize(rendered);
    }

    private String replaceInternalPlaceholders(String raw, PlaceholderContext context) {
        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("{prefix}", this.messages.prefix());
        placeholders.putAll(context.values());

        String output = raw == null ? "" : raw;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            output = output.replace(entry.getKey(), entry.getValue());
        }
        return output;
    }

    private static String legacyToMiniMessage(String input) {
        Matcher matcher = LEGACY_COLOR.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(legacyCodeToTag(matcher.group(1))));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String legacyCodeToTag(String code) {
        return switch (code.toLowerCase(Locale.ROOT)) {
            case "0" -> "<black>";
            case "1" -> "<dark_blue>";
            case "2" -> "<dark_green>";
            case "3" -> "<dark_aqua>";
            case "4" -> "<dark_red>";
            case "5" -> "<dark_purple>";
            case "6" -> "<gold>";
            case "7" -> "<gray>";
            case "8" -> "<dark_gray>";
            case "9" -> "<blue>";
            case "a" -> "<green>";
            case "b" -> "<aqua>";
            case "c" -> "<red>";
            case "d" -> "<light_purple>";
            case "e" -> "<yellow>";
            case "f" -> "<white>";
            case "k" -> "<obfuscated>";
            case "l" -> "<bold>";
            case "m" -> "<strikethrough>";
            case "n" -> "<underlined>";
            case "o" -> "<italic>";
            case "r" -> "<reset>";
            default -> "";
        };
    }

    private static String stripMiniMessage(String input) {
        return input.replaceAll("<[^>]+>", "");
    }
}
