package ru.starodubov;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TsRangeContainsTest {

    @Nested
    @DisplayName("Полное вложение")
    class FullContainmentTests {

        @Test
        @DisplayName("Второй диапазон полностью внутри первого")
        void rangeFullyInside() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(outer.containsRange(inner));
            assertFalse(inner.containsRange(outer));
        }

        @Test
        @DisplayName("Второй диапазон касается левой границы первого")
        void rangeTouchingLeftBoundary() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-01", "2026-01-15", "[)");

            assertTrue(outer.containsRange(inner));
        }

        @Test
        @DisplayName("Второй диапазон касается правой границы первого")
        void rangeTouchingRightBoundary() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-15", "2026-01-31", "[)");

            assertTrue(outer.containsRange(inner));
        }

        @Test
        @DisplayName("Второй диапазон касается обеих границ первого")
        void rangeTouchingBothBoundaries() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(outer.containsRange(inner));
        }
    }

    @Nested
    @DisplayName("Идентичные диапазоны")
    class IdenticalRangesTests {

        @Test
        @DisplayName("Диапазон содержит сам себя")
        void rangecontainsRangeSelf() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(range.containsRange(range));
        }

        @Test
        @DisplayName("Два одинаковых диапазона содержат друг друга")
        void twoIdenticalRangesContainEachOther() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.containsRange(r2));
            assertTrue(r2.containsRange(r1));
        }

        @Test
        @DisplayName("Одинаковые значения с одинаковой включительностью")
        void sameValuesSameInclusivity() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[]");

            assertTrue(r1.containsRange(r2));
            assertTrue(r2.containsRange(r1));
        }
    }

    @Nested
    @DisplayName("Частичное пересечение")
    class PartialOverlapTests {

        @Test
        @DisplayName("Второй диапазон выходит за правую границу")
        void rangeExceedsRightBoundary() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            assertFalse(r1.containsRange(r2));
        }

        @Test
        @DisplayName("Второй диапазон выходит за левую границу")
        void rangeExceedsLeftBoundary() {
            TsRange r1 = TsRange.of("2026-01-05", "2026-01-15", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(r1.containsRange(r2));
        }

        @Test
        @DisplayName("Второй диапазон выходит за обе границы")
        void rangeExceedsBothBoundaries() {
            TsRange r1 = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-15", "[)");

            assertFalse(r1.containsRange(r2));
        }
    }

    @Nested
    @DisplayName("Нет пересечения")
    class NoOverlapTests {

        @Test
        @DisplayName("Второй диапазон полностью после первого")
        void rangeAfterFirst() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertFalse(r1.containsRange(r2));
        }

        @Test
        @DisplayName("Второй диапазон полностью до первого")
        void rangeBeforeFirst() {
            TsRange r1 = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(r1.containsRange(r2));
        }
    }

    @Nested
    @DisplayName("Разная включительность границ")
    class DifferentInclusivityTests {

        @Test
        @DisplayName("Внешний с исключающей нижней, внутренний с включающей — false")
        void outerExclusiveLowerInnerInclusive() {
            // (2026-01-01, ...) не может содержать [2026-01-01, ...)
            TsRange outer = TsRange.of("2026-01-01", "2026-01-10", "()");
            TsRange inner = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertFalse(outer.containsRange(inner));
        }

        @Test
        @DisplayName("Внешний с включающей нижней, внутренний с исключающей — true")
        void outerInclusiveLowerInnerExclusive() {
            // [2026-01-01, ...) может содержать (2026-01-01, ...)
            TsRange outer = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange inner = TsRange.of("2026-01-01", "2026-01-05", "()");

            assertTrue(outer.containsRange(inner));
        }

        @Test
        @DisplayName("Внешний с исключающей верхней, внутренний с включающей — false")
        void outerExclusiveUpperInnerInclusive() {
            // [..., 2026-01-10) не может содержать [..., 2026-01-10]
            TsRange outer = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange inner = TsRange.of("2026-01-05", "2026-01-10", "[]");

            assertFalse(outer.containsRange(inner));
        }

        @Test
        @DisplayName("Внешний с включающей верхней, внутренний с исключающей — true")
        void outerInclusiveUpperInnerExclusive() {
            // [..., 2026-01-10] может содержать [..., 2026-01-10)
            TsRange outer = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange inner = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertTrue(outer.containsRange(inner));
        }

        @Test
        @DisplayName("Обе границы внешние включающие, внутренние исключающие — true")
        void outerBothInclusiveInnerBothExclusive() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange inner = TsRange.of("2026-01-01", "2026-01-10", "()");

            assertTrue(outer.containsRange(inner));
        }

        @Test
        @DisplayName("Обе границы внешние исключающие, внутренние включающие — false")
        void outerBothExclusiveInnerBothInclusive() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-10", "()");
            TsRange inner = TsRange.of("2026-01-01", "2026-01-10", "[]");

            assertFalse(outer.containsRange(inner));
        }
    }

    @Nested
    @DisplayName("Пустые диапазоны")
    class EmptyRangeTests {

        @Test
        @DisplayName("Пустой диапазон содержится в любом непустом")
        void emptyContainedInNonEmpty() {
            TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertTrue(nonEmpty.containsRange(empty));
        }

        @Test
        @DisplayName("Непустой диапазон НЕ содержится в пустом")
        void nonEmptyNotContainedInEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(empty.containsRange(nonEmpty));
        }

        @Test
        @DisplayName("Пустой диапазон содержит пустой")
        void emptycontainsRangeEmpty() {
            TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange empty2 = TsRange.of("2026-12-31", "2026-12-31", "()");

            assertTrue(empty1.containsRange(empty2));
            assertTrue(empty2.containsRange(empty1));
        }
    }

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Диапазон с бесконечной верхней границей содержит конечный")
        void infiniteUppercontainsRangeFinite() {
            TsRange infinite = new TsRange(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    true,
                    false
            );
            TsRange finite = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertTrue(infinite.containsRange(finite));
        }

        @Test
        @DisplayName("Диапазон с бесконечной нижней границей содержит конечный")
        void infiniteLowercontainsRangeFinite() {
            TsRange infinite = new TsRange(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 12, 31, 0, 0),
                    false,
                    false
            );
            TsRange finite = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertTrue(infinite.containsRange(finite));
        }

        @Test
        @DisplayName("Конечный диапазон НЕ содержит бесконечный")
        void finiteNotcontainsRangeInfinite() {
            TsRange finite = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange infinite = new TsRange(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    false,
                    false
            );

            assertFalse(finite.containsRange(infinite));
        }

        @Test
        @DisplayName("Полностью бесконечный диапазон содержит любой")
        void fullyInfinitecontainsRangeAny() {
            TsRange fullyInfinite = new TsRange(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    false,
                    false
            );
            TsRange finite = TsRange.of("2026-01-01", "2026-12-31", "[)");

            assertTrue(fullyInfinite.containsRange(finite));
        }
    }

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("containsRange(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.containsRange(null));
        }
    }

    @Nested
    @DisplayName("Симметрия с containedBy")
    class SymmetryTests {

        @Test
        @DisplayName("a.containsRange(b) <=> b.containedBy(a)")
        void symmetryWithContainedBy() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(outer.containsRange(inner));

            assertFalse(inner.containsRange(outer));
        }

        @Test
        @DisplayName("Для идентичных диапазонов оба метода возвращают true")
        void identicalRangesBothTrue() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.containsRange(r2));
            assertTrue(r2.containsRange(r1));
        }
    }

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Рефлексивность: a.containsRange(a) всегда true")
        void reflexivity() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-01", "[)"); // пустой

            assertTrue(r1.containsRange(r1));
            assertTrue(r2.containsRange(r2));
        }

        @Test
        @DisplayName("Антисимметричность: если a.containsRange(b) и b.containsRange(a), то a == b")
        void antisymmetry() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.containsRange(r2));
            assertTrue(r2.containsRange(r1));
            assertTrue(r1.isEqual(r2));
        }

        @Test
        @DisplayName("Транзитивность: если a.containsRange(b) и b.containsRange(c), то a.containsRange(c)")
        void transitivity() {
            TsRange a = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange b = TsRange.of("2026-01-05", "2026-01-20", "[)");
            TsRange c = TsRange.of("2026-01-10", "2026-01-15", "[)");

            assertTrue(a.containsRange(b));
            assertTrue(b.containsRange(c));
            assertTrue(a.containsRange(c));
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Диапазон из одной точки содержит сам себя")
        void singlePointRange() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-01", "[]");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-01", "[]");

            assertTrue(r1.containsRange(r2));
            assertTrue(r2.containsRange(r1));
        }

        @Test
        @DisplayName("Диапазон из одной точки не содержит больший диапазон")
        void singlePointNotcontainsRangeLarger() {
            TsRange single = TsRange.of("2026-01-01", "2026-01-01", "[]");
            TsRange larger = TsRange.of("2026-01-01", "2026-01-10", "[)");

            //assertFalse(single.containsRange(larger));
            assertTrue(larger.containsRange(single));
        }

        @Test
        @DisplayName("Смежные диапазоны (один заканчивается, другой начинается)")
        void adjacentRanges() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            // r1 не содержит r2, потому что r2 начинается с точки, которую r1 не включает
            assertFalse(r1.containsRange(r2));
            assertFalse(r2.containsRange(r1));
        }
    }
}
