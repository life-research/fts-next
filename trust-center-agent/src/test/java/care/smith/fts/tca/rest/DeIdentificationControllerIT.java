package care.smith.fts.tca.rest;

import static care.smith.fts.test.FhirGenerators.fromList;
import static care.smith.fts.test.MockServerUtil.APPLICATION_FHIR_JSON;
import static java.time.Duration.ofDays;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.matchers.MatchType.ONLY_MATCHING_FIELDS;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static reactor.test.StepVerifier.create;

import care.smith.fts.tca.BaseIT;
import care.smith.fts.test.FhirGenerators;
import care.smith.fts.test.TestWebClientFactory;
import care.smith.fts.util.tca.PseudonymizeResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(TestWebClientFactory.class)
class DeIdentificationControllerIT extends BaseIT {

  private static WebClient cdClient;
  private static WebClient rdClient;

  @BeforeAll
  static void setUp(@LocalServerPort int port, @Autowired TestWebClientFactory factory) {
    cdClient = factory.webClient("cd-agent").baseUrl("https://localhost:" + port).build();
    rdClient = factory.webClient("rd-agent").baseUrl("https://localhost:" + port).build();
  }

  @Test
  void successfulRequest() throws IOException {
    var fhirGenerator =
        FhirGenerators.gpasGetOrCreateResponse(
            fromList(List.of("id-144218", "Salt_id-144218", "PT336H_id-144218")),
            fromList(List.of("469680023", "123", "12345")));

    List.of("id-144218", "Salt_id-144218", "PT336H_id-144218")
        .forEach(
            key ->
                gpas.when(
                        request()
                            .withMethod("POST")
                            .withPath("/ttp-fhir/fhir/gpas/$pseudonymizeAllowCreate")
                            .withContentType(APPLICATION_FHIR_JSON)
                            .withBody(
                                json(
                                    """
                                    { "resourceType": "Parameters",
                                      "parameter": [
                                        {"name": "target", "valueString": "MII"},
                                        {"name": "original", "valueString": "%s"}]}
                                    """
                                        .formatted(key),
                                    ONLY_MATCHING_FIELDS)))
                    .respond(
                        response()
                            .withBody(
                                fhirGenerator.generateString(),
                                MediaType.create("application", "fhir+json"))));

    var response =
        doPost(
            ofEntries(
                entry(
                    "tcaDomains",
                    ofEntries(
                        entry("pseudonym", "MII"),
                        entry("salt", "MII"),
                        entry("dateShift", "MII"))),
                entry("patientId", "id-144218"),
                entry("ids", Set.of("id-144218", "id-244194")),
                entry("maxDateShift", ofDays(14).getSeconds())));

    create(response)
        .assertNext(
            res -> {
              assertThat(res).isNotNull();
              assertThat(res.dateShiftValue().toDays()).isBetween(-140L, 140L);
              assertThat(res.originalToTransportIDMap()).containsKeys("id-144218", "id-244194");
            })
        .verifyComplete();
  }

  @Test
  void firstRequestToGpasFails() {
    var statusCodes = new LinkedList<>(List.of(500));

    var map =
        Map.of("id-144218", "469680023", "Salt_id-144218", "123", "PT336H_id-144218", "12345");

    List.of("id-144218", "Salt_id-144218", "PT336H_id-144218")
        .forEach(
            key ->
                gpas.when(
                        request()
                            .withMethod("POST")
                            .withPath("/ttp-fhir/fhir/gpas/$pseudonymizeAllowCreate")
                            .withContentType(APPLICATION_FHIR_JSON)
                            .withBody(
                                json(
                                    """
                                    { "resourceType": "Parameters",
                                      "parameter": [
                                        {"name": "target", "valueString": "MII"},
                                        {"name": "original", "valueString": "%s"}]}
                                    """
                                        .formatted(key),
                                    ONLY_MATCHING_FIELDS)))
                    .respond(
                        request ->
                            Optional.ofNullable(statusCodes.poll())
                                .filter(statusCode -> statusCode >= 400)
                                .map(statusCode -> response().withStatusCode(statusCode))
                                .orElseGet(
                                    () -> {
                                      try {
                                        return response()
                                            .withBody(
                                                FhirGenerators.gpasGetOrCreateResponse(
                                                        () -> key, () -> map.get(key))
                                                    .generateString(),
                                                MediaType.create("application", "fhir+json"));
                                      } catch (IOException e) {
                                        throw new RuntimeException(e);
                                      }
                                    })));

    var response =
        doPost(
            ofEntries(
                entry(
                    "tcaDomains",
                    ofEntries(
                        entry("pseudonym", "MII"),
                        entry("salt", "MII"),
                        entry("dateShift", "MII"))),
                entry("patientId", "id-144218"),
                entry("ids", Set.of("id-144218", "id-244194")),
                entry("maxDateShift", ofDays(14).getSeconds())));

    create(response)
        .assertNext(
            res -> {
              assertThat(res).isNotNull();
              assertThat(res.dateShiftValue().toDays()).isBetween(-140L, 140L);
              assertThat(res.originalToTransportIDMap()).containsKeys("id-144218", "id-244194");
            })
        .verifyComplete();
  }

  @Test
  void rejectInvalidIds() {
    var response =
        rdClient
            .post()
            .uri("/api/v2/rd/resolve-pseudonyms")
            .contentType(APPLICATION_JSON)
            .body(fromValue(Set.of("username=Guest'%0AUser:'Admin")))
            .accept(APPLICATION_JSON)
            .retrieve()
            .toBodilessEntity();

    create(response)
        .expectErrorSatisfies(
            e -> {
              assertThat(e).isInstanceOf(WebClientResponseException.class);
              assertThat(((WebClientResponseException) e).getStatusCode())
                  .isEqualTo(HttpStatus.BAD_REQUEST);
            })
        .verify();
  }

  @Test
  void getTransportIdsAndDateShiftingValuesAndFetchPseudonyms() throws IOException {
    var fhirGenerator =
        FhirGenerators.gpasGetOrCreateResponse(
            fromList(List.of("id-144218", "Salt_id-144218", "PT336H_id-144218")),
            fromList(List.of("469680023", "123", "12345")));

    List.of("id-144218", "Salt_id-144218", "PT336H_id-144218")
        .forEach(
            key ->
                gpas.when(
                        request()
                            .withMethod("POST")
                            .withPath("/ttp-fhir/fhir/gpas/$pseudonymizeAllowCreate")
                            .withContentType(APPLICATION_FHIR_JSON)
                            .withBody(
                                json(
                                    """
                                    { "resourceType": "Parameters",
                                      "parameter": [
                                        {"name": "target", "valueString": "MII"},
                                        {"name": "original", "valueString": "%s"}]}
                                    """
                                        .formatted(key),
                                    ONLY_MATCHING_FIELDS)))
                    .respond(
                        response()
                            .withBody(
                                fhirGenerator.generateString(),
                                MediaType.create("application", "fhir+json"))));

    var tIDMapName =
        doPost(
                ofEntries(
                    entry(
                        "tcaDomains",
                        ofEntries(
                            entry("pseudonym", "MII"),
                            entry("salt", "MII"),
                            entry("dateShift", "MII"))),
                    entry("patientId", "id-144218"),
                    entry("ids", Set.of("id-144218", "id-244194")),
                    entry("maxDateShift", ofDays(14).getSeconds())))
            .block()
            .tIDMapName();

    var response =
        rdClient
            .post()
            .uri("/api/v2/rd/resolve-pseudonyms")
            .contentType(APPLICATION_JSON)
            .body(fromValue(tIDMapName))
            .accept(APPLICATION_JSON)
            .retrieve()
            .toBodilessEntity();

    create(response)
        .assertNext(
            res -> {
              assertThat(res).isNotNull();
              assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
            })
        .verifyComplete();
  }

  private static Mono<PseudonymizeResponse> doPost(Map<String, Object> body) {
    return cdClient
        .post()
        .uri("/api/v2/cd/transport-ids-and-date-shifting-values")
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(PseudonymizeResponse.class);
  }

  @AfterEach
  void tearDown() {
    gics.reset();
    gpas.reset();
  }
}
