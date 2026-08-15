package ru.starodubov;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void testTsRangeParser4() {
        TsRange actual = TsRangeParser.parseRange("[\"2026-06-07 08:30:00.123457\",\"2026-06-07 09:45:00.789\"]");
        assertEquals(LocalDateTime.parse("2026-06-07T08:30:00.123457"), actual.lower());
        assertEquals(LocalDateTime.parse("2026-06-07T09:45:00.789"), actual.upper());
        assertEquals(true, actual.lowerInc());
        assertEquals(true, actual.upperInc());
    }

    @Test
    void testTsRangeParser3() {
        TsRange actual = TsRangeParser.parseRange("[\"2020-01-01 10:10:10.143\",\"2020-01-02 10:20:30.123456\")");
        assertEquals(LocalDateTime.parse("2020-01-01T10:10:10.143"), actual.lower());
        assertEquals(LocalDateTime.parse("2020-01-02T10:20:30.123456"), actual.upper());
        assertEquals(true, actual.lowerInc());
        assertEquals(false, actual.upperInc());
    }

    @Test
    void testTsRangeParser2() {
        TsRange actual = TsRangeParser.parseRange("[\"2020-01-01 10:10:10.143\",\"2020-01-02 10:20:30.12\")");
        assertEquals(LocalDateTime.parse("2020-01-01T10:10:10.143"), actual.lower());
        assertEquals(LocalDateTime.parse("2020-01-02T10:20:30.12"), actual.upper());
        assertEquals(true, actual.lowerInc());
        assertEquals(false, actual.upperInc());
    }

    @Test
    void testTsRangeParser() {
        TsRange actual = TsRangeParser.parseRange("[\"2020-01-01 10:10:10\",\"2020-01-02 10:20:30\")");
        assertEquals(LocalDateTime.parse("2020-01-01T10:10:10"), actual.lower());
        assertEquals(LocalDateTime.parse("2020-01-02T10:20:30"), actual.upper());
        assertEquals(true, actual.lowerInc());
        assertEquals(false, actual.upperInc());
    }

    @Test
    void parsesOriginalSixCases() {
        assertAll(
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T10:10:10.1"),
                        TsRangeParser.parseTimestamp("[[[2020-01-01 10:10:10.1,", 3, 21),
                        "1 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T10:10:10.12"),
                        TsRangeParser.parseTimestamp("!!2020-01-01 10:10:10.12", 2, 22),
                        "2 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T10:10:10.123"),
                        TsRangeParser.parseTimestamp("[2020-01-01 10:10:10.123,", 1, 23),
                        "3 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T10:10:10.1234"),
                        TsRangeParser.parseTimestamp("eerer2020-01-01 10:10:10.1234]]]]", 5, 24),
                        "4 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T10:10:10.12345"),
                        TsRangeParser.parseTimestamp("yyyy2020-01-01 10:10:10.12345!!!!", 4, 25),
                        "5 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T10:10:10.123456"),
                        TsRangeParser.parseTimestamp("rrrr2020-01-01 10:10:10.123456ggggg", 4, 26),
                        "6 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T00:00:00"),
                        TsRangeParser.parseTimestamp("[2020-01-01T00:00:00,]", 1, 19),
                        "7 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T00:00:00"),
                        TsRangeParser.parseTimestamp("[2020-01-01]", 1, 10),
                        "8 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T10:10:10.12"),
                        TsRangeParser.parseTimestamp("2020-01-01 10:10:10.12", 0, 22),
                        "9 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T00:00:00"),
                        TsRangeParser.parseTimestamp("2020-01-01", 0, 10),
                        "10 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T10:10:10.1"),
                        TsRangeParser.parseTimestamp("2020-01-01T10:10:10.1", 0, 21),
                        "11 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T10:10:10.1"),
                        TsRangeParser.parseTimestamp("2020-01-01 10:10:10.1", 0, 21),
                        "12 test"
                ),

                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T10:10:10.123"),
                        TsRangeParser.parseTimestamp("2020-01-01 10:10:10.123", 0, 23),
                        "13 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T10:10:10.1234"),
                        TsRangeParser.parseTimestamp("2020-01-01 10:10:10.1234", 0, 24),
                        "14 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T10:10:10.12345"),
                        TsRangeParser.parseTimestamp("2020-01-01 10:10:10.12345", 0, 25),
                        "15 test"
                ),
                () -> assertEquals(
                        LocalDateTime.parse("2020-01-01T10:10:10.123456"),
                        TsRangeParser.parseTimestamp("2020-01-01 10:10:10.123456", 0, 26),
                        "16 test"
                )
        );
    }

    @ParameterizedTest(name = "[{index}] \"{0}\" -> {1}")
    @MethodSource("validTimestamps")
    @DisplayName("Дополнительные валидные timestamp")
    void parsesValidTimestamps(String input, LocalDateTime expected) {
        assertEquals(expected, TsRangeParser.parseTimestamp(input, 0, input.length()));
    }

    static Stream<Arguments> validTimestamps() {
        return Stream.of(
                Arguments.of(
                        "2020-01-01 10:10:10",
                        LocalDateTime.of(2020, 1, 1, 10, 10, 10, 0)
                ),
                Arguments.of(
                        "2020-01-01 10:10:10.0",
                        LocalDateTime.parse("2020-01-01T10:10:10.0")
                ),
                Arguments.of(
                        "2020-01-01 10:10:10.000000",
                        LocalDateTime.parse("2020-01-01T10:10:10.000000")
                ),
                Arguments.of(
                        "2020-01-01 10:10:10.000001",
                        LocalDateTime.parse("2020-01-01T10:10:10.000001")
                ),
                Arguments.of(
                        "2020-01-01 10:10:10.100000",
                        LocalDateTime.parse("2020-01-01T10:10:10.100000")
                ),
                Arguments.of(
                        "2020-02-29 23:59:59.999999",
                        LocalDateTime.parse("2020-02-29T23:59:59.999999")
                ),
                Arguments.of(
                        "2000-02-29 00:00:00.000001",
                        LocalDateTime.parse("2000-02-29T00:00:00.000001")
                ),
                Arguments.of(
                        "1904-02-29 12:34:56.789012",
                        LocalDateTime.parse("1904-02-29T12:34:56.789012")
                ),
                Arguments.of(
                        "0000-01-01 00:00:00.000000",
                        LocalDateTime.parse("0000-01-01T00:00:00.000000")
                ),
                Arguments.of(
                        "0000-02-29 00:00:00.1",
                        LocalDateTime.parse("0000-02-29T00:00:00.1")
                ),
                Arguments.of(
                        "9999-12-31 23:59:59.999999",
                        LocalDateTime.parse("9999-12-31T23:59:59.999999")
                )
        );
    }

    @Test
    @DisplayName("null возвращает null")
    void parsesNullAsNull() {
        assertThrows(IllegalArgumentException.class, () -> TsRangeParser.parseTimestamp(null, 0, 0));
    }

    @ParameterizedTest(name = "[{index}] \"{0}\" -> null")
    @ValueSource(strings = {
            "",
            " ",
            "2020-01-01 10:10:1",
            "2020-01-01 10:10:10.",
            "2020-01-01 10:10:10." + " ",
            "2020-01-01 10:10:10.1" + " ",
            "2020-01-01 10:10:10.1234567",
            "2020-01-01 10:10:10.123456789",
            "2020/01/01 10:10:10.1",
            "2020-01-01 10.10:10.1",
            "2020-01-01 10:10-10.1",
            "2020-01-01 10:10:10x1",
            "20a0-01-01 10:10:10.1",
            "2020-0a-01 10:10:10.1",
            "2020-01-0a 10:10:10.1",
            "2020-01-01 1a:10:10.1",
            "2020-01-01 10:1a:10.1",
            "2020-01-01 10:10:1a.1",
            "2020-01-01 10:10:10.a",
            "2020-01-01 10:10:10.1a",
            "2020-00-01 10:10:10.1",
            "2020-13-01 10:10:10.1",
            "2020-01-00 10:10:10.1",
            "2020-01-32 10:10:10.1",
            "2020-04-31 10:10:10.1",
            "2020-01-01 24:00:00.1",
            "2020-01-01 10:60:00.1",
            "2020-01-01 10:10:60.1",
            " " + "2020-01-01 10:10:10.1"
    })
    @DisplayName("Невалидные timestamp")
    void rejectsInvalidTimestamps(String input) {
        assertThrows(DateTimeException.class, () -> TsRangeParser.parseTimestamp(input, 0, input.length()));
    }
}