package care.smith.fts.tca.deidentification;

import static java.time.Duration.ofMillis;

import com.google.common.hash.Hashing;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
public class FhirShiftedDatesProvider implements ShiftedDatesProvider {

  @Override
  public Tuple2<Duration, Duration> generateDateShift(
      @NotBlank String salt, @NotNull Duration maxDateShift) {
    var dateShifts = getDateShifts(salt, maxDateShift);
    var cdDateShift = ofMillis(dateShifts.getT1());
    var rdDateShift = ofMillis(dateShifts.getT2());
    return Tuples.of(cdDateShift, rdDateShift.minus(cdDateShift));
  }

  /**
   * @param salt for the hashing algorithm
   * @param maxDateShift the maximal date shift
   * @return A tuple with cdDateShift, rdDateShift
   */
  public Tuple2<Long, Long> getDateShifts(@NotBlank String salt, @NotNull Duration maxDateShift) {
    var seed = Hashing.sha256().hashString(salt, StandardCharsets.UTF_8).padToLong();
    Random random = new Random(seed);
    var shiftBy = maxDateShift.toMillis();
    return Tuples.of(random.nextLong(-shiftBy, shiftBy), random.nextLong(-shiftBy, shiftBy));
  }
}
