package care.smith.fts.rda.impl;

import care.smith.fts.api.rda.BundleSender;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component("fhirStoreBundleSender")
public class FhirStoreBundleSenderFactory
    implements BundleSender.Factory<FhirStoreBundleSenderConfig> {

  private final WebClient.Builder builder;
  private final WebClientSsl ssl;
  private final MeterRegistry meterRegistry;

  public FhirStoreBundleSenderFactory(
      WebClient.Builder builder, WebClientSsl ssl, MeterRegistry meterRegistry) {
    this.builder = builder;
    this.ssl = ssl;
    this.meterRegistry = meterRegistry;
  }

  @Override
  public Class<FhirStoreBundleSenderConfig> getConfigType() {
    return FhirStoreBundleSenderConfig.class;
  }

  @Override
  public BundleSender create(
      BundleSender.Config commonConfig, FhirStoreBundleSenderConfig implConfig) {
    return new FhirStoreBundleSender(implConfig.server().createClient(builder, ssl), meterRegistry);
  }
}
