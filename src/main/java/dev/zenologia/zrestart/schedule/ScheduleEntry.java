package dev.zenologia.zrestart.schedule;

import java.time.LocalTime;

public record ScheduleEntry(ScheduleDay day, int hour, int minute, String reason, String raw) {
    public LocalTime localTime() {
        return LocalTime.of(this.hour, this.minute);
    }

    public boolean hasReason() {
        return this.reason != null && !this.reason.isBlank();
    }
}
