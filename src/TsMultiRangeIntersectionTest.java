package ru.nspk.pcl.common.tsrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("intersection() тесты для TsMultiRange (аналог PostgreSQL *)")
class TsMultiRangeIntersectionTest {

    @Nested
    @DisplayName("Простое пересечение")
    class SimpleIntersectionTests {

        @Test
        @DisplayName("Частичное пересечение одиночных диапазонов")
        void partialOverlap() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            TsMultiRange result = mr1.intersection(mr2);

            assertEquals(1, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Пересечение с разными включительностями")
        void intersectionWithDifferentInclusivity() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-10", "[]")
            ));

            TsMultiRange result = mr1.intersection(mr2);

            assertEquals(1, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
            assertFalse(result.upperInc()); // AND: false AND true = false
        }
    }

    @Nested
    @DisplayName("Пересечение с множественными диапазонами")
    class MultipleRangesTests {

        @Test
        @DisplayName("Один диапазон пересекает оба диапазона другого multirange")
        void oneRangeIntersectsBoth() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-25", "[)")
            ));

            TsMultiRange result = mr1.intersection(mr2);

            assertEquals(2, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), result.get(0).lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.get(0).upper());
            assertEquals(LocalDateTime.of(2026, 1, 20, 0, 0), result.get(1).lower());
            assertEquals(LocalDateTime.of(2026, 1, 25, 0, 0), result.get(1).upper());
        }

        @Test
        @DisplayName("Попарные пересечения")
        void pairwiseIntersections() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-05", "[)"),
                    TsRange.of("2026-01-10", "2026-01-15", "[)"),
                    TsRange.of("2026-01-20", "2026-01-25", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-03", "2026-01-12", "[)"),
                    TsRange.of("2026-01-22", "2026-02-01", "[)")
            ));

            TsMultiRange result = mr1.intersection(mr2);

            // [01-03,01-12) пересекает [01-01,01-05) и [01-10,01-15)
            // [01-22,02-01) пересекает [01-20,01-25)
            assertEquals(3, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 3, 0, 0), result.get(0).lower());
            assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), result.get(0).upper());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.get(1).lower());
            assertEquals(LocalDateTime.of(2026, 1, 12, 0, 0), result.get(1).upper());
            assertEquals(LocalDateTime.of(2026, 1, 22, 0, 0), result.get(2).lower());
            assertEquals(LocalDateTime.of(2026, 1, 25, 0, 0), result.get(2).upper());
        }
    }

    @Nested
    @DisplayName("Нет пересечения")
    class NoIntersectionTests {

        @Test
        @DisplayName("Разнесённые multirange — пустой результат")
        void disjointMultiranges() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-15", "2026-01-20", "[)")
            ));

            TsMultiRange result = mr1.intersection(mr2);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Диапазон в разрыве — пустой результат")
        void rangeInGap() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-12", "2026-01-18", "[)")
            ));

            TsMultiRange result = mr1.intersection(mr2);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Полное вложение")
    class FullContainmentTests {

        @Test
        @DisplayName("Первый multirange содержит второй")
        void firstContainsSecond() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-31", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-10", "[)"),
                    TsRange.of("2026-01-15", "2026-01-20", "[)")
            ));

            TsMultiRange result = mr1.intersection(mr2);

            assertTrue(result.isEqual(mr2));
        }

        @Test
        @DisplayName("Второй multirange содержит первый")
        void secondContainsFirst() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-10", "[)"),
                    TsRange.of("2026-01-15", "2026-01-20", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-31", "[)")
            ));

            TsMultiRange result = mr1.intersection(mr2);

            assertTrue(result.isEqual(mr1));
        }
    }

    @Nested
    @DisplayName("Пустые multirange")
    class EmptyMultiRangeTests {

        @Test
        @DisplayName("Пустой intersection непустой — пустой")
        void emptyIntersectionNonEmpty() {
            TsMultiRange empty = TsMultiRange.of(List.of());
            TsMultiRange nonEmpty = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange result1 = empty.intersection(nonEmpty);
            TsMultiRange result2 = nonEmpty.intersection(empty);

            assertTrue(result1.isEmpty());
            assertTrue(result2.isEmpty());
        }

        @Test
        @DisplayName("Пустой intersection пустой — пустой")
        void emptyIntersectionEmpty() {
            TsMultiRange empty1 = TsMultiRange.of(List.of());
            TsMultiRange empty2 = TsMultiRange.of(List.of());

            TsMultiRange result = empty1.intersection(empty2);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Идентичные multirange")
    class IdenticalTests {

        @Test
        @DisplayName("Идентичные multirange — результат тот же")
        void identicalMultiranges() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange result = mr1.intersection(mr2);

            assertTrue(result.isEqual(mr1));
        }
    }

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Коммутативность: a * b == b * a")
        void commutativity() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            TsMultiRange result1 = mr1.intersection(mr2);
            TsMultiRange result2 = mr2.intersection(mr1);

            assertTrue(result1.isEqual(result2));
        }

        @Test
        @DisplayName("Идемпотентность: a * a == a")
        void idempotency() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange result = mr.intersection(mr);

            assertTrue(result.isEqual(mr));
        }

        @Test
        @DisplayName("Поглощающий элемент: a * empty == empty")
        void absorbingElement() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange empty = TsMultiRange.of(List.of());

            TsMultiRange result = mr.intersection(empty);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("a && b ⟺ a * b не пустой")
        void overlapIffNonEmptyIntersection() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            TsMultiRange mr3 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-15", "2026-01-20", "[)")
            ));

            // mr1 и mr2 пересекаются
            assertTrue(mr1.overlaps(mr2));
            assertFalse(mr1.intersection(mr2).isEmpty());

            // mr1 и mr3 не пересекаются
            assertFalse(mr1.overlaps(mr3));
            assertTrue(mr1.intersection(mr3).isEmpty());
        }

        @Test
        @DisplayName("a @> b ⟺ a * b == b")
        void containsIffIntersectionEqualsSecond() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-31", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-10", "[)")
            ));

            assertTrue(mr1.containsMultirange(mr2));
            assertTrue(mr1.intersection(mr2).isEqual(mr2));
        }
    }

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("intersection(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertThrows(IllegalArgumentException.class, () -> mr.intersection(null));
        }
    }
}