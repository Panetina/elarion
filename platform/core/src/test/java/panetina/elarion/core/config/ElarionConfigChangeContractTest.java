package panetina.elarion.core.config;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionConfigChangeContractTest {
    @Test
    void requestNormalizesIdsAndPreservesSubmittedValue() {
        UUID actor = UUID.randomUUID();

        ElarionConfigChangeRequest request = new ElarionConfigChangeRequest(
                " Core ",
                " UI_Theme ",
                " Defaults.Font-Scale-Percent ",
                " 125 ",
                null,
                actor,
                "  admin changed font scale  ");

        assertEquals("core", request.domainId());
        assertEquals("ui_theme", request.categoryId());
        assertEquals("defaults.font-scale-percent", request.entryId());
        assertEquals("core:ui_theme:defaults.font-scale-percent", request.targetKey());
        assertEquals(" 125 ", request.proposedValue());
        assertEquals("", request.expectedCurrentValue());
        assertEquals(actor, request.actorId());
        assertEquals("admin changed font scale", request.reason());
    }

    @Test
    void requestRejectsInvalidTargetIds() {
        assertThrows(IllegalArgumentException.class, () -> new ElarionConfigChangeRequest(
                "core", "ui theme", "enabled", "true", "", null, ""));
        assertThrows(IllegalArgumentException.class, () -> new ElarionConfigChangeRequest(
                "core", "ui_theme", "", "true", "", null, ""));
    }

    @Test
    void errorUsesStableCodeAndDefaultMessage() {
        ElarionConfigChangeError error = new ElarionConfigChangeError(
                ElarionConfigChangeError.Code.PERMISSION_DENIED,
                " ui_theme.yml.defaults.font-scale-percent ",
                "");

        assertEquals(ElarionConfigChangeError.Code.PERMISSION_DENIED, error.code());
        assertEquals("ui_theme.yml.defaults.font-scale-percent", error.path());
        assertEquals("Permission denied.", error.message());
    }

    @Test
    void resultFactoriesModelValidatedAppliedAndRejectedStates() {
        ElarionConfigChangeRequest request = request();

        ElarionConfigChangeResult validated = ElarionConfigChangeResult.validated(
                request, "100", "125", true, false);
        assertEquals(ElarionConfigChangeResult.Status.VALIDATED, validated.status());
        assertTrue(validated.success());
        assertTrue(validated.reloadRequired());
        assertFalse(validated.restartRequired());
        assertTrue(validated.errors().isEmpty());

        ElarionConfigChangeResult applied = ElarionConfigChangeResult.applied(
                request, "100", "125", true, false, "admin-config-changed");
        assertEquals(ElarionConfigChangeResult.Status.APPLIED, applied.status());
        assertTrue(applied.success());
        assertTrue(applied.reloadRequired());
        assertFalse(applied.restartRequired());
        assertEquals("admin-config-changed", applied.auditEventType());

        ElarionConfigChangeError error = ElarionConfigChangeError.of(
                ElarionConfigChangeError.Code.VALIDATION_FAILED,
                "ui_theme.yml.defaults.font-scale-percent",
                "must be between 100 and 150");
        ElarionConfigChangeResult rejected = ElarionConfigChangeResult.rejected(request, List.of(error));
        assertEquals(ElarionConfigChangeResult.Status.REJECTED, rejected.status());
        assertFalse(rejected.success());
        assertEquals(List.of(error), rejected.errors());
    }

    @Test
    void resultErrorsAreImmutableAndStateCombinationsAreValidated() {
        ElarionConfigChangeRequest request = request();
        ElarionConfigChangeError error = ElarionConfigChangeError.of(
                ElarionConfigChangeError.Code.UNSUPPORTED, "core.enabled", "read-only");

        ElarionConfigChangeResult rejected = ElarionConfigChangeResult.rejected(request, List.of(error));
        assertThrows(UnsupportedOperationException.class, () -> rejected.errors().add(error));

        assertThrows(IllegalArgumentException.class, () -> new ElarionConfigChangeResult(
                ElarionConfigChangeResult.Status.VALIDATED, request, "old", "new",
                false, false, "", List.of(error)));
        assertThrows(IllegalArgumentException.class, () -> new ElarionConfigChangeResult(
                ElarionConfigChangeResult.Status.REJECTED, request, "", "",
                false, false, "", List.of()));
        assertThrows(NullPointerException.class, () -> new ElarionConfigChangeResult(
                ElarionConfigChangeResult.Status.REJECTED, null, "", "",
                false, false, "", List.of(error)));
        assertThrows(IllegalArgumentException.class, () -> new ElarionConfigChangeResult(
                ElarionConfigChangeResult.Status.REJECTED, request, "", "",
                false, false, "", java.util.Arrays.asList(error, null)));
        assertThrows(IllegalArgumentException.class, () -> new ElarionConfigChangeResult(
                ElarionConfigChangeResult.Status.APPLIED, request, "old", "new",
                true, false, "", List.of()));
    }

    private static ElarionConfigChangeRequest request() {
        return new ElarionConfigChangeRequest(
                "core",
                "ui_theme",
                "defaults.font-scale-percent",
                "125",
                "100",
                UUID.randomUUID(),
                "test");
    }
}
