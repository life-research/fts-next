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

    var maxDateShift = Duration.ofDays(14);
    var dateShiftValues = provider.generateDateShift("1", maxDateShift);
    assertThat(dateShiftValues).isEqualTo(expectedShiftedDateCD);
  }
}
