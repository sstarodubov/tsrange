package ru.starodubov;

import java.time.DateTimeException;
import java.time.LocalDateTime;

public class TsRangeParser {
    /*
          TsRange должен иметь формат типа:
                "[\"2026-06-07 08:30:00.123457\",\"2026-06-07 09:45:00.789\"]"
                "[\"2026-06-07 08:30:00\",\"2026-06-07 09:45:00.789\"]"
                "[\"2026-06-07\",\"2026-06-07 09:45:00.789\"]"
                "[\"2026-06-07\",\"2026-06-07 09:45:00.789134\"]"

          Метод не далает никакких валидаций.
          Метод подразумевает что формат корректный.
          Валидацию входного формата прописывать САМОСТОЯТЕЛЬНО.
         */
    public static TsRange parseRange(final String range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }

        final boolean lowerInc = parseLeftBound(range.charAt(0));
        final boolean upperInc = parseRightBound(range.charAt(range.length() - 1));

        final int commaIdx = range.indexOf(',');

        final LocalDateTime lower = parseTimestamp(range, 2, commaIdx - 3);
        final LocalDateTime upper = parseTimestamp(range, commaIdx + 2, range.length() - commaIdx - 4);

        return TsRange.of(lower, upper, lowerInc, upperInc);
    }

    static boolean parseLeftBound(final char bound) {
        return switch (bound) {
            case '(' -> false;
            case '[' -> true;
            default -> throw new UnsupportedOperationException("unknown bound: %s".formatted(bound));
        };
    }

    static boolean parseRightBound(final char bound) {
        return switch (bound) {
            case ')' -> false;
            case ']' -> true;
            default -> throw new UnsupportedOperationException("unknown bound: %s".formatted(bound));
        };
    }


    /**
     * Парсит timestamp вида:
     * "2020-01-01 10:10:10.1"
     * ...
     * "2020-01-01 10:10:10.123456"
     *
     * Также понимает формат без дробной части:
     * "2020-01-01 10:10:10"
     * "2020-01-01
     *
     */
    static LocalDateTime parseTimestamp(final String timestamp, final int offset, final int len) {
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must be not null");
        }

        final int year = fourDigits(timestamp, 0 + offset);
        final int month = twoDigits(timestamp, 5 + offset);
        final int day = twoDigits(timestamp, 8 + offset);

        if (len == 10) {
            return LocalDateTime.of(year, month, day, 0, 0, 0);
        }

        final int hour = twoDigits(timestamp, 11 + offset);
        final int minute = twoDigits(timestamp, 14 + offset);
        final int second = twoDigits(timestamp, 17 + offset);

        int nanoOfSecond = 0;

        if (len > 20) {
            final int fracDigits = len - 20;
            for (int i = 20 + offset; i < len + offset; ++i) {
                final char c = timestamp.charAt(i);
                nanoOfSecond = nanoOfSecond * 10 + (c - '0');
            }

            for (int i = fracDigits; i < 9; ++i) {
                nanoOfSecond *= 10;
            }
        }

        return LocalDateTime.of(year, month, day, hour, minute, second, nanoOfSecond);
    }

    private static int fourDigits(final String s, final int i) {
        final char c0 = s.charAt(i);
        final char c1 = s.charAt(i + 1);
        final char c2 = s.charAt(i + 2);
        final char c3 = s.charAt(i + 3);

        return (c0 - '0') * 1000
                + (c1 - '0') * 100
                + (c2 - '0') * 10
                + (c3 - '0');
    }

    private static int twoDigits(final String s, final int i) {
        final char c0 = s.charAt(i);
        final char c1 = s.charAt(i + 1);

        return (c0 - '0') * 10 + (c1 - '0');
    }
}
