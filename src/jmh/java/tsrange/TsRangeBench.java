package tsrange;

import org.openjdk.jmh.annotations.*;
import ru.starodubov.*;

import java.time.LocalDateTime;
import java.util.BitSet;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class TsRangeBench {


}
