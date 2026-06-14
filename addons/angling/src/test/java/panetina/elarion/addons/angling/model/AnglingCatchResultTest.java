package panetina.elarion.addons.angling.model;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingCatchResultTest {
    @Test
    void convertsToCoreTelemetryWithoutVisibleContent() {
        AnglingCatchResult result = result(1);

        var telemetry = result.toTelemetryEvent();

        assertEquals(result.eventId(), telemetry.eventId());
        assertEquals(result.fishDefinitionId(), telemetry.fishDefinitionId());
        assertEquals(result.rarityId(), telemetry.rarityId());
        assertTrue(telemetry.metadata().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> telemetry.metadata().put("name", "forbidden"));
    }

    @Test
    void rejectsInvalidQuantityThroughCoreContractValidation() {
        assertThrows(IllegalArgumentException.class, () -> result(0));
    }

    private static AnglingCatchResult result(long quantity) {
        return new AnglingCatchResult(
                UUID.randomUUID(),
                1,
                UUID.randomUUID(),
                Identifier.of("elarion_angling", "fishing"),
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                Identifier.of("elarion_angling", "placeholder_common"),
                quantity,
                null,
                null,
                null);
    }
}
