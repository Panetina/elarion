package panetina.elarion.core.config;

import java.util.List;
import java.util.Objects;

public final class ElarionConfigChangeValidator {
    private ElarionConfigChangeValidator() {
    }

    public static ElarionConfigChangeResult validate(
            ElarionConfigRegistry registry,
            ElarionConfigChangeRequest request,
            ElarionConfigPermission actorPermission
    ) {
        Objects.requireNonNull(registry, "Config registry is required");
        Objects.requireNonNull(request, "Config change request is required");
        ElarionConfigPermission permission = actorPermission == null
                ? ElarionConfigPermission.PUBLIC
                : actorPermission;

        ElarionConfigDomain domain = registry.domain(request.domainId()).orElse(null);
        if (domain == null) {
            return rejected(request, ElarionConfigChangeError.Code.UNKNOWN_DOMAIN,
                    request.domainId(), "Unknown config domain: " + request.domainId());
        }

        ElarionConfigCategory category = domain.category(request.categoryId()).orElse(null);
        if (category == null) {
            return rejected(request, ElarionConfigChangeError.Code.UNKNOWN_CATEGORY,
                    request.domainId() + "." + request.categoryId(),
                    "Unknown config category: " + request.categoryId());
        }

        ElarionConfigEntry<?> entry = category.entry(request.entryId()).orElse(null);
        if (entry == null) {
            return rejected(request, ElarionConfigChangeError.Code.UNKNOWN_ENTRY,
                    request.domainId() + "." + request.categoryId() + "." + request.entryId(),
                    "Unknown config entry: " + request.entryId());
        }

        if (!allows(permission, entry.writePermission())) {
            return rejected(request, ElarionConfigChangeError.Code.PERMISSION_DENIED,
                    entry.path(), "Requires " + entry.writePermission().label() + ".");
        }

        return validateEntry(request, entry);
    }

    private static <T> ElarionConfigChangeResult validateEntry(
            ElarionConfigChangeRequest request,
            ElarionConfigEntry<T> entry
    ) {
        String currentDisplay;
        try {
            currentDisplay = entry.currentDisplayValue();
        } catch (RuntimeException exception) {
            return rejected(request, ElarionConfigChangeError.Code.INTERNAL_ERROR,
                    entry.path(), "Could not read current config value: " + exception.getMessage());
        }

        if (!request.expectedCurrentValue().isBlank()
                && !request.expectedCurrentValue().equals(currentDisplay)) {
            return rejected(request, ElarionConfigChangeError.Code.STALE_VALUE,
                    entry.path(), "Expected current value '" + request.expectedCurrentValue()
                            + "' but found '" + currentDisplay + "'.");
        }

        T parsed;
        try {
            parsed = entry.codec().parse(request.proposedValue());
        } catch (RuntimeException exception) {
            return rejected(request, ElarionConfigChangeError.Code.PARSE_FAILED,
                    entry.path(), exception.getMessage());
        }

        List<String> validationErrors;
        try {
            validationErrors = entry.validator().validate(parsed);
        } catch (RuntimeException exception) {
            return rejected(request, ElarionConfigChangeError.Code.INTERNAL_ERROR,
                    entry.path(), "Config validator failed: " + exception.getMessage());
        }
        if (validationErrors != null && !validationErrors.isEmpty()) {
            return ElarionConfigChangeResult.rejected(request, validationErrors.stream()
                    .map(error -> ElarionConfigChangeError.of(
                            ElarionConfigChangeError.Code.VALIDATION_FAILED, entry.path(), error))
                    .toList());
        }

        return ElarionConfigChangeResult.validated(request, currentDisplay, entry.codec().format(parsed),
                entry.runtimeReloadable(), entry.restartRequired());
    }

    private static boolean allows(ElarionConfigPermission actor, ElarionConfigPermission required) {
        return rank(actor) >= rank(required);
    }

    private static int rank(ElarionConfigPermission permission) {
        if (permission == ElarionConfigPermission.OPERATOR) return 1;
        return 0;
    }

    private static ElarionConfigChangeResult rejected(
            ElarionConfigChangeRequest request,
            ElarionConfigChangeError.Code code,
            String path,
            String message
    ) {
        return ElarionConfigChangeResult.rejected(request, List.of(
                ElarionConfigChangeError.of(code, path, message)));
    }
}
