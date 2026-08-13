package ru.starodubov;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TsRangeOverlapsTest {

    // ==================== ЧАСТИЧНОЕ ПЕРЕСЕЧЕНИЕ ====================

    @Nested
    @DisplayName("Частичное пересечение")
    class PartialOverlapTests {

        @Test
        @DisplayName("Диапазоны пересекаются в середине")
        void rangesOverlapInMiddle() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            assertTrue(r1.overlaps(r2));
            assertTrue(r2.overlaps(r1));
        }

        @Test
        @DisplayName("Первый начинается раньше, заканчивается внутри второго")
        void firstStartsEarlierEndsInsideSecond() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-20", "[)");

            assertTrue(r1.overlaps(r2));
        }

        @Test
        @DisplayName("Первый начинается внутри второго, заканчивается позже")
        void firstStartsInsideSecondEndsLater() {
            TsRange r1 = TsRange.of("2026-01-05", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.overlaps(r2));
        }

        @Test
        @DisplayName("Пересечение на одну наносекунду")
        void overlapByOneNanosecond() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 10, 0, 0, 0, 1),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    LocalDateTime.of(2026, 1, 15, 0, 0),
                    "[)"
            );

            assertTrue(r1.overlaps(r2));
        }
    }

// ==================== ПОЛНОЕ ВЛОЖЕНИЕ ====================

    @Nested
    @DisplayName("Полное вложение")
    class FullContainmentTests {

        @Test
        @DisplayName("Второй диапазон полностью внутри первого")
        void secondFullyInsideFirst() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(outer.overlaps(inner));
            assertTrue(inner.overlaps(outer));
        }

        @Test
        @DisplayName("Первый диапазон полностью внутри второго")
        void firstFullyInsideSecond() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(inner.overlaps(outer));
        }

        @Test
        @DisplayName("Вложенный диапазон касается границ внешнего")
        void innerTouchesOuterBoundaries() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(outer.overlaps(inner));
        }
    }

// ==================== ИДЕНТИЧНЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Идентичные диапазоны")
    class IdenticalRangesTests {

        @Test
        @DisplayName("Диапазон пересекается сам с собой")
        void rangeOverlapsSelf() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(range.overlaps(range));
        }

        @Test
        @DisplayName("Два одинаковых диапазона пересекаются")
        void twoIdenticalRangesOverlap() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.overlaps(r2));
            assertTrue(r2.overlaps(r1));
        }

        @Test
        @DisplayName("Одинаковые значения с разной включительностью пересекаются")
        void sameValuesDifferentInclusivityOverlap() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[]");

            assertTrue(r1.overlaps(r2));
        }
    }

// ==================== НЕТ ПЕРЕСЕЧЕНИЯ ====================

    @Nested
    @DisplayName("Нет пересечения")
    class NoOverlapTests {

        @Test
        @DisplayName("Первый диапазон полностью до второго")
        void firstCompletelyBeforeSecond() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertFalse(r1.overlaps(r2));
            assertFalse(r2.overlaps(r1));
        }

        @Test
        @DisplayName("Первый диапазон полностью после второго")
        void firstCompletelyAfterSecond() {
            TsRange r1 = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(r1.overlaps(r2));
            assertFalse(r2.overlaps(r1));
        }

        @Test
        @DisplayName("Диапазоны далеко друг от друга")
        void rangesFarApart() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-06-01", "2026-06-10", "[)");

            assertFalse(r1.overlaps(r2));
        }

        @Test
        @DisplayName("Разрыв в один день")
        void oneDayGap() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-11", "2026-01-20", "[)");

            assertFalse(r1.overlaps(r2));
        }
    }

// ==================== КАСАНИЕ ГРАНИЦ ====================

    @Nested
    @DisplayName("Касание границ")
    class BoundaryTouchingTests {

        @Test
        @DisplayName("Касание: первая исключающая ), вторая включающая [ — false")
        void touchFirstExclusiveSecondInclusive() {
            // [01-01, 01-10) и [01-10, 01-15)
            // Точка 01-10 не входит в первый
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-15", "[)");

            assertFalse(r1.overlaps(r2));
            assertFalse(r2.overlaps(r1));
        }

        @Test
        @DisplayName("Касание: первая включающая ], вторая исключающая ( — false")
        void touchFirstInclusiveSecondExclusive() {
            // [01-01, 01-10] и (01-10, 01-15)
            // Точка 01-10 входит в первый, но не входит во второй
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-15", "()");

            assertFalse(r1.overlaps(r2));
            assertFalse(r2.overlaps(r1));
        }

        @Test
        @DisplayName("Касание: обе включающие ] и [ — true")
        void touchBothInclusive() {
            // [01-01, 01-10] и [01-10, 01-15)
            // Точка 01-10 входит в оба диапазона
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-15", "[)");

            assertTrue(r1.overlaps(r2));
            assertTrue(r2.overlaps(r1));
        }

        @Test
        @DisplayName("Касание: обе исключающие ) и ( — false")
        void touchBothExclusive() {
            // [01-01, 01-10) и (01-10, 01-15)
            // Точка 01-10 не входит ни в один
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-15", "()");

            assertFalse(r1.overlaps(r2));
            assertFalse(r2.overlaps(r1));
        }

        @ParameterizedTest
        @CsvSource({
                // bounds1, bounds2, ожидается пересечение
                "'[)', '[)', false",  // ) и [ — нет общей точки
                "'[)', '()', false",  // ) и ( — нет общей точки
                "'[]', '[)', true",   // ] и [ — общая точка есть
                "'[]', '()', false",  // ] и ( — нет общей точки
                "'(]', '[)', true",  // ] и [ — но нижняя ( не входит... подожди
                "'()', '[)', false",  // ) и [ — нет общей точки
        })
        @DisplayName("Параметризованный тест касания границ")
        void boundaryTouchingParameterized(String bounds1, String bounds2, boolean expected) {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", bounds1);
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-15", bounds2);

            assertEquals(expected, r1.overlaps(r2),
                    "Для bounds1=" + bounds1 + ", bounds2=" + bounds2);
        }
    }

// ==================== ПУСТЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Пустые диапазоны")
    class EmptyRangeTests {

        @Test
        @DisplayName("Пустой диапазон не пересекается с непустым")
        void emptyNotOverlapsNonEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(empty.overlaps(nonEmpty));
            assertFalse(nonEmpty.overlaps(empty));
        }

        @Test
        @DisplayName("Два пустых диапазона не пересекаются")
        void emptyNotOverlapsEmpty() {
            TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange empty2 = TsRange.of("2026-01-05", "2026-01-05", "()");

            assertFalse(empty1.overlaps(empty2));
            assertFalse(empty2.overlaps(empty1));
        }

        @Test
        @DisplayName("Пустой диапазон не пересекается сам с собой")
        void emptyNotOverlapsSelf() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertFalse(empty.overlaps(empty));
        }

        @Test
        @DisplayName("Пустой диапазон внутри непустого не пересекается")
        void emptyInsideNonEmptyNotOverlaps() {
            TsRange empty = TsRange.of("2026-01-05", "2026-01-05", "[)");
            TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(empty.overlaps(nonEmpty));
            assertFalse(nonEmpty.overlaps(empty));
        }
    }

// ==================== БЕСКОНЕЧНЫЕ ГРАНИЦЫ ====================

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Бесконечный вправо пересекается с конечным справа")
        void infiniteRightOverlapsFiniteRight() {
            TsRange infinite = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );
            TsRange finite = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertTrue(infinite.overlaps(finite));
        }

        @Test
        @DisplayName("Бесконечный вправо не пересекается с конечным слева")
        void infiniteRightNotOverlapsFiniteLeft() {
            TsRange infinite = TsRange.of(
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );
            TsRange finite = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertFalse(infinite.overlaps(finite));
        }

        @Test
        @DisplayName("Бесконечный влево пересекается с конечным слева")
        void infiniteLeftOverlapsFiniteLeft() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "()"
            );
            TsRange finite = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertTrue(infinite.overlaps(finite));
        }

        @Test
        @DisplayName("Бесконечный влево не пересекается с конечным справа")
        void infiniteLeftNotOverlapsFiniteRight() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "()"
            );
            TsRange finite = TsRange.of("2026-01-10", "2026-01-15", "[)");

            assertFalse(infinite.overlaps(finite));
        }

        @Test
        @DisplayName("Полностью бесконечный пересекается с любым непустым")
        void fullyInfiniteOverlapsAnyNonEmpty() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );
            TsRange finite = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(infinite.overlaps(finite));
        }

        @Test
        @DisplayName("Полностью бесконечный не пересекается с пустым")
        void fullyInfiniteNotOverlapsEmpty() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertFalse(infinite.overlaps(empty));
        }

        @Test
        @DisplayName("Два бесконечных диапазона пересекаются")
        void twoInfiniteRangesOverlap() {
            TsRange r1 = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "()"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );

            assertTrue(r1.overlaps(r2));
        }

        @Test
        @DisplayName("Бесконечные диапазоны не пересекаются, если разнесены")
        void infiniteRangesNotOverlapIfApart() {
            TsRange r1 = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "()"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );

            assertFalse(r1.overlaps(r2));
        }
    }

// ==================== ДИАПАЗОН ИЗ ОДНОЙ ТОЧКИ ====================

    @Nested
    @DisplayName("Диапазон из одной точки")
    class SinglePointRangeTests {

        @Test
        @DisplayName("Точка внутри диапазона — пересекаются")
        void singlePointInsideRange() {
            TsRange single = TsRange.of("2026-01-05", "2026-01-05", "[]");
            TsRange larger = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(single.overlaps(larger));
            assertTrue(larger.overlaps(single));
        }

        @Test
        @DisplayName("Точка вне диапазона — не пересекаются")
        void singlePointOutsideRange() {
            TsRange single = TsRange.of("2026-01-15", "2026-01-15", "[]");
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(single.overlaps(range));
            assertFalse(range.overlaps(single));
        }

        @Test
        @DisplayName("Точка на исключающей границе — не пересекаются")
        void singlePointOnExclusiveBoundary() {
            TsRange single = TsRange.of("2026-01-10", "2026-01-10", "[]");
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(single.overlaps(range));
        }

        @Test
        @DisplayName("Точка на включающей границе — пересекаются")
        void singlePointOnInclusiveBoundary() {
            TsRange single = TsRange.of("2026-01-10", "2026-01-10", "[]");
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[]");

            assertTrue(single.overlaps(range));
        }

        @Test
        @DisplayName("Две одинаковые точки — пересекаются")
        void twoSameSinglePoints() {
            TsRange p1 = TsRange.of("2026-01-05", "2026-01-05", "[]");
            TsRange p2 = TsRange.of("2026-01-05", "2026-01-05", "[]");

            assertTrue(p1.overlaps(p2));
        }

        @Test
        @DisplayName("Две разные точки — не пересекаются")
        void twoDifferentSinglePoints() {
            TsRange p1 = TsRange.of("2026-01-05", "2026-01-05", "[]");
            TsRange p2 = TsRange.of("2026-01-06", "2026-01-06", "[]");

            assertFalse(p1.overlaps(p2));
        }
    }

// ==================== ОБРАБОТКА NULL ====================

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("overlaps(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.overlaps(null));
        }
    }

// ==================== СИММЕТРИЧНОСТЬ ====================

    @Nested
    @DisplayName("Симметричность")
    class SymmetryTests {

        @Test
        @DisplayName("a.overlaps(b) всегда равно b.overlaps(a)")
        void overlapsIsSymmetric() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");
            TsRange r3 = TsRange.of("2026-01-20", "2026-01-25", "[)");
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertEquals(r1.overlaps(r2), r2.overlaps(r1));
            assertEquals(r1.overlaps(r3), r3.overlaps(r1));
            assertEquals(r2.overlaps(r3), r3.overlaps(r2));
            assertEquals(r1.overlaps(empty), empty.overlaps(r1));
        }
    }

// ==================== СВЯЗЬ С ДРУГИМИ ОПЕРАТОРАМИ ====================

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("Если a.containsRange(b) и b непустой, то a.overlaps(b)")
        void containmentImpliesOverlap() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(outer.containsRange(inner));
            assertTrue(outer.overlaps(inner));
        }

        @Test
        @DisplayName("Если a.containedBy(b) и a непустой, то a.overlaps(b)")
        void containedByImpliesOverlap() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(inner.rangeIsContainedBy(outer));
            assertTrue(inner.overlaps(outer));
        }

        @Test
        @DisplayName("overlap НЕ означает containment")
        void overlapDoesNotImplyContainment() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            assertTrue(r1.overlaps(r2));
            assertFalse(r1.containsRange(r2));
            assertFalse(r2.containsRange(r1));
        }

        @Test
        @DisplayName("overlap НЕ означает равенство")
        void overlapDoesNotImplyEquality() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            assertTrue(r1.overlaps(r2));
            assertFalse(r1.isEqual(r2));
        }
    }

// ==================== МАТЕМАТИЧЕСКИЕ СВОЙСТВА ====================

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Рефлексивность для непустых: a.overlaps(a) == true")
        void reflexivityForNonEmpty() {
            TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(nonEmpty.overlaps(nonEmpty));
        }

        @Test
        @DisplayName("Отсутствие рефлексивности для пустых: empty.overlaps(empty) == false")
        void noReflexivityForEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertFalse(empty.overlaps(empty));
        }

        @Test
        @DisplayName("Симметричность: a.overlaps(b) == b.overlaps(a)")
        void symmetry() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            assertEquals(r1.overlaps(r2), r2.overlaps(r1));
        }

        @Test
        @DisplayName("overlap НЕ транзитивен")
        void overlapNotTransitive() {
            // a пересекается с b, b пересекается с c, но a НЕ пересекается с c
            TsRange a = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange b = TsRange.of("2026-01-05", "2026-01-15", "[)");
            TsRange c = TsRange.of("2026-01-12", "2026-01-20", "[)");

            assertTrue(a.overlaps(b));  // a && b
            assertTrue(b.overlaps(c));  // b && c
            assertFalse(a.overlaps(c)); // a НЕ && c
        }
    }

// ==================== ТОЧНОЕ ВРЕМЯ ====================

    @Nested
    @DisplayName("Точное время")
    class ExactTimeTests {

        @Test
        @DisplayName("Пересечение с точным временем")
        void overlapWithExactTime() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 11, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );

            assertTrue(r1.overlaps(r2));
        }

        @Test
        @DisplayName("Нет пересечения с точным временем")
        void noOverlapWithExactTime() {
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

            assertFalse(r1.overlaps(r2));
        }

        @Test
        @DisplayName("Касание на точное время")
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

            assertFalse(r1.overlaps(r2)); // ) и [ — нет общей точки
        }
    }

// ==================== ВСЕ КОМБИНАЦИИ ГРАНИЦ ====================

    @Nested
    @DisplayName("Все комбинации границ")
    class AllBoundsCombinationsTests {

        @Test
        @DisplayName("Пересекающиеся диапазоны с разными комбинациями границ")
        void overlappingWithDifferentBounds() {
            String[] bounds = {"[)", "()", "[]", "(]"};

            for (String b1 : bounds) {
                for (String b2 : bounds) {
                    TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", b1);
                    TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", b2);

                    assertTrue(r1.overlaps(r2),
                            "Должны пересекаться для bounds1=" + b1 + ", bounds2=" + b2);
                }
            }
        }

        @Test
        @DisplayName("Непересекающиеся диапазоны с разными комбинациями границ")
        void nonOverlappingWithDifferentBounds() {
            String[] bounds = {"[)", "()", "[]", "(]"};

            for (String b1 : bounds) {
                for (String b2 : bounds) {
                    TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", b1);
                    TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", b2);

                    assertFalse(r1.overlaps(r2),
                            "Не должны пересекаться для bounds1=" + b1 + ", bounds2=" + b2);
                }
            }
        }
    }
}
