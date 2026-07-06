package panetina.elarion.core.config;

import java.util.List;
import java.util.Objects;

public record ElarionConfigApplyReadiness(
        ElarionConfigEditTarget target,
        boolean ready,
        String auditEventType,
        List<String> affectedFiles,
        List<ElarionConfigChangeError> errors
) {
    public ElarionConfigApplyReadiness {
        target = Objects.requireNonNull(target, "Config apply target is required");
        auditEventType = auditEventType == null ? "" : auditEventType.trim();
        affectedFiles = affectedFiles == null ? List.of() : List.copyOf(affectedFiles);
        if (errors != null && errors.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Config apply readiness errors must not contain null");
        }
        errors = errors == null ? List.of() : List.copyOf(errors);
        if (ready && auditEventType.isBlank()) {
            throw new IllegalArgumentException("Ready config apply state requires an audit event type");
        }
        if (ready && affectedFiles.isEmpty()) {
            throw new IllegalArgumentException("Ready config apply state requires affected files");
        }
        if (ready && !errors.isEmpty()) {
            throw new IllegalArgumentException("Ready config apply state must not contain errors");
        }
        if (!ready && errors.isEmpty()) {
            throw new IllegalArgumentException("Blocked config apply state must contain at least one error");
        }
    }

    public static ElarionConfigApplyReadiness ready(
            ElarionConfigEditTarget target,
            String auditEventType,
            List<String> affectedFiles
    ) {
        return new ElarionConfigApplyReadiness(target, true, auditEventType, affectedFiles, List.of());
    }

    public static ElarionConfigApplyReadiness blocked(
            ElarionConfigEditTarget target,
            ElarionConfigChangeError error
    ) {
        return new ElarionConfigApplyReadiness(target, false, "", List.of(), List.of(error));
    }

    public String firstErrorMessage() {
        return errors.isEmpty() ? "" : errors.getFirst().message();
    }
}
