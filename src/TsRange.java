package ru.nspk.pcl.common.tsrange;

import java.time.LocalDateTime;
import java.util.Objects;

/**
  Примеры из документации постгрес
  operator|  description                |  example                                                |   result          | impl status
  =	    is equal	                    int4range(1,5) = '[1,4]'::int4range	                            t               +
  <>	not equal	                    numrange(1.1,2.2) <> numrange(1.1,2.3)	                        t               +
  <	    less than	                    int4range(1,10) < int4range(2,3)	                            t               +
  >	    greater than	                int4range(1,10) > int4range(1,5)	                            t               +
  <=	less than or equal	            numrange(1.1,2.2) <= numrange(1.1,2.2)	                        t               +
  >=	greater than or equal	        numrange(1.1,2.2) >= numrange(1.1,2.0)	                        t               +
  @>    contains range	                int4range(2,4) @> int4range(2,3)	                            t               +
  @>    contains element	            '[2011-01-01,2011-03-01)'::tsrange @> '2011-01-10'::timestamp	t               +
  <@	range is contained by	        int4range(2,4) <@ int4range(1,7)	                            t               +
  &&	overlap 	                    int8range(3,7) && int8range(4,12)	                            t               +
  <<	strictly left of	            int8range(1,10) << int8range(100,110)	                        t               +
  >>	strictly right of	            int8range(50,60) >> int8range(20,30)	                        t               +
  &<	not extend to the right of	    int8range(1,20) &< int8range(18,20)	                            t               +
  &>	not extend to the left of	    int8range(7,20) &> int8range(5,10)	                            t               +
  -|-	is adjacent to	                numrange(1.1,2.2) -|- numrange(2.2,3.3)	                        t               +
  +	    union	                        numrange(5,15) + numrange(10,20)	                            [5,20)          +
 '*'    intersection	                int8range(5,15) * int8range(10,20)	                            [10,15)         +
  -	    difference	                    int8range(5,15) - int8range(10,20)	                            [5,10)          +
 **/

public final class TsRange implements Comparable<TsRange> {
    private final LocalDateTime lower;
    private final LocalDateTime upper;
    private final boolean lowerInc;
    private final boolean upperInc;
    public final static LocalDateTime INFINITY = LocalDateTime.MAX;
    public final static LocalDateTime MINUS_INFINITY = LocalDateTime.MIN;
    public final static TsRange EMPTY = TsRange.of(MINUS_INFINITY, MINUS_INFINITY, "()");

    public final static String DEFAULT_BOUNDS = "[)";

    TsRange(LocalDateTime lower, LocalDateTime upper, boolean lowerInc, boolean upperInc) {
        this.lower = lower;
        this.upper = upper;
        this.lowerInc = lowerInc;
        this.upperInc = upperInc;
    }

    public static TsRange of(final String lower, final String upper, final String bounds) {
        final LocalDateTime l = TsRangeParser.parseTimestamp(lower, 0, lower.length());
        final LocalDateTime u = TsRangeParser.parseTimestamp(upper, 0, upper.length());
        return TsRange.of(l, u, bounds);
    }

    public static TsRange of(final String lower, final String upper) {
        final LocalDateTime l = TsRangeParser.parseTimestamp(lower, 0, lower.length());
        final LocalDateTime u = TsRangeParser.parseTimestamp(upper, 0, upper.length());

        return TsRange.of(l, u, DEFAULT_BOUNDS);
    }

    public static TsRange of(final LocalDateTime lower, final LocalDateTime upper, final boolean lowerInc, final boolean upperInc) {
        if (lower == null || upper == null) {
            throw new UnsupportedOperationException("null is not allowed. lower: %s, upper:%s"
                    .formatted(lower, upper));
        }

        if (lower.isAfter(upper)) {
            throw new UnsupportedOperationException("lower must be before or equal upper");
        }

        return new TsRange(lower, upper, lowerInc, upperInc);
    }

    public static TsRange of(LocalDateTime lower, LocalDateTime upper, String bounds) {
        if (bounds == null || bounds.length() != 2) {
            throw new IllegalArgumentException("unknown bounds expression");
        }

        final boolean lowerInc = TsRangeParser.parseLeftBound(bounds.charAt(0));
        final boolean upperInc = TsRangeParser.parseRightBound(bounds.charAt(1));

        return TsRange.of(lower, upper, lowerInc, upperInc);
    }

    public static TsRange of(LocalDateTime lower, LocalDateTime upper) {
        return TsRange.of(lower, upper, true, false);
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

    public TsRange difference(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }

        if (range.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return TsRange.EMPTY;
        }

        if (!this.overlaps(range)) {
            return this;
        }

        if (this.isEqual(range)) {
            return TsRange.EMPTY;
        }

        if (this.lessThan(range)) {
            final int cmp = this.compareUpper(range);

            if (cmp > 0) {
                throw new IllegalArgumentException(
                        "result of range difference would not be contiguous. this: %s, range: %s"
                                .formatted(this, range));
            }

            return TsRange.of(this.lower(), range.lower(), this.lowerInc(), !range.lowerInc());
        }

        final int cmp = range.compareUpper(this);

        if (cmp >= 0) {
            return TsRange.EMPTY;
        }

        return TsRange.of(range.upper(), this.upper(), !range.upperInc(), this.upperInc());
    }

    public TsRange intersection(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }

        if (this.isEmpty() || range.isEmpty()) {
            return TsRange.EMPTY;
        }

        if (!this.overlaps(range)) {
            return TsRange.EMPTY;
        }

        if (this.lessThan(range)) {
            final int cmp = this.compareUpper(range);
            if (cmp >= 0) {
                return TsRange.of(range.lower(), range.upper(), range.lowerInc(), range.upperInc());
            }
            return TsRange.of(range.lower(), this.upper(), range.lowerInc(), this.upperInc());
        }

        final int cmp = range.compareUpper(this);

        if (cmp >= 0) {
            return TsRange.of(this.lower(), this.upper(), this.lowerInc(), this.upperInc());
        }

        return TsRange.of(this.lower(), range.upper(), this.lowerInc(), range.upperInc());
    }

    public TsRange union(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }

        if (range.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return range;
        }

        if (!(this.overlaps(range) || this.isAdjacentTo(range))) {
            throw new IllegalArgumentException("result of range union would not be contiguous. this: %s, range: %s"
                    .formatted(this, range));
        }

        if (this.lessThan(range)) {
            final int cmp = this.compareUpper(range);
            if (cmp >= 0) {
                return TsRange.of(this.lower(), this.upper(), this.lowerInc(), this.upperInc());
            }
            return TsRange.of(this.lower(), range.upper(), this.lowerInc(), range.upperInc());
        }

        final int cmp = range.compareUpper(this);

        if (cmp >= 0) {
            return TsRange.of(range.lower(), range.upper(), range.lowerInc(), range.upperInc());
        }

        return TsRange.of(range.lower(), this.upper(), range.lowerInc(), this.upperInc());
    }

    public boolean containsRange(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }

        if (!this.isEmpty() && range.isEmpty()) {
            return true;
        }

        if (this.isEmpty() && !range.isEmpty()) {
            return false;
        }

        if (this.isEmpty() && range.isEmpty()) {
            return true;
        }

        final int cmpLower = this.compareLower(range);
        final int cmpUp = this.compareUpper(range);

        return cmpLower <= 0 && cmpUp >= 0;
    }

    public boolean containsElement(final LocalDateTime element) {
        if (element == null) {
            throw new IllegalArgumentException("element must not be null");
        }

        if (this.isEmpty()) {
            return false;
        }

        final int cmpLower = compareDateTime(element, this.lower);
        if (cmpLower < 0) {
            return false;
        }
        final int cmpUpper = compareDateTime(element, this.upper);
        if (cmpUpper > 0) {
            return false;
        }
        if (cmpLower > 0 && cmpUpper < 0) {
            return true;
        }
        if (cmpLower == 0) {
            return lowerInc;
        }

        if (cmpUpper == 0) {
            return upperInc;
        }

        return false;
    }

    public boolean rangeIsContainedBy(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range is null");
        }

        return range.containsRange(this);
    }

    public boolean lowerInc() {
        return lowerInc;
    }

    public boolean upperInc() {
        return upperInc;
    }

    public boolean lowerInf() {
        return this.lower.isEqual(MINUS_INFINITY);
    }

    public boolean upperInf() {
        return this.upper.isEqual(INFINITY);
    }

    public boolean notExtendsLeftOf(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }
        if (this.isEmpty() || range.isEmpty()) {
            return false;
        }
        final int cmp = this.compareLower(range);
        return cmp >= 0;
    }

    public boolean isAdjacentTo(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }

        if (this.isEmpty() || range.isEmpty()) {
            return false;
        }

        final int cmpA = compareDateTime(this.upper(), range.lower());
        final int cmpB = compareDateTime(this.lower(), range.upper());

        if (cmpA == 0) {
            return cmpB < 0 && ((!this.upperInc() && range.lowerInc()) || (this.upperInc() && !range.lowerInc()));
        }

        if (cmpB == 0) {
            return cmpA > 0 && ((!this.lowerInc() && range.upperInc()) || (this.lowerInc() && !range.upperInc()));
        }

        return false;
    }

    public boolean notExtendsRightOf(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }
        if (this.isEmpty() || range.isEmpty()) {
            return false;
        }
        final int cmpUpper = this.compareUpper(range);
        return cmpUpper <= 0;
    }

    public boolean strictlyRightOf(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }

        if (range.isEmpty() || this.isEmpty()) {
            return false;
        }

        return range.strictlyLeftOf(this);
    }

    public boolean strictlyLeftOf(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }
        if (range.isEmpty() || this.isEmpty()) {
            return false;
        }

        if (this.overlaps(range)) {
            return false;
        }

        if (this.lessThan(range)) {
            return true;
        }

        return false;
    }

    public boolean isEqual(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }
        if (this.isEmpty() && range.isEmpty()) {
            return true;
        }
        return this.lower.isEqual(range.lower()) &&
                this.upper.isEqual(range.upper()) &&
                this.upperInc == range.upperInc() &&
                this.lowerInc == range.lowerInc();
    }

    public boolean greaterThanOrEqual(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }

        return !this.lessThan(range);
    }

    public boolean lessThanOrEqual(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }

        return !this.greaterThan(range);
    }

    public boolean lessThan(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }
        if (this.isEmpty() && !range.isEmpty()) {
            return true;
        }
        if (!this.isEmpty() && range.isEmpty()) {
            return false;
        }
        if (this.isEmpty() && range.isEmpty()) {
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

    public boolean overlaps(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }
        if (range.isEmpty() || this.isEmpty()) {
            return false;
        }

        if (this.lessThan(range)) {
            final int cmp = compareDateTime(this.upper(), range.lower());
            if (cmp > 0) {
                return true;
            } else if (cmp < 0) {
                return false;
            } else {
                return this.upperInc() && range.lowerInc();
            }
        }

        final int cmp = compareDateTime(this.lower(), range.upper());
        if (cmp > 0) {
            return false;
        } else if (cmp < 0) {
            return true;
        } else {
            return this.lowerInc() && range.upperInc();
        }
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

        if (this.isEmpty()) {
            return range;
        }

        if (this.lessThan(range)) {
            final int cmp = compareDateTime(upper, range.upper());
            if (cmp == 0) {
                return TsRange.of(lower, upper, lowerInc, upperInc || range.upperInc());
            }
            if (cmp < 0) {
                return TsRange.of(lower, range.upper(), lowerInc, range.upperInc());
            }
            //cmp > 0
            return TsRange.of(lower, upper, lowerInc, upperInc);
        }

        // greaterThan or equal
        final int cmp = compareDateTime(upper, range.upper());
        if (cmp == 0) {
            return TsRange.of(range.lower(), range.upper(), range.lowerInc(), range.upperInc() || upperInc);
        }
        if (cmp < 0) {
            return TsRange.of(range.lower(), range.upper(), range.lowerInc(), range.upperInc());
        }
        //cmp > 0
        return TsRange.of(range.lower(), upper, range.lowerInc(), upperInc);
    }

    public boolean notEq(final TsRange range) {
        return !this.isEqual(range);
    }

    public boolean greaterThan(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }

        if (this.isEmpty() && range.isEmpty()) {
            return false;
        }

        if (this.isEmpty()) {
            return false;
        }

        if (range.isEmpty()) {
            return true;
        }

        return range.lessThan(this);
    }

    public int compareUpper(final TsRange range) {
        return compareUpperEndpoints(this.upper, range.upper, this.upperInc, range.upperInc);
    }

    public int compareLower(final TsRange range) {
        return compareLowerEndpoints(this.lower, range.lower, this.lowerInc, range.lowerInc);
    }

    public static int compareLowerEndpoints(final LocalDateTime datetime1, final LocalDateTime datetime2,
                                            final boolean bound1, final boolean bound2) {
        final int cmp = compareDateTime(datetime1, datetime2);
        if (cmp != 0) {
            return cmp;
        }
        return compareBounds(bound1, bound2);
    }


    public static int compareUpperEndpoints(final LocalDateTime datetime1, final LocalDateTime datetime2,
                                            final boolean bound1, final boolean bound2) {
        final int cmp = compareDateTime(datetime1, datetime2);
        if (cmp != 0) {
            return cmp;
        }

        //сравниваем инверисонно
        return compareBounds(bound2, bound1);
    }

    /*
        сравнение границ
     */
    public static int compareBounds(final boolean b1, final boolean b2) {
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
    public static int compareDateTime(final LocalDateTime d1, final LocalDateTime d2) {
        return d1.compareTo(d2);
    }

    @Override
    public int compareTo(TsRange range) {
        if (this.lessThan(range)) {
            return -1;
        }

        if (this.isEqual(range)) {
            return 0;
        }

        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof TsRange range) {
            return this.lower.isEqual(range.lower()) &&
                    this.upper.isEqual(range.upper()) &&
                    this.upperInc == range.upperInc() &&
                    this.lowerInc == range.lowerInc();
        }
        return false;
    }

    /*
     * anyrange @> anymultirange → boolean
     * Диапазон содержит мультисписок?
     * int4range(1,10) @> '{[2,3),[5,6)}'::int4multirange → t
     */
    public boolean containsMultirange(final TsMultiRange multirange) {
        if (multirange == null) {
            throw new IllegalArgumentException("multirange must not be null");
        }

        if (multirange.isEmpty()) {
            return true;
        }

        if (this.isEmpty()) {
            return false;
        }

        return this.containsRange(multirange.getFirst())
                && this.containsRange(multirange.getLast());
    }

    /*
     * anyrange <@ anymultirange → boolean
     * Диапазон содержится в мультисписке?
     * int4range(2,4) <@ '{[1,5),[10,15)}'::int4multirange → t
     */
    public boolean isContainedBy(final TsMultiRange multirange) {
        if (multirange == null) {
            throw new IllegalArgumentException("multirange must not be null");
        }
        return multirange.containsRange(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lower, upper, lowerInc, upperInc);
    }

    @Override
    public String toString() {
        if (this.isEmpty()) {
            return "empty";
        }
        final String l = lower.toString();
        final String u = upper.toString();
        return (lowerInc ? "[" : "(") +
                (lowerInf() ? "-infinity" : ("\"" + l + "\"")) +
                "," +
                (upperInf() ? "infinity" : ("\"" + u + "\"")) +
                (upperInc ? "]" : ")");
    }
}
