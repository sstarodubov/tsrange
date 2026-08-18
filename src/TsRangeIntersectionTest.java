package ru.nspk.pcl.common.tsrange;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TsRangeIntersectionTest {

    // ==================== ЧАСТИЧНОЕ ПЕРЕСЕЧЕНИЕ ====================

    @Nested
    @DisplayName("Частичное пересечение")
    class PartialOverlapTests {

        @Test
        @DisplayName("Пересечение в середине")
        void intersectionInMiddle() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            TsRange result = r1.intersection(r2);

            assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
            assertTrue(result.lowerInc());
            assertFalse(result.upperInc());
        }

        @Test
        @DisplayName("Пересечение в обратном порядке")
        void intersectionReverse() {
            TsRange r1 = TsRange.of("2026-01-05", "2026-01-15", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = r1.intersection(r2);

            assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Пересечение с разными включительностями нижних границ")
        void intersectionWithDifferentLowerInclusivity() {
            // [01-01, 01-10) и (01-01, 01-15)
            // lower_result = 01-01, lowerInc = true AND false = false
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-15", "()");

            TsRange result = r1.intersection(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertFalse(result.lowerInc()); // AND: true AND false = false
        }

        @Test
        @DisplayName("Пересечение с разными включительностями верхних границ")
        void intersectionWithDifferentUpperInclusivity() {
            // [01-01, 01-10) и [01-05, 01-10]
            // upper_result = 01-10, upperInc = false AND true = false
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-10", "[]");

            TsRange result = r1.intersection(r2);

            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
            assertFalse(result.upperInc()); // AND: false AND true = false
        }

        @Test
        @DisplayName("Пересечение с обеими включающими нижними границами")
        void intersectionWithBothInclusiveLower() {
            // [01-01, 01-10) и [01-01, 01-15)
            // lower_result = 01-01, lowerInc = true AND true = true
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-15", "[)");

            TsRange result = r1.intersection(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertTrue(result.lowerInc()); // AND: true AND true = true
        }
    }

// ==================== ПОЛНОЕ ВЛОЖЕНИЕ ====================

    @Nested
    @DisplayName("Полное вложение")
    class FullContainmentTests {

        @Test
        @DisplayName("Второй диапазон внутри первого — результат второй")
        void secondInsideFirst() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            TsRange result = outer.intersection(inner);

            assertTrue(inner.isEqual(result));
        }

        @Test
        @DisplayName("Первый диапазон внутри второго — результат первый")
        void firstInsideSecond() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");

            TsRange result = inner.intersection(outer);

            assertTrue(inner.isEqual(result));
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

            TsRange result = r1.intersection(r2);

            assertTrue(r1.isEqual(result));
        }

        @Test
        @DisplayName("Диапазон intersection сам с собой")
        void intersectionWithSelf() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = range.intersection(range);

            assertTrue(range.isEqual(result));
        }
    }

// ==================== СМЕЖНЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Смежные диапазоны")
    class AdjacentRangesTests {

        @Test
        @DisplayName("Смежные ) и [ — пустой результат")
        void adjacentExclusiveInclusive() {
            // [01-01, 01-10) и [01-10, 01-20)
            // lower_result = 01-10 (из r2), upper_result = 01-10 (из r1)
            // lowerInc = true, upperInc = false → пустой
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            TsRange result = r1.intersection(r2);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Смежные ] и [ — диапазон из одной точки")
        void adjacentBothInclusive() {
            // [01-01, 01-10] и [01-10, 01-20)
            // lower_result = 01-10 (из r2), upper_result = 01-10 (из r1)
            // lowerInc = true, upperInc = true → [01-10, 01-10]
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            TsRange result = r1.intersection(r2);

            assertFalse(result.isEmpty());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
            assertTrue(result.lowerInc());
            assertTrue(result.upperInc());
        }

        @Test
        @DisplayName("Смежные ) и ( — пустой результат")
        void adjacentBothExclusive() {
            // [01-01, 01-10) и (01-10, 01-20)
            // lower_result = 01-10, upper_result = 01-10
            // lowerInc = false, upperInc = false → пустой
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "()");

            TsRange result = r1.intersection(r2);

            assertTrue(result.isEmpty());
        }
    }

// ==================== РАЗНЕСЕННЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Разнесенные диапазоны")
    class DisjointRangesTests {

        @Test
        @DisplayName("Разнесенные диапазоны — пустой результат")
        void disjointRanges() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            TsRange result = r1.intersection(r2);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Диапазоны далеко друг от друга — пустой результат")
        void rangesFarApart() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-06-01", "2026-06-10", "[)");

            TsRange result = r1.intersection(r2);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Разрыв в одну наносекунду — пустой результат")
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

            TsRange result = r1.intersection(r2);

            assertTrue(result.isEmpty());
        }
    }

// ==================== ПУСТЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Пустые диапазоны")
    class EmptyRangeTests {

        @Test
        @DisplayName("Пустой intersection непустой — пустой")
        void emptyIntersectionNonEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange nonEmpty = TsRange.of("2026-01-05", "2026-01-10", "[)");

            TsRange result1 = empty.intersection(nonEmpty);
            TsRange result2 = nonEmpty.intersection(empty);

            assertTrue(result1.isEmpty());
            assertTrue(result2.isEmpty());
        }

        @Test
        @DisplayName("Пустой intersection пустой — пустой")
        void emptyIntersectionEmpty() {
            TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange empty2 = TsRange.of("2026-01-05", "2026-01-05", "()");

            TsRange result = empty1.intersection(empty2);

            assertTrue(result.isEmpty());
        }
    }

// ==================== БЕСКОНЕЧНЫЕ ГРАНИЦЫ ====================

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Бесконечная верхняя: intersection с конечным")
        void infiniteUpperIntersectionFinite() {
            TsRange infinite = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );
            TsRange finite = TsRange.of("2026-01-05", "2026-01-15", "[)");

            TsRange result = infinite.intersection(finite);

            assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 15, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Бесконечная нижняя: intersection с конечным")
        void infiniteLowerIntersectionFinite() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "()"
            );
            TsRange finite = TsRange.of("2026-01-05", "2026-01-15", "[)");

            TsRange result = infinite.intersection(finite);

            assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Оба бесконечные: intersection")
        void bothInfiniteIntersection() {
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

            TsRange result = r1.intersection(r2);

            assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Полностью бесконечный intersection конечный — конечный")
        void fullyInfiniteIntersectionFinite() {
            TsRange infinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );
            TsRange finite = TsRange.of("2026-01-05", "2026-01-15", "[)");

            TsRange result = infinite.intersection(finite);

            assertTrue(finite.isEqual(result));
        }

        @Test
        @DisplayName("Бесконечные диапазоны без пересечения — пустой")
        void infiniteRangesNoIntersection() {
            TsRange r1 = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "()"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );

            TsRange result = r1.intersection(r2);

            assertTrue(result.isEmpty());
        }
    }

// ==================== ДИАПАЗОН ИЗ ОДНОЙ ТОЧКИ ====================

    @Nested
    @DisplayName("Диапазон из одной точки")
    class SinglePointRangeTests {

        @Test
        @DisplayName("Точка внутри диапазона — результат точка")
        void singlePointInsideRange() {
            TsRange single = TsRange.of("2026-01-05", "2026-01-05", "[]");
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = single.intersection(range);

            assertTrue(single.isEqual(result));
        }

        @Test
        @DisplayName("Точка на исключающей границе — пустой")
        void singlePointOnExclusiveBoundary() {
            TsRange single = TsRange.of("2026-01-10", "2026-01-10", "[]");
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = single.intersection(range);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Точка на включающей границе — результат точка")
        void singlePointOnInclusiveBoundary() {
            TsRange single = TsRange.of("2026-01-10", "2026-01-10", "[]");
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[]");

            TsRange result = single.intersection(range);

            assertTrue(single.isEqual(result));
        }

        @Test
        @DisplayName("Точка вне диапазона — пустой")
        void singlePointOutsideRange() {
            TsRange single = TsRange.of("2026-01-15", "2026-01-15", "[]");
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = single.intersection(range);

            assertTrue(result.isEmpty());
        }
    }

// ==================== ОБРАБОТКА NULL ====================

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("intersection(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.intersection(null));
        }
    }

// ==================== СВЯЗЬ С ДРУГИМИ ОПЕРАТОРАМИ ====================

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("intersection непустой ⟺ overlaps")
        void intersectionNonEmptyIffOverlaps() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");
            TsRange r3 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            // r1 и r2 пересекаются
            assertTrue(r1.overlaps(r2));
            assertFalse(r1.intersection(r2).isEmpty());

            // r1 и r3 не пересекаются
            assertFalse(r1.overlaps(r3));
            assertTrue(r1.intersection(r3).isEmpty());
        }

        @Test
        @DisplayName("intersection содержится в обоих диапазонах")
        void intersectionContainedInBoth() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            TsRange result = r1.intersection(r2);

            assertTrue(result.rangeIsContainedBy(r1));
            assertTrue(result.rangeIsContainedBy(r2));
        }

        @Test
        @DisplayName("a * b = a ⟺ a <@ b")
        void intersectionEqualsFirstIffContainedBy() {
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");

            TsRange result = inner.intersection(outer);

            assertTrue(inner.isEqual(result));
            assertTrue(inner.rangeIsContainedBy(outer));
        }

        @Test
        @DisplayName("a * b = b ⟺ b <@ a")
        void intersectionEqualsSecondIffContainedBy() {
            TsRange inner = TsRange.of("2026-01-10", "2026-01-20", "[)");
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");

            TsRange result = outer.intersection(inner);

            assertTrue(inner.isEqual(result));
            assertTrue(inner.rangeIsContainedBy(outer));
        }
    }

// ==================== МАТЕМАТИЧЕСКИЕ СВОЙСТВА ====================

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Коммутативность: a.intersection(b) == b.intersection(a)")
        void commutativity() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-15", "[)");

            TsRange result1 = r1.intersection(r2);
            TsRange result2 = r2.intersection(r1);

            assertTrue(result1.isEqual(result2));
        }

        @Test
        @DisplayName("Идемпотентность: a.intersection(a) == a")
        void idempotency() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = range.intersection(range);

            assertTrue(range.isEqual(result));
        }

        @Test
        @DisplayName("Ассоциативность: (a*b)*c == a*(b*c)")
        void associativity() {
            TsRange a = TsRange.of("2026-01-01", "2026-01-15", "[)");
            TsRange b = TsRange.of("2026-01-05", "2026-01-20", "[)");
            TsRange c = TsRange.of("2026-01-10", "2026-01-25", "[)");

            TsRange leftAssoc = a.intersection(b).intersection(c);
            TsRange rightAssoc = a.intersection(b.intersection(c));

            assertTrue(leftAssoc.isEqual(rightAssoc));
        }

        @Test
        @DisplayName("Поглощающий элемент: a.intersection(empty) == empty")
        void absorbingElement() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            TsRange result = range.intersection(empty);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Нейтральный элемент: a.intersection(fully_infinite) == a")
        void neutralElement() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange fullyInfinite = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );

            TsRange result = range.intersection(fullyInfinite);

            assertTrue(range.isEqual(result));
        }
    }

// ==================== ПАРАМЕТРИЗОВАННЫЙ ТЕСТ ====================

    @Nested
    @DisplayName("Параметризованный тест включительностей")
    class InclusivityParameterizedTests {

        @ParameterizedTest
        @CsvSource({
                // bounds1, bounds2, ожидается lowerInc, ожидается upperInc
                "'[)', '[)', true,  false",  // [ AND [ = [, ) AND ) = )
                "'()', '()', false, false",  // ( AND ( = (, ) AND ) = )
                "'[]', '[]', true,  true",   // [ AND [ = [, ] AND ] = ]
                "'[)', '[]', true,  false",  // [ AND [ = [, ) AND ] = )
                "'()', '[)', false, false",  // ( AND [ = (, ) AND ) = )
                "'(]', '[)', false, false",  // ( AND [ = (, ] AND ) = )
        })
        @DisplayName("Включительности границ при одинаковых точках")
        void inclusivityParameterized(String bounds1, String bounds2, boolean expectedLowerInc, boolean expectedUpperInc) {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", bounds1);
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", bounds2);

            TsRange result = r1.intersection(r2);

            if (!result.isEmpty()) {
                assertEquals(expectedLowerInc, result.lowerInc(),
                        "lowerInc для bounds1=" + bounds1 + ", bounds2=" + bounds2);
                assertEquals(expectedUpperInc, result.upperInc(),
                        "upperInc для bounds1=" + bounds1 + ", bounds2=" + bounds2);
            }
        }
    }

// ==================== ТОЧНОЕ ВРЕМЯ ====================

    @Nested
    @DisplayName("Точное время")
    class ExactTimeTests {

        @Test
        @DisplayName("Intersection с точным временем")
        void intersectionWithExactTime() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 11, 0),
                    LocalDateTime.of(2026, 1, 1, 16, 0),
                    "[)"
            );

            TsRange result = r1.intersection(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 11, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 1, 14, 0), result.upper());
        }

        @Test
        @DisplayName("Intersection на одну наносекунду")
        void intersectionByOneNanosecond() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 10, 0, 0, 0, 1),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    LocalDateTime.of(2026, 1, 15, 0, 0),
                    "[)"
            );

            TsRange result = r1.intersection(r2);

            // Пересечение: [01-10 00:00:00, 01-10 00:00:00.000000001)
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0, 0, 1), result.upper());
            assertFalse(result.isEmpty());
        }
    }
}
