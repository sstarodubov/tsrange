package ru.starodubov;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AIGeneratedTest {

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

    @Disabled //todo(enable after implementation)
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

            assertTrue(range.equal(result1));
            assertTrue(range.equal(result2));
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
            assertTrue(range.equal(result));
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
            assertTrue(result1.equal(result2));
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
            assertTrue(r1.equal(r2));
            assertTrue(r2.equal(r1));
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
            assertFalse(r1.equal(r2));
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
            assertFalse(r1.equal(r2));
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
            assertFalse(r1.equal(r2));
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
            assertFalse(r1.equal(r2));
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
            assertThrows(UnsupportedOperationException.class, () ->
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
}
