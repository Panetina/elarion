package panetina.elarion.addons.angling.condition;

import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.UUID;

public record AnglingConditionContext(
        UUID actorId,
        Identifier worldId,
        Identifier dimensionId,
        Identifier biomeId,
        Identifier fluidId,
        Identifier baitId,
        int blockY,
        long timeOfDay,
        boolean raining,
        boolean thundering
) {
    public static final long TICKS_PER_DAY = 24_000L;

    public AnglingConditionContext {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(worldId, "worldId");
        Objects.requireNonNull(dimensionId, "dimensionId");
        Objects.requireNonNull(biomeId, "biomeId");
        Objects.requireNonNull(fluidId, "fluidId");
        if (timeOfDay < 0 || timeOfDay >= TICKS_PER_DAY) {
            throw new IllegalArgumentException("timeOfDay must be between 0 and 23999");
        }
        if (thundering && !raining) {
            throw new IllegalArgumentException("thundering context must also be raining");
        }
    }
}
