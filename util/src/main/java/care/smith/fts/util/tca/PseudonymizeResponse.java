package care.smith.fts.util.tca;

import java.time.Duration;
import java.util.Map;

public record PseudonymizeResponse(
    String mapName, Map<String, String> idMap, Duration dateShiftValue) {}
