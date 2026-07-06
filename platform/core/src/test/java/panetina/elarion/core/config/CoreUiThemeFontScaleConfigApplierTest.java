package panetina.elarion.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import panetina.elarion.core.service.ElarionConfigApplyService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CoreUiThemeFontScaleConfigApplierTest {
    private static final UUID ACTOR = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @TempDir
    Path tempDir;

    @Test
    void readinessIsScopedToFontScaleTarget() {
        Fixture fixture = fixture();
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());
        service.bind(tempDir.resolve("world"));

        ElarionConfigApplyReadiness ready = service.readiness(
                CoreUiThemeFontScaleConfigApplier.TARGET);
        ElarionConfigApplyReadiness missing = service.readiness(
                new ElarionConfigEditTarget("core", "ui_theme", "defaults.row-height"));

        assertTrue(ready.ready());
        assertEquals(CoreUiThemeFontScaleConfigApplier.AUDIT_EVENT_TYPE, ready.auditEventType());
        assertEquals(java.util.List.of(CoreUiThemeFontScaleConfigApplier.AFFECTED_FILE),
                ready.affectedFiles());
        assertFalse(missing.ready());
        assertTrue(missing.firstErrorMessage().contains("No config applier"));
    }

    @Test
    void appliesFontScaleThroughAuditBackedService() throws Exception {
        Fixture fixture = fixture();
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());
        service.bind(tempDir.resolve("world"));

        ElarionConfigChangeResult result = service.apply(request("125", "100"),
                ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeResult.Status.APPLIED, result.status());
        assertEquals("100", result.oldDisplayValue());
        assertEquals("125", result.newDisplayValue());
        assertTrue(result.reloadRequired());
        assertFalse(result.restartRequired());
        assertEquals(CoreUiThemeFontScaleConfigApplier.AUDIT_EVENT_TYPE, result.auditEventType());
        assertEquals(125, fixture.config().uiTheme().fontScalePercent());
        assertEquals("125", fixture.descriptors().domain("core").orElseThrow()
                .entry("ui_theme", "defaults.font-scale-percent").orElseThrow()
                .currentDisplayValue());
        assertEquals(1, fixture.syncs().get());
        assertTrue(Files.readString(tempDir.resolve("ui_theme.yml"), StandardCharsets.UTF_8)
                .contains("font-scale-percent: 125"));
        assertEquals(2, Files.readAllLines(service.journalFile()).size());
        assertTrue(new ElarionConfigApplyAuditJournal(service.journalFile())
                .recoverUnresolvedTail(10).unresolved().isEmpty());
    }

    @Test
    void appliesFontScaleWhenOldConfigFileIsMissingKey() throws Exception {
        Fixture fixture = fixture();
        Path theme = tempDir.resolve("ui_theme.yml");
        Files.writeString(theme, Files.readString(theme, StandardCharsets.UTF_8)
                .replace("  font-scale-percent: 100\n", ""), StandardCharsets.UTF_8);
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());
        service.bind(tempDir.resolve("world"));

        ElarionConfigChangeResult result = service.apply(request("125", "100"),
                ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeResult.Status.APPLIED, result.status());
        String updated = Files.readString(theme, StandardCharsets.UTF_8);
        assertTrue(updated.contains("  minimum-scale-percent: 60\n  font-scale-percent: 125\n"));
        assertEquals(125, fixture.config().uiTheme().fontScalePercent());
        assertEquals(1, fixture.syncs().get());
    }

    @Test
    void duplicateFontScaleLinesRejectWithoutMutation() throws Exception {
        Fixture fixture = fixture();
        Path theme = tempDir.resolve("ui_theme.yml");
        String before = Files.readString(theme, StandardCharsets.UTF_8)
                .replace("  font-scale-percent: 100\n",
                        "  font-scale-percent: 100\n  font-scale-percent: 125\n");
        Files.writeString(theme, before, StandardCharsets.UTF_8);
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());
        service.bind(tempDir.resolve("world"));

        ElarionConfigChangeResult result = service.apply(request("125", "100"),
                ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeResult.Status.REJECTED, result.status());
        assertEquals(before, Files.readString(theme, StandardCharsets.UTF_8));
        assertEquals(100, fixture.config().uiTheme().fontScalePercent());
        assertEquals(0, fixture.syncs().get());
    }

    @Test
    void validatorRejectsInvalidValuesBeforeFileMutation() throws Exception {
        Fixture fixture = fixture();
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), fixture.appliers());
        service.bind(tempDir.resolve("world"));
        String before = Files.readString(tempDir.resolve("ui_theme.yml"), StandardCharsets.UTF_8);

        ElarionConfigChangeResult nonInteger = service.apply(request("large", "100"),
                ElarionConfigPermission.OPERATOR);
        ElarionConfigChangeResult outOfRange = service.apply(request("151", "100"),
                ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeError.Code.PARSE_FAILED, nonInteger.errors().getFirst().code());
        assertEquals(ElarionConfigChangeError.Code.VALIDATION_FAILED, outOfRange.errors().getFirst().code());
        assertEquals(before, Files.readString(tempDir.resolve("ui_theme.yml"), StandardCharsets.UTF_8));
        assertEquals(100, fixture.config().uiTheme().fontScalePercent());
        assertEquals(0, fixture.syncs().get());
    }

    @Test
    void rollbackRestoresFileAndRuntimeThemeWhenReloadFailsAfterWrite() throws Exception {
        Fixture fixture = fixture();
        AtomicInteger reloads = new AtomicInteger();
        ElarionConfigApplyRegistry appliers = new ElarionConfigApplyRegistry();
        CoreUiThemeFontScaleConfigApplier.register(appliers::register, fixture.config(), () -> {
            if (reloads.incrementAndGet() == 1) {
                throw new IllegalStateException("reload failed");
            }
            fixture.config().load();
        }, fixture.syncs()::incrementAndGet);
        ElarionConfigApplyService service = new ElarionConfigApplyService(
                fixture.descriptors(), appliers);
        service.bind(tempDir.resolve("world"));
        String before = Files.readString(tempDir.resolve("ui_theme.yml"), StandardCharsets.UTF_8);

        ElarionConfigChangeResult result = service.apply(request("125", "100"),
                ElarionConfigPermission.OPERATOR);

        assertEquals(ElarionConfigChangeResult.Status.REJECTED, result.status());
        assertEquals(ElarionConfigChangeError.Code.APPLY_FAILED, result.errors().getFirst().code());
        assertTrue(result.errors().getFirst().message().contains("reload failed"));
        assertEquals(before, Files.readString(tempDir.resolve("ui_theme.yml"), StandardCharsets.UTF_8));
        assertEquals(100, fixture.config().uiTheme().fontScalePercent());
        assertEquals(1, fixture.syncs().get());
    }

    private Fixture fixture() {
        CoreConfigManager config = new CoreConfigManager(LoggerFactory.getLogger("font-scale-test"), tempDir);
        config.load();
        ElarionConfigRegistry descriptors = new ElarionConfigRegistry();
        CoreConfigDescriptors.register(descriptors, config);
        ElarionConfigApplyRegistry appliers = new ElarionConfigApplyRegistry();
        AtomicInteger syncs = new AtomicInteger();
        CoreUiThemeFontScaleConfigApplier.register(appliers::register, config, syncs::incrementAndGet);
        return new Fixture(config, descriptors, appliers, syncs);
    }

    private static ElarionConfigChangeRequest request(String proposed, String expected) {
        ElarionConfigEditTarget target = CoreUiThemeFontScaleConfigApplier.TARGET;
        return new ElarionConfigChangeRequest(
                target.domainId(),
                target.categoryId(),
                target.entryId(),
                proposed,
                expected,
                ACTOR,
                "test font scale apply");
    }

    private record Fixture(
            CoreConfigManager config,
            ElarionConfigRegistry descriptors,
            ElarionConfigApplyRegistry appliers,
            AtomicInteger syncs
    ) {
    }
}
