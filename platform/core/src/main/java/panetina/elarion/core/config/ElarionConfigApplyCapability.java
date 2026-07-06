package panetina.elarion.core.config;

import java.util.List;

public record ElarionConfigApplyCapability(
        String auditEventType,
        List<String> affectedFiles,
        boolean runtimeReloadSupported,
        boolean restartRequiredSupported,
        String disabledReason
) {
    public ElarionConfigApplyCapability {
        auditEventType = clean(auditEventType);
        affectedFiles = affectedFiles == null ? List.of() : affectedFiles.stream()
                .map(ElarionConfigApplyCapability::clean)
                .filter(value -> !value.isBlank())
                .toList();
        disabledReason = clean(disabledReason);
        if (disabledReason.isBlank()) {
            if (auditEventType.isBlank()) {
                throw new IllegalArgumentException("Config apply capability audit event type is required");
            }
            if (affectedFiles.isEmpty()) {
                throw new IllegalArgumentException("Config apply capability affected files are required");
            }
        }
    }

    public static ElarionConfigApplyCapability runtimeReload(
            String auditEventType,
            List<String> affectedFiles
    ) {
        return new ElarionConfigApplyCapability(auditEventType, affectedFiles, true, false, "");
    }

    public static ElarionConfigApplyCapability disabled(String reason) {
        return new ElarionConfigApplyCapability("", List.of(), false, false,
                clean(reason).isBlank() ? "Config apply is disabled." : reason);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
