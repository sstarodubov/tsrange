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


public class TsRangeDifferenceTest {

    // ==================== ОТРЕЗАНИЕ ПРАВОЙ ЧАСТИ ====================

    @Nested
    @DisplayName("range отрезает правую часть this")
    class CutsRightPartTests {

        @Test
        @DisplayName("Отрезание правой части")
        void cutsRightPart() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-02-15", "[)");

            TsRange result = r1.difference(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
            assertTrue(result.lowerInc());
            assertFalse(result.upperInc()); // NOT lower2Inc = NOT true = false
        }

        @Test
        @DisplayName("Отрезание правой части: range с исключающей нижней")
        void cutsRightPartExclusiveLower() {
            // [01-01, 01-31) - (01-10, 02-15) = [01-01, 01-10]
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-02-15", "()");

            TsRange result = r1.difference(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
            assertTrue(result.lowerInc());
            assertTrue(result.upperInc()); // NOT lower2Inc = NOT false = true
        }

        @Test
        @DisplayName("Отрезание правой части: this с исключающей нижней")
        void cutsRightPartThisExclusiveLower() {
            // (01-01, 01-31) - [01-10, 02-15) = (01-01, 01-10)
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "()");
            TsRange r2 = TsRange.of("2026-01-10", "2026-02-15", "[)");

            TsRange result = r1.difference(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertFalse(result.lowerInc()); // из r1
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
            assertFalse(result.upperInc()); // NOT lower2Inc = NOT true = false
        }
    }

// ==================== ОТРЕЗАНИЕ ЛЕВОЙ ЧАСТИ ====================

    @Nested
    @DisplayName("range отрезает левую часть this")
    class CutsLeftPartTests {

        @Test
        @DisplayName("Отрезание левой части")
        void cutsLeftPart() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange r2 = TsRange.of("2025-12-15", "2026-01-10", "[)");

            TsRange result = r1.difference(r2);

            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 31, 0, 0), result.upper());
            assertTrue(result.lowerInc()); // NOT upper2Inc = NOT false = true
            assertFalse(result.upperInc()); // из r1
        }

        @Test
        @DisplayName("Отрезание левой части: range с включающей верхней")
        void cutsLeftPartInclusiveUpper() {
            // [01-01, 01-31) - [12-15, 01-10] = (01-10, 01-31)
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange r2 = TsRange.of("2025-12-15", "2026-01-10", "[]");

            TsRange result = r1.difference(r2);

            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.lower());
            assertFalse(result.lowerInc()); // NOT upper2Inc = NOT true = false
            assertEquals(LocalDateTime.of(2026, 1, 31, 0, 0), result.upper());
            assertFalse(result.upperInc()); // из r1
        }

        @Test
        @DisplayName("Отрезание левой части: this с включающей верхней")
        void cutsLeftPartThisInclusiveUpper() {
            // [01-01, 01-31] - [12-15, 01-10) = [01-10, 01-31]
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[]");
            TsRange r2 = TsRange.of("2025-12-15", "2026-01-10", "[)");

            TsRange result = r1.difference(r2);

            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.lower());
            assertTrue(result.lowerInc()); // NOT upper2Inc = NOT false = true
            assertEquals(LocalDateTime.of(2026, 1, 31, 0, 0), result.upper());
            assertTrue(result.upperInc()); // из r1
        }
    }

// ==================== РАЗРЕЗАНИЕ ПОСЕРЕДИНЕ ====================

    @Nested
    @DisplayName("range разрезает this посередине — ОШИБКА")
    class CutsMiddleTests {

        @Test
        @DisplayName("Разрезание посередине — UnsupportedOperationException")
        void cutsMiddleThrows() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            UnsupportedOperationException ex = assertThrows(
                    UnsupportedOperationException.class,
                    () -> r1.difference(r2)
            );

            assertTrue(ex.getMessage().contains("not be contiguous"));
        }

        @Test
        @DisplayName("Разрезание посередине с бесконечной границей — ОШИБКА")
        void cutsMiddleWithInfiniteThrows() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertThrows(UnsupportedOperationException.class, () -> r1.difference(r2));
        }
    }

// ==================== ПОЛНОЕ ВЛОЖЕНИЕ ====================

    @Nested
    @DisplayName("Полное вложение")
    class FullContainmentTests {

        @Test
        @DisplayName("range полностью содержит this — EMPTY")
        void rangeFullyContainsThis() {
            TsRange r1 = TsRange.of("2026-01-10", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-31", "[)");

            TsRange result = r1.difference(r2);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Идентичные диапазоны — EMPTY")
        void identicalRanges() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = r1.difference(r2);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("this вложен в range с разными включительностями — EMPTY")
        void thisContainedInRangeDifferentInclusivity() {
            TsRange r1 = TsRange.of("2026-01-10", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-31", "[]");

            TsRange result = r1.difference(r2);

            assertTrue(result.isEmpty());
        }
    }

// ==================== НЕТ ПЕРЕСЕЧЕНИЯ ====================

    @Nested
    @DisplayName("Нет пересечения")
    class NoOverlapTests {

        @Test
        @DisplayName("Разнесенные диапазоны — результат this")
        void disjointRanges() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-15", "2026-01-20", "[)");

            TsRange result = r1.difference(r2);

            assertTrue(r1.isEqual(result));
        }

        @Test
        @DisplayName("Смежные диапазоны — результат this")
        void adjacentRanges() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            TsRange result = r1.difference(r2);

            assertTrue(r1.isEqual(result));
        }
    }

// ==================== ПУСТЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Пустые диапазоны")
    class EmptyRangeTests {

        @Test
        @DisplayName("this - empty = this")
        void thisMinusEmpty() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = r1.difference(TsRange.EMPTY);

            assertTrue(r1.isEqual(result));
        }

        @Test
        @DisplayName("empty - range = empty")
        void emptyMinusRange() {
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = TsRange.EMPTY.difference(r2);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("empty - empty = empty")
        void emptyMinusEmpty() {
            TsRange result = TsRange.EMPTY.difference(TsRange.EMPTY);

            assertTrue(result.isEmpty());
        }
    }

// ==================== БЕСКОНЕЧНЫЕ ГРАНИЦЫ ====================

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Бесконечная верхняя: отрезание правой части")
        void infiniteUpperCutsRight() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );

            TsRange result = r1.difference(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
            assertFalse(result.upperInc());
        }

        @Test
        @DisplayName("Бесконечная нижняя: отрезание левой части")
        void infiniteLowerCutsLeft() {
            TsRange r1 = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 31, 0, 0),
                    "()"
            );
            TsRange r2 = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "()"
            );

            TsRange result = r1.difference(r2);

            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 31, 0, 0), result.upper());
            assertTrue(result.lowerInc()); // NOT upper2Inc = NOT false = true
        }

        @Test
        @DisplayName("Полностью бесконечный минус конечный — ОШИБКА (разрез посередине)")
        void fullyInfiniteMinusFiniteThrows() {
            TsRange r1 = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertThrows(UnsupportedOperationException.class, () -> r1.difference(r2));
        }

        @Test
        @DisplayName("Полностью бесконечный минус бесконечный вправо")
        void fullyInfiniteMinusInfiniteRight() {
            TsRange r1 = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );

            TsRange result = r1.difference(r2);

            assertEquals(TsRange.MINUS_INFINITY, result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
            assertFalse(result.upperInc());
        }
    }

// ==================== ДИАПАЗОН ИЗ ОДНОЙ ТОЧКИ ====================

    @Nested
    @DisplayName("Диапазон из одной точки")
    class SinglePointRangeTests {

        @Test
        @DisplayName("Вычитание точки из диапазона — разрез посередине")
        void subtractPointFromRange() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange point = TsRange.of("2026-01-15", "2026-01-15", "[]");

            assertThrows(UnsupportedOperationException.class, () -> r1.difference(point));
        }

        @Test
        @DisplayName("Вычитание точки на границе — отрезание части")
        void subtractPointOnBoundary() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange point = TsRange.of("2026-01-01", "2026-01-01", "[]");

            TsRange result = r1.difference(point);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertFalse(result.lowerInc()); // NOT upper2Inc = NOT true = false
        }

        @Test
        @DisplayName("Вычитание диапазона из точки — EMPTY")
        void subtractRangeFromPoint() {
            TsRange point = TsRange.of("2026-01-15", "2026-01-15", "[]");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-31", "[)");

            TsRange result = point.difference(r2);

            assertTrue(result.isEmpty());
        }
    }

// ==================== ОБРАБОТКА NULL ====================

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("difference(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.difference(null));
        }
    }

// ==================== СВЯЗЬ С ДРУГИМИ ОПЕРАТОРАМИ ====================

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("(a - b) + (a * b) == a для непрерывных случаев")
        void differencePlusIntersectionEqualsOriginal() {
            TsRange a = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange b = TsRange.of("2026-01-10", "2026-02-15", "[)");

            TsRange diff = a.difference(b);
            TsRange inter = a.intersection(b);

            // diff = [01-01, 01-10), inter = [01-10, 01-31)
            // Они смежны, поэтому union работает
            TsRange union = diff.union(inter);

            assertTrue(a.isEqual(union));
        }

        @Test
        @DisplayName("Если НЕ a.overlaps(b), то a.difference(b) == a")
        void noOverlapMeansNoDifference() {
            TsRange a = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange b = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertFalse(a.overlaps(b));
            assertTrue(a.difference(b).isEqual(a));
        }

        @Test
        @DisplayName("Если b.containsRange(a), то a.difference(b) == EMPTY")
        void containsMeansEmptyDifference() {
            TsRange a = TsRange.of("2026-01-10", "2026-01-20", "[)");
            TsRange b = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(b.containsRange(a));
            assertTrue(a.difference(b).isEmpty());
        }
    }

// ==================== МАТЕМАТИЧЕСКИЕ СВОЙСТВА ====================

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("a.difference(a) == EMPTY")
        void selfDifference() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(range.difference(range).isEmpty());
        }

        @Test
        @DisplayName("a.difference(EMPTY) == a")
        void differenceWithEmpty() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(range.difference(TsRange.EMPTY).isEqual(range));
        }

        @Test
        @DisplayName("EMPTY.difference(a) == EMPTY")
        void emptyDifferenceWithAny() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(TsRange.EMPTY.difference(range).isEmpty());
        }

        @Test
        @DisplayName("difference НЕ коммутативна: a - b != b - a")
        void notCommutative() {
            TsRange a = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange b = TsRange.of("2026-01-10", "2026-02-15", "[)");

            TsRange aMinusB = a.difference(b); // [01-01, 01-10)
            TsRange bMinusA = b.difference(a); // [01-31, 02-15)

            assertFalse(aMinusB.isEqual(bMinusA));
        }

        @Test
        @DisplayName("difference НЕ ассоциативна: (a-b)-c != a-(b-c)")
        void notAssociative() {
            TsRange a = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange b = TsRange.of("2026-01-10", "2026-02-15", "[)");
            TsRange c = TsRange.of("2025-12-20", "2026-01-05", "[)");

            // Левая ассоциативность: (a-b)-c
            // a - b = [01-01, 01-10)  — отрезание правой части
            // (a-b) - c = [01-05, 01-10)  — отрезание левой части
            TsRange leftAssoc = a.difference(b).difference(c);

            // Правая ассоциативность: a-(b-c)
            // b - c = [01-10, 02-15)  — нет пересечения, возвращается b
            // a - (b-c) = [01-01, 01-10)  — отрезание правой части
            TsRange rightAssoc = a.difference(b.difference(c));

            // Результаты разные: [01-05, 01-10) ≠ [01-01, 01-10)
            assertFalse(leftAssoc.isEqual(rightAssoc),
                    "difference не ассоциативна: (a-b)-c != a-(b-c)");

            // Проверяем конкретные результаты
            assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), leftAssoc.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), leftAssoc.upper());

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), rightAssoc.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), rightAssoc.upper());
        }
    }

    @Nested
    @DisplayName("Баг 1: пустой range")
    class Bug1Tests {

        @Test
        @DisplayName("this - empty = this")
        void thisMinusEmpty() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = r1.difference(TsRange.EMPTY);

            assertTrue(r1.isEqual(result));
        }

        @Test
        @DisplayName("empty - range = empty")
        void emptyMinusRange() {
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            TsRange result = TsRange.EMPTY.difference(r2);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Баг 2: cmp == 0 в ветке lessThan")
    class Bug2Tests {

        @Test
        @DisplayName("range отрезает правую часть, cmp == 0 — НЕ исключение")
        void cutsRightPartSameUpper() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-31", "[)");

            TsRange result = r1.difference(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.upper());
            assertTrue(result.lowerInc());
            assertFalse(result.upperInc());
        }

        @Test
        @DisplayName("Разрез посередине, cmp > 0 — исключение")
        void cutsMiddleThrows() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertThrows(UnsupportedOperationException.class, () -> r1.difference(r2));
        }
    }

    @Nested
    @DisplayName("Баг 3: инверсия включительности при отрезании правой части")
    class Bug3Tests {

        @Test
        @DisplayName("range с включающей нижней → верхняя результата исключающая")
        void inclusiveLowerGivesExclusiveUpper() {
            // [01-01, 01-31) - [01-10, 02-15) = [01-01, 01-10)
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-02-15", "[)");

            TsRange result = r1.difference(r2);

            assertFalse(result.upperInc()); // NOT true = false
        }

        @Test
        @DisplayName("range с исключающей нижней → верхняя результата включающая")
        void exclusiveLowerGivesInclusiveUpper() {
            // [01-01, 01-31) - (01-10, 02-15) = [01-01, 01-10]
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange r2 = TsRange.of("2026-01-10", "2026-02-15", "()");

            TsRange result = r1.difference(r2);

            assertTrue(result.upperInc()); // NOT false = true
        }
    }

    @Nested
    @DisplayName("Баг 4 и 5: границы и включительности при отрезании левой части")
    class Bug4And5Tests {

        @Test
        @DisplayName("Отрезание левой части: нижняя граница = range.upper")
        void cutsLeftPartLowerBound() {
            // [01-01, 01-31) - [12-15, 01-10) = [01-10, 01-31)
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange r2 = TsRange.of("2025-12-15", "2026-01-10", "[)");

            TsRange result = r1.difference(r2);

            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.lower()); // range.upper
            assertEquals(LocalDateTime.of(2026, 1, 31, 0, 0), result.upper()); // this.upper
        }

        @Test
        @DisplayName("Отрезание левой части: нижняя включительность = NOT range.upperInc")
        void cutsLeftPartLowerInclusivity() {
            // [01-01, 01-31) - [12-15, 01-10) = [01-10, 01-31)
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange r2 = TsRange.of("2025-12-15", "2026-01-10", "[)");

            TsRange result = r1.difference(r2);

            assertTrue(result.lowerInc()); // NOT false = true
        }

        @Test
        @DisplayName("Отрезание левой части: range с включающей верхней → нижняя исключающая")
        void cutsLeftPartInclusiveUpper() {
            // [01-01, 01-31) - [12-15, 01-10] = (01-10, 01-31)
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange r2 = TsRange.of("2025-12-15", "2026-01-10", "[]");

            TsRange result = r1.difference(r2);

            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result.lower());
            assertFalse(result.lowerInc()); // NOT true = false
        }

        @Test
        @DisplayName("Отрезание левой части: верхняя включительность = this.upperInc")
        void cutsLeftPartUpperInclusivity() {
            // [01-01, 01-31] - [12-15, 01-10) = [01-10, 01-31]
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[]");
            TsRange r2 = TsRange.of("2025-12-15", "2026-01-10", "[)");

            TsRange result = r1.difference(r2);

            assertTrue(result.upperInc()); // this.upperInc = true
        }
    }

    @Nested
    @DisplayName("Диапазон из одной точки")
    class SinglePointResultTests {

        @Test
        @DisplayName("Разные верхние включительности → диапазон из одной точки")
        void differentUpperInclusivityGivesSinglePoint() {
            // [01-01, 01-31] - [12-15, 01-31) = [01-31, 01-31]
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-31", "[]");
            TsRange r2 = TsRange.of("2025-12-15", "2026-01-31", "[)");

            TsRange result = r1.difference(r2);

            assertFalse(result.isEmpty());
            assertEquals(LocalDateTime.of(2026, 1, 31, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 31, 0, 0), result.upper());
            assertTrue(result.lowerInc());
            assertTrue(result.upperInc());
        }
    }
// ==================== ТОЧНОЕ ВРЕМЯ ====================

    @Nested
    @DisplayName("Точное время")
    class ExactTimeTests {

        @Test
        @DisplayName("Difference с точным временем")
        void differenceWithExactTime() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 18, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    LocalDateTime.of(2026, 1, 1, 20, 0),
                    "[)"
            );

            TsRange result = r1.difference(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 9, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 1, 14, 0), result.upper());
        }

        @Test
        @DisplayName("Difference на одну наносекунду")
        void differenceByOneNanosecond() {
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

            // r1 и r2 не пересекаются (разрыв в 1 наносекунду)
            TsRange result = r1.difference(r2);

            assertTrue(r1.isEqual(result));
        }
    }
}
