package care.smith.fts.tca.consent;

import static care.smith.fts.util.ConsentedPatientExtractor.hasAllPolicies;
import static care.smith.fts.util.FhirUtils.*;

import com.google.common.base.Predicates;
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/** This class provides functionalities for handling FHIR consents using an HTTP client. */
@Slf4j
public class FhirConsentProvider implements ConsentProvider {
  private final int defaultPageSize;

  private final WebClient httpClient;
  private final PolicyHandler policyHandler;

  /**
   * Constructs a FhirConsentProvider with the specified parameters.
   *
   * @param httpClient the WebClient used for HTTP requests
   * @param policyHandler the handler for policy-related operations
   * @param defaultPageSize the default page size for paginated results
   */
  public FhirConsentProvider(
      WebClient httpClient, PolicyHandler policyHandler, int defaultPageSize) {
    this.policyHandler = policyHandler;
    this.httpClient = httpClient;
    this.defaultPageSize = defaultPageSize;
  }

  /**
   * Retrieves the first page (with defaultPageSize) of consented patients for the given domain and
   * policies.
   *
   * @param domain the domain to search for consented patients
   * @param policies the set of policies to filter patients
   * @return a Mono emitting a Bundle of consented patients
   */
  @Override
  public Mono<Bundle> consentedPatientsPage(
      String domain, String policySystem, HashSet<String> policies) {
    return consentedPatientsPage(domain, policySystem, policies, 0, defaultPageSize);
  }

  /**
   * Retrieves a page of consented patients for the given domain and policies, with pagination.
   *
   * @param domain the domain to search for consented patients
   * @param policies the set of policies to filter patients
   * @param from the starting index for pagination
   * @param count the number of patients to retrieve
   * @return a Mono emitting a Bundle of consented patients
   */
  @Override
  public Mono<Bundle> consentedPatientsPage(
      String domain, String policySystem, HashSet<String> policies, int from, int count) {
    HashSet<String> policiesToCheck = policyHandler.getPoliciesToCheck(policies);
    if (policiesToCheck.isEmpty()) {
      return Mono.just(new Bundle());
    }
    return fetchConsentedPatientsFromGics(policySystem, policiesToCheck, from, count);
  }

  /**
   * Fetches a page of consented patients from the GICS system, filtered by policies.
   *
   * @param policiesToCheck the set of policies to check
   * @param from the starting index for pagination
   * @param count the number of patients to retrieve
   * @return a Mono emitting a filtered Bundle of consented patients
   */
  private Mono<Bundle> fetchConsentedPatientsFromGics(
      String policySystem, HashSet<String> policiesToCheck, int from, int count) {
    return fetchConsentPageFromGics(from, count)
        .map(outerBundle -> filterOuterBundle(policySystem, policiesToCheck, outerBundle));
  }

  /**
   * Filters an outer Bundle based on the provided policies. The outer Bundle contains Bundles that
   * in turn contain with Consent, Patient, and others. More info can be found here: <a
   * href="https://www.ths-greifswald.de/wp-content/uploads/tools/fhirgw/ig/2023-1-2/ImplementationGuide-markdown-Einwilligungsmanagement-Operations-allConsentsForDomain.html">...</a>
   *
   * @param policiesToCheck the set of policies to check
   * @param outerBundle the outer Bundle to filter
   * @return a filtered Bundle
   */
  private Bundle filterOuterBundle(
      String policySystem, HashSet<String> policiesToCheck, Bundle outerBundle) {
    return typedResourceStream(outerBundle, Bundle.class)
        .filter(b -> hasAllPolicies(policySystem, b, policiesToCheck))
        .map(FhirConsentProvider::filterInnerBundle)
        .collect(toBundle());
  }

  /**
   * Filters an inner Bundle to include only Patient and Consent resources.
   *
   * @param b the inner Bundle to filter
   * @return a filtered Bundle
   */
  private static Bundle filterInnerBundle(Bundle b) {
    return resourceStream(b)
        .filter(Predicates.or(Patient.class::isInstance, Consent.class::isInstance))
        .collect(toBundle());
  }

  /**
   * Fetches a page of consents from the GICS system.
   *
   * @param from the starting index for pagination
   * @param count the number of consents to retrieve
   * @return a Mono emitting a Bundle of consents
   */
  private Mono<Bundle> fetchConsentPageFromGics(int from, int count) {
    int to = from + count;
    return httpClient
        .post()
        .uri("/$allConsentsForDomain?_count=%s&_offset=%s".formatted(to, from))
        .headers(h -> h.setContentType(MediaType.APPLICATION_JSON))
        .retrieve()
        .bodyToMono(Bundle.class);
  }
}
