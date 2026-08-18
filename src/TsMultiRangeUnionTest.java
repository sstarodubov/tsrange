package ru.nspk.pcl.common.tsrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("union() тесты для TsMultiRange (аналог PostgreSQL +)")
class TsMultiRangeUnionTest {

    @Nested
    @DisplayName("Разнесённые диапазоны")
    class DisjointRangesTests {

        @Test
        @DisplayName("Разнесённые диапазоны остаются раздельными")
        void disjointRangesStaySeparate() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange result = mr1.union(mr2);

            assertEquals(2, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.get(0).lower());
            assertEquals(LocalDateTime.of(2026, 1, 20, 0, 0), result.get(1).lower());
        }

        @Test
        @DisplayName("Несколько разнесённых диапазонов")
        void multipleDisjointRanges() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-05", "[)"),
                    TsRange.of("2026-01-10", "2026-01-15", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-25", "[)"),
                    TsRange.of("2026-02-01", "2026-02-05", "[)")
            ));

            TsMultiRange result = mr1.union(mr2);

            assertEquals(4, result.size());
        }
    }

    @Nested
    @DisplayName("Пересекающиеся диапазоны")
    class OverlappingRangesTests {

        @Test
        @DisplayName("Пересекающиеся диапазоны объединяются")
        void overlappingRangesMerge() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            TsMultiRange result = mr1.union(mr2);

            assertEquals(1, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 15, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Полное вложение")
        void fullContainment() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-31", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            TsMultiRange result = mr1.union(mr2);

            assertEquals(1, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 31, 0, 0), result.upper());
        }
    }

    @Nested
    @DisplayName("Смежные диапазоны")
    class AdjacentRangesTests {

        @Test
        @DisplayName("Смежные диапазоны объединяются")
        void adjacentRangesMerge() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            TsMultiRange result = mr1.union(mr2);

            assertEquals(1, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 20, 0, 0), result.upper());
        }
    }

    @Nested
    @DisplayName("Заполнение разрыва")
    class GapFillingTests {

        @Test
        @DisplayName("Второй multirange заполняет разрыв в первом")
        void secondFillsGapInFirst() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            TsMultiRange result = mr1.union(mr2);

            assertEquals(1, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 30, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Второй multirange заполняет несколько разрывов")
        void secondFillsMultipleGaps() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-05", "[)"),
                    TsRange.of("2026-01-10", "2026-01-15", "[)"),
                    TsRange.of("2026-01-20", "2026-01-25", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-10", "[)"),
                    TsRange.of("2026-01-15", "2026-01-20", "[)")
            ));

            TsMultiRange result = mr1.union(mr2);

            assertEquals(1, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 25, 0, 0), result.upper());
        }
    }

    @Nested
    @DisplayName("Пустые multirange")
    class EmptyMultiRangeTests {

        @Test
        @DisplayName("Пустой + непустой = непустой")
        void emptyUnionNonEmpty() {
            TsMultiRange empty = TsMultiRange.of(List.of());
            TsMultiRange nonEmpty = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange result1 = empty.union(nonEmpty);
            TsMultiRange result2 = nonEmpty.union(empty);

            assertEquals(1, result1.size());
            assertEquals(1, result2.size());
            assertTrue(result1.isEqual(nonEmpty));
            assertTrue(result2.isEqual(nonEmpty));
        }

        @Test
        @DisplayName("Пустой + пустой = пустой")
        void emptyUnionEmpty() {
            TsMultiRange empty1 = TsMultiRange.of(List.of());
            TsMultiRange empty2 = TsMultiRange.of(List.of());

            TsMultiRange result = empty1.union(empty2);

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
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange result = mr1.union(mr2);

            assertEquals(1, result.size());
            assertTrue(result.isEqual(mr1));
        }
    }

    @Nested
    @DisplayName("Сложные случаи")
    class ComplexCasesTests {

        @Test
        @DisplayName("Частичное пересечение нескольких диапазонов")
        void partialOverlapMultiple() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-25", "[)")
            ));

            TsMultiRange result = mr1.union(mr2);

            // [01-05,01-25) пересекает оба диапазона первого
            // Всё объединяется в [01-01,01-30)
            assertEquals(1, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 30, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Смешанный случай: пересечение и разнесённость")
        void mixedOverlapAndDisjoint() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-05", "[)"),
                    TsRange.of("2026-01-10", "2026-01-15", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-03", "2026-01-12", "[)"),
                    TsRange.of("2026-01-20", "2026-01-25", "[)")
            ));

            TsMultiRange result = mr1.union(mr2);

            // [01-03,01-12) объединяется с [01-01,01-05) и [01-10,01-15)
            // [01-20,01-25) остаётся отдельным
            assertEquals(2, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.get(0).lower());
            assertEquals(LocalDateTime.of(2026, 1, 15, 0, 0), result.get(0).upper());
            assertEquals(LocalDateTime.of(2026, 1, 20, 0, 0), result.get(1).lower());
            assertEquals(LocalDateTime.of(2026, 1, 25, 0, 0), result.get(1).upper());
        }
    }

    @Nested
    @DisplayName("Коммутативность")
    class CommutativityTests {

        @Test
        @DisplayName("a.union(b) == b.union(a)")
        void unionIsCommutative() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            TsMultiRange result1 = mr1.union(mr2);
            TsMultiRange result2 = mr2.union(mr1);

            assertTrue(result1.isEqual(result2));
        }
    }

    @Nested
    @DisplayName("Ассоциативность")
    class AssociativityTests {

        @Test
        @DisplayName("(a+b)+c == a+(b+c)")
        void unionIsAssociative() {
            TsMultiRange a = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange b = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            TsMultiRange c = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-25", "[)")
            ));

            TsMultiRange leftAssoc = a.union(b).union(c);
            TsMultiRange rightAssoc = a.union(b.union(c));

            assertTrue(leftAssoc.isEqual(rightAssoc));
        }
    }

    @Nested
    @DisplayName("Идемпотентность")
    class IdempotencyTests {

        @Test
        @DisplayName("a.union(a) == a")
        void unionIsIdempotent() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange result = mr.union(mr);

            assertTrue(result.isEqual(mr));
        }
    }

    @Nested
    @DisplayName("Нейтральный элемент")
    class NeutralElementTests {

        @Test
        @DisplayName("a.union(empty) == a")
        void emptyIsNeutralElement() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange empty = TsMultiRange.of(List.of());

            TsMultiRange result = mr.union(empty);

            assertTrue(result.isEqual(mr));
        }
    }

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("union(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertThrows(IllegalArgumentException.class, () -> mr.union(null));
        }
    }
}