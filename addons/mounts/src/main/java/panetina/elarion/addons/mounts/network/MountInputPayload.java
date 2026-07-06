package panetina.elarion.addons.mounts.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MountInputPayload(
        int entityId,
        float forward,
        float sideways,
        float lookYaw,
        float turnIntent,
        boolean jump,
        boolean sneak,
        boolean boost,
        boolean dismount
) implements CustomPayload {
    public static final Id<MountInputPayload> ID =
            new Id<>(Identifier.of("elarion_mounts", "mount_input"));
    public static final PacketCodec<PacketByteBuf, MountInputPayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeVarInt(payload.entityId);
                buf.writeFloat(payload.forward);
                buf.writeFloat(payload.sideways);
                buf.writeFloat(payload.lookYaw);
                buf.writeFloat(payload.turnIntent);
                buf.writeBoolean(payload.jump);
                buf.writeBoolean(payload.sneak);
                buf.writeBoolean(payload.boost);
                buf.writeBoolean(payload.dismount);
            },
            buf -> new MountInputPayload(
                    buf.readVarInt(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean())
    );

    public MountInputPayload {
        forward = clampFinite(forward, -1.0F, 1.0F);
        sideways = clampFinite(sideways, -1.0F, 1.0F);
        lookYaw = clampFinite(lookYaw, -360.0F, 360.0F);
        turnIntent = clampFinite(turnIntent, -1.0F, 1.0F);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private static float clampFinite(float value, float min, float max) {
        if (!Float.isFinite(value)) return 0.0F;
        return Math.max(min, Math.min(max, value));
    }
}
