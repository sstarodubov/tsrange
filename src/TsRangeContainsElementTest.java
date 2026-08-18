package ru.nspk.pcl.common.tsrange;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TsRangeContainsElementTest {

    @Nested
    @DisplayName("Элемент внутри диапазона")
    class ElementInsideTests {

        @Test
        @DisplayName("Элемент строго внутри")
        void elementStrictlyInside() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-31", "[)");
            LocalDateTime element = LocalDateTime.of(2026, 1, 15, 0, 0);

            assertTrue(range.containsElement(element));
        }

        @Test
        @DisplayName("Элемент ближе к нижней границе")
        void elementNearLowerBound() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-31", "[)");
            LocalDateTime element = LocalDateTime.of(2026, 1, 2, 0, 0);

            assertTrue(range.containsElement(element));
        }

        @Test
        @DisplayName("Элемент ближе к верхней границе")
        void elementNearUpperBound() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-31", "[)");
            LocalDateTime element = LocalDateTime.of(2026, 1, 30, 0, 0);

            assertTrue(range.containsElement(element));
        }
    }

    @Nested
    @DisplayName("Элемент вне диапазона")
    class ElementOutsideTests {

        @Test
        @DisplayName("Элемент до диапазона")
        void elementBeforeRange() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-31", "[)");
            LocalDateTime element = LocalDateTime.of(2025, 12, 15, 0, 0);

            assertFalse(range.containsElement(element));
        }

        @Test
        @DisplayName("Элемент после диапазона")
        void elementAfterRange() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-31", "[)");
            LocalDateTime element = LocalDateTime.of(2026, 2, 15, 0, 0);

            assertFalse(range.containsElement(element));
        }
    }

    @Nested
    @DisplayName("Элемент на нижней границе")
    class ElementOnLowerBoundTests {

        @Test
        @DisplayName("Нижняя граница включающая — true")
        void inclusiveLowerBound() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-31", "[)");
            LocalDateTime element = LocalDateTime.of(2026, 1, 1, 0, 0);

            assertTrue(range.containsElement(element));
        }

        @Test
        @DisplayName("Нижняя граница исключающая — false")
        void exclusiveLowerBound() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-31", "()");
            LocalDateTime element = LocalDateTime.of(2026, 1, 1, 0, 0);

            assertFalse(range.containsElement(element));
        }
    }

    @Nested
    @DisplayName("Элемент на верхней границе")
    class ElementOnUpperBoundTests {

        @Test
        @DisplayName("Верхняя граница включающая — true")
        void inclusiveUpperBound() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-31", "[]");
            LocalDateTime element = LocalDateTime.of(2026, 1, 31, 0, 0);

            assertTrue(range.containsElement(element));
        }

        @Test
        @DisplayName("Верхняя граница исключающая — false")
        void exclusiveUpperBound() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-31", "[)");
            LocalDateTime element = LocalDateTime.of(2026, 1, 31, 0, 0);

            assertFalse(range.containsElement(element));
        }
    }

    @Nested
    @DisplayName("Пустой диапазон")
    class EmptyRangeTests {

        @Test
        @DisplayName("Пустой диапазон не содержит никакого элемента")
        void emptyRangeContainsNothing() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            LocalDateTime element = LocalDateTime.of(2026, 1, 1, 0, 0);

            assertFalse(empty.containsElement(element));
        }

        @Test
        @DisplayName("Пустой диапазон не содержит элемент внутри его границ")
        void emptyRangeContainsNothingInside() {
            TsRange empty = TsRange.of("2026-01-01", "2026-01-01", "[]");
            LocalDateTime element = LocalDateTime.of(2026, 1, 1, 0, 0);

            // Пустой диапазон не содержит ничего, даже если формально границы совпадают
            // Но диапазон [] с равными границами НЕ пустой (isEmpty() вернет false)
            // Поэтому нужно использовать действительно пустой диапазон
            TsRange realEmpty = TsRange.of("2026-01-01", "2026-01-01", "[)");
            assertFalse(realEmpty.containsElement(element));
        }
    }

    @Nested
    @DisplayName("Бесконечные границы")
    class InfiniteBoundaryTests {

        @Test
        @DisplayName("Бесконечная верхняя граница содержит любой элемент после нижней")
        void infiniteUpperContainsElementAfterLower() {
            TsRange range = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );
            LocalDateTime element = LocalDateTime.of(2030, 12, 31, 0, 0);

            assertTrue(range.containsElement(element));
        }

        @Test
        @DisplayName("Бесконечная верхняя граница не содержит элемент до нижней")
        void infiniteUpperNotContainsElementBeforeLower() {
            TsRange range = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 0, 0),
                    TsRange.INFINITY,
                    "[)"
            );
            LocalDateTime element = LocalDateTime.of(2025, 6, 15, 0, 0);

            assertFalse(range.containsElement(element));
        }

        @Test
        @DisplayName("Бесконечная нижняя граница содержит любой элемент до верхней")
        void infiniteLowerContainsElementBeforeUpper() {
            TsRange range = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 12, 31, 0, 0),
                    "()"
            );
            LocalDateTime element = LocalDateTime.of(2020, 1, 1, 0, 0);

            assertTrue(range.containsElement(element));
        }

        @Test
        @DisplayName("Бесконечная нижняя граница не содержит элемент после верхней")
        void infiniteLowerNotContainsElementAfterUpper() {
            TsRange range = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    LocalDateTime.of(2026, 12, 31, 0, 0),
                    "()"
            );
            LocalDateTime element = LocalDateTime.of(2027, 6, 15, 0, 0);

            assertFalse(range.containsElement(element));
        }

        @Test
        @DisplayName("Полностью бесконечный диапазон содержит любой элемент")
        void fullyInfiniteContainsAnyElement() {
            TsRange range = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY,
                    "()"
            );
            LocalDateTime element = LocalDateTime.of(2026, 6, 15, 12, 30);

            assertTrue(range.containsElement(element));
        }
    }

    @Nested
    @DisplayName("Обработка null")
    class NullHandlingTests {

        @Test
        @DisplayName("containsElement(null) бросает IllegalArgumentException")
        void nullThrowsException() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-31", "[)");

            assertThrows(IllegalArgumentException.class, () -> range.containsElement(null));
        }
    }

    @Nested
    @DisplayName("Диапазон из одной точки")
    class SinglePointRangeTests {

        @Test
        @DisplayName("Диапазон из одной точки содержит эту точку")
        void singlePointContainsItself() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-01", "[]");
            LocalDateTime element = LocalDateTime.of(2026, 1, 1, 0, 0);

            assertTrue(range.containsElement(element));
        }

        @Test
        @DisplayName("Диапазон из одной точки не содержит другую точку")
        void singlePointNotContainsOther() {
            TsRange range = TsRange.of("2026-01-01", "2026-01-01", "[]");
            LocalDateTime element = LocalDateTime.of(2026, 1, 2, 0, 0);

            assertFalse(range.containsElement(element));
        }
    }

    @Nested
    @DisplayName("Точное время (не только даты)")
    class ExactTimeTests {

        @Test
        @DisplayName("Элемент с точным временем внутри диапазона")
        void elementWithExactTime() {
            TsRange range = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 18, 0),
                    "[)"
            );
            LocalDateTime element = LocalDateTime.of(2026, 1, 1, 12, 30);

            assertTrue(range.containsElement(element));
        }

        @Test
        @DisplayName("Элемент на границе с точным временем")
        void elementOnExactTimeBoundary() {
            TsRange range = TsRange.of(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 18, 0),
                    "[)"
            );
            LocalDateTime atStart = LocalDateTime.of(2026, 1, 1, 9, 0);
            LocalDateTime atEnd = LocalDateTime.of(2026, 1, 1, 18, 0);

            assertTrue(range.containsElement(atStart));   // [ включая
            assertFalse(range.containsElement(atEnd));    // ) исключая
        }
    }
}
