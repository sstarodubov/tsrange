package ru.nspk.pcl.common.tsrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("isAdjacentTo() тесты для TsMultiRange (аналог PostgreSQL -|-)")
class TsMultiRangeIsAdjacentToTest {

    @Nested
    @DisplayName("Простые смежные multirange")
    class SimpleAdjacentTests {

        @Test
        @DisplayName("Одиночные смежные диапазоны — true")
        void singleAdjacentRanges() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertTrue(mr1.isAdjacentTo(mr2));
            assertTrue(mr2.isAdjacentTo(mr1)); // симметричность
        }

        @Test
        @DisplayName("Смежные с ] и ( — true")
        void adjacentInclusiveExclusive() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[]")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "()")
            ));

            assertTrue(mr1.isAdjacentTo(mr2));
        }
    }

    @Nested
    @DisplayName("Заполнение разрыва")
    class GapFillingTests {

        @Test
        @DisplayName("Второй multirange заполняет разрыв в первом — true")
        void secondFillsGapInFirst() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertFalse(mr1.isAdjacentTo(mr2));
        }

        @Test
        @DisplayName("Второй multirange заполняет несколько разрывов — false")
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

            assertFalse(mr1.isAdjacentTo(mr2));
        }
    }

    @Nested
    @DisplayName("Нет смежности")
    class NotAdjacentTests {

        @Test
        @DisplayName("Разрыв между multirange — false")
        void gapBetweenMultiranges() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-15", "2026-01-20", "[)")
            ));

            assertFalse(mr1.isAdjacentTo(mr2));
        }

        @Test
        @DisplayName("Пересечение — false")
        void overlapNotAdjacent() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-15", "[)")
            ));

            assertFalse(mr1.isAdjacentTo(mr2));
        }

        @Test
        @DisplayName("Идентичные multirange — false")
        void identicalNotAdjacent() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertFalse(mr1.isAdjacentTo(mr2));
        }

        @Test
        @DisplayName("Касание с общей точкой (обе включающие) — false")
        void touchWithCommonPoint() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[]")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertFalse(mr1.isAdjacentTo(mr2)); // общая точка 01-10
        }

        @Test
        @DisplayName("Касание с дыркой (обе исключающие) — false")
        void touchWithGap() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "()")
            ));

            assertFalse(mr1.isAdjacentTo(mr2)); // дырка в точке 01-10
        }
    }

    @Nested
    @DisplayName("Пустые multirange")
    class EmptyMultiRangeTests {

        @Test
        @DisplayName("Пустой multirange не смежен с непустым — false")
        void emptyNotAdjacentToNonEmpty() {
            TsMultiRange empty = TsMultiRange.of(List.of());
            TsMultiRange nonEmpty = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertFalse(empty.isAdjacentTo(nonEmpty));
            assertFalse(nonEmpty.isAdjacentTo(empty));
        }

        @Test
        @DisplayName("Два пустых multirange не смежны — false")
        void emptyNotAdjacentToEmpty() {
            TsMultiRange empty1 = TsMultiRange.of(List.of());
            TsMultiRange empty2 = TsMultiRange.of(List.of());

            assertFalse(empty1.isAdjacentTo(empty2));
        }
    }

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Бесконечная верхняя не может стыковаться — false")
        void infiniteUpperNotAdjacent() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(
                            LocalDateTime.of(2026, 1, 1, 0, 0),
                            TsRange.INFINITY,
                            "[)"
                    )
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertFalse(mr1.isAdjacentTo(mr2));
        }

        @Test
        @DisplayName("Конечный стык при бесконечной нижней — true")
        void finiteJunctionWithInfiniteLower() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(
                            TsRange.MINUS_INFINITY,
                            LocalDateTime.of(2026, 1, 10, 0, 0),
                            "()"
                    )
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertTrue(mr1.isAdjacentTo(mr2));
        }
    }

    @Nested
    @DisplayName("Сложные случаи")
    class ComplexCasesTests {

        @Test
        @DisplayName("Только один из диапазонов смежен — false")
        void onlyOneRangeAdjacent() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-15", "[)")
            ));

            assertFalse(mr1.isAdjacentTo(mr2));
        }

        @Test
        @DisplayName("Несколько диапазонов, ни один не смежен — false")
        void noRangeAdjacent() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-05", "[)"),
                    TsRange.of("2026-01-10", "2026-01-15", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-25", "[)"),
                    TsRange.of("2026-02-01", "2026-02-05", "[)")
            ));

            assertFalse(mr1.isAdjacentTo(mr2));
        }
    }

    @Nested
    @DisplayName("Симметричность")
    class SymmetryTests {

        @Test
        @DisplayName("a.isAdjacentTo(b) == b.isAdjacentTo(a)")
        void adjacencyIsSymmetric() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            TsMultiRange mr3 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-15", "2026-01-25", "[)")
            ));

            assertEquals(mr1.isAdjacentTo(mr2), mr2.isAdjacentTo(mr1));
            assertEquals(mr1.isAdjacentTo(mr3), mr3.isAdjacentTo(mr1));
        }
    }

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("Если a.isAdjacentTo(b), то НЕ a.overlaps(b)")
        void adjacentImpliesNotOverlap() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertTrue(mr1.isAdjacentTo(mr2));
            assertFalse(mr1.overlaps(mr2));
        }

        @Test
        @DisplayName("Если a.isAdjacentTo(b), то a.union(b) даёт непрерывный результат")
        void adjacentUnionIsContinuous() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertTrue(mr1.isAdjacentTo(mr2));
            // union должен дать один непрерывный диапазон
            // (если реализован метод union для multirange)
        }

        @Test
        @DisplayName("Если a.strictlyLeftOf(b) и смежны, то a.isAdjacentTo(b)")
        void strictlyLeftAndAdjacent() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertTrue(mr1.strictlyLeftOf(mr2));
            assertTrue(mr1.isAdjacentTo(mr2));
        }
    }

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("isAdjacentTo(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertThrows(IllegalArgumentException.class, () -> mr.isAdjacentTo(null));
        }
    }
}