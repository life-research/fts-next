package care.smith.fts.tca.deidentification;

import java.util.Map;
import java.util.Set;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface PseudonymProvider {

  /**
   * @param ids to transport ids
   * @param pseudonymDomain the domain
   * @param saltDomain
   * @return the <code>PseudonymResponse</code>
   */
  Mono<Tuple2<String, Map<String, String>>> retrieveTransportIds(
      String patientId, Set<String> ids, String pseudonymDomain, String saltDomain);

  /**
   * Retrieves the mapping of <code>transportId</code> to <code>secureId</code> using the mappings
   * name.
   *
   * @param tIDMapName the transport id map name
   * @return the mapped tid:sid
   */
  Mono<Map<String, String>> fetchPseudonymizedIds(String tIDMapName);
}
