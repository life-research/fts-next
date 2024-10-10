package care.smith.fts.cda.impl;

import static care.smith.fts.cda.services.deidentifhir.DeidentifhirUtils.generateRegistry;
import static care.smith.fts.util.RetryStrategies.defaultRetryStrategy;

import care.smith.fts.api.ConsentedPatientBundle;
import care.smith.fts.api.TransportBundle;
import care.smith.fts.api.cda.Deidentificator;
import care.smith.fts.cda.services.deidentifhir.DeidentifhirUtils;
import care.smith.fts.cda.services.deidentifhir.IDATScraper;
import care.smith.fts.util.error.TransferProcessException;
import care.smith.fts.util.tca.*;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
class DeidentifhirStep implements Deidentificator {
  private final WebClient httpClient;
  private final String domain;
  private final Duration maxDateShift;
  private final com.typesafe.config.Config deidentifhirConfig;
  private final com.typesafe.config.Config scraperConfig;
  private final MeterRegistry meterRegistry;

  public DeidentifhirStep(
      WebClient httpClient,
      String domain,
      Duration maxDateShift,
      com.typesafe.config.Config deidentifhirConfig,
      com.typesafe.config.Config scraperConfig,
      MeterRegistry meterRegistry) {
    this.httpClient = httpClient;
    this.domain = domain;
    this.maxDateShift = maxDateShift;
    this.deidentifhirConfig = deidentifhirConfig;
    this.scraperConfig = scraperConfig;
    this.meterRegistry = meterRegistry;
  }

  @Override
  public Mono<TransportBundle> deidentify(ConsentedPatientBundle bundle) {
    var patient = bundle.consentedPatient();
    var idatScraper = new IDATScraper(scraperConfig, patient);
    var ids = idatScraper.gatherIDs(bundle.bundle());
    return fetchTransportIdsAndDateShiftingValues(patient.id(), ids)
        .map(
            response -> {
              var transportIDs = response.originalToTransportIDMap();
              var dateShiftValue = response.dateShiftValue();
              var registry = generateRegistry(patient.id(), transportIDs, dateShiftValue);
              var deidentified =
                  DeidentifhirUtils.deidentify(
                      deidentifhirConfig, registry, bundle.bundle(), patient.id(), meterRegistry);
              return new TransportBundle(deidentified, response.tIDMapName());
            });
  }

  private Mono<PseudonymizeResponse> fetchTransportIdsAndDateShiftingValues(
      String patientId, Set<String> ids) {
    PseudonymizeRequest request = new PseudonymizeRequest(patientId, ids, domain, maxDateShift);

    log.trace("Fetch TIDs and date shifting values for {} IDs", ids.size());

    return httpClient
        .post()
        .uri("/api/v2/cd/transport-ids-and-date-shifting-values")
        .headers(h -> h.setContentType(MediaType.APPLICATION_JSON))
        .bodyValue(request)
        .retrieve()
        .onStatus(
            r -> r.equals(HttpStatus.BAD_REQUEST),
            s ->
                s.bodyToMono(ProblemDetail.class)
                    .flatMap(b -> Mono.error(new TransferProcessException(b.getDetail()))))
        .bodyToMono(PseudonymizeResponse.class)
        .timeout(Duration.ofSeconds(30))
        .retryWhen(defaultRetryStrategy(meterRegistry, "transport-ids-and-date-shifting-values"))
        .doOnError(
            e ->
                log.error(
                    "Cannot fetch transport deidentification data from TCA: {}", e.getMessage()));
  }
}
