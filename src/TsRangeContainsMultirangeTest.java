package ru.nspk.pcl.common.tsrange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class TsRangeContainsMultirangeTest {

    private static LocalDateTime dt(int y, int m, int d) {
        return LocalDateTime.of(y, m, d, 0, 0);
    }

    private static TsRange range(LocalDateTime lo, LocalDateTime hi) {
        return TsRange.of(lo, hi); // [lo, hi)
    }

    private static TsMultiRange mr(TsRange... ranges) {
        return TsMultiRange.of(List.of(ranges));
    }

    // -------------------------------------------------------
    // Положительные случаи
    // -------------------------------------------------------

    @Test
    @DisplayName("Диапазон покрывает мультисписок с дырками")
    void rangeCoversMultirangeWithGaps() {
        TsRange r = range(dt(2024, 1, 1), dt(2024, 12, 31));
        TsMultiRange multi = mr(
                range(dt(2024, 3, 1), dt(2024, 3, 15)),
                range(dt(2024, 6, 1), dt(2024, 6, 10)),
                range(dt(2024, 9, 1), dt(2024, 9, 20))
        );
        assertThat(r.containsMultirange(multi)).isTrue();
    }

    @Test
    @DisplayName("Диапазон точно совпадает с мультисписком из одного поддиапазона")
    void rangeEqualsSingleSubrange() {
        TsRange r = range(dt(2024, 1, 1), dt(2024, 1, 10));
        TsMultiRange multi = mr(range(dt(2024, 1, 1), dt(2024, 1, 10)));
        assertThat(r.containsMultirange(multi)).isTrue();
    }

    @Test
    @DisplayName("Диапазон покрывает мультисписок из одного поддиапазона")
    void rangeCoversSingleSubrange() {
        TsRange r = range(dt(2024, 1, 1), dt(2024, 1, 31));
        TsMultiRange multi = mr(range(dt(2024, 1, 5), dt(2024, 1, 15)));
        assertThat(r.containsMultirange(multi)).isTrue();
    }

    @Test
    @DisplayName("Пустой мультисписок содержится в любом диапазоне")
    void emptyMultirangeAlwaysContained() {
        TsRange r = range(dt(2024, 1, 1), dt(2024, 1, 10));
        TsMultiRange empty = TsMultiRange.of(Collections.emptyList());
        assertThat(r.containsMultirange(empty)).isTrue();
    }

    @Test
    @DisplayName("Пустой мультисписок содержится даже в пустом диапазоне")
    void emptyMultirangeInEmptyRange() {
        TsRange emptyRange = TsRange.EMPTY;
        TsMultiRange emptyMulti = TsMultiRange.of(Collections.emptyList());
        assertThat(emptyRange.containsMultirange(emptyMulti)).isTrue();
    }

    // -------------------------------------------------------
    // Отрицательные случаи
    // -------------------------------------------------------

    @Test
    @DisplayName("Последний поддиапазон выходит за верхнюю границу")
    void lastSubrangeExceedsUpper() {
        TsRange r = range(dt(2024, 1, 1), dt(2024, 6, 30));
        TsMultiRange multi = mr(
                range(dt(2024, 2, 1), dt(2024, 2, 15)),
                range(dt(2024, 7, 1), dt(2024, 7, 10))   // выходит за [.., Jun 30)
        );
        assertThat(r.containsMultirange(multi)).isFalse();
    }

    @Test
    @DisplayName("Первый поддиапазон начинается до нижней границы")
    void firstSubrangeBeforeLower() {
        TsRange r = range(dt(2024, 3, 1), dt(2024, 12, 31));
        TsMultiRange multi = mr(
                range(dt(2024, 1, 1), dt(2024, 1, 15)),  // начинается до Mar 1
                range(dt(2024, 6, 1), dt(2024, 6, 10))
        );
        assertThat(r.containsMultirange(multi)).isFalse();
    }

    @Test
    @DisplayName("Оба поддиапазона выходят за границы")
    void bothSubrangesOutside() {
        TsRange r = range(dt(2024, 3, 1), dt(2024, 6, 30));
        TsMultiRange multi = mr(
                range(dt(2024, 1, 1), dt(2024, 1, 15)),
                range(dt(2024, 9, 1), dt(2024, 9, 10))
        );
        assertThat(r.containsMultirange(multi)).isFalse();
    }

    @Test
    @DisplayName("Пустой диапазон не содержит непустой мультисписок")
    void emptyRangeDoesNotContain() {
        TsRange emptyRange = TsRange.EMPTY;
        TsMultiRange multi = mr(range(dt(2024, 1, 1), dt(2024, 1, 10)));
        assertThat(emptyRange.containsMultirange(multi)).isFalse();
    }

    // -------------------------------------------------------
    // Граничные случаи
    // -------------------------------------------------------

    @Test
    @DisplayName("Границы: верхняя граница диапазона исключена, поддиапазона — тоже")
    void exclusiveUpperBounds() {
        // range = [Jan 1, Jun 30)
        // last subrange = [Jun 1, Jun 30)  → upper совпадает, оба исключены
        TsRange r = range(dt(2024, 1, 1), dt(2024, 6, 30));
        TsMultiRange multi = mr(range(dt(2024, 6, 1), dt(2024, 6, 30)));
        assertThat(r.containsMultirange(multi)).isTrue();
    }

    @Test
    @DisplayName("Границы: поддиапазон включает точку, которую диапазон исключает")
    void inclusiveVsExclusiveBound() {
        // range = [Jan 1, Jun 30)
        // last subrange = [Jun 1, Jun 30]  → включает Jun 30, а range исключает
        TsRange r = range(dt(2024, 1, 1), dt(2024, 6, 30));
        TsMultiRange multi = mr(
                TsRange.of(dt(2024, 6, 1), dt(2024, 6, 30), "[]")
        );
        assertThat(r.containsMultirange(multi)).isFalse();
    }

    @Test
    @DisplayName("Мультисписок из одного поддиапазона, совпадающего с границей")
    void singleSubrangeAtBoundary() {
        TsRange r = range(dt(2024, 1, 1), dt(2024, 12, 31));
        TsMultiRange multi = mr(
                range(dt(2024, 1, 1), dt(2024, 1, 1)),  // [Jan 1, Jan 1) → empty
                range(dt(2024, 6, 1), dt(2024, 6, 10))
        );
        // После нормализации пустой поддиапазон удалится,
        // останется только [Jun 1, Jun 10)
        assertThat(r.containsMultirange(multi)).isTrue();
    }

    // -------------------------------------------------------
    // null
    // -------------------------------------------------------

    @Test
    @DisplayName("null мультисписок бросает IllegalArgumentException")
    void nullMultirangeThrows() {
        TsRange r = range(dt(2024, 1, 1), dt(2024, 1, 10));
        assertThatThrownBy(() -> r.containsMultirange(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}