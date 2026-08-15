package ru.starodubov;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TsRangeUnionTest {
    // ==================== ПЕРЕСЕКАЮЩИЕСЯ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Пересекающиеся диапазоны")
    class OverlappingRangesTests {

        @Test
        @DisplayName("Частичное пересечение — объединяются")
        void partialOverlap() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            TsRange result = r1.union(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 15, 0, 0), result.upper());
            assertTrue(result.lowerInc());
            assertFalse(result.upperInc());
        }

        @Test
        @DisplayName("Частичное пересечение в обратном порядке")
        void partialOverlapReverse() {
            TsRange r1 = TsRange.of("2026-01-05", "2026-01-15", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = r1.union(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 15, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Пересечение с разными включительностями")
        void overlapWithDifferentInclusivity() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "()");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            TsRange result = r1.union(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertFalse(result.lowerInc()); // исключающая из r1
            assertEquals(LocalDateTime.of(2026, 1, 15, 0, 0), result.upper());
            assertFalse(result.upperInc()); // исключающая из r2
        }

        @Test
        @DisplayName("Пересечение с одинаковыми нижними границами")
        void overlapWithSameLowerBound() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-15", "()");

            TsRange result = r1.union(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertTrue(result.lowerInc()); // включающая из r1
        }

        @Test
        @DisplayName("Пересечение с одинаковыми верхними границами")
        void overlapWithSameUpperBound() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-10", "[]");

            TsRange result = r1.union(r2);

            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
            assertTrue(result.upperInc()); // включающая из r2
        }
    }

// ==================== СМЕЖНЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Смежные диапазоны")
    class AdjacentRangesTests {

        @Test
        @DisplayName("Смежность: ) и [ — объединяются")
        void adjacentExclusiveInclusive() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            TsRange result = r1.union(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 20, 0, 0), result.upper());
            assertTrue(result.lowerInc());
            assertFalse(result.upperInc());
        }

        @Test
        @DisplayName("Смежность: ] и ( — объединяются")
        void adjacentInclusiveExclusive() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "()");

            TsRange result = r1.union(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 20, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Смежность в обратном порядке")
        void adjacentReverse() {
            TsRange r1 = TsRange.of("2026-01-10", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = r1.union(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 20, 0, 0), result.upper());
        }
    }

// ==================== ПОЛНОЕ ВЛОЖЕНИЕ ====================

    @Nested
    @DisplayName("Полное вложение")
    class FullContainmentTests {

        @Test
        @DisplayName("Второй диапазон внутри первого")
        void secondInsideFirst() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            TsRange result = outer.union(inner);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 31, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Первый диапазон внутри второго")
        void firstInsideSecond() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            TsRange result = inner.union(outer);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 31, 0, 0), result.upper());
        }
    }

// ==================== ИДЕНТИЧНЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Идентичные диапазоны")
    class IdenticalRangesTests {

        @Test
        @DisplayName("Идентичные диапазоны — результат тот же")
        void identicalRanges() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = r1.union(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
            assertTrue(result.lowerInc());
            assertFalse(result.upperInc());
        }

        @Test
        @DisplayName("Диапазон union сам с собой")
        void unionWithSelf() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = range.union(range);

            assertTrue(range.isEqual(result));
        }
    }

// ==================== РАЗНЕСЕННЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Разнесенные диапазоны — исключение")
    class DisjointRangesTests {

        @Test
        @DisplayName("Разрыв между диапазонами — UnsupportedOperationException")
        void gapBetweenRanges() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertThrows(IllegalArgumentException.class, () -> r1.union(r2));
        }

        @Test
        @DisplayName("Диапазоны далеко друг от друга — UnsupportedOperationException")
        void rangesFarApart() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-06-01", "2026-06-10", "[)");

            assertThrows(IllegalArgumentException.class, () -> r1.union(r2));
        }

        @Test
        @DisplayName("Разрыв в одну наносекунду — UnsupportedOperationException")
        void gapOfOneNanosecond() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 10, 0, 0, 0, 1),
                    LocalDateTime.of(2026, 1, 20, 0, 0),
                    "[)"
            );

            assertThrows(IllegalArgumentException.class, () -> r1.union(r2));
        }

        @Test
        @DisplayName("Дырка в точке стыка (обе исключающие) — UnsupportedOperationException")
        void gapAtJunctionPoint() {
            // [01-01, 01-10) и (01-10, 01-20) — дырка в точке 01-10
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "()");

            assertThrows(IllegalArgumentException.class, () -> r1.union(r2));
        }

        @Test
        @DisplayName("Пересечение в точке стыка (обе включающие) — объединяются")
        void overlapAtJunctionPoint() {
            // [01-01, 01-10] и [01-10, 01-20) — пересечение в точке 01-10
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            // Это пересечение, а не разрыв, поэтому union работает
            TsRange result = r1.union(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 20, 0, 0), result.upper());
        }
    }

// ==================== ПУСТЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Пустые диапазоны")
    class EmptyRangeTests {

        @Test
        @DisplayName("Пустой union непустой — возвращает непустой")
        void emptyUnionNonEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange nonEmpty = TsRange.of("2026-01-05", "2026-01-10", "[)");

            TsRange result1 = empty.union(nonEmpty);
            TsRange result2 = nonEmpty.union(empty);

            assertTrue(nonEmpty.isEqual(result1));
            assertTrue(nonEmpty.isEqual(result2));
        }

        @Test
        @DisplayName("Пустой union пустой — возвращает пустой")
        void emptyUnionEmpty() {
            TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange empty2 = TsRange.of("2026-01-05", "2026-01-05", "()");

            TsRange result = empty1.union(empty2);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Непустой union пустой — возвращает непустой")
        void nonEmptyUnionEmpty() {
            TsRange nonEmpty = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            TsRange result = nonEmpty.union(empty);

            assertTrue(nonEmpty.isEqual(result));
        }
    }

// ==================== БЕСКОНЕЧНЫЕ ГРАНИЦЫ ====================

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Бесконечная верхняя: union с конечным")
        void infiniteUpperUnionFinite() {
            TsRange infinite = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );
            TsRange finite = TsRange.of("2026-01-05", "2026-01-15", "[)");

            TsRange result = infinite.union(finite);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(TsRange.INFINITY, result.upper());
        }

        @Test
        @DisplayName("Бесконечная нижняя: union с конечным")
        void infiniteLowerUnionFinite() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "()"
            );
            TsRange finite = TsRange.of("2026-01-05", "2026-01-15", "[)");

            TsRange result = infinite.union(finite);

            assertEquals(TsRange.MINUS_INFINITY, result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 15, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Оба бесконечные: union")
        void bothInfiniteUnion() {
            TsRange r1 = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "()"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );

            TsRange result = r1.union(r2);

            assertEquals(TsRange.MINUS_INFINITY, result.lower());
            assertEquals(TsRange.INFINITY, result.upper());
        }

        @Test
        @DisplayName("Полностью бесконечный union конечный")
        void fullyInfiniteUnionFinite() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );
            TsRange finite = TsRange.of("2026-01-05", "2026-01-15", "[)");

            TsRange result = infinite.union(finite);

            assertEquals(TsRange.MINUS_INFINITY, result.lower());
            assertEquals(TsRange.INFINITY, result.upper());
        }
    }

// ==================== ДИАПАЗОН ИЗ ОДНОЙ ТОЧКИ ====================

    @Nested
    @DisplayName("Диапазон из одной точки")
    class SinglePointRangeTests {

        @Test
        @DisplayName("Точка внутри диапазона — union возвращает диапазон")
        void singlePointInsideRange() {
            TsRange single = TsRange.of("2026-01-05", "2026-01-05", "[]");
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = single.union(range);

            assertTrue(range.isEqual(result));
        }

        @Test
        @DisplayName("Точка на границе диапазона — union расширяет")
        void singlePointOnBoundary() {
            TsRange single = TsRange.of("2026-01-10", "2026-01-10", "[]");
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            // Точка 01-10 не входит в range (исключающая), но входит в single
            // Они смежны: ) и [ стыкуются
            TsRange result = single.union(range);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
            assertTrue(result.upperInc()); // теперь включающая
        }

        @Test
        @DisplayName("Точка вне диапазона — UnsupportedOperationException")
        void singlePointOutsideRange() {
            TsRange single = TsRange.of("2026-01-15", "2026-01-15", "[]");
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertThrows(IllegalArgumentException.class, () -> single.union(range));
        }
    }

// ==================== ОБРАБОТКА NULL ====================

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("union(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.union(null));
        }
    }

// ==================== СВЯЗЬ С ДРУГИМИ МЕТОДАМИ ====================

    @Nested
    @DisplayName("Связь с другими методами")
    class RelationshipTests {

        @Test
        @DisplayName("Для пересекающихся: union == merge")
        void unionEqualsMergeForOverlapping() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            TsRange unionResult = r1.union(r2);
            TsRange mergeResult = r1.merge(r2);

            assertTrue(unionResult.isEqual(mergeResult));
        }

        @Test
        @DisplayName("Для смежных: union == merge")
        void unionEqualsMergeForAdjacent() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            TsRange unionResult = r1.union(r2);
            TsRange mergeResult = r1.merge(r2);

            assertTrue(unionResult.isEqual(mergeResult));
        }

        @Test
        @DisplayName("Для разнесенных: union бросает, merge заполняет")
        void unionThrowsMergeFillsForDisjoint() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            // union бросает исключение
            assertThrows(IllegalArgumentException.class, () -> r1.union(r2));

            // merge заполняет разрыв
            TsRange mergeResult = r1.merge(r2);
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), mergeResult.lower());
            assertEquals(LocalDateTime.of(2026, 1, 20, 0, 0), mergeResult.upper());
        }

        @Test
        @DisplayName("union работает только если overlaps или isAdjacentTo")
        void unionRequiresOverlapOrAdjacency() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");
            TsRange r3 = TsRange.of("2026-01-10", "2026-01-20", "[)");
            TsRange r4 = TsRange.of("2026-01-15", "2026-01-25", "[)");

            // r1 и r2 пересекаются
            assertTrue(r1.overlaps(r2));
            assertDoesNotThrow(() -> r1.union(r2));

            // r1 и r3 смежны
            assertTrue(r1.isAdjacentTo(r3));
            assertDoesNotThrow(() -> r1.union(r3));

            // r1 и r4 разнесены
            assertFalse(r1.overlaps(r4));
            assertFalse(r1.isAdjacentTo(r4));
            assertThrows(IllegalArgumentException.class, () -> r1.union(r4));
        }
    }

// ==================== МАТЕМАТИЧЕСКИЕ СВОЙСТВА ====================

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Коммутативность: a.union(b) == b.union(a)")
        void commutativity() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            TsRange result1 = r1.union(r2);
            TsRange result2 = r2.union(r1);

            assertTrue(result1.isEqual(result2));
        }

        @Test
        @DisplayName("Идемпотентность: a.union(a) == a")
        void idempotency() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = range.union(range);

            assertTrue(range.isEqual(result));
        }

        @Test
        @DisplayName("Ассоциативность для пересекающихся: (a+b)+c == a+(b+c)")
        void associativity() {
            TsRange a = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange b = TsRange.of("2026-01-05", "2026-01-15", "[)");
            TsRange c = TsRange.of("2026-01-10", "2026-01-20", "[)");

            TsRange leftAssoc = a.union(b).union(c);
            TsRange rightAssoc = a.union(b.union(c));

            assertTrue(leftAssoc.isEqual(rightAssoc));
        }

        @Test
        @DisplayName("Нейтральный элемент: a.union(empty) == a")
        void neutralElement() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            TsRange result = range.union(empty);

            assertTrue(range.isEqual(result));
        }
    }

// ==================== ТОЧНОЕ ВРЕМЯ ====================

    @Nested
    @DisplayName("Точное время")
    class ExactTimeTests {

        @Test
        @DisplayName("Union с точным временем")
        void unionWithExactTime() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 11, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );

            TsRange result = r1.union(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 9, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 1, 14, 0), result.upper());
        }

        @Test
        @DisplayName("Union смежных с точным временем")
        void unionAdjacentWithExactTime() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );

            TsRange result = r1.union(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 9, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 1, 14, 0), result.upper());
        }
    }
}
