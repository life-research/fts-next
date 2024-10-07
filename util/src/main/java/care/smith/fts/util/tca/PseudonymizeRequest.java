package care.smith.fts.util.tca;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Set;

public record PseudonymizeRequest(
    @NotNull(groups = PseudonymizeRequest.class) String patientId,
    @NotNull(groups = PseudonymizeRequest.class) Set<String> ids,
    @NotBlank(groups = PseudonymizeRequest.class) String pseudonymDomain,
    @NotBlank(groups = PseudonymizeRequest.class) String saltDomain,
    @NotBlank(groups = PseudonymizeRequest.class) String dateShiftDomain,
    @NotNull(groups = PseudonymizeRequest.class) Duration maxDateShift) {}
