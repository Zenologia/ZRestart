package dev.zenologia.zrestart.time;

import dev.zenologia.zrestart.config.RestartConfig;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class TimeFormatter {
    private final Supplier<RestartConfig> configSupplier;

    public TimeFormatter(Supplier<RestartConfig> configSupplier) {
        this.configSupplier = configSupplier;
    }

    public String format(Duration duration) {
        long totalSeconds = Math.max(0L, duration.toSeconds());
        RestartConfig.TimeFormat format = this.configSupplier.get().formatting().timeFormat();

        long days = totalSeconds / 86_400L;
        long remaining = totalSeconds % 86_400L;
        long hours = remaining / 3_600L;
        remaining %= 3_600L;
        long minutes = remaining / 60L;
        long seconds = remaining % 60L;

        List<String> parts = new ArrayList<>();
        add(parts, days, "d", "day", format.includeDays(), format.compact());
        add(parts, hours, "h", "hour", format.includeHours(), format.compact());
        add(parts, minutes, "m", "minute", format.includeMinutes(), format.compact());
        add(parts, seconds, "s", "second", format.includeSeconds(), format.compact());

        if (parts.isEmpty()) {
            return format.compact() ? "0s" : "0 seconds";
        }
        return String.join(format.compact() ? " " : ", ", parts);
    }

    private static void add(List<String> parts, long value, String compactUnit, String unit, boolean include, boolean compact) {
        if (!include || value <= 0) {
            return;
        }
        if (compact) {
            parts.add(value + compactUnit);
        } else {
            parts.add(value + " " + unit + (value == 1 ? "" : "s"));
        }
    }
}
