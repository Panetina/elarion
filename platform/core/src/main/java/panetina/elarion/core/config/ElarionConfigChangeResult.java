package panetina.elarion.core.config;

import java.util.List;
import java.util.Objects;

public record ElarionConfigChangeResult(
        Status status,
        ElarionConfigChangeRequest request,
        String oldDisplayValue,
        String newDisplayValue,
        boolean reloadRequired,
        boolean restartRequired,
        String auditEventType,
        List<ElarionConfigChangeError> errors
) {
    public ElarionConfigChangeResult {
        status = status == null ? Status.REJECTED : status;
        request = Objects.requireNonNull(request, "Config change request is required");
        oldDisplayValue = oldDisplayValue == null ? "" : oldDisplayValue;
        newDisplayValue = newDisplayValue == null ? "" : newDisplayValue;
        auditEventType = auditEventType == null ? "" : auditEventType.trim();
        if (errors != null && errors.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Config change errors must not contain null");
        }
        errors = errors == null ? List.of() : List.copyOf(errors);
        if (status != Status.REJECTED && !errors.isEmpty()) {
            throw new IllegalArgumentException("Successful config change results must not contain errors");
        }
        if (status == Status.REJECTED && errors.isEmpty()) {
            throw new IllegalArgumentException("Rejected config change results must contain at least one error");
        }
        if (status == Status.APPLIED && auditEventType.isBlank()) {
            throw new IllegalArgumentException("Applied config change results require an audit event type");
        }
    }

    public static ElarionConfigChangeResult validated(
            ElarionConfigChangeRequest request,
            String oldDisplayValue,
            String newDisplayValue,
            boolean reloadRequired,
            boolean restartRequired
    ) {
        return new ElarionConfigChangeResult(Status.VALIDATED, request, oldDisplayValue, newDisplayValue,
                reloadRequired, restartRequired, "", List.of());
    }

    public static ElarionConfigChangeResult applied(
            ElarionConfigChangeRequest request,
            String oldDisplayValue,
            String newDisplayValue,
            String auditEventType
    ) {
        return applied(request, oldDisplayValue, newDisplayValue, false, false, auditEventType);
    }

    public static ElarionConfigChangeResult applied(
            ElarionConfigChangeRequest request,
            String oldDisplayValue,
            String newDisplayValue,
            boolean reloadRequired,
            boolean restartRequired,
            String auditEventType
    ) {
        return new ElarionConfigChangeResult(Status.APPLIED, request, oldDisplayValue, newDisplayValue,
                reloadRequired, restartRequired, auditEventType, List.of());
    }

    public static ElarionConfigChangeResult rejected(
            ElarionConfigChangeRequest request,
            List<ElarionConfigChangeError> errors
    ) {
        return new ElarionConfigChangeResult(Status.REJECTED, request, "", "", false, false, "", errors);
    }

    public boolean success() {
        return status != Status.REJECTED;
    }

    public enum Status {
        VALIDATED,
        APPLIED,
        REJECTED
    }
}
