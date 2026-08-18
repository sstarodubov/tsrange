package ru.nspk.pcl.common.tsrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("lessThan() тесты для TsMultiRange (аналог PostgreSQL <)")
class TsMultiRangeLessThanTest {

    @Nested
    @DisplayName("Сравнение первых диапазонов")
    class FirstRangeComparisonTests {
        @Test
        @DisplayName("Первый диапазон меньше — true")
        void firstRangeLess() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            assertTrue(mr1.lessThan(mr2));
            assertFalse(mr2.lessThan(mr1));
        }

        @Test
        @DisplayName("Первый диапазон больше — false")
        void firstRangeGreater() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertFalse(mr1.lessThan(mr2));
            assertTrue(mr2.lessThan(mr1));
        }
    }

    @Nested
    @DisplayName("Сравнение вторых диапазонов")
    class SecondRangeComparisonTests {

        @Test
        @DisplayName("Первые равны, второй меньше — true")
        void firstEqualSecondLess() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-25", "2026-02-05", "[)")
            ));

            assertTrue(mr1.lessThan(mr2));
        }

        @Test
        @DisplayName("Первые равны, второй больше — false")
        void firstEqualSecondGreater() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-25", "2026-02-05", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertFalse(mr1.lessThan(mr2));
        }
    }

    @Nested
    @DisplayName("Префикс")
    class PrefixTests {

        @Test
        @DisplayName("Более короткий multirange является префиксом — true")
        void shorterIsPrefix() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertTrue(mr1.lessThan(mr2));
            assertFalse(mr2.lessThan(mr1));
        }

        @Test
        @DisplayName("Более длинный multirange не меньше префикса — false")
        void longerNotLessThanPrefix() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)"),
                    TsRange.of("2026-02-01", "2026-02-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertFalse(mr1.lessThan(mr2));
        }
    }

    @Nested
    @DisplayName("Идентичные multirange")
    class IdenticalTests {

        @Test
        @DisplayName("Идентичные multirange не меньше друг друга — false")
        void identicalNotLess() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertFalse(mr1.lessThan(mr2));
            assertFalse(mr2.lessThan(mr1));
        }
    }

    @Nested
    @DisplayName("Пустые multirange")
    class EmptyMultiRangeTests {

        @Test
        @DisplayName("Пустой меньше непустого — true")
        void emptyLessThanNonEmpty() {
            TsMultiRange empty = TsMultiRange.of(List.of());
            TsMultiRange nonEmpty = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertTrue(empty.lessThan(nonEmpty));
        }

        @Test
        @DisplayName("Непустой не меньше пустого — false")
        void nonEmptyNotLessThanEmpty() {
            TsMultiRange nonEmpty = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));
            TsMultiRange empty = TsMultiRange.of(List.of());

            assertFalse(nonEmpty.lessThan(empty));
        }

        @Test
        @DisplayName("Два пустых не меньше друг друга — false")
        void emptyNotLessThanEmpty() {
            TsMultiRange empty1 = TsMultiRange.of(List.of());
            TsMultiRange empty2 = TsMultiRange.of(List.of());

            assertFalse(empty1.lessThan(empty2));
        }
    }

    @Nested
    @DisplayName("Разные включительности")
    class DifferentInclusivityTests {

        @Test
        @DisplayName("[ меньше ( при одинаковых значениях — true")
        void inclusiveLessThanExclusive() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "()")
            ));

            assertTrue(mr1.lessThan(mr2));
        }

        @Test
        @DisplayName("( больше [ при одинаковых значениях — false")
        void exclusiveGreaterThanInclusive() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "()")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertFalse(mr1.lessThan(mr2));
        }
    }

    @Nested
    @DisplayName("Симметричность с >")
    class SymmetryTests {

        @Test
        @DisplayName("a.lessThan(b) <=> b.greaterThan(a)")
        void symmetryWithGreaterThan() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            assertTrue(mr1.lessThan(mr2));
            assertTrue(mr2.greaterThan(mr1));

            assertFalse(mr2.lessThan(mr1));
            assertFalse(mr1.greaterThan(mr2));
        }
    }

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Антирефлексивность: НЕ a < a")
        void irreflexivity() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertFalse(mr.lessThan(mr));
        }

        @Test
        @DisplayName("Асимметричность: если a < b, то НЕ b < a")
        void asymmetry() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            assertTrue(mr1.lessThan(mr2));
            assertFalse(mr2.lessThan(mr1));
        }

        @Test
        @DisplayName("Транзитивность: если a < b и b < c, то a < c")
        void transitivity() {
            TsMultiRange a = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-05", "[)")
            ));

            TsMultiRange b = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-15", "[)")
            ));

            TsMultiRange c = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-25", "[)")
            ));

            assertTrue(a.lessThan(b));
            assertTrue(b.lessThan(c));
            assertTrue(a.lessThan(c));
        }

        @Test
        @DisplayName("Полнота: для любых a и b, либо a < b, либо b < a, либо a = b")
        void totality() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            TsMultiRange mr3 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            // mr1 и mr2: одно меньше другого
            assertTrue(mr1.lessThan(mr2) || mr2.lessThan(mr1));

            // mr1 и mr3: равны
            assertFalse(mr1.lessThan(mr3));
            assertFalse(mr3.lessThan(mr1));
            assertTrue(mr1.isEqual(mr3));
        }
    }

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("lessThan(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertThrows(IllegalArgumentException.class, () -> mr.lessThan(null));
        }
    }
}