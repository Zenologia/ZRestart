package dev.zenologia.zrestart.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ScheduleParserTest {
    private final ScheduleParser parser = new ScheduleParser();

    @ParameterizedTest
    @ValueSource(strings = {
        "Daily;05;00;Daily maintenance",
        "daily;06;00;Lowercase daily",
        "monday;11;00;Lowercase weekday",
        "Friday;22;00"
    })
    void acceptsValidCaseInsensitiveEntries(String raw) {
        ScheduleParseResult result = this.parser.parse(raw);

        assertTrue(result.successful(), result.error());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Everyday;05;00",
        "BadDay;05;00",
        "Daily;24;00",
        "Daily;05;60",
        "Daily;aa;00",
        "Daily;05"
    })
    void rejectsInvalidEntries(String raw) {
        ScheduleParseResult result = this.parser.parse(raw);

        assertFalse(result.successful());
    }

    @Test
    void keepsReasonAfterThirdSemicolon() {
        ScheduleParseResult result = this.parser.parse("Monday;11;00;Reason with; semicolon");

        assertTrue(result.successful(), result.error());
        assertEquals("Reason with; semicolon", result.entry().reason());
    }
}
