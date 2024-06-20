package care.smith.fts.cda.services.deidentifhir;

import static org.assertj.core.api.Assertions.assertThat;

import care.smith.fts.util.deidentifhir.NamespacingService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NamespacingServiceTest {

  NamespacingService namespacingService;

  @BeforeEach
  void setUp() {
    Map<String, String> transportIDs =
        Map.of("test.id.Patient:id1", "tid1", "test.identifier.Patient:id1", "tid1");
    namespacingService = NamespacingService.withNamespacing("test", transportIDs);
  }

  @Test
  void getIDReplacement() {
    assertThat(namespacingService.getIDReplacement("Patient", "id1")).isEqualTo("tid1");
  }

  @Test
  void getValueReplacement() {
    assertThat(namespacingService.getValueReplacement("Patient", "id1")).isEqualTo("tid1");
  }
}
