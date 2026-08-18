package ru.nspk.pcl.common.tsrange;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TsRangeContainedByTest {

    @Nested
    @DisplayName("Полное вложение")
    class FullContainmentTests {

        @Test
        @DisplayName("Первый диапазон полностью внутри второго")
        void rangeFullyInside() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(inner.rangeIsContainedBy(outer));
            assertFalse(outer.rangeIsContainedBy(inner));
        }

        @Test
        @DisplayName("Первый диапазон касается левой границы второго")
        void rangeTouchingLeftBoundary() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-01", "2026-01-15", "[)");

            assertTrue(inner.rangeIsContainedBy(outer));
        }

        @Test
        @DisplayName("Первый диапазон касается правой границы второго")
        void rangeTouchingRightBoundary() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-15", "2026-01-31", "[)");

            assertTrue(inner.rangeIsContainedBy(outer));
        }

        @Test
        @DisplayName("Первый диапазон касается обеих границ второго")
        void rangeTouchingBothBoundaries() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(inner.rangeIsContainedBy(outer));
        }
    }

    @Nested
    @DisplayName("Идентичные диапазоны")
    class IdenticalRangesTests {

        @Test
        @DisplayName("Диапазон содержится сам в себе")
        void rangerangeIsContainedBySelf() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(range.rangeIsContainedBy(range));
        }

        @Test
        @DisplayName("Два одинаковых диапазона содержатся друг в друге")
        void twoIdenticalRangesrangeIsContainedByEachOther() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.rangeIsContainedBy(r2));
            assertTrue(r2.rangeIsContainedBy(r1));
        }

        @Test
        @DisplayName("Одинаковые значения с одинаковой включительностью")
        void sameValuesSameInclusivity() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[]");

            assertTrue(r1.rangeIsContainedBy(r2));
            assertTrue(r2.rangeIsContainedBy(r1));
        }
    }

    @Nested
    @DisplayName("Частичное пересечение")
    class PartialOverlapTests {

        @Test
        @DisplayName("Первый диапазон выходит за правую границу — false")
        void rangeExceedsRightBoundary() {
            TsRange r1 = TsRange.of("2026-01-05", "2026-01-15", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(r1.rangeIsContainedBy(r2));
        }

        @Test
        @DisplayName("Первый диапазон выходит за левую границу — false")
        void rangeExceedsLeftBoundary() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            assertFalse(r1.rangeIsContainedBy(r2));
        }

        @Test
        @DisplayName("Первый диапазон выходит за обе границы — false")
        void rangeExceedsBothBoundaries() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-15", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertFalse(r1.rangeIsContainedBy(r2));
        }
    }

    @Nested
    @DisplayName("Нет пересечения")
    class NoOverlapTests {

        @Test
        @DisplayName("Первый диапазон полностью после второго — false")
        void rangeAfterSecond() {
            TsRange r1 = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(r1.rangeIsContainedBy(r2));
        }

        @Test
        @DisplayName("Первый диапазон полностью до второго — false")
        void rangeBeforeSecond() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertFalse(r1.rangeIsContainedBy(r2));
        }
    }

    @Nested
    @DisplayName("Разная включительность границ")
    class DifferentInclusivityTests {

        @Test
        @DisplayName("Первый с включающей нижней, второй с исключающей — false")
        void firstInclusiveLowerSecondExclusive() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "()");

            assertFalse(r1.rangeIsContainedBy(r2));
        }

        @Test
        @DisplayName("Первый с исключающей нижней, второй с включающей — true")
        void firstExclusiveLowerSecondInclusive() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "()");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.rangeIsContainedBy(r2));
        }

        @Test
        @DisplayName("Первый с включающей верхней, второй с исключающей — false")
        void firstInclusiveUpperSecondExclusive() {
            TsRange r1 = TsRange.of("2026-01-05", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(r1.rangeIsContainedBy(r2));
        }

        @Test
        @DisplayName("Первый с исключающей верхней, второй с включающей — true")
        void firstExclusiveUpperSecondInclusive() {
            TsRange r1 = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[]");

            assertTrue(r1.rangeIsContainedBy(r2));
        }

        @Test
        @DisplayName("Обе границы первого исключающие, второго включающие — true")
        void firstBothExclusiveSecondBothInclusive() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "()");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[]");

            assertTrue(r1.rangeIsContainedBy(r2));
        }

        @Test
        @DisplayName("Обе границы первого включающие, второго исключающие — false")
        void firstBothInclusiveSecondBothExclusive() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "()");

            assertFalse(r1.rangeIsContainedBy(r2));
        }
    }

    @Nested
    @DisplayName("Пустые диапазоны")
    class EmptyRangeTests {

        @Test
        @DisplayName("Пустой диапазон содержится в любом непустом")
        void emptyContainedInNonEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(empty.rangeIsContainedBy(nonEmpty));
        }

        @Test
        @DisplayName("Непустой диапазон НЕ содержится в пустом")
        void nonEmptyNotContainedInEmpty() {
            TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertFalse(nonEmpty.rangeIsContainedBy(empty));
        }

        @Test
        @DisplayName("Пустой диапазон содержится в пустом")
        void emptyContainedInEmpty() {
            TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange empty2 = TsRange.of("2026-12-31", "2026-12-31", "()");

            assertTrue(empty1.rangeIsContainedBy(empty2));
            assertTrue(empty2.rangeIsContainedBy(empty1));
        }
    }

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Конечный диапазон содержится в бесконечном")
        void finiteContainedInInfinite() {
            TsRange finite = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );

            assertTrue(finite.rangeIsContainedBy(infinite));
        }

        @Test
        @DisplayName("Бесконечный диапазон НЕ содержится в конечном")
        void infiniteNotContainedInFinite() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );
            TsRange finite = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertFalse(infinite.rangeIsContainedBy(finite));
        }

        @Test
        @DisplayName("Диапазон с бесконечной верхней содержится в диапазоне с большей верхней")
        void infiniteUpperContainedInLargerUpper() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );

            assertTrue(r1.rangeIsContainedBy(r2));
        }
    }

    @Nested
    @DisplayName("Симметрия с containsRange")
    class SymmetryTests {

        @Test
        @DisplayName("a.rangeIsContainedBy(b) <=> b.containsRange(a)")
        void symmetryWithContainsRange() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(inner.rangeIsContainedBy(outer));
            assertTrue(outer.containsRange(inner));

            assertFalse(outer.rangeIsContainedBy(inner));
            assertFalse(inner.containsRange(outer));
        }

        @Test
        @DisplayName("Для идентичных диапазонов оба метода возвращают true")
        void identicalRangesBothTrue() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.rangeIsContainedBy(r2));
            assertTrue(r2.rangeIsContainedBy(r1));
            assertTrue(r1.containsRange(r2));
            assertTrue(r2.containsRange(r1));
        }
    }

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("rangeIsContainedBy(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.rangeIsContainedBy(null));
        }
    }

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Рефлексивность: a.rangeIsContainedBy(a) всегда true")
        void reflexivity() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertTrue(r1.rangeIsContainedBy(r1));
            assertTrue(r2.rangeIsContainedBy(r2));
        }

        @Test
        @DisplayName("Антисимметричность: если a.rangeIsContainedBy(b) и b.rangeIsContainedBy(a), то a == b")
        void antisymmetry() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.rangeIsContainedBy(r2));
            assertTrue(r2.rangeIsContainedBy(r1));
            assertTrue(r1.isEqual(r2));
        }

        @Test
        @DisplayName("Транзитивность: если a.rangeIsContainedBy(b) и b.rangeIsContainedBy(c), то a.rangeIsContainedBy(c)")
        void transitivity() {
            TsRange a = TsRange.of("2026-01-10", "2026-01-15", "[)");
            TsRange b = TsRange.of("2026-01-05", "2026-01-20", "[)");
            TsRange c = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(a.rangeIsContainedBy(b));
            assertTrue(b.rangeIsContainedBy(c));
            assertTrue(a.rangeIsContainedBy(c));
        }
    }
}
