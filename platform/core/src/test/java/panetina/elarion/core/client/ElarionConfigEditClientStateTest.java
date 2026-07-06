package panetina.elarion.core.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.config.ElarionConfigChangeError;
import panetina.elarion.core.config.ElarionConfigChangeResult;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigEditControl;
import panetina.elarion.core.config.ElarionConfigEditTarget;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.network.ElarionConfigEditResultPayload;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionConfigEditClientStateTest {
    @AfterEach
    void clearState() {
        ElarionConfigEditClientState.clear();
    }

    @Test
    void storesAndClearsLastConfigEditResult() {
        ElarionConfigEditResultPayload result = new ElarionConfigEditResultPayload(
                new ElarionConfigEditTarget("core", "general", "creation.fee"),
                ElarionConfigChangeResult.Status.VALIDATED,
                "125",
                "90",
                true,
                false,
                false,
                "Would change core:general:creation.fee from 125 to 90.",
                List.of(),
                "Valid: 125 -> 90. Reload required.");

        ElarionConfigEditClientState.update(result);

        assertEquals(result, ElarionConfigEditClientState.lastResult().orElseThrow());

        ElarionConfigEditClientState.clear();

        assertTrue(ElarionConfigEditClientState.lastResult().isEmpty());
    }

    @Test
    void storesRejectedResults() {
        ElarionConfigEditResultPayload result = new ElarionConfigEditResultPayload(
                new ElarionConfigEditTarget("core", "general", "creation.fee"),
                ElarionConfigChangeResult.Status.REJECTED,
                "",
                "",
                false,
                false,
                false,
                "",
                List.of(ElarionConfigChangeError.of(
                        ElarionConfigChangeError.Code.PERMISSION_DENIED,
                        "",
                        "Only OP level 4 admins can validate config edits.")),
                "Only OP level 4 admins can validate config edits.");

        ElarionConfigEditClientState.update(result);

        assertEquals(ElarionConfigChangeResult.Status.REJECTED,
                ElarionConfigEditClientState.lastResult().orElseThrow().status());
        assertEquals(ElarionConfigChangeError.Code.PERMISSION_DENIED,
                ElarionConfigEditClientState.lastResult().orElseThrow().errors().getFirst().code());
    }

    @Test
    void clearsLastResultWithoutClosingOpenControl() {
        ElarionConfigEditControl control = control("creation.fee");
        ElarionConfigEditResultPayload result = new ElarionConfigEditResultPayload(
                new ElarionConfigEditTarget("core", "general", "creation.fee"),
                ElarionConfigChangeResult.Status.VALIDATED,
                "125",
                "90",
                true,
                false,
                false,
                "Would change core:general:creation.fee from 125 to 90.",
                List.of(),
                "Valid: 125 -> 90. Reload required.");

        ElarionConfigEditClientState.open(control);
        ElarionConfigEditClientState.update(result);
        ElarionConfigEditClientState.clearLastResult();

        assertEquals(control, ElarionConfigEditClientState.openControl().orElseThrow());
        assertTrue(ElarionConfigEditClientState.lastResult().isEmpty());
    }

    @Test
    void storesOpenControlAndClearsWithState() {
        ElarionConfigEditControl control = control("creation.fee");

        ElarionConfigEditClientState.open(control);

        assertEquals(control, ElarionConfigEditClientState.openControl().orElseThrow());
        assertFalse(ElarionConfigEditClientState.openControl().orElseThrow().applyAvailable());
        assertEquals("Config apply is not enabled yet.",
                ElarionConfigEditClientState.openControl().orElseThrow().applyDisabledReason());

        ElarionConfigEditClientState.clear();

        assertTrue(ElarionConfigEditClientState.openControl().isEmpty());
        assertTrue(ElarionConfigEditClientState.lastResult().isEmpty());
    }

    @Test
    void openingNewControlClearsStaleResult() {
        ElarionConfigEditResultPayload result = new ElarionConfigEditResultPayload(
                new ElarionConfigEditTarget("core", "general", "creation.fee"),
                ElarionConfigChangeResult.Status.VALIDATED,
                "125",
                "90",
                true,
                false,
                false,
                "Would change core:general:creation.fee from 125 to 90.",
                List.of(),
                "Valid: 125 -> 90. Reload required.");
        ElarionConfigEditClientState.update(result);

        ElarionConfigEditControl control = control("nickname.max_length");
        ElarionConfigEditClientState.open(control);

        assertEquals(control, ElarionConfigEditClientState.openControl().orElseThrow());
        assertTrue(ElarionConfigEditClientState.lastResult().isEmpty());
    }

    @Test
    void closingOpenControlPreservesLastResult() {
        ElarionConfigEditControl control = control("creation.fee");
        ElarionConfigEditResultPayload result = new ElarionConfigEditResultPayload(
                new ElarionConfigEditTarget("core", "general", "creation.fee"),
                ElarionConfigChangeResult.Status.VALIDATED,
                "125",
                "90",
                true,
                false,
                false,
                "Would change core:general:creation.fee from 125 to 90.",
                List.of(),
                "Valid: 125 -> 90. Reload required.");

        ElarionConfigEditClientState.open(control);
        ElarionConfigEditClientState.update(result);
        ElarionConfigEditClientState.closeOpenControl();

        assertTrue(ElarionConfigEditClientState.openControl().isEmpty());
        assertEquals(result, ElarionConfigEditClientState.lastResult().orElseThrow());
    }

    @Test
    void appliedResultClosesOpenControl() {
        ElarionConfigEditControl control = control("creation.fee");
        ElarionConfigEditResultPayload result = new ElarionConfigEditResultPayload(
                new ElarionConfigEditTarget("core", "general", "creation.fee"),
                ElarionConfigChangeResult.Status.APPLIED,
                "125",
                "90",
                true,
                false,
                false,
                "Changed core:general:creation.fee from 125 to 90.",
                List.of(),
                "Applied: 125 -> 90. Reload required.");

        ElarionConfigEditClientState.open(control);
        ElarionConfigEditClientState.update(result);

        assertTrue(ElarionConfigEditClientState.openControl().isEmpty());
        assertEquals(result, ElarionConfigEditClientState.lastResult().orElseThrow());
    }

    private static ElarionConfigEditControl control(String entryId) {
        return new ElarionConfigEditControl(
                new ElarionConfigEditTarget("core", "general", entryId),
                "Creation Fee",
                "Creation fee",
                "test.yml." + entryId,
                ElarionConfigCodec.ValueType.LONG,
                "125",
                "25",
                List.of(),
                "0",
                "",
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR,
                false,
                false,
                "Config editing is not enabled yet.",
                "Config apply is not enabled yet.");
    }
}
