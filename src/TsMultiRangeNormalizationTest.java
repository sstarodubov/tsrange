package ru.nspk.pcl.common.tsrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TsMultiRangeNormalizationTest {

    // Вспомогательные временные точки для тестов
    private static final LocalDateTime T1 = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalDateTime T2 = LocalDateTime.of(2026, 1, 1, 12, 0);
    private static final LocalDateTime T3 = LocalDateTime.of(2026, 1, 1, 14, 0);
    private static final LocalDateTime T4 = LocalDateTime.of(2026, 1, 1, 16, 0);
    private static final LocalDateTime T5 = LocalDateTime.of(2026, 1, 1, 18, 0);

    private TsRange range(LocalDateTime lower, LocalDateTime upper, boolean lInc, boolean uInc) {
        return TsRange.of(lower, upper, lInc, uInc);
    }

    @Test
    @DisplayName("1. Пустой список и null нормализуются в пустой мультисписок")
    void normalizesEmptyAndNullInput() {
        assertTrue(TsMultiRange.of(Collections.emptyList()).isEmpty());
    }

    @Test
    @DisplayName("2. Список из пустых и null диапазонов нормализуется в пустой мультисписок")
    void normalizesListOfEmptyAndNulls() {
        List<TsRange> input = Arrays.asList(TsRange.EMPTY, TsRange.EMPTY);
        TsMultiRange result = TsMultiRange.of(input);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("3. Один валидный диапазон остается без изменений")
    void normalizesSingleValidRange() {
        TsRange r = range(T1, T2, true, false);
        TsMultiRange result = TsMultiRange.of(r);

        assertEquals(1, result.size());
        assertEquals(r, result.get(0));
    }

    @Test
    @DisplayName("4. Удаляет null и пустые диапазоны из середины списка")
    void removesNullsAndEmptyRanges() {
        TsRange r1 = range(T1, T2, true, false);
        TsRange r2 = range(T3, T4, true, false);

        List<TsRange> input = Arrays.asList(r1, TsRange.EMPTY, r2);
        TsMultiRange result = TsMultiRange.of(input);
        assertEquals(2, result.size());
        assertEquals(r1, result.get(0));
        assertEquals(r2, result.get(1));
    }

    @Test
    void checkTTT() {
        TsRange r1 = range(T1, T2, true, false);
        TsRange r2 = range(T3, T4, true, false);

        List<TsRange> input = List.of(r1, TsRange.EMPTY, r2);
        TsMultiRange result = TsMultiRange.of((input));

        assertEquals(2, result.size());
        assertEquals(r1, result.get(0));
        assertEquals(r2, result.get(1));
    }

    @Test
    @DisplayName("5. Сортирует неупорядоченные диапазоны по возрастанию нижней границы (без схлопывания)")
    void sortsUnorderedRanges() {
        TsRange r1 = range(T1, T2, true, false); // 10:00 - 12:00

        // Делаем r2 короче, чтобы между ним и r3 образовалась "дырка" в 1 час
        LocalDateTime T2_5 = LocalDateTime.of(2026, 1, 1, 15, 0);
        TsRange r2 = range(T3, T2_5, true, false); // 14:00 - 15:00

        TsRange r3 = range(T4, T5, true, false); // 16:00 - 18:00

        // Передаем в обратном порядке
        List<TsRange> input = Arrays.asList(r3, r1, r2);
        TsMultiRange result = TsMultiRange.of(input);

        // Теперь диапазоны не смежные и не пересекающиеся, их останется ровно 3
        assertEquals(3, result.size()); // Примечание: если у вас геттер называется ranges(), используйте его

        // Проверяем, что они отсортировались правильно
        assertEquals(r1, result.get(0)); // 10:00 - 12:00
        assertEquals(r2, result.get(1)); // 14:00 - 15:00
        assertEquals(r3, result.get(2)); // 16:00 - 18:00
    }

    @Test
    @DisplayName("6. Схлопывает пересекающиеся диапазоны (Overlap)")
    void coalescesOverlappingRanges() {
        TsRange r1 = range(T1, T3, true, false); // 10:00 - 14:00
        TsRange r2 = range(T2, T4, true, false); // 12:00 - 16:00

        List<TsRange> input = Arrays.asList(r1, r2);
        TsMultiRange result = TsMultiRange.of(input);

        assertEquals(1, result.size());
        TsRange expected = range(T1, T4, true, false); // 10:00 - 16:00
        assertEquals(expected, result.get(0));
    }

    @Test
    @DisplayName("7. Схлопывает смежные диапазоны (Adjacent) [a, b) и [b, c)")
    void coalescesAdjacentRanges() {
        TsRange r1 = range(T1, T2, true, false); // 10:00 - 12:00
        TsRange r2 = range(T2, T3, true, false); // 12:00 - 14:00

        List<TsRange> input = Arrays.asList(r1, r2);
        TsMultiRange result = TsMultiRange.of(input);

        assertEquals(1, result.size());
        TsRange expected = range(T1, T3, true, false); // 10:00 - 14:00
        assertEquals(expected, result.get(0));
    }

    @Test
    @DisplayName("8. Схлопывает смежные диапазоны с разными границами [a, b] и (b, c)")
    void coalescesAdjacentRangesWithDifferentBounds() {
        TsRange r1 = range(T1, T2, true, true);  // 10:00 - 12:00 ]
        TsRange r2 = range(T2, T3, false, false); // 12:00 - 14:00 )

        List<TsRange> input = Arrays.asList(r1, r2);
        TsMultiRange result = TsMultiRange.of(input);

        assertEquals(1, result.size());
        TsRange expected = range(T1, T3, true, false); // 10:00 - 14:00 )
        assertEquals(expected, result.get(0));
    }

    @Test
    @DisplayName("9. Схлопывает цепочку из нескольких пересекающихся/смежных диапазонов")
    void coalescesChainOfRanges() {
        TsRange r1 = range(T1, T2, true, false); // 10 - 12
        TsRange r2 = range(T2, T3, true, false); // 12 - 14 (adjacent)
        TsRange r3 = range(T3, T4, true, false); // 14 - 16 (adjacent)
        TsRange r4 = range(T3, T5, true, false); // 14 - 18 (overlaps with r3)

        // Передаем перемешанными
        List<TsRange> input = Arrays.asList(r4, r2, r1, r3);
        TsMultiRange result = TsMultiRange.of(input);

        assertEquals(1, result.size());
        TsRange expected = range(T1, T5, true, false); // 10 - 18
        assertEquals(expected, result.get(0));
    }

    @Test
    @DisplayName("10. Оставляет 'дырки' (непересекающиеся и несмежные диапазоны) как есть")
    void leavesGapsIntact() {
        TsRange r1 = range(T1, T2, true, false); // 10:00 - 12:00
        TsRange r2 = range(T3, T4, true, false); // 14:00 - 16:00
        TsRange r3 = range(T4, T5, true, false); // 16:00 - 18:00

        // r2 и r3 смежные, схлопнутся. r1 отделен дыркой.
        List<TsRange> input = Arrays.asList(r3, r1, r2);
        TsMultiRange result = TsMultiRange.of(input);

        assertEquals(2, result.size());

        TsRange expected1 = range(T1, T2, true, false);
        TsRange expected2 = range(T3, T5, true, false); // r2 + r3

        assertEquals(expected1, result.get(0));
        assertEquals(expected2, result.get(1));
    }

    @Test
    @DisplayName("11. Если один диапазон полностью покрывает другой, они схлопываются в больший")
    void coalescesContainedRanges() {
        TsRange outer = range(T1, T5, true, false); // 10:00 - 18:00
        TsRange inner = range(T2, T3, true, false); // 12:00 - 14:00

        List<TsRange> input = Arrays.asList(inner, outer);
        TsMultiRange result = TsMultiRange.of(input);

        assertEquals(1, result.size());
        assertEquals(outer, result.get(0));
    }
}