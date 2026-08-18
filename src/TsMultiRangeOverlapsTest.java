package ru.nspk.pcl.common.tsrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("overlaps() тесты для TsMultiRange (аналог PostgreSQL &&)")
class TsMultiRangeOverlapsTest {

    @Nested
    @DisplayName("Частичное пересечение")
    class PartialOverlapTests {

        @Test
        @DisplayName("Один диапазон из первого пересекается с одним из второго")
        void oneRangeOverlaps() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            assertTrue(mr1.overlaps(mr2));
            assertTrue(mr2.overlaps(mr1)); // симметричность
        }

        @Test
        @DisplayName("Несколько диапазонов пересекаются")
        void multipleRangesOverlap() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-25", "[)")
            ));

            assertTrue(mr1.overlaps(mr2));
        }
    }

    @Nested
    @DisplayName("Полное совпадение")
    class FullOverlapTests {

        @Test
        @DisplayName("Идентичные multirange пересекаются")
        void identicalMultiranges() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertTrue(mr1.overlaps(mr2));
        }

        @Test
        @DisplayName("Multirange пересекается сам с собой")
        void selfOverlap() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertTrue(mr.overlaps(mr));
        }
    }

    @Nested
    @DisplayName("Нет пересечения")
    class NoOverlapTests {

        @Test
        @DisplayName("Полностью разнесённые multirange")
        void completelyDisjoint() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertFalse(mr1.overlaps(mr2));
            assertFalse(mr2.overlaps(mr1));
        }

        @Test
        @DisplayName("Диапазон второго в разрыве первого")
        void secondInGapOfFirst() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-12", "2026-01-18", "[)")
            ));

            assertFalse(mr1.overlaps(mr2));
        }

        @Test
        @DisplayName("Смежные диапазоны не пересекаются")
        void adjacentRangesNotOverlap() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertFalse(mr1.overlaps(mr2));
        }
    }

    @Nested
    @DisplayName("Пустые multirange")
    class EmptyMultiRangeTests {

        @Test
        @DisplayName("Пустой multirange не пересекается с непустым")
        void emptyNotOverlapsNonEmpty() {
            TsMultiRange empty = TsMultiRange.of(List.of());
            TsMultiRange nonEmpty = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertFalse(empty.overlaps(nonEmpty));
            assertFalse(nonEmpty.overlaps(empty));
        }

        @Test
        @DisplayName("Два пустых multirange не пересекаются")
        void emptyNotOverlapsEmpty() {
            TsMultiRange empty1 = TsMultiRange.of(List.of());
            TsMultiRange empty2 = TsMultiRange.of(List.of());

            assertFalse(empty1.overlaps(empty2));
        }
    }

    @Nested
    @DisplayName("Полное вложение")
    class FullContainmentTests {

        @Test
        @DisplayName("Первый multirange полностью содержит второй")
        void firstContainsSecond() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-31", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-10", "[)"),
                    TsRange.of("2026-01-15", "2026-01-20", "[)")
            ));

            assertTrue(mr1.overlaps(mr2));
        }

        @Test
        @DisplayName("Второй multirange полностью содержит первый")
        void secondContainsFirst() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-10", "[)"),
                    TsRange.of("2026-01-15", "2026-01-20", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-31", "[)")
            ));

            assertTrue(mr1.overlaps(mr2));
        }
    }

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Бесконечный диапазон пересекается с конечным")
        void infiniteOverlapsFinite() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(
                            LocalDateTime.of(2026, 1, 1, 0, 0),
                            TsRange.INFINITY,
                            "[)"
                    )
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2030-01-01", "2030-12-31", "[)")
            ));

            assertTrue(mr1.overlaps(mr2));
        }
    }

    @Nested
    @DisplayName("Сложные случаи")
    class ComplexCasesTests {

        @Test
        @DisplayName("Множественные пересечения")
        void multipleIntersections() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-05", "[)"),
                    TsRange.of("2026-01-10", "2026-01-15", "[)"),
                    TsRange.of("2026-01-20", "2026-01-25", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-03", "2026-01-07", "[)"),
                    TsRange.of("2026-01-18", "2026-01-22", "[)")
            ));

            // [01-03,01-07) пересекается с [01-01,01-05)
            // [01-18,01-22) пересекается с [01-20,01-25)
            assertTrue(mr1.overlaps(mr2));
        }

        @Test
        @DisplayName("Один диапазон второго пересекает несколько диапазонов первого")
        void oneRangeOverlapsMultiple() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-15", "2026-01-20", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-18", "[)")
            ));

            // [01-05,01-18) пересекается с обоими диапазонами первого
            assertTrue(mr1.overlaps(mr2));
        }
    }

    @Nested
    @DisplayName("Симметричность")
    class SymmetryTests {

        @Test
        @DisplayName("a.overlaps(b) == b.overlaps(a)")
        void overlapsIsSymmetric() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            TsMultiRange mr3 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-12", "2026-01-18", "[)")
            ));

            assertEquals(mr1.overlaps(mr2), mr2.overlaps(mr1));
            assertEquals(mr1.overlaps(mr3), mr3.overlaps(mr1));
        }
    }

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("overlaps(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertThrows(IllegalArgumentException.class, () -> mr.overlaps(null));
        }
    }

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("Если a.containsMultirange(b), то a.overlaps(b)")
        void containmentImpliesOverlap() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-31", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertTrue(mr1.containsMultirange(mr2));
            assertTrue(mr1.overlaps(mr2));
        }

        @Test
        @DisplayName("overlap НЕ означает containment")
        void overlapDoesNotImplyContainment() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            assertTrue(mr1.overlaps(mr2));
            assertFalse(mr1.containsMultirange(mr2));
            assertFalse(mr2.containsMultirange(mr1));
        }

        @Test
        @DisplayName("Если НЕ a.overlaps(b), то a и b разнесены")
        void notOverlapMeansDisjoint() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertFalse(mr1.overlaps(mr2));
            assertTrue(mr1.get(0).strictlyLeftOf(mr2.get(0)) ||
                    mr1.get(0).strictlyRightOf(mr2.get(0)));
        }
    }
}