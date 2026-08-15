package ru.starodubov;

import java.time.DateTimeException;
import java.time.LocalDateTime;

public class TsRangeParser {

    public static boolean parseLeftBound(final char bound) {
        return switch (bound) {
            case '(' -> false;
            case '[' -> true;
            default -> throw new UnsupportedOperationException("unknown bound: %s".formatted(bound));
        };
    }


    public static boolean parseRightBound(final char bound) {
        return switch (bound) {
            case ')' -> false;
            case ']' -> true;
            default -> throw new UnsupportedOperationException("unknown bound: %s".formatted(bound));
        };
    }
    /*
      range must have format "[\"2026-06-07 08:30:00.123457\",\"2026-06-07 09:45:00.789\"]"
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
    public static LocalDateTime parseTimestamp(final String timestamp, final int offset, final int len) {
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must be not null");
        }

        if (len != 10 && len != 19 && (len < 21 || len > 26)) {
            throw new DateTimeException("wrong timestamp format: %s, offset: %s, len: %s".formatted(timestamp, offset, len));
        }

        if (timestamp.charAt(4 + offset) != '-' || timestamp.charAt(7 + offset) != '-') {
            throw new DateTimeException("wrong timestamp format: %s, offset: %s, len: %s".formatted(timestamp, offset, len));
        }

        final int year = fourDigits(timestamp, 0 + offset);
        final int month = twoDigits(timestamp, 5 + offset);
        final int day = twoDigits(timestamp, 8 + offset);

        if (len == 10) {
            return LocalDateTime.of(year, month, day, 0, 0, 0);
        }

        if (len > 19 && timestamp.charAt(19 + offset) != '.') {
            throw new DateTimeException("wrong timestamp format: %s, offset: %s, len: %s".formatted(timestamp, offset, len));
        }

        if (!(timestamp.charAt(10 + offset) == ' ' || timestamp.charAt(10 + offset) == 'T')
                || timestamp.charAt(13 + offset) != ':'
                || timestamp.charAt(16 + offset) != ':') {
            throw new DateTimeException("wrong timestamp format: %s, offset: %s, len: %s".formatted(timestamp, offset, len));
        }

        final int hour = twoDigits(timestamp, 11 + offset);
        final int minute = twoDigits(timestamp, 14 + offset);
        final int second = twoDigits(timestamp, 17 + offset);

        int nanoOfSecond = 0;

        if (len > 20) {
            final int fracDigits = len - 20;
            for (int i = 20 + offset; i < len + offset; ++i) {
                final char c = timestamp.charAt(i);
                if (c < '0' || c > '9') {
                    throw new DateTimeException("wrong timestamp format: %s, offset: %s, len: %s".formatted(timestamp, offset, len));
                }
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

        if (c0 < '0' || c0 > '9'
                || c1 < '0' || c1 > '9'
                || c2 < '0' || c2 > '9'
                || c3 < '0' || c3 > '9') {
            throw new DateTimeException("wrong timestamp format: %s, index: %d".formatted(s, i));
        }

        return (c0 - '0') * 1000
                + (c1 - '0') * 100
                + (c2 - '0') * 10
                + (c3 - '0');
    }

    private static int twoDigits(final String s, final int i) {
        final char c0 = s.charAt(i);
        final char c1 = s.charAt(i + 1);

        if (c0 < '0' || c0 > '9' || c1 < '0' || c1 > '9') {
            throw new DateTimeException("wrong timestamp format: %s, index: %d".formatted(s, i));
        }

        return (c0 - '0') * 10 + (c1 - '0');
    }
}
