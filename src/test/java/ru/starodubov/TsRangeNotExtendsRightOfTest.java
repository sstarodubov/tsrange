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

public class TsRangeNotExtendsRightOfTest {
    // ==================== ЛЕВЫЙ ВНУТРИ ПРАВОГО ====================

    @Nested
    @DisplayName("Левый диапазон внутри правого")
    class LeftInsideRightTests {

        @Test
        @DisplayName("Левый полностью внутри правого — true")
        void leftFullyInsideRight() {
            TsRange left = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Левый касается левой границы правого — true")
        void leftTouchesLeftBoundaryOfRight() {
            TsRange left = TsRange.of("2026-01-01", "2026-01-15", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Левый касается правой границы правого — true")
        void leftTouchesRightBoundaryOfRight() {
            TsRange left = TsRange.of("2026-01-15", "2026-01-31", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(left.notExtendsRightOf(right));
        }
    }

// ==================== ЛЕВЫЙ ВЫХОДИТ ПРАВЕЕ ====================

    @Nested
    @DisplayName("Левый диапазон выходит правее")
    class LeftExtendsRightTests {

        @Test
        @DisplayName("Левый выходит за правую границу правого — false")
        void leftExtendsBeyondRightBoundary() {
            TsRange left = TsRange.of("2026-01-01", "2026-02-15", "[)");
            TsRange right = TsRange.of("2026-01-05", "2026-01-31", "[)");

            assertFalse(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Левый полностью после правого — false")
        void leftCompletelyAfterRight() {
            TsRange left = TsRange.of("2026-02-01", "2026-02-10", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Левый выходит правее на один день — false")
        void leftExtendsByOneDay() {
            TsRange left = TsRange.of("2026-01-01", "2026-01-11", "[)");
            TsRange right = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertFalse(left.notExtendsRightOf(right));
        }
    }

// ==================== ЧАСТИЧНОЕ ПЕРЕСЕЧЕНИЕ ====================

    @Nested
    @DisplayName("Частичное пересечение")
    class PartialOverlapTests {

        @Test
        @DisplayName("Пересечение, левый не выходит правее — true")
        void overlapLeftNotExtendsRight() {
            TsRange left = TsRange.of("2026-01-01", "2026-01-15", "[)");
            TsRange right = TsRange.of("2026-01-05", "2026-01-20", "[)");

            assertTrue(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Пересечение, левый выходит правее — false")
        void overlapLeftExtendsRight() {
            TsRange left = TsRange.of("2026-01-05", "2026-01-25", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-20", "[)");

            assertFalse(left.notExtendsRightOf(right));
        }
    }

// ==================== ИДЕНТИЧНЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Идентичные диапазоны")
    class IdenticalRangesTests {

        @Test
        @DisplayName("Диапазон не выходит за правую границу сам себя")
        void rangeNotExtendsRightOfSelf() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(range.notExtendsRightOf(range));
        }

        @Test
        @DisplayName("Два одинаковых диапазона не выходят за правую границу друг друга")
        void twoIdenticalRangesNotExtendsRight() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.notExtendsRightOf(r2));
            assertTrue(r2.notExtendsRightOf(r1));
        }
    }

// ==================== ОДИНАКОВЫЕ ВЕРХНИЕ ГРАНИЦЫ ====================

    @Nested
    @DisplayName("Одинаковые верхние границы")
    class SameUpperBoundTests {

        @Test
        @DisplayName("Левый исключающая ), правый включающая ] — true")
        void leftExclusiveRightInclusive() {
            // [01-01, 01-10) и [01-05, 01-10]
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange right = TsRange.of("2026-01-05", "2026-01-10", "[]");

            assertTrue(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Левый включающая ], правый исключающая ) — false")
        void leftInclusiveRightExclusive() {
            // [01-01, 01-10] и [01-05, 01-10)
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange right = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertFalse(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Обе включающие ] и ] — true")
        void bothInclusive() {
            // [01-01, 01-10] и [01-05, 01-10]
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange right = TsRange.of("2026-01-05", "2026-01-10", "[]");

            assertTrue(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Обе исключающие ) и ) — true")
        void bothExclusive() {
            // [01-01, 01-10) и [01-05, 01-10)
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange right = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertTrue(left.notExtendsRightOf(right));
        }

        @ParameterizedTest
        @CsvSource({
                // upperBounds1, upperBounds2, ожидается notExtendsRightOf
                "'[)', '[]', true",   // ) и ] — левый не включает, правый включает
                "'[]', '[)', false",  // ] и ) — левый включает, правый нет
                "'[]', '[]', true",   // ] и ] — оба включают
                "'[)', '[)', true",   // ) и ) — оба не включают
                "'()', '[]', true",   // ) и ] — левый не включает, правый включает
                "'(]', '[)', false",  // ] и ) — левый включает, правый нет
                "'(]', '[]', true",   // ] и ] — оба включают
                "'()', '()', true",   // ) и ) — оба не включают
        })
        @DisplayName("Параметризованный тест одинаковых верхних границ")
        void sameUpperBoundParameterized(String bounds1, String bounds2, boolean expected) {
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", bounds1);
            TsRange right = TsRange.of("2026-01-05", "2026-01-10", bounds2);

            assertEquals(expected, left.notExtendsRightOf(right),
                    "Для bounds1=" + bounds1 + ", bounds2=" + bounds2);
        }
    }

// ==================== ПУСТЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Пустые диапазоны")
    class EmptyRangeTests {

        @Test
        void emptyNotExtendsRightOfNonEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange nonEmpty = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertFalse(empty.notExtendsRightOf(nonEmpty));
        }

        @Test
        void nonEmptyNotExtendsRightOfEmpty() {
            TsRange nonEmpty = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertFalse(nonEmpty.notExtendsRightOf(empty));
        }

        @Test
        void emptyNotExtendsRightOfEmpty() {
            TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange empty2 = TsRange.of("2026-12-31", "2026-12-31", "()");

            assertFalse(empty1.notExtendsRightOf(empty2));
            assertFalse(empty2.notExtendsRightOf(empty1));
        }
    }

// ==================== БЕСКОНЕЧНЫЕ ГРАНИЦЫ ====================

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Левый с бесконечной верхней, правый с конечной — false")
        void leftInfiniteUpperRightFinite() {
            TsRange left = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );
            TsRange right = TsRange.of("2026-01-05", "2026-01-31", "[)");

            assertFalse(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Левый с конечной верхней, правый с бесконечной — true")
        void leftFiniteRightInfiniteUpper() {
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange right = TsRange.of(
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );

            assertTrue(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Оба с бесконечной верхней — true")
        void bothInfiniteUpper() {
            TsRange left = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );
            TsRange right = TsRange.of(
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );

            assertTrue(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Полностью бесконечный левый, конечный правый — false")
        void fullyInfiniteLeftFiniteRight() {
            TsRange left = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );
            TsRange right = TsRange.of("2026-01-05", "2026-01-31", "[)");

            assertFalse(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Конечный левый, полностью бесконечный правый — true")
        void finiteLeftFullyInfiniteRight() {
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange right = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );

            assertTrue(left.notExtendsRightOf(right));
        }
    }

// ==================== ДИАПАЗОН ИЗ ОДНОЙ ТОЧКИ ====================

    @Nested
    @DisplayName("Диапазон из одной точки")
    class SinglePointRangeTests {

        @Test
        @DisplayName("Точка внутри правого диапазона — true")
        void singlePointInsideRight() {
            TsRange single = TsRange.of("2026-01-05", "2026-01-05", "[]");
            TsRange right = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(single.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Точка правее правого диапазона — false")
        void singlePointRightOfRight() {
            TsRange single = TsRange.of("2026-01-15", "2026-01-15", "[]");
            TsRange right = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(single.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Точка на верхней границе правого диапазона — зависит от включительности")
        void singlePointOnUpperBoundaryOfRight() {
            TsRange single = TsRange.of("2026-01-10", "2026-01-10", "[]");
            TsRange rightInclusive = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange rightExclusive = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(single.notExtendsRightOf(rightInclusive));
            assertFalse(single.notExtendsRightOf(rightExclusive));
        }
    }

// ==================== ОБРАБОТКА NULL ====================

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("notExtendsRightOf(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.notExtendsRightOf(null));
        }
    }

// ==================== СВЯЗЬ С ДРУГИМИ ОПЕРАТОРАМИ ====================

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("Если a.strictlyLeftOf(b), то a.notExtendsRightOf(b)")
        void strictlyLeftImpliesNotExtendsRight() {
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange right = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertTrue(left.strictlyLeftOf(right));
            assertTrue(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Если a.containedBy(b), то a.notExtendsRightOf(b)")
        void containedByImpliesNotExtendsRight() {
            TsRange inner = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(inner.rangeIsContainedBy(outer));
            assertTrue(inner.notExtendsRightOf(outer));
        }

        @Test
        @DisplayName("notExtendsRightOf НЕ означает strictlyLeftOf")
        void notExtendsRightDoesNotImplyStrictlyLeft() {
            TsRange left = TsRange.of("2026-01-01", "2026-01-15", "[)");
            TsRange right = TsRange.of("2026-01-05", "2026-01-20", "[)");

            assertTrue(left.notExtendsRightOf(right));
            assertFalse(left.strictlyLeftOf(right)); // пересекаются
        }

        @Test
        @DisplayName("notExtendsRightOf НЕ означает containedBy")
        void notExtendsRightDoesNotImplyContainedBy() {
            TsRange left = TsRange.of("2026-01-01", "2026-01-15", "[)");
            TsRange right = TsRange.of("2026-01-05", "2026-01-20", "[)");

            assertTrue(left.notExtendsRightOf(right));
            assertFalse(left.rangeIsContainedBy(right)); // левый выходит за левую границу правого
        }

        @Test
        @DisplayName("Если a.overlaps(b) и a.notExtendsRightOf(b), то верхняя a <= верхней b")
        void overlapAndNotExtendsRightMeansUpperRelation() {
            TsRange left = TsRange.of("2026-01-01", "2026-01-15", "[)");
            TsRange right = TsRange.of("2026-01-05", "2026-01-20", "[)");

            assertTrue(left.overlaps(right));
            assertTrue(left.notExtendsRightOf(right));
            // upper(left) = 01-15, upper(right) = 01-20
            assertTrue(left.upper().isBefore(right.upper()) || left.upper().isEqual(right.upper()));
        }
    }

// ==================== МАТЕМАТИЧЕСКИЕ СВОЙСТВА ====================

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Рефлексивность: a.notExtendsRightOf(a) всегда true")
        void reflexivity() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(range.notExtendsRightOf(range));
        }

        @Test
        @DisplayName("НЕ антисимметричность: оба могут быть true")
        void notAntisymmetric() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-10", "[)");

            // Оба имеют одинаковую верхнюю границу
            assertTrue(r1.notExtendsRightOf(r2));
            assertTrue(r2.notExtendsRightOf(r1));
        }

        @Test
        @DisplayName("Транзитивность: если a.notExtendsRightOf(b) и b.notExtendsRightOf(c), то a.notExtendsRightOf(c)")
        void transitivity() {
            TsRange a = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange b = TsRange.of("2026-01-05", "2026-01-15", "[)");
            TsRange c = TsRange.of("2026-01-10", "2026-01-20", "[)");

            assertTrue(a.notExtendsRightOf(b));
            assertTrue(b.notExtendsRightOf(c));
            assertTrue(a.notExtendsRightOf(c));
        }

        @Test
        @DisplayName("Полнота: для любых a и b, либо a.notExtendsRightOf(b), либо b.notExtendsRightOf(a)")
        void totality() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-05", "2026-01-20", "[)");

            assertTrue(r1.notExtendsRightOf(r2) || r2.notExtendsRightOf(r1));
        }
    }

// ==================== ТОЧНОЕ ВРЕМЯ ====================

    @Nested
    @DisplayName("Точное время")
    class ExactTimeTests {

        @Test
        @DisplayName("Точное время: левый не выходит правее — true")
        void exactTimeNotExtendsRight() {
            TsRange left = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[)"
            );
            TsRange right = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 11, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );

            assertTrue(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Точное время: левый выходит правее — false")
        void exactTimeExtendsRight() {
            TsRange left = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 15, 0),
                    "[)"
            );
            TsRange right = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 11, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );

            assertFalse(left.notExtendsRightOf(right));
        }

        @Test
        @DisplayName("Точное время: одинаковые верхние границы с разной включительностью")
        void exactTimeSameUpperDifferentInclusivity() {
            TsRange left = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );
            TsRange rightInclusive = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 11, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[]"
            );
            TsRange rightExclusive = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 11, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );

            assertTrue(left.notExtendsRightOf(rightInclusive));
            assertTrue(left.notExtendsRightOf(rightExclusive)); // оба исключающие
        }
    }

// ==================== ВСЕ КОМБИНАЦИИ ГРАНИЦ ====================

    @Nested
    @DisplayName("Все комбинации границ")
    class AllBoundsCombinationsTests {

        @Test
        @DisplayName("Левый с верхней раньше правого — всегда true")
        void leftUpperBeforeRightUpper() {
            String[] bounds = {"[)", "()", "[]", "(]"};

            for (String b1 : bounds) {
                for (String b2 : bounds) {
                    TsRange left = TsRange.of("2026-01-01", "2026-01-10", b1);
                    TsRange right = TsRange.of("2026-01-05", "2026-01-20", b2);

                    assertTrue(left.notExtendsRightOf(right),
                            "Для bounds1=" + b1 + ", bounds2=" + b2);
                }
            }
        }

        @Test
        @DisplayName("Левый с верхней позже правого — всегда false")
        void leftUpperAfterRightUpper() {
            String[] bounds = {"[)", "()", "[]", "(]"};

            for (String b1 : bounds) {
                for (String b2 : bounds) {
                    TsRange left = TsRange.of("2026-01-01", "2026-01-20", b1);
                    TsRange right = TsRange.of("2026-01-05", "2026-01-10", b2);

                    assertFalse(left.notExtendsRightOf(right),
                            "Для bounds1=" + b1 + ", bounds2=" + b2);
                }
            }
        }
    }

// ==================== ЗЕРКАЛЬНОСТЬ С &> ====================

    @Nested
    @DisplayName("Зеркальность с notExtendsLeftOf")
    class MirrorTests {

        @Test
        @DisplayName("notExtendsRightOf и notExtendsLeftOf независимы")
        void notExtendsRightAndLeftIndependent() {
            TsRange inner = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");

            // inner не выходит ни за правую, ни за левую границу outer
            assertTrue(inner.notExtendsRightOf(outer));

            // outer не выходит за правую границу inner? Нет, выходит!
            assertFalse(outer.notExtendsRightOf(inner));
        }

        @Test
        @DisplayName("Если оба notExtendsRightOf и notExtendsLeftOf, то containedBy")
        void bothNotExtendsImpliesContainedBy() {
            TsRange inner = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(inner.notExtendsRightOf(outer));
            assertTrue(inner.rangeIsContainedBy(outer));
        }
    }
}
