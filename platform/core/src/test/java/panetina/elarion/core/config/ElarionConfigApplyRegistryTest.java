package panetina.elarion.core.config;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.api.ElarionSystemApi;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionConfigApplyRegistryTest {
    @Test
    void registrationFacadeDelegatesWithoutExposingExecutableLookup() throws Exception {
        ElarionConfigApplyRegistry registry = new ElarionConfigApplyRegistry();
        ElarionConfigApplyRegistrar registrar = registry::register;
        ElarionConfigEditTarget target = target("enabled");
        ElarionConfigApplyCapability capability = ElarionConfigApplyCapability.runtimeReload(
                "admin-config-applied",
                List.of("config/elarion/core/test.yml"));
        ElarionConfigApplier applier = context -> prepared(ElarionConfigChangeResult.applied(
                context.request(), "true", "false", capability.auditEventType()));

        registrar.register(target, capability, applier);

        assertTrue(registry.registration(target).isPresent());
        assertThrows(IllegalArgumentException.class, () -> registrar.register(target, capability, applier));
        assertEquals(List.of("register"), Arrays.stream(ElarionConfigApplyRegistrar.class.getDeclaredMethods())
                .map(java.lang.reflect.Method::getName)
                .sorted()
                .toList());
        assertEquals(ElarionConfigApplyRegistrar.class,
                ElarionSystemApi.class.getMethod("configAppliers").getReturnType());
    }

    @Test
    void registersAppliersAndReportsReadyTargets() {
        ElarionConfigRegistry descriptors = descriptors(entry(
                "font-scale",
                "ui_theme.yml.defaults.font-scale-percent",
                true,
                false));
        ElarionConfigEditTarget target = target("font-scale");
        ElarionConfigApplyRegistry registry = new ElarionConfigApplyRegistry();
        ElarionConfigApplyCapability capability = ElarionConfigApplyCapability.runtimeReload(
                "admin-config-applied",
                List.of("config/elarion/core/ui_theme.yml"));

        registry.register(target, capability, context -> prepared(ElarionConfigChangeResult.applied(
                context.request(), "100", "125", capability.auditEventType())));

        ElarionConfigApplyReadiness readiness = registry.readiness(descriptors, target);
        assertTrue(readiness.ready());
        assertEquals("admin-config-applied", readiness.auditEventType());
        assertEquals(List.of("config/elarion/core/ui_theme.yml"), readiness.affectedFiles());
        assertTrue(readiness.errors().isEmpty());
        assertTrue(registry.registration(target).isPresent());
    }

    @Test
    void rejectsDuplicateAndNullRegistrations() {
        ElarionConfigApplyRegistry registry = new ElarionConfigApplyRegistry();
        ElarionConfigEditTarget target = target("enabled");
        ElarionConfigApplyCapability capability = ElarionConfigApplyCapability.runtimeReload(
                "admin-config-applied",
                List.of("config/elarion/core/test.yml"));
        ElarionConfigApplier applier = context -> prepared(ElarionConfigChangeResult.applied(
                context.request(), "true", "false", capability.auditEventType()));

        registry.register(target, capability, applier);

        assertThrows(IllegalArgumentException.class, () -> registry.register(target, capability, applier));
        assertThrows(NullPointerException.class, () -> registry.register(null, capability, applier));
        assertThrows(NullPointerException.class, () -> registry.register(target, null, applier));
        assertThrows(NullPointerException.class, () -> registry.register(target, capability, null));
    }

    @Test
    void reportsMissingDescriptorAndMissingApplierAsBlocked() {
        ElarionConfigRegistry descriptors = descriptors(entry(
                "enabled",
                "test.yml.enabled",
                true,
                false));
        ElarionConfigApplyRegistry registry = new ElarionConfigApplyRegistry();

        assertBlocked(registry.readiness(descriptors, new ElarionConfigEditTarget(
                        "missing", "general", "enabled")),
                ElarionConfigChangeError.Code.UNKNOWN_DOMAIN);
        assertBlocked(registry.readiness(descriptors, new ElarionConfigEditTarget(
                        "core", "missing", "enabled")),
                ElarionConfigChangeError.Code.UNKNOWN_CATEGORY);
        assertBlocked(registry.readiness(descriptors, target("missing")),
                ElarionConfigChangeError.Code.UNKNOWN_ENTRY);
        assertBlocked(registry.readiness(descriptors, target("enabled")),
                ElarionConfigChangeError.Code.UNSUPPORTED);
    }

    @Test
    void reportsDisabledReloadAndRestartUnsafeAppliersAsBlocked() {
        ElarionConfigApplyRegistry registry = new ElarionConfigApplyRegistry();
        ElarionConfigApplier noOp = context -> prepared(ElarionConfigChangeResult.applied(
                context.request(), "old", "new", "admin-config-applied"));

        registry.register(target("disabled"), ElarionConfigApplyCapability.disabled("Read-only for now."), noOp);
        registry.register(target("reloadable"), new ElarionConfigApplyCapability(
                "admin-config-applied",
                List.of("config/elarion/core/reload.yml"),
                false,
                false,
                ""), noOp);
        registry.register(target("restart"), ElarionConfigApplyCapability.runtimeReload(
                "admin-config-applied",
                List.of("config/elarion/core/restart.yml")), noOp);

        ElarionConfigRegistry descriptors = descriptors(
                entry("disabled", "test.yml.disabled", false, false),
                entry("reloadable", "test.yml.reloadable", true, false),
                entry("restart", "test.yml.restart", false, true));

        assertBlocked(registry.readiness(descriptors, target("disabled")),
                ElarionConfigChangeError.Code.UNSUPPORTED);
        assertBlocked(registry.readiness(descriptors, target("reloadable")),
                ElarionConfigChangeError.Code.RELOAD_REQUIRED);
        assertBlocked(registry.readiness(descriptors, target("restart")),
                ElarionConfigChangeError.Code.RESTART_REQUIRED);
    }

    @Test
    void capabilityAndReadinessContractsAreImmutableAndValidated() {
        ElarionConfigEditTarget target = target("enabled");
        ElarionConfigApplyCapability capability = ElarionConfigApplyCapability.runtimeReload(
                " admin-config-applied ",
                List.of(" config/elarion/core/test.yml "));

        assertEquals("admin-config-applied", capability.auditEventType());
        assertEquals(List.of("config/elarion/core/test.yml"), capability.affectedFiles());
        assertThrows(UnsupportedOperationException.class, () -> capability.affectedFiles().add("other.yml"));
        assertThrows(IllegalArgumentException.class, () -> new ElarionConfigApplyCapability(
                "", List.of("config/elarion/core/test.yml"), true, false, ""));
        assertThrows(IllegalArgumentException.class, () -> new ElarionConfigApplyCapability(
                "admin-config-applied", List.of(), true, false, ""));

        ElarionConfigApplyReadiness ready = ElarionConfigApplyReadiness.ready(
                target, capability.auditEventType(), capability.affectedFiles());
        assertThrows(UnsupportedOperationException.class, () -> ready.affectedFiles().add("other.yml"));
        assertThrows(UnsupportedOperationException.class, () -> ready.errors().add(
                ElarionConfigChangeError.of(ElarionConfigChangeError.Code.UNSUPPORTED, "x", "x")));
        ElarionConfigChangeError error = ElarionConfigChangeError.of(
                ElarionConfigChangeError.Code.UNSUPPORTED, "x", "x");
        assertThrows(IllegalArgumentException.class, () -> new ElarionConfigApplyReadiness(
                target, true, "audit", List.of("test.yml"), List.of(error)));
        assertThrows(IllegalArgumentException.class, () -> ElarionConfigApplyReadiness.ready(
                target, "", List.of("test.yml")));
        assertThrows(IllegalArgumentException.class, () -> ElarionConfigApplyReadiness.ready(
                target, "audit", List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ElarionConfigApplyReadiness(
                target, false, "", List.of(), List.of()));
    }

    @Test
    void applyContextRequiresMatchingResolvedDescriptor() {
        ElarionConfigRegistry descriptors = descriptors(entry(
                "enabled",
                "test.yml.enabled",
                true,
                false));
        ElarionConfigDomain domain = descriptors.domain("core").orElseThrow();
        ElarionConfigCategory category = domain.category("general").orElseThrow();
        ElarionConfigEntry<?> entry = category.entry("enabled").orElseThrow();
        ElarionConfigChangeRequest request = new ElarionConfigChangeRequest(
                "core", "general", "enabled", "false", "true", UUID.randomUUID(), "test");

        ElarionConfigApplyContext context = new ElarionConfigApplyContext(
                descriptors, domain, category, entry, request);
        assertEquals(entry, context.entry());

        assertThrows(IllegalArgumentException.class, () -> new ElarionConfigApplyContext(
                descriptors, domain, category, entry, new ElarionConfigChangeRequest(
                "core", "general", "other", "false", "true", UUID.randomUUID(), "test")));
        assertThrows(NullPointerException.class, () -> new ElarionConfigApplyContext(
                null, domain, category, entry, request));
    }

    private static ElarionConfigRegistry descriptors(ElarionConfigEntry<?>... entries) {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(new ElarionConfigDomain(
                "core",
                "platform:core",
                "Core",
                "Core config",
                List.of("config/elarion/core/test.yml"),
                "/e reload",
                List.of(new ElarionConfigCategory(
                        "general",
                        "General",
                        "General settings",
                        List.of(entries)))));
        return registry;
    }

    private static ElarionConfigEntry<Boolean> entry(
            String id,
            String path,
            boolean runtimeReloadable,
            boolean restartRequired
    ) {
        return new ElarionConfigEntry<>(
                id,
                id,
                "Test entry",
                path,
                ElarionConfigCodec.BOOLEAN,
                true,
                () -> true,
                ElarionConfigValidator.pass(),
                List.of("true", "false"),
                "",
                "",
                runtimeReloadable,
                restartRequired,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEditTarget target(String entryId) {
        return new ElarionConfigEditTarget("core", "general", entryId);
    }

    private static ElarionConfigPreparedChange prepared(ElarionConfigChangeResult result) {
        return ElarionConfigPreparedChange.of(() -> result, () -> { });
    }

    private static void assertBlocked(
            ElarionConfigApplyReadiness readiness,
            ElarionConfigChangeError.Code expectedCode
    ) {
        assertFalse(readiness.ready());
        assertEquals(expectedCode, readiness.errors().getFirst().code());
        assertFalse(readiness.firstErrorMessage().isBlank());
    }
}
