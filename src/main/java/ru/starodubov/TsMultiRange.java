package ru.starodubov;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    PostgreSQL хранит мультисписки в каноническом (нормализованном) виде.
    Это означает, что при создании или изменении tsmultirange СУБД автоматически применяет три правила:
        Сортировка: Диапазоны всегда отсортированы по возрастанию нижней границы.
        Схлопывание (Coalescing): Если диапазоны пересекаются (overlap) или соприкасаются (adjacent),
                                  они автоматически объединяются в один.
        Очистка: Пустые диапазоны удаляются.
     */

    private static List<TsRange> normalize(final List<TsRange> ranges) {
        final List<TsRange> sortedRanges = ranges.stream().sorted().collect(Collectors.toList());
        final var resultList = new ArrayList<TsRange>(ranges.size());
        TsRange last;
        for (final TsRange r : sortedRanges) {
            if (r.isEmpty()) {
                continue;
            }
            if (resultList.isEmpty()) {
                resultList.add(r);
            } else {
                last = resultList.get(resultList.size() - 1);
                if (last.overlaps(r) || last.isAdjacentTo(r)) {
                    resultList.remove(resultList.size() - 1);
                    resultList.add(last.merge(r));
                } else {
                    resultList.add(r);
                }
            }
        }

        return Collections.unmodifiableList(resultList);
    }

    public boolean isEmpty() {
        return this.ranges.isEmpty();
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
