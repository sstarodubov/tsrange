package ru.nspk.pcl.common.tsrange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class TsMultiRangeTest {

    // ===== Вспомогательные методы для читаемости тестов =====

    private static LocalDateTime dt(int year, int month, int day) {
        return LocalDateTime.of(year, month, day, 0, 0);
    }

    private static TsRange range(LocalDateTime lower, LocalDateTime upper) {
        return TsRange.of(lower, upper); // [lower, upper)
    }

    private static TsRange range(LocalDateTime lower, LocalDateTime upper, String bounds) {
        return TsRange.of(lower, upper, bounds);
    }

    private static TsMultiRange multirange(TsRange... ranges) {
        return TsMultiRange.of(List.of(ranges));
    }

    private static TsMultiRange mr(TsRange... ranges) {
        return TsMultiRange.of(List.of(ranges));
    }

    @Nested
    class TsMultiRangeIsContainedByTest {

        // -------------------------------------------------------
        // Положительные случаи
        // -------------------------------------------------------

        @Test
        @DisplayName("Мультисписок содержится в самом себе")
        void containedByItself() {
            TsMultiRange a = mr(
                    range(dt(2024, 1, 1), dt(2024, 1, 10)),
                    range(dt(2024, 3, 1), dt(2024, 3, 15))
            );
            assertThat(a.isContainedBy(a)).isTrue();
        }

        @Test
        @DisplayName("Подмножество поддиапазонов содержится в надмножестве")
        void subsetContainedInSuperset() {
            TsMultiRange small = mr(
                    range(dt(2024, 3, 1), dt(2024, 3, 10))
            );
            TsMultiRange big = mr(
                    range(dt(2024, 1, 1), dt(2024, 1, 31)),
                    range(dt(2024, 3, 1), dt(2024, 3, 31)),
                    range(dt(2024, 6, 1), dt(2024, 6, 30))
            );
            assertThat(small.isContainedBy(big)).isTrue();
        }

        @Test
        @DisplayName("Поддиапазон уже, чем покрывающий его диапазон")
        void narrowerRangeContained() {
            TsMultiRange small = mr(
                    range(dt(2024, 1, 5), dt(2024, 1, 15))
            );
            TsMultiRange big = mr(
                    range(dt(2024, 1, 1), dt(2024, 1, 31))
            );
            assertThat(small.isContainedBy(big)).isTrue();
        }

        @Test
        @DisplayName("Пустой мультисписок содержится в любом")
        void emptyContainedInAnything() {
            TsMultiRange empty = TsMultiRange.of(Collections.emptyList());
            TsMultiRange nonEmpty = mr(range(dt(2024, 1, 1), dt(2024, 1, 10)));
            assertThat(empty.isContainedBy(nonEmpty)).isTrue();
        }

        @Test
        @DisplayName("Пустой содержится в пустом")
        void emptyContainedInEmpty() {
            TsMultiRange empty1 = TsMultiRange.of(Collections.emptyList());
            TsMultiRange empty2 = TsMultiRange.of(Collections.emptyList());
            assertThat(empty1.isContainedBy(empty2)).isTrue();
        }

        @Test
        @DisplayName("Несколько поддиапазонов, каждый в своём покрывающем")
        void multipleSubrangesEachCovered() {
            TsMultiRange small = mr(
                    range(dt(2024, 1, 5), dt(2024, 1, 15)),
                    range(dt(2024, 6, 5), dt(2024, 6, 15))
            );
            TsMultiRange big = mr(
                    range(dt(2024, 1, 1), dt(2024, 1, 31)),
                    range(dt(2024, 6, 1), dt(2024, 6, 30))
            );
            assertThat(small.isContainedBy(big)).isTrue();
        }

        // -------------------------------------------------------
        // Отрицательные случаи
        // -------------------------------------------------------

        @Test
        @DisplayName("Поддиапазон попадает в дырку")
        void subrangeFallsInGap() {
            TsMultiRange small = mr(
                    range(dt(2024, 2, 1), dt(2024, 2, 10))  // дырка между Jan и Mar
            );
            TsMultiRange big = mr(
                    range(dt(2024, 1, 1), dt(2024, 1, 31)),
                    range(dt(2024, 3, 1), dt(2024, 3, 31))
            );
            assertThat(small.isContainedBy(big)).isFalse();
        }

        @Test
        @DisplayName("Поддиапазон шире покрывающего")
        void subrangeWiderThanCovering() {
            TsMultiRange small = mr(
                    range(dt(2024, 1, 1), dt(2024, 2, 15))  // вылезает за Jan 31
            );
            TsMultiRange big = mr(
                    range(dt(2024, 1, 1), dt(2024, 1, 31))
            );
            assertThat(small.isContainedBy(big)).isFalse();
        }

        @Test
        @DisplayName("Один из поддиапазонов не содержится")
        void oneSubrangeNotContained() {
            TsMultiRange small = mr(
                    range(dt(2024, 1, 5), dt(2024, 1, 15)),  // содержится
                    range(dt(2024, 9, 1), dt(2024, 9, 10))   // НЕ содержится
            );
            TsMultiRange big = mr(
                    range(dt(2024, 1, 1), dt(2024, 1, 31))
            );
            assertThat(small.isContainedBy(big)).isFalse();
        }

        @Test
        @DisplayName("Непустой мультисписок не содержится в пустом")
        void nonEmptyNotContainedInEmpty() {
            TsMultiRange nonEmpty = mr(range(dt(2024, 1, 1), dt(2024, 1, 10)));
            TsMultiRange empty = TsMultiRange.of(Collections.emptyList());
            assertThat(nonEmpty.isContainedBy(empty)).isFalse();
        }

        @Test
        @DisplayName("Больший мультисписок не содержится в меньшем")
        void supersetNotContainedInSubset() {
            TsMultiRange big = mr(
                    range(dt(2024, 1, 1), dt(2024, 1, 31)),
                    range(dt(2024, 6, 1), dt(2024, 6, 30))
            );
            TsMultiRange small = mr(
                    range(dt(2024, 1, 1), dt(2024, 1, 31))
            );
            assertThat(big.isContainedBy(small)).isFalse();
        }

        // -------------------------------------------------------
        // Граничные случаи с границами
        // -------------------------------------------------------

        @Test
        @DisplayName("Границы совпадают: оба исключают верхнюю")
        void matchingExclusiveUpperBounds() {
            TsMultiRange small = mr(range(dt(2024, 1, 1), dt(2024, 1, 31)));
            TsMultiRange big   = mr(range(dt(2024, 1, 1), dt(2024, 1, 31)));
            assertThat(small.isContainedBy(big)).isTrue();
        }

        @Test
        @DisplayName("small включает точку, которую big исключает")
        void smallIncludesPointBigExcludes() {
            // small = [Jan 1, Jan 31]  (включает Jan 31)
            // big   = [Jan 1, Jan 31)  (исключает Jan 31)
            TsMultiRange small = mr(TsRange.of(dt(2024, 1, 1), dt(2024, 1, 31), "[]"));
            TsMultiRange big   = mr(TsRange.of(dt(2024, 1, 1), dt(2024, 1, 31), "[)"));
            assertThat(small.isContainedBy(big)).isFalse();
        }

        // -------------------------------------------------------
        // Симметрия с containsMultirange
        // -------------------------------------------------------

        @Test
        @DisplayName("isContainedBy симметричен containsMultirange")
        void symmetryWithContainsMultirange() {
            TsMultiRange a = mr(range(dt(2024, 1, 5), dt(2024, 1, 15)));
            TsMultiRange b = mr(range(dt(2024, 1, 1), dt(2024, 1, 31)));

            // a <@ b  ≡  b @> a
            assertThat(a.isContainedBy(b)).isTrue();
            assertThat(b.containsMultirange(a)).isTrue();

            // b <@ a  ≡  a @> b
            assertThat(b.isContainedBy(a)).isFalse();
            assertThat(a.containsMultirange(b)).isFalse();
        }

        // -------------------------------------------------------
        // null
        // -------------------------------------------------------

        @Test
        @DisplayName("null бросает IllegalArgumentException")
        void nullThrows() {
            TsMultiRange a = mr(range(dt(2024, 1, 1), dt(2024, 1, 10)));
            assertThatThrownBy(() -> a.isContainedBy((TsMultiRange) null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
    // ============================================================
    // 1. ТЕСТЫ НОРМАЛИЗАЦИИ (normalize)
    // ============================================================

    @Nested
    @DisplayName("Нормализация мультисписка")
    class NormalizeTests {

        @Test
        @DisplayName("Пустой список остаётся пустым")
        void emptyListRemainsEmpty() {
            TsMultiRange mr = TsMultiRange.of(Collections.emptyList());
            assertThat(mr.isEmpty()).isTrue();
            assertThat(mr.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Один диапазон остаётся как есть")
        void singleRangeRemains() {
            TsRange r = range(dt(2024, 1, 1), dt(2024, 1, 10));
            TsMultiRange mr = multirange(r);

            assertThat(mr.size()).isEqualTo(1);
            assertThat(mr.get(0)).isEqualTo(r);
        }

        @Test
        @DisplayName("Непересекающиеся диапазоны сохраняются и сортируются")
        void nonOverlappingRangesSorted() {
            TsRange r1 = range(dt(2024, 3, 1), dt(2024, 3, 10));
            TsRange r2 = range(dt(2024, 1, 1), dt(2024, 1, 10));
            TsRange r3 = range(dt(2024, 2, 1), dt(2024, 2, 10));

            TsMultiRange mr = TsMultiRange.of(List.of(r1, r2, r3));

            assertThat(mr.size()).isEqualTo(3);
            assertThat(mr.get(0)).isEqualTo(r2);
            assertThat(mr.get(1)).isEqualTo(r3);
            assertThat(mr.get(2)).isEqualTo(r1);
        }

        @Test
        @DisplayName("Пересекающиеся диапазоны схлопываются")
        void overlappingRangesCoalesce() {
            TsRange r1 = range(dt(2024, 1, 1), dt(2024, 1, 15));
            TsRange r2 = range(dt(2024, 1, 10), dt(2024, 1, 25));

            TsMultiRange mr = TsMultiRange.of(List.of(r1, r2));

            assertThat(mr.size()).isEqualTo(1);
            assertThat(mr.get(0).lower()).isEqualTo(dt(2024, 1, 1));
            assertThat(mr.get(0).upper()).isEqualTo(dt(2024, 1, 25));
        }

        @Test
        @DisplayName("Смежные диапазоны схлопываются")
        void adjacentRangesCoalesce() {
            TsRange r1 = range(dt(2024, 1, 1), dt(2024, 1, 10)); // [1, 10)
            TsRange r2 = range(dt(2024, 1, 10), dt(2024, 1, 20)); // [10, 20)

            TsMultiRange mr = TsMultiRange.of(List.of(r1, r2));

            assertThat(mr.size()).isEqualTo(1);
            assertThat(mr.get(0).lower()).isEqualTo(dt(2024, 1, 1));
            assertThat(mr.get(0).upper()).isEqualTo(dt(2024, 1, 20));
        }

        @Test
        @DisplayName("Пустые диапазоны удаляются")
        void emptyRangesRemoved() {
            TsRange r1 = range(dt(2024, 1, 1), dt(2024, 1, 10));
            TsRange empty = range(dt(2024, 5, 5), dt(2024, 5, 5)); // [x, x) -> empty

            TsMultiRange mr = TsMultiRange.of(List.of(r1, empty));

            assertThat(mr.size()).isEqualTo(1);
            assertThat(mr.get(0)).isEqualTo(r1);
        }

        @Test
        @DisplayName("Сложный случай: пересечения + смежность + пустые")
        void complexNormalization() {
            TsRange r1 = range(dt(2024, 1, 1), dt(2024, 1, 10));
            TsRange r2 = range(dt(2024, 1, 8), dt(2024, 1, 20));  // пересекается с r1
            TsRange r3 = range(dt(2024, 1, 20), dt(2024, 1, 30));  // смежный с r2
            TsRange empty = range(dt(2024, 2, 1), dt(2024, 2, 1)); // пустой
            TsRange r4 = range(dt(2024, 3, 1), dt(2024, 3, 10));   // отдельный

            TsMultiRange mr = TsMultiRange.of(List.of(r1, r2, r3, empty, r4));

            assertThat(mr.size()).isEqualTo(2);
            // Первый: [2024-01-01, 2024-01-30)
            assertThat(mr.get(0).lower()).isEqualTo(dt(2024, 1, 1));
            assertThat(mr.get(0).upper()).isEqualTo(dt(2024, 1, 30));
            // Второй: [2024-03-01, 2024-03-10)
            assertThat(mr.get(1).lower()).isEqualTo(dt(2024, 3, 1));
            assertThat(mr.get(1).upper()).isEqualTo(dt(2024, 3, 10));
        }
    }

    // ============================================================
    // 2. ТЕСТЫ containsElement (anymultirange @> anyelement)
    // ============================================================

    @Nested
    @DisplayName("containsElement: anymultirange @> anyelement")
    class ContainsElementTests {

        private final TsMultiRange mr = multirange(
                range(dt(2024, 1, 1), dt(2024, 1, 10)),   // [Jan 1, Jan 10)
                range(dt(2024, 2, 1), dt(2024, 2, 15)),   // [Feb 1, Feb 15)
                range(dt(2024, 3, 5), dt(2024, 3, 5), "[]") // [Mar 5, Mar 5] - одна точка
        );

        @Test
        @DisplayName("Элемент внутри первого диапазона")
        void elementInFirstRange() {
            assertThat(mr.containsElement(dt(2024, 1, 5))).isTrue();
        }

        @Test
        @DisplayName("Элемент внутри второго диапазона")
        void elementInSecondRange() {
            assertThat(mr.containsElement(dt(2024, 2, 10))).isTrue();
        }

        @Test
        @DisplayName("Элемент на нижней включительной границе")
        void elementOnLowerInclusiveBound() {
            assertThat(mr.containsElement(dt(2024, 1, 1))).isTrue();
        }

        @Test
        @DisplayName("Элемент на верхней исключительной границе")
        void elementOnUpperExclusiveBound() {
            assertThat(mr.containsElement(dt(2024, 1, 10))).isFalse();
        }

        @Test
        @DisplayName("Элемент в дырке между диапазонами")
        void elementInGap() {
            assertThat(mr.containsElement(dt(2024, 1, 20))).isFalse();
        }

        @Test
        @DisplayName("Элемент левее всех диапазонов")
        void elementBeforeAll() {
            assertThat(mr.containsElement(dt(2023, 12, 31))).isFalse();
        }

        @Test
        @DisplayName("Элемент правее всех диапазонов")
        void elementAfterAll() {
            assertThat(mr.containsElement(dt(2024, 4, 1))).isFalse();
        }

        @Test
        @DisplayName("Элемент в одноточечном диапазоне [x, x]")
        void elementInSinglePointRange() {
            assertThat(mr.containsElement(dt(2024, 3, 5))).isTrue();
        }

        @Test
        @DisplayName("Элемент рядом с одноточечным диапазоном")
        void elementNearSinglePointRange() {
            assertThat(mr.containsElement(dt(2024, 3, 4))).isFalse();
            assertThat(mr.containsElement(dt(2024, 3, 6))).isFalse();
        }

        @Test
        @DisplayName("Пустой мультисписок не содержит ничего")
        void emptyMultirangeContainsNothing() {
            TsMultiRange empty = TsMultiRange.of(Collections.emptyList());
            assertThat(empty.containsElement(dt(2024, 1, 1))).isFalse();
        }

        @Test
        @DisplayName("null элемент бросает IllegalArgumentException")
        void nullElementThrows() {
            assertThatThrownBy(() -> mr.containsElement(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ============================================================
    // 3. ТЕСТЫ containsRange (anymultirange @> anyrange)
    // ============================================================

    @Nested
    @DisplayName("containsRange: anymultirange @> anyrange")
    class ContainsRangeTests {

        private final TsMultiRange mr = multirange(
                range(dt(2024, 1, 1), dt(2024, 1, 10)),
                range(dt(2024, 2, 1), dt(2024, 2, 15))
        );

        @Test
        @DisplayName("Диапазон целиком внутри одного из диапазонов мультисписка")
        void rangeFullyInside() {
            TsRange r = range(dt(2024, 1, 3), dt(2024, 1, 7));
            assertThat(mr.containsRange(r)).isTrue();
        }

        @Test
        @DisplayName("Диапазон совпадает с одним из диапазонов мультисписка")
        void rangeExactlyMatches() {
            TsRange r = range(dt(2024, 1, 1), dt(2024, 1, 10));
            assertThat(mr.containsRange(r)).isTrue();
        }

        @Test
        @DisplayName("Диапазон пересекает дырку между диапазонами")
        void rangeSpansGap() {
            TsRange r = range(dt(2024, 1, 5), dt(2024, 2, 5));
            assertThat(mr.containsRange(r)).isFalse();
        }

        @Test
        @DisplayName("Диапазон вылезает за правую границу")
        void rangeExtendsBeyondUpper() {
            TsRange r = range(dt(2024, 1, 5), dt(2024, 1, 15));
            assertThat(mr.containsRange(r)).isFalse();
        }

        @Test
        @DisplayName("Пустой диапазон содержится всегда")
        void emptyRangeAlwaysContained() {
            TsRange empty = range(dt(2024, 6, 1), dt(2024, 6, 1)); // [x, x) -> empty
            assertThat(mr.containsRange(empty)).isTrue();
        }

        @Test
        @DisplayName("Пустой мультисписок не содержит непустой диапазон")
        void emptyMultirangeContainsNothing() {
            TsMultiRange emptyMr = TsMultiRange.of(Collections.emptyList());
            TsRange r = range(dt(2024, 1, 1), dt(2024, 1, 5));
            assertThat(emptyMr.containsRange(r)).isFalse();
        }

        @Test
        @DisplayName("null бросает исключение")
        void nullRangeThrows() {
            assertThatThrownBy(() -> mr.containsRange(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ============================================================
    // 4. ТЕСТЫ containsMultirange (anymultirange @> anymultirange)
    // ============================================================

    @Nested
    @DisplayName("containsMultirange: anymultirange @> anymultirange")
    class ContainsMultirangeTests {

        private final TsMultiRange outer = multirange(
                range(dt(2024, 1, 1), dt(2024, 1, 31)),
                range(dt(2024, 3, 1), dt(2024, 3, 31))
        );

        @Test
        @DisplayName("Мультисписок содержится целиком")
        void multirangeFullyContained() {
            TsMultiRange inner = multirange(
                    range(dt(2024, 1, 5), dt(2024, 1, 15)),
                    range(dt(2024, 3, 10), dt(2024, 3, 20))
            );
            assertThat(outer.containsMultirange(inner)).isTrue();
        }

        @Test
        @DisplayName("Один из диапазонов не содержится")
        void oneRangeNotContained() {
            TsMultiRange inner = multirange(
                    range(dt(2024, 1, 5), dt(2024, 1, 15)),
                    range(dt(2024, 2, 10), dt(2024, 2, 20)) // дырка в outer
            );
            assertThat(outer.containsMultirange(inner)).isFalse();
        }

        @Test
        @DisplayName("Пустой мультисписок содержится в любом")
        void emptyMultirangeAlwaysContained() {
            TsMultiRange empty = TsMultiRange.of(Collections.emptyList());
            assertThat(outer.containsMultirange(empty)).isTrue();
        }

        @Test
        @DisplayName("Пустой не содержит непустой")
        void emptyDoesNotContainNonEmpty() {
            TsMultiRange empty = TsMultiRange.of(Collections.emptyList());
            TsMultiRange nonEmpty = multirange(range(dt(2024, 1, 1), dt(2024, 1, 5)));
            assertThat(empty.containsMultirange(nonEmpty)).isFalse();
        }

        @Test
        @DisplayName("Мультисписок содержит сам себя")
        void containsItself() {
            assertThat(outer.containsMultirange(outer)).isTrue();
        }
    }

    // ============================================================
    // 5. ТЕСТЫ merge (range_merge)
    // ============================================================

    @Nested
    @DisplayName("merge: range_merge (выпуклая оболочка)")
    class MergeTests {

        @Test
        @DisplayName("merge пустого мультисписка возвращает EMPTY")
        void mergeEmpty() {
            TsMultiRange empty = TsMultiRange.of(Collections.emptyList());
            assertThat(empty.merge()).isEqualTo(TsRange.EMPTY);
        }

        @Test
        @DisplayName("merge одного диапазона возвращает его же")
        void mergeSingle() {
            TsRange r = range(dt(2024, 1, 1), dt(2024, 1, 10));
            TsMultiRange mr = multirange(r);
            assertThat(mr.merge()).isEqualTo(r);
        }

        @Test
        @DisplayName("merge нескольких диапазонов возвращает минимальный охватывающий")
        void mergeMultiple() {
            TsMultiRange mr = multirange(
                    range(dt(2024, 3, 1), dt(2024, 3, 15)),
                    range(dt(2024, 1, 10), dt(2024, 1, 20)),
                    range(dt(2024, 2, 5), dt(2024, 2, 25))
            );

            TsRange merged = mr.merge();

            assertThat(merged.lower()).isEqualTo(dt(2024, 1, 10));
            assertThat(merged.upper()).isEqualTo(dt(2024, 3, 15));
            assertThat(merged.lowerInc()).isTrue();
            assertThat(merged.upperInc()).isFalse();
        }
    }

    // ============================================================
    // 6. ТЕСТЫ вспомогательных методов
    // ============================================================

    @Nested
    @DisplayName("Вспомогательные методы")
    class HelperMethodsTests {

        private final TsMultiRange mr = multirange(
                range(dt(2024, 1, 1), dt(2024, 1, 10)),
                range(dt(2024, 3, 1), dt(2024, 3, 15))
        );

        @Test
        @DisplayName("lower() возвращает нижнюю границу первого диапазона")
        void lowerReturnsFirstLower() {
            assertThat(mr.lower()).isEqualTo(dt(2024, 1, 1));
        }

        @Test
        @DisplayName("upper() возвращает верхнюю границу последнего диапазона")
        void upperReturnsLastUpper() {
            assertThat(mr.upper()).isEqualTo(dt(2024, 3, 15));
        }

        @Test
        @DisplayName("lowerInc() и upperInc() корректны")
        void boundsInc() {
            assertThat(mr.lowerInc()).isTrue();
            assertThat(mr.upperInc()).isFalse();
        }

        @Test
        @DisplayName("size() возвращает количество диапазонов")
        void sizeReturnsCount() {
            assertThat(mr.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("isEmpty() для непустого мультисписка")
        void nonEmptyIsNotEmpty() {
            assertThat(mr.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("isEmpty() для пустого мультисписка")
        void emptyIsEmpty() {
            TsMultiRange empty = TsMultiRange.of(Collections.emptyList());
            assertThat(empty.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("get() возвращает диапазон по индексу")
        void getByIndex() {
            assertThat(mr.get(0).lower()).isEqualTo(dt(2024, 1, 1));
            assertThat(mr.get(1).lower()).isEqualTo(dt(2024, 3, 1));
        }

        @Test
        @DisplayName("getFirst() и getLast()")
        void firstAndLast() {
            assertThat(mr.getFirst().lower()).isEqualTo(dt(2024, 1, 1));
            assertThat(mr.getLast().lower()).isEqualTo(dt(2024, 3, 1));
        }
    }

    // ============================================================
    // 7. ТЕСТЫ toString
    // ============================================================

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("Пустой мультисписок")
        void emptyToString() {
            TsMultiRange empty = TsMultiRange.of(Collections.emptyList());
            assertThat(empty.toString()).isEqualTo("{}");
        }

        @Test
        @DisplayName("Один диапазон")
        void singleRangeToString() {
            TsMultiRange mr = multirange(range(dt(2024, 1, 1), dt(2024, 1, 10)));
            String str = mr.toString();
            assertThat(str).startsWith("{");
            assertThat(str).endsWith("}");
            assertThat(str).contains("2024-01-01");
        }

        @Test
        @DisplayName("Несколько диапазонов разделены запятыми")
        void multipleRangesToString() {
            TsMultiRange mr = multirange(
                    range(dt(2024, 1, 1), dt(2024, 1, 10)),
                    range(dt(2024, 3, 1), dt(2024, 3, 15))
            );
            String str = mr.toString();
            assertThat(str).contains(",");
            assertThat(str).startsWith("{");
            assertThat(str).endsWith("}");
        }
    }

    // ============================================================
    // 8. ТЕСТЫ equals и hashCode
    // ============================================================

    @Nested
    @DisplayName("equals и hashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Разные мультисписки не равны")
        void differentMultiranges() {
            TsMultiRange mr1 = multirange(range(dt(2024, 1, 1), dt(2024, 1, 10)));
            TsMultiRange mr2 = multirange(range(dt(2024, 2, 1), dt(2024, 2, 10)));
            assertThat(mr1).isNotEqualTo(mr2);
        }

        @Test
        @DisplayName("Мультисписок не равен null")
        void notEqualToNull() {
            TsMultiRange mr = multirange(range(dt(2024, 1, 1), dt(2024, 1, 10)));
            assertThat(mr).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Мультисписок равен самому себе")
        void equalToSelf() {
            TsMultiRange mr = multirange(range(dt(2024, 1, 1), dt(2024, 1, 10)));
            assertThat(mr).isEqualTo(mr);
        }
    }

    // ============================================================
    // 9. ТЕСТЫ iterator
    // ============================================================

    @Nested
    @DisplayName("Iterable / Iterator")
    class IteratorTests {

        @Test
        @DisplayName("Итератор обходит все диапазоны по порядку")
        void iteratorTraversesAll() {
            TsMultiRange mr = multirange(
                    range(dt(2024, 1, 1), dt(2024, 1, 10)),
                    range(dt(2024, 2, 1), dt(2024, 2, 15)),
                    range(dt(2024, 3, 1), dt(2024, 3, 20))
            );

            int count = 0;
            for (TsRange r : mr) {
                assertThat(r).isNotNull();
                assertThat(r.isEmpty()).isFalse();
                count++;
            }
            assertThat(count).isEqualTo(3);
        }
    }

    // ============================================================
    // 10. EDGE CASES
    // ============================================================

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("of(null) бросает IllegalArgumentException")
        void ofNullThrows() {
            assertThatThrownBy(() -> TsMultiRange.of((TsRange) null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("of(null list) бросает IllegalArgumentException")
        void ofNullListThrows() {
            assertThatThrownBy(() -> TsMultiRange.of((List<TsRange>) null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Бесконечные границы обрабатываются корректно")
        void infinityBounds() {
            TsRange r = TsRange.of(
                    TsRange.MINUS_INFINITY,
                    TsRange.INFINITY
            );
            TsMultiRange mr = multirange(r);

            assertThat(mr.lowerInf()).isTrue();
            assertThat(mr.upperInf()).isTrue();
            assertThat(mr.containsElement(dt(2024, 6, 15))).isTrue();
        }
    }
}