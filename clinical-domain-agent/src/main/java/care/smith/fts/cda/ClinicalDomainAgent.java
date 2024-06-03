package care.smith.fts.cda;

import static ca.uhn.fhir.rest.client.api.IRestfulClientFactory.*;
import static org.apache.hc.core5.util.Timeout.ofMilliseconds;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import java.util.concurrent.ForkJoinPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
public class ClinicalDomainAgent {

  public static void main(String... args) {
    SpringApplication.run(ClinicalDomainAgent.class, args);
  }

  @Bean
  public FhirContext fhirContext() {
    return FhirContext.forR4();
  }

  @Bean
  public IRestfulClientFactory fhirClientFactory(FhirContext fhir) {
    IRestfulClientFactory factory = fhir.getRestfulClientFactory();
    factory.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
    factory.setSocketTimeout(DEFAULT_SOCKET_TIMEOUT);
    factory.setConnectionRequestTimeout(IRestfulClientFactory.DEFAULT_CONNECTION_REQUEST_TIMEOUT);
    return factory;
  }

  @Bean
  public PoolingHttpClientConnectionManager connectionManager() {
    return new PoolingHttpClientConnectionManager();
  }

  @Bean
  public HttpClientBuilder clientBuilder(PoolingHttpClientConnectionManager manager) {
    RequestConfig requestConfig =
        RequestConfig.custom()
            .setConnectionRequestTimeout(ofMilliseconds(DEFAULT_CONNECTION_REQUEST_TIMEOUT))
            .setResponseTimeout(ofMilliseconds(DEFAULT_SOCKET_TIMEOUT))
            .build();
    return HttpClients.custom()
        .setConnectionManager(manager)
        .setDefaultRequestConfig(requestConfig);
  }

  @Bean
  IGenericClient client(FhirContext fhir) {
    return fhir.newRestfulGenericClient("http://localhost");
  }

  @Bean("transfer-process")
  public ForkJoinPool transferProcessPool(
      @Value("${transferProcess.parallelism}") int parallelism) {
    return new ForkJoinPool(parallelism);
  }
}
