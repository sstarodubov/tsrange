package ru.nspk.pcl.common.tsrange;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TsRangeStrictlyLeftOfTest {

    // ==================== ПОЛНОСТЬЮ РАЗНЕСЕНЫ ====================

    @Nested
    @DisplayName("Полностью разнесены")
    class FullyApartTests {

        @Test
        @DisplayName("Первый диапазон полностью до второго — true")
        void firstCompletelyBeforeSecond() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertTrue(r1.strictlyLeftOf(r2));
            assertFalse(r2.strictlyLeftOf(r1));
        }

        @Test
        @DisplayName("Первый диапазон полностью после второго — false")
        void firstCompletelyAfterSecond() {
            TsRange r1 = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(r1.strictlyLeftOf(r2));
            assertTrue(r2.strictlyLeftOf(r1));
        }

        @Test
        @DisplayName("Диапазоны далеко друг от друга — true")
        void rangesFarApart() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-06-01", "2026-06-10", "[)");

            assertTrue(r1.strictlyLeftOf(r2));
        }

        @Test
        @DisplayName("Разрыв в один день — true")
        void oneDayGap() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-11", "2026-01-20", "[)");

            assertTrue(r1.strictlyLeftOf(r2));
        }
    }

// ==================== ПЕРЕСЕЧЕНИЕ ====================

    @Nested
    @DisplayName("Пересечение")
    class OverlapTests {

        @Test
        @DisplayName("Частичное пересечение — false")
        void partialOverlap() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            assertFalse(r1.strictlyLeftOf(r2));
            assertFalse(r2.strictlyLeftOf(r1));
        }

        @Test
        @DisplayName("Полное вложение — false")
        void fullContainment() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertFalse(outer.strictlyLeftOf(inner));
            assertFalse(inner.strictlyLeftOf(outer));
        }

        @Test
        @DisplayName("Идентичные диапазоны — false")
        void identicalRanges() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(r1.strictlyLeftOf(r2));
            assertFalse(r2.strictlyLeftOf(r1));
        }
    }

// ==================== КАСАНИЕ ГРАНИЦ ====================

    @Nested
    @DisplayName("Касание границ")
    class BoundaryTouchingTests {

        @Test
        @DisplayName("Касание: первая исключающая ), вторая включающая [ — true")
        void touchFirstExclusiveSecondInclusive() {
            // [01-01, 01-10) и [01-10, 01-15)
            // Точка 01-10 не входит в первый, общих точек нет
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-15", "[)");

            assertTrue(r1.strictlyLeftOf(r2));
            assertFalse(r2.strictlyLeftOf(r1));
        }

        @Test
        @DisplayName("Касание: обе включающие ] и [ — false")
        void touchBothInclusive() {
            // [01-01, 01-10] и [01-10, 01-15)
            // Точка 01-10 входит в оба диапазона
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-15", "[)");

            assertFalse(r1.strictlyLeftOf(r2));
        }

        @Test
        @DisplayName("Касание: первая включающая ], вторая исключающая ( — true")
        void touchFirstInclusiveSecondExclusive() {
            // [01-01, 01-10] и (01-10, 01-15)
            // Точка 01-10 входит в первый, но не входит во второй
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-15", "()");

            assertTrue(r1.strictlyLeftOf(r2));
        }

        @Test
        @DisplayName("Касание: обе исключающие ) и ( — true")
        void touchBothExclusive() {
            // [01-01, 01-10) и (01-10, 01-15)
            // Точка 01-10 не входит ни в один диапазон
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-15", "()");

            assertTrue(r1.strictlyLeftOf(r2));
        }

        @ParameterizedTest
        @CsvSource({
                // bounds1, bounds2, ожидается strictlyLeftOf
                "'[)', '[)', true",   // ) и [ — нет общей точки
                "'[)', '()', true",   // ) и ( — нет общей точки
                "'[]', '[)', false",  // ] и [ — общая точка есть
                "'[]', '()', true",   // ] и ( — нет общей точки
                "'()', '[)', true",   // ) и [ — нет общей точки
                "'()', '()', true",   // ) и ( — нет общей точки
                "'(]', '[)', false",  // ] и [ — общая точка есть
                "'(]', '()', true",   // ] и ( — нет общей точки
        })
        @DisplayName("Параметризованный тест касания границ")
        void boundaryTouchingParameterized(String bounds1, String bounds2, boolean expected) {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", bounds1);
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-15", bounds2);

            assertEquals(expected, r1.strictlyLeftOf(r2),
                    "Для bounds1=" + bounds1 + ", bounds2=" + bounds2);
        }
    }

// ==================== ПУСТЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Пустые диапазоны")
    class EmptyRangeTests {

        @Test
        void emptyStrictlyLeftOfNonEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange nonEmpty = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertFalse(empty.strictlyLeftOf(nonEmpty));
        }

        @Test
        void nonEmptyStrictlyLeftOfEmpty() {
            TsRange nonEmpty = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertFalse(nonEmpty.strictlyLeftOf(empty));
        }

        @Test
        @DisplayName("Пустой диапазон строго слева от пустого — true")
        void emptyStrictlyLeftOfEmpty() {
            TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange empty2 = TsRange.of("2026-12-31", "2026-12-31", "()");

            assertFalse(empty1.strictlyLeftOf(empty2));
            assertFalse(empty2.strictlyLeftOf(empty1));
        }
    }

// ==================== БЕСКОНЕЧНЫЕ ГРАНИЦЫ ====================

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Бесконечная верхняя граница: НЕ строго слева — false")
        void infiniteUpperNotStrictlyLeft() {
            TsRange infinite = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );
            TsRange finite = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertFalse(infinite.strictlyLeftOf(finite));
        }

        @Test
        @DisplayName("Бесконечная нижняя граница у второго: НЕ строго слева — false")
        void infiniteLowerOfSecondNotStrictlyLeft() {
            TsRange finite = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 20, 0, 0),
                    "()"
            );

            assertFalse(finite.strictlyLeftOf(infinite));
        }

        @Test
        @DisplayName("Бесконечный влево строго слева от бесконечного вправо — true")
        void infiniteLeftStrictlyLeftOfInfiniteRight() {
            TsRange left = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "()"
            );
            TsRange right = TsRange.of(
                    LocalDateTime.of(2026, 1, 15, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );

            assertTrue(left.strictlyLeftOf(right));
            assertFalse(right.strictlyLeftOf(left));
        }

        @Test
        @DisplayName("Полностью бесконечный НЕ строго слева от чего-либо — false")
        void fullyInfiniteNotStrictlyLeft() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );
            TsRange finite = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(infinite.strictlyLeftOf(finite));
        }
    }

// ==================== ДИАПАЗОН ИЗ ОДНОЙ ТОЧКИ ====================

    @Nested
    @DisplayName("Диапазон из одной точки")
    class SinglePointRangeTests {

        @Test
        @DisplayName("Точка строго слева от диапазона — true")
        void singlePointStrictlyLeftOfRange() {
            TsRange single = TsRange.of("2026-01-05", "2026-01-05", "[]");
            TsRange range = TsRange.of("2026-01-10", "2026-01-15", "[)");

            assertTrue(single.strictlyLeftOf(range));
        }

        @Test
        @DisplayName("Точка строго слева от диапазона с касанием — зависит от границ")
        void singlePointTouchingRange() {
            TsRange single = TsRange.of("2026-01-10", "2026-01-10", "[]");
            TsRange rangeInclusive = TsRange.of("2026-01-10", "2026-01-15", "[)");
            TsRange rangeExclusive = TsRange.of("2026-01-10", "2026-01-15", "()");

            assertFalse(single.strictlyLeftOf(rangeInclusive)); // общая точка
            assertTrue(single.strictlyLeftOf(rangeExclusive));  // общей точки нет
        }

        @Test
        @DisplayName("Диапазон строго слева от точки — true")
        void rangeStrictlyLeftOfSinglePoint() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange single = TsRange.of("2026-01-10", "2026-01-10", "[]");

            assertTrue(range.strictlyLeftOf(single));
        }
    }

// ==================== ОБРАБОТКА NULL ====================

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("strictlyLeftOf(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.strictlyLeftOf(null));
        }
    }

// ==================== СИММЕТРИЯ С >> ====================

    @Nested
    @DisplayName("Симметрия с strictlyRightOf")
    class SymmetryTests {

        @Test
        @DisplayName("a.strictlyLeftOf(b) <=> b.strictlyRightOf(a)")
        void symmetryWithStrictlyRightOf() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertTrue(r1.strictlyLeftOf(r2));

            assertFalse(r2.strictlyLeftOf(r1));
        }

        @Test
        @DisplayName("Для пересекающихся диапазонов оба false")
        void overlappingRangesBothFalse() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            assertFalse(r1.strictlyLeftOf(r2));
            assertFalse(r2.strictlyLeftOf(r1));
        }
    }

// ==================== СВЯЗЬ С ДРУГИМИ ОПЕРАТОРАМИ ====================

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("Если a.strictlyLeftOf(b), то НЕ a.overlaps(b)")
        void strictlyLeftImpliesNotOverlap() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertTrue(r1.strictlyLeftOf(r2));
            assertFalse(r1.overlaps(r2));
        }

        @Test
        @DisplayName("Если НЕ a.overlaps(b) и оба непустые, то a.strictlyLeftOf(b) ИЛИ a.strictlyRightOf(b)")
        void notOverlapImpliesStrictlyLeftOrRight() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertFalse(r1.overlaps(r2));
            assertTrue(r1.strictlyLeftOf(r2) );
        }

        @Test
        @DisplayName("Если a.strictlyLeftOf(b), то a.lessThan(b)")
        void strictlyLeftImpliesLessThan() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertTrue(r1.strictlyLeftOf(r2));
            assertTrue(r1.lessThan(r2));
        }
    }

// ==================== МАТЕМАТИЧЕСКИЕ СВОЙСТВА ====================

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Антирефлексивность: НЕ a.strictlyLeftOf(a)")
        void irreflexivity() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(range.strictlyLeftOf(range));
        }

        @Test
        @DisplayName("Асимметричность: если a.strictlyLeftOf(b), то НЕ b.strictlyLeftOf(a)")
        void asymmetry() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertTrue(r1.strictlyLeftOf(r2));
            assertFalse(r2.strictlyLeftOf(r1));
        }

        @Test
        @DisplayName("Транзитивность: если a.strictlyLeftOf(b) и b.strictlyLeftOf(c), то a.strictlyLeftOf(c)")
        void transitivity() {
            TsRange a = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange b = TsRange.of("2026-01-10", "2026-01-15", "[)");
            TsRange c = TsRange.of("2026-01-20", "2026-01-25", "[)");

            assertTrue(a.strictlyLeftOf(b));
            assertTrue(b.strictlyLeftOf(c));
            assertTrue(a.strictlyLeftOf(c));
        }
    }

// ==================== ТОЧНОЕ ВРЕМЯ ====================

    @Nested
    @DisplayName("Точное время")
    class ExactTimeTests {

        @Test
        @DisplayName("Строго слева с точным временем — true")
        void strictlyLeftWithExactTime() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 13, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );

            assertTrue(r1.strictlyLeftOf(r2));
        }

        @Test
        @DisplayName("Касание на точное время — true")
        void touchAtExactTime() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );

            assertTrue(r1.strictlyLeftOf(r2)); // ) и [ — нет общей точки
        }

        @Test
        @DisplayName("Пересечение на точное время — false")
        void overlapAtExactTime() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 30),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );

            assertFalse(r1.strictlyLeftOf(r2));
        }
    }
}
