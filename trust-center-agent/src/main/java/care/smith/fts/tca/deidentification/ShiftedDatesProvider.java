package care.smith.fts.tca.deidentification;

import java.time.Duration;
import reactor.util.function.Tuple2;

public interface ShiftedDatesProvider {
  Tuple2<Duration, Duration> generateDateShift(String salt, Duration maxDateShift);
}
