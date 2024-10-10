package care.smith.fts.tca.deidentification;

import static care.smith.fts.util.RetryStrategies.defaultRetryStrategy;
import static java.util.stream.Collectors.toMap;

import care.smith.fts.tca.deidentification.configuration.PseudonymizationConfiguration;
import care.smith.fts.util.tca.PseudonymizeResponse;
import care.smith.fts.util.tca.TCADomains;
import com.google.common.hash.Hashing;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

@Slf4j
@Component
public class FhirPseudonymProvider implements PseudonymProvider {
  private final GpasClient gpasClient;
  private final PseudonymizationConfiguration configuration;
  private final RedissonClient redisClient;

  private final MeterRegistry meterRegistry;
  private final RandomStringGenerator randomStringGenerator;

  public FhirPseudonymProvider(
      GpasClient gpasClient,
      RedissonClient redisClient,
      PseudonymizationConfiguration configuration,
      MeterRegistry meterRegistry,
      RandomStringGenerator randomStringGenerator) {
    this.gpasClient = gpasClient;
    this.configuration = configuration;
    this.redisClient = redisClient;
    this.meterRegistry = meterRegistry;
    this.randomStringGenerator = randomStringGenerator;
  }

  /**
   * For all provided IDs fetch the id:pid pairs from gPAS. Then create TransportIDs (id:tid pairs).
   * Store tid:pid in the key-value-store.
   *
   * @param ids the IDs to pseudonymize
   * @param tcaDomains the domains in gPAS
   * @return Map<TID, PID>
   */
  @Override
  public Mono<PseudonymizeResponse> retrieveTransportIds(
      String patientId, Set<String> ids, TCADomains tcaDomains, Duration maxDateShift) {
    log.trace("retrieveTransportIds patientId={}, ids={}", patientId, ids);
    var saltKey = "Salt_" + patientId;
    var tIDMapName = randomStringGenerator.generate();
    var originalToTransportIDMapping =
        ids.stream().collect(toMap(id -> id, id -> randomStringGenerator.generate()));
    var rMap = redisClient.reactive().getMapCache(tIDMapName);
    var dateShiftKey = "%s_%s".formatted(maxDateShift.toString(), patientId);
    return rMap.expire(Duration.ofSeconds(configuration.getTransportIdTTLinSeconds()))
        .then(
            Mono.zip(
                gpasClient.fetchOrCreatePseudonyms(tcaDomains.pseudonym(), patientId),
                gpasClient.fetchOrCreatePseudonyms(tcaDomains.salt(), saltKey),
                gpasClient.fetchOrCreatePseudonyms(tcaDomains.dateShift(), dateShiftKey)))
        .flatMap(
            TupleUtils.function(
                (originalToSecureIDMapping, salt, dateShiftSalt) -> {
                  var transportToSecureIDMapping =
                      getTransportToSecureIDMapping(salt, originalToTransportIDMapping);
                  replacePatientIdMapping(
                      patientId,
                      originalToSecureIDMapping,
                      transportToSecureIDMapping,
                      originalToTransportIDMapping);

                  var dateShift = getDateShift(maxDateShift, dateShiftSalt);
                  return rMap.putAll(transportToSecureIDMapping).thenReturn(dateShift);
                }))
        .map(
            cdDateShift ->
                new PseudonymizeResponse(tIDMapName, originalToTransportIDMapping, cdDateShift));
  }

  private static Map<String, String> getTransportToSecureIDMapping(
      String transportSalt, Map<String, String> originalToTransportIDMapping) {
    var sha256 = Hashing.sha256();
    return originalToTransportIDMapping.entrySet().stream()
        .collect(
            toMap(
                Entry::getValue,
                entry ->
                    sha256
                        .hashString(transportSalt + entry.getKey(), StandardCharsets.UTF_8)
                        .toString()));
  }

  private static Duration getDateShift(Duration maxDateShift, String dateShiftSalt) {
    return new FhirShiftedDatesProvider().generateDateShift(dateShiftSalt, maxDateShift);
  }

  private record Result(Duration cdDateShift, Duration rdDateShift) {}

  /**
   * With this function we make sure that the patient's ID in the RDA is the de-identified ID stored
   * in gPAS. This ensures that we can re-identify patients.
   */
  private static void replacePatientIdMapping(
      String patientId,
      String patientIdPseudonym,
      Map<String, String> transportToSecureIDMapping,
      Map<String, String> originalToTransportIDMapping) {
    if (originalToTransportIDMapping.keySet().stream()
        .anyMatch(id -> id.endsWith("Patient." + patientId))) {
      transportToSecureIDMapping.put(
          originalToTransportIDMapping.get("Patient." + patientId), patientIdPseudonym);
    }
  }

  @Override
  public Mono<Map<String, String>> fetchPseudonymizedIds(String tIDMapName) {
    RedissonReactiveClient redis = redisClient.reactive();
    return Mono.just(tIDMapName)
        .flatMap(name -> redis.getMapCache(name).readAllMap())
        .map(
            m ->
                m.entrySet().stream()
                    .collect(toMap(e -> (String) e.getKey(), e -> (String) e.getValue())))
        .retryWhen(defaultRetryStrategy(meterRegistry, "fetchPseudonymizedIds"));
  }
}
