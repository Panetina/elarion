package panetina.elarion.addons.angling.domainmap;

import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;
import panetina.elarion.addons.angling.modifier.AnglingCompiledModifier;
import panetina.elarion.addons.angling.treasure.AnglingTreasureDefinition;
import com.mojang.serialization.JsonOps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingDomainMapParityTest {
    @Test
    void authorizedAquariumAndTackleMapsCompileIntoOneImmutableSnapshot() throws Exception {
        Map<AnglingRegistrySelector, AnglingAquariumInteraction> aquarium = decode(
                "data/elarion_angling/elarion_angling/aquarium/interactions.json",
                value -> AnglingAquariumInteraction.parse(value.getAsString()));
        Map<AnglingRegistrySelector, Identifier> tackle = decode(
                "data/elarion_angling/elarion_angling/equipment/tackle_skins.json",
                value -> Identifier.of(value.getAsString()));
        Map<AnglingRegistrySelector, List<AnglingCompiledModifier>> itemModifiers = decode(
                "data/elarion_angling/elarion_angling/equipment/item_modifiers.json",
                AnglingDomainMapReloadListener::compileModifierList);
        Map<AnglingRegistrySelector, List<AnglingCompiledModifier>> effectModifiers = decode(
                "data/elarion_angling/elarion_angling/equipment/effect_modifiers.json",
                AnglingDomainMapReloadListener::compileModifierList);
        Map<AnglingRegistrySelector, AnglingTreasureDefinition> treasures = decode(
                "data/elarion_angling/elarion_angling/treasure/by_catch.json",
                value -> AnglingTreasureDefinition.CODEC.parse(JsonOps.INSTANCE, value).getOrThrow());

        assertEquals(13, aquarium.size());
        assertEquals(8, tackle.size());
        assertEquals(56, itemModifiers.size());
        assertEquals(1, effectModifiers.size());
        assertEquals(11, treasures.size());
        assertEquals(AnglingAquariumInteraction.REMOVE_FISH,
                aquarium.get(AnglingRegistrySelector.parse("#c:buckets/empty")));
        assertEquals(Identifier.of("elarion_angling", "valley"),
                tackle.get(AnglingRegistrySelector.parse("elarion_angling:valley_smithing_template")));

        AnglingDomainMapRepository repository = new AnglingDomainMapRepository();
        repository.publish(aquarium, tackle, itemModifiers, effectModifiers, treasures);
        AnglingDomainMapSnapshot snapshot = repository.current();
        assertEquals(1, snapshot.revision());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.tackleSkins().clear());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.itemModifiers().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.itemModifiers().values().iterator().next().clear());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.treasures().clear());
    }

    @Test
    void selectorsAndInteractionIdsFailClosed() {
        assertTrue(AnglingRegistrySelector.parse("#minecraft:pickaxes").tag());
        assertThrows(IllegalArgumentException.class, () -> AnglingRegistrySelector.parse("#invalid id"));
        assertThrows(IllegalArgumentException.class, () -> AnglingAquariumInteraction.parse("unknown"));
    }

    private static <T> Map<AnglingRegistrySelector, T> decode(
            String resource,
            java.util.function.Function<com.google.gson.JsonElement, T> parser
    ) throws Exception {
        try (var stream = AnglingDomainMapParityTest.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) throw new AssertionError("missing test resource " + resource);
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return AnglingDomainMapReloadListener.decode(
                        Identifier.of("elarion_angling", resource.substring(resource.indexOf("elarion_angling/", 5))),
                        JsonParser.parseReader(reader), parser);
            }
        }
    }
}
