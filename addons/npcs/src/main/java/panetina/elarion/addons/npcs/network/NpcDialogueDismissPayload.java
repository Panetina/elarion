package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record NpcDialogueDismissPayload(UUID npcId) implements CustomPayload {
    public static final Id<NpcDialogueDismissPayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "dialogue_dismiss"));

    public static final PacketCodec<PacketByteBuf, NpcDialogueDismissPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> buffer.writeUuid(payload.npcId()),
            buffer -> new NpcDialogueDismissPayload(buffer.readUuid())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
