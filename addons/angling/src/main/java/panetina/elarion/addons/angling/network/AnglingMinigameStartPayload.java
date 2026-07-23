package panetina.elarion.addons.angling.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;
import panetina.elarion.addons.angling.minigame.AnglingNativeModifier;
import panetina.elarion.addons.angling.minigame.AnglingServerMinigameSpec;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshot;

import java.util.Objects;
import java.util.UUID;

/** Immutable server-selected minigame presentation contract sent once per session. */
public record AnglingMinigameStartPayload(
        UUID sessionId,
        int bobberEntityId,
        long seed,
        Identifier catchDefinitionId,
        Identifier surfaceTextureId,
        Identifier displayItemId,
        Identifier treasureItemId,
        int hitPoints,
        float pointerSpeed,
        float missPenalty,
        float decay,
        float hitDelayTicks,
        float initialProgress,
        boolean treasureAvailable,
        boolean disableHitSounds,
        boolean disableMissSounds,
        boolean pullDownPresentation
) implements CustomPayload {
    public static final Id<AnglingMinigameStartPayload> ID = new Id<>(
            Identifier.of(ElarionAnglingAddon.MOD_ID, "minigame_start"));
    public static final PacketCodec<PacketByteBuf, AnglingMinigameStartPayload> CODEC = PacketCodec.of(
            AnglingMinigameStartPayload::write,
            AnglingMinigameStartPayload::read);

    public AnglingMinigameStartPayload {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(catchDefinitionId, "catchDefinitionId");
        Objects.requireNonNull(surfaceTextureId, "surfaceTextureId");
        Objects.requireNonNull(displayItemId, "displayItemId");
        Objects.requireNonNull(treasureItemId, "treasureItemId");
        if (bobberEntityId < 0 || hitPoints < 1 || hitPoints > 1_000_000
                || !Float.isFinite(pointerSpeed) || !Float.isFinite(missPenalty)
                || !Float.isFinite(decay) || !Float.isFinite(hitDelayTicks)
                || !Float.isFinite(initialProgress)) {
            throw new IllegalArgumentException("Invalid minigame start payload");
        }
    }

    public static AnglingMinigameStartPayload create(
            UUID sessionId,
            int bobberEntityId,
            long seed,
            Identifier catchDefinitionId,
            AnglingServerMinigameSpec spec
    ) {
        return create(sessionId, bobberEntityId, seed, catchDefinitionId,
                Identifier.of("elarion_angling", "textures/gui/minigame/surface.png"),
                Identifier.of("elarion_angling", "unknown_fish"), Identifier.ofVanilla("air"), false, spec);
    }

    public static AnglingMinigameStartPayload create(
            UUID sessionId,
            int bobberEntityId,
            long seed,
            Identifier catchDefinitionId,
            Identifier surfaceTextureId,
            Identifier displayItemId,
            Identifier treasureItemId,
            boolean hideCatch,
            AnglingServerMinigameSpec spec
    ) {
        Objects.requireNonNull(spec, "spec");
        return new AnglingMinigameStartPayload(
                sessionId, bobberEntityId, seed, catchDefinitionId, surfaceTextureId,
                hideCatch ? Identifier.of("elarion_angling", "unknown_fish") : displayItemId,
                treasureItemId,
                spec.hitPoints(), spec.pointerSpeed(), spec.missPenalty(), spec.decay(),
                spec.hitDelayTicks(), spec.initialProgress(), spec.treasureAvailable(),
                spec.modifiers().stream().anyMatch(AnglingNativeModifier.DisableHitSounds.class::isInstance),
                spec.modifiers().stream().anyMatch(AnglingNativeModifier.DisableMissSounds.class::isInstance),
                spec.modifiers().stream().anyMatch(AnglingNativeModifier.PullDown.class::isInstance));
    }

    private static void write(AnglingMinigameStartPayload payload, PacketByteBuf buffer) {
        buffer.writeUuid(payload.sessionId);
        buffer.writeVarInt(payload.bobberEntityId);
        buffer.writeLong(payload.seed);
        buffer.writeIdentifier(payload.catchDefinitionId);
        buffer.writeIdentifier(payload.surfaceTextureId);
        buffer.writeIdentifier(payload.displayItemId);
        buffer.writeIdentifier(payload.treasureItemId);
        buffer.writeVarInt(payload.hitPoints);
        buffer.writeFloat(payload.pointerSpeed);
        buffer.writeFloat(payload.missPenalty);
        buffer.writeFloat(payload.decay);
        buffer.writeFloat(payload.hitDelayTicks);
        buffer.writeFloat(payload.initialProgress);
        buffer.writeBoolean(payload.treasureAvailable);
        buffer.writeBoolean(payload.disableHitSounds);
        buffer.writeBoolean(payload.disableMissSounds);
        buffer.writeBoolean(payload.pullDownPresentation);
    }

    private static AnglingMinigameStartPayload read(PacketByteBuf buffer) {
        return new AnglingMinigameStartPayload(
                buffer.readUuid(), buffer.readVarInt(), buffer.readLong(), buffer.readIdentifier(),
                buffer.readIdentifier(), buffer.readIdentifier(), buffer.readIdentifier(),
                buffer.readVarInt(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
