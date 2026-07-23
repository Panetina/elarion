package panetina.elarion.addons.angling.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;

import java.util.Objects;
import java.util.UUID;

/**
 * A client input edge, never a gameplay result. Duration, hits, progress,
 * treasure, perfect state, and success are deliberately absent.
 */
public record AnglingMinigameInputPayload(
        UUID sessionId,
        int bobberEntityId,
        int sequence,
        AnglingMinigameInputAction action
) implements CustomPayload {
    public static final Id<AnglingMinigameInputPayload> ID = new Id<>(
            Identifier.of(ElarionAnglingAddon.MOD_ID, "minigame_input"));
    public static final PacketCodec<PacketByteBuf, AnglingMinigameInputPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeUuid(payload.sessionId());
                buffer.writeVarInt(payload.bobberEntityId());
                buffer.writeVarInt(payload.sequence());
                buffer.writeByte(payload.action().ordinal());
            },
            buffer -> new AnglingMinigameInputPayload(
                    buffer.readUuid(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    AnglingMinigameInputAction.fromWire(buffer.readUnsignedByte()))
    );

    public AnglingMinigameInputPayload {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(action, "action");
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
