package dev.zenologia.zrestart.schedule;

public record ScheduleParseResult(boolean successful, ScheduleEntry entry, String error) {
    public static ScheduleParseResult success(ScheduleEntry entry) {
        return new ScheduleParseResult(true, entry, "");
    }

    public static ScheduleParseResult failure(String error) {
        return new ScheduleParseResult(false, null, error);
    }
}
