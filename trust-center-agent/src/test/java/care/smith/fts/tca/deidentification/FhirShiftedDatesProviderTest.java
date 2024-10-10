package care.smith.fts.tca.deidentification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class FhirShiftedDatesProviderTest {

  @Test
  void generateDateShift() {
    var provider = new FhirShiftedDatesProvider();

    var expectedShiftedDateCD = Duration.ofMillis(957039857);
    var expectedShiftedDateRD = Duration.ofMillis(-1443531032);

    var maxDateShift = Duration.ofDays(14);
    var dateShiftValues = provider.generateDateShift("1", maxDateShift);
    assertThat(dateShiftValues.getT1()).isEqualTo(expectedShiftedDateCD);
    assertThat(dateShiftValues.getT2()).isEqualTo(expectedShiftedDateRD);
    for (int i = 0; i < 1024; i++) {
      dateShiftValues = provider.generateDateShift(String.valueOf(i), maxDateShift);
      assertThat(dateShiftValues.getT2().plus(dateShiftValues.getT1()).abs())
          .isLessThanOrEqualTo(maxDateShift);
    }
  }
}
