package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.config.ElarionConfigEditTarget;

import java.util.Objects;

public record ElarionConfigEditRequestPayload(
        ElarionConfigEditTarget target,
        String expectedCurrentDisplayValue,
        String proposedRawValue,
        String reason,
        Intent intent
) implements CustomPayload {
    public static final Id<ElarionConfigEditRequestPayload> ID =
            new Id<>(Identifier.of("elarion_core", "config_edit_request"));
    public static final PacketCodec<PacketByteBuf, ElarionConfigEditRequestPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionConfigEditPayloadCodecs.writeTarget(buffer, payload.target());
                ElarionPacketCodecs.writeString(buffer, payload.expectedCurrentDisplayValue(),
                        ElarionConfigEditPayloadCodecs.MAX_VALUE);
                ElarionPacketCodecs.writeString(buffer, payload.proposedRawValue(),
                        ElarionConfigEditPayloadCodecs.MAX_VALUE);
                ElarionPacketCodecs.writeString(buffer, payload.reason(), ElarionConfigEditPayloadCodecs.MAX_REASON);
                buffer.writeEnumConstant(payload.intent());
            },
            buffer -> new ElarionConfigEditRequestPayload(
                    ElarionConfigEditPayloadCodecs.readTarget(buffer),
                    ElarionPacketCodecs.readString(buffer, ElarionConfigEditPayloadCodecs.MAX_VALUE),
                    ElarionPacketCodecs.readString(buffer, ElarionConfigEditPayloadCodecs.MAX_VALUE),
                    ElarionPacketCodecs.readString(buffer, ElarionConfigEditPayloadCodecs.MAX_REASON),
                    ElarionPacketCodecs.readEnumOrDefault(buffer, Intent.class, Intent.VALIDATE)));

    public ElarionConfigEditRequestPayload {
        target = Objects.requireNonNull(target, "Config edit target is required");
        expectedCurrentDisplayValue = clean(expectedCurrentDisplayValue);
        proposedRawValue = clean(proposedRawValue);
        reason = clean(reason);
        intent = intent == null ? Intent.VALIDATE : intent;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public enum Intent {
        VALIDATE,
        APPLY
    }
}
