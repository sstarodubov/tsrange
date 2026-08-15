package ru.starodubov;

import java.time.LocalDateTime;
import java.util.*;

/*
   https://postgrespro.ru/docs/postgrespro/current/functions-range
 */
public final class TsMultiRange implements Iterable<TsRange> {

    private final List<TsRange> ranges;

    private TsMultiRange(final List<TsRange> ranges) {
        this.ranges = ranges;
    }

    public static TsMultiRange of(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }
        return new TsMultiRange(normalize(List.of(range)));
    }

    public static TsMultiRange of(final List<TsRange> ranges) {
        if (ranges == null) {
            throw new IllegalArgumentException("ranges must not be null");
        }
        return new TsMultiRange(normalize(ranges));
    }

    /*
    anymultirange @> anymultirange → boolean
    Первый мультидиапазон содержит второй?
            '{[2,4)}'::int4multirange @> '{[2,3)}'::int4multirange → t
     */

    public boolean containsMultirange(final TsMultiRange mrange) {
       if (mrange == null) {
           throw new IllegalArgumentException("range must not be null");
       }

        if (mrange.isEmpty()) {
            return true;
        }

        if (this.isEmpty()) {
            return false;
        }

        int thisIdx = 0; // указатель на this.ranges
        int mrangeIdx = 0; // указатель на mrange.ranges
        TsRange target;
        while (mrangeIdx < mrange.size()) {
            target = mrange.get(mrangeIdx);

            while (thisIdx < this.size() && this.get(thisIdx).strictlyLeftOf(target)) {
                thisIdx++;
            }

            if (thisIdx >= this.size()) {
                return false;
            }

            if (!this.get(thisIdx).containsRange(target)) {
                return false;
            }

            mrangeIdx++;
        }

        return true;
    }

    /*
      range_merge принимает мультисписок и возвращает один минимальный диапазон,
      который покрывает все элементы мультисписка. По сути — «выпуклая оболочка» (convex hull) всех диапазонов.
     */
    public TsRange merge() {
        if (this.isEmpty()) {
            return TsRange.EMPTY;
        }
        if (this.size() == 1) {
            return this.getFirst();
        }

        return TsRange.of(this.getFirst().lower(), this.getLast().upper(),
                this.getFirst().lowerInc(), this.getLast().upperInc());
    }
    /*
    PostgreSQL хранит мультисписки в каноническом (нормализованном) виде.
    Это означает, что при создании или изменении tsmultirange СУБД автоматически применяет три правила:
        Сортировка: Диапазоны всегда отсортированы по возрастанию нижней границы.
        Схлопывание (Coalescing): Если диапазоны пересекаются (overlap) или соприкасаются (adjacent),
                                  они автоматически объединяются в один.
        Очистка: Пустые диапазоны удаляются.
     */

    private static List<TsRange> normalize(final List<TsRange> ranges) {
        final var normList = copyAndSort(ranges);
        TsRange last, cur;
        int wrIdx = 0; // индекс записи
        for (int readIdx = 0; readIdx <  normList.size(); readIdx++) {
            cur = normList.get(readIdx);
            if (cur.isEmpty()) {
                continue;
            }

            if (wrIdx == 0) {
                normList.set(wrIdx, cur);
                wrIdx++;
            } else {
                last = normList.get(wrIdx - 1);
                if (last.overlaps(cur) || last.isAdjacentTo(cur)) {
                    wrIdx--; // удалили последний элемент
                    normList.set(wrIdx, last.merge(cur)); // на его место зависали смерженный отрезок
                } else {
                    normList.set(wrIdx, cur);
                }
                wrIdx++;
            }
        }

        shrink(normList, wrIdx); //выравнимаем размер массива по индексу записи

        return Collections.unmodifiableList(normList);
    }

    public boolean isEmpty() {
        return this.ranges.isEmpty();
    }

    static List<TsRange> copyAndSort(final List<TsRange> ranges) {
        final var result = new ArrayList<TsRange>(ranges.size());
        for (var r : ranges) {
            result.add(r);
        }
        Collections.sort(result);
        return result;
    }

    static void shrink(List<TsRange> list, int idx) {
        if (idx < list.size()) {
            list.subList(idx, list.size()).clear();
        }
    }

    public LocalDateTime lower() {
        if (this.isEmpty()) {
            return null;
        }
        return this.getFirst().lower();
    }

    public LocalDateTime upper() {
        if (this.isEmpty()) {
            return null;
        }
        return this.getLast().upper();
    }

    public boolean lowerInc() {
        if (this.isEmpty()) {
            return false;
        }
        return this.getFirst().lowerInc();
    }


    public boolean upperInc() {
        if (this.isEmpty()) {
            return false;
        }
        return this.getLast().upperInc();
    }

    public boolean lowerInf() {
        if (this.isEmpty()) {
            return false;
        }
        return this.getFirst().lowerInf();
    }

    public boolean upperInf() {
        if (this.isEmpty()) {
            return false;
        }
        return this.getLast().upperInf();
    }

    @Override
    public Iterator<TsRange> iterator() {
        return this.ranges.iterator();
    }

    public TsRange get(final int idx) {
        return this.ranges.get(idx);
    }

    public TsRange getLast() {
        return this.ranges.get(this.ranges.size() - 1);
    }

    public TsRange getFirst() {
        return this.ranges.get(0);
    }

    public int size() {
        return this.ranges.size();
    }

    @Override
    public String toString() {
        if (isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < ranges.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(ranges.get(i).toString());
        }
        sb.append("}");
        return sb.toString();
    }
}
