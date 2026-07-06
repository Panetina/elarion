package panetina.elarion.core.config;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ElarionConfigApplyRegistry {
    private final Map<String, Registration> registrations = new ConcurrentHashMap<>();

    public void register(
            ElarionConfigEditTarget target,
            ElarionConfigApplyCapability capability,
            ElarionConfigApplier applier
    ) {
        Objects.requireNonNull(target, "Config apply target is required");
        Objects.requireNonNull(capability, "Config apply capability is required");
        Objects.requireNonNull(applier, "Config applier is required");
        Registration registration = new Registration(target, capability, applier);
        Registration previous = registrations.putIfAbsent(target.targetKey(), registration);
        if (previous != null) {
            throw new IllegalArgumentException("Config applier already registered: " + target.targetKey());
        }
    }

    public Optional<Registration> registration(ElarionConfigEditTarget target) {
        Objects.requireNonNull(target, "Config apply target is required");
        return Optional.ofNullable(registrations.get(target.targetKey()));
    }

    public ElarionConfigApplyReadiness readiness(
            ElarionConfigRegistry registry,
            ElarionConfigEditTarget target
    ) {
        Objects.requireNonNull(registry, "Config registry is required");
        Objects.requireNonNull(target, "Config apply target is required");

        ElarionConfigDomain domain = registry.domain(target.domainId()).orElse(null);
        if (domain == null) {
            return blocked(target, ElarionConfigChangeError.Code.UNKNOWN_DOMAIN,
                    target.domainId(), "Unknown config domain: " + target.domainId());
        }
        ElarionConfigCategory category = domain.category(target.categoryId()).orElse(null);
        if (category == null) {
            return blocked(target, ElarionConfigChangeError.Code.UNKNOWN_CATEGORY,
                    target.domainId() + "." + target.categoryId(),
                    "Unknown config category: " + target.categoryId());
        }
        ElarionConfigEntry<?> entry = category.entry(target.entryId()).orElse(null);
        if (entry == null) {
            return blocked(target, ElarionConfigChangeError.Code.UNKNOWN_ENTRY,
                    target.domainId() + "." + target.categoryId() + "." + target.entryId(),
                    "Unknown config entry: " + target.entryId());
        }

        Registration registration = registration(target).orElse(null);
        if (registration == null) {
            return blocked(target, ElarionConfigChangeError.Code.UNSUPPORTED,
                    entry.path(), "No config applier is registered for " + target.targetKey() + ".");
        }

        ElarionConfigApplyCapability capability = registration.capability();
        if (!capability.disabledReason().isBlank()) {
            return blocked(target, ElarionConfigChangeError.Code.UNSUPPORTED,
                    entry.path(), capability.disabledReason());
        }
        if (entry.restartRequired() && !capability.restartRequiredSupported()) {
            return blocked(target, ElarionConfigChangeError.Code.RESTART_REQUIRED,
                    entry.path(), "This config entry requires restart-safe apply support.");
        }
        if (entry.runtimeReloadable() && !capability.runtimeReloadSupported()) {
            return blocked(target, ElarionConfigChangeError.Code.RELOAD_REQUIRED,
                    entry.path(), "This config entry requires reload-safe apply support.");
        }

        return ElarionConfigApplyReadiness.ready(target, capability.auditEventType(),
                capability.affectedFiles());
    }

    private static ElarionConfigApplyReadiness blocked(
            ElarionConfigEditTarget target,
            ElarionConfigChangeError.Code code,
            String path,
            String message
    ) {
        return ElarionConfigApplyReadiness.blocked(target,
                ElarionConfigChangeError.of(code, path, message));
    }

    public record Registration(
            ElarionConfigEditTarget target,
            ElarionConfigApplyCapability capability,
            ElarionConfigApplier applier
    ) {
        public Registration {
            target = Objects.requireNonNull(target, "Config apply target is required");
            capability = Objects.requireNonNull(capability, "Config apply capability is required");
            applier = Objects.requireNonNull(applier, "Config applier is required");
        }
    }
}
