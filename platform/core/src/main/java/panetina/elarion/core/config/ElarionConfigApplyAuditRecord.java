package panetina.elarion.core.config;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ElarionConfigApplyAuditRecord(
        ElarionConfigEditTarget target,
        UUID actorId,
        String reason,
        String oldDisplayValue,
        String newDisplayValue,
        boolean reloadRequired,
        boolean restartRequired,
        String auditEventType,
        List<String> affectedFiles
) {
    public ElarionConfigApplyAuditRecord {
        target = Objects.requireNonNull(target, "Config apply audit target is required");
        reason = clean(reason);
        oldDisplayValue = clean(oldDisplayValue);
        newDisplayValue = clean(newDisplayValue);
        auditEventType = clean(auditEventType);
        affectedFiles = affectedFiles == null ? List.of() : List.copyOf(affectedFiles);
        if (auditEventType.isBlank()) {
            throw new IllegalArgumentException("Config apply audit event type is required");
        }
        if (affectedFiles.isEmpty()) {
            throw new IllegalArgumentException("Config apply audit affected files are required");
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
