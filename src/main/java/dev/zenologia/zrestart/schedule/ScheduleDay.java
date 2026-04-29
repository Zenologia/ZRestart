package dev.zenologia.zrestart.schedule;

import java.time.DayOfWeek;
import java.util.Locale;
import java.util.Optional;

public enum ScheduleDay {
    DAILY(null),
    MONDAY(DayOfWeek.MONDAY),
    TUESDAY(DayOfWeek.TUESDAY),
    WEDNESDAY(DayOfWeek.WEDNESDAY),
    THURSDAY(DayOfWeek.THURSDAY),
    FRIDAY(DayOfWeek.FRIDAY),
    SATURDAY(DayOfWeek.SATURDAY),
    SUNDAY(DayOfWeek.SUNDAY);

    private final DayOfWeek dayOfWeek;

    ScheduleDay(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public boolean daily() {
        return this == DAILY;
    }

    public DayOfWeek dayOfWeek() {
        return this.dayOfWeek;
    }

    public static Optional<ScheduleDay> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (ScheduleDay day : values()) {
            if (day.name().equals(normalized)) {
                return Optional.of(day);
            }
        }
        return Optional.empty();
    }
}
