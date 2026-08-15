package ru.starodubov;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты нормализации TsMultiRange (минимум 10 элементов на вход)")
class TsMultiRangeNormalizationStressTest {

    // Базовая дата для тестов
    private static final LocalDateTime BASE_DATE = LocalDateTime.of(2026, 1, 1, 0, 0);

    // Вспомогательный метод для создания времени
    private LocalDateTime time(int hour) {
        return BASE_DATE.plusHours(hour);
    }

    // Вспомогательный метод для создания диапазона
    private TsRange range(int startHour, int endHour, boolean lowerInc, boolean upperInc) {
        return TsRange.of(time(startHour), time(endHour), lowerInc, upperInc);
    }

    // Дефолтный диапазон [start, end)
    private TsRange range(int startHour, int endHour) {
        return range(startHour, endHour, true, false);
    }

    // Пустой диапазон
    private TsRange emptyRange() {
        return TsRange.EMPTY;
    }

    // Вспомогательный метод для проверки конкретного диапазона в результате
    private void assertRange(TsRange actual, int expStart, int expEnd, boolean expLInc, boolean expUInc) {
        assertEquals(time(expStart), actual.lower(), "Нижняя граница не совпадает");
        assertEquals(time(expEnd), actual.upper(), "Верхняя граница не совпадает");
        assertEquals(expLInc, actual.lowerInc(), "Нижняя включенность не совпадает");
        assertEquals(expUInc, actual.upperInc(), "Верхняя включенность не совпадает");
    }

    @Nested
    @DisplayName("Комплексные сценарии (Микс из всего)")
    class ComplexScenarios {

        @Test
        @DisplayName("1. Кухонная мойка: null, empty, пересечения, дырки, сортировка (12 элементов)")
        void mixedScenario() {
            // Вход: 12 элементов в хаотичном порядке
            List<TsRange> input = Arrays.asList(
                    range(10, 12),          // 0. [10, 12)
                    range(1, 3),            // 2. [01, 03)
                    emptyRange(),           // 3. empty
                    range(2, 4),            // 4. [02, 04) -> merge с #2 -> [01, 04)
                    range(5, 6),            // 5. [05, 06)
                    range(4, 5, true, true),// 6. [04, 05] -> adjacent с #5 -> [04, 06)
                    range(11, 13),          // 7. [11, 13) -> overlap с #0 -> [10, 13)
                    range(8, 9),            // 8. [08, 09) -> дырка
                    range(14, 15),          // 10. [14, 15) -> дырка от #7
                    emptyRange()            // 11. empty
            );

            TsMultiRange result = TsMultiRange.of(input);

            // Ожидаем 4 диапазона после нормализации
            assertEquals(4, result.size());

            // [01, 04) + [04, 06) -> [01, 06)
            assertRange(result.get(0), 1, 6, true, false);

            // [08, 09) остается как есть
            assertRange(result.get(1), 8, 9, true, false);

            // [10, 12) + [11, 13) -> [10, 13)
            assertRange(result.get(2), 10, 13, true, false);

            // [14, 15) остается как есть
            assertRange(result.get(3), 14, 15, true, false);
        }

        @Test
        @DisplayName("2. Цепная реакция: все 10 элементов сливаются в один")
        void chainReactionMerge() {
            // Вход: 10 элементов в обратном порядке (проверка сортировки)
            List<TsRange> input = Arrays.asList(
                    range(9, 10),
                    range(8, 9),
                    range(7, 8),
                    range(6, 7),
                    range(5, 6),
                    range(4, 5),
                    range(3, 4),
                    range(2, 3),
                    range(1, 2),
                    range(0, 1)
            );
            TsMultiRange result = TsMultiRange.of(input);

            assertEquals(1, result.size());
            assertRange(result.get(0), 0, 10, true, false);
        }

        @Test
        @DisplayName("3. Строго изолированные: только сортировка и фильтрация, без merge (12 элементов)")
        void strictlyDisjoint() {
            List<TsRange> input = Arrays.asList(
                    range(10, 11),
                    range(8, 9),
                    range(6, 7),
                    emptyRange(),
                    range(4, 5),
                    range(2, 3),
                    range(0, 1),
                    range(12, 13),
                    range(14, 15),
                    range(16, 17),
                    range(18, 19)
            );

            TsMultiRange result = TsMultiRange.of(input);

            // 12 элементов - 1 null - 1 empty = 10 диапазонов
            assertEquals(10, result.size());

            // Проверяем, что они отсортированы и не изменились
            assertRange(result.get(0), 0, 1, true, false);
            assertRange(result.get(1), 2, 3, true, false);
            assertRange(result.get(2), 4, 5, true, false);
            assertRange(result.get(3), 6, 7, true, false);
            assertRange(result.get(4), 8, 9, true, false);
            assertRange(result.get(5), 10, 11, true, false);
            assertRange(result.get(6), 12, 13, true, false);
            assertRange(result.get(7), 14, 15, true, false);
            assertRange(result.get(8), 16, 17, true, false);
            assertRange(result.get(9), 18, 19, true, false);
        }
    }

    @Nested
    @DisplayName("Специфичные паттерны")
    class SpecificPatterns {

        @Test
        @DisplayName("4. Матрёшка: один большой диапазон покрывает 9 маленьких (10 элементов)")
        void nestedRanges() {
            List<TsRange> input = Arrays.asList(
                    range(0, 20),       // Большой внешний
                    range(1, 2),        // Внутренние
                    range(3, 4),
                    range(5, 6),
                    range(7, 8),
                    range(9, 10),
                    range(11, 12),
                    range(13, 14),
                    range(15, 16),
                    range(17, 18)
            );

            TsMultiRange result = TsMultiRange.of(input);

            assertEquals(1, result.size());
            assertRange(result.get(0), 0, 20, true, false);
        }

        @Test
        @DisplayName("5. Граничные условия: включенность/исключенность границ (10 элементов)")
        void boundaryConditions() {
            List<TsRange> input = Arrays.asList(
                    range(0, 1, false, false),  // (00, 01)
                    range(1, 2, true, false),   // [01, 02) -> Adjacent с #0 -> (00, 02)

                    range(3, 4, false, false),  // (03, 04)
                    range(4, 5, false, false),  // (04, 05) -> Дырка в точке 04, НЕ merge

                    range(6, 7, true, true),    // [06, 07]
                    range(7, 8, true, true),    // [07, 08] -> Overlap в точке 07 -> [06, 08]

                    range(9, 10, false, true),  // (09, 10]
                    range(10, 11, true, false), // [10, 11) -> Adjacent -> (09, 11)

                    emptyRange()               // empty
            );

            TsMultiRange result = TsMultiRange.of(input);

            assertEquals(5, result.size());

            assertRange(result.get(0), 0, 2, false, false); // (00, 02)
            assertRange(result.get(1), 3, 4, false, false); // (03, 04)
            assertRange(result.get(2), 4, 5, false, false); // (04, 05)
            assertRange(result.get(3), 6, 8, true, true);   // [06, 08]
            assertRange(result.get(4), 9, 11, false, false);// (09, 11)
        }

    }

    @Nested
    @DisplayName("Проверка инвариантов и исключений")
    class InvariantsAndExceptions {

        @Test
        @DisplayName("7. Передача null вместо списка бросает IllegalArgumentException")
        void nullListThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> TsMultiRange.of((List<TsRange>) null));
        }

        @Test
        @DisplayName("8. Передача пустого списка возвращает пустой TsMultiRange")
        void emptyListReturnsEmptyMultiRange() {
            TsMultiRange result = TsMultiRange.of(Collections.emptyList());
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("9. Результат иммутабелен (проверка через итератор и get)")
        void resultIsImmutable() {
            List<TsRange> input = Arrays.asList(
                    range(0, 1), range(2, 3), range(4, 5), range(6, 7), range(8, 9),
                    range(10, 11), range(12, 13), range(14, 15), range(16, 17), range(18, 19)
            );

            TsMultiRange result = TsMultiRange.of(input);

            // Проверяем, что итератор работает
            int count = 0;
            for (TsRange r : result) {
                assertNotNull(r);
                count++;
            }
            assertEquals(10, count);

            // Проверяем getFirst и getLast
            assertRange(result.getFirst(), 0, 1, true, false);
            assertRange(result.getLast(), 18, 19, true, false);
        }
    }
}