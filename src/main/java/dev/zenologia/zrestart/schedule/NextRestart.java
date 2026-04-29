package dev.zenologia.zrestart.schedule;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

public record NextRestart(ScheduleEntry entry, ZonedDateTime time, List<DstAdjustment> adjustments) {
    public Instant instant() {
        return this.time.toInstant();
    }

    public Duration remainingFrom(Instant now) {
        return Duration.between(now, this.instant());
    }
}
