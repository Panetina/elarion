package panetina.elarion.addons.angling.treasure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.definition.AnglingItemReference;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Immutable, registry-independent treasure selection definition compiled during server-data reload. */
public record AnglingTreasureDefinition(
        List<WeightedLootTable> lootTables,
        List<WeightedStack> stacks,
        List<IngredientSelector> blacklist
) {
    public static final int MAX_ENTRIES_PER_LIST = 64;

    private static <T> Codec<List<T>> boundedList(Codec<T> element, String label) {
        return element.listOf().validate(values -> values.size() <= MAX_ENTRIES_PER_LIST
                ? DataResult.success(values)
                : DataResult.error(() -> label + " exceed " + MAX_ENTRIES_PER_LIST));
    }

    public static final Codec<AnglingTreasureDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            boundedList(WeightedLootTable.CODEC, "treasure loot tables").optionalFieldOf("loot_tables", List.of())
                    .forGetter(AnglingTreasureDefinition::lootTables),
            boundedList(WeightedStack.CODEC, "treasure stacks").optionalFieldOf("stacks", List.of())
                    .forGetter(AnglingTreasureDefinition::stacks),
            boundedList(IngredientSelector.CODEC, "treasure blacklist entries").optionalFieldOf("blacklist", List.of())
                    .forGetter(AnglingTreasureDefinition::blacklist)
    ).apply(instance, AnglingTreasureDefinition::new));

    public AnglingTreasureDefinition {
        lootTables = List.copyOf(Objects.requireNonNull(lootTables, "lootTables"));
        stacks = List.copyOf(Objects.requireNonNull(stacks, "stacks"));
        blacklist = List.copyOf(Objects.requireNonNull(blacklist, "blacklist"));
        if (lootTables.size() > MAX_ENTRIES_PER_LIST || stacks.size() > MAX_ENTRIES_PER_LIST
                || blacklist.size() > MAX_ENTRIES_PER_LIST) {
            throw new IllegalArgumentException("Treasure definition exceeds bounded list sizes");
        }
    }

    public record WeightedLootTable(Identifier lootTable, int weight) {
        public static final Codec<WeightedLootTable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("loot_table").forGetter(WeightedLootTable::lootTable),
                Codec.intRange(1, 1_000_000).optionalFieldOf("weight", 1).forGetter(WeightedLootTable::weight)
        ).apply(instance, WeightedLootTable::new));

        public WeightedLootTable { Objects.requireNonNull(lootTable, "lootTable"); }
    }

    public record WeightedStack(AnglingItemReference stack, int weight) {
        public static final Codec<WeightedStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                AnglingItemReference.CODEC.fieldOf("stack").forGetter(WeightedStack::stack),
                Codec.intRange(1, 1_000_000).optionalFieldOf("weight", 1).forGetter(WeightedStack::weight)
        ).apply(instance, WeightedStack::new));

        public WeightedStack { Objects.requireNonNull(stack, "stack"); }
    }

    /** Vanilla ingredient identity used here without resolving registries in reload DTOs. */
    public record IngredientSelector(Optional<Identifier> item, Optional<Identifier> tag) {
        public static final Codec<IngredientSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("item").forGetter(IngredientSelector::item),
                Identifier.CODEC.optionalFieldOf("tag").forGetter(IngredientSelector::tag)
        ).apply(instance, IngredientSelector::new));

        public IngredientSelector {
            Objects.requireNonNull(item, "item");
            Objects.requireNonNull(tag, "tag");
            if (item.isPresent() == tag.isPresent()) {
                throw new IllegalArgumentException("Treasure blacklist ingredient must contain exactly one item or tag");
            }
        }
    }
}
