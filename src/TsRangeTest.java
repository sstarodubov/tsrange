package ru.nspk.pcl.common.tsrange;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TsRangeTest {

    @Test
    void lessThan8() {
        assertTrue(
                TsRange.of("2001-01-05", "2001-01-05", "()")
                        .lessThan(TsRange.of("2001-01-05", "2001-01-10", "[)")));
    }

    @Test
    void lessThan7() {
        assertFalse(
                TsRange.of("2001-01-01", "2001-01-05", "[)")
                        .lessThan(TsRange.of("2001-01-01", "2001-01-05", "[)")));
    }

    @Test
    void lessThan6() {
        assertTrue(
                TsRange.of("2001-01-01", "2001-01-05", "[)")
                        .lessThan(TsRange.of("2001-01-01", "2001-01-05", "[]")));
    }

    @Test
    void lessThan5() {
        assertFalse(
                TsRange.of("2001-01-01", "2001-01-02", "[]")
                        .lessThan(TsRange.of("2001-01-01", "2001-01-02", "[]")));
    }

    @Test
    void lessThan4() {
        assertFalse(
                TsRange.of("2001-01-01", "2001-01-02", "(]")
                        .lessThan(TsRange.of("2001-01-01", "2001-01-02", "[]")));
    }

    @Test
    void lessThan3() {
        assertFalse(
                TsRange.of("2001-01-01", "2001-01-06" )
                        .lessThan(TsRange.of("2001-01-01", "2001-01-05")));
    }

    @Test
    void lessThan2() {
        assertFalse(
                TsRange.of("2001-01-01", "2001-01-05", "(]")
                        .lessThan(TsRange.of("2001-01-01", "2001-01-02", "[]")));
    }

    @Test
    void lessThan() {
        assertTrue(TsRange.of("2001-01-01", "2001-01-02")
                .lessThan(TsRange.of("2001-01-01", "2001-01-05", "(]")));
    }

    @Test
    void createTsRange() {
        assertThrows(UnsupportedOperationException.class, () -> TsRange.of(
                LocalDateTime.of(2020, 1, 2, 0, 0, 0),
                LocalDateTime.of(2020, 1, 1, 0, 0, 0)
        ));
    }

    @Test
    void empty() {
        assertTrue(TsRange.of(LocalDateTime.MAX, LocalDateTime.MAX).isEmpty());
    }

    @Test
    void empty2() {
        assertTrue(TsRange.of(LocalDateTime.MAX, LocalDateTime.MAX, "()").isEmpty());
    }

    @Test
    void empty3() {
        assertTrue(TsRange.of(LocalDateTime.MAX, LocalDateTime.MAX, "[)").isEmpty());
    }

    @Test
    void empty4() {
        assertFalse(TsRange.of(LocalDateTime.MAX, LocalDateTime.MAX, "[]").isEmpty());
    }

    @Test
    void empty5() {
        assertFalse(TsRange.of(LocalDateTime.MIN, LocalDateTime.MAX, "()").isEmpty());
    }

    @Test
    void empty6() {
        TsRange r = TsRange.of("2026-01-01", "2026-01-01", "[)");
        assertTrue(r.isEmpty());
    }
}