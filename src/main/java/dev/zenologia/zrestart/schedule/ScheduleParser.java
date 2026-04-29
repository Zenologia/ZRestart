package dev.zenologia.zrestart.schedule;

public final class ScheduleParser {
    public ScheduleParseResult parse(String rawEntry) {
        if (rawEntry == null || rawEntry.trim().isEmpty()) {
            return ScheduleParseResult.failure("Schedule entry is blank.");
        }

        String[] parts = rawEntry.split(";", 4);
        if (parts.length < 3) {
            return ScheduleParseResult.failure("Expected DAY;HOUR;MINUTE or DAY;HOUR;MINUTE;REASON.");
        }

        ScheduleDay day = ScheduleDay.parse(parts[0])
            .orElse(null);
        if (day == null) {
            return ScheduleParseResult.failure("Day must be Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday, or Daily.");
        }

        int hour;
        int minute;
        try {
            hour = Integer.parseInt(parts[1].trim());
            minute = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException ex) {
            return ScheduleParseResult.failure("Hour and minute must be whole numbers.");
        }

        if (hour < 0 || hour > 23) {
            return ScheduleParseResult.failure("Hour must be between 0 and 23.");
        }
        if (minute < 0 || minute > 59) {
            return ScheduleParseResult.failure("Minute must be between 0 and 59.");
        }

        String reason = parts.length == 4 ? parts[3].trim() : "";
        return ScheduleParseResult.success(new ScheduleEntry(day, hour, minute, reason, rawEntry));
    }
}
