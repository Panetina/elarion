package panetina.elarion.addons.angling.minigame;

import net.minecraft.util.Identifier;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Bounded immutable projection safe to encode for client corrections and rendering. */
public record AnglingServerMinigameSnapshot(
        UUID sessionId,
        long revision,
        long elapsedTicks,
        AnglingServerMinigameStatus status,
        float pointerPosition,
        float pointerSpeed,
        int pointerRotation,
        int pointerLayer,
        int maximumLayers,
        float progress,
        float smoothedProgress,
        int treasureProgress,
        boolean perfect,
        int consecutiveHits,
        int totalHits,
        int darkness,
        List<Sweetspot> sweetspots
) {
    public AnglingServerMinigameSnapshot {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(status, "status");
        sweetspots = List.copyOf(Objects.requireNonNull(sweetspots, "sweetspots"));
        if (revision < 0 || elapsedTicks < 0 || !Float.isFinite(pointerPosition)
                || !Float.isFinite(pointerSpeed) || !Float.isFinite(progress)
                || !Float.isFinite(smoothedProgress) || pointerRotation < -1 || pointerRotation > 1
                || pointerLayer < 0 || maximumLayers < 0 || pointerLayer > maximumLayers
                || treasureProgress < 0 || treasureProgress > 100
                || consecutiveHits < 0 || totalHits < 0 || darkness < 0 || darkness > 255
                || sweetspots.size() > AnglingServerMinigameSpec.MAX_RUNTIME_SWEETSPOTS) {
            throw new IllegalArgumentException("Invalid server minigame snapshot");
        }
    }

    public record Sweetspot(
            int index,
            AnglingSweetspotBehaviorType behavior,
            Identifier texturePath,
            float position,
            int layer,
            int hitboxSizePixels,
            float alpha,
            int color
    ) {
        public Sweetspot {
            Objects.requireNonNull(behavior, "behavior");
            Objects.requireNonNull(texturePath, "texturePath");
            if (index < 0 || layer < 0 || hitboxSizePixels < 1 || hitboxSizePixels > 512
                    || !Float.isFinite(position) || !Float.isFinite(alpha)) {
                throw new IllegalArgumentException("Invalid minigame sweetspot snapshot");
            }
        }
    }
}
