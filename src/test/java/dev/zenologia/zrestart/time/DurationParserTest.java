package dev.zenologia.zrestart.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DurationParserTest {
    private final DurationParser parser = new DurationParser();

    @ParameterizedTest
    @CsvSource({
        "3600,3600",
        "1:00,3600",
        "1:30,5400",
        "0:30,1800",
        "30m,1800",
        "30M,1800",
        "1h30m,5400",
        "'1h 30m',5400",
        "1h30m15m,6300",
        "5s,5",
        "2h,7200"
    })
    void acceptsDocumentedDurationForms(String input, long expectedSeconds) {
        DurationParser.ParseResult result = this.parser.parse(input);

        assertTrue(result.successful(), result.error());
        assertEquals(Duration.ofSeconds(expectedSeconds), result.duration());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0:00", "-1", "abc", "1x", "1:60", "1h -5m", "999999999999999999999999999999999999h"})
    void rejectsInvalidDurations(String input) {
        DurationParser.ParseResult result = this.parser.parse(input);

        assertFalse(result.successful());
    }

    @ParameterizedTest
    @CsvSource({
        "'1h 30m Maintenance',5400,Maintenance",
        "'30m Daily maintenance',1800,Daily maintenance",
        "'3600 Raw seconds',3600,Raw seconds",
        "'0:30',1800,''"
    })
    void parsesLeadingIntervalForManualCommands(String input, long expectedSeconds, String expectedRemainder) {
        DurationParser.LeadingParseResult result = this.parser.parseLeading(input);

        assertTrue(result.successful(), result.error());
        assertEquals(Duration.ofSeconds(expectedSeconds), result.duration());
        assertEquals(expectedRemainder, result.remainder());
    }
}
