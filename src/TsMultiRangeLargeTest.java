package ru.nspk.pcl.common.tsrange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TsMultiRange стресс-тесты: 10+ элементов")
class TsMultiRangeLargeTest {

    private static final int RANGE_COUNT = 15;
    private static final LocalDateTime BASE_DATE = LocalDateTime.of(2026, 1, 1, 0, 0);

    /**
     * Создаёт multirange с 15 разнесёнными диапазонами:
     * Каждый диапазон: 5 дней, разрыв 5 дней
     * [01-01,01-06), [01-11,01-16), [01-21,01-26), [01-31,02-05), ...
     */
    private TsMultiRange createLargeMultirange() {
        List<TsRange> ranges = new ArrayList<>();
        for (int i = 0; i < RANGE_COUNT; i++) {
            LocalDateTime start = BASE_DATE.plusDays(i * 10L);
            LocalDateTime end = start.plusDays(5);
            ranges.add(TsRange.of(start, end, "[)"));
        }
        return TsMultiRange.of(ranges);
    }

    /**
     * Создаёт multirange с 12 пересекающимися диапазонами,
     * которые после нормализации должны объединиться в один
     */
    private TsMultiRange createOverlappingMultirange() {
        List<TsRange> ranges = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            LocalDateTime start = BASE_DATE.plusDays(i * 2L);
            LocalDateTime end = start.plusDays(5);
            ranges.add(TsRange.of(start, end, "[)"));
        }
        return TsMultiRange.of(ranges);
    }

    /**
     * Создаёт multirange с 11 смежными диапазонами,
     * которые после нормализации должны объединиться в один
     */
    private TsMultiRange createAdjacentMultirange() {
        List<TsRange> ranges = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            LocalDateTime start = BASE_DATE.plusDays(i * 3L);
            LocalDateTime end = start.plusDays(3);
            ranges.add(TsRange.of(start, end, "[)"));
        }
        return TsMultiRange.of(ranges);
    }

    /**
     * Создаёт multirange с диапазонами в разрывах большого multirange
     */
    private TsMultiRange createGapMultirange() {
        List<TsRange> ranges = new ArrayList<>();
        for (int i = 0; i < RANGE_COUNT - 1; i++) {
            LocalDateTime gapStart = BASE_DATE.plusDays(i * 10L + 5);
            LocalDateTime gapEnd = BASE_DATE.plusDays((i + 1) * 10L);
            ranges.add(TsRange.of(gapStart, gapEnd, "[)"));
        }
        return TsMultiRange.of(ranges);
    }

    /**
     * Создаёт multirange полностью после большого multirange
     */
    private TsMultiRange createAfterMultirange() {
        List<TsRange> ranges = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDateTime start = BASE_DATE.plusDays(200 + i * 10L);
            LocalDateTime end = start.plusDays(5);
            ranges.add(TsRange.of(start, end, "[)"));
        }
        return TsMultiRange.of(ranges);
    }

    // ==================== СОЗДАНИЕ И НОРМАЛИЗАЦИЯ ====================

    @Nested
    @DisplayName("Создание и нормализация")
    class CreationTests {

        @Test
        @DisplayName("15 разнесённых диапазонов остаются раздельными")
        void fifteenDisjointRanges() {
            TsMultiRange mr = createLargeMultirange();

            assertEquals(RANGE_COUNT, mr.size());

            // Проверяем порядок
            for (int i = 0; i < mr.size() - 1; i++) {
                assertTrue(mr.get(i).strictlyLeftOf(mr.get(i + 1)));
            }
        }

        @Test
        @DisplayName("12 пересекающихся диапазонов объединяются в один")
        void twelveOverlappingRangesMergeToOne() {
            TsMultiRange mr = createOverlappingMultirange();

            assertEquals(1, mr.size());
            assertEquals(BASE_DATE, mr.lower());
            // Последний: start = 0 + 11*2 = 22 дня, end = 22 + 5 = 27 дней
            assertEquals(BASE_DATE.plusDays(27), mr.upper());
        }

        @Test
        @DisplayName("11 смежных диапазонов объединяются в один")
        void elevenAdjacentRangesMergeToOne() {
            TsMultiRange mr = createAdjacentMultirange();

            assertEquals(1, mr.size());
            assertEquals(BASE_DATE, mr.lower());
            // Последний: start = 0 + 10*3 = 30 дней, end = 30 + 3 = 33 дня
            assertEquals(BASE_DATE.plusDays(33), mr.upper());
        }

        @Test
        @DisplayName("Неотсортированные диапазоны сортируются при создании")
        void unsortedRangesGetSorted() {
            List<TsRange> ranges = new ArrayList<>();
            // Добавляем в обратном порядке
            for (int i = RANGE_COUNT - 1; i >= 0; i--) {
                LocalDateTime start = BASE_DATE.plusDays(i * 10L);
                LocalDateTime end = start.plusDays(5);
                ranges.add(TsRange.of(start, end, "[)"));
            }

            TsMultiRange mr = TsMultiRange.of(ranges);

            assertEquals(RANGE_COUNT, mr.size());

            // Проверяем, что отсортированы по возрастанию
            for (int i = 0; i < mr.size() - 1; i++) {
                assertTrue(mr.get(i).lower().isBefore(mr.get(i + 1).lower()));
            }
        }

        @Test
        @DisplayName("Смешанные пересекающиеся и разнесённые диапазоны")
        void mixedOverlappingAndDisjoint() {
            List<TsRange> ranges = new ArrayList<>();

            // Группа 1: три пересекающихся диапазона
            ranges.add(TsRange.of(BASE_DATE, BASE_DATE.plusDays(10), "[)"));
            ranges.add(TsRange.of(BASE_DATE.plusDays(5), BASE_DATE.plusDays(15), "[)"));
            ranges.add(TsRange.of(BASE_DATE.plusDays(12), BASE_DATE.plusDays(20), "[)"));

            // Группа 2: два смежных диапазона
            ranges.add(TsRange.of(BASE_DATE.plusDays(25), BASE_DATE.plusDays(30), "[)"));
            ranges.add(TsRange.of(BASE_DATE.plusDays(30), BASE_DATE.plusDays(35), "[)"));

            // Группа 3: один диапазон
            ranges.add(TsRange.of(BASE_DATE.plusDays(40), BASE_DATE.plusDays(45), "[)"));

            TsMultiRange mr = TsMultiRange.of(ranges);

            // Группа 1 объединяется в [0, 20)
            // Группа 2 объединяется в [25, 35)
            // Группа 3 остаётся [40, 45)
            assertEquals(3, mr.size());

            assertEquals(BASE_DATE, mr.get(0).lower());
            assertEquals(BASE_DATE.plusDays(20), mr.get(0).upper());

            assertEquals(BASE_DATE.plusDays(25), mr.get(1).lower());
            assertEquals(BASE_DATE.plusDays(35), mr.get(1).upper());

            assertEquals(BASE_DATE.plusDays(40), mr.get(2).lower());
            assertEquals(BASE_DATE.plusDays(45), mr.get(2).upper());
        }
    }

    // ==================== containsElement ====================

    @Nested
    @DisplayName("containsElement с 15 диапазонами")
    class ContainsElementTests {

        @Test
        @DisplayName("Элемент в каждом из 15 диапазонов")
        void elementInEachRange() {
            TsMultiRange mr = createLargeMultirange();

            for (int i = 0; i < RANGE_COUNT; i++) {
                LocalDateTime element = BASE_DATE.plusDays(i * 10L + 2).plusHours(12);

                assertTrue(mr.containsElement(element),
                        "Элемент " + element + " должен быть в диапазоне " + i);
            }
        }

        @Test
        @DisplayName("Элемент в разрыве между диапазонами")
        void elementInGaps() {
            TsMultiRange mr = createLargeMultirange();

            for (int i = 0; i < RANGE_COUNT - 1; i++) {
                LocalDateTime element = BASE_DATE.plusDays(i * 10L + 7).plusHours(12);

                assertFalse(mr.containsElement(element),
                        "Элемент " + element + " НЕ должен быть в multirange");
            }
        }

        @Test
        @DisplayName("Элемент до первого диапазона")
        void elementBeforeFirstRange() {
            TsMultiRange mr = createLargeMultirange();

            LocalDateTime element = BASE_DATE.minusDays(1);

            assertFalse(mr.containsElement(element));
        }

        @Test
        @DisplayName("Элемент после последнего диапазона")
        void elementAfterLastRange() {
            TsMultiRange mr = createLargeMultirange();

            LocalDateTime element = BASE_DATE.plusDays(RANGE_COUNT * 10L + 10);

            assertFalse(mr.containsElement(element));
        }

        @Test
        @DisplayName("Элемент на границах каждого диапазона")
        void elementOnBoundaries() {
            TsMultiRange mr = createLargeMultirange();

            for (int i = 0; i < RANGE_COUNT; i++) {
                LocalDateTime lower = BASE_DATE.plusDays(i * 10L);
                LocalDateTime upper = lower.plusDays(5);

                // Нижняя граница включающая [
                assertTrue(mr.containsElement(lower),
                        "Нижняя граница диапазона " + i + " должна быть включена");

                // Верхняя граница исключающая )
                assertFalse(mr.containsElement(upper),
                        "Верхняя граница диапазона " + i + " должна быть исключена");
            }
        }
    }

    // ==================== containsRange ====================

    @Nested
    @DisplayName("containsRange с 15 диапазонами")
    class ContainsRangeTests {

        @Test
        @DisplayName("Диапазон внутри каждого из 15 диапазонов")
        void rangeInsideEachRange() {
            TsMultiRange mr = createLargeMultirange();

            for (int i = 0; i < RANGE_COUNT; i++) {
                LocalDateTime start = BASE_DATE.plusDays(i * 10L + 1);
                LocalDateTime end = start.plusDays(2);

                TsRange inner = TsRange.of(start, end, "[)");

                assertTrue(mr.containsRange(inner),
                        "Диапазон " + inner + " должен содержаться в multirange");
            }
        }

        @Test
        @DisplayName("Диапазон, пересекающий два диапазона multirange — не содержится")
        void rangeSpanningTwoRangesNotContained() {
            TsMultiRange mr = createLargeMultirange();

            // Диапазон, который пересекает первый и второй диапазоны
            TsRange spanning = TsRange.of(
                    BASE_DATE.plusDays(3),
                    BASE_DATE.plusDays(12),
                    "[)"
            );

            assertFalse(mr.containsRange(spanning));
        }

        @Test
        @DisplayName("Диапазон в разрыве — не содержится")
        void rangeInGapNotContained() {
            TsMultiRange mr = createLargeMultirange();

            TsRange gapRange = TsRange.of(
                    BASE_DATE.plusDays(6),
                    BASE_DATE.plusDays(9),
                    "[)"
            );

            assertFalse(mr.containsRange(gapRange));
        }

        @Test
        @DisplayName("Пустой диапазон содержится в любом multirange")
        void emptyRangeContainedInLargeMultirange() {
            TsMultiRange mr = createLargeMultirange();

            assertTrue(mr.containsRange(TsRange.EMPTY));
        }

        @Test
        @DisplayName("Весь multirange как один диапазон — не содержится")
        void entireMultirangeAsOneRangeNotContained() {
            TsMultiRange mr = createLargeMultirange();

            // Диапазон, охватывающий весь multirange
            TsRange covering = TsRange.of(mr.lower(), mr.upper(), "[)");

            assertFalse(mr.containsRange(covering));
        }
    }

    // ==================== containsMultirange ====================

    @Nested
    @DisplayName("containsMultirange с 15 диапазонами")
    class ContainsMultirangeTests {

        @Test
        @DisplayName("Multirange с подмножеством диапазонов содержится")
        void subsetMultirangeContained() {
            TsMultiRange mr = createLargeMultirange();

            // Создаём multirange с диапазонами из первого, третьего и пятого
            List<TsRange> subset = new ArrayList<>();
            for (int i = 0; i < RANGE_COUNT; i += 2) {
                subset.add(mr.get(i));
            }

            TsMultiRange subMr = TsMultiRange.of(subset);

            assertTrue(mr.containsMultirange(subMr));
        }

        @Test
        @DisplayName("Multirange с диапазонами в разрывах — не содержится")
        void multirangeInGapsNotContained() {
            TsMultiRange mr = createLargeMultirange();
            TsMultiRange gapMr = createGapMultirange();

            assertFalse(mr.containsMultirange(gapMr));
        }

        @Test
        @DisplayName("Сам multirange содержится в себе")
        void selfContainment() {
            TsMultiRange mr = createLargeMultirange();

            assertTrue(mr.containsMultirange(mr));
        }

        @Test
        @DisplayName("Пустой multirange содержится в большом")
        void emptyContainedInLarge() {
            TsMultiRange mr = createLargeMultirange();
            TsMultiRange empty = TsMultiRange.of(List.of());

            assertTrue(mr.containsMultirange(empty));
        }
    }

    // ==================== overlaps ====================

    @Nested
    @DisplayName("overlaps с 15 диапазонами")
    class OverlapsTests {

        @Test
        @DisplayName("Пересечение с одним диапазоном из 15")
        void overlapsWithOneRange() {
            TsMultiRange mr = createLargeMultirange();

            // Диапазон, пересекающий пятый диапазон (день 41-46)
            TsMultiRange other = TsMultiRange.of(List.of(
                    TsRange.of(
                            BASE_DATE.plusDays(42),
                            BASE_DATE.plusDays(48),
                            "[)"
                    )
            ));

            assertTrue(mr.overlaps(other));
        }

        @Test
        @DisplayName("Пересечение с несколькими диапазонами")
        void overlapsWithMultipleRanges() {
            TsMultiRange mr = createLargeMultirange();

            // Большой диапазон, пересекающий несколько диапазонов
            TsMultiRange other = TsMultiRange.of(List.of(
                    TsRange.of(
                            BASE_DATE.plusDays(3),
                            BASE_DATE.plusDays(50),
                            "[)"
                    )
            ));

            assertTrue(mr.overlaps(other));
        }

        @Test
        @DisplayName("Нет пересечения — все в разрывах")
        void noOverlapAllInGaps() {
            TsMultiRange mr = createLargeMultirange();
            TsMultiRange gapMr = createGapMultirange();

            assertFalse(mr.overlaps(gapMr));
        }

        @Test
        @DisplayName("Два больших multirange без пересечения")
        void twoLargeMultirangesNoOverlap() {
            TsMultiRange mr1 = createLargeMultirange();
            TsMultiRange mr2 = createAfterMultirange();

            assertFalse(mr1.overlaps(mr2));
        }
    }

    // ==================== union ====================

    @Nested
    @DisplayName("union с 15 диапазонами")
    class UnionTests {

        @Test
        @DisplayName("Union двух больших разнесённых multirange")
        void unionTwoLargeDisjoint() {
            TsMultiRange mr1 = createLargeMultirange();
            TsMultiRange mr2 = createGapMultirange();

            TsMultiRange result = mr1.union(mr2);

            // Все разрывы заполнены смежными диапазонами → один непрерывный диапазон
            assertEquals(1, result.size());

            // Результат охватывает весь исходный mr1
            assertEquals(mr1.lower(), result.lower());
            assertEquals(mr1.upper(), result.upper());
        }

        @Test
        @DisplayName("Union двух multirange заполняет все разрывы")
        void unionFillsAllGaps() {
            TsMultiRange mr1 = createLargeMultirange();
            TsMultiRange mr2 = createGapMultirange();

            TsMultiRange result = mr1.union(mr2);

            // Все разрывы заполнены → один непрерывный диапазон
            assertEquals(1, result.size());
            assertEquals(mr1.lower(), result.lower());
            assertEquals(mr1.upper(), result.upper());
        }

        @Test
        @DisplayName("Union с пустым — результат тот же")
        void unionWithEmpty() {
            TsMultiRange mr = createLargeMultirange();
            TsMultiRange empty = TsMultiRange.of(List.of());

            TsMultiRange result = mr.union(empty);

            assertTrue(result.isEqual(mr));
        }

        @Test
        @DisplayName("Union с самим собой — результат тот же")
        void unionWithSelf() {
            TsMultiRange mr = createLargeMultirange();

            TsMultiRange result = mr.union(mr);

            assertTrue(result.isEqual(mr));
        }
    }

    // ==================== intersection ====================

    @Nested
    @DisplayName("intersection с 15 диапазонами")
    class IntersectionTests {

        @Test
        @DisplayName("Intersection с охватывающим диапазоном — результат тот же")
        void intersectionWithCoveringRange() {
            TsMultiRange mr = createLargeMultirange();

            // Один большой диапазон, охватывающий весь multirange
            TsMultiRange covering = TsMultiRange.of(List.of(
                    TsRange.of(
                            mr.lower().minusDays(1),
                            mr.upper().plusDays(1),
                            "[)"
                    )
            ));

            TsMultiRange result = mr.intersection(covering);

            assertTrue(result.isEqual(mr));
        }

        @Test
        @DisplayName("Intersection с диапазоном, пересекающим несколько")
        void intersectionWithSpanningRange() {
            TsMultiRange mr = createLargeMultirange();

            // Диапазон, пересекающий первые 5 диапазонов
            TsMultiRange spanning = TsMultiRange.of(List.of(
                    TsRange.of(
                            BASE_DATE.plusDays(3),
                            BASE_DATE.plusDays(48),
                            "[)"
                    )
            ));

            TsMultiRange result = mr.intersection(spanning);

            // Должны получить части первых 5 диапазонов
            assertTrue(result.size() >= 4);
            assertTrue(result.size() <= 5);
        }

        @Test
        @DisplayName("Intersection с диапазоном в разрыве — пустой результат")
        void intersectionWithGapRange() {
            TsMultiRange mr = createLargeMultirange();

            TsMultiRange gapRange = TsMultiRange.of(List.of(
                    TsRange.of(
                            BASE_DATE.plusDays(6),
                            BASE_DATE.plusDays(9),
                            "[)"
                    )
            ));

            TsMultiRange result = mr.intersection(gapRange);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Intersection с самим собой — результат тот же")
        void intersectionWithSelf() {
            TsMultiRange mr = createLargeMultirange();

            TsMultiRange result = mr.intersection(mr);

            assertTrue(result.isEqual(mr));
        }

        @Test
        @DisplayName("Intersection с пустым — пустой результат")
        void intersectionWithEmpty() {
            TsMultiRange mr = createLargeMultirange();
            TsMultiRange empty = TsMultiRange.of(List.of());

            TsMultiRange result = mr.intersection(empty);

            assertTrue(result.isEmpty());
        }
    }

    // ==================== strictlyLeftOf / strictlyRightOf ====================

    @Nested
    @DisplayName("strictlyLeftOf / strictlyRightOf с 15 диапазонами")
    class StrictlyLeftRightTests {

        @Test
        @DisplayName("Большой multirange строго левее другого")
        void largeStrictlyLeftOfOther() {
            TsMultiRange mr1 = createLargeMultirange();
            TsMultiRange mr2 = createAfterMultirange();

            assertTrue(mr1.strictlyLeftOf(mr2));
            assertTrue(mr2.strictlyRightOf(mr1));
            assertFalse(mr2.strictlyLeftOf(mr1));
            assertFalse(mr1.strictlyRightOf(mr2));
        }

        @Test
        @DisplayName("Пересекающиеся multirange не строго левее/правее")
        void overlappingNotStrictlyLeftOrRight() {
            TsMultiRange mr1 = createLargeMultirange();

            // Создаём multirange, пересекающий первый
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(
                            BASE_DATE.plusDays(50),
                            BASE_DATE.plusDays(70),
                            "[)"
                    )
            ));

            assertFalse(mr1.strictlyLeftOf(mr2));
            assertFalse(mr2.strictlyLeftOf(mr1));
        }
    }

    // ==================== isAdjacentTo ====================

    @Nested
    @DisplayName("isAdjacentTo с 15 диапазонами")
    class IsAdjacentToTests {

        @Test
        @DisplayName("Большой multirange смежен с другим")
        void largeAdjacentToOther() {
            TsMultiRange mr1 = createLargeMultirange();

            // Создаём multirange, начинающийся сразу после последнего диапазона
            LocalDateTime lastEnd = mr1.upper();
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(lastEnd, lastEnd.plusDays(10), "[)")
            ));

            assertTrue(mr1.isAdjacentTo(mr2));
            assertTrue(mr2.isAdjacentTo(mr1));
        }

        @Test
        @DisplayName("Большой multirange не смежен с разнесённым")
        void largeNotAdjacentToDisjoint() {
            TsMultiRange mr1 = createLargeMultirange();

            // Создаём multirange с разрывом после последнего диапазона
            LocalDateTime lastEnd = mr1.upper();
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(lastEnd.plusDays(5), lastEnd.plusDays(15), "[)")
            ));

            assertFalse(mr1.isAdjacentTo(mr2));
        }
    }

    // ==================== merge ====================

    @Nested
    @DisplayName("merge с 15 диапазонами")
    class MergeTests {

        @Test
        @DisplayName("merge возвращает охватывающий диапазон")
        void mergeReturnsConvexHull() {
            TsMultiRange mr = createLargeMultirange();

            TsRange merged = mr.merge();

            assertEquals(mr.lower(), merged.lower());
            assertEquals(mr.upper(), merged.upper());
            assertEquals(mr.lowerInc(), merged.lowerInc());
            assertEquals(mr.upperInc(), merged.upperInc());
        }

        @Test
        @DisplayName("merge охватывает все 15 диапазонов")
        void mergeCoversAllRanges() {
            TsMultiRange mr = createLargeMultirange();

            TsRange merged = mr.merge();

            // Каждый диапазон должен содержаться в merged
            for (int i = 0; i < mr.size(); i++) {
                assertTrue(merged.containsRange(mr.get(i)),
                        "Диапазон " + i + " должен содержаться в merged");
            }
        }
    }

    // ==================== isEqual ====================

    @Nested
    @DisplayName("isEqual с 15 диапазонами")
    class IsEqualTests {

        @Test
        @DisplayName("Два идентичных больших multirange равны")
        void twoIdenticalLargeMultirangesEqual() {
            TsMultiRange mr1 = createLargeMultirange();
            TsMultiRange mr2 = createLargeMultirange();

            assertTrue(mr1.isEqual(mr2));
        }

        @Test
        @DisplayName("Большие multirange с одним изменённым диапазоном не равны")
        void largeMultirangesWithOneDifferenceNotEqual() {
            TsMultiRange mr1 = createLargeMultirange();

            // Создаём копию с изменённым последним диапазоном
            List<TsRange> ranges = new ArrayList<>();
            for (int i = 0; i < RANGE_COUNT - 1; i++) {
                ranges.add(mr1.get(i));
            }
            // Изменяем последний диапазон
            ranges.add(TsRange.of(
                    BASE_DATE.plusDays(200),
                    BASE_DATE.plusDays(205),
                    "[)"
            ));

            TsMultiRange mr2 = TsMultiRange.of(ranges);

            assertFalse(mr1.isEqual(mr2));
        }
    }

    // ==================== lessThan ====================

    @Nested
    @DisplayName("lessThan с 15 диапазонами")
    class LessThanTests {

        @Test
        @DisplayName("Большой multirange меньше другого с более поздними диапазонами")
        void largeLessThanLaterMultirange() {
            TsMultiRange mr1 = createLargeMultirange();
            TsMultiRange mr2 = createAfterMultirange();

            assertTrue(mr1.lessThan(mr2));
            assertFalse(mr2.lessThan(mr1));
        }

        @Test
        @DisplayName("Multirange с префиксом меньше полного")
        void prefixLessThanFull() {
            TsMultiRange full = createLargeMultirange();

            // Создаём multirange с первыми 10 диапазонами
            List<TsRange> prefix = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                prefix.add(full.get(i));
            }

            TsMultiRange prefixMr = TsMultiRange.of(prefix);

            assertTrue(prefixMr.lessThan(full));
            assertFalse(full.lessThan(prefixMr));
        }
    }

    // ==================== toString ====================

    @Nested
    @DisplayName("toString с 15 диапазонами")
    class ToStringTests {

        @Test
        @DisplayName("toString содержит все диапазоны")
        void toStringContainsAllRanges() {
            TsMultiRange mr = createLargeMultirange();

            String str = mr.toString();

            assertTrue(str.startsWith("{"));
            assertTrue(str.endsWith("}"));

            // Проверяем, что все диапазоны присутствуют
            for (int i = 0; i < mr.size(); i++) {
                assertTrue(str.contains(mr.get(i).toString()),
                        "toString должен содержать диапазон " + i);
            }
        }
    }

    // ==================== iterator ====================

    @Nested
    @DisplayName("iterator с 15 диапазонами")
    class IteratorTests {

        @Test
        @DisplayName("Итератор проходит по всем 15 диапазонам")
        void iteratorGoesThroughAllRanges() {
            TsMultiRange mr = createLargeMultirange();

            int count = 0;
            for (TsRange range : mr) {
                assertNotNull(range);
                assertFalse(range.isEmpty());
                count++;
            }

            assertEquals(RANGE_COUNT, count);
        }

        @Test
        @DisplayName("Итератор возвращает диапазоны в порядке")
        void iteratorReturnsRangesInOrder() {
            TsMultiRange mr = createLargeMultirange();

            TsRange previous = null;
            for (TsRange range : mr) {
                if (previous != null) {
                    assertTrue(previous.strictlyLeftOf(range),
                            "Диапазоны должны быть в порядке возрастания");
                }
                previous = range;
            }
        }
    }

    // ==================== getFirst / getLast / get ====================

    @Nested
    @DisplayName("getFirst / getLast / get с 15 диапазонами")
    class AccessorTests {

        @Test
        @DisplayName("getFirst возвращает первый диапазон")
        void getFirstReturnsFirstRange() {
            TsMultiRange mr = createLargeMultirange();

            TsRange first = mr.getFirst();

            assertEquals(BASE_DATE, first.lower());
        }

        @Test
        @DisplayName("getLast возвращает последний диапазон")
        void getLastReturnsLastRange() {
            TsMultiRange mr = createLargeMultirange();

            TsRange last = mr.getLast();

            LocalDateTime expectedStart = BASE_DATE.plusDays((RANGE_COUNT - 1) * 10L);
            assertEquals(expectedStart, last.lower());
        }

        @Test
        @DisplayName("get возвращает диапазон по индексу")
        void getReturnsRangeByIndex() {
            TsMultiRange mr = createLargeMultirange();

            for (int i = 0; i < RANGE_COUNT; i++) {
                TsRange range = mr.get(i);
                LocalDateTime expectedStart = BASE_DATE.plusDays(i * 10L);
                assertEquals(expectedStart, range.lower());
            }
        }

        @Test
        @DisplayName("get с невалидным индексом бросает исключение")
        void getWithInvalidIndexThrows() {
            TsMultiRange mr = createLargeMultirange();

            assertThrows(IndexOutOfBoundsException.class, () -> mr.get(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> mr.get(RANGE_COUNT));
        }
    }

    // ==================== size ====================

    @Nested
    @DisplayName("size с 15 диапазонами")
    class SizeTests {

        @Test
        @DisplayName("size возвращает 15")
        void sizeReturnsFifteen() {
            TsMultiRange mr = createLargeMultirange();

            assertEquals(RANGE_COUNT, mr.size());
        }

        @Test
        @DisplayName("size после нормализации пересекающихся")
        void sizeAfterNormalizationOfOverlapping() {
            TsMultiRange mr = createOverlappingMultirange();

            assertEquals(1, mr.size());
        }

        @Test
        @DisplayName("size после нормализации смежных")
        void sizeAfterNormalizationOfAdjacent() {
            TsMultiRange mr = createAdjacentMultirange();

            assertEquals(1, mr.size());
        }
    }
}