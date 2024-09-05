package care.smith.fts.rda.impl;

import care.smith.fts.util.HttpClientConfig;
import java.io.File;
import java.time.Duration;

public record DeidentifhirStepConfig(TCAConfig tca, Duration dateShift, File deidentifhirConfig) {

  record TCAConfig(HttpClientConfig server, String domain) {}
}
