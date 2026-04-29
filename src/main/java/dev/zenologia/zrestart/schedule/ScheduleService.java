package dev.zenologia.zrestart.schedule;

import dev.zenologia.zrestart.config.RestartConfig;
import dev.zenologia.zrestart.countdown.CountdownManager;
import dev.zenologia.zrestart.placeholders.PlaceholderContext;
import dev.zenologia.zrestart.util.TextRenderer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ScheduleService {
    private final ScheduleParser parser;
    private final NextRestartCalculator calculator;
    private final TextRenderer renderer;
    private RestartConfig config;
    private List<ScheduleEntry> validEntries = List.of();
    private Optional<NextRestart> nextRestart = Optional.empty();

    public ScheduleService(ScheduleParser parser, NextRestartCalculator calculator, TextRenderer renderer) {
        this.parser = parser;
        this.calculator = calculator;
        this.renderer = renderer;
    }

    public void reload(RestartConfig config) {
        this.config = config;
        this.validEntries = parseEntries(config.schedule().entries());
        if (config.schedule().enabled() && this.validEntries.isEmpty()) {
            this.renderer.console("console.no-valid-schedules", PlaceholderContext.empty());
        }
    }

    public Optional<NextRestart> recalculate(Instant now) {
        if (this.config == null || !this.config.schedule().enabled() || this.validEntries.isEmpty()) {
            this.nextRestart = Optional.empty();
            return this.nextRestart;
        }
        this.nextRestart = this.calculator.calculate(this.validEntries, now, this.config.settings().zoneId());
        this.nextRestart.ifPresent(this::logDstAdjustments);
        return this.nextRestart;
    }

    public Optional<NextRestart> recalculateAfter(Instant now, Instant after) {
        if (this.config == null || !this.config.schedule().enabled() || this.validEntries.isEmpty()) {
            this.nextRestart = Optional.empty();
            return this.nextRestart;
        }
        this.nextRestart = this.calculator.calculateAfter(this.validEntries, now, after, this.config.settings().zoneId());
        this.nextRestart.ifPresent(this::logDstAdjustments);
        return this.nextRestart;
    }

    public void scheduleNext(CountdownManager countdownManager, Instant now) {
        recalculate(now).ifPresent(next -> {
            if (!countdownManager.manualActive()) {
                countdownManager.startScheduled(next);
            }
        });
    }

    public Optional<NextRestart> nextRestart() {
        return this.nextRestart;
    }

    public List<ScheduleEntry> validEntries() {
        return this.validEntries;
    }

    private List<ScheduleEntry> parseEntries(List<String> rawEntries) {
        List<ScheduleEntry> parsed = new ArrayList<>();
        for (String raw : rawEntries) {
            ScheduleParseResult result = this.parser.parse(raw);
            if (result.successful()) {
                parsed.add(result.entry());
            } else {
                this.renderer.console(
                    "console.invalid-schedule-entry",
                    PlaceholderContext.builder()
                        .put("entry", raw)
                        .put("error", result.error())
                        .build()
                );
            }
        }
        return List.copyOf(parsed);
    }

    private void logDstAdjustments(NextRestart nextRestart) {
        for (DstAdjustment adjustment : nextRestart.adjustments()) {
            this.renderer.console(
                "console.dst-adjustment",
                PlaceholderContext.builder()
                    .put("entry", adjustment.entry().raw())
                    .put("error", adjustment.detail())
                    .build()
            );
        }
    }
}
