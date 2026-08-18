package ru.nspk.pcl.common.tsrange;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
public class TsRangeIsAdjacentToTest {

    // ==================== СМЕЖНЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Смежные диапазоны")
    class AdjacentRangesTests {

        @Test
        @DisplayName("Первый слева, второй справа: ) и [ стыкуются — true")
        void firstLeftSecondRightExclusiveInclusive() {
            // [01-01, 01-10) и [01-10, 01-20)
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(r1.isAdjacentTo(r2));
            assertTrue(r2.isAdjacentTo(r1)); // симметричность
        }

        @Test
        @DisplayName("Первый слева, второй справа: ] и ( стыкуются — true")
        void firstLeftSecondRightInclusiveExclusive() {
            // [01-01, 01-10] и (01-10, 01-20)
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "()");

            assertTrue(r1.isAdjacentTo(r2));
            assertTrue(r2.isAdjacentTo(r1));
        }

        @Test
        @DisplayName("Второй слева, первый справа: ) и [ стыкуются — true")
        void secondLeftFirstRight() {
            // [01-10, 01-20) и [01-01, 01-10)
            TsRange r1 = TsRange.of("2026-01-10", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.isAdjacentTo(r2));
            assertTrue(r2.isAdjacentTo(r1));
        }

        @Test
        @DisplayName("Смежность с точным временем — true")
        void adjacentWithExactTime() {
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

            assertTrue(r1.isAdjacentTo(r2));
        }
    }

// ==================== НЕ СМЕЖНЫЕ: ДЫРКА ====================

    @Nested
    @DisplayName("Не смежные: дырка в точке стыка")
    class NotAdjacentGapTests {

        @Test
        @DisplayName("Обе исключающие ) и ( — дырка — false")
        void bothExclusiveGap() {
            // [01-01, 01-10) и (01-10, 01-20)
            // Точка 01-10 не входит ни в один диапазон
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "()");

            assertFalse(r1.isAdjacentTo(r2));
            assertFalse(r2.isAdjacentTo(r1));
        }

        @Test
        @DisplayName("Обе исключающие ( и ) — дырка — false")
        void bothExclusiveReverseGap() {
            // (01-01, 01-10) и (01-10, 01-20)
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "()");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "()");

            assertFalse(r1.isAdjacentTo(r2));
        }
    }

// ==================== НЕ СМЕЖНЫЕ: ПЕРЕСЕЧЕНИЕ ====================

    @Nested
    @DisplayName("Не смежные: пересечение в точке стыка")
    class NotAdjacentOverlapTests {

        @Test
        @DisplayName("Обе включающие ] и [ — пересечение — false")
        void bothInclusiveOverlap() {
            // [01-01, 01-10] и [01-10, 01-20)
            // Точка 01-10 входит в оба диапазона
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertFalse(r1.isAdjacentTo(r2));
            assertFalse(r2.isAdjacentTo(r1));
        }

        @Test
        @DisplayName("Обе включающие [ и ] — пересечение — false")
        void bothInclusiveReverseOverlap() {
            // [01-01, 01-10] и [01-10, 01-20]
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[]");

            assertFalse(r1.isAdjacentTo(r2));
        }

        @Test
        @DisplayName("Частичное пересечение — false")
        void partialOverlap() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-15", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertFalse(r1.isAdjacentTo(r2));
        }

        @Test
        @DisplayName("Полное пересечение — false")
        void fullOverlap() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertFalse(r1.isAdjacentTo(r2));
        }
    }

// ==================== НЕ СМЕЖНЫЕ: РАЗРЫВ ====================

    @Nested
    @DisplayName("Не смежные: разрыв между диапазонами")
    class NotAdjacentSeparateTests {

        @Test
        @DisplayName("Разрыв между диапазонами — false")
        void gapBetweenRanges() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertFalse(r1.isAdjacentTo(r2));
            assertFalse(r2.isAdjacentTo(r1));
        }

        @Test
        @DisplayName("Разрыв в один день — false")
        void oneDayGap() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-11", "2026-01-20", "[)");

            assertFalse(r1.isAdjacentTo(r2));
        }

        @Test
        @DisplayName("Диапазоны далеко друг от друга — false")
        void rangesFarApart() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-06-01", "2026-06-10", "[)");

            assertFalse(r1.isAdjacentTo(r2));
        }
    }

// ==================== ИДЕНТИЧНЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Идентичные диапазоны")
    class IdenticalRangesTests {

        @Test
        @DisplayName("Идентичные диапазоны не смежные — false")
        void identicalRangesNotAdjacent() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(r1.isAdjacentTo(r2));
        }

        @Test
        @DisplayName("Диапазон не смежен сам с собой — false")
        void rangeNotAdjacentToSelf() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(range.isAdjacentTo(range));
        }
    }

// ==================== ПАРАМЕТРИЗОВАННЫЙ ТЕСТ ====================

    @Nested
    @DisplayName("Параметризованный тест стыков")
    class BoundaryParameterizedTests {

        @ParameterizedTest
        @CsvSource({
                // bounds1 (r1), bounds2 (r2), ожидается isAdjacentTo
                "'[)', '[)', true",   // ) и [ — стыкуются
                "'[]', '()', true",   // ] и ( — стыкуются
                "'[)', '()', false",  // ) и ( — дырка
                "'[]', '[)', false",  // ] и [ — пересечение
                "'()', '[)', true",   // ) и [ — стыкуются (r1.upper=01-10, r2.lower=01-10)
                "'()', '()', false",  // ) и ( — дырка
                "'(]', '[)', false",   // ] и [ — но r1.upper=01-10, r2.lower=01-10, ] и [ пересечение... подожди
                "'(]', '()', true",  // ] и ( — но r1.upper=01-10, r2.lower=01-10, ] и ( стыкуются...
        })
        @DisplayName("Параметризованный тест стыков при r1.upper == r2.lower")
        void boundaryParameterized(String bounds1, String bounds2, boolean expected) {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", bounds1);
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", bounds2);

            assertEquals(expected, r1.isAdjacentTo(r2),
                    "Для bounds1=" + bounds1 + ", bounds2=" + bounds2);
        }
    }

// ==================== ПУСТЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Пустые диапазоны")
    class EmptyRangeTests {

        @Test
        @DisplayName("Пустой диапазон не смежен с непустым — false")
        void emptyNotAdjacentToNonEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(empty.isAdjacentTo(nonEmpty));
            assertFalse(nonEmpty.isAdjacentTo(empty));
        }

        @Test
        @DisplayName("Два пустых диапазона не смежные — false")
        void emptyNotAdjacentToEmpty() {
            TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange empty2 = TsRange.of("2026-01-10", "2026-01-10", "()");

            assertFalse(empty1.isAdjacentTo(empty2));
        }

        @Test
        @DisplayName("Пустой диапазон не смежен сам с собой — false")
        void emptyNotAdjacentToSelf() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertFalse(empty.isAdjacentTo(empty));
        }
    }

// ==================== БЕСКОНЕЧНЫЕ ГРАНИЦЫ ====================

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Бесконечная верхняя не стыкуется — false")
        void infiniteUpperNotAdjacent() {
            TsRange infinite = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );
            TsRange finite = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertFalse(infinite.isAdjacentTo(finite));
        }

        @Test
        @DisplayName("Бесконечная нижняя не стыкуется — false")
        void infiniteLowerNotAdjacent() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "()"
            );
            TsRange finite = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertFalse(infinite.isAdjacentTo(finite));
        }

        @Test
        @DisplayName("Конечный стык при бесконечной нижней — true")
        void finiteJunctionWithInfiniteLower() {
            // (, 01-10) и [01-10, 01-20)
            TsRange r1 = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "()"
            );
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(r1.isAdjacentTo(r2));
        }

        @Test
        @DisplayName("Конечный стык при бесконечной верхней — true")
        void finiteJunctionWithInfiniteUpper() {
            // [01-01, 01-10) и [01-10, )
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );

            assertTrue(r1.isAdjacentTo(r2));
        }

        @Test
        @DisplayName("Полностью бесконечный не смежен — false")
        void fullyInfiniteNotAdjacent() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );
            TsRange finite = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(infinite.isAdjacentTo(finite));
        }
    }

// ==================== ДИАПАЗОН ИЗ ОДНОЙ ТОЧКИ ====================

    @Nested
    @DisplayName("Диапазон из одной точки")
    class SinglePointRangeTests {

        @Test
        @DisplayName("Точка смежна с диапазоном, начинающимся после неё — true")
        void singlePointAdjacentToRangeAfter() {
            TsRange single = TsRange.of("2026-01-10", "2026-01-10", "[]");
            TsRange range = TsRange.of("2026-01-10", "2026-01-20", "()");

            assertTrue(single.isAdjacentTo(range));
        }

        @Test
        @DisplayName("Точка смежна с диапазоном, заканчивающимся на ней — true")
        void singlePointAdjacentToRangeBefore() {
            TsRange single = TsRange.of("2026-01-10", "2026-01-10", "[]");
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(single.isAdjacentTo(range));
        }

        @Test
        @DisplayName("Точка не смежна с диапазоном, включающим её — false")
        void singlePointNotAdjacentToContainingRange() {
            TsRange single = TsRange.of("2026-01-10", "2026-01-10", "[]");
            TsRange range = TsRange.of("2026-01-01", "2026-01-20", "[)");

            assertFalse(single.isAdjacentTo(range));
        }
    }

// ==================== ОБРАБОТКА NULL ====================

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("isAdjacentTo(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.isAdjacentTo(null));
        }
    }

// ==================== СИММЕТРИЯ ====================

    @Nested
    @DisplayName("Симметрия")
    class SymmetryTests {

        @Test
        @DisplayName("a.isAdjacentTo(b) == b.isAdjacentTo(a)")
        void adjacencyIsSymmetric() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");
            TsRange r3 = TsRange.of("2026-01-15", "2026-01-25", "[)");

            assertEquals(r1.isAdjacentTo(r2), r2.isAdjacentTo(r1));
            assertEquals(r1.isAdjacentTo(r3), r3.isAdjacentTo(r1));
        }
    }

// ==================== СВЯЗЬ С ДРУГИМИ ОПЕРАТОРАМИ ====================

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("Если a.isAdjacentTo(b), то НЕ a.overlaps(b)")
        void adjacentImpliesNotOverlap() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(r1.isAdjacentTo(r2));
            assertFalse(r1.overlaps(r2));
        }

        @Test
        @DisplayName("Если a.isAdjacentTo(b), то a.strictlyLeftOf(b) ИЛИ b.strictlyLeftOf(a)")
        void adjacentImpliesStrictlyLeftOrRight() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(r1.isAdjacentTo(r2));
            assertTrue(r1.strictlyLeftOf(r2) || r2.strictlyLeftOf(r1));
        }

        @Test
        @DisplayName("Если a.isAdjacentTo(b), то merge(a,b) дает непрерывный диапазон")
        void adjacentMergeGivesContinuousRange() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(r1.isAdjacentTo(r2));

            TsRange merged = r1.merge(r2);
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), merged.lower());
            assertEquals(LocalDateTime.of(2026, 1, 20, 0, 0), merged.upper());
            // merge смежных диапазонов дает непрерывный диапазон
        }

        @Test
        @DisplayName("Если НЕ a.overlaps(b) и НЕ a.isAdjacentTo(b), то разрыв")
        void notOverlapAndNotAdjacentMeansGap() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertFalse(r1.overlaps(r2));
            assertFalse(r1.isAdjacentTo(r2));
            // Между диапазонами есть разрыв
        }
    }

// ==================== МАТЕМАТИЧЕСКИЕ СВОЙСТВА ====================

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Антирефлексивность: НЕ a.isAdjacentTo(a)")
        void irreflexivity() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(range.isAdjacentTo(range));
        }

        @Test
        @DisplayName("Симметричность: a.isAdjacentTo(b) == b.isAdjacentTo(a)")
        void symmetry() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertEquals(r1.isAdjacentTo(r2), r2.isAdjacentTo(r1));
        }

        @Test
        @DisplayName("НЕ транзитивность: смежность не транзитивна")
        void notTransitive() {
            TsRange a = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange b = TsRange.of("2026-01-10", "2026-01-20", "[)");
            TsRange c = TsRange.of("2026-01-20", "2026-01-30", "[)");

            assertTrue(a.isAdjacentTo(b));
            assertTrue(b.isAdjacentTo(c));
            assertFalse(a.isAdjacentTo(c)); // a и c не смежны
        }
    }

// ==================== ТОЧНОЕ ВРЕМЯ ====================

    @Nested
    @DisplayName("Точное время")
    class ExactTimeTests {

        @Test
        @DisplayName("Смежность на точное время — true")
        void adjacentAtExactTime() {
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

            assertTrue(r1.isAdjacentTo(r2));
        }

        @Test
        @DisplayName("Не смежность: разрыв на одну наносекунду")
        void notAdjacentNanosecondGap() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 12, 0, 0, 1),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );

            assertFalse(r1.isAdjacentTo(r2)); // разрыв в 1 наносекунду
        }

        @Test
        @DisplayName("Не смежность: пересечение на одну наносекунду")
        void notAdjacentNanosecondOverlap() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0, 0, 1),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );

            assertFalse(r1.isAdjacentTo(r2)); // пересечение в 1 наносекунду
        }
    }

// ==================== ВСЕ КОМБИНАЦИИ ГРАНИЦ ====================

    @Nested
    @DisplayName("Все комбинации границ")
    class AllBoundsCombinationsTests {

        @Test
        @DisplayName("Перебор всех комбинаций границ для стыка")
        void allBoundsCombinationsForJunction() {
            String[] bounds = {"[)", "()", "[]", "(]"};

            for (String b1 : bounds) {
                for (String b2 : bounds) {
                    TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", b1);
                    TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", b2);

                    boolean upper1Inc = r1.upperInc();
                    boolean lower2Inc = r2.lowerInc();
                    boolean expected = upper1Inc != lower2Inc; // XOR

                    assertEquals(expected, r1.isAdjacentTo(r2),
                            "Для bounds1=" + b1 + ", bounds2=" + b2 +
                                    ", upper1Inc=" + upper1Inc + ", lower2Inc=" + lower2Inc);
                }
            }
        }
    }
}
