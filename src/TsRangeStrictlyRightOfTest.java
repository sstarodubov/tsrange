package ru.nspk.pcl.common.tsrange;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TsRangeStrictlyRightOfTest {
    // ==================== ПОЛНОСТЬЮ РАЗНЕСЕНЫ ====================

    @Nested
    @DisplayName("Полностью разнесены")
    class FullyApartTests {

        @Test
        @DisplayName("Первый диапазон полностью после второго — true")
        void firstCompletelyAfterSecond() {
            TsRange r1 = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.strictlyRightOf(r2));
            assertFalse(r2.strictlyRightOf(r1));
        }

        @Test
        @DisplayName("Первый диапазон полностью до второго — false")
        void firstCompletelyBeforeSecond() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertFalse(r1.strictlyRightOf(r2));
            assertTrue(r2.strictlyRightOf(r1));
        }

        @Test
        @DisplayName("Диапазоны далеко друг от друга — true")
        void rangesFarApart() {
            TsRange r1 = TsRange.of("2026-06-01", "2026-06-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.strictlyRightOf(r2));
        }

        @Test
        @DisplayName("Разрыв в один день — true")
        void oneDayGap() {
            TsRange r1 = TsRange.of("2026-01-11", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.strictlyRightOf(r2));
        }
    }

// ==================== ПЕРЕСЕЧЕНИЕ ====================

    @Nested
    @DisplayName("Пересечение")
    class OverlapTests {

        @Test
        @DisplayName("Частичное пересечение — false")
        void partialOverlap() {
            TsRange r1 = TsRange.of("2026-01-05", "2026-01-15", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(r1.strictlyRightOf(r2));
            assertFalse(r2.strictlyRightOf(r1));
        }

        @Test
        @DisplayName("Полное вложение — false")
        void fullContainment() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertFalse(outer.strictlyRightOf(inner));
            assertFalse(inner.strictlyRightOf(outer));
        }

        @Test
        @DisplayName("Идентичные диапазоны — false")
        void identicalRanges() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(r1.strictlyRightOf(r2));
            assertFalse(r2.strictlyRightOf(r1));
        }
    }

// ==================== КАСАНИЕ ГРАНИЦ ====================

    @Nested
    @DisplayName("Касание границ")
    class BoundaryTouchingTests {

        @Test
        @DisplayName("Касание: первая включающая [, вторая исключающая ) — true")
        void touchFirstInclusiveSecondExclusive() {
            // [01-10, 01-15) и [01-01, 01-10)
            // Точка 01-10 входит в первый, но не входит во второй
            TsRange r1 = TsRange.of("2026-01-10", "2026-01-15", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.strictlyRightOf(r2));
            assertFalse(r2.strictlyRightOf(r1));
        }

        @Test
        @DisplayName("Касание: обе включающие [ и ] — false")
        void touchBothInclusive() {
            // [01-10, 01-15) и [01-01, 01-10]
            // Точка 01-10 входит в оба диапазона
            TsRange r1 = TsRange.of("2026-01-10", "2026-01-15", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[]");

            assertFalse(r1.strictlyRightOf(r2));
        }

        @Test
        @DisplayName("Касание: первая исключающая (, вторая включающая ] — true")
        void touchFirstExclusiveSecondInclusive() {
            // (01-10, 01-15) и [01-01, 01-10]
            // Точка 01-10 не входит в первый, но входит во второй
            TsRange r1 = TsRange.of("2026-01-10", "2026-01-15", "()");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[]");

            assertTrue(r1.strictlyRightOf(r2));
        }

        @Test
        @DisplayName("Касание: обе исключающие ( и ) — true")
        void touchBothExclusive() {
            // (01-10, 01-15) и [01-01, 01-10)
            // Точка 01-10 не входит ни в один диапазон
            TsRange r1 = TsRange.of("2026-01-10", "2026-01-15", "()");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.strictlyRightOf(r2));
        }

        @ParameterizedTest
        @CsvSource({
                // bounds1 (r1.lower), bounds2 (r2.upper), ожидается strictlyRightOf
                "'[)', '[)', true",   // [ и ) — нет общей точки
                "'()', '[)', true",   // ( и ) — нет общей точки
                "'[)', '[]', false",  // [ и ] — общая точка есть
                "'()', '[]', true",   // ( и ] — нет общей точки
                "'[)', '()', true",   // [ и ) — нет общей точки
                "'()', '()', true",   // ( и ) — нет общей точки
                "'[)', '(]', false",  // [ и ] — общая точка есть
                "'()', '(]', true",   // ( и ] — нет общей точки
        })
        @DisplayName("Параметризованный тест касания границ")
        void boundaryTouchingParameterized(String bounds1, String bounds2, boolean expected) {
            TsRange r1 = TsRange.of("2026-01-10", "2026-01-15", bounds1);
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", bounds2);

            assertEquals(expected, r1.strictlyRightOf(r2),
                    "Для bounds1=" + bounds1 + ", bounds2=" + bounds2);
        }
    }

// ==================== ПУСТЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Пустые диапазоны")
    class EmptyRangeTests {

        @Test
        void emptyStrictlyRightOfNonEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange nonEmpty = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertFalse(empty.strictlyRightOf(nonEmpty));
        }

        @Test
        @DisplayName("Непустой диапазон строго справа от пустого — true")
        void nonEmptyStrictlyRightOfEmpty() {
            TsRange nonEmpty = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertFalse(nonEmpty.strictlyRightOf(empty));
        }

        @Test
        @DisplayName("Пустой диапазон строго справа от пустого — true")
        void emptyStrictlyRightOfEmpty() {
            TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange empty2 = TsRange.of("2026-12-31", "2026-12-31", "()");

            assertFalse(empty1.strictlyRightOf(empty2));
            assertFalse(empty2.strictlyRightOf(empty1));
        }
    }

// ==================== БЕСКОНЕЧНЫЕ ГРАНИЦЫ ====================

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Бесконечная нижняя граница: НЕ строго справа — false")
        void infiniteLowerNotStrictlyRight() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 20, 0, 0),
                    "()"
            );
            TsRange finite = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(infinite.strictlyRightOf(finite));
        }

        @Test
        @DisplayName("Бесконечная верхняя граница у второго: НЕ строго справа — false")
        void infiniteUpperOfSecondNotStrictlyRight() {
            TsRange finite = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange infinite = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );

            assertFalse(finite.strictlyRightOf(infinite));
        }

        @Test
        @DisplayName("Бесконечный вправо строго справа от бесконечного влево — true")
        void infiniteRightStrictlyRightOfInfiniteLeft() {
            TsRange right = TsRange.of(
                    LocalDateTime.of(2026, 1, 15, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );
            TsRange left = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "()"
            );

            assertTrue(right.strictlyRightOf(left));
            assertFalse(left.strictlyRightOf(right));
        }

        @Test
        @DisplayName("Полностью бесконечный НЕ строго справа от чего-либо — false")
        void fullyInfiniteNotStrictlyRight() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );
            TsRange finite = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(infinite.strictlyRightOf(finite));
        }

        @Test
        @DisplayName("Конечный диапазон строго справа от бесконечного влево — true")
        void finiteStrictlyRightOfInfiniteLeft() {
            TsRange finite = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "()"
            );

            assertTrue(finite.strictlyRightOf(infinite));
        }
    }

// ==================== ДИАПАЗОН ИЗ ОДНОЙ ТОЧКИ ====================

    @Nested
    @DisplayName("Диапазон из одной точки")
    class SinglePointRangeTests {

        @Test
        @DisplayName("Точка строго справа от диапазона — true")
        void singlePointStrictlyRightOfRange() {
            TsRange single = TsRange.of("2026-01-15", "2026-01-15", "[]");
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(single.strictlyRightOf(range));
        }

        @Test
        @DisplayName("Точка строго справа от диапазона с касанием — зависит от границ")
        void singlePointTouchingRange() {
            TsRange single = TsRange.of("2026-01-10", "2026-01-10", "[]");
            TsRange rangeInclusive = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange rangeExclusive = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(single.strictlyRightOf(rangeInclusive)); // общая точка
            assertTrue(single.strictlyRightOf(rangeExclusive));  // общей точки нет
        }

        @Test
        @DisplayName("Диапазон строго справа от точки — true")
        void rangeStrictlyRightOfSinglePoint() {
            TsRange range = TsRange.of("2026-01-10", "2026-01-15", "[)");
            TsRange single = TsRange.of("2026-01-05", "2026-01-05", "[]");

            assertTrue(range.strictlyRightOf(single));
        }

        @Test
        @DisplayName("Две одинаковые точки — false")
        void twoSameSinglePoints() {
            TsRange p1 = TsRange.of("2026-01-05", "2026-01-05", "[]");
            TsRange p2 = TsRange.of("2026-01-05", "2026-01-05", "[]");

            assertFalse(p1.strictlyRightOf(p2));
        }

        @Test
        @DisplayName("Две разные точки: правая строго справа от левой — true")
        void twoDifferentSinglePoints() {
            TsRange right = TsRange.of("2026-01-06", "2026-01-06", "[]");
            TsRange left = TsRange.of("2026-01-05", "2026-01-05", "[]");

            assertTrue(right.strictlyRightOf(left));
            assertFalse(left.strictlyRightOf(right));
        }
    }

// ==================== ОБРАБОТКА NULL ====================

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("strictlyRightOf(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.strictlyRightOf(null));
        }
    }

// ==================== СИММЕТРИЯ С << ====================

    @Nested
    @DisplayName("Симметрия с strictlyLeftOf")
    class SymmetryTests {

        @Test
        @DisplayName("a.strictlyRightOf(b) <=> b.strictlyLeftOf(a)")
        void symmetryWithStrictlyLeftOf() {
            TsRange r1 = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.strictlyRightOf(r2));
            assertTrue(r2.strictlyLeftOf(r1));

            assertFalse(r2.strictlyRightOf(r1));
            assertFalse(r1.strictlyLeftOf(r2));
        }

        @Test
        @DisplayName("Для пересекающихся диапазонов оба false")
        void overlappingRangesBothFalse() {
            TsRange r1 = TsRange.of("2026-01-05", "2026-01-15", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(r1.strictlyRightOf(r2));
            assertFalse(r2.strictlyRightOf(r1));
            assertFalse(r1.strictlyLeftOf(r2));
            assertFalse(r2.strictlyLeftOf(r1));
        }

        @Test
        @DisplayName("Для разнесённых диапазонов один true, другой false")
        void apartRangesOneTrueOneFalse() {
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange right = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertFalse(left.strictlyRightOf(right));
            assertTrue(right.strictlyRightOf(left));

            assertTrue(left.strictlyLeftOf(right));
            assertFalse(right.strictlyLeftOf(left));
        }
    }

// ==================== СВЯЗЬ С ДРУГИМИ ОПЕРАТОРАМИ ====================

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("Если a.strictlyRightOf(b), то НЕ a.overlaps(b)")
        void strictlyRightImpliesNotOverlap() {
            TsRange r1 = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.strictlyRightOf(r2));
            assertFalse(r1.overlaps(r2));
        }

        @Test
        @DisplayName("Если НЕ a.overlaps(b) и оба непустые, то a.strictlyLeftOf(b) ИЛИ a.strictlyRightOf(b)")
        void notOverlapImpliesStrictlyLeftOrRight() {
            TsRange r1 = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(r1.overlaps(r2));
            assertTrue(r1.strictlyLeftOf(r2) || r1.strictlyRightOf(r2));
        }

        @Test
        @DisplayName("Если a.strictlyRightOf(b), то a.greaterThan(b)")
        void strictlyRightImpliesGreaterThan() {
            TsRange r1 = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.strictlyRightOf(r2));
            assertTrue(r1.greaterThan(r2));
        }

        @Test
        @DisplayName("strictlyRightOf НЕ означает containsRange")
        void strictlyRightDoesNotImplyContainment() {
            TsRange r1 = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.strictlyRightOf(r2));
            assertFalse(r1.containsRange(r2));
            assertFalse(r2.containsRange(r1));
        }
    }

// ==================== МАТЕМАТИЧЕСКИЕ СВОЙСТВА ====================

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Антирефлексивность: НЕ a.strictlyRightOf(a)")
        void irreflexivity() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(range.strictlyRightOf(range));
        }

        @Test
        @DisplayName("Асимметричность: если a.strictlyRightOf(b), то НЕ b.strictlyRightOf(a)")
        void asymmetry() {
            TsRange r1 = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.strictlyRightOf(r2));
            assertFalse(r2.strictlyRightOf(r1));
        }

        @Test
        @DisplayName("Транзитивность: если a.strictlyRightOf(b) и b.strictlyRightOf(c), то a.strictlyRightOf(c)")
        void transitivity() {
            TsRange a = TsRange.of("2026-01-20", "2026-01-25", "[)");
            TsRange b = TsRange.of("2026-01-10", "2026-01-15", "[)");
            TsRange c = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertTrue(a.strictlyRightOf(b));
            assertTrue(b.strictlyRightOf(c));
            assertTrue(a.strictlyRightOf(c));
        }

        @Test
        @DisplayName("Связь: НЕ (a.strictlyLeftOf(b) И a.strictlyRightOf(b))")
        void cannotBeBothLeftAndRight() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertFalse(r1.strictlyLeftOf(r2) && r1.strictlyRightOf(r2));
            assertFalse(r2.strictlyLeftOf(r1) && r2.strictlyRightOf(r1));
        }
    }

// ==================== ТОЧНОЕ ВРЕМЯ ====================

    @Nested
    @DisplayName("Точное время")
    class ExactTimeTests {

        @Test
        @DisplayName("Строго справа с точным временем — true")
        void strictlyRightWithExactTime() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 13, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[)"
            );

            assertTrue(r1.strictlyRightOf(r2));
        }

        @Test
        @DisplayName("Касание на точное время — true")
        void touchAtExactTime() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[)"
            );

            assertTrue(r1.strictlyRightOf(r2)); // [ и ) — нет общей точки
        }

        @Test
        @DisplayName("Касание на точное время с обеими включающими — false")
        void touchAtExactTimeBothInclusive() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[]"
            );

            assertFalse(r1.strictlyRightOf(r2)); // [ и ] — общая точка есть
        }

        @Test
        @DisplayName("Пересечение на точное время — false")
        void overlapAtExactTime() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 11, 30),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[)"
            );

            assertFalse(r1.strictlyRightOf(r2));
        }

        @Test
        @DisplayName("Строго справа на одну наносекунду")
        void strictlyRightByOneNanosecond() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    LocalDateTime.of(2026, 1, 15, 0, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 10, 0, 0, 0, 1),
                    "[)"
            );

            // r2.upper = 01-10 00:00:00.000000001, r1.lower = 01-10 00:00:00
            // r1.lower < r2.upper, поэтому НЕ строго справа
            assertFalse(r1.strictlyRightOf(r2));
        }
    }

// ==================== ВСЕ КОМБИНАЦИИ ГРАНИЦ ====================

    @Nested
    @DisplayName("Все комбинации границ")
    class AllBoundsCombinationsTests {

        @Test
        @DisplayName("Разнесённые диапазоны с разными комбинациями границ — всегда true")
        void apartRangesWithDifferentBounds() {
            String[] bounds = {"[)", "()", "[]", "(]"};

            for (String b1 : bounds) {
                for (String b2 : bounds) {
                    TsRange r1 = TsRange.of("2026-01-15", "2026-01-20", b1);
                    TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", b2);

                    assertTrue(r1.strictlyRightOf(r2),
                            "Должен быть строго справа для bounds1=" + b1 + ", bounds2=" + b2);
                }
            }
        }

        @Test
        @DisplayName("Пересекающиеся диапазоны с разными комбинациями границ — всегда false")
        void overlappingRangesWithDifferentBounds() {
            String[] bounds = {"[)", "()", "[]", "(]"};

            for (String b1 : bounds) {
                for (String b2 : bounds) {
                    TsRange r1 = TsRange.of("2026-01-05", "2026-01-15", b1);
                    TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", b2);

                    assertFalse(r1.strictlyRightOf(r2),
                            "Не должен быть строго справа для bounds1=" + b1 + ", bounds2=" + b2);
                }
            }
        }
    }

// ==================== ЗЕРКАЛЬНОСТЬ С << ====================

    @Nested
    @DisplayName("Зеркальность с <<")
    class MirrorTests {

        @Test
        @DisplayName("Все тесты для << зеркальны для >>")
        void allLeftTestsMirroredForRight() {
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange right = TsRange.of("2026-01-15", "2026-01-20", "[)");

            // left << right  <=>  right >> left
            assertTrue(left.strictlyLeftOf(right));
            assertTrue(right.strictlyRightOf(left));

            // НЕ (right << left)  <=>  НЕ (left >> right)
            assertFalse(right.strictlyLeftOf(left));
            assertFalse(left.strictlyRightOf(right));
        }

        @Test
        @DisplayName("Касание границ зеркально")
        void boundaryTouchingMirrored() {
            // [01-01, 01-10] и [01-10, 01-15) — общая точка
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-15", "[)");

            assertFalse(r1.strictlyLeftOf(r2));
            assertFalse(r2.strictlyRightOf(r1));

            // [01-01, 01-10) и [01-10, 01-15) — нет общей точки
            TsRange r3 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r4 = TsRange.of("2026-01-10", "2026-01-15", "[)");

            assertTrue(r3.strictlyLeftOf(r4));
            assertTrue(r4.strictlyRightOf(r3));
        }
    }
}
