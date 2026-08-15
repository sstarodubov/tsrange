package ru.starodubov;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ContainsElementTest {

    public TsRange range(int l , int r, boolean lb, boolean rb) {
        var lower = LocalDateTime.of(2020, 1, 1, l, 0);
        var upper = LocalDateTime.of(2020, 1, 1, r, 0);
        return TsRange.of(lower, upper, lb, rb);
    }

    public LocalDateTime time(int t) {
        return LocalDateTime.of(2020, 1, 1, t, 0);
    }
    @Test
    @DisplayName("@> простой случай: {1-5} содержит {2-3}")
    void containsSimple2() {
        TsRange r1 = range(1, 5, true, false);
        TsRange r2 = range(10, 15, true, false);

        TsRange r3 = range(2, 3, true, false);
        TsRange r4 = range(11, 12, true, false);

        TsMultiRange m1 = TsMultiRange.of(List.of(r1, r2));
        TsMultiRange m2 = TsMultiRange.of(List.of(r3, r4));

        assertTrue(m1.containsMultirange(m2));
        assertFalse(m2.containsMultirange(m1));  // обратное неверно
    }

    @Test
    @DisplayName("@> простой случай: {1-5} содержит {2-3}")
    void containsSimple() {
        TsRange r1 = range(1, 5, true, false);
        TsRange r2 = range(2, 3, true, false);

        TsMultiRange m1 = TsMultiRange.of(r1);
        TsMultiRange m2 = TsMultiRange.of(r2);

        assertTrue(m1.containsMultirange(m2));
        assertFalse(m2.containsMultirange(m1));  // обратное неверно
    }

    @Test
    @DisplayName("@> несколько диапазонов: каждый элемент покрыт")
    void containsMultipleRanges() {
        TsMultiRange m1 = TsMultiRange.of(Arrays.asList(
                range(1, 5, true, false),
                range(10, 15, true, false)
        ));
        TsMultiRange m2 = TsMultiRange.of(Arrays.asList(
                range(2, 3, true, false),
                range(11, 12, true, false)
        ));

        assertTrue(m1.containsMultirange(m2));
        assertFalse(m2.containsMultirange(m1));
    }

    @Test
    @DisplayName("@> элемент не покрыт: {1-5, 10-15} не содержит {2-3, 7-8}")
    void notContainsUncoveredElement() {
        TsMultiRange m1 = TsMultiRange.of(Arrays.asList(
                range(1, 5, true, false),
                range(10, 15, true, false)
        ));
        TsMultiRange m2 = TsMultiRange.of(Arrays.asList(
                range(2, 3, true, false),
                range(7, 8, true, false)  // не покрыт!
        ));

        assertFalse(m1.containsMultirange(m2));
    }

    @Test
    @DisplayName("@> пустой мультисписок содержится в любом")
    void emptyIsContainedByAny() {
        TsMultiRange nonEmpty = TsMultiRange.of(range(1, 5, true, false));
        TsMultiRange empty = TsMultiRange.of(Collections.emptyList());

        assertTrue(nonEmpty.containsMultirange(empty));  // любой содержит empty
        assertFalse(empty.containsMultirange(nonEmpty)); // empty не содержит non-empty
        assertTrue(empty.containsMultirange(empty));     // empty содержит empty
    }

    @Test
    @DisplayName("@> граничные случаи с inclusive/exclusive")
    void boundaryConditions() {
        TsMultiRange m1 = TsMultiRange.of(range(1, 5, true, false));   // [1, 5)
        TsMultiRange m2 = TsMultiRange.of(range(1, 5, false, false));  // (1, 5)
        TsMultiRange m3 = TsMultiRange.of(range(1, 5, true, true));    // [1, 5]

        assertTrue(m1.containsMultirange(m2));   // [1,5) содержит (1,5)
        assertFalse(m2.containsMultirange(m1));  // (1,5) не содержит [1,5)
        assertFalse(m1.containsMultirange(m3));  // [1,5) не содержит [1,5]
    }

    @Test
    @DisplayName("@> равные мультисписки содержат друг друга")
    void equalMultirangesContainEachOther() {
        TsMultiRange m1 = TsMultiRange.of(Arrays.asList(
                range(1, 5, true, false),
                range(10, 15, true, false)
        ));
        TsMultiRange m2 = TsMultiRange.of(Arrays.asList(
                range(1, 5, true, false),
                range(10, 15, true, false)
        ));

        assertTrue(m1.containsMultirange(m2));
        assertTrue(m2.containsMultirange(m1));
    }

    @Test
    @DisplayName("@> элемент выходит за границу")
    void elementExceedsBoundary() {
        TsMultiRange m1 = TsMultiRange.of(range(1, 5, true, false));
        TsMultiRange m2 = TsMultiRange.of(range(2, 6, true, false));  // 6 > 5

        assertFalse(m1.containsMultirange(m2));
    }
}
