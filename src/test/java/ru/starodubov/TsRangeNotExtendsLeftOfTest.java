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

public class TsRangeNotExtendsLeftOfTest {
    // ==================== ЛЕВЫЙ ВНУТРИ ПРАВОГО ====================

    @Nested
    @DisplayName("Левый диапазон внутри правого")
    class LeftInsideRightTests {

        @Test
        @DisplayName("Левый полностью внутри правого — true")
        void leftFullyInsideRight() {
            TsRange left = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Левый касается левой границы правого — true")
        void leftTouchesLeftBoundaryOfRight() {
            TsRange left = TsRange.of("2026-01-01", "2026-01-15", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Левый касается правой границы правого — true")
        void leftTouchesRightBoundaryOfRight() {
            TsRange left = TsRange.of("2026-01-15", "2026-01-31", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(left.notExtendsLeftOf(right));
        }
    }

// ==================== ЛЕВЫЙ ВЫХОДИТ ЛЕВЕЕ ====================

    @Nested
    @DisplayName("Левый диапазон выходит левее")
    class LeftExtendsLeftTests {

        @Test
        @DisplayName("Левый выходит за левую границу правого — false")
        void leftExtendsBeyondLeftBoundary() {
            TsRange left = TsRange.of("2025-12-15", "2026-01-31", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertFalse(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Левый полностью до правого — false")
        void leftCompletelyBeforeRight() {
            TsRange left = TsRange.of("2025-12-01", "2025-12-10", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Левый выходит левее на один день — false")
        void leftExtendsByOneDay() {
            TsRange left = TsRange.of("2025-12-31", "2026-01-10", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(left.notExtendsLeftOf(right));
        }
    }

// ==================== ЧАСТИЧНОЕ ПЕРЕСЕЧЕНИЕ ====================

    @Nested
    @DisplayName("Частичное пересечение")
    class PartialOverlapTests {

        @Test
        @DisplayName("Пересечение, левый не выходит левее — true")
        void overlapLeftNotExtendsLeft() {
            TsRange left = TsRange.of("2026-01-05", "2026-01-20", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-15", "[)");

            assertTrue(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Пересечение, левый выходит левее — false")
        void overlapLeftExtendsLeft() {
            TsRange left = TsRange.of("2025-12-25", "2026-01-15", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-20", "[)");

            assertFalse(left.notExtendsLeftOf(right));
        }
    }

// ==================== ИДЕНТИЧНЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Идентичные диапазоны")
    class IdenticalRangesTests {

        @Test
        @DisplayName("Диапазон не выходит за левую границу сам себя")
        void rangeNotExtendsLeftOfSelf() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(range.notExtendsLeftOf(range));
        }

        @Test
        @DisplayName("Два одинаковых диапазона не выходят за левую границу друг друга")
        void twoIdenticalRangesNotExtendsLeft() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.notExtendsLeftOf(r2));
            assertTrue(r2.notExtendsLeftOf(r1));
        }
    }

// ==================== ОДИНАКОВЫЕ НИЖНИЕ ГРАНИЦЫ ====================

    @Nested
    @DisplayName("Одинаковые нижние границы")
    class SameLowerBoundTests {

        @Test
        @DisplayName("Левый исключающая (, правый включающая [ — true")
        void leftExclusiveRightInclusive() {
            // (01-01, 01-10] и [01-01, 01-05)
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "()");
            TsRange right = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertTrue(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Левый включающая [, правый исключающая ( — false")
        void leftInclusiveRightExclusive() {
            // [01-01, 01-10] и (01-01, 01-05)
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange right = TsRange.of("2026-01-01", "2026-01-05", "()");

            assertFalse(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Обе включающие [ и [ — true")
        void bothInclusive() {
            // [01-01, 01-10] и [01-01, 01-05)
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "[]");
            TsRange right = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertTrue(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Обе исключающие ( и ( — true")
        void bothExclusive() {
            // (01-01, 01-10] и (01-01, 01-05)
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "()");
            TsRange right = TsRange.of("2026-01-01", "2026-01-05", "()");

            assertTrue(left.notExtendsLeftOf(right));
        }

        @ParameterizedTest
        @CsvSource({
                // lowerBounds1, lowerBounds2, ожидается notExtendsLeftOf
                "'()', '[)', true",   // ( и [ — левый не включает, правый включает
                "'[]', '()', false",  // [ и ( — левый включает, правый нет
                "'[]', '[)', true",   // [ и [ — оба включают
                "'()', '()', true",   // ( и ( — оба не включают
                "'(]', '[)', true",   // ( и [ — левый не включает, правый включает
                "'[]', '(]', false",  // [ и ( — левый включает, правый нет
                "'(]', '(]', true",   // ( и ( — оба не включают
                "'[]', '[]', true",   // [ и [ — оба включают
        })
        @DisplayName("Параметризованный тест одинаковых нижних границ")
        void sameLowerBoundParameterized(String bounds1, String bounds2, boolean expected) {
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", bounds1);
            TsRange right = TsRange.of("2026-01-01", "2026-01-05", bounds2);

            assertEquals(expected, left.notExtendsLeftOf(right),
                    "Для bounds1=" + bounds1 + ", bounds2=" + bounds2);
        }
    }

// ==================== ПУСТЫЕ ДИАПАЗОНЫ ====================

    @Nested
    @DisplayName("Пустые диапазоны")
    class EmptyRangeTests {

        @Test
        @DisplayName("Пустой диапазон не выходит за левую границу непустого — true")
        void emptyNotExtendsLeftOfNonEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange nonEmpty = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertFalse(empty.notExtendsLeftOf(nonEmpty));
        }

        @Test
        void nonEmptyNotExtendsLeftOfEmpty() {
            TsRange nonEmpty = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertFalse(nonEmpty.notExtendsLeftOf(empty));
        }

        @Test
        void emptyNotExtendsLeftOfEmpty() {
            TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange empty2 = TsRange.of("2026-12-31", "2026-12-31", "()");

            assertFalse(empty1.notExtendsLeftOf(empty2));
            assertFalse(empty2.notExtendsLeftOf(empty1));
        }
    }

// ==================== БЕСКОНЕЧНЫЕ ГРАНИЦЫ ====================

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Левый с бесконечной нижней, правый с конечной — false")
        void leftInfiniteLowerRightFinite() {
            TsRange left = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "()"
            );
            TsRange right = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertFalse(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Левый с конечной нижней, правый с бесконечной — true")
        void leftFiniteRightInfiniteLower() {
            TsRange left = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange right = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 31, 0, 0),
                    "()"
            );

            assertTrue(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Оба с бесконечной нижней — true")
        void bothInfiniteLower() {
            TsRange left = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "()"
            );
            TsRange right = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "()"
            );

            assertTrue(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Полностью бесконечный левый, конечный правый — false")
        void fullyInfiniteLeftFiniteRight() {
            TsRange left = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );
            TsRange right = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertFalse(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Конечный левый, полностью бесконечный правый — true")
        void finiteLeftFullyInfiniteRight() {
            TsRange left = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange right = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );

            assertTrue(left.notExtendsLeftOf(right));
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

            assertTrue(single.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Точка левее правого диапазона — false")
        void singlePointLeftOfRight() {
            TsRange single = TsRange.of("2025-12-25", "2025-12-25", "[]");
            TsRange right = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertFalse(single.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Точка на нижней границе правого диапазона — зависит от включительности")
        void singlePointOnLowerBoundaryOfRight() {
            TsRange single = TsRange.of("2026-01-01", "2026-01-01", "[]");
            TsRange rightInclusive = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange rightExclusive = TsRange.of("2026-01-01", "2026-01-10", "()");

            assertTrue(single.notExtendsLeftOf(rightInclusive));
            assertFalse(single.notExtendsLeftOf(rightExclusive));
        }
    }

// ==================== ОБРАБОТКА NULL ====================

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("notExtendsLeftOf(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.notExtendsLeftOf(null));
        }
    }

// ==================== СВЯЗЬ С ДРУГИМИ ОПЕРАТОРАМИ ====================

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("Если a.strictlyRightOf(b), то a.notExtendsLeftOf(b)")
        void strictlyRightImpliesNotExtendsLeft() {
            TsRange left = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(left.strictlyRightOf(right));
            assertTrue(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Если a.containsRange(b), то a.notExtendsLeftOf(b)")
        void containsRangeImpliesNotExtendsLeft() {
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");
            TsRange inner = TsRange.of("2026-01-05", "2026-01-10", "[)");

            assertTrue(outer.containsRange(inner));
            assertFalse(outer.notExtendsLeftOf(inner));
        }

        @Test
        @DisplayName("notExtendsLeftOf НЕ означает strictlyRightOf")
        void notExtendsLeftDoesNotImplyStrictlyRight() {
            TsRange left = TsRange.of("2026-01-05", "2026-01-20", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-15", "[)");

            assertTrue(left.notExtendsLeftOf(right));
            assertFalse(left.strictlyRightOf(right)); // пересекаются
        }

        @Test
        @DisplayName("notExtendsLeftOf НЕ означает containsRange")
        void notExtendsLeftDoesNotImplyContainsRange() {
            TsRange left = TsRange.of("2026-01-05", "2026-01-20", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-15", "[)");

            assertTrue(left.notExtendsLeftOf(right));
            assertFalse(left.containsRange(right)); // левый не содержит правый
        }

        @Test
        @DisplayName("Если a.overlaps(b) и a.notExtendsLeftOf(b), то нижняя a >= нижней b")
        void overlapAndNotExtendsLeftMeansLowerRelation() {
            TsRange left = TsRange.of("2026-01-05", "2026-01-20", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-15", "[)");

            assertTrue(left.overlaps(right));
            assertTrue(left.notExtendsLeftOf(right));
            // lower(left) = 01-05, lower(right) = 01-01
            assertTrue(left.lower().isAfter(right.lower()) || left.lower().isEqual(right.lower()));
        }
    }

// ==================== МАТЕМАТИЧЕСКИЕ СВОЙСТВА ====================

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Рефлексивность: a.notExtendsLeftOf(a) всегда true")
        void reflexivity() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(range.notExtendsLeftOf(range));
        }

        @Test
        @DisplayName("НЕ антисимметричность: оба могут быть true")
        void notAntisymmetric() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-15", "[)");

            // Оба имеют одинаковую нижнюю границу
            assertTrue(r1.notExtendsLeftOf(r2));
            assertTrue(r2.notExtendsLeftOf(r1));
        }

        @Test
        @DisplayName("Транзитивность: если a.notExtendsLeftOf(b) и b.notExtendsLeftOf(c), то a.notExtendsLeftOf(c)")
        void transitivity() {
            TsRange a = TsRange.of("2026-01-10", "2026-01-20", "[)");
            TsRange b = TsRange.of("2026-01-05", "2026-01-15", "[)");
            TsRange c = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(a.notExtendsLeftOf(b));
            assertTrue(b.notExtendsLeftOf(c));
            assertTrue(a.notExtendsLeftOf(c));
        }

        @Test
        @DisplayName("Полнота: для любых a и b, либо a.notExtendsLeftOf(b), либо b.notExtendsLeftOf(a)")
        void totality() {
            TsRange r1 = TsRange.of("2026-01-05", "2026-01-20", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(r1.notExtendsLeftOf(r2) || r2.notExtendsLeftOf(r1));
        }
    }

// ==================== ТОЧНОЕ ВРЕМЯ ====================

    @Nested
    @DisplayName("Точное время")
    class ExactTimeTests {

        @Test
        @DisplayName("Точное время: левый не выходит левее — true")
        void exactTimeNotExtendsLeft() {
            TsRange left = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 11, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );
            TsRange right = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[)"
            );

            assertTrue(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Точное время: левый выходит левее — false")
        void exactTimeExtendsLeft() {
            TsRange left = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 8, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "[)"
            );
            TsRange right = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[)"
            );

            assertFalse(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Точное время: одинаковые нижние границы с разной включительностью")
        void exactTimeSameLowerDifferentInclusivity() {
            TsRange left = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 14, 0),
                    "()"
            );
            TsRange rightInclusive = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "[)"
            );
            TsRange rightExclusive = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 12, 0),
                    "()"
            );

            assertTrue(left.notExtendsLeftOf(rightInclusive));
            assertTrue(left.notExtendsLeftOf(rightExclusive)); // оба исключающие
        }
    }

// ==================== ВСЕ КОМБИНАЦИИ ГРАНИЦ ====================

    @Nested
    @DisplayName("Все комбинации границ")
    class AllBoundsCombinationsTests {

        @Test
        @DisplayName("Левый с нижней позже правого — всегда true")
        void leftLowerAfterRightLower() {
            String[] bounds = {"[)", "()", "[]", "(]"};

            for (String b1 : bounds) {
                for (String b2 : bounds) {
                    TsRange left = TsRange.of("2026-01-10", "2026-01-20", b1);
                    TsRange right = TsRange.of("2026-01-01", "2026-01-15", b2);

                    assertTrue(left.notExtendsLeftOf(right),
                            "Для bounds1=" + b1 + ", bounds2=" + b2);
                }
            }
        }

        @Test
        @DisplayName("Левый с нижней раньше правого — всегда false")
        void leftLowerBeforeRightLower() {
            String[] bounds = {"[)", "()", "[]", "(]"};

            for (String b1 : bounds) {
                for (String b2 : bounds) {
                    TsRange left = TsRange.of("2026-01-01", "2026-01-20", b1);
                    TsRange right = TsRange.of("2026-01-10", "2026-01-15", b2);

                    assertFalse(left.notExtendsLeftOf(right),
                            "Для bounds1=" + b1 + ", bounds2=" + b2);
                }
            }
        }
    }

// ==================== ЗЕРКАЛЬНОСТЬ С &< ====================

    @Nested
    @DisplayName("Зеркальность с notExtendsRightOf")
    class MirrorTests {

        @Test
        @DisplayName("notExtendsLeftOf и notExtendsRightOf независимы")
        void notExtendsLeftAndRightIndependent() {
            TsRange inner = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");

            // inner не выходит ни за правую, ни за левую границу outer
            assertTrue(inner.notExtendsRightOf(outer));
            assertTrue(inner.notExtendsLeftOf(outer));

            // outer выходит и за правую, и за левую границу inner
            assertFalse(outer.notExtendsRightOf(inner));
            assertFalse(outer.notExtendsLeftOf(inner));
        }

        @Test
        @DisplayName("Если оба notExtendsLeftOf и notExtendsRightOf, то containedBy")
        void bothNotExtendsImpliesContainedBy() {
            TsRange inner = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(inner.notExtendsLeftOf(outer));
            assertTrue(inner.notExtendsRightOf(outer));
            assertTrue(inner.rangeIsContainedBy(outer));
        }

        @Test
        @DisplayName("Зеркальность: a.notExtendsLeftOf(b) <=> b.notExtendsRightOf(a) для симметричных случаев")
        void mirrorSymmetry() {
            TsRange r1 = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-15", "[)");

            // r1 не выходит за левую границу r2 (lower r1 > lower r2)
            assertTrue(r1.notExtendsLeftOf(r2));
            // r2 не выходит за правую границу r1 (upper r2 > upper r1)
            assertFalse(r2.notExtendsRightOf(r1));
        }
    }

// ==================== СВЯЗЬ С << И >> ====================

    @Nested
    @DisplayName("Связь с strictlyLeftOf и strictlyRightOf")
    class StrictlyLeftRightRelationshipTests {

        @Test
        @DisplayName("Если a.strictlyRightOf(b), то a.notExtendsLeftOf(b)")
        void strictlyRightImpliesNotExtendsLeft() {
            TsRange left = TsRange.of("2026-01-15", "2026-01-20", "[)");
            TsRange right = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(left.strictlyRightOf(right));
            assertTrue(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Если a.strictlyLeftOf(b), то НЕ a.notExtendsLeftOf(b) для непустых")
        void strictlyLeftImpliesExtendsLeft() {
            TsRange left = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange right = TsRange.of("2026-01-15", "2026-01-20", "[)");

            assertTrue(left.strictlyLeftOf(right));
            assertFalse(left.notExtendsLeftOf(right));
        }

        @Test
        @DisplayName("Если a.notExtendsLeftOf(b) и a.notExtendsRightOf(b), то a.containedBy(b)")
        void bothNotExtendsImpliesContainedBy() {
            TsRange inner = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange outer = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertTrue(inner.notExtendsLeftOf(outer));
            assertTrue(inner.notExtendsRightOf(outer));
            assertTrue(inner.rangeIsContainedBy(outer));
        }
    }
}
