package panetina.elarion.addons.angling.definition;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.compile.AnglingCompiledCatchDefinition;
import panetina.elarion.addons.angling.minigame.AnglingNativeModifier;
import panetina.elarion.addons.angling.minigame.AnglingSweetspotBehaviorType;
import panetina.elarion.addons.angling.restriction.AnglingRestriction;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Immutable, pre-indexed runtime view published only after a complete reload succeeds. */
public final class AnglingCatchSnapshot {
    private static final AnglingCatchSnapshot EMPTY = new AnglingCatchSnapshot(0, Map.of(), Map.of(), Map.of());

    private final long revision;
    private final Map<Identifier, NativeCatch> byId;
    private final Map<AnglingRarity, List<NativeCatch>> byRarity;
    private final Map<AnglingCatchType, List<NativeCatch>> byType;

    AnglingCatchSnapshot(
            long revision,
            Map<Identifier, NativeCatch> byId,
            Map<AnglingRarity, List<NativeCatch>> byRarity,
            Map<AnglingCatchType, List<NativeCatch>> byType
    ) {
        this.revision = revision;
        this.byId = Map.copyOf(byId);
        this.byRarity = immutableIndex(byRarity);
        this.byType = immutableIndex(byType);
    }

    public static AnglingCatchSnapshot empty() {
        return EMPTY;
    }

    public long revision() {
        return revision;
    }

    public int size() {
        return byId.size();
    }

    public Optional<NativeCatch> find(Identifier id) {
        return Optional.ofNullable(byId.get(Objects.requireNonNull(id, "id")));
    }

    public Map<Identifier, NativeCatch> all() {
        return byId;
    }

    public List<NativeCatch> byRarity(AnglingRarity rarity) {
        return byRarity.getOrDefault(Objects.requireNonNull(rarity, "rarity"), List.of());
    }

    public List<NativeCatch> byType(AnglingCatchType type) {
        return byType.getOrDefault(Objects.requireNonNull(type, "type"), List.of());
    }

    private static <K> Map<K, List<NativeCatch>> immutableIndex(Map<K, List<NativeCatch>> source) {
        Map<K, List<NativeCatch>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Map.copyOf(copy);
    }

    public record NativeCatch(
            Identifier id,
            AnglingCompiledCatchDefinition<AnglingRestriction, AnglingNativeModifier,
                    AnglingSweetspotBehaviorType> definition
    ) {
        public NativeCatch {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(definition, "definition");
        }

        public AnglingRarity rarity() {
            return definition.source().rarity();
        }

        public AnglingCatchType type() {
            return definition.source().catchInfo().type();
        }
    }
}
