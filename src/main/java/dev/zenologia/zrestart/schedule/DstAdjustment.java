package dev.zenologia.zrestart.schedule;

import java.time.ZonedDateTime;

public record DstAdjustment(ScheduleEntry entry, ZonedDateTime adjustedTime, String detail) {
}
