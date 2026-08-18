package ru.nspk.pcl.common.tsrange;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("difference() тесты для TsMultiRange с 10 элементами")
class TsMultiRangeDifferenceLargeTest {

    private static final int RANGE_COUNT = 10;
    private static final LocalDateTime BASE_DATE = LocalDateTime.of(2026, 1, 1, 0, 0);

    /**
     * Создаёт multirange с 10 разнесёнными диапазонами:
     * Каждый диапазон: 5 дней, разрыв 5 дней
     * [день 0, день 5), [день 10, день 15), [день 20, день 25), ...
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
     * Создаёт multirange с диапазонами в разрывах большого multirange.
     * Заполняет разрывы между диапазонами.
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
     * Создаёт multirange с одним большим диапазоном, охватывающим всё
     */
    private TsMultiRange createCoveringMultirange() {
        return TsMultiRange.of(List.of(
                TsRange.of(
                        BASE_DATE.minusDays(1),
                        BASE_DATE.plusDays(RANGE_COUNT * 10L + 1),
                        "[)"
                )
        ));
    }

    // ==================== ВЫРЕЗАНИЕ СЕРЕДИНЫ ====================

    @Nested
    @DisplayName("Вырезание середины из одного диапазона")
    class CutMiddleTests {

        @Test
        @DisplayName("Вычитание из середины первого диапазона даёт 2 куска")
        void cutMiddleOfFirstRange() {
            TsMultiRange mr1 = createLargeMultirange();

            // Вычитаем середину первого диапазона [день 0, день 5)
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(1), BASE_DATE.plusDays(3), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            // Было 10 диапазонов, первый разрезан на 2 куска → 11 диапазонов
            assertEquals(RANGE_COUNT + 1, result.size());

            // Проверяем первые два куска (разрезанный первый диапазон)
            assertEquals(BASE_DATE, result.get(0).lower());
            assertEquals(BASE_DATE.plusDays(1), result.get(0).upper());

            assertEquals(BASE_DATE.plusDays(3), result.get(1).lower());
            assertEquals(BASE_DATE.plusDays(5), result.get(1).upper());
        }

        @Test
        @DisplayName("Вычитание из середины последнего диапазона")
        void cutMiddleOfLastRange() {
            TsMultiRange mr1 = createLargeMultirange();

            // Вычитаем середину последнего диапазона
            int lastStart = (RANGE_COUNT - 1) * 10;
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(
                            BASE_DATE.plusDays(lastStart + 1),
                            BASE_DATE.plusDays(lastStart + 3),
                            "[)"
                    )
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(RANGE_COUNT + 1, result.size());
        }

        @Test
        @DisplayName("Вычитание из середины среднего диапазона")
        void cutMiddleOfMiddleRange() {
            TsMultiRange mr1 = createLargeMultirange();

            // Вычитаем середину пятого диапазона (индекс 4)
            int middleStart = 4 * 10;
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(
                            BASE_DATE.plusDays(middleStart + 1),
                            BASE_DATE.plusDays(middleStart + 3),
                            "[)"
                    )
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(RANGE_COUNT + 1, result.size());
        }
    }

    // ==================== ВЫЧИТАНИЕ ИЗ НЕСКОЛЬКИХ ДИАПАЗОНОВ ====================

    @Nested
    @DisplayName("Вычитание из нескольких диапазонов")
    class CutMultipleTests {

        @Test
        @DisplayName("Один большой диапазон вырезает куски из нескольких диапазонов")
        void oneBigRangeCutsMultiple() {
            TsMultiRange mr1 = createLargeMultirange();

            // Один диапазон, пересекающий первые 3 диапазона из mr1
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(2), BASE_DATE.plusDays(22), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            // Должны остаться:
            // [день 0, день 2) — левый кусок первого диапазона
            // [день 22, день 25) — правый кусок третьего диапазона
            // [день 30, день 35), ..., [день 90, день 95) — остальные 7 диапазонов
            // Итого: 1 + 1 + 7 = 9 диапазонов
            assertEquals(9, result.size());

            // Проверяем первый кусок
            assertEquals(BASE_DATE, result.get(0).lower());
            assertEquals(BASE_DATE.plusDays(2), result.get(0).upper());

            // Проверяем второй кусок (правая часть третьего диапазона)
            assertEquals(BASE_DATE.plusDays(22), result.get(1).lower());
            assertEquals(BASE_DATE.plusDays(25), result.get(1).upper());

            // Проверяем, что второй диапазон полностью поглощён (его нет в результате)
            // Третий кусок должен быть четвёртым диапазоном из исходного
            assertEquals(BASE_DATE.plusDays(30), result.get(2).lower());
            assertEquals(BASE_DATE.plusDays(35), result.get(2).upper());
        }

        @Test
        @DisplayName("Два разнесённых диапазона вычитают из разных частей")
        void twoDisjointRangesCutDifferentParts() {
            TsMultiRange mr1 = createLargeMultirange();

            // Два диапазона, вычитающие из разных частей
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(1), BASE_DATE.plusDays(3), "[)"),
                    TsRange.of(BASE_DATE.plusDays(51), BASE_DATE.plusDays(53), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            // Два диапазона разрезаны → 10 + 2 = 12 диапазонов
            assertEquals(RANGE_COUNT + 2, result.size());
        }

        @Test
        @DisplayName("Multirange с 5 диапазонами вычитает из 10 диапазонов")
        void fiveRangesCutFromTen() {
            TsMultiRange mr1 = createLargeMultirange();

            // Создаём 5 диапазонов, каждый вычитает из своего диапазона в mr1
            List<TsRange> subtractRanges = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                int start = i * 2 * 10 + 1; // дни 1, 21, 41, 61, 81
                subtractRanges.add(TsRange.of(
                        BASE_DATE.plusDays(start),
                        BASE_DATE.plusDays(start + 2),
                        "[)"
                ));
            }

            TsMultiRange mr2 = TsMultiRange.of(subtractRanges);

            TsMultiRange result = mr1.difference(mr2);

            // 5 диапазонов разрезаны → 10 + 5 = 15 диапазонов
            assertEquals(RANGE_COUNT + 5, result.size());
        }
    }

    // ==================== ПОЛНОЕ ПОГЛОЩЕНИЕ ====================

    @Nested
    @DisplayName("Полное поглощение")
    class FullAbsorptionTests {

        @Test
        @DisplayName("Большой охватывающий диапазон поглощает весь multirange")
        void coveringRangeAbsorbsAll() {
            TsMultiRange mr1 = createLargeMultirange();
            TsMultiRange mr2 = createCoveringMultirange();

            TsMultiRange result = mr1.difference(mr2);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Вычитание самого себя даёт пустой результат")
        void subtractSelfGivesEmpty() {
            TsMultiRange mr1 = createLargeMultirange();

            TsMultiRange result = mr1.difference(mr1);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Вычитание multirange с разрывами, заполняющими все разрывы первого")
        void subtractGapFillerGivesOnlyGaps() {
            TsMultiRange mr1 = createLargeMultirange();
            TsMultiRange mr2 = createGapMultirange();

            TsMultiRange result = mr1.difference(mr2);

            // mr2 находится в разрывах mr1, не пересекается с ним
            // Результат должен быть тем же, что и mr1
            assertTrue(result.isEqual(mr1));
        }
    }

    // ==================== НЕТ ПЕРЕСЕЧЕНИЯ ====================

    @Nested
    @DisplayName("Нет пересечения")
    class NoOverlapTests {

        @Test
        @DisplayName("Вычитание разнесённого multirange не меняет результат")
        void subtractDisjointReturnsSame() {
            TsMultiRange mr1 = createLargeMultirange();

            // Создаём multirange полностью после mr1
            List<TsRange> ranges = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                LocalDateTime start = BASE_DATE.plusDays(200 + i * 10L);
                ranges.add(TsRange.of(start, start.plusDays(5), "[)"));
            }
            TsMultiRange mr2 = TsMultiRange.of(ranges);

            TsMultiRange result = mr1.difference(mr2);

            assertTrue(result.isEqual(mr1));
        }

        @Test
        @DisplayName("Вычитание multirange в разрывах не меняет результат")
        void subtractGapRangesReturnsSame() {
            TsMultiRange mr1 = createLargeMultirange();
            TsMultiRange mr2 = createGapMultirange();

            TsMultiRange result = mr1.difference(mr2);

            assertTrue(result.isEqual(mr1));
        }

        @Test
        @DisplayName("Вычитание смежного диапазона не меняет результат")
        void subtractAdjacentReturnsSame() {
            TsMultiRange mr1 = createLargeMultirange();

            // Смежный диапазон сразу после последнего
            LocalDateTime lastEnd = mr1.upper();
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(lastEnd, lastEnd.plusDays(5), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertTrue(result.isEqual(mr1));
        }
    }

    // ==================== ПУСТЫЕ MULTIRANGE ====================

    @Nested
    @DisplayName("Пустые multirange")
    class EmptyTests {

        @Test
        @DisplayName("Пустой - непустой = пустой")
        void emptyMinusNonEmpty() {
            TsMultiRange empty = TsMultiRange.of(List.of());
            TsMultiRange nonEmpty = createLargeMultirange();

            TsMultiRange result = empty.difference(nonEmpty);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Непустой - пустой = непустой")
        void nonEmptyMinusEmpty() {
            TsMultiRange nonEmpty = createLargeMultirange();
            TsMultiRange empty = TsMultiRange.of(List.of());

            TsMultiRange result = nonEmpty.difference(empty);

            assertTrue(result.isEqual(nonEmpty));
        }

        @Test
        @DisplayName("Пустой - пустой = пустой")
        void emptyMinusEmpty() {
            TsMultiRange empty1 = TsMultiRange.of(List.of());
            TsMultiRange empty2 = TsMultiRange.of(List.of());

            TsMultiRange result = empty1.difference(empty2);

            assertTrue(result.isEmpty());
        }
    }

    // ==================== РАЗНЫЕ ВКЛЮЧИТЕЛЬНОСТИ ГРАНИЦ ====================

    @Nested
    @DisplayName("Разные включительности границ")
    class InclusivityTests {

        @Test
        @DisplayName("Вычитание с включающей нижней границей вычитаемого")
        void subtractWithInclusiveLower() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(10), "[)")
            ));

            // Вычитаем с включающей нижней границей
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(3), BASE_DATE.plusDays(7), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(2, result.size());
            // Левый кусок: [день 0, день 3) — верхняя граница исключающая
            assertEquals(BASE_DATE, result.get(0).lower());
            assertEquals(BASE_DATE.plusDays(3), result.get(0).upper());
            assertFalse(result.get(0).upperInc());

            // Правый кусок: [день 7, день 10) — нижняя граница исключающая
            assertEquals(BASE_DATE.plusDays(7), result.get(1).lower());
            assertTrue(result.get(1).lowerInc());
        }

        @Test
        @DisplayName("Вычитание с исключающей нижней границей вычитаемого")
        void subtractWithExclusiveLower() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(10), "[)")
            ));

            // Вычитаем с исключающей нижней границей
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(3), BASE_DATE.plusDays(7), "()")
            ));

            TsMultiRange result = mr1.difference(mr2);

            assertEquals(2, result.size());
            // Левый кусок должен включать точку день 3, потому что mr2 её не включает
            assertEquals(BASE_DATE.plusDays(3), result.get(0).upper());
            assertTrue(result.get(0).upperInc());
        }

        @Test
        @DisplayName("Вычитание с исключающей верхней границей вычитаемого")
        void subtractWithExclusiveUpper() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(10), "[)")
            ));

            // Вычитаем с исключающей верхней границей
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(3), BASE_DATE.plusDays(7), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            // Правый кусок должен включать точку день 7, потому что mr2 её не включает
            assertEquals(BASE_DATE.plusDays(7), result.get(1).lower());
            assertTrue(result.get(1).lowerInc());
        }
    }

    // ==================== ЧАСТИЧНОЕ ВЫЧИТАНИЕ ====================

    @Nested
    @DisplayName("Частичное вычитание (левый и правый край)")
    class PartialSubtractionTests {

        @Test
        @DisplayName("Вычитание левой части диапазона")
        void subtractLeftPart() {
            TsMultiRange mr1 = createLargeMultirange();

            // Вычитаем левую часть первого диапазона
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.minusDays(2), BASE_DATE.plusDays(2), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            // Первый диапазон обрезан слева: [день 2, день 5)
            assertEquals(RANGE_COUNT, result.size());
            assertEquals(BASE_DATE.plusDays(2), result.get(0).lower());
            assertEquals(BASE_DATE.plusDays(5), result.get(0).upper());
        }

        @Test
        @DisplayName("Вычитание правой части диапазона")
        void subtractRightPart() {
            TsMultiRange mr1 = createLargeMultirange();

            // Вычитаем правую часть первого диапазона
            TsMultiRange mr2 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(3), BASE_DATE.plusDays(10), "[)")
            ));

            TsMultiRange result = mr1.difference(mr2);

            // Первый диапазон обрезан справа: [день 0, день 3)
            assertEquals(RANGE_COUNT, result.size());
            assertEquals(BASE_DATE, result.get(0).lower());
            assertEquals(BASE_DATE.plusDays(3), result.get(0).upper());
        }
    }

    // ==================== СВЯЗЬ С ДРУГИМИ ОПЕРАТОРАМИ ====================

    @Nested
    @DisplayName("Связь с другими операторами")
    class RelationshipTests {

        @Test
        @DisplayName("(a - b) + (a * b) == a")
        void differencePlusIntersectionEqualsOriginal() {
            TsMultiRange a = createLargeMultirange();

            // b пересекает часть a
            TsMultiRange b = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(3), BASE_DATE.plusDays(33), "[)")
            ));

            TsMultiRange diff = a.difference(b);
            TsMultiRange inter = a.intersection(b);
            TsMultiRange union = diff.union(inter);

            assertTrue(a.isEqual(union));
        }

        @Test
        @DisplayName("Если a и b не пересекаются, то a - b == a")
        void noOverlapMeansNoDifference() {
            TsMultiRange a = createLargeMultirange();

            // b находится в разрывах a
            TsMultiRange b = createGapMultirange();

            TsMultiRange diff = a.difference(b);

            assertTrue(diff.isEqual(a));
        }

        @Test
        @DisplayName("Если b содержит a, то a - b == empty")
        void containmentMeansEmptyDifference() {
            TsMultiRange a = createLargeMultirange();
            TsMultiRange b = createCoveringMultirange();

            assertTrue(b.containsMultirange(a));
            assertTrue(a.difference(b).isEmpty());
        }

        @Test
        @DisplayName("a - a == empty")
        void selfDifferenceIsEmpty() {
            TsMultiRange a = createLargeMultirange();

            assertTrue(a.difference(a).isEmpty());
        }
    }

    // ==================== КОММУТАТИВНОСТЬ И АССОЦИАТИВНОСТЬ ====================

    @Nested
    @DisplayName("Математические свойства")
    class MathematicalPropertyTests {

        @Test
        @DisplayName("difference НЕ коммутативна: a - b != b - a")
        void differenceNotCommutative() {
            TsMultiRange a = createLargeMultirange();

            TsMultiRange b = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(3), BASE_DATE.plusDays(33), "[)")
            ));

            TsMultiRange aMinusB = a.difference(b);
            TsMultiRange bMinusA = b.difference(a);

            assertFalse(aMinusB.isEqual(bMinusA));
        }

        @Test
        @DisplayName("difference НЕ ассоциативна: (a-b)-c != a-(b-c) в общем случае")
        void differenceNotAssociative() {
            // Создаём три multirange, где ассоциативность нарушается
            TsMultiRange a = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(30), "[)")
            ));

            TsMultiRange b = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(5), BASE_DATE.plusDays(25), "[)")
            ));

            TsMultiRange c = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE.plusDays(10), BASE_DATE.plusDays(20), "[)")
            ));

            TsMultiRange leftAssoc = a.difference(b).difference(c);
            TsMultiRange rightAssoc = a.difference(b.difference(c));

            // (a-b) = [0,5) + [25,30)
            // (a-b)-c = [0,5) + [25,30) (c не пересекается)

            // (b-c) = [5,10) + [20,25)
            // a-(b-c) = [0,5) + [10,20) + [25,30)

            // Результаты разные!
            assertFalse(leftAssoc.isEqual(rightAssoc));
        }
    }

    // ==================== СЛОЖНЫЕ СЛУЧАИ ====================

    @Nested
    @DisplayName("Сложные случаи")
    class ComplexCasesTests {

        @Test
        @DisplayName("Вычитание из 10 диапазонов multirange с 10 пересекающимися диапазонами")
        void tenMinusTenOverlapping() {
            TsMultiRange mr1 = createLargeMultirange();

            // Создаём 10 диапазонов, каждый пересекает соответствующий диапазон из mr1
            List<TsRange> ranges = new ArrayList<>();
            for (int i = 0; i < RANGE_COUNT; i++) {
                LocalDateTime start = BASE_DATE.plusDays(i * 10L + 2);
                LocalDateTime end = start.plusDays(5);
                ranges.add(TsRange.of(start, end, "[)"));
            }
            TsMultiRange mr2 = TsMultiRange.of(ranges);

            TsMultiRange result = mr1.difference(mr2);

            // Каждый диапазон из mr1 обрезан слева → остаётся 10 кусков
            assertEquals(RANGE_COUNT, result.size());

            // Проверяем, что каждый кусок имеет правильные границы
            for (int i = 0; i < RANGE_COUNT; i++) {
                LocalDateTime expectedLower = BASE_DATE.plusDays(i * 10L);
                LocalDateTime expectedUpper = BASE_DATE.plusDays(i * 10L + 2);

                assertEquals(expectedLower, result.get(i).lower());
                assertEquals(expectedUpper, result.get(i).upper());
            }
        }

        @Test
        @DisplayName("Вычитание, создающее много маленьких кусков")
        void subtractionCreatesManySmallPieces() {
            TsMultiRange mr1 = TsMultiRange.of(List.of(
                    TsRange.of(BASE_DATE, BASE_DATE.plusDays(100), "[)")
            ));

            // Создаём 10 маленьких диапазонов, вырезающих дырки
            List<TsRange> holes = new ArrayList<>();
            for (int i = 0; i < RANGE_COUNT; i++) {
                LocalDateTime start = BASE_DATE.plusDays(i * 10L + 3);
                LocalDateTime end = start.plusDays(2);
                holes.add(TsRange.of(start, end, "[)"));
            }
            TsMultiRange mr2 = TsMultiRange.of(holes);

            TsMultiRange result = mr1.difference(mr2);

            // 10 дырок разрезают один диапазон на 11 кусков
            assertEquals(RANGE_COUNT + 1, result.size());
        }
    }
}