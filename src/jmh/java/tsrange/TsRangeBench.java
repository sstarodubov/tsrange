package tsrange;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.openjdk.jmh.annotations.*;
import ru.starodubov.*;
import ru.starodubov.main.Main;

import java.time.LocalDateTime;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class TsRangeBench {

    TsMultiRange mrange;
    RangeSet<LocalDateTime> set;
    List<TsRange> ranges;
    List<Range<LocalDateTime>> granges;

    TsMultiRange mrange2;
    RangeSet<LocalDateTime> set2;

    @Setup
    public void setup() {
        var test = Main.generate(50);
        var gtest = Main.generateGoogleRanges(test);
        mrange2 = TsMultiRange.of(test);
        set2 = TreeRangeSet.create(gtest);

        ranges = Main.generate(50);
        granges = Main.generateGoogleRanges(ranges);
        set = TreeRangeSet.create(granges);
        mrange = TsMultiRange.of(ranges);
    }

    @Benchmark
    public TsMultiRange diffTsMultiRange() {
        return mrange.difference(mrange2);
    }

    @Benchmark
    public RangeSet<LocalDateTime> diffSetGoogle() {
        set.removeAll(set2);
        return set;
    }
}
