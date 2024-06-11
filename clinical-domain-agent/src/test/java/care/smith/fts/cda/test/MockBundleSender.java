package care.smith.fts.cda.test;

import care.smith.fts.api.BundleSender;
import java.util.Set;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Component;

@Component("mockBundleSender")
public class MockBundleSender implements BundleSender.Factory<Bundle, MockBundleSender.Config> {
  @Override
  public Class<Config> getConfigType() {
    return Config.class;
  }

  @Override
  public BundleSender<Bundle> create(BundleSender.Config commonConfig, Config implConfig) {
    return new Impl(implConfig);
  }

  public record Config(Set<String> expect) {}

  public static class Impl implements BundleSender<Bundle> {
    private final MockBundleSender.Config implConfig;

    public Impl(MockBundleSender.Config implConfig) {
      this.implConfig = implConfig;
    }

    @Override
    public boolean send(Bundle bundle, String ignored) {
      return bundle
          .getEntry().stream()
              .allMatch(entry -> implConfig.expect().contains(entry.getResource().getId()));
    }
  }
}
