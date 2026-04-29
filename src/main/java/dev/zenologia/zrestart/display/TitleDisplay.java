package dev.zenologia.zrestart.display;

import dev.zenologia.zrestart.config.MessageManager;
import dev.zenologia.zrestart.config.RestartConfig;
import dev.zenologia.zrestart.countdown.CountdownState;
import dev.zenologia.zrestart.placeholders.PlaceholderContext;
import dev.zenologia.zrestart.time.TimeFormatter;
import dev.zenologia.zrestart.util.TextRenderer;
import java.time.Duration;
import java.util.function.Supplier;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class TitleDisplay implements DisplayChannel {
    private static final long TICK_MILLIS = 50L;

    private final MessageManager messages;
    private final TextRenderer renderer;
    private final Supplier<RestartConfig> configSupplier;
    private final TimeFormatter timeFormatter;

    public TitleDisplay(
        MessageManager messages,
        TextRenderer renderer,
        Supplier<RestartConfig> configSupplier,
        TimeFormatter timeFormatter
    ) {
        this.messages = messages;
        this.renderer = renderer;
        this.configSupplier = configSupplier;
        this.timeFormatter = timeFormatter;
    }

    @Override
    public void showWarning(CountdownState state, Duration remaining) {
        RestartConfig config = this.configSupplier.get();
        if (!config.countdown().title().enabled()) {
            return;
        }

        RestartConfig.Title titleConfig = config.countdown().title();
        PlaceholderContext placeholders = DisplayPlaceholders.countdown(state, remaining, config, this.timeFormatter);
        Title.Times times = Title.Times.times(
            Duration.ofMillis(titleConfig.fadeInTicks() * TICK_MILLIS),
            Duration.ofMillis(titleConfig.stayTicks() * TICK_MILLIS),
            Duration.ofMillis(titleConfig.fadeOutTicks() * TICK_MILLIS)
        );
        for (Player player : Bukkit.getOnlinePlayers()) {
            Title title = Title.title(
                this.renderer.component(player, this.messages.string("countdown.title"), placeholders),
                this.renderer.component(player, this.messages.string("countdown.subtitle"), placeholders),
                times
            );
            player.showTitle(title);
        }
    }
}
