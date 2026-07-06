package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigApplyCapability;
import panetina.elarion.core.config.ElarionConfigApplyExecutor;
import panetina.elarion.core.config.ElarionConfigApplyReadiness;
import panetina.elarion.core.config.ElarionConfigApplyRegistry;
import panetina.elarion.core.config.ElarionConfigChangeError;
import panetina.elarion.core.config.ElarionConfigChangeRequest;
import panetina.elarion.core.config.ElarionConfigChangeResult;
import panetina.elarion.core.config.ElarionConfigChangeValidator;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigEditControl;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEditTarget;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigPreparedChange;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.config.ElarionConfigValidator;
import panetina.elarion.core.model.ElarionAdminPanelRow;
import panetina.elarion.core.network.ElarionConfigEditRequestPayload;
import panetina.elarion.core.network.ElarionConfigEditResultPayload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

final class ElarionAdminPanelServiceTest {
    @Test
    void adminSnapshotBuildsRowsOnlyForSelectedTab() {
        AtomicInteger selectedBuilds = new AtomicInteger();
        AtomicInteger inactiveBuilds = new AtomicInteger();

        var selected = ElarionAdminPanelService.selectedTab(
                "overview", "overview", "Overview", "Status",
                () -> {
                    selectedBuilds.incrementAndGet();
                    return List.of(ElarionAdminPanelRow.card(
                            "status", "Status", "Ready", "Healthy", "Active",
                            "item:minecraft:compass", List.of()));
                });
        var inactive = ElarionAdminPanelService.selectedTab(
                "overview", "configs", "Config", "Descriptors",
                () -> {
                    inactiveBuilds.incrementAndGet();
                    return List.of(ElarionAdminPanelRow.card(
                            "config", "Config", "Large", "Should not be serialized", "Active",
                            "item:minecraft:writable_book", List.of()));
                });

        assertEquals(1, selectedBuilds.get());
        assertEquals(0, inactiveBuilds.get());
        assertEquals(1, selected.rows().size());
        assertTrue(inactive.rows().isEmpty());
        assertEquals("configs", inactive.id());
    }

    @Test
    void providersAreDeterministicallySortedAndReplaceById() {
        ElarionAdminPanelService service = new ElarionAdminPanelService();

        service.registerProvider(provider("underworld", "Underworld"));
        service.registerProvider(provider("government", "Government"));
        service.registerProvider(provider("government", "Government Replacement"));

        assertEquals(2, service.providers().size());
        assertEquals("government", service.providers().get(0).id());
        assertEquals("Government Replacement", service.providers().get(0).title());
        assertEquals("underworld", service.providers().get(1).id());
    }

    @Test
    void configRowsExposeReadOnlyDescriptorSummaries() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("groups", "Groups", 80L));
        registry.registerDomain(domain("core", "Core", 125L));

        var rows = ElarionAdminPanelService.configRows(registry);

        assertEquals(4, rows.size());
        assertEquals("config:core", rows.get(0).id());
        assertEquals("Config: Core", rows.get(0).title());
        assertEquals("1 categories", rows.get(0).state());
        assertTrue(rows.get(0).actions().isEmpty());
        assertTrue(rows.get(0).body().contains("Owner: test:core"));
        assertTrue(rows.get(0).body().contains("Files: config/elarion/addons/core/test.yml"));
        assertTrue(rows.get(0).body().contains("Reload: /e core reload"));
        assertTrue(rows.get(0).body().contains("Mode: Read-only discovery"));
        assertTrue(rows.get(0).body().contains("Categories: 1"));
        assertTrue(rows.get(0).body().contains("Entries: 1"));
        assertTrue(rows.get(0).body().contains("Reloadable entries: 1"));
        assertTrue(rows.get(0).body().contains("Restart-required entries: 0"));
        assertTrue(rows.get(0).body().contains("Invalid entries: 0"));
        assertTrue(rows.get(0).body().contains("- General: 1 entries"));

        assertEquals("config:core:category:general", rows.get(1).id());
        assertEquals("Core: General", rows.get(1).title());
        assertEquals("1 entries", rows.get(1).state());
        assertTrue(rows.get(1).actions().isEmpty());
        assertTrue(rows.get(1).body().contains("Domain: Core (core)"));
        assertTrue(rows.get(1).body().contains("Category: General (general)"));
        assertTrue(rows.get(1).body().contains("Select this category to load entry rows."));

        assertEquals("config:groups", rows.get(2).id());
        assertEquals("config:groups:category:general", rows.get(3).id());
    }

    @Test
    void configRowsIncludeEntriesOnlyForSelectedCategory() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("groups", "Groups", 80L));
        registry.registerDomain(domain("core", "Core", 125L));

        var rows = ElarionAdminPanelService.configRows(registry, "config:core:category:general");

        assertEquals(5, rows.size());
        assertEquals("config:core", rows.get(0).id());
        assertEquals("config:core:category:general", rows.get(1).id());
        assertTrue(rows.get(1).body().contains("Showing entries below."));

        assertEquals("config-entry|core|general|creation.fee", rows.get(2).id());
        assertEquals("Creation Fee", rows.get(2).title());
        assertEquals("Reloadable", rows.get(2).state());
        assertTrue(rows.get(2).body().contains("Current: 125"));
        assertTrue(rows.get(2).body().contains("Validate Value previews parsing and validation only"));
        assertEquals(2, rows.get(2).actions().size());
        assertEquals(ElarionAdminPanelService.CORE_PROVIDER, rows.get(2).actions().getFirst().providerId());
        assertEquals(ElarionAdminPanelService.OPEN_CONFIG_EDITOR_ACTION, rows.get(2).actions().getFirst().id());
        assertEquals("", rows.get(2).actions().getFirst().parameterKey());
        assertEquals(ElarionAdminPanelService.CORE_PROVIDER, rows.get(2).actions().get(1).providerId());
        assertEquals(ElarionAdminPanelService.VALIDATE_CONFIG_VALUE_ACTION, rows.get(2).actions().get(1).id());
        assertEquals("value", rows.get(2).actions().get(1).parameterKey());

        assertEquals("config:groups", rows.get(3).id());
        assertEquals("config:groups:category:general", rows.get(4).id());
    }

    @Test
    void configRowsPreserveEntryFocusAfterSelectingEntry() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));

        var rows = ElarionAdminPanelService.configRows(registry, "config-entry|core|general|creation.fee");

        assertEquals(3, rows.size());
        assertEquals("config-entry|core|general|creation.fee", rows.get(2).id());
    }

    @Test
    void configValidationPreviewDoesNotRequireMutatingConfig() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));

        ElarionAdminPanelService.ActionResult result = ElarionAdminPanelService.configValidationPreview(
                registry,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "config-entry|core|general|creation.fee",
                "90");

        assertTrue(result.success());
        assertTrue(result.message().contains("Valid: 125 -> 90"));
        assertTrue(result.message().contains("Reload required"));
    }

    @Test
    void configValidationPreviewRejectsInvalidValuesAndTargets() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));

        ElarionAdminPanelService.ActionResult invalidValue = ElarionAdminPanelService.configValidationPreview(
                registry,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "config-entry|core|general|creation.fee",
                "-1");
        assertFalse(invalidValue.success());
        assertTrue(invalidValue.message().startsWith("Invalid:"));

        ElarionAdminPanelService.ActionResult invalidTarget = ElarionAdminPanelService.configValidationPreview(
                registry,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "config:core:category:general",
                "90");
        assertFalse(invalidTarget.success());
        assertEquals("Invalid config entry target.", invalidTarget.message());
    }

    @Test
    void configEditOpenControlBuildsDisabledServerAuthoredControl() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));

        ElarionAdminPanelService.ConfigEditOpenResult result = ElarionAdminPanelService.configEditOpenControl(
                registry,
                "config-entry|core|general|creation.fee");

        assertTrue(result.success());
        assertTrue(result.message().contains("Opened config editor for Creation Fee"));
        ElarionConfigEditControl control = result.control();
        assertEquals("core:general:creation.fee", control.target().targetKey());
        assertEquals("Creation Fee", control.label());
        assertEquals("test.yml.creation.fee", control.path());
        assertEquals("125", control.currentDisplayValue());
        assertEquals("25", control.defaultDisplayValue());
        assertEquals(ElarionConfigCodec.ValueType.LONG, control.valueType());
        assertEquals("0", control.minimum());
        assertTrue(control.runtimeReloadable());
        assertFalse(control.restartRequired());
        assertFalse(control.editable());
        assertFalse(control.inputEditable());
        assertFalse(control.applyAvailable());
        assertTrue(control.applyDisabledReason().contains("not enabled"));
        assertTrue(control.disabledReason().contains("not enabled"));
    }

    @Test
    void configEditOpenControlUsesTargetReadinessForEditingState() {
        ElarionConfigRegistry descriptors = new ElarionConfigRegistry();
        descriptors.registerDomain(domain("core", "Core", 125L));
        ElarionConfigApplyRegistry applyRegistry = new ElarionConfigApplyRegistry();
        String targetId = "config-entry|core|general|creation.fee";

        ElarionAdminPanelService.ConfigEditOpenResult missing =
                ElarionAdminPanelService.configEditOpenControl(
                        descriptors, target -> applyRegistry.readiness(descriptors, target), targetId);

        assertTrue(missing.success());
        assertFalse(missing.control().editable());
        assertFalse(missing.control().applyAvailable());
        assertTrue(missing.control().applyDisabledReason().contains("No config applier"));
        assertTrue(missing.control().disabledReason().contains("No config applier"));

        ElarionConfigEditTarget target = missing.control().target();
        ElarionConfigApplyCapability capability = ElarionConfigApplyCapability.runtimeReload(
                "admin-config-applied", List.of("config/elarion/core/test.yml"));
        applyRegistry.register(target, capability, context -> ElarionConfigPreparedChange.of(
                () -> ElarionConfigChangeResult.applied(
                        context.request(), "125", "90", capability.auditEventType()),
                () -> { }));

        ElarionAdminPanelService.ConfigEditOpenResult ready =
                ElarionAdminPanelService.configEditOpenControl(
                        descriptors, candidate -> applyRegistry.readiness(descriptors, candidate), targetId);

        assertTrue(ready.success());
        assertTrue(ready.control().editable());
        assertTrue(ready.control().inputEditable());
        assertTrue(ready.control().applyAvailable());
        assertEquals("", ready.control().applyDisabledReason());
        assertEquals("", ready.control().disabledReason());
    }

    @Test
    void configEditOpenControlRejectsInvalidAndStaleTargets() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));

        ElarionAdminPanelService.ConfigEditOpenResult invalid = ElarionAdminPanelService.configEditOpenControl(
                registry,
                "config:core:category:general");
        assertFalse(invalid.success());
        assertEquals("Invalid config entry target.", invalid.message());

        ElarionAdminPanelService.ConfigEditOpenResult unknownDomain = ElarionAdminPanelService.configEditOpenControl(
                registry,
                "config-entry|missing|general|creation.fee");
        assertFalse(unknownDomain.success());
        assertEquals("Unknown config domain.", unknownDomain.message());

        ElarionAdminPanelService.ConfigEditOpenResult unknownCategory = ElarionAdminPanelService.configEditOpenControl(
                registry,
                "config-entry|core|missing|creation.fee");
        assertFalse(unknownCategory.success());
        assertEquals("Unknown config category.", unknownCategory.message());

        ElarionAdminPanelService.ConfigEditOpenResult unknownEntry = ElarionAdminPanelService.configEditOpenControl(
                registry,
                "config-entry|core|general|missing");
        assertFalse(unknownEntry.success());
        assertEquals("Unknown config entry.", unknownEntry.message());
    }

    @Test
    void configEditResultValidatesWithoutEnablingApply() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));

        ElarionConfigEditResultPayload result = ElarionAdminPanelService.configEditResult(
                registry,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                true,
                request("core", "general", "creation.fee", "125", "90",
                        ElarionConfigEditRequestPayload.Intent.VALIDATE));

        assertEquals(ElarionConfigChangeResult.Status.VALIDATED, result.status());
        assertEquals("125", result.oldDisplayValue());
        assertEquals("90", result.newDisplayValue());
        assertTrue(result.reloadRequired());
        assertFalse(result.restartRequired());
        assertFalse(result.canApply());
        assertTrue(result.errors().isEmpty());
        assertTrue(result.message().contains("Valid: 125 -> 90"));
        assertTrue(result.auditPreview().contains("core:general:creation.fee"));
    }

    @Test
    void configEditResultIncludesReadinessReasonWithoutEnablingApply() {
        ElarionConfigRegistry descriptors = new ElarionConfigRegistry();
        descriptors.registerDomain(domain("core", "Core", 125L));
        ElarionConfigApplyRegistry applyRegistry = new ElarionConfigApplyRegistry();

        ElarionConfigEditResultPayload result = ElarionAdminPanelService.configEditResult(
                descriptors,
                target -> applyRegistry.readiness(descriptors, target),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                true,
                request("core", "general", "creation.fee", "125", "90",
                        ElarionConfigEditRequestPayload.Intent.VALIDATE));

        assertEquals(ElarionConfigChangeResult.Status.VALIDATED, result.status());
        assertFalse(result.canApply());
        assertTrue(result.message().contains("No config applier"));
    }

    @Test
    void configEditResultEnablesApplyForReadyExecutor() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));

        ElarionConfigEditResultPayload result = ElarionAdminPanelService.configEditResult(
                registry,
                fakeExecutor(new AtomicInteger(), null, null),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                true,
                request("core", "general", "creation.fee", "125", "90",
                        ElarionConfigEditRequestPayload.Intent.VALIDATE));

        assertEquals(ElarionConfigChangeResult.Status.VALIDATED, result.status());
        assertTrue(result.canApply());
        assertTrue(result.message().contains("Apply available"));
    }

    @Test
    void configEditResultRejectsInvalidValues() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));

        ElarionConfigEditResultPayload result = ElarionAdminPanelService.configEditResult(
                registry,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                true,
                request("core", "general", "creation.fee", "125", "-1",
                        ElarionConfigEditRequestPayload.Intent.VALIDATE));

        assertEquals(ElarionConfigChangeResult.Status.REJECTED, result.status());
        assertEquals(ElarionConfigChangeError.Code.VALIDATION_FAILED, result.errors().getFirst().code());
        assertFalse(result.canApply());
        assertTrue(result.message().startsWith("Invalid:"));
    }

    @Test
    void configEditResultRejectsStaleExpectedValues() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));

        ElarionConfigEditResultPayload result = ElarionAdminPanelService.configEditResult(
                registry,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                true,
                request("core", "general", "creation.fee", "90", "100",
                        ElarionConfigEditRequestPayload.Intent.VALIDATE));

        assertEquals(ElarionConfigChangeResult.Status.REJECTED, result.status());
        assertEquals(ElarionConfigChangeError.Code.STALE_VALUE, result.errors().getFirst().code());
        assertFalse(result.canApply());
    }

    @Test
    void configEditResultRequiresOperatorPermission() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));

        ElarionConfigEditResultPayload result = ElarionAdminPanelService.configEditResult(
                registry,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                false,
                request("core", "general", "creation.fee", "125", "90",
                        ElarionConfigEditRequestPayload.Intent.VALIDATE));

        assertEquals(ElarionConfigChangeResult.Status.REJECTED, result.status());
        assertEquals(ElarionConfigChangeError.Code.PERMISSION_DENIED, result.errors().getFirst().code());
        assertFalse(result.canApply());
        assertTrue(result.message().contains("OP level 4"));
    }

    @Test
    void configEditResultRejectsApplyUntilWriteSupportExists() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));

        ElarionConfigEditResultPayload result = ElarionAdminPanelService.configEditResult(
                registry,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                true,
                request("core", "general", "creation.fee", "125", "90",
                        ElarionConfigEditRequestPayload.Intent.APPLY));

        assertEquals(ElarionConfigChangeResult.Status.REJECTED, result.status());
        assertEquals(ElarionConfigChangeError.Code.UNSUPPORTED, result.errors().getFirst().code());
        assertFalse(result.canApply());
        assertTrue(result.message().contains("not enabled"));
    }

    @Test
    void configEditResultRejectsApplyForNonOperatorBeforeExecutorDispatch() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));
        AtomicInteger calls = new AtomicInteger();

        ElarionConfigEditResultPayload result = ElarionAdminPanelService.configEditResult(
                registry,
                fakeExecutor(calls, null, null),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                false,
                request("core", "general", "creation.fee", "125", "90",
                        ElarionConfigEditRequestPayload.Intent.APPLY));

        assertEquals(ElarionConfigChangeResult.Status.REJECTED, result.status());
        assertEquals(ElarionConfigChangeError.Code.PERMISSION_DENIED, result.errors().getFirst().code());
        assertEquals(0, calls.get());
        assertFalse(result.canApply());
        assertTrue(result.message().contains("OP level 4"));
    }

    @Test
    void configEditResultDispatchesApplyToExecutorForOperator() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<ElarionConfigChangeRequest> captured = new AtomicReference<>();

        ElarionConfigEditResultPayload result = ElarionAdminPanelService.configEditResult(
                registry,
                fakeExecutor(calls, captured, request -> ElarionConfigChangeResult.applied(
                        request, "125", "90", true, false, "admin-config-applied")),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                true,
                requestWithReason("core", "general", "creation.fee", "125", "90", "",
                        ElarionConfigEditRequestPayload.Intent.APPLY));

        assertEquals(1, calls.get());
        assertEquals("admin-panel-config-edit-apply", captured.get().reason());
        assertEquals(ElarionConfigChangeResult.Status.APPLIED, result.status());
        assertEquals("125", result.oldDisplayValue());
        assertEquals("90", result.newDisplayValue());
        assertTrue(result.reloadRequired());
        assertFalse(result.restartRequired());
        assertFalse(result.canApply());
        assertTrue(result.errors().isEmpty());
        assertTrue(result.auditPreview().contains("Changed core:general:creation.fee"));
        assertTrue(result.message().startsWith("Applied: 125 -> 90"));
    }

    @Test
    void configEditResultPreservesRejectedExecutorResult() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));

        ElarionConfigEditResultPayload result = ElarionAdminPanelService.configEditResult(
                registry,
                fakeExecutor(new AtomicInteger(), null, request -> ElarionConfigChangeResult.rejected(
                        request,
                        List.of(ElarionConfigChangeError.of(
                                ElarionConfigChangeError.Code.UNSUPPORTED,
                                "test.yml.creation.fee",
                                "Config apply is blocked.")))),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                true,
                request("core", "general", "creation.fee", "125", "90",
                        ElarionConfigEditRequestPayload.Intent.APPLY));

        assertEquals(ElarionConfigChangeResult.Status.REJECTED, result.status());
        assertEquals(ElarionConfigChangeError.Code.UNSUPPORTED, result.errors().getFirst().code());
        assertFalse(result.canApply());
        assertTrue(result.message().contains("Config apply is blocked."));
    }

    @Test
    void configEditResultApplyPreservesStaleExpectedValueFromExecutor() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();
        registry.registerDomain(domain("core", "Core", 125L));

        ElarionConfigEditResultPayload result = ElarionAdminPanelService.configEditResult(
                registry,
                fakeExecutor(new AtomicInteger(), null,
                        request -> ElarionConfigChangeValidator.validate(
                                registry, request, ElarionConfigPermission.OPERATOR)),
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                true,
                request("core", "general", "creation.fee", "90", "100",
                        ElarionConfigEditRequestPayload.Intent.APPLY));

        assertEquals(ElarionConfigChangeResult.Status.REJECTED, result.status());
        assertEquals(ElarionConfigChangeError.Code.STALE_VALUE, result.errors().getFirst().code());
        assertFalse(result.canApply());
    }

    @Test
    void configTabIsSeparateFromProviderSystemsTab() {
        assertEquals(List.of("overview", "players", "systems", "configs", "realms", "danger"),
                ElarionAdminPanelService.tabOrder());
    }

    private static ElarionAdminPanelProvider provider(String id, String title) {
        return new ElarionAdminPanelProvider() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String title() {
                return title;
            }
        };
    }

    private static ElarionConfigApplyExecutor fakeExecutor(
            AtomicInteger calls,
            AtomicReference<ElarionConfigChangeRequest> captured,
            java.util.function.Function<ElarionConfigChangeRequest, ElarionConfigChangeResult> apply
    ) {
        return new ElarionConfigApplyExecutor() {
            @Override
            public ElarionConfigApplyReadiness readiness(ElarionConfigEditTarget target) {
                return ElarionConfigApplyReadiness.ready(
                        target,
                        "admin-config-applied",
                        List.of("config/elarion/core/test.yml"));
            }

            @Override
            public ElarionConfigChangeResult apply(
                    ElarionConfigChangeRequest request,
                    ElarionConfigPermission actorPermission
            ) {
                calls.incrementAndGet();
                if (captured != null) captured.set(request);
                if (apply == null) {
                    return ElarionConfigChangeResult.rejected(request, List.of(
                            ElarionConfigChangeError.of(
                                    ElarionConfigChangeError.Code.UNSUPPORTED,
                                    request.domainId() + ":" + request.categoryId() + ":" + request.entryId(),
                                    "Unexpected apply dispatch.")));
                }
                return apply.apply(request);
            }
        };
    }

    private static ElarionConfigEditRequestPayload request(
            String domainId,
            String categoryId,
            String entryId,
            String expectedCurrentDisplayValue,
            String proposedRawValue,
            ElarionConfigEditRequestPayload.Intent intent
    ) {
        return requestWithReason(domainId, categoryId, entryId, expectedCurrentDisplayValue,
                proposedRawValue, "test", intent);
    }

    private static ElarionConfigEditRequestPayload requestWithReason(
            String domainId,
            String categoryId,
            String entryId,
            String expectedCurrentDisplayValue,
            String proposedRawValue,
            String reason,
            ElarionConfigEditRequestPayload.Intent intent
    ) {
        return new ElarionConfigEditRequestPayload(
                new ElarionConfigEditTarget(domainId, categoryId, entryId),
                expectedCurrentDisplayValue,
                proposedRawValue,
                reason,
                intent);
    }

    private static ElarionConfigDomain domain(String id, String label, long current) {
        return new ElarionConfigDomain(
                id,
                "test:" + id,
                label,
                "Test config domain",
                List.of("config/elarion/addons/" + id + "/test.yml"),
                "/e " + id + " reload",
                List.of(new ElarionConfigCategory(
                        "general",
                        "General",
                        "General settings",
                        List.of(new ElarionConfigEntry<>(
                                "creation.fee",
                                "Creation Fee",
                                "Creation fee",
                                "test.yml.creation.fee",
                                ElarionConfigCodec.LONG,
                                25L,
                                () -> current,
                                ElarionConfigValidator.longMinimum("test.yml.creation.fee", 0L),
                                List.of(),
                                "0",
                                "",
                                true,
                                false,
                                ElarionConfigPermission.OPERATOR,
                                ElarionConfigPermission.OPERATOR)))));
    }
}
