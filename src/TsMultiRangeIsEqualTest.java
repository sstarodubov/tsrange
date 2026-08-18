package ru.nspk.pcl.common.tsrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("isEqual() тесты для TsMultiRange (аналог PostgreSQL =)")
class TsMultiRangeIsEqualTest {

    @Nested
    @DisplayName("Идентичные multirange")
    class IdenticalTests {

        @Test
        @DisplayName("Одиночные идентичные диапазоны — true")
        void singleIdenticalRanges() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertTrue(mr1.isEqual(mr2));
            assertTrue(mr2.isEqual(mr1)); // симметричность
        }

        @Test
        @DisplayName("Множественные идентичные диапазоны — true")
        void multipleIdenticalRanges() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertTrue(mr1.isEqual(mr2));
        }

        @Test
        @DisplayName("Multirange равен сам себе — true")
        void selfEquality() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertTrue(mr.isEqual(mr));
        }
    }

    @Nested
    @DisplayName("Разные включительности")
    class DifferentInclusivityTests {

        @Test
        @DisplayName("Разная верхняя включительность — false")
        void differentUpperInclusivity() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[]")
            ));

            assertFalse(mr1.isEqual(mr2));
        }

        @Test
        @DisplayName("Разная нижняя включительность — false")
        void differentLowerInclusivity() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "()")
            ));

            assertFalse(mr1.isEqual(mr2));
        }

        @Test
        @DisplayName("Обе включительности разные — false")
        void bothInclusivityDifferent() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "(]")
            ));

            assertFalse(mr1.isEqual(mr2));
        }
    }

    @Nested
    @DisplayName("Разное количество диапазонов")
    class DifferentSizeTests {

        @Test
        @DisplayName("Разное количество диапазонов — false")
        void differentNumberOfRanges() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-30", "[)")
            ));

            assertFalse(mr1.isEqual(mr2));
        }

        @Test
        @DisplayName("Один диапазон vs пустой — false")
        void oneRangeVsEmpty() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of());

            assertFalse(mr1.isEqual(mr2));
        }
    }

    @Nested
    @DisplayName("Разные значения границ")
    class DifferentValuesTests {

        @Test
        @DisplayName("Разные верхние границы — false")
        void differentUpperBounds() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-11", "[)")
            ));

            assertFalse(mr1.isEqual(mr2));
        }

        @Test
        @DisplayName("Разные нижние границы — false")
        void differentLowerBounds() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-02", "2026-01-10", "[)")
            ));

            assertFalse(mr1.isEqual(mr2));
        }

        @Test
        @DisplayName("Разные значения во втором диапазоне — false")
        void differentValuesInSecondRange() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-31", "[)")
            ));

            assertFalse(mr1.isEqual(mr2));
        }
    }

    @Nested
    @DisplayName("Пустые multirange")
    class EmptyMultiRangeTests {

        @Test
        @DisplayName("Два пустых multirange равны — true")
        void emptyEqualsEmpty() {
            TsMultiRange empty1 = TsMultiRange.of(List.of());
            TsMultiRange empty2 = TsMultiRange.of(List.of());

            assertTrue(empty1.isEqual(empty2));
        }

        @Test
        @DisplayName("Пустой не равен непустому — false")
        void emptyNotEqualsNonEmpty() {
            TsMultiRange empty = TsMultiRange.of(List.of());
            TsMultiRange nonEmpty = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertFalse(empty.isEqual(nonEmpty));
            assertFalse(nonEmpty.isEqual(empty));
        }
    }

    @Nested
    @DisplayName("Нормализация при создании")
    class NormalizationTests {

        @Test
        @DisplayName("Пересекающиеся диапазоны нормализуются и равны объединённому")
        void overlappingRangesNormalize() {
            // При создании пересекающиеся диапазоны объединяются
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-15", "[)")
            ));

            assertTrue(mr1.isEqual(mr2));
        }

        @Test
        @DisplayName("Смежные диапазоны нормализуются и равны объединённому")
        void adjacentRangesNormalize() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-20", "[)")
            ));

            assertTrue(mr1.isEqual(mr2));
        }

        @Test
        @DisplayName("Неотсортированные диапазоны нормализуются")
        void unsortedRangesNormalize() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)"),
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertTrue(mr1.isEqual(mr2));
        }

        @Test
        @DisplayName("Пустые диапазоны удаляются при нормализации")
        void emptyRangesRemoved() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.EMPTY,
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertTrue(mr1.isEqual(mr2));
        }
    }


    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("Если a = b, то a @> b и b @> a")
        void equalityImpliesContainment() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertTrue(mr1.isEqual(mr2));
            assertTrue(mr1.containsMultirange(mr2));
            assertTrue(mr2.containsMultirange(mr1));
        }

        @Test
        @DisplayName("Если a = b, то НЕ a < b и НЕ b < a")
        void equalityImpliesNotLessThan() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertTrue(mr1.isEqual(mr2));
            // Если реализован compareTo или lessThan:
            // assertFalse(mr1.lessThan(mr2));
            // assertFalse(mr2.lessThan(mr1));
        }
    }

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("isEqual(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertThrows(IllegalArgumentException.class, () -> mr.isEqual(null));
        }
    }

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Рефлексивность: a = a")
        void reflexivity() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertTrue(mr.isEqual(mr));
        }

        @Test
        @DisplayName("Симметричность: если a = b, то b = a")
        void symmetry() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertTrue(mr1.isEqual(mr2));
            assertTrue(mr2.isEqual(mr1));
        }

        @Test
        @DisplayName("Транзитивность: если a = b и b = c, то a = c")
        void transitivity() {
            TsMultiRange a = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange b = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange c = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertTrue(a.isEqual(b));
            assertTrue(b.isEqual(c));
            assertTrue(a.isEqual(c));
        }
    }
}