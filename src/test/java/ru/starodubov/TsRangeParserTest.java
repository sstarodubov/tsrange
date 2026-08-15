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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TsRangeParserTest {

    private static void assertRange(
            final TsRange actual,
            final TsRange expected
    ) {
        assertEquals(expected.lower(), actual.lower());
        assertEquals(expected.upper(), actual.upper());
        assertEquals(expected.lowerInc(), actual.lowerInc());
        assertEquals(expected.upperInc(), actual.upperInc());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("validRanges")
    @DisplayName("Валидные range парсятся корректно")
    void parsesValidRanges(final String range, final TsRange expected) {
        assertRange(expected, TsRangeParser.parseRange(range));
    }

    static Stream<Arguments> validRanges() {
        return Stream.of(
                // Исходный пример: обе границы inclusive
                Arguments.of(
                        "[\"2026-06-07 08:30:00.123457\",\"2026-06-07 09:45:00.789\"]",
                        TsRange.of(
                                LocalDateTime.of(2026, 6, 7, 8, 30, 0, 123_457_000),
                                LocalDateTime.of(2026, 6, 7, 9, 45, 0, 789_000_000),
                                true,
                                true
                        )
                ),

                // Обе границы exclusive
                Arguments.of(
                        "(\"2026-06-07 08:30:00.123457\",\"2026-06-07 09:45:00.789\")",
                        TsRange.of(
                                LocalDateTime.of(2026, 6, 7, 8, 30, 0, 123_457_000),
                                LocalDateTime.of(2026, 6, 7, 9, 45, 0, 789_000_000),
                                false,
                                false
                        )
                ),

                // lower inclusive, upper exclusive
                Arguments.of(
                        "[\"2026-06-07 08:30:00.123457\",\"2026-06-07 09:45:00.789\")",
                        TsRange.of(
                                LocalDateTime.of(2026, 6, 7, 8, 30, 0, 123_457_000),
                                LocalDateTime.of(2026, 6, 7, 9, 45, 0, 789_000_000),
                                true,
                                false
                        )
                ),

                // lower exclusive, upper inclusive
                Arguments.of(
                        "(\"2026-06-07 08:30:00.123457\",\"2026-06-07 09:45:00.789\"]",
                        TsRange.of(
                                LocalDateTime.of(2026, 6, 7, 8, 30, 0, 123_457_000),
                                LocalDateTime.of(2026, 6, 7, 9, 45, 0, 789_000_000),
                                false,
                                true
                        )
                ),

                // Без дробной части
                Arguments.of(
                        "[\"2026-06-07 08:30:00\",\"2026-06-07 09:45:00\"]",
                        TsRange.of(
                                LocalDateTime.of(2026, 6, 7, 8, 30, 0, 0),
                                LocalDateTime.of(2026, 6, 7, 9, 45, 0, 0),
                                true,
                                true
                        )
                ),

                // Только дата, время должно быть 00:00:00
                Arguments.of(
                        "[\"2026-06-07\",\"2026-06-08\"]",
                        TsRange.of(
                                LocalDateTime.of(2026, 6, 7, 0, 0, 0, 0),
                                LocalDateTime.of(2026, 6, 8, 0, 0, 0, 0),
                                true,
                                true
                        )
                ),

                // Разная длина дробной части: 1 и 6 цифр
                Arguments.of(
                        "[\"2026-06-07 08:30:00.1\",\"2026-06-07 09:45:00.123456\"]",
                        TsRange.of(
                                LocalDateTime.of(2026, 6, 7, 8, 30, 0, 100_000_000),
                                LocalDateTime.of(2026, 6, 7, 9, 45, 0, 123_456_000),
                                true,
                                true
                        )
                ),

                // Разделитель даты и времени T
                Arguments.of(
                        "[\"2026-06-07T08:30:00.123457\",\"2026-06-07T09:45:00.789\"]",
                        TsRange.of(
                                LocalDateTime.of(2026, 6, 7, 8, 30, 0, 123_457_000),
                                LocalDateTime.of(2026, 6, 7, 9, 45, 0, 789_000_000),
                                true,
                                true
                        )
                ),

                // Нулевая дробная часть
                Arguments.of(
                        "[\"2026-06-07 08:30:00.000000\",\"2026-06-07 09:45:00.0\"]",
                        TsRange.of(
                                LocalDateTime.of(2026, 6, 7, 8, 30, 0, 0),
                                LocalDateTime.of(2026, 6, 7, 9, 45, 0, 0),
                                true,
                                true
                        )
                )
        );
    }
}