package dev.zenologia.zrestart.time;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationParser {
    private static final Pattern RAW_SECONDS = Pattern.compile("\\d+");
    private static final Pattern HOUR_MINUTE = Pattern.compile("(\\d+):(\\d{1,2})");
    private static final Pattern READABLE_PART = Pattern.compile("(\\d+)([hmsHMS])");

    public ParseResult parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return ParseResult.failure("Duration is required.");
        }

        String trimmed = input.trim();
        Matcher rawSeconds = RAW_SECONDS.matcher(trimmed);
        if (rawSeconds.matches()) {
            return parseRawSeconds(trimmed);
        }

        Matcher hourMinute = HOUR_MINUTE.matcher(trimmed);
        if (hourMinute.matches()) {
            return parseHourMinute(hourMinute);
        }

        return parseReadable(trimmed);
    }

    public LeadingParseResult parseLeading(String input) {
        if (input == null || input.trim().isEmpty()) {
            return LeadingParseResult.failure("Duration is required.");
        }

        String trimmed = input.trim();
        String[] tokens = trimmed.split("\\s+");
        for (int i = tokens.length; i >= 1; i--) {
            String prefix = join(tokens, 0, i);
            ParseResult parsed = parse(prefix);
            if (parsed.successful()) {
                String remainder = join(tokens, i, tokens.length);
                return LeadingParseResult.success(parsed.duration(), remainder);
            }
        }

        return LeadingParseResult.failure(parse(tokens[0]).error());
    }

    private ParseResult parseRawSeconds(String value) {
        try {
            long seconds = Long.parseLong(value);
            if (seconds <= 0) {
                return ParseResult.failure("Duration must be positive.");
            }
            return ParseResult.success(Duration.ofSeconds(seconds));
        } catch (NumberFormatException ex) {
            return ParseResult.failure("Duration is too large.");
        }
    }

    private ParseResult parseHourMinute(Matcher matcher) {
        try {
            long hours = Long.parseLong(matcher.group(1));
            int minutes = Integer.parseInt(matcher.group(2));
            if (minutes < 0 || minutes > 59) {
                return ParseResult.failure("Minute value must be between 0 and 59.");
            }
            long seconds = Math.addExact(Math.multiplyExact(hours, 3600L), minutes * 60L);
            if (seconds <= 0) {
                return ParseResult.failure("Duration must be positive.");
            }
            return ParseResult.success(Duration.ofSeconds(seconds));
        } catch (NumberFormatException | ArithmeticException ex) {
            return ParseResult.failure("Duration is too large.");
        }
    }

    private ParseResult parseReadable(String value) {
        long seconds = 0L;
        int position = 0;
        int parts = 0;

        while (position < value.length()) {
            while (position < value.length() && Character.isWhitespace(value.charAt(position))) {
                position++;
            }
            if (position >= value.length()) {
                break;
            }

            Matcher matcher = READABLE_PART.matcher(value);
            matcher.region(position, value.length());
            if (!matcher.lookingAt()) {
                return ParseResult.failure("Expected readable duration parts such as 30m, 5s, or 1h30m.");
            }

            long amount;
            try {
                amount = Long.parseLong(matcher.group(1));
            } catch (NumberFormatException ex) {
                return ParseResult.failure("Duration is too large.");
            }
            if (amount <= 0) {
                return ParseResult.failure("Duration parts must be positive.");
            }

            long multiplier = switch (matcher.group(2).toLowerCase(Locale.ROOT)) {
                case "h" -> 3600L;
                case "m" -> 60L;
                case "s" -> 1L;
                default -> throw new IllegalStateException("Unsupported duration unit.");
            };

            try {
                seconds = Math.addExact(seconds, Math.multiplyExact(amount, multiplier));
            } catch (ArithmeticException ex) {
                return ParseResult.failure("Duration is too large.");
            }
            parts++;
            position = matcher.end();
        }

        if (parts == 0 || seconds <= 0) {
            return ParseResult.failure("Duration must be positive.");
        }
        return ParseResult.success(Duration.ofSeconds(seconds));
    }

    private static String join(String[] tokens, int start, int end) {
        if (start >= end) {
            return "";
        }
        List<String> selected = new ArrayList<>();
        for (int i = start; i < end; i++) {
            selected.add(tokens[i]);
        }
        return String.join(" ", selected);
    }

    public record ParseResult(boolean successful, Duration duration, String error) {
        public static ParseResult success(Duration duration) {
            return new ParseResult(true, duration, "");
        }

        public static ParseResult failure(String error) {
            return new ParseResult(false, Duration.ZERO, error);
        }
    }

    public record LeadingParseResult(boolean successful, Duration duration, String remainder, String error) {
        public static LeadingParseResult success(Duration duration, String remainder) {
            return new LeadingParseResult(true, duration, remainder == null ? "" : remainder, "");
        }

        public static LeadingParseResult failure(String error) {
            return new LeadingParseResult(false, Duration.ZERO, "", error);
        }
    }
}
