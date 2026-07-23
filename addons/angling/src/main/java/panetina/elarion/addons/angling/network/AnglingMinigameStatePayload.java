package panetina.elarion.addons.angling.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;
import panetina.elarion.addons.angling.minigame.AnglingServerMinigameSnapshot;
import panetina.elarion.addons.angling.minigame.AnglingServerMinigameSpec;
import panetina.elarion.addons.angling.minigame.AnglingServerMinigameStatus;
import panetina.elarion.addons.angling.minigame.AnglingSweetspotBehaviorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Server-owned bounded correction state. It contains presentation facts, never mutable authority. */
public record AnglingMinigameStatePayload(AnglingServerMinigameSnapshot snapshot) implements CustomPayload {
    public static final Id<AnglingMinigameStatePayload> ID = new Id<>(
            Identifier.of(ElarionAnglingAddon.MOD_ID, "minigame_state"));
    public static final PacketCodec<PacketByteBuf, AnglingMinigameStatePayload> CODEC = PacketCodec.of(
            AnglingMinigameStatePayload::write,
            AnglingMinigameStatePayload::read);

    public AnglingMinigameStatePayload {
        Objects.requireNonNull(snapshot, "snapshot");
    }

    private static void write(AnglingMinigameStatePayload payload, PacketByteBuf buffer) {
        AnglingServerMinigameSnapshot state = payload.snapshot;
        buffer.writeUuid(state.sessionId());
        buffer.writeVarLong(state.revision());
        buffer.writeVarLong(state.elapsedTicks());
        buffer.writeByte(state.status().ordinal());
        buffer.writeFloat(state.pointerPosition());
        buffer.writeFloat(state.pointerSpeed());
        buffer.writeByte(state.pointerRotation());
        buffer.writeVarInt(state.pointerLayer());
        buffer.writeVarInt(state.maximumLayers());
        buffer.writeFloat(state.progress());
        buffer.writeFloat(state.smoothedProgress());
        buffer.writeVarInt(state.treasureProgress());
        buffer.writeBoolean(state.perfect());
        buffer.writeVarInt(state.consecutiveHits());
        buffer.writeVarInt(state.totalHits());
        buffer.writeByte(state.darkness());
        buffer.writeVarInt(state.sweetspots().size());
        for (AnglingServerMinigameSnapshot.Sweetspot spot : state.sweetspots()) {
            buffer.writeVarInt(spot.index());
            buffer.writeByte(spot.behavior().ordinal());
            buffer.writeIdentifier(spot.texturePath());
            buffer.writeFloat(spot.position());
            buffer.writeVarInt(spot.layer());
            buffer.writeVarInt(spot.hitboxSizePixels());
            buffer.writeFloat(spot.alpha());
            buffer.writeInt(spot.color());
        }
    }

    private static AnglingMinigameStatePayload read(PacketByteBuf buffer) {
        var sessionId = buffer.readUuid();
        long revision = buffer.readVarLong();
        long elapsedTicks = buffer.readVarLong();
        AnglingServerMinigameStatus status = enumValue(
                AnglingServerMinigameStatus.values(), buffer.readUnsignedByte(), "status");
        float pointerPosition = buffer.readFloat();
        float pointerSpeed = buffer.readFloat();
        int pointerRotation = buffer.readByte();
        int pointerLayer = buffer.readVarInt();
        int maximumLayers = buffer.readVarInt();
        float progress = buffer.readFloat();
        float smoothedProgress = buffer.readFloat();
        int treasureProgress = buffer.readVarInt();
        boolean perfect = buffer.readBoolean();
        int consecutiveHits = buffer.readVarInt();
        int totalHits = buffer.readVarInt();
        int darkness = buffer.readUnsignedByte();
        int count = buffer.readVarInt();
        if (count < 0 || count > AnglingServerMinigameSpec.MAX_RUNTIME_SWEETSPOTS) {
            throw new IllegalArgumentException("Minigame state sweetspot count exceeds the protocol bound");
        }
        List<AnglingServerMinigameSnapshot.Sweetspot> spots = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            int spotIndex = buffer.readVarInt();
            AnglingSweetspotBehaviorType behavior = enumValue(
                    AnglingSweetspotBehaviorType.values(), buffer.readUnsignedByte(), "sweetspot behavior");
            spots.add(new AnglingServerMinigameSnapshot.Sweetspot(
                    spotIndex, behavior, buffer.readIdentifier(), buffer.readFloat(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readFloat(), buffer.readInt()));
        }
        return new AnglingMinigameStatePayload(new AnglingServerMinigameSnapshot(
                sessionId, revision, elapsedTicks, status, pointerPosition, pointerSpeed,
                pointerRotation, pointerLayer, maximumLayers, progress, smoothedProgress,
                treasureProgress, perfect, consecutiveHits, totalHits, darkness, spots));
    }

    private static <E extends Enum<E>> E enumValue(E[] values, int ordinal, String label) {
        if (ordinal < 0 || ordinal >= values.length) {
            throw new IllegalArgumentException("Invalid minigame " + label + " value " + ordinal);
        }
        return values[ordinal];
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
