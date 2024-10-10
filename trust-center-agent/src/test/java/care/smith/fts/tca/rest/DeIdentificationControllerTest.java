package care.smith.fts.tca.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static reactor.test.StepVerifier.create;

import care.smith.fts.tca.deidentification.PseudonymProvider;
import care.smith.fts.tca.deidentification.ShiftedDatesProvider;
import care.smith.fts.util.error.UnknownDomainException;
import care.smith.fts.util.tca.PseudonymizeRequest;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DeIdentificationControllerTest {

  @Mock PseudonymProvider pseudonymProvider;
  @Mock ShiftedDatesProvider shiftedDatesProvider;

  private DeIdentificationController controller;

  @BeforeEach
  void setUp() {
    this.controller = new DeIdentificationController(pseudonymProvider, shiftedDatesProvider);
  }

  @Test
  void getTransportIdsAndDateShiftingValues() {
    var ids = Set.of("id1", "id2");
    var mapName = "tIDMapName";
    given(pseudonymProvider.retrieveTransportIds("patientId1", ids, "domain", "domain"))
        .willReturn(Mono.just(Tuples.of(mapName, Map.of("id1", "tid1", "id2", "tid2"))));
    given(shiftedDatesProvider.generateDateShift("patientId1", Duration.ofDays(14)))
        .willReturn(Mono.just(Duration.ofDays(1)));

    var body =
        new PseudonymizeRequest(
            "patientId1", ids, "domain", "domain", "domain", Duration.ofDays(14));

    create(controller.getTransportIdsAndDateShiftingValues(Mono.just(body)))
        .assertNext(
            r -> {
              assertThat(r.getStatusCode().is2xxSuccessful()).isTrue();
              assertThat(r.getBody().dateShiftValue()).isEqualTo(Duration.ofSeconds(86400));
              assertThat(r.getBody().originalToTransportIDMap())
                  .containsEntry("id1", "tid1")
                  .containsEntry("id2", "tid2");
              assertThat(r.getBody().tIDMapName()).isEqualTo("tIDMapName");
            })
        .verifyComplete();
  }

  @Test
  void getTransportIdsAndDateShiftingValuesUnknownDomain() {
    given(
            pseudonymProvider.retrieveTransportIds(
                "id1", Set.of("id1"), "unknown domain", "unknown domain"))
        .willReturn(Mono.error(new UnknownDomainException("unknown domain")));
    given(shiftedDatesProvider.generateDateShift("id1", Duration.ofDays(14)))
        .willReturn(Mono.just(Duration.ofDays(1)));

    var body =
        new PseudonymizeRequest(
            "id1",
            Set.of("id1"),
            "unknown domain",
            "unknown domain",
            "unknown domain",
            Duration.ofDays(14));

    create(controller.getTransportIdsAndDateShiftingValues(Mono.just(body)))
        .assertNext(
            r -> {
              assertThat(r.getStatusCode().is4xxClientError()).isTrue();
            })
        .verifyComplete();
  }

  @Test
  void getTransportIdsAndDateShiftingValuesIllegalArgumentException() {
    given(pseudonymProvider.retrieveTransportIds("id1", Set.of("id1"), "domain", "domain"))
        .willReturn(Mono.error(new IllegalArgumentException("Illegal argument")));
    given(shiftedDatesProvider.generateDateShift("id1", Duration.ofDays(14)))
        .willReturn(Mono.just(Duration.ofDays(1)));

    var body =
        new PseudonymizeRequest(
            "id1", Set.of("id1"), "domain", "domain", "domain", Duration.ofDays(14));

    create(controller.getTransportIdsAndDateShiftingValues(Mono.just(body)))
        .assertNext(
            r -> {
              assertThat(r.getStatusCode().is4xxClientError()).isTrue();
            })
        .verifyComplete();
  }

  @Test
  void getTransportIdsAndDateShiftingValuesEmptyIds() {
    var body =
        new PseudonymizeRequest("id1", Set.of(), "domain", "domain", "domain", Duration.ofDays(14));

    create(controller.getTransportIdsAndDateShiftingValues(Mono.just(body))).verifyComplete();
  }

  @Test
  void getTransportIdsAndDateShiftingValuesInternalServerError() {
    var ids = Set.of("id1", "id2");
    given(pseudonymProvider.retrieveTransportIds("id1", ids, "domain", "domain"))
        .willReturn(Mono.error(new InternalServerErrorException("Internal Server Error")));
    given(shiftedDatesProvider.generateDateShift("id1", Duration.ofDays(14)))
        .willReturn(Mono.just(Duration.ofDays(1)));

    var body =
        new PseudonymizeRequest("id1", ids, "domain", "domain", "domain", Duration.ofDays(14));

    create(controller.getTransportIdsAndDateShiftingValues(Mono.just(body)))
        .assertNext(
            r -> {
              assertThat(r.getStatusCode().is5xxServerError()).isTrue();
            })
        .verifyComplete();
  }

  @Test
  void fetchPseudonymizedIds() {
    given(pseudonymProvider.fetchPseudonymizedIds("tIDMapName"))
        .willReturn(Mono.just(Map.of("tid-1", "pid1", "tid-2", "pid2")));

    create(controller.fetchPseudonymizedIds("tIDMapName"))
        .assertNext(
            r -> {
              assertThat(r.getStatusCode().is2xxSuccessful()).isTrue();
              assertThat(r.getBody()).containsEntry("tid-1", "pid1").containsEntry("tid-2", "pid2");
            })
        .verifyComplete();
  }

  @Test
  void fetchPseudonymizedIdsEmptyIds() {
    given(pseudonymProvider.fetchPseudonymizedIds("tIDMapName")).willReturn(Mono.empty());

    create(controller.fetchPseudonymizedIds("tIDMapName")).verifyComplete();
  }
}
