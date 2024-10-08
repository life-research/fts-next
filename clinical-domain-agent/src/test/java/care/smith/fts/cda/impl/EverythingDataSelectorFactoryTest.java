package care.smith.fts.cda.impl;

import static org.assertj.core.api.Assertions.assertThat;

import care.smith.fts.cda.services.FhirResolveConfig;
import care.smith.fts.util.HttpClientConfig;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
class EverythingDataSelectorFactoryTest {

  @Autowired WebClient.Builder client;
  @Autowired MeterRegistry meterRegistry;
  @Autowired WebClientSsl ssl;

  private EverythingDataSelectorFactory factory;

  @BeforeEach
  void setUp() {
    factory = new EverythingDataSelectorFactory(client, ssl, meterRegistry);
  }

  @Test
  void testConfigType() {
    assertThat(factory.getConfigType()).isNotNull();
  }

  @Test
  void testCreateWithoutResolver() {
    assertThat(
            factory.create(
                null, new EverythingDataSelectorConfig(new HttpClientConfig("http://localhost"))))
        .isNotNull();
  }

  @Test
  void testCreateWithResolver() {
    assertThat(
            factory.create(
                null,
                new EverythingDataSelectorConfig(
                    new HttpClientConfig("http://localhost"),
                    new FhirResolveConfig("https://patient-identifier.example.com"))))
        .isNotNull();
  }
}
