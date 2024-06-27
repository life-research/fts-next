package care.smith.fts.tca.rest;

import care.smith.fts.tca.consent.ConsentProvider;
import care.smith.fts.util.MediaTypes;
import care.smith.fts.util.tca.ConsentRequest;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = "api/v2")
public class ConsentController {
  private final ConsentProvider consentProvider;
  private final int defaultPageSize;

  @Autowired
  ConsentController(
      ConsentProvider consentProvider, @Qualifier("defaultPageSize") int defaultPageSize) {
    this.consentProvider = consentProvider;
    this.defaultPageSize = defaultPageSize;
  }

  @PostMapping(
      value = "/cd/consented-patients",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaTypes.APPLICATION_FHIR_JSON_VALUE)
  public Mono<ResponseEntity<Bundle>> consentedPatients(
      @Validated(ConsentRequest.class) @RequestBody Mono<ConsentRequest> request,
      UriComponentsBuilder uriBuilder,
      @RequestParam("from") Optional<Integer> from,
      @RequestParam("count") Optional<Integer> count) {
    var response =
        request.flatMap(
            r ->
                consentProvider.consentedPatientsPage(
                    r.domain(),
                    r.policySystem(),
                    r.policies(),
                    uriBuilder,
                    from.orElse(0),
                    count.orElse(defaultPageSize)));
    return response.map(
        consentedPatients -> new ResponseEntity<>(consentedPatients, HttpStatus.OK));
  }
}
