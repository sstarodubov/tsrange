package ru.starodubov;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LowUpMultiRangeTest {

    @Test
    @DisplayName("lower() возвращает lower первого диапазона")
    void lowerReturnsFirstLower() {
        TsRange r1 = TsRange.of(
                LocalDateTime.of(2026, 1, 5, 10, 0),
                LocalDateTime.of(2026, 1, 5, 12, 0),
                true, false
        );
        TsRange r2 = TsRange.of(
                LocalDateTime.of(2026, 1, 15, 14, 0),
                LocalDateTime.of(2026, 1, 15, 18, 0),
                true, false
        );

        // Передаём в обратном порядке — нормализация отсортирует
        TsMultiRange multi = TsMultiRange.of(Arrays.asList(r2, r1));

        assertEquals(LocalDateTime.of(2026, 1, 5, 10, 0), multi.lower());
    }

    @Test
    @DisplayName("lower() пустого мультисписка возвращает null")
    void lowerOfEmptyReturnsNull() {
        TsMultiRange empty = TsMultiRange.of(Collections.emptyList());
        assertNull(empty.lower());
    }

    @Test
    @DisplayName("upper() возвращает upper последнего диапазона")
    void upperReturnsLastUpper() {
        TsRange r1 = TsRange.of(
                LocalDateTime.of(2026, 1, 5, 10, 0),
                LocalDateTime.of(2026, 1, 5, 12, 0),
                true, false
        );
        TsRange r2 = TsRange.of(
                LocalDateTime.of(2026, 1, 15, 14, 0),
                LocalDateTime.of(2026, 1, 15, 18, 0),
                true, true  // inclusive upper
        );

        TsMultiRange multi = TsMultiRange.of(Arrays.asList(r1, r2));

        assertEquals(LocalDateTime.of(2026, 1, 15, 18, 0), multi.upper());
        assertTrue(multi.upperInc());
    }

    @Test
    @DisplayName("lower_inc() и upper_inc() работают корректно")
    void boundsIncMethods() {
        TsRange r1 = TsRange.of(
                LocalDateTime.of(2026, 1, 5, 10, 0),
                LocalDateTime.of(2026, 1, 5, 12, 0),
                false, false  // exclusive lower
        );
        TsRange r2 = TsRange.of(
                LocalDateTime.of(2026, 1, 15, 14, 0),
                LocalDateTime.of(2026, 1, 15, 18, 0),
                true, true    // inclusive upper
        );

        TsMultiRange multi = TsMultiRange.of(Arrays.asList(r1, r2));

        assertFalse(multi.lowerInc());  // ( от первого
        assertTrue(multi.upperInc());   // ] от последнего
    }

    @Test
    @DisplayName("Все функции для пустого мультисписка")
    void allFunctionsForEmpty() {
        TsMultiRange empty = TsMultiRange.of(Collections.emptyList());

        assertNull(empty.lower());
        assertNull(empty.upper());
        assertFalse(empty.lowerInc());
        assertFalse(empty.upperInc());
        assertFalse(empty.lowerInf());
        assertFalse(empty.upperInf());
    }
}
