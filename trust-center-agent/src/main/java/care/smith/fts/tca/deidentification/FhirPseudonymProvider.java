package care.smith.fts.tca.deidentification;

import static care.smith.fts.util.MediaTypes.APPLICATION_FHIR_JSON;
import static care.smith.fts.util.RetryStrategies.defaultRetryStrategy;

import care.smith.fts.tca.deidentification.configuration.PseudonymizationConfiguration;
import care.smith.fts.util.error.UnknownDomainException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
@Component
public class FhirPseudonymProvider implements PseudonymProvider {
  private static final String ALLOWED_PSEUDONYM_CHARS =
      "0123456789abcdefghijklmnopqrstuvwxyz-_ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  private final WebClient httpClient;
  private final PseudonymizationConfiguration configuration;
  private final RedissonClient redisClient;
  private final RandomGenerator randomGenerator;

  public FhirPseudonymProvider(
      @Qualifier("gpasFhirHttpClient") WebClient httpClient,
      RedissonClient redisClient,
      PseudonymizationConfiguration configuration,
      RandomGenerator randomGenerator) {
    this.httpClient = httpClient;
    this.configuration = configuration;
    this.redisClient = redisClient;
    this.randomGenerator = randomGenerator;
  }

  /**
   * For all provided IDs fetch the id:pid pairs from gPAS. Then create TransportIDs (id:tid pairs).
   * Store tid:pid in the key-value-store.
   *
   * @param ids the IDs to pseudonymize
   * @param domain the domain used in gPAS
   * @return Map<TID, PID>
   */
  @Override
  public Mono<Tuple2<String, Map<String, String>>> retrieveTransportIds(
      Set<String> ids, String domain) {
    RedissonReactiveClient redis = redisClient.reactive();
    var mapName = String.valueOf(ids.hashCode());
    return Mono.just(mapName)
        .map(redis::getMap)
        .flatMapMany(
            map -> fetchOrCreatePseudonyms(domain, ids).map(pidTuple -> Tuples.of(map, pidTuple)))
        .flatMap(
            tuple -> {
              var map = tuple.getT1();
              var pidTuple = tuple.getT2();
              var id = pidTuple.getT1();
              var pid = pidTuple.getT2();
              var tid = generateTID();
              return map.fastPut(tid, pid).map(i -> Tuples.of(id, tid));
            })
        .collectMap(Tuple2::getT1, Tuple2::getT2)
        .map(m -> Tuples.of(mapName, m));
  }

  private String generateTID() {
    return randomGenerator
        .ints(9, 0, ALLOWED_PSEUDONYM_CHARS.length())
        .mapToObj(ALLOWED_PSEUDONYM_CHARS::charAt)
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
        .toString();
  }

  /**
   * @return Flux of (id, pid) tuples
   */
  private Flux<Tuple2<String, String>> fetchOrCreatePseudonyms(String domain, Set<String> ids) {
    var idParams =
        Stream.concat(
            Stream.of(Map.of("name", "target", "valueString", domain)),
            ids.stream().map(id -> Map.of("name", "original", "valueString", id)));
    var params = Map.of("resourceType", "Parameters", "parameter", idParams.toList());

    log.trace("fetchOrCreatePseudonyms for domain: %s and %d ids".formatted(domain, ids.size()));
    var t0 = LocalDateTime.now();
    return httpClient
        .post()
        .uri("/$pseudonymizeAllowCreate")
        .headers(h -> h.setContentType(APPLICATION_FHIR_JSON))
        .bodyValue(params)
        .headers(h -> h.setAccept(List.of(APPLICATION_FHIR_JSON)))
        .retrieve()
        .onStatus(
            r -> r.equals(HttpStatus.BAD_REQUEST), FhirPseudonymProvider::handleGpasBadRequest)
        .bodyToMono(GpasParameterResponse.class)
        .timeout(Duration.ofSeconds(20))
        .doOnNext(
            i -> {
              var d = Duration.between(t0, LocalDateTime.now());
              log.trace("Duration for fetching pseud: {}", d);
            })
        .doOnError(e -> log.error(e.getMessage()))
        .retryWhen(defaultRetryStrategy())
        .doOnNext(r -> log.trace("$pseudonymize response: {} parameters", r.parameter().size()))
        .map(GpasParameterResponse::getMappedID)
        .flatMapMany(
            map -> Flux.fromIterable(map.entrySet()).map(e -> Tuples.of(e.getKey(), e.getValue())));
  }

  private static Mono<Throwable> handleGpasBadRequest(ClientResponse r) {
    return r.bodyToMono(OperationOutcome.class)
        .flatMap(
            b -> {
              var diagnostics = b.getIssueFirstRep().getDiagnostics();
              log.error("Bad Request: {}", diagnostics);
              if (diagnostics != null && diagnostics.startsWith("Unknown domain")) {
                return Mono.error(new UnknownDomainException(diagnostics));
              } else {
                return Mono.error(new UnknownError());
              }
            });
  }

  @Override
  public Mono<Map<String, String>> fetchPseudonymizedIds(String mapName) {
    RedissonReactiveClient redis = redisClient.reactive();
    return Mono.just(mapName)
        .flatMap(name -> redis.getMap(name).readAllMap())
        .map(
            m ->
                m.entrySet().stream()
                    .collect(
                        Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue())));
  }
}
