package panetina.elarion.addons.angling.model;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class FishDefinitionIndex {
    public static final int MAX_DEFINITIONS = 4096;

    private final List<FishDefinition> all;
    private final Map<Identifier, FishDefinition> byId;
    private final Map<AnglingRarity, List<FishDefinition>> byRarity;
    private final Map<AnglingConditionId, List<FishDefinition>> byCondition;

    public FishDefinitionIndex(Collection<FishDefinition> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        if (definitions.size() > MAX_DEFINITIONS) {
            throw new FishDefinitionValidationException(
                    "Fish definition index may contain at most " + MAX_DEFINITIONS + " definitions.");
        }

        List<FishDefinition> ordered = new ArrayList<>(definitions.size());
        Map<Identifier, FishDefinition> idIndex = new LinkedHashMap<>();
        Map<AnglingRarity, List<FishDefinition>> rarityIndex = new EnumMap<>(AnglingRarity.class);
        Map<AnglingConditionId, List<FishDefinition>> conditionIndex = new LinkedHashMap<>();

        for (FishDefinition definition : definitions) {
            Objects.requireNonNull(definition, "definition");
            FishDefinition previous = idIndex.putIfAbsent(definition.id(), definition);
            if (previous != null) {
                throw new FishDefinitionValidationException("Duplicate fish ID: " + definition.id());
            }

            ordered.add(definition);
            rarityIndex.computeIfAbsent(definition.rarity(), ignored -> new ArrayList<>()).add(definition);
            for (AnglingConditionId condition : definition.conditions()) {
                conditionIndex.computeIfAbsent(condition, ignored -> new ArrayList<>()).add(definition);
            }
        }

        this.all = List.copyOf(ordered);
        this.byId = Map.copyOf(idIndex);
        this.byRarity = freezeMap(rarityIndex);
        this.byCondition = freezeMap(conditionIndex);
    }

    public List<FishDefinition> all() {
        return all;
    }

    public Optional<FishDefinition> get(Identifier id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<FishDefinition> byRarity(AnglingRarity rarity) {
        return byRarity.getOrDefault(rarity, List.of());
    }

    public List<FishDefinition> byCondition(AnglingConditionId condition) {
        return byCondition.getOrDefault(condition, List.of());
    }

    private static <K> Map<K, List<FishDefinition>> freezeMap(Map<K, List<FishDefinition>> source) {
        Map<K, List<FishDefinition>> frozen = new LinkedHashMap<>();
        source.forEach((key, value) -> frozen.put(key, List.copyOf(value)));
        return Map.copyOf(frozen);
    }
}
