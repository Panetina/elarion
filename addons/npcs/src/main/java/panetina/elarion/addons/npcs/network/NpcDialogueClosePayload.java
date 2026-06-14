package panetina.elarion.addons.npcs.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record NpcDialogueClosePayload(boolean close) implements CustomPayload {
    public static final NpcDialogueClosePayload INSTANCE = new NpcDialogueClosePayload(true);
    public static final Id<NpcDialogueClosePayload> ID =
            new Id<>(Identifier.of("elarion_npcs", "dialogue_close"));

    public static final PacketCodec<PacketByteBuf, NpcDialogueClosePayload> CODEC = PacketCodec.of(
            (payload, buffer) -> buffer.writeBoolean(payload.close()),
            buffer -> new NpcDialogueClosePayload(buffer.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
