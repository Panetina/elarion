package panetina.elarion.addons.angling.definition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.Objects;

/**
 * Reload-time item reference compatible with both upstream string references
 * and counted {@code {id,count}} objects. Registry resolution happens only
 * after the complete definition snapshot has validated.
 */
public record AnglingItemReference(Identifier id, int count) {
    private static final Codec<AnglingItemReference> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(AnglingItemReference::id),
            Codec.intRange(1, 99).optionalFieldOf("count", 1).forGetter(AnglingItemReference::count)
    ).apply(instance, AnglingItemReference::new));

    public static final Codec<AnglingItemReference> CODEC = Codec.either(Identifier.CODEC, OBJECT_CODEC)
            .xmap(value -> value.map(id -> new AnglingItemReference(id, 1), reference -> reference),
                    reference -> reference.count == 1
                            ? Either.left(reference.id)
                            : Either.right(reference));

    public AnglingItemReference {
        Objects.requireNonNull(id, "id");
        if (count < 1 || count > 99) {
            throw new IllegalArgumentException("Item reference count must be between 1 and 99");
        }
    }
}
