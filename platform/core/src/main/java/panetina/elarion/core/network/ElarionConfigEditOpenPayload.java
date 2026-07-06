package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.config.ElarionConfigEditControl;

import java.util.Objects;

public record ElarionConfigEditOpenPayload(
        ElarionConfigEditControl control,
        String message
) implements CustomPayload {
    public static final Id<ElarionConfigEditOpenPayload> ID =
            new Id<>(Identifier.of("elarion_core", "config_edit_open"));
    public static final PacketCodec<PacketByteBuf, ElarionConfigEditOpenPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionConfigEditPayloadCodecs.writeControl(buffer, payload.control());
                ElarionPacketCodecs.writeString(buffer, payload.message(), ElarionConfigEditPayloadCodecs.MAX_MESSAGE);
            },
            buffer -> new ElarionConfigEditOpenPayload(
                    ElarionConfigEditPayloadCodecs.readControl(buffer),
                    ElarionPacketCodecs.readString(buffer, ElarionConfigEditPayloadCodecs.MAX_MESSAGE)));

    public ElarionConfigEditOpenPayload {
        control = Objects.requireNonNull(control, "Config edit control is required");
        message = clean(message);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
