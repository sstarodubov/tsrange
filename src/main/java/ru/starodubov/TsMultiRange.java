package ru.starodubov;

import java.time.LocalDateTime;
import java.util.*;

/*
   https://postgrespro.ru/docs/postgrespro/current/functions-range
 */
public final class TsMultiRange implements Iterable<TsRange> {

    private final List<TsRange> ranges;
    public static final TsMultiRange EMPTY = new TsMultiRange(Collections.emptyList());

    private TsMultiRange(final List<TsRange> ranges) {
        this.ranges = ranges;
    }

    public static TsMultiRange of(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }
        if (range.isEmpty()) {
            return EMPTY;
        }
        return new TsMultiRange(List.of(range));
    }

    public static TsMultiRange of(final List<TsRange> ranges) {
        if (ranges == null) {
            throw new IllegalArgumentException("ranges must not be null");
        }
        return new TsMultiRange(normalize(ranges));
    }


    /*
        anymultirange * anymultirange → anymultirange
        Вычисляет пересечение мультидиапазонов.
        '{[5,15)}'::int8multirange * '{[10,20)}'::int8multirange → {[10,15)}
     */
    public TsMultiRange intersection(final TsMultiRange other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }

        if (this.isEmpty() || other.isEmpty()) {
            return TsMultiRange.of(List.of());
        }

        final List<TsRange> result = new ArrayList<>();

        int i = 0, j = 0;
        TsRange r1, r2;
        while (i < this.size() && j < other.size()) {
            r1 = this.get(i);
            r2 = other.get(j);

            final TsRange inter = r1.intersection(r2);
            if (!inter.isEmpty()) {
                result.add(inter);
            }

            if (r1.compareUpper(r2) < 0) {
                i++;
            } else {
                j++;
            }
        }

        return new TsMultiRange(result);
    }

    /*
    anymultirange + anymultirange → anymultirange
    Вычисляет объединение мультидиапазонов. Мультидиапазоны могут не пересекаться и не примыкать друг к другу.
    '{[5,10)}'::nummultirange + '{[15,20)}'::nummultirange → {[5,10), [15,20)}
     */

    public TsMultiRange union(final TsMultiRange other) {
        if (other == null) {
            throw new IllegalArgumentException("range is null");
        }

        if (this.isEmpty()) {
            return other;
        }

        if (other.isEmpty()) {
            return this;
        }

        final var arr = new ArrayList<TsRange>(other.size() + this.size());
        arr.addAll(other.ranges);
        arr.addAll(this.ranges);
        Collections.sort(arr);
        normalizeNoCopy(arr);
        return new TsMultiRange(arr);
    }

    /*
       anymultirange < anymultirange → boolean
        */
    public boolean lessThan(final TsMultiRange other) {
        if (other == null) {
            throw new IllegalArgumentException("range must not be null");
        }
        final boolean thisEmpty = this.isEmpty();
        final boolean otherEmpty = other.isEmpty();

        if (thisEmpty && otherEmpty) {
            return false;
        }

        if (thisEmpty) {
            return true;
        }

        if (otherEmpty) {
            return false;
        }

        final int minSize = Math.min(this.size(), other.size());
        TsRange r1, r2;
        for (int i = 0; i < minSize; i++) {
            r1 = this.get(i);
            r2 = other.get(i);

            if (r1.lessThan(r2)) {
                return true;
            }
            if (r2.lessThan(r1)) {
                return false;
            }
            // Если равны, продолжаем сравнивать следующие
        }

        return this.size() < other.size();
    }

    /*
       anymultirange > anymultirange → boolean
    */
    public boolean greaterThan(final TsMultiRange other) {
        return other.lessThan(this);
    }

    /*
           anymultirange <= anymultirange → boolean
        */
    public boolean lessThanOrEqual(final TsMultiRange other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }
        return !other.lessThan(this);
    }

    /*
           anymultirange >= anymultirange → boolean
        */
    public boolean greaterThanOrEqual(final TsMultiRange other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }
        return !this.lessThan(other);
    }

    /*
       anymultirange = anymultirange → boolean
    */
    public boolean isEqual(final TsMultiRange other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }

        if (this.isEmpty() && other.isEmpty()) {
            return true;
        }

        if (this.isEmpty() || other.isEmpty()) {
            return false;
        }

        if (this.size() != other.size()) {
            return false;
        }

        for (int i = 0; i < this.size(); i++) {
            if (!this.get(i).isEqual(other.get(i))) {
                return false;
            }
        }
        return true;
    }

    /*
    anymultirange -|- anymultirange → boolean
     Мультидиапазоны примыкают друг к другу?
     '{[1.1,2.2)}'::nummultirange -|- '{[2.2,3.3)}'::nummultirange → t
     */
    public boolean isAdjacentTo(final TsMultiRange other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }

        if (this.isEmpty() || other.isEmpty()) {
            return false;
        }
        final TsRange m1 = this.merge();
        final TsRange m2 = other.merge();
        return m1.isAdjacentTo(m2);
    }

    /*
    anymultirange >> anymultirange → boolean
    Первый мультидиапазон располагается строго справа от второго?
    {[50,60)}'::int8multirange >> '{[20,30)}'::int8multirange → t

     */
    public boolean strictlyRightOf(final TsMultiRange other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }

        if (this.isEmpty() || other.isEmpty()) {
            return false;
        }

        return other.strictlyLeftOf(this);
    }

    /*
        anymultirange << anymultirange → boolean
        Первый мультидиапазон располагается строго слева от второго?
        '{[1,10)}'::int8multirange << '{[100,110)}'::int8multirange → t
     */
    public boolean strictlyLeftOf(final TsMultiRange other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }

        if (this.isEmpty() || other.isEmpty()) {
            return false;
        }

        final TsRange lastOfThis = this.getLast();
        final TsRange firstOfOther = other.getFirst();

        return lastOfThis.strictlyLeftOf(firstOfOther);
    }


    /*
    anymultirange && anymultirange → boolean
    Мультидиапазоны пересекаются (у них есть общие элементы)?
    '{[3,7)}'::int8multirange && '{[4,12)}'::int8multirange → t
     */
    public boolean overlaps(final TsMultiRange other) {
        if (other == null) {
            throw new IllegalArgumentException("range must not be null");
        }

        if (this.isEmpty() || other.isEmpty()) {
            return false;
        }

        int curIdx = 0;
        int otherIdx = 0;

        while (curIdx < this.size() && otherIdx < other.size()) {

            if (this.get(curIdx).overlaps(other.get(otherIdx))) {
                return true;
            }

            if (this.get(curIdx).lessThan(other.get(otherIdx))) {
                curIdx++;
            } else {
                otherIdx++;
            }
        }


        return false;
    }

    /*
    anymultirange <@ anyrange → boolean
    Мультидиапазон содержится в диапазоне?
    '{[2,4)}'::int4multirange <@ int4range(1,7) → t
     */
    public boolean isContainedBy(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }
        return range.containsMultirange(this);
    }

    /*
     * anymultirange <@ anymultirange → boolean
     * Первый мультисписок содержится во втором?
     * '{[2,3)}'::int4multirange <@ '{[2,4),[5,7)}'::int4multirange → t
     */
    public boolean isContainedBy(final TsMultiRange other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }
        return other.containsMultirange(this);
    }

    /*
    anymultirange @> anyelement → boolean
    Мультидиапазон содержит заданный элемент?
    '{[2011-01-01,2011-03-01)}'::tsmultirange @> '2011-01-10'::timestamp → t
     */

    public boolean containsElement(final LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("dateTime must not be null");
        }
        if (this.isEmpty()) {
            return false;
        }

        int mid, left = 0, right = this.ranges.size() - 1;
        TsRange r;
        while (left <= right) {
            mid = (left + right) >>> 1;
            r = this.ranges.get(mid);
            if (r.containsElement(dateTime)) {
                return true;
            }

            int cmp = TsRange.compareDateTime(dateTime, r.lower());
            if (cmp < 0 || (cmp == 0 && !r.lowerInc())) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        return false;
    }

    /*
    anymultirange @> anyrange → boolean
    Мультидиапазон содержит заданный диапазон?
            '{[2,4)}'::int4multirange @> int4range(2,3) → t
     */
    public boolean containsRange(final TsRange range) {
        if (range == null) {
            throw new IllegalArgumentException("range must not be null");
        }

        if (range.isEmpty()) {
            return true;
        }

        if (this.isEmpty()) {
            return false;
        }

        int lo = 0;
        int hi = this.size() - 1;
        int mid;
        TsRange element;
        while (lo <= hi) {
            mid = (lo + hi) >>> 1;
            element = this.get(mid);

            if (element.containsRange(range)) {
                return true;
            }

            if (element.lessThan(range)) {
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }

        return false;
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
    anymultirange - anymultirange → anymultirange
    Вычисляет разность мультидиапазонов.
    '{[5,20)}'::int8multirange - '{[10,15)}'::int8multirange → {[5,10), [15,20)}
     */
    public TsMultiRange difference(final TsMultiRange other) {
        if (other == null) {
            throw new IllegalArgumentException("other must not be null");
        }
        if (this.isEmpty() || other.isEmpty()) {
            return this;
        }

        // Выделяем память только один раз под финальный результат
        final List<TsRange> result = new ArrayList<>();
        int j = 0; // указатель на other.ranges (никогда не сбрасывается, сложность O(N + M))
        int otherSize = other.size();

        for (int i = 0; i < this.size(); i++) {
            TsRange rangeA = this.get(i);

            // 1. Быстро пропускаем диапазоны в other, которые заведомо левее текущего rangeA
            while (j < otherSize && other.get(j).strictlyLeftOf(rangeA)) {
                j++;
            }

            // 2. Отслеживаем эффективное начало оставшейся (невычтенной) части диапазона A
            LocalDateTime currentStart = rangeA.lower();
            boolean currentStartInc = rangeA.lowerInc();
            boolean finishedA = false;

            // 3. Проверяем пересечения, начиная с индекса j
            int k = j;
            while (k < otherSize) {
                TsRange rangeB = other.get(k);

                // Если rangeB строго правее исходного rangeA, он не может пересекаться
                // ни с rangeA, ни с любой его оставшейся частью.
                if (rangeB.strictlyRightOf(rangeA)) {
                    break;
                }

                // Если rangeB строго левее текущей оставшейся части A, он её не затрагивает.
                // (В other могут быть мелкие непересекающиеся диапазоны между основными)
                if (isStrictlyBefore(rangeB.upper(), rangeB.upperInc(), currentStart, currentStartInc)) {
                    k++;
                    continue;
                }

                // --- ЗДЕСЬ ЕСТЬ ПЕРЕСЕЧЕНИЕ rangeB с текущим активным сегментом ---

                // Шаг А: Сохраняем левый "хвост" диапазона A, если он есть
                // (от currentStart до начала rangeB)
                if (isStrictlyBefore(currentStart, currentStartInc, rangeB.lower(), rangeB.lowerInc())) {
                    result.add(TsRange.of(currentStart, rangeB.lower(), currentStartInc, !rangeB.lowerInc()));
                }

                // Шаг Б: "Сдвигаем" начало оставшейся части A за пределы rangeB
                currentStart = rangeB.upper();
                currentStartInc = !rangeB.upperInc();

                // Шаг В: Проверяем, не "съел" ли rangeB весь оставшийся диапазон A
                if (!isValidInterval(currentStart, currentStartInc, rangeA.upper(), rangeA.upperInc())) {
                    finishedA = true;
                    break;
                }

                k++;
            }

            // 4. Если после всех вычитаний от диапазона A что-то осталось, добавляем это в результат
            if (!finishedA && isValidInterval(currentStart, currentStartInc, rangeA.upper(), rangeA.upperInc())) {
                // Микро-оптимизация: если границы не менялись, добавляем исходный объект без создания нового
                if (currentStart.equals(rangeA.lower()) && currentStartInc == rangeA.lowerInc()) {
                    result.add(rangeA);
                } else {
                    result.add(TsRange.of(currentStart, rangeA.upper(), currentStartInc, rangeA.upperInc()));
                }
            }
        }

        return new TsMultiRange(result);
    }

    /**
     * Проверяет, что граница (v1, inc1) строго левее границы (v2, inc2).
     * Полностью эквивалентно TsRange.compareLowerEndpoints(v1, v2, inc1, inc2) < 0,
     * но работает с сырыми значениями без создания объектов.
     */
    private static boolean isStrictlyBefore(LocalDateTime v1, boolean inc1, LocalDateTime v2, boolean inc2) {
        return TsRange.compareLowerEndpoints(v1, v2, inc1, inc2) < 0;
    }

    /**
     * Проверяет, что интервал от (start, startInc) до (end, endInc) не является пустым.
     * Логика в точности повторяет отрицание TsRange.isEmpty().
     */
    private static boolean isValidInterval(LocalDateTime start, boolean startInc, LocalDateTime end, boolean endInc) {
        int cmp = TsRange.compareLowerEndpoints(start, end, startInc, endInc);
        if (cmp < 0) return true;       // start строго левее end
        if (cmp > 0) return false;      // start строго правее end
        // cmp == 0: интервал из одной точки валиден только если обе границы включительные
        return startInc && endInc;
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
        normalizeNoCopy(normList);
        return Collections.unmodifiableList(normList);
    }

    /**
     *  Схлопывает и удаляет пустые диапазоны
     * @param normList - отсортированный массив с диапазонами
     */
    private static void normalizeNoCopy(final List<TsRange> normList) {
        TsRange last, cur;
        int wrIdx = 0; // индекс записи
        for (int readIdx = 0; readIdx < normList.size(); readIdx++) {
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
                    normList.set(wrIdx, last.merge(cur)); // на его место записали смерженный отрезок
                } else {
                    normList.set(wrIdx, cur);
                }
                wrIdx++;
            }
        }

        shrink(normList, wrIdx); //выравниваем размер массива по индексу записи
    }

    public boolean isEmpty() {
        return this.ranges.isEmpty();
    }

    static List<TsRange> copyAndSort(final List<TsRange> ranges) {
        final var result = new ArrayList<TsRange>(ranges.size());
        result.addAll(ranges);
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
