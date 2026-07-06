package panetina.elarion.addons.mounts.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public enum MountToggleActivePayload implements CustomPayload {
    INSTANCE;

    public static final Id<MountToggleActivePayload> ID =
            new Id<>(Identifier.of("elarion_mounts", "mount_toggle_active"));
    public static final PacketCodec<PacketByteBuf, MountToggleActivePayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
