package ru.starodubov;

import java.time.LocalDateTime;

public final class TsRange {
    private final LocalDateTime lower;
    private final LocalDateTime upper;
    private final boolean lowerInc;
    private final boolean upperInc;
    public final static LocalDateTime INFINITY = LocalDateTime.MAX;
    public final static LocalDateTime MINUS_INFINITY = LocalDateTime.MIN;
    public final static String DEFAULT_BOUNDS = "[)";

    TsRange(LocalDateTime lower, LocalDateTime upper, boolean lowerInc, boolean upperInc) {
        this.lower = lower;
        this.upper = upper;
        this.lowerInc = lowerInc;
        this.upperInc = upperInc;
    }

    public static TsRange of(final String lower, final String upper, final String bounds) {
        final String l = lower.length() == 10 ? lower + "T00:00:00" : lower;
        final String u = upper.length() == 10 ? upper + "T00:00:00" : upper;

        return TsRange.of(LocalDateTime.parse(l), LocalDateTime.parse(u), bounds);
    }

    public static TsRange of(final String lower, final String upper) {
        final String l = lower.length() == 10 ? lower + "T00:00:00" : lower;
        final String u = upper.length() == 10 ? upper + "T00:00:00" : upper;

        return TsRange.of(LocalDateTime.parse(l), LocalDateTime.parse(u), DEFAULT_BOUNDS);
    }


    public static TsRange of(LocalDateTime lower, LocalDateTime upper, String bounds) {
        if (lower == null || upper == null) {
            throw new UnsupportedOperationException("null is not allowed. lower: %s, upper:%s"
                    .formatted(lower, upper));
        }
        if (bounds == null || bounds.length() != 2) {
            throw new UnsupportedOperationException("unknown bounds expression");
        }

        final boolean lowerInc = switch (bounds.charAt(0)) {
            case '(' -> false;
            case '[' -> true;
            default -> throw new UnsupportedOperationException("unknown bound: %s".formatted(bounds.charAt(0)));
        };

        final boolean upperInc = switch (bounds.charAt(1)) {
            case ')' -> false;
            case ']' -> true;
            default -> throw new UnsupportedOperationException("unknown bound: %s".formatted(bounds.charAt(1)));
        };

        if (lower.isAfter(upper)) {
            throw new UnsupportedOperationException("lower must be before upper");
        }

        return new TsRange(lower, upper, lowerInc, upperInc);
    }

    public static TsRange of(LocalDateTime lower, LocalDateTime upper) {
        return TsRange.of(lower, upper, DEFAULT_BOUNDS);
    }

    public boolean isEmpty() {
        return lower.isEqual(upper) && (!lowerInc || !upperInc);
    }

    public LocalDateTime lower() {
        return lower;
    }

    public LocalDateTime upper() {
        return upper;
    }

    public boolean lowerInc() {
        return lowerInc;
    }

    public boolean upperInc() {
        return upperInc;
    }

    public boolean lowerInf() {
        return lower.isEqual(MINUS_INFINITY) || lower.isEqual(INFINITY);
    }

    public boolean upperInf() {
        return upper.isEqual(MINUS_INFINITY) || upper.isEqual(INFINITY);
    }

    public boolean eq(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }
        if (isEmpty() && range.isEmpty()) {
            return true;
        }
        return this.lower.isEqual(range.lower()) &&
                this.upper.isEqual(range.upper()) &&
                this.upperInc == range.upperInc() &&
                this.lowerInc == range.lowerInc();
    }

    public boolean lessThan(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }
        if (isEmpty() && !range.isEmpty()) {
            return true;
        }
        if (!isEmpty() && range.isEmpty()) {
            return false;
        }
        if (isEmpty() && range.isEmpty()) {
            return false; // Оба пустые - равны
        }

        //Сначала сравниваются нижние границы.
        final int lowerCmp = compareLower(range);
        if (lowerCmp < 0) {
            return true;
        }

        if (lowerCmp > 0) {
            return false;
        }

        //Если они равны, сравниваются верхние границы.
        final int upperCmp = compareUpper(range);

        if (upperCmp < 0) {
            return true;
        }
        if (upperCmp > 0) {
            return false;
        }

        return false;
    }

    /*
     объединяет два диапазона в один общий минимальный охватывающий диапазон.
     */
    public TsRange merge(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }
        if (range.isEmpty()) {
            return this;
        }

        if (isEmpty()) {
            return range;
        }

        if (lessThan(range)) {
            final int cmp = comparePoints(upper, range.upper());
            if (cmp == 0) {
                return new TsRange(lower, upper, lowerInc, upperInc || range.upperInc());
            }
            if (cmp < 0) {
                return new TsRange(lower, range.upper(), lowerInc, range.upperInc());
            }
            //cmp > 0
            return new TsRange(lower, upper, lowerInc, upperInc);
        }

        // greaterThan
        final int cmp = comparePoints(upper, range.upper());
        if (cmp == 0) {
            return new TsRange(range.lower(), range.upper(), range.lowerInc(), range.upperInc() || upperInc);
        }
        if (cmp < 0) {
            return new TsRange(range.lower(), range.upper(), range.lowerInc(), range.upperInc());
        }
        //cmp > 0
        return new TsRange(range.lower(), upper, range.lowerInc(), upperInc);
    }

    public boolean notEq(final TsRange range) {
        return !eq(range);
    }

    private int compareUpper(final TsRange range) {
        final int cmp = comparePoints(upper, range.upper);
        if (cmp != 0) {
            return cmp;
        }
        //сравниваем инверисонно
        return compareBounds(range.upperInc, upperInc);
    }

    private int compareLower(final TsRange range) {
        final int cmp = comparePoints(lower, range.lower);
        if (cmp != 0) {
            return cmp;
        }
        return compareBounds(lowerInc, range.lowerInc);
    }

    /*
        сравнение границ
     */
    private static int compareBounds(final boolean b1, final boolean b2) {
        if (b1 && !b2) {
            return -1;
        }

        if (!b1 && !b2) {
            return 0;
        }

        if (b1 && b2) {
            return 0;
        }

        if (!b1 && b2) {
            return 1;
        }
        // cannot reach here
        throw new RuntimeException();
    }

    /*
        сравнение временных точек
     */
    private static int comparePoints(final LocalDateTime d1, final LocalDateTime d2) {
        return d1.compareTo(d2);
    }
}
