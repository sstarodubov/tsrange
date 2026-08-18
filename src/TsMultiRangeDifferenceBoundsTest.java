package ru.nspk.pcl.common.tsrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("difference() тесты с разными границами")
class TsMultiRangeDifferenceBoundsTest {

    private static final LocalDateTime BASE_DATE = LocalDateTime.of(2026, 1, 1, 0, 0);

    // ==================== ВЫЧИТАНИЕ С РАЗНЫМИ ВКЛЮЧИТЕЛЬНОСТЯМИ ====================

    @Nested
    @DisplayName("Вычитание с разными включительностями вычитаемого")
    class SubtrahendInclusivityTests {

        @Test
        @DisplayName("Вычитание [a,b) — правый кусок с нижней включающей")
        void subtractExclusiveRange() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(20), "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(5), BASE_DATE.plusDays(15), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(2, result.size());

            // Левый кусок: [день 0, день 5)
            assertEquals(BASE_DATE, result.get(0).lower());
            assertEquals(BASE_DATE.plusDays(5), result.get(0).upper());
            assertTrue(result.get(0).lowerInc());
            assertFalse(result.get(0).upperInc()); // NOT mr2.lowerInc = NOT true = false

            // Правый кусок: [день 15, день 20)
            assertEquals(BASE_DATE.plusDays(15), result.get(1).lower());
            assertEquals(BASE_DATE.plusDays(20), result.get(1).upper());
            assertTrue(result.get(1).lowerInc()); // NOT mr2.upperInc = NOT false = true
            assertFalse(result.get(1).upperInc());
        }

        @Test
        @DisplayName("Вычитание (a,b) — оба куска включают точку стыка")
        void subtractFullyExclusive() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(20), "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(5), BASE_DATE.plusDays(15), "()")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(2, result.size());

            // Левый кусок: [день 0, день 5] — включает день 5
            assertEquals(BASE_DATE, result.get(0).lower());
            assertEquals(BASE_DATE.plusDays(5), result.get(0).upper());
            assertTrue(result.get(0).lowerInc());
            assertTrue(result.get(0).upperInc()); // NOT mr2.lowerInc = NOT false = true

            // Правый кусок: [день 15, день 20) — включает день 15
            assertEquals(BASE_DATE.plusDays(15), result.get(1).lower());
            assertEquals(BASE_DATE.plusDays(20), result.get(1).upper());
            assertTrue(result.get(1).lowerInc()); // NOT mr2.upperInc = NOT false = true
            assertFalse(result.get(1).upperInc());
        }

        @Test
        @DisplayName("Вычитание [a,b] — оба куска исключают точку стыка")
        void subtractFullyInclusive() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(20), "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(5), BASE_DATE.plusDays(15), "[]")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(2, result.size());

            // Левый кусок: [день 0, день 5) — не включает день 5
            assertEquals(BASE_DATE.plusDays(5), result.get(0).upper());
            assertFalse(result.get(0).upperInc()); // NOT mr2.lowerInc = NOT true = false

            // Правый кусок: (день 15, день 20) — не включает день 15
            assertEquals(BASE_DATE.plusDays(15), result.get(1).lower());
            assertFalse(result.get(1).lowerInc()); // NOT mr2.upperInc = NOT true = false
        }

        @Test
        @DisplayName("Вычитание (a,b] — левый кусок включает, правый исключает")
        void subtractExclusiveLowerInclusiveUpper() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(20), "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(5), BASE_DATE.plusDays(15), "(]")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(2, result.size());

            // Левый кусок: [день 0, день 5] — включает день 5 (NOT false = true)
            assertEquals(BASE_DATE.plusDays(5), result.get(0).upper());
            assertTrue(result.get(0).upperInc());

            // Правый кусок: (день 15, день 20) — не включает день 15 (NOT true = false)
            assertEquals(BASE_DATE.plusDays(15), result.get(1).lower());
            assertFalse(result.get(1).lowerInc());
        }
    }

    // ==================== ВЫЧИТАНИЕ ИЗ РАЗНЫХ ВКЛЮЧИТЕЛЬНОСТЕЙ ====================

    @Nested
    @DisplayName("Вычитание из разных включительностей уменьшаемого")
    class MinuendInclusivityTests {

        @Test
        @DisplayName("Вычитание из () — оба куска исключающие")
        void subtractFromFullyExclusive() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(20), "()")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(5), BASE_DATE.plusDays(15), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(2, result.size());

            // Левый кусок: (день 0, день 5)
            assertEquals(BASE_DATE, result.get(0).lower());
            assertFalse(result.get(0).lowerInc()); // из mr1
            assertEquals(BASE_DATE.plusDays(5), result.get(0).upper());
            assertFalse(result.get(0).upperInc()); // NOT mr2.lowerInc = NOT true = false

            // Правый кусок: [день 15, день 20)
            assertEquals(BASE_DATE.plusDays(15), result.get(1).lower());
            assertTrue(result.get(1).lowerInc()); // NOT mr2.upperInc = NOT false = true
            assertEquals(BASE_DATE.plusDays(20), result.get(1).upper());
            assertFalse(result.get(1).upperInc()); // из mr1
        }

        @Test
        @DisplayName("Вычитание из [] — оба куска включающие")
        void subtractFromFullyInclusive() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(20), "[]")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(5), BASE_DATE.plusDays(15), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(2, result.size());

            // Левый кусок: [день 0, день 5)
            assertEquals(BASE_DATE, result.get(0).lower());
            assertTrue(result.get(0).lowerInc()); // из mr1
            assertEquals(BASE_DATE.plusDays(5), result.get(0).upper());
            assertFalse(result.get(0).upperInc()); // NOT mr2.lowerInc

            // Правый кусок: [день 15, день 20]
            assertEquals(BASE_DATE.plusDays(15), result.get(1).lower());
            assertTrue(result.get(1).lowerInc()); // NOT mr2.upperInc
            assertEquals(BASE_DATE.plusDays(20), result.get(1).upper());
            assertTrue(result.get(1).upperInc()); // из mr1
        }

        @Test
        @DisplayName("Вычитание из (] — левый исключающий, правый включающий")
        void subtractFromExclusiveInclusive() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(20), "(]")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(5), BASE_DATE.plusDays(15), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(2, result.size());

            // Левый кусок: (день 0, день 5)
            assertFalse(result.get(0).lowerInc()); // из mr1
            assertFalse(result.get(0).upperInc()); // NOT mr2.lowerInc

            // Правый кусок: [день 15, день 20]
            assertTrue(result.get(1).lowerInc()); // NOT mr2.upperInc
            assertTrue(result.get(1).upperInc()); // из mr1
        }
    }

    // ==================== ПАРАМЕТРИЗОВАННЫЙ ТЕСТ: 16 КОМБИНАЦИЙ ====================

    @Nested
    @DisplayName("Все 16 комбинаций границ при вырезании середины")
    class AllBoundsCombinationsTests {

        @ParameterizedTest
        @CsvSource({
                // bounds1 (уменьшаемое), bounds2 (вычитаемое),
                // ожидается lowerInc левого, upperInc левого, lowerInc правого, upperInc правого
                "'[)', '[)', true,  false, true,  false",
                "'[)', '()', true,  true,  true,  false",
                "'[)', '[]', true,  false, false, false",
                "'[)', '(]', true,  true,  false, false",

                "'()', '[)', false, false, true,  false",
                "'()', '()', false, true,  true,  false",
                "'()', '[]', false, false, false, false",
                "'()', '(]', false, true,  false, false",

                "'[]', '[)', true,  false, true,  true",
                "'[]', '()', true,  true,  true,  true",
                "'[]', '[]', true,  false, false, true",
                "'[]', '(]', true,  true,  false, true",

                "'(]', '[)', false, false, true,  true",
                "'(]', '()', false, true,  true,  true",
                "'(]', '[]', false, false, false, true",
                "'(]', '(]', false, true,  false, true",
        })
        @DisplayName("Все 16 комбинаций границ")
        void allBoundsCombinations(
                String bounds1,
                String bounds2,
                boolean expectedLowerLeft,
                boolean expectedUpperLeft,
                boolean expectedLowerRight,
                boolean expectedUpperRight) {

            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(20), bounds1)
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(5), BASE_DATE.plusDays(15), bounds2)
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(2, result.size(),
                    "Для bounds1=" + bounds1 + ", bounds2=" + bounds2);

            // Проверяем левый кусок
            assertEquals(BASE_DATE, result.get(0).lower(),
                    "Левый кусок: lower для bounds1=" + bounds1);
            assertEquals(expectedLowerLeft, result.get(0).lowerInc(),
                    "Левый кусок: lowerInc для bounds1=" + bounds1);
            assertEquals(BASE_DATE.plusDays(5), result.get(0).upper(),
                    "Левый кусок: upper для bounds2=" + bounds2);
            assertEquals(expectedUpperLeft, result.get(0).upperInc(),
                    "Левый кусок: upperInc для bounds1=" + bounds1 + ", bounds2=" + bounds2);

            // Проверяем правый кусок
            assertEquals(BASE_DATE.plusDays(15), result.get(1).lower(),
                    "Правый кусок: lower для bounds2=" + bounds2);
            assertEquals(expectedLowerRight, result.get(1).lowerInc(),
                    "Правый кусок: lowerInc для bounds1=" + bounds1 + ", bounds2=" + bounds2);
            assertEquals(BASE_DATE.plusDays(20), result.get(1).upper(),
                    "Правый кусок: upper для bounds1=" + bounds1);
            assertEquals(expectedUpperRight, result.get(1).upperInc(),
                    "Правый кусок: upperInc для bounds1=" + bounds1);
        }
    }

    // ==================== ВЫЧИТАНИЕ НА ГРАНИЦАХ ====================

    @Nested
    @DisplayName("Вычитание на границах")
    class BoundarySubtractionTests {

        @Test
        @DisplayName("Вычитание с совпадающей нижней границей")
        void subtractAtLowerBoundary() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(20), "[)")
            ));

            // Вычитаемый начинается там же, где и уменьшаемый
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(10), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(1, result.size());
            assertEquals(BASE_DATE.plusDays(10), result.get(0).lower());
            assertEquals(BASE_DATE.plusDays(20), result.get(0).upper());
            assertTrue(result.get(0).lowerInc()); // NOT mr2.upperInc = NOT false = true
            assertFalse(result.get(0).upperInc());
        }

        @Test
        @DisplayName("Вычитание с совпадающей верхней границей")
        void subtractAtUpperBoundary() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(20), "[)")
            ));

            // Вычитаемый заканчивается там же, где и уменьшаемый
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(10), BASE_DATE.plusDays(20), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(1, result.size());
            assertEquals(BASE_DATE, result.get(0).lower());
            assertEquals(BASE_DATE.plusDays(10), result.get(0).upper());
            assertTrue(result.get(0).lowerInc());
            assertFalse(result.get(0).upperInc()); // NOT mr2.lowerInc = NOT true = false
        }

        @Test
        @DisplayName("Вычитание точки [a,a] из [a,b)")
        void subtractPointFromRange() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(10), "[)")
            ));

            // Вычитаем точку в середине
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(5), BASE_DATE.plusDays(5), "[]")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(2, result.size());

            // Левый кусок: [день 0, день 5)
            assertEquals(BASE_DATE.plusDays(5), result.get(0).upper());
            assertFalse(result.get(0).upperInc()); // NOT mr2.lowerInc = NOT true = false

            // Правый кусок: (день 5, день 10)
            assertEquals(BASE_DATE.plusDays(5), result.get(1).lower());
            assertFalse(result.get(1).lowerInc()); // NOT mr2.upperInc = NOT true = false
        }
    }

    // ==================== ПОЛНОЕ ПОГЛОЩЕНИЕ С РАЗНЫМИ ГРАНИЦАМИ ====================

    @Nested
    @DisplayName("Полное поглощение с разными границами")
    class FullAbsorptionBoundsTests {

        @Test
        @DisplayName("Вычитание большего диапазона с разными границами")
        void subtractLargerRangeDifferentBounds() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(10), "[)")
            ));

            // Вычитаемый больше и имеет другие границы
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.minusDays(5), BASE_DATE.plusDays(15), "()")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Вычитание идентичного диапазона с такими же границами")
        void subtractIdenticalRange() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(10), "[)")
            ));

            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(10), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Вычитание идентичного диапазона с разными границами")
        void subtractIdenticalRangeDifferentBounds() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(10), "[)")
            ));

            // Другая верхняя включительность
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(10), "[]")
            ));

            TsMultiRange result = mr1.difference(mr2);

            // mr2 содержит mr1 (mr2 больше на одну точку на правой границе)
            assertTrue(result.isEmpty());
        }
    }

    // ==================== СЛОЖНЫЕ СЛУЧАИ ====================

    @Nested
    @DisplayName("Сложные случаи с разными границами")
    class ComplexBoundsTests {

        @Test
        @DisplayName("Вычитание из 10 диапазонов с разными границами")
        void subtractFromTenRangesMixedBounds() {
            // Создаём 10 диапазонов с чередующимися границами
            List<TsRange> ranges = new ArrayList<>();
            String[] bounds = {"[)", "()", "[]", "(]", "[)", "()", "[]", "(]", "[)", "()"};
            for (int i = 0; i < 10; i++) {
                LocalDateTime start = BASE_DATE.plusDays(i * 10L);
                LocalDateTime end = start.plusDays(5);
                ranges.add(TsRange.of(start, end, bounds[i]));
            }
            TsMultiRange mr1 = TsMultiRange.of(ranges);

            // Вычитаем диапазон, пересекающий первые три
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(3), BASE_DATE.plusDays(22), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            // Должно получиться несколько кусков с разными включительностями
            assertTrue(result.size() > 5);

            // Проверяем, что все диапазоны валидны
            for (TsRange r : result) {
                assertFalse(r.isEmpty(), "Все куски должны быть непустыми");
                assertTrue(r.lower().isBefore(r.upper()) ||
                                (r.lower().isEqual(r.upper()) && r.lowerInc() && r.upperInc()),
                        "Все диапазоны должны быть валидными");
            }
        }

        @Test
        @DisplayName("Множественные вычитания с разными границами")
        void multipleSubtractionsMixedBounds() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(100), "[)")
            ));

            // Создаём 10 диапазонов для вычитания с разными границами
            List<TsRange> subtractRanges = new ArrayList<>();
            String[] bounds = {"[)", "()", "[]", "(]", "[)", "()", "[]", "(]", "[)", "()"};
            for (int i = 0; i < 10; i++) {
                LocalDateTime start = BASE_DATE.plusDays(i * 10L + 2);
                LocalDateTime end = start.plusDays(3);
                subtractRanges.add(TsRange.of(start, end, bounds[i]));
            }
            TsMultiRange mr2 = TsMultiRange.of(subtractRanges);

            TsMultiRange result = mr1.difference(mr2);

            // Должно получиться 11 кусков (10 дырок)
            assertEquals(11, result.size());

            // Проверяем, что все куски не пересекаются
            for (int i = 0; i < result.size() - 1; i++) {
                assertTrue(result.get(i).strictlyLeftOf(result.get(i + 1)),
                        "Куски не должны пересекаться");
            }
        }
    }
}