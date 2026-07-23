package panetina.elarion.addons.angling.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Optional;

/** Item/entity output identity for one catch definition. */
public record AnglingCatchOutput(
        AnglingItemReference item,
        Optional<AnglingItemReference> fishBucket,
        Optional<Identifier> entity,
        boolean alwaysSpawnEntity,
        Optional<AnglingItemReference> overrideMinigameItem,
        AnglingCatchType type
) {
    public static final Codec<AnglingCatchOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AnglingItemReference.CODEC.fieldOf("item").forGetter(AnglingCatchOutput::item),
            AnglingItemReference.CODEC.optionalFieldOf("fish_bucket").forGetter(AnglingCatchOutput::fishBucket),
            Identifier.CODEC.optionalFieldOf("entity").forGetter(AnglingCatchOutput::entity),
            Codec.BOOL.optionalFieldOf("always_spawn_entity", false).forGetter(AnglingCatchOutput::alwaysSpawnEntity),
            AnglingItemReference.CODEC.optionalFieldOf("override_minigame_item")
                    .forGetter(AnglingCatchOutput::overrideMinigameItem),
            AnglingCatchType.CODEC.optionalFieldOf("type", AnglingCatchType.FISH).forGetter(AnglingCatchOutput::type)
    ).apply(instance, AnglingCatchOutput::new));

    public AnglingCatchOutput {
        Objects.requireNonNull(item, "item");
        fishBucket = Objects.requireNonNull(fishBucket, "fishBucket");
        entity = Objects.requireNonNull(entity, "entity");
        overrideMinigameItem = Objects.requireNonNull(overrideMinigameItem, "overrideMinigameItem");
        Objects.requireNonNull(type, "type");
        if (alwaysSpawnEntity && entity.isEmpty()) {
            throw new IllegalArgumentException("always_spawn_entity requires an entity identifier");
        }
    }
}
