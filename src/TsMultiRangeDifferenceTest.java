package ru.nspk.pcl.common.tsrange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Nested
@DisplayName("difference() тесты для TsMultiRange (аналог PostgreSQL -)")
class TsMultiRangeDifferenceTest {

    @Nested
    @DisplayName("Вырезание середины (разрезание)")
    class CutMiddleTests {

        @Test
        @DisplayName("Вырезание середины из одного диапазона даёт два куска")
        void cutMiddleGivesTwoPieces() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-31", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(2, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.get(0).lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.get(0).upper());
            assertEquals(LocalDateTime.of(2026, 1, 20, 0, 0), result.get(1).lower());
            assertEquals(LocalDateTime.of(2026, 1, 31, 0, 0), result.get(1).upper());
        }
    }

    @Nested
    @DisplayName("Вырезание из нескольких диапазонов")
    class CutMultipleTests {

        @Test
        @DisplayName("Один диапазон вырезает куски из обоих диапазонов первого")
        void oneCutsBoth() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-05", "2026-01-25", "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(2, result.size());
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.get(0).lower());
            assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), result.get(0).upper());
            assertEquals(LocalDateTime.of(2026, 1, 25, 0, 0), result.get(1).lower());
            assertEquals(LocalDateTime.of(2026, 1, 30, 0, 0), result.get(1).upper());
        }
    }

    @Nested
    @DisplayName("Нет пересечения")
    class NoOverlapTests {

        @Test
        @DisplayName("Разнесённые multirange — результат тот же")
        void disjointReturnsSame() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertTrue(result.isEqual(mr1));
        }
    }

    @Nested
    @DisplayName("Полное поглощение")
    class FullAbsorptionTests {

        @Test
        @DisplayName("Второй multirange полностью содержит первый — пустой результат")
        void secondContainsFirst() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-31", "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Пустые multirange")
    class EmptyTests {

        @Test
        @DisplayName("Пустой - непустой = пустой")
        void emptyMinusNonEmpty() {
            TsMultiRange empty = TsMultiRange.of(List.of());
            TsMultiRange nonEmpty = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange result = empty.difference(nonEmpty);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Непустой - пустой = непустой")
        void nonEmptyMinusEmpty() {
            TsMultiRange nonEmpty = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));
            TsMultiRange empty = TsMultiRange.of(List.of());

            TsMultiRange result = nonEmpty.difference(empty);

            assertTrue(result.isEqual(nonEmpty));
        }
    }

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("(a - b) + (a * b) == a")
        void differencePlusIntersectionEqualsOriginal() {
            TsMultiRange a = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-31", "[)")
            ));
            TsMultiRange b = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            TsMultiRange diff = a.difference(b);
            TsMultiRange inter = a.intersection(b);
            TsMultiRange union = diff.union(inter);

            assertTrue(a.isEqual(union));
        }
    }
}