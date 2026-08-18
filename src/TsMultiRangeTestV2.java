package ru.nspk.pcl.common.tsrange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TsMultiRange тесты")
class TsMultiRangeTestV2 {

    @Nested
    @DisplayName("Нормализация")
    class NormalizationTests {

        @Test
        @DisplayName("Пересекающиеся диапазоны объединяются")
        void overlappingRangesMerge() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-15", "[)"),
                    TsRange.of("2026-01-10", "2026-01-25", "[)")
            ));

            assertEquals(1, mr.size());
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), mr.lower());
            assertEquals(LocalDateTime.of(2026, 1, 25, 0, 0), mr.upper());
        }

        @Test
        @DisplayName("Смежные диапазоны объединяются")
        void adjacentRangesMerge() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertEquals(1, mr.size());
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), mr.lower());
            assertEquals(LocalDateTime.of(2026, 1, 20, 0, 0), mr.upper());
        }

        @Test
        @DisplayName("Разнесённые диапазоны остаются раздельными")
        void disjointRangesStaySeparate() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertEquals(2, mr.size());
        }

        @Test
        @DisplayName("Пустые диапазоны удаляются")
        void emptyRangesRemoved() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.EMPTY,
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertEquals(2, mr.size());
        }

        @Test
        @DisplayName("Неотсортированные диапазоны сортируются")
        void unsortedRangesGetSorted() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)"),
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertEquals(2, mr.size());
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), mr.get(0).lower());
            assertEquals(LocalDateTime.of(2026, 1, 20, 0, 0), mr.get(1).lower());
        }
    }

    @Nested
    @DisplayName("containsElement")
    class ContainsElementTests {

        @Test
        @DisplayName("Элемент на границе двух непересекающихся диапазонов")
        void elementInRange() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "()"),
                    TsRange.of("2026-01-10", "2026-01-30", "()")
            ));

            assertFalse(mr.containsElement(LocalDateTime.of(2026, 1, 10, 0, 0)));
        }

        @Test
        @DisplayName("Элемент в первом диапазоне")
        void elementInFirstRange() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertTrue(mr.containsElement(LocalDateTime.of(2026, 1, 5, 0, 0)));
        }

        @Test
        @DisplayName("Элемент во втором диапазоне")
        void elementInSecondRange() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertTrue(mr.containsElement(LocalDateTime.of(2026, 1, 25, 0, 0)));
        }

        @Test
        @DisplayName("Элемент в разрыве")
        void elementInGap() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertFalse(mr.containsElement(LocalDateTime.of(2026, 1, 15, 0, 0)));
        }

        @Test
        @DisplayName("Элемент на включающей границе")
        void elementOnInclusiveBoundary() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertTrue(mr.containsElement(LocalDateTime.of(2026, 1, 1, 0, 0)));
        }

        @Test
        @DisplayName("Элемент на исключающей границе")
        void elementOnExclusiveBoundary() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertFalse(mr.containsElement(LocalDateTime.of(2026, 1, 10, 0, 0)));
        }
    }

    @Nested
    @DisplayName("containsRange")
    class ContainsRangeTests {

        @Test
        @DisplayName("Диапазон полностью внутри одного из диапазонов")
        void rangeFullyInside() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-30", "[)")
            ));

            assertTrue(mr.containsRange(TsRange.of("2026-01-10", "2026-01-20", "[)")));
        }

        @Test
        @DisplayName("Диапазон пересекает границы")
        void rangeCrossesBoundaries() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertFalse(mr.containsRange(TsRange.of("2026-01-05", "2026-01-25", "[)")));
        }

        @Test
        @DisplayName("Пустой диапазон содержится в любом")
        void emptyRangeContainedInAny() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertTrue(mr.containsRange(TsRange.EMPTY));
        }
    }

    @Nested
    @DisplayName("containsMultirange")
    class ContainsMultirangeTests {

        @Test
        @DisplayName("Multirange содержится в большем multirange")
        void multirangeContainedInLarger() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertTrue(mr1.containsMultirange(mr2));
            assertFalse(mr2.containsMultirange(mr1));
        }

        @Test
        @DisplayName("Пустой multirange содержится в любом")
        void emptyMultirangeContainedInAny() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertTrue(mr.containsMultirange(TsMultiRange.of(List.of())));
        }
    }

    @Nested
    @DisplayName("merge")
    class MergeTests {

        @Test
        @DisplayName("Merge возвращает минимальный охватывающий диапазон")
        void mergeReturnsConvexHull() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsRange merged = mr.merge();

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), merged.lower());
            assertEquals(LocalDateTime.of(2026, 1, 30, 0, 0), merged.upper());
        }

        @Test
        @DisplayName("Merge пустого multirange возвращает EMPTY")
        void mergeEmptyReturnsEmpty() {
            TsMultiRange mr = TsMultiRange.of(List.of());

            assertTrue(mr.merge().isEmpty());
        }
    }
}