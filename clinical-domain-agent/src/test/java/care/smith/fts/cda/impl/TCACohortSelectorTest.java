package care.smith.fts.cda.impl;

import static care.smith.fts.util.FhirUtils.toBundle;
import static care.smith.fts.util.auth.HttpClientAuthMethod.AuthMethod.NONE;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.reactive.function.client.ClientResponse.create;
import static org.springframework.web.reactive.function.client.WebClient.builder;
import static reactor.core.publisher.Mono.just;
import static reactor.test.StepVerifier.create;

import care.smith.fts.util.HttpClientConfig;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

@Slf4j
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class TCACohortSelectorTest {

  @Autowired MeterRegistry meterRegistry;

  private static final Set<String> POLICIES = Set.of("any");

  private static final String PID_SYSTEM =
      "https://ths-greifswald.de/fhir/gics/identifiers/Pseudonym";
  private static final String POLICY_SYSTEM =
      "https://ths-greifswald.de/fhir/CodeSystem/gics/Policy";

  @Mock ClientResponse response;

  private TCACohortSelectorConfig config;

  @BeforeEach
  void setUp() {
    var address = "http://localhost";
    var server = new HttpClientConfig(address, NONE);
    config = new TCACohortSelectorConfig(server, PID_SYSTEM, POLICY_SYSTEM, POLICIES, "MII");
  }

  @Test
  void responseInvalidErrors() {
    var client = builder().exchangeFunction(req -> just(response));
    given(response.statusCode()).willReturn(OK);
    given(response.bodyToMono(String.class)).willReturn(Mono.just(""));
    var cohortSelector =
        new TCACohortSelector(config, config.server().createClient(client, null), meterRegistry);

    create(cohortSelector.selectCohort()).expectError().verify();
  }

  @Test
  void badRequestErrors() {
    given(response.bodyToMono(ProblemDetail.class))
        .willReturn(Mono.just(ProblemDetail.forStatusAndDetail(BAD_REQUEST, "Some TCA Error")));
    var client = builder().exchangeFunction(req -> just(response));
    var cohortSelector =
        new TCACohortSelector(config, config.server().createClient(client, null), meterRegistry);

    create(cohortSelector.selectCohort()).expectError().verify();
  }

  @Test
  void consentBundleSucceeds() {
    var client = builder().exchangeFunction(req -> just(response));
    given(response.statusCode()).willReturn(OK);
    Bundle inner =
        Stream.of(
                new Patient()
                    .addIdentifier(
                        new Identifier().setSystem(PID_SYSTEM).setValue("patient-122651")),
                new Consent().setProvision(denyProvision()))
            .collect(toBundle());
    Bundle outer = Stream.of(inner).collect(toBundle());
    given(response.bodyToMono(Bundle.class)).willReturn(Mono.just(outer));
    var cohortSelector =
        new TCACohortSelector(config, config.server().createClient(client, null), meterRegistry);

    create(cohortSelector.selectCohort()).expectNextCount(1).verifyComplete();
  }

  @Test
  void emptyOuterBundleGivesEmptyResult() {
    var client = builder().exchangeFunction(req -> just(response));
    given(response.statusCode()).willReturn(OK);
    Bundle outer = Stream.<Resource>of().collect(toBundle());
    given(response.bodyToMono(Bundle.class)).willReturn(Mono.just(outer));
    var cohortSelector =
        new TCACohortSelector(config, config.server().createClient(client, null), meterRegistry);

    create(cohortSelector.selectCohort()).verifyComplete();
  }

  @Test
  void emptyInnerBundleGivesEmptyResult() {
    var client = builder().exchangeFunction(req -> just(response));
    given(response.statusCode()).willReturn(OK);
    Bundle outer = Stream.of(Stream.<Resource>of().collect(toBundle())).collect(toBundle());
    given(response.bodyToMono(Bundle.class)).willReturn(Mono.just(outer));
    var cohortSelector =
        new TCACohortSelector(config, config.server().createClient(client, null), meterRegistry);

    create(cohortSelector.selectCohort()).verifyComplete();
  }

  private static Consent.provisionComponent denyProvision() {
    return new Consent.provisionComponent()
        .setType(Consent.ConsentProvisionType.DENY)
        .addProvision(permitProvision());
  }

  private static Consent.provisionComponent permitProvision() {
    var policy =
        new CodeableConcept().addCoding(new Coding().setSystem(POLICY_SYSTEM).setCode("any"));
    return new Consent.provisionComponent()
        .setType(Consent.ConsentProvisionType.PERMIT)
        .setCode(List.of(policy))
        .setPeriod(new Period().setStart(new Date(1)).setEnd(new Date(2)));
  }
}
