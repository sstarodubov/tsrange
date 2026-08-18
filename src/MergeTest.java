package ru.nspk.pcl.common.tsrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MergeTest {

    public TsRange range(int l , int r, boolean lb, boolean rb) {
        var lower = LocalDateTime.of(2020, 1, 1, l, 0);
        var upper = LocalDateTime.of(2020, 1, 1, r, 0);
        return TsRange.of(lower, upper, lb, rb);
    }

    public LocalDateTime time(int t) {
        return LocalDateTime.of(2020, 1, 1, t, 0);
    }

    @Test
    @DisplayName("rangeMerge: два диапазона с дыркой → один сплошной")
    void rangeMergeFillsGaps() {
        TsRange r1 = range(1, 5, true, false);   // [01:00, 05:00)
        TsRange r2 = range(10, 15, true, false);  // [10:00, 15:00)

        TsMultiRange multi = TsMultiRange.of(List.of(r1, r2));
        TsRange merged = multi.merge();

        // Дырка 05:00–10:00 заполнена
        assertEquals(time(1), merged.lower());
        assertEquals(time(15), merged.upper());
        assertTrue(merged.lowerInc());
        assertFalse(merged.upperInc());
    }

    @Test
    @DisplayName("rangeMerge: один элемент → возвращает его")
    void rangeMergeSingleElement() {
        TsRange r = range(3, 7, false, true);
        TsMultiRange multi = TsMultiRange.of(r);

        assertEquals(r, multi.merge());
    }

    @Test
    @DisplayName("rangeMerge: пустой мультисписок → EMPTY")
    void rangeMergeEmpty() {
        TsMultiRange multi = TsMultiRange.of(Collections.emptyList());
        assertEquals(TsRange.EMPTY, multi.merge());
    }

    @Test
    @DisplayName("rangeMerge: 10 элементов → один диапазон от первого до последнего")
    void rangeMergeManyElements() {
        List<TsRange> input = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            input.add(range(i * 2, i * 2 + 1, true, false));
        }
        // Диапазоны: [0,1), [2,3), [4,5), ..., [18,19)

        TsMultiRange multi = TsMultiRange.of(input);
        TsRange merged = multi.merge();

        assertEquals(time(0), merged.lower());
        assertEquals(time(19), merged.upper());
        assertTrue(merged.lowerInc());
        assertFalse(merged.upperInc());
    }
}
