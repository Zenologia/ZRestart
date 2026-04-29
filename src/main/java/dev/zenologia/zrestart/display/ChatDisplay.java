package dev.zenologia.zrestart.display;

import dev.zenologia.zrestart.config.RestartConfig;
import dev.zenologia.zrestart.countdown.CountdownState;
import dev.zenologia.zrestart.time.TimeFormatter;
import dev.zenologia.zrestart.util.TextRenderer;
import java.time.Duration;
import java.util.function.Supplier;

public final class ChatDisplay implements DisplayChannel {
    private final TextRenderer renderer;
    private final Supplier<RestartConfig> configSupplier;
    private final TimeFormatter timeFormatter;

    public ChatDisplay(TextRenderer renderer, Supplier<RestartConfig> configSupplier, TimeFormatter timeFormatter) {
        this.renderer = renderer;
        this.configSupplier = configSupplier;
        this.timeFormatter = timeFormatter;
    }

    @Override
    public void showWarning(CountdownState state, Duration remaining) {
        RestartConfig config = this.configSupplier.get();
        if (!config.countdown().chat().enabled()) {
            return;
        }
        this.renderer.sendToPlayers(
            "countdown.chat",
            DisplayPlaceholders.countdown(state, remaining, config, this.timeFormatter)
        );
    }
}
