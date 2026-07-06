package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import panetina.elarion.core.config.ElarionConfigApplyAuditJournal;
import panetina.elarion.core.config.ElarionConfigApplyCapability;
import panetina.elarion.core.config.ElarionConfigApplyReadiness;
import panetina.elarion.core.config.ElarionConfigApplyRegistry;
import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigChangeError;
import panetina.elarion.core.config.ElarionConfigChangeRequest;
import panetina.elarion.core.config.ElarionConfigChangeResult;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEditTarget;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigPreparedChange;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.config.ElarionConfigValidator;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionConfigApplyServiceTest {
    private static final ElarionConfigEditTarget TARGET =
            new ElarionConfigEditTarget("core", "general", "enabled");
    private static final UUID ACTOR = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @TempDir
    Path tempDir;

    @Test
    void unboundServiceBlocksOtherwiseReadyTarget() {
        Fixture fixture = fixture(true);
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());

        ElarionConfigApplyReadiness readiness = service.readiness(TARGET);

        assertFalse(service.executionReady());
        assertNull(service.journalFile());
        assertFalse(readiness.ready());
        assertEquals(ElarionConfigChangeError.Code.UNSUPPORTED, readiness.errors().getFirst().code());
        assertTrue(readiness.firstErrorMessage().contains("not bound"));
    }

    @Test
    void boundServiceDelegatesReadyTargetAndExposesJournalPath() {
        Fixture fixture = fixture(true);
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());

        service.bind(tempDir);

        ElarionConfigApplyReadiness readiness = service.readiness(TARGET);
        assertTrue(service.executionReady());
        assertEquals(ElarionConfigApplyAuditJournal.journalPath(tempDir), service.journalFile());
        assertTrue(readiness.ready());
        assertEquals("config.apply.test", readiness.auditEventType());
        assertEquals(List.of("config/elarion/core/test.yml"), readiness.affectedFiles());
    }

    @Test
    void serviceDelegatesDescriptorAndMissingApplierFailures() {
        Fixture fixture = fixture(false);
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());
        service.bind(tempDir);

        ElarionConfigApplyReadiness missingApplier = service.readiness(TARGET);
        assertFalse(missingApplier.ready());
        assertEquals(ElarionConfigChangeError.Code.UNSUPPORTED, missingApplier.errors().getFirst().code());
        assertTrue(missingApplier.firstErrorMessage().contains("No config applier"));

        ElarionConfigApplyReadiness missingEntry = service.readiness(
                new ElarionConfigEditTarget("core", "general", "missing"));
        assertFalse(missingEntry.ready());
        assertEquals(ElarionConfigChangeError.Code.UNKNOWN_ENTRY, missingEntry.errors().getFirst().code());
    }

    @Test
    void unresolvedPreparedAuditBlocksExecutionAfterBind() {
        ElarionConfigApplyAuditJournal journal = new ElarionConfigApplyAuditJournal(
                ElarionConfigApplyAuditJournal.journalPath(tempDir));
        journal.prepare(auditRecord("old", "new"));
        Fixture fixture = fixture(true);
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());

        service.bind(tempDir);

        ElarionConfigApplyReadiness readiness = service.readiness(TARGET);
        assertFalse(service.executionReady());
        assertFalse(readiness.ready());
        assertTrue(readiness.firstErrorMessage().contains("unresolved prepared"));
    }

    @Test
    void truncatedRecoveryWindowBlocksExecutionAfterBind() {
        ElarionConfigApplyAuditJournal journal = new ElarionConfigApplyAuditJournal(
                ElarionConfigApplyAuditJournal.journalPath(tempDir));
        journal.prepare(auditRecord("old", "new")).committed();
        Fixture fixture = fixture(true);
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers(), 1);

        service.bind(tempDir);

        ElarionConfigApplyReadiness readiness = service.readiness(TARGET);
        assertFalse(service.executionReady());
        assertFalse(readiness.ready());
        assertTrue(readiness.firstErrorMessage().contains("truncated"));
    }

    @Test
    void unbindClearsExecutionState() {
        Fixture fixture = fixture(true);
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());
        service.bind(tempDir);

        service.unbind();

        assertFalse(service.executionReady());
        assertNull(service.journalFile());
        assertTrue(service.disabledReason().contains("not bound"));
        assertFalse(service.readiness(TARGET).ready());
    }

    @Test
    void applyRejectsWhileUnbound() {
        Fixture fixture = fixture(true);
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());

        ElarionConfigChangeResult result = service.apply(request("false", "true"),
                ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeResult.Status.REJECTED, result.status());
        assertEquals(ElarionConfigChangeError.Code.UNSUPPORTED, result.errors().getFirst().code());
        assertTrue(result.errors().getFirst().message().contains("not bound"));
    }

    @Test
    void applyRejectsWhenRecoveryIsUnsafe() {
        ElarionConfigApplyAuditJournal journal = new ElarionConfigApplyAuditJournal(
                ElarionConfigApplyAuditJournal.journalPath(tempDir));
        journal.prepare(auditRecord("old", "new"));
        Fixture fixture = fixture(true);
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());
        service.bind(tempDir);

        ElarionConfigChangeResult result = service.apply(request("false", "true"),
                ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeResult.Status.REJECTED, result.status());
        assertEquals(ElarionConfigChangeError.Code.UNSUPPORTED, result.errors().getFirst().code());
        assertTrue(result.errors().getFirst().message().contains("unresolved prepared"));
    }

    @Test
    void applyDelegatesToCoordinatorWhenBoundAndSafe() throws Exception {
        Fixture fixture = fixture(true);
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());
        service.bind(tempDir);

        ElarionConfigChangeResult result = service.apply(request("false", "true"),
                ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeResult.Status.APPLIED, result.status());
        assertEquals("true", result.oldDisplayValue());
        assertEquals("false", result.newDisplayValue());
        assertEquals("config.apply.test", result.auditEventType());
        ElarionConfigApplyAuditJournal journal = new ElarionConfigApplyAuditJournal(service.journalFile());
        assertTrue(journal.recoverUnresolvedTail(10).unresolved().isEmpty());
        assertEquals(2, Files.readAllLines(service.journalFile()).size());
    }

    @Test
    void applyDelegatesValidationAndReadinessFailuresWhenBound() {
        Fixture fixture = fixture(false);
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());
        service.bind(tempDir);

        ElarionConfigChangeResult stale = service.apply(request("false", "false"),
                ElarionConfigPermission.OPERATOR);
        assertEquals(ElarionConfigChangeError.Code.STALE_VALUE, stale.errors().getFirst().code());

        ElarionConfigChangeResult missingApplier = service.apply(request("false", "true"),
                ElarionConfigPermission.OPERATOR);
        assertEquals(ElarionConfigChangeError.Code.UNSUPPORTED, missingApplier.errors().getFirst().code());
        assertTrue(missingApplier.errors().getFirst().message().contains("No config applier"));
    }

    private static Fixture fixture(boolean registerApplier) {
        ElarionConfigRegistry descriptors = new ElarionConfigRegistry();
        descriptors.registerDomain(new ElarionConfigDomain(
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
                        List.of(entry())))));
        ElarionConfigApplyRegistry appliers = new ElarionConfigApplyRegistry();
        if (registerApplier) {
            appliers.register(TARGET, ElarionConfigApplyCapability.runtimeReload(
                    "config.apply.test",
                    List.of("config/elarion/core/test.yml")),
                    context -> ElarionConfigPreparedChange.of(
                            () -> ElarionConfigChangeResult.applied(
                                    context.request(), "true", "false", true, false, "config.apply.test"),
                            () -> { }));
        }
        return new Fixture(descriptors, appliers);
    }

    private static ElarionConfigEntry<Boolean> entry() {
        return new ElarionConfigEntry<>(
                "enabled",
                "Enabled",
                "Test entry",
                "test.yml.enabled",
                ElarionConfigCodec.BOOLEAN,
                true,
                () -> true,
                ElarionConfigValidator.pass(),
                List.of("true", "false"),
                "",
                "",
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
    }

    private static panetina.elarion.core.config.ElarionConfigApplyAuditRecord auditRecord(
            String oldValue,
            String newValue
    ) {
        return new panetina.elarion.core.config.ElarionConfigApplyAuditRecord(
                TARGET,
                ACTOR,
                "test reason",
                oldValue,
                newValue,
                true,
                false,
                "config.apply.test",
                List.of("config/elarion/core/test.yml"));
    }

    private static ElarionConfigChangeRequest request(String proposed, String expected) {
        return new ElarionConfigChangeRequest(
                TARGET.domainId(),
                TARGET.categoryId(),
                TARGET.entryId(),
                proposed,
                expected,
                ACTOR,
                "test apply");
    }

    private record Fixture(
            ElarionConfigRegistry descriptors,
            ElarionConfigApplyRegistry appliers
    ) {
    }
}
