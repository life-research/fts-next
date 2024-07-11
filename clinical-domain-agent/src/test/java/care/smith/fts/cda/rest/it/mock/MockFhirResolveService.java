package care.smith.fts.cda.rest.it.mock;

import static care.smith.fts.util.MediaTypes.APPLICATION_FHIR_JSON_VALUE;
import static org.mockserver.model.HttpResponse.response;

import care.smith.fts.test.FhirGenerator;
import care.smith.fts.test.FhirGenerator.Fixed;
import care.smith.fts.test.FhirGenerator.UUID;
import care.smith.fts.util.FhirUtils;
import java.io.IOException;
import lombok.Builder;
import org.hl7.fhir.r4.model.Bundle;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Delay;
import org.mockserver.model.HttpError;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.MediaType;

@Builder
public class MockFhirResolveService {

  private final MockServerClient hds;
  private final HttpRequest mockRequestSpec;

  public MockFhirResolveService(MockServerClient hds, HttpRequest mockRequestSpec) {
    this.hds = hds;
    this.mockRequestSpec = mockRequestSpec;
  }

  public void success(String patientId) throws IOException {
    var fhirResolveGen = new FhirGenerator("FhirResolveSearchRequestTemplate.json");
    fhirResolveGen.replaceTemplateFieldWith("$PATIENT_ID", new Fixed(patientId));
    fhirResolveGen.replaceTemplateFieldWith("$HDS_ID", new UUID());

    hds.when(mockRequestSpec)
        .respond(
            response()
                .withStatusCode(200)
                .withContentType(MediaType.parse(APPLICATION_FHIR_JSON_VALUE))
                .withBody(
                    FhirUtils.fhirResourceToString(
                        fhirResolveGen.generateBundle(1, 1).getEntryFirstRep().getResource())));
  }

  public void isDown() {
    hds.when(mockRequestSpec).error(HttpError.error().withDropConnection(true));
  }

  public void timeout() {
    hds.when(mockRequestSpec).respond(request -> null, Delay.minutes(10));
  }

  public void wrongContentType() {
    hds.when(mockRequestSpec)
        .respond(
            response()
                .withStatusCode(200)
                .withContentType(MediaType.PLAIN_TEXT_UTF_8)
                .withBody(FhirUtils.fhirResourceToString(new Bundle())));
  }

  public void moreThanOneResult() throws IOException {
    var fhirResolveGen = new FhirGenerator("FhirResolveSearchRequestTemplate.json");
    fhirResolveGen.replaceTemplateFieldWith("$PATIENT_ID", new Fixed("id1"));
    fhirResolveGen.replaceTemplateFieldWith("$HDS_ID", new UUID());

    hds.when(mockRequestSpec)
        .respond(
            response()
                .withStatusCode(200)
                .withContentType(MediaType.parse(APPLICATION_FHIR_JSON_VALUE))
                .withBody(FhirUtils.fhirResourceToString(fhirResolveGen.generateBundle(2, 2))));
  }

  public void emptyBundle() {
    hds.when(mockRequestSpec)
        .respond(
            response()
                .withStatusCode(200)
                .withContentType(MediaType.parse(APPLICATION_FHIR_JSON_VALUE))
                .withBody(FhirUtils.fhirResourceToString(new Bundle())));
  }
}
