package panetina.elarion.addons.angling.model;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.CatchTelemetryEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class CatchTelemetryContractTest {
    @Test
    void placeholderDefinitionPopulatesCoreTelemetryWithoutVisibleContent() {
        FishDefinition definition = new FishDefinition(
                Identifier.of("elarion_angling", "placeholder_fish_001"),
                "fish.elarion_angling.placeholder_fish_001",
                AnglingRarity.PLACEHOLDER_COMMON,
                10,
                List.of());

        CatchTelemetryEvent event = new CatchTelemetryEvent(
                UUID.randomUUID(),
                1,
                UUID.randomUUID(),
                Identifier.of("elarion_angling", "fishing"),
                definition.id(),
                definition.rarity().id(),
                1,
                null,
                null,
                null,
                Map.of());

        assertEquals(definition.id(), event.fishDefinitionId());
        assertEquals(Identifier.of("elarion_angling", "placeholder_common"), event.rarityId());
        assertEquals("elarion_angling", event.sourceId().getNamespace());
        assertEquals("elarion_angling", event.fishDefinitionId().getNamespace());
        assertEquals("elarion_angling", event.rarityId().getNamespace());
        assertFalse(event.toString().contains(definition.translationKey()));
    }
}
