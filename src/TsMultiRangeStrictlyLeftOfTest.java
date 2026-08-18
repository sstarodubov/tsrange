package ru.nspk.pcl.common.tsrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("strictlyLeftOf() тесты для TsMultiRange (аналог PostgreSQL <<)")
class TsMultiRangeStrictlyLeftOfTest {

    @Nested
    @DisplayName("Полностью разнесённые multirange")
    class FullyApartTests {

        @Test
        @DisplayName("Одиночные диапазоны, первый левее — true")
        void singleRangesFirstLeft() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertTrue(mr1.strictlyLeftOf(mr2));
            assertFalse(mr2.strictlyLeftOf(mr1));
        }

        @Test
        @DisplayName("Множественные диапазоны, все левее — true")
        void multipleRangesAllLeft() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-12", "2026-01-15", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)"),
                    TsRange.of("2026-02-01", "2026-02-10", "[)")
            ));

            assertTrue(mr1.strictlyLeftOf(mr2));
        }

        @Test
        @DisplayName("Диапазоны далеко друг от друга — true")
        void rangesFarApart() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-06-01", "2026-06-10", "[)")
            ));

            assertTrue(mr1.strictlyLeftOf(mr2));
        }
    }

    @Nested
    @DisplayName("Пересечение")
    class OverlapTests {

        @Test
        @DisplayName("Частичное пересечение — false")
        void partialOverlap() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)"),
                    TsRange.of("2026-01-15", "2026-01-25", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertFalse(mr1.strictlyLeftOf(mr2));
            assertFalse(mr2.strictlyLeftOf(mr1));
        }

        @Test
        @DisplayName("Полное вложение — false")
        void fullContainment() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-31", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertFalse(mr1.strictlyLeftOf(mr2));
            assertFalse(mr2.strictlyLeftOf(mr1));
        }

        @Test
        @DisplayName("Идентичные multirange — false")
        void identicalMultiranges() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertFalse(mr1.strictlyLeftOf(mr2));
            assertFalse(mr2.strictlyLeftOf(mr1));
        }
    }

    @Nested
    @DisplayName("Касание границ")
    class BoundaryTouchingTests {

        @Test
        @DisplayName("Касание: ) и [ — true")
        void touchExclusiveInclusive() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertTrue(mr1.strictlyLeftOf(mr2));
            assertFalse(mr2.strictlyLeftOf(mr1));
        }

        @Test
        @DisplayName("Касание: ] и [ — false (общая точка)")
        void touchBothInclusive() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[]")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertFalse(mr1.strictlyLeftOf(mr2));
        }

        @Test
        @DisplayName("Касание: ] и ( — true")
        void touchInclusiveExclusive() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[]")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "()")
            ));

            assertTrue(mr1.strictlyLeftOf(mr2));
        }

        @Test
        @DisplayName("Касание: ) и ( — true (дырка)")
        void touchBothExclusive() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "()")
            ));

            assertTrue(mr1.strictlyLeftOf(mr2));
        }

        @ParameterizedTest
        @CsvSource({
                // bounds1 (upper mr1), bounds2 (lower mr2), ожидается strictlyLeftOf
                "'[)', '[)', true",   // ) и [ — нет общей точки
                "'[)', '()', true",   // ) и ( — дырка
                "'[]', '[)', false",  // ] и [ — общая точка
                "'[]', '()', true",   // ] и ( — нет общей точки
                "'()', '[)', true",   // ) и [ — нет общей точки
                "'()', '()', true",   // ) и ( — дырка
                "'(]', '[)', false",  // ] и [ — общая точка
                "'(]', '()', true",   // ] и ( — нет общей точки
        })
        @DisplayName("Параметризованный тест касания границ")
        void boundaryTouchingParameterized(String bounds1, String bounds2, boolean expected) {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", bounds1)
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", bounds2)
            ));

            assertEquals(expected, mr1.strictlyLeftOf(mr2),
                    "Для bounds1=" + bounds1 + ", bounds2=" + bounds2);
        }
    }

    @Nested
    @DisplayName("Пустые multirange")
    class EmptyMultiRangeTests {

        @Test
        @DisplayName("Пустой multirange строго левее непустого — false")
        void emptyStrictlyLeftOfNonEmpty() {
            TsMultiRange empty = TsMultiRange.of(List.of());
            TsMultiRange nonEmpty = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertFalse(empty.strictlyLeftOf(nonEmpty));
        }

        @Test
        @DisplayName("Непустой multirange строго левее пустого — true")
        void nonEmptyStrictlyLeftOfEmpty() {
            TsMultiRange nonEmpty = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));
            TsMultiRange empty = TsMultiRange.of(List.of());

            assertFalse(nonEmpty.strictlyLeftOf(empty));
        }

        @Test
        @DisplayName("Пустой multirange строго левее пустого — false")
        void emptyStrictlyLeftOfEmpty() {
            TsMultiRange empty1 = TsMultiRange.of(List.of());
            TsMultiRange empty2 = TsMultiRange.of(List.of());

            assertFalse(empty1.strictlyLeftOf(empty2));
        }
    }

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Бесконечная верхняя: НЕ строго левее — false")
        void infiniteUpperNotStrictlyLeft() {
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

            assertFalse(mr1.strictlyLeftOf(mr2));
        }

        @Test
        @DisplayName("Бесконечная нижняя у второго: НЕ строго левее — false")
        void infiniteLowerOfSecondNotStrictlyLeft() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(
                            TsRange.MINUS_INFINITY,
                            LocalDateTime.of(2026, 1, 30, 0, 0),
                            "()"
                    )
            ));

            assertFalse(mr1.strictlyLeftOf(mr2));
        }

        @Test
        @DisplayName("Бесконечный влево строго левее бесконечного вправо — true")
        void infiniteLeftStrictlyLeftOfInfiniteRight() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(
                            TsRange.MINUS_INFINITY,
                            LocalDateTime.of(2026, 1, 10, 0, 0),
                            "()"
                    )
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(
                            LocalDateTime.of(2026, 1, 20, 0, 0),
                            TsRange.INFINITY,
                            "[)"
                    )
            ));

            assertTrue(mr1.strictlyLeftOf(mr2));
            assertFalse(mr2.strictlyLeftOf(mr1));
        }

        @Test
        @DisplayName("Полностью бесконечный НЕ строго левее — false")
        void fullyInfiniteNotStrictlyLeft() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(
                            TsRange.MINUS_INFINITY,
                            TsRange.INFINITY,
                            "()"
                    )
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertFalse(mr1.strictlyLeftOf(mr2));
        }
    }

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("strictlyLeftOf(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertThrows(IllegalArgumentException.class, () -> mr.strictlyLeftOf(null));
        }
    }

    @Nested
    @DisplayName("Симметрия с >>")
    class SymmetryTests {

        @Test
        @DisplayName("a.strictlyLeftOf(b) <=> b.strictlyRightOf(a)")
        void symmetryWithStrictlyRightOf() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertTrue(mr1.strictlyLeftOf(mr2));
            assertTrue(mr2.strictlyRightOf(mr1));

            assertFalse(mr2.strictlyLeftOf(mr1));
            assertFalse(mr1.strictlyRightOf(mr2));
        }
    }

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("Если a.strictlyLeftOf(b), то НЕ a.overlaps(b)")
        void strictlyLeftImpliesNotOverlap() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertTrue(mr1.strictlyLeftOf(mr2));
            assertFalse(mr1.overlaps(mr2));
        }

        @Test
        @DisplayName("Если НЕ a.overlaps(b) и оба непустые, то a.strictlyLeftOf(b) ИЛИ a.strictlyRightOf(b)")
        void notOverlapImpliesStrictlyLeftOrRight() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertFalse(mr1.overlaps(mr2));
            assertTrue(mr1.strictlyLeftOf(mr2) || mr1.strictlyRightOf(mr2));
        }

        @Test
        @DisplayName("Если a.containsMultirange(b), то НЕ a.strictlyLeftOf(b)")
        void containmentImpliesNotStrictlyLeft() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-31", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-20", "[)")
            ));

            assertTrue(mr1.containsMultirange(mr2));
            assertFalse(mr1.strictlyLeftOf(mr2));
            assertFalse(mr2.strictlyLeftOf(mr1));
        }
    }

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("Антирефлексивность: НЕ a.strictlyLeftOf(a)")
        void irreflexivity() {
            TsMultiRange mr = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            assertFalse(mr.strictlyLeftOf(mr));
        }

        @Test
        @DisplayName("Асимметричность: если a.strictlyLeftOf(b), то НЕ b.strictlyLeftOf(a)")
        void asymmetry() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-10", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-30", "[)")
            ));

            assertTrue(mr1.strictlyLeftOf(mr2));
            assertFalse(mr2.strictlyLeftOf(mr1));
        }

        @Test
        @DisplayName("Транзитивность: если a.strictlyLeftOf(b) и b.strictlyLeftOf(c), то a.strictlyLeftOf(c)")
        void transitivity() {
            TsMultiRange a = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-05", "[)")
            ));

            TsMultiRange b = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-10", "2026-01-15", "[)")
            ));

            TsMultiRange c = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-20", "2026-01-25", "[)")
            ));

            assertTrue(a.strictlyLeftOf(b));
            assertTrue(b.strictlyLeftOf(c));
            assertTrue(a.strictlyLeftOf(c));
        }
    }

    @Nested
    @DisplayName("Точное время")
    class ExactTimeTests {

        @Test
        @DisplayName("Строго левее с точным временем — true")
        void strictlyLeftWithExactTime() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(
                            LocalDateTime.of(2026, 1, 1, 9, 0),
                            LocalDateTime.of(2026, 1, 1, 12, 0),
                            "[)"
                    )
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(
                            LocalDateTime.of(2026, 1, 1, 13, 0),
                            LocalDateTime.of(2026, 1, 1, 14, 0),
                            "[)"
                    )
            ));

            assertTrue(mr1.strictlyLeftOf(mr2));
        }

        @Test
        @DisplayName("Касание на точное время — true")
        void touchAtExactTime() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(
                            LocalDateTime.of(2026, 1, 1, 9, 0),
                            LocalDateTime.of(2026, 1, 1, 12, 0),
                            "[)"
                    )
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(
                            LocalDateTime.of(2026, 1, 1, 12, 0),
                            LocalDateTime.of(2026, 1, 1, 14, 0),
                            "[)"
                    )
            ));

            assertTrue(mr1.strictlyLeftOf(mr2)); // ) и [ — нет общей точки
        }
    }

    @Nested
    @DisplayName("Сложные случаи")
    class ComplexCasesTests {

        @Test
        @DisplayName("Множественные диапазоны, только последний пересекается — false")
        void onlyLastRangeOverlaps() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-05", "[)"),
                    TsRange.of("2026-01-10", "2026-01-15", "[)"),
                    TsRange.of("2026-01-20", "2026-01-25", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-22", "2026-01-30", "[)")
            ));

            assertFalse(mr1.strictlyLeftOf(mr2)); // последний диапазон пересекается
        }

        @Test
        @DisplayName("Множественные диапазоны, все строго левее — true")
        void allRangesStrictlyLeft() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of("2026-01-01", "2026-01-05", "[)"),
                    TsRange.of("2026-01-10", "2026-01-15", "[)"),
                    TsRange.of("2026-01-20", "2026-01-25", "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of("2026-02-01", "2026-02-10", "[)")
            ));

            assertTrue(mr1.strictlyLeftOf(mr2));
        }
    }
}