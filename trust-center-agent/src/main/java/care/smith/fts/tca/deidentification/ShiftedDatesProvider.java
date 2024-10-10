package care.smith.fts.tca.deidentification;

import java.time.Duration;

public interface ShiftedDatesProvider {
  Duration generateDateShift(String salt, Duration maxDateShift);
}
