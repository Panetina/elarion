package panetina.elarion.addons.angling.domainmap;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.modifier.AnglingCompiledModifier;
import panetina.elarion.addons.angling.treasure.AnglingTreasureDefinition;

import java.util.List;
import java.util.Map;

public record AnglingDomainMapSnapshot(
        long revision,
        Map<AnglingRegistrySelector, AnglingAquariumInteraction> aquariumInteractions,
        Map<AnglingRegistrySelector, Identifier> tackleSkins,
        Map<AnglingRegistrySelector, List<AnglingCompiledModifier>> itemModifiers,
        Map<AnglingRegistrySelector, List<AnglingCompiledModifier>> effectModifiers,
        Map<AnglingRegistrySelector, AnglingTreasureDefinition> treasures
) {
    public static final AnglingDomainMapSnapshot EMPTY = new AnglingDomainMapSnapshot(
            0, Map.of(), Map.of(), Map.of(), Map.of(), Map.of());

    public AnglingDomainMapSnapshot {
        if (revision < 0) throw new IllegalArgumentException("domain-map revision cannot be negative");
        aquariumInteractions = Map.copyOf(aquariumInteractions);
        tackleSkins = Map.copyOf(tackleSkins);
        itemModifiers = immutableLists(itemModifiers);
        effectModifiers = immutableLists(effectModifiers);
        treasures = Map.copyOf(treasures);
    }

    private static <K, V> Map<K, List<V>> immutableLists(Map<K, List<V>> source) {
        java.util.LinkedHashMap<K, List<V>> copy = new java.util.LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Map.copyOf(copy);
    }
}
