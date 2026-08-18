package ru.starodubov.main;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import ru.starodubov.TsMultiRange;
import ru.starodubov.TsRange;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    public static void main(String[] args) {
        var t1 = generate(1);
        var g1 = generateGoogleRanges(t1);

        var tsranges = generate(1);
        var mrange = TsMultiRange.of(tsranges);
        RangeSet<LocalDateTime> set = TreeRangeSet.create(generateGoogleRanges(tsranges));

        var mr = mrange.union(TsMultiRange.of(t1));
        set.removeAll(g1);
        System.out.println(set);
    }

    public static LocalDateTime randomLocalDateTime() {
        // Random date between 2000-01-01 and 2026-12-31
        long minDay = LocalDate.of(2000, 1, 1).toEpochDay();
        long maxDay = LocalDate.of(2026, 12, 31).toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay + 1);

        // Random time of day (0 to 86399 seconds)
        int randomSecondOfDay = ThreadLocalRandom.current().nextInt(86400);

        return LocalDateTime.of(
                LocalDate.ofEpochDay(randomDay),
                LocalTime.ofSecondOfDay(randomSecondOfDay)
        );
    }

    public static List<Range<LocalDateTime>> generateGoogleRanges(List<TsRange> ranges) {
        return ranges.stream().map(r -> Range.range(
            r.lower(), r.lowerInc() ? BoundType.CLOSED : BoundType.OPEN, r.upper(), r.upperInc() ? BoundType.CLOSED : BoundType.OPEN
        )).toList();
    }

    public static List<TsRange> generate(int count) {
        var arr = new ArrayList<TsRange>(count);
        for (int i = 0; i < count; i++) {
            var date = randomLocalDateTime();
            arr.add(TsRange.of(date, date.plusDays(ThreadLocalRandom.current().nextInt(300)),
                   true, ThreadLocalRandom.current().nextBoolean()
                    ));
        }

        return arr;
    }
}
