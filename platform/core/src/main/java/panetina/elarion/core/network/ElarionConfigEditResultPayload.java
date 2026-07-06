package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.config.ElarionConfigChangeError;
import panetina.elarion.core.config.ElarionConfigChangeResult;
import panetina.elarion.core.config.ElarionConfigEditTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ElarionConfigEditResultPayload(
        ElarionConfigEditTarget target,
        ElarionConfigChangeResult.Status status,
        String oldDisplayValue,
        String newDisplayValue,
        boolean reloadRequired,
        boolean restartRequired,
        boolean canApply,
        String auditPreview,
        List<ElarionConfigChangeError> errors,
        String message
) implements CustomPayload {
    public static final Id<ElarionConfigEditResultPayload> ID =
            new Id<>(Identifier.of("elarion_core", "config_edit_result"));
    public static final PacketCodec<PacketByteBuf, ElarionConfigEditResultPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionConfigEditPayloadCodecs.writeTarget(buffer, payload.target());
                buffer.writeEnumConstant(payload.status());
                ElarionPacketCodecs.writeString(buffer, payload.oldDisplayValue(),
                        ElarionConfigEditPayloadCodecs.MAX_VALUE);
                ElarionPacketCodecs.writeString(buffer, payload.newDisplayValue(),
                        ElarionConfigEditPayloadCodecs.MAX_VALUE);
                buffer.writeBoolean(payload.reloadRequired());
                buffer.writeBoolean(payload.restartRequired());
                buffer.writeBoolean(payload.canApply());
                ElarionPacketCodecs.writeString(buffer, payload.auditPreview(),
                        ElarionConfigEditPayloadCodecs.MAX_AUDIT_PREVIEW);
                int count = Math.min(payload.errors().size(), ElarionConfigEditPayloadCodecs.MAX_ERRORS);
                buffer.writeVarInt(count);
                for (int index = 0; index < count; index++) {
                    ElarionConfigEditPayloadCodecs.writeError(buffer, payload.errors().get(index));
                }
                ElarionPacketCodecs.writeString(buffer, payload.message(),
                        ElarionConfigEditPayloadCodecs.MAX_MESSAGE);
            },
            buffer -> {
                ElarionConfigEditTarget target = ElarionConfigEditPayloadCodecs.readTarget(buffer);
                ElarionConfigChangeResult.Status status = ElarionPacketCodecs.readEnumOrDefault(
                        buffer, ElarionConfigChangeResult.Status.class, ElarionConfigChangeResult.Status.REJECTED);
                String oldValue = ElarionPacketCodecs.readString(buffer, ElarionConfigEditPayloadCodecs.MAX_VALUE);
                String newValue = ElarionPacketCodecs.readString(buffer, ElarionConfigEditPayloadCodecs.MAX_VALUE);
                boolean reloadRequired = buffer.readBoolean();
                boolean restartRequired = buffer.readBoolean();
                boolean canApply = buffer.readBoolean();
                String auditPreview = ElarionPacketCodecs.readString(
                        buffer, ElarionConfigEditPayloadCodecs.MAX_AUDIT_PREVIEW);
                int errorCount = ElarionPacketCodecs.readBoundedCount(buffer, ElarionConfigEditPayloadCodecs.MAX_ERRORS);
                List<ElarionConfigChangeError> errors = new ArrayList<>(errorCount);
                for (int index = 0; index < errorCount; index++) {
                    errors.add(ElarionConfigEditPayloadCodecs.readError(buffer));
                }
                String message = ElarionPacketCodecs.readString(buffer, ElarionConfigEditPayloadCodecs.MAX_MESSAGE);
                return new ElarionConfigEditResultPayload(target, status, oldValue, newValue,
                        reloadRequired, restartRequired, canApply, auditPreview, errors, message);
            });

    public ElarionConfigEditResultPayload {
        target = Objects.requireNonNull(target, "Config edit target is required");
        status = status == null ? ElarionConfigChangeResult.Status.REJECTED : status;
        oldDisplayValue = clean(oldDisplayValue);
        newDisplayValue = clean(newDisplayValue);
        auditPreview = clean(auditPreview);
        if (errors != null && errors.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Config edit result errors must not contain null");
        }
        errors = errors == null ? List.of() : List.copyOf(errors);
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
