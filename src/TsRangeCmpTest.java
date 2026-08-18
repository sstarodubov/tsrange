package ru.nspk.pcl.common.tsrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TsRangeCmpTest {

    @Nested
    @DisplayName("Пустые диапазоны")
    class LEEmptyRangeTests {

        @Nested
        @DisplayName("Пустые диапазоны")
        class EmptyRangeTests {

            @Test
            @DisplayName("Непустой >= пустой: true")
            void nonEmptyGreaterThanOrEqualEmpty() {
                TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-05", "[)");
                TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

                assertTrue(nonEmpty.greaterThanOrEqual(empty));
            }

            @Test
            @DisplayName("Пустой >= непустой: false")
            void emptyNotGreaterThanOrEqualNonEmpty() {
                TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
                TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-05", "[)");

                assertFalse(empty.greaterThanOrEqual(nonEmpty));
            }

            @Test
            @DisplayName("Два пустых диапазона >= друг друга: true (они равны)")
            void emptyGreaterThanOrEqualEmpty() {
                TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
                TsRange empty2 = TsRange.of("2026-12-31", "2026-12-31", "()");

                assertTrue(empty1.greaterThanOrEqual(empty2));
                assertTrue(empty2.greaterThanOrEqual(empty1));
            }
        }

        @Nested
        @DisplayName("Идентичные диапазоны")
        class IdenticalRangesTests {

            @Test
            @DisplayName("Диапазон >= сам себя: true")
            void rangeGreaterThanOrEqualSelf() {
                TsRange range = TsRange.of("2026-01-01", "2026-01-05", "[)");

                assertTrue(range.greaterThanOrEqual(range));
            }

            @Test
            @DisplayName("Два одинаковых диапазона >= друг друга: true")
            void twoIdenticalRangesGreaterThanOrEqual() {
                TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[)");
                TsRange r2 = TsRange.of("2026-01-01", "2026-01-05", "[)");

                assertTrue(r1.greaterThanOrEqual(r2));
                assertTrue(r2.greaterThanOrEqual(r1));
            }

            @Test
            @DisplayName("Одинаковые значения с одинаковой включительностью: true")
            void sameValuesSameInclusivity() {
                TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[]");
                TsRange r2 = TsRange.of("2026-01-01", "2026-01-05", "[]");

                assertTrue(r1.greaterThanOrEqual(r2));
            }
        }

        @Nested
        @DisplayName("Строго больше (greaterThan = true)")
        class StrictlyGreaterTests {

            @Test
            @DisplayName("Большая нижняя граница: true")
            void largerLowerBound() {
                TsRange later = TsRange.of("2026-01-05", "2026-01-10", "[)");
                TsRange earlier = TsRange.of("2026-01-01", "2026-01-03", "[)");

                assertTrue(later.greaterThanOrEqual(earlier));
            }

            @Test
            @DisplayName("Одинаковые нижние, большая верхняя: true")
            void sameLowerLargerUpper() {
                TsRange longer = TsRange.of("2026-01-01", "2026-01-10", "[)");
                TsRange shorter = TsRange.of("2026-01-01", "2026-01-05", "[)");

                assertTrue(longer.greaterThanOrEqual(shorter));
            }

            @Test
            @DisplayName("Исключающая нижняя >= включающей нижней при равных точках: false")
            void exclusiveLowerNotGreaterThanOrEqualInclusive() {
                TsRange exclusive = TsRange.of("2026-01-01", "2026-01-05", "()");
                TsRange inclusive = TsRange.of("2026-01-01", "2026-01-05", "[)");

                assertTrue(exclusive.greaterThanOrEqual(inclusive));
            }

            @Test
            @DisplayName("Включающая нижняя >= исключающей нижней при равных точках: true")
            void inclusiveLowerGreaterThanOrEqualExclusive() {
                TsRange inclusive = TsRange.of("2026-01-01", "2026-01-05", "[)");
                TsRange exclusive = TsRange.of("2026-01-01", "2026-01-05", "()");

                assertFalse(inclusive.greaterThanOrEqual(exclusive));
            }

            @Test
            @DisplayName("Включающая верхняя >= исключающей верхней при равных точках: true")
            void inclusiveUpperGreaterThanOrEqualExclusive() {
                TsRange inclusive = TsRange.of("2026-01-01", "2026-01-05", "[]");
                TsRange exclusive = TsRange.of("2026-01-01", "2026-01-05", "[)");

                assertTrue(inclusive.greaterThanOrEqual(exclusive));
            }

            @Test
            @DisplayName("Исключающая верхняя >= включающей верхней при равных точках: false")
            void exclusiveUpperNotGreaterThanOrEqualInclusive() {
                TsRange exclusive = TsRange.of("2026-01-01", "2026-01-05", "[)");
                TsRange inclusive = TsRange.of("2026-01-01", "2026-01-05", "[]");

                assertFalse(exclusive.greaterThanOrEqual(inclusive));
            }
        }

        @Nested
        @DisplayName("Строго меньше (greaterThan = false, equal = false)")
        class StrictlyLessTests {

            @Test
            @DisplayName("Меньшая нижняя граница: false")
            void smallerLowerBound() {
                TsRange earlier = TsRange.of("2026-01-01", "2026-01-05", "[)");
                TsRange later = TsRange.of("2026-01-03", "2026-01-10", "[)");

                assertFalse(earlier.greaterThanOrEqual(later));
            }

            @Test
            @DisplayName("Одинаковые нижние, меньшая верхняя: false")
            void sameLowerSmallerUpper() {
                TsRange shorter = TsRange.of("2026-01-01", "2026-01-05", "[)");
                TsRange longer = TsRange.of("2026-01-01", "2026-01-10", "[)");

                assertFalse(shorter.greaterThanOrEqual(longer));
            }
        }

        @Nested
        @DisplayName("Связь с другими методами")
        class RelationshipTests {

            @Test
            @DisplayName("greaterThanOrEqual = greaterThan OR equal")
            void greaterThanOrEqualIsGreaterThanOrEqual() {
                TsRange r1 = TsRange.of("2026-01-05", "2026-01-10", "[)");
                TsRange r2 = TsRange.of("2026-01-01", "2026-01-03", "[)");
                TsRange r3 = TsRange.of("2026-01-05", "2026-01-10", "[)");

                // r1 > r2, r1 != r2
                assertTrue(r1.greaterThan(r2));
                assertFalse(r1.isEqual(r2));
                assertTrue(r1.greaterThanOrEqual(r2));

                // r1 == r3
                assertFalse(r1.greaterThan(r3));
                assertTrue(r1.isEqual(r3));
                assertTrue(r1.greaterThanOrEqual(r3));
            }

            @Test
            @DisplayName("greaterThanOrEqual = NOT lessThan")
            void greaterThanOrEqualIsNotLessThan() {
                TsRange r1 = TsRange.of("2026-01-05", "2026-01-10", "[)");
                TsRange r2 = TsRange.of("2026-01-01", "2026-01-03", "[)");

                assertTrue(r1.greaterThanOrEqual(r2));
                assertFalse(r1.lessThan(r2));

                assertFalse(r2.greaterThanOrEqual(r1));
                assertTrue(r2.lessThan(r1));
            }

            @Test
            @DisplayName("Симметрия: a >= b <=> b <= a")
            void symmetryWithLessThanOrEqual() {
                TsRange r1 = TsRange.of("2026-01-05", "2026-01-10", "[)");
                TsRange r2 = TsRange.of("2026-01-01", "2026-01-03", "[)");

                assertTrue(r1.greaterThanOrEqual(r2));
                assertTrue(r2.lessThanOrEqual(r1));

                assertFalse(r2.greaterThanOrEqual(r1));
                assertFalse(r1.lessThanOrEqual(r2));
            }
        }

        @Nested
        @DisplayName("Обработка null")
        class NullHandlingTests {

            @Test
            @DisplayName("greaterThanOrEqual(null) бросает IllegalArgumentException")
            void nullThrowsException() {
                TsRange range = TsRange.of("2026-01-01", "2026-01-05", "[)");

                assertThrows(IllegalArgumentException.class, () -> range.greaterThanOrEqual(null));
            }
        }

        @Nested
        @DisplayName("Математические свойства")
        class MathematicalPropertyTests {

            @Test
            @DisplayName("Если a >= b и b >= a, то a == b (антисимметричность)")
            void antisymmetry() {
                TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[)");
                TsRange r2 = TsRange.of("2026-01-01", "2026-01-05", "[)");

                assertTrue(r1.greaterThanOrEqual(r2));
                assertTrue(r2.greaterThanOrEqual(r1));
                assertTrue(r1.isEqual(r2));
            }

            @Test
            @DisplayName("Если a >= b и b >= c, то a >= c (транзитивность)")
            void transitivity() {
                TsRange a = TsRange.of("2026-01-05", "2026-01-10", "[)");
                TsRange b = TsRange.of("2026-01-03", "2026-01-07", "[)");
                TsRange c = TsRange.of("2026-01-01", "2026-01-04", "[)");

                assertTrue(a.greaterThanOrEqual(b));
                assertTrue(b.greaterThanOrEqual(c));
                assertTrue(a.greaterThanOrEqual(c));
            }

            @Test
            @DisplayName("Для любых a и b: либо a >= b, либо b >= a (полнота)")
            void totality() {
                TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[)");
                TsRange r2 = TsRange.of("2026-01-03", "2026-01-10", "[)");

                assertTrue(r1.greaterThanOrEqual(r2) || r2.greaterThanOrEqual(r1));
            }

            @Test
            @DisplayName("Рефлексивность: a >= a всегда true")
            void reflexivity() {
                TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[)");
                TsRange r2 = TsRange.of("2026-01-01", "2026-01-01", "[)"); // пустой

                assertTrue(r1.greaterThanOrEqual(r1));
                assertTrue(r2.greaterThanOrEqual(r2));
            }
        }

        @Nested
        @DisplayName("Комбинация разных включительностей")
        class MixedInclusiveTests {

            @Test
            @DisplayName("Разные нижние границы с разной включительностью")
            void differentLowerWithDifferentInclusivity() {
                // [2026-01-05, ...) >= (2026-01-01, ...)
                TsRange r1 = TsRange.of("2026-01-05", "2026-01-10", "[)");
                TsRange r2 = TsRange.of("2026-01-01", "2026-01-03", "()");

                assertTrue(r1.greaterThanOrEqual(r2));
            }

            @Test
            @DisplayName("Разные верхние границы с разной включительностью")
            void differentUpperWithDifferentInclusivity() {
                // [..., 2026-01-10] >= [..., 2026-01-05)
                TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[]");
                TsRange r2 = TsRange.of("2026-01-01", "2026-01-05", "[)");

                assertTrue(r1.greaterThanOrEqual(r2));
            }

            @Test
            @DisplayName("Полная инверсия: все границы разные")
            void completeInversion() {
                TsRange bigger = TsRange.of("2026-01-05", "2026-01-10", "[]");
                TsRange smaller = TsRange.of("2026-01-01", "2026-01-03", "()");

                assertTrue(bigger.greaterThanOrEqual(smaller));
                assertFalse(smaller.greaterThanOrEqual(bigger));
            }
        }

        @Test
        @DisplayName("Пустой <= непустой: true")
        void emptyLessThanOrEqualNonEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertTrue(empty.lessThanOrEqual(nonEmpty));
        }

        @Test
        @DisplayName("Непустой <= пустой: false")
        void nonEmptyNotLessThanOrEqualEmpty() {
            TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertFalse(nonEmpty.lessThanOrEqual(empty));
        }

        @Test
        @DisplayName("Два пустых диапазона <= друг друга: true (они равны)")
        void emptyLessThanOrEqualEmpty() {
            TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange empty2 = TsRange.of("2026-12-31", "2026-12-31", "()");

            assertTrue(empty1.lessThanOrEqual(empty2));
            assertTrue(empty2.lessThanOrEqual(empty1));
        }
    }

    @Nested
    @DisplayName("Идентичные диапазоны")
    class LEIdenticalRangesTests {

        @Test
        @DisplayName("Диапазон <= сам себя: true")
        void rangeLessThanOrEqualSelf() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertTrue(range.lessThanOrEqual(range));
        }

        @Test
        @DisplayName("Два одинаковых диапазона <= друг друга: true")
        void twoIdenticalRangesLessThanOrEqual() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertTrue(r1.lessThanOrEqual(r2));
            assertTrue(r2.lessThanOrEqual(r1));
        }

        @Test
        @DisplayName("Одинаковые значения с одинаковой включительностью: true")
        void sameValuesSameInclusivity() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[]");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-05", "[]");

            assertTrue(r1.lessThanOrEqual(r2));
        }
    }

    @Nested
    @DisplayName("Строго меньше (lessThan = true)")
    class StrictlyLessTests {

        @Test
        @DisplayName("Меньшая нижняя граница: true")
        void smallerLowerBound() {
            TsRange earlier = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange later = TsRange.of("2026-01-03", "2026-01-10", "[)");

            assertTrue(earlier.lessThanOrEqual(later));
        }

        @Test
        @DisplayName("Одинаковые нижние, меньшая верхняя: true")
        void sameLowerSmallerUpper() {
            TsRange shorter = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange longer = TsRange.of("2026-01-01", "2026-01-10", "[)");

            assertTrue(shorter.lessThanOrEqual(longer));
        }

        @Test
        @DisplayName("Включающая нижняя <= исключающей нижней при равных точках: true")
        void inclusiveLowerLessThanOrEqualExclusive() {
            TsRange inclusive = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange exclusive = TsRange.of("2026-01-01", "2026-01-05", "()");

            assertTrue(inclusive.lessThanOrEqual(exclusive));
        }

        @Test
        @DisplayName("Исключающая верхняя <= включающей верхней при равных точках: true")
        void exclusiveUpperLessThanOrEqualInclusive() {
            TsRange exclusive = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange inclusive = TsRange.of("2026-01-01", "2026-01-05", "[]");

            assertTrue(exclusive.lessThanOrEqual(inclusive));
        }
    }

    @Nested
    @DisplayName("Строго больше (lessThan = false, equal = false)")
    class StrictlyGreaterTests {

        @Test
        @DisplayName("Большая нижняя граница: false")
        void largerLowerBound() {
            TsRange later = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange earlier = TsRange.of("2026-01-01", "2026-01-03", "[)");

            assertFalse(later.lessThanOrEqual(earlier));
        }

        @Test
        @DisplayName("Одинаковые нижние, большая верхняя: false")
        void sameLowerLargerUpper() {
            TsRange longer = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange shorter = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertFalse(longer.lessThanOrEqual(shorter));
        }

        @Test
        @DisplayName("Исключающая нижняя <= включающей нижней при равных точках: false")
        void exclusiveLowerNotLessThanOrEqualInclusive() {
            TsRange exclusive = TsRange.of("2026-01-01", "2026-01-05", "()");
            TsRange inclusive = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertFalse(exclusive.lessThanOrEqual(inclusive));
        }

        @Test
        @DisplayName("Включающая верхняя <= исключающей верхней при равных точках: false")
        void inclusiveUpperNotLessThanOrEqualExclusive() {
            TsRange inclusive = TsRange.of("2026-01-01", "2026-01-05", "[]");
            TsRange exclusive = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertFalse(inclusive.lessThanOrEqual(exclusive));
        }
    }

    @Nested
    @DisplayName("Связь с lessThan и equal")
    class RelationshipTests {

        @Test
        @DisplayName("lessThanOrEqual = lessThan OR equal")
        void lessThanOrEqualIsLessThanOrEqual() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange r2 = TsRange.of("2026-01-03", "2026-01-10", "[)");
            TsRange r3 = TsRange.of("2026-01-01", "2026-01-05", "[)");

            // r1 < r2, r1 != r2
            assertTrue(r1.lessThan(r2));
            assertFalse(r1.isEqual(r2));
            assertTrue(r1.lessThanOrEqual(r2));

            // r1 == r3
            assertFalse(r1.lessThan(r3));
            assertTrue(r1.isEqual(r3));
            assertTrue(r1.lessThanOrEqual(r3));
        }

        @Test
        @DisplayName("lessThanOrEqual = NOT greaterThan")
        void lessThanOrEqualIsNotGreaterThan() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange r2 = TsRange.of("2026-01-03", "2026-01-10", "[)");

            assertTrue(r1.lessThanOrEqual(r2));
            assertFalse(r1.greaterThan(r2));

            assertFalse(r2.lessThanOrEqual(r1));
            assertTrue(r2.greaterThan(r1));
        }
    }

    @Nested
    @DisplayName("Обработка null")
    class LeNullHandlingTests {

        @Test
        @DisplayName("lessThanOrEqual(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.lessThanOrEqual(null));
        }
    }

    @Nested
    @DisplayName("Коммутативность и транзитивность")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Если a <= b и b <= a, то a == b")
        void antisymmetry() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertTrue(r1.lessThanOrEqual(r2));
            assertTrue(r2.lessThanOrEqual(r1));
            assertTrue(r1.isEqual(r2));
        }

        @Test
        @DisplayName("Если a <= b и b <= c, то a <= c (транзитивность)")
        void transitivity() {
            TsRange a = TsRange.of("2026-01-01", "2026-01-03", "[)");
            TsRange b = TsRange.of("2026-01-02", "2026-01-05", "[)");
            TsRange c = TsRange.of("2026-01-04", "2026-01-10", "[)");

            assertTrue(a.lessThanOrEqual(b));
            assertTrue(b.lessThanOrEqual(c));
            assertTrue(a.lessThanOrEqual(c));
        }

        @Test
        @DisplayName("Для любых a и b: либо a <= b, либо b <= a (полнота)")
        void totality() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange r2 = TsRange.of("2026-01-03", "2026-01-10", "[)");

            assertTrue(r1.lessThanOrEqual(r2) || r2.lessThanOrEqual(r1));
        }
    }

    @Nested
    @DisplayName("Пустые диапазоны")
    class EmptyRangeTests {

        @Test
        @DisplayName("Пустой НЕ больше непустого")
        void emptyNotGreaterThanNonEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertFalse(empty.greaterThan(nonEmpty));
        }

        @Test
        @DisplayName("Непустой больше пустого")
        void nonEmptyGreaterThanEmpty() {
            TsRange nonEmpty = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");

            assertTrue(nonEmpty.greaterThan(empty));
        }

        @Test
        @DisplayName("Два пустых диапазона не больше друг друга")
        void emptyNotGreaterThanEmpty() {
            TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange empty2 = TsRange.of("2026-12-31", "2026-12-31", "()");

            assertFalse(empty1.greaterThan(empty2));
            assertFalse(empty2.greaterThan(empty1));
        }
    }

    @Nested
    @DisplayName("Идентичные диапазоны")
    class IdenticalRangesTests {

        @Test
        @DisplayName("Идентичный диапазон не больше самого себя")
        void identicalRangeNotGreaterThanSelf() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertFalse(range.greaterThan(range));
        }

        @Test
        @DisplayName("Два одинаковых диапазона не больше друг друга")
        void twoIdenticalRangesNotGreaterThan() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertFalse(r1.greaterThan(r2));
            assertFalse(r2.greaterThan(r1));
        }
    }

    @Nested
    @DisplayName("Разные нижние границы")
    class DifferentLowerBoundsTests {

        @Test
        @DisplayName("Большая нижняя граница = больший диапазон")
        void largerLowerBoundMeansGreater() {
            TsRange later = TsRange.of("2026-01-05", "2026-01-10", "[)");
            TsRange earlier = TsRange.of("2026-01-01", "2026-01-03", "[)");

            assertTrue(later.greaterThan(earlier));
            assertFalse(earlier.greaterThan(later));
        }

        @Test
        @DisplayName("Включающая нижняя граница больше исключающей при равных точках")
        void inclusiveLowerGreaterThanExclusive() {
            // [2026-01-01, ...) > (2026-01-01, ...)
            TsRange inclusive = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange exclusive = TsRange.of("2026-01-01", "2026-01-05", "()");

            assertFalse(inclusive.greaterThan(exclusive));
            assertTrue(exclusive.greaterThan(inclusive));
        }
    }

    @Nested
    @DisplayName("Одинаковые нижние, разные верхние")
    class SameLowerDifferentUpperTests {

        @Test
        @DisplayName("Большая верхняя граница = больший диапазон")
        void largerUpperBoundMeansGreater() {
            TsRange longer = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange shorter = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertTrue(longer.greaterThan(shorter));
            assertFalse(shorter.greaterThan(longer));
        }

        @Test
        @DisplayName("Включающая верхняя граница больше исключающей при равных точках")
        void inclusiveUpperGreaterThanExclusive() {
            // [..., 2026-01-05] > [..., 2026-01-05)
            TsRange inclusive = TsRange.of("2026-01-01", "2026-01-05", "[]");
            TsRange exclusive = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertTrue(inclusive.greaterThan(exclusive));
            assertFalse(exclusive.greaterThan(inclusive));
        }
    }

    @Nested
    @DisplayName("Симметрия с lessThan")
    class SymmetryTests {

        @Test
        @DisplayName("a.greaterThan(b) <=> b.lessThan(a)")
        void symmetryWithLessThan() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange r2 = TsRange.of("2026-01-03", "2026-01-10", "[)");

            // r1 < r2, значит r2 > r1
            assertTrue(r1.lessThan(r2));
            assertTrue(r2.greaterThan(r1));

            // Обратное неверно
            assertFalse(r2.lessThan(r1));
            assertFalse(r1.greaterThan(r2));
        }

        @Test
        @DisplayName("Для равных диапазонов оба метода возвращают false")
        void equalRangesBothFalse() {
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-05", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertFalse(r1.lessThan(r2));
            assertFalse(r2.lessThan(r1));
            assertFalse(r1.greaterThan(r2));
            assertFalse(r2.greaterThan(r1));
        }
    }

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("greaterThan(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.greaterThan(null));
        }
    }

    @Nested
    @DisplayName("Комбинация разных включительностей")
    class MixedInclusiveTests {

        @Test
        @DisplayName("Разные нижние границы с разной включительностью")
        void differentLowerWithDifferentInclusivity() {
            // (2026-01-05, ...) vs [2026-01-01, ...)
            // Нижняя 01-05 > 01-01, значит первый больше
            TsRange r1 = TsRange.of("2026-01-05", "2026-01-10", "()");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-03", "[)");

            assertTrue(r1.greaterThan(r2));
        }

        @Test
        @DisplayName("Разные верхние границы с разной включительностью")
        void differentUpperWithDifferentInclusivity() {
            // [..., 2026-01-10) vs [..., 2026-01-05]
            // Верхняя 01-10 > 01-05, значит первый больше
            TsRange r1 = TsRange.of("2026-01-01", "2026-01-10", "[)");
            TsRange r2 = TsRange.of("2026-01-01", "2026-01-05", "[]");

            assertTrue(r1.greaterThan(r2));
        }
    }

    @Nested
    @DisplayName("Баг 1: Смешивание включительности при разных нижних границах")
    class DifferentLowerBoundsBugTests {

        @Test
        @DisplayName("Ветка lessThan: r1.lower < r2.lower, но верхние равны")
        void bug1_lessThanBranch_upperEqual() {
            // r1: (2026-01-01, 2026-01-05) — нижняя исключающая
            // r2: [2026-01-03, 2026-01-05) — нижняя включающая
            // Верхние границы равны: 2026-01-05

            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "()"  // нижняя исключающая
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 3, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"  // нижняя включающая
            );

            TsRange merged = r1.merge(r2);

            // ОЖИДАЕМОЕ: (2026-01-01, 2026-01-05)
            // Мы берём нижнюю границу из r1 (она меньше), поэтому она должна остаться исключающей

            assertEquals(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    merged.lower(),
                    "Нижняя граница должна быть из r1"
            );

            assertFalse(
                    merged.lowerInc(),
                    "Нижняя граница должна быть ИСКЛЮЧАЮЩЕЙ, потому что мы берём её из r1, " +
                            "где она исключающая. Включительность r2 (относится к 2026-01-03) не должна влиять на 2026-01-01"
            );

            assertEquals(
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    merged.upper()
            );
        }

        @Test
        @DisplayName("Ветка greaterThan: r1.lower > r2.lower, но верхние равны")
        void bug1_greaterThanBranch_upperEqual() {
            // r1: [2026-01-03, 2026-01-05) — нижняя включающая
            // r2: (2026-01-01, 2026-01-05) — нижняя исключающая
            // Верхние границы равны: 2026-01-05

            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 3, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"  // нижняя включающая
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "()"  // нижняя исключающая
            );

            TsRange merged = r1.merge(r2);

            // ОЖИДАЕМОЕ: (2026-01-01, 2026-01-05)
            // Мы берём нижнюю границу из r2 (она меньше), поэтому она должна остаться исключающей

            assertEquals(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    merged.lower(),
                    "Нижняя граница должна быть из r2"
            );

            assertFalse(
                    merged.lowerInc(),
                    "Нижняя граница должна быть ИСКЛЮЧАЮЩЕЙ, потому что мы берём её из r2, " +
                            "где она исключающая. Включительность r1 (относится к 2026-01-03) не должна влиять на 2026-01-01"
            );
        }

        @Test
        @DisplayName("Симметричный тест: r2.merge(r1) должен давать тот же результат")
        void bug1_commutativity() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "()"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 3, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );

            TsRange merged1 = r1.merge(r2);
            TsRange merged2 = r2.merge(r1);

            assertEquals(merged1.lower(), merged2.lower());
            assertEquals(merged1.upper(), merged2.upper());
            assertEquals(merged1.lowerInc(), merged2.lowerInc(),
                    "Включительность должна быть одинаковой независимо от порядка");
            assertEquals(merged1.upperInc(), merged2.upperInc());
        }
    }

    @Nested
    @DisplayName("Корректные случаи (должны работать правильно)")
    class CorrectCasesTests {

        @Test
        @DisplayName("Одинаковые нижние границы: включительность должна объединяться через ИЛИ")
        void sameLowerBounds_shouldMergeInclusive() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "()"  // исключающая
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 7, 0, 0),
                    "[)"  // включающая
            );

            TsRange merged = r1.merge(r2);

            // ОЖИДАЕМОЕ: [2026-01-01, 2026-01-07)
            // Нижние границы равны, поэтому объединяем включительность: false || true = true

            assertEquals(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    merged.lower()
            );

            assertTrue(
                    merged.lowerInc(),
                    "Когда нижние границы равны, включительность должна объединяться через ИЛИ"
            );
        }

        @Test
        @DisplayName("Одинаковые верхние границы: включительность должна объединяться через ИЛИ")
        void sameUpperBounds_shouldMergeInclusive() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"  // исключающая верхняя
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 3, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[]"  // включающая верхняя
            );

            TsRange merged = r1.merge(r2);

            // ОЖИДАЕМОЕ: [2026-01-01, 2026-01-05]

            assertEquals(
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    merged.upper()
            );

            assertTrue(
                    merged.upperInc(),
                    "Когда верхние границы равны, включительность должна объединяться через ИЛИ"
            );
        }
    }

    @Nested
    @DisplayName("Дополнительные edge cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Обе нижние границы исключающие")
        void bothLowerBoundsExclusive() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "()"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 3, 0, 0),
                    LocalDateTime.of(2026, 1, 7, 0, 0),
                    "()"
            );

            TsRange merged = r1.merge(r2);

            assertFalse(
                    merged.lowerInc(),
                    "Когда берём нижнюю границу из r1 (меньшую), она должна остаться исключающей"
            );
        }

        @Test
        @DisplayName("Обе нижние границы включающие")
        void bothLowerBoundsInclusive() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 3, 0, 0),
                    LocalDateTime.of(2026, 1, 7, 0, 0),
                    "[)"
            );

            TsRange merged = r1.merge(r2);

            assertTrue(
                    merged.lowerInc(),
                    "Когда берём нижнюю границу из r1 (меньшую), она должна остаться включающей"
            );
        }
    }

    @Nested
    @DisplayName("isEmpty()")
    class IsEmptyTests {
        @Test
        @DisplayName("Диапазон [a,a) должен быть пустым")
        void emptyRangeWithInclusiveLower() {
            TsRange range = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    "[)"
            );
            assertTrue(range.isEmpty());
        }

        @Test
        @DisplayName("Диапазон (a,a] должен быть пустым")
        void emptyRangeWithInclusiveUpper() {
            TsRange range = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    "(]"
            );
            assertTrue(range.isEmpty());
        }

        @Test
        @DisplayName("Диапазон (a,a) должен быть пустым")
        void emptyRangeWithBothExclusive() {
            TsRange range = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    "()"
            );
            assertTrue(range.isEmpty());
        }

        @Test
        @DisplayName("Диапазон [a,a] НЕ должен быть пустым (содержит точку)")
        void nonEmptyRangeWithBothInclusive() {
            TsRange range = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    "[]"
            );
            assertFalse(range.isEmpty());
        }

        @Test
        @DisplayName("Обычный диапазон [a,b) где a < b не пустой")
        void normalRangeIsNotEmpty() {
            TsRange range = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 2, 0, 0),
                    "[)"
            );
            assertFalse(range.isEmpty());
        }
    }

    @Nested
    @DisplayName("lessThan()")
    class LessThanTests {

        @Test
        @DisplayName("Пустой диапазон всегда меньше непустого")
        void emptyLessThanNonEmpty() {
            TsRange empty = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    "()"
            );
            TsRange nonEmpty = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 2, 0, 0),
                    "[)"
            );
            assertTrue(empty.lessThan(nonEmpty));
            assertFalse(nonEmpty.lessThan(empty));
        }

        @Test
        @DisplayName("Два пустых диапазона не меньше друг друга")
        void emptyNotLessThanEmpty() {
            TsRange empty1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    "()"
            );
            TsRange empty2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "()"
            );
            assertFalse(empty1.lessThan(empty2));
            assertFalse(empty2.lessThan(empty1));
        }

        @Test
        @DisplayName("Идентичные диапазоны не меньше друг друга")
        void identicalRangesNotLessThan() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            assertFalse(r1.lessThan(r2));
            assertFalse(r2.lessThan(r1));
        }

        @Test
        @DisplayName("Разные нижние границы: меньшая нижняя граница = меньший диапазон")
        void differentLowerBounds() {
            TsRange earlier = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "[)"
            );
            TsRange later = TsRange.of(
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    LocalDateTime.of(2026, 1, 8, 0, 0),
                    "[)"
            );
            assertTrue(earlier.lessThan(later));
            assertFalse(later.lessThan(earlier));
        }

        @Test
        @DisplayName("Одинаковые нижние, разные верхние: меньшая верхняя = меньший диапазон")
        void sameLowerDifferentUpper() {
            TsRange shorter = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange longer = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "[)"
            );
            assertTrue(shorter.lessThan(longer));
            assertFalse(longer.lessThan(shorter));
        }

        @Test
        @DisplayName("Одинаковые значения, включающая нижняя < исключающей нижней")
        void inclusiveLowerLessThanExclusive() {
            TsRange inclusive = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange exclusive = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "()"
            );
            assertTrue(inclusive.lessThan(exclusive));
            assertFalse(exclusive.lessThan(inclusive));
        }

        @Test
        @DisplayName("Одинаковые значения, исключающая верхняя < включающей верхней")
        void exclusiveUpperLessThanInclusive() {
            TsRange exclusive = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange inclusive = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[]"
            );
            assertTrue(exclusive.lessThan(inclusive));
            assertFalse(inclusive.lessThan(exclusive));
        }

        @Test
        @DisplayName("Большие значения compareTo (> 1 и < -1) обрабатываются корректно")
        void largeCompareToValues() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 10, 0, 0)
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    LocalDateTime.of(2026, 1, 10, 0, 0)
            );
            // Разница в 4 дня, compareTo может вернуть значение != -1
            assertTrue(r1.lessThan(r2));
        }
    }

    @Nested
    @DisplayName("merge()")
    class MergeTests {

        @Test
        @DisplayName("merge с пустым диапазоном возвращает другой диапазон")
        void mergeWithEmpty() {
            TsRange empty = TsRange.of(
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "()"
            );
            TsRange range = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 3, 0, 0),
                    "[)"
            );

            TsRange result1 = range.merge(empty);
            TsRange result2 = empty.merge(range);

            assertTrue(range.isEqual(result1));
            assertTrue(range.isEqual(result2));
        }

        @Test
        @DisplayName("merge с самим собой возвращает идентичный диапазон")
        void mergeWithSelf() {
            TsRange range = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange result = range.merge(range);
            assertTrue(range.isEqual(result));
        }

        @Test
        @DisplayName("Пересекающиеся диапазоны объединяются")
        void overlappingRanges() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 3, 0, 0),
                    LocalDateTime.of(2026, 1, 8, 0, 0),
                    "[)"
            );

            TsRange result = r1.merge(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 8, 0, 0), result.upper());
            assertTrue(result.lowerInc());
            assertFalse(result.upperInc());
        }

        @Test
        @DisplayName("Непересекающиеся диапазоны заполняют разрыв (как в PostgreSQL)")
        void nonOverlappingRanges() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 3, 0, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    LocalDateTime.of(2026, 1, 15, 0, 0),
                    "[)"
            );

            TsRange result = r1.merge(r2);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 15, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Один диапазон полностью внутри другого")
        void oneRangeInsideAnother() {
            TsRange outer = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "[)"
            );
            TsRange inner = TsRange.of(
                    LocalDateTime.of(2026, 1, 3, 0, 0),
                    LocalDateTime.of(2026, 1, 7, 0, 0),
                    "[)"
            );

            TsRange result1 = outer.merge(inner);
            TsRange result2 = inner.merge(outer);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result1.lower());
            assertEquals(LocalDateTime.of(2026, 1, 10, 0, 0), result1.upper());
            assertTrue(result1.isEqual(result2));
        }

        @Test
        @DisplayName("Одинаковые нижние границы с разной включительностью: результирующая включающая")
        void sameLowerDifferentInclusive() {
            TsRange inclusive = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange exclusive = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 7, 0, 0),
                    "()"
            );

            TsRange result = inclusive.merge(exclusive);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertTrue(result.lowerInc(), "Нижняя граница должна быть включающей");
            assertEquals(LocalDateTime.of(2026, 1, 7, 0, 0), result.upper());
        }

        @Test
        @DisplayName("Одинаковые верхние границы с разной включительностью: результирующая включающая")
        void sameUpperDifferentInclusive() {
            TsRange exclusive = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange inclusive = TsRange.of(
                    LocalDateTime.of(2026, 1, 3, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[]"
            );

            TsRange result = exclusive.merge(inclusive);

            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.lower());
            assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), result.upper());
            assertTrue(result.upperInc(), "Верхняя граница должна быть включающей");
        }

        @Test
        @DisplayName("Merge с бесконечными границами")
        void mergeWithInfinity() {
            TsRange finite = TsRange.of(
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    LocalDateTime.of(2026, 1, 10, 0, 0),
                    "[)"
            );
            TsRange infinite = new TsRange(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    false,
                    false
            );

            TsRange result = finite.merge(infinite);

            assertEquals(TsRange.MINUS_INFINITY, result.lower());
            assertEquals(TsRange.INFINITY, result.upper());
        }
    }

    @Nested
    @DisplayName("equal()")
    class EqualTests {

        @Test
        @DisplayName("Идентичные диапазоны равны")
        void identicalRangesEqual() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            assertTrue(r1.isEqual(r2));
            assertTrue(r2.isEqual(r1));
        }

        @Test
        @DisplayName("Разные нижние границы не равны")
        void differentLowerNotEqual() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 2, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            assertFalse(r1.isEqual(r2));
        }

        @Test
        @DisplayName("Разные верхние границы не равны")
        void differentUpperNotEqual() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 6, 0, 0),
                    "[)"
            );
            assertFalse(r1.isEqual(r2));
        }

        @Test
        @DisplayName("Одинаковые значения, разная включительность нижней границы не равны")
        void differentLowerInclusiveNotEqual() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "()"
            );
            assertFalse(r1.isEqual(r2));
        }

        @Test
        @DisplayName("Одинаковые значения, разная включительность верхней границы не равны")
        void differentUpperInclusiveNotEqual() {
            TsRange r1 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            TsRange r2 = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[]"
            );
            assertFalse(r1.isEqual(r2));
        }
    }

    @Nested
    @DisplayName("Factory methods of()")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Парсинг даты без времени добавляет T00:00:00")
        void parseDateWithoutTime() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-05");
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), range.lower());
            assertEquals(LocalDateTime.of(2026, 1, 5, 0, 0), range.upper());
        }

        @Test
        @DisplayName("Парсинг даты с временем работает корректно")
        void parseDateWithTime() {
            TsRange range = TsRange.of("2026-01-01T10:30:00", "2026-01-05T15:45:00");
            assertEquals(LocalDateTime.of(2026, 1, 1, 10, 30), range.lower());
            assertEquals(LocalDateTime.of(2026, 1, 5, 15, 45), range.upper());
        }

        @Test
        @DisplayName("Исключение при lower > upper")
        void lowerAfterUpperThrowsException() {
            assertThrows(UnsupportedOperationException.class, () ->
                    TsRange.of("2026-01-05", "2026-01-01")
            );
        }

        @Test
        @DisplayName("Исключение при null lower")
        void nullLowerThrowsException() {
            assertThrows(UnsupportedOperationException.class, () ->
                    TsRange.of(null, LocalDateTime.of(2026, 1, 5, 0, 0), "[)")
            );
        }

        @Test
        @DisplayName("Исключение при null upper")
        void nullUpperThrowsException() {
            assertThrows(UnsupportedOperationException.class, () ->
                    TsRange.of(LocalDateTime.of(2026, 1, 1, 0, 0), null, "[)")
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"(", ")", "[", "]", "", "[[]", "(])"})
        @DisplayName("Исключение при неверных bounds")
        void invalidBoundsThrowsException(String bounds) {
            assertThrows(IllegalArgumentException.class, () ->
                    TsRange.of(
                            LocalDateTime.of(2026, 1, 1, 0, 0),
                            LocalDateTime.of(2026, 1, 5, 0, 0),
                            bounds
                    )
            );
        }
    }

    @Nested
    @DisplayName("lowerInf() и upperInf()")
    class InfinityTests {

        @Test
        @DisplayName("lowerInf возвращает true для MINUS_INFINITY")
        void lowerInfWithMinusInfinity() {
            TsRange range = new TsRange(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    false,
                    false
            );
            assertTrue(range.lowerInf());
            assertFalse(range.upperInf());
        }

        @Test
        @DisplayName("upperInf возвращает true для INFINITY")
        void upperInfWithInfinity() {
            TsRange range = new TsRange(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    false,
                    false
            );
            assertFalse(range.lowerInf());
            assertTrue(range.upperInf());
        }

        @Test
        @DisplayName("Обе бесконечности")
        void bothInfinities() {
            TsRange range = new TsRange(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    false,
                    false
            );
            assertTrue(range.lowerInf());
            assertTrue(range.upperInf());
        }

        @Test
        @DisplayName("Обычный диапазон не бесконечный")
        void normalRangeNotInfinite() {
            TsRange range = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    LocalDateTime.of(2026, 1, 5, 0, 0),
                    "[)"
            );
            assertFalse(range.lowerInf());
            assertFalse(range.upperInf());
        }
    }

    @Nested
    @DisplayName("equal() - специфика пустых диапазонов")
    class EqualEmptyRangesTests {

        @Test
        @DisplayName("Два пустых диапазона с РАЗНЫМИ датами должны быть равны (как в PostgreSQL)")
        void differentEmptyRangesAreEqual() {
            TsRange empty1 = TsRange.of("2026-01-01", "2026-01-01", "[)");
            TsRange empty2 = TsRange.of("2026-12-31", "2026-12-31", "()");

            assertTrue(empty1.isEqual(empty2));
            assertTrue(empty2.isEqual(empty1));
        }

        @Test
        @DisplayName("Пустой диапазон НЕ равен непустому, даже если даты совпадают")
        void emptyNotEqualToNonEmpty() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            // Создадим диапазон из одной точки (включающий обе границы)
            TsRange singlePoint = TsRange.of("2026-01-01", "2026-01-01", "[]");

            assertFalse(empty.isEqual(singlePoint));
            assertFalse(singlePoint.isEqual(empty));
        }

        @Test
        @DisplayName("Сравнение с null возвращает false, а не кидает NPE")
        void equalToNullThrowIAE() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-05", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.isEqual(null));
        }
    }
}
