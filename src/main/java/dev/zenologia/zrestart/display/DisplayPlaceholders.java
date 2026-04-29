package dev.zenologia.zrestart.display;

import dev.zenologia.zrestart.config.RestartConfig;
import dev.zenologia.zrestart.countdown.CountdownState;
import dev.zenologia.zrestart.placeholders.PlaceholderContext;
import dev.zenologia.zrestart.time.TimeFormatter;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DisplayPlaceholders {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private DisplayPlaceholders() {
    }

    public static PlaceholderContext countdown(
        CountdownState state,
        Duration remaining,
        RestartConfig config,
        TimeFormatter formatter
    ) {
        ZoneId zoneId = config.settings().zoneId();
        var restartTime = state.target().atZone(zoneId);
        String formatted = formatter.format(remaining);
        return PlaceholderContext.builder()
            .put("time", formatted)
            .put("time_formatted", formatted)
            .put("seconds", Math.max(0L, remaining.toSeconds()))
            .put("reason", normalizeReason(state.reason(), config))
            .put("restart_time", TIME_FORMATTER.format(restartTime))
            .put("restart_day", prettyDay(restartTime.getDayOfWeek().toString()))
            .put("timezone", config.settings().timezone())
            .build();
    }

    public static String normalizeReason(String reason, RestartConfig config) {
        if (reason == null || reason.isBlank()) {
            return config.formatting().emptyReason();
        }
        return reason;
    }

    private static String prettyDay(String day) {
        String lower = day.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
