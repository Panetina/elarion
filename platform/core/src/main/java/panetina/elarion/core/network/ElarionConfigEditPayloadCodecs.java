package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import panetina.elarion.core.config.ElarionConfigChangeError;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigEditControl;
import panetina.elarion.core.config.ElarionConfigEditTarget;
import panetina.elarion.core.config.ElarionConfigPermission;

import java.util.ArrayList;
import java.util.List;

final class ElarionConfigEditPayloadCodecs {
    static final int MAX_ID = 64;
    static final int MAX_PATH = 256;
    static final int MAX_LABEL = 128;
    static final int MAX_DESCRIPTION = 1024;
    static final int MAX_VALUE = 2048;
    static final int MAX_REASON = 256;
    static final int MAX_MESSAGE = 512;
    static final int MAX_AUDIT_PREVIEW = 1024;
    static final int MAX_CHOICES = 64;
    static final int MAX_ERRORS = 16;

    private ElarionConfigEditPayloadCodecs() {
    }

    static void writeTarget(PacketByteBuf buffer, ElarionConfigEditTarget target) {
        ElarionPacketCodecs.writeString(buffer, target.domainId(), MAX_ID);
        ElarionPacketCodecs.writeString(buffer, target.categoryId(), MAX_ID);
        ElarionPacketCodecs.writeString(buffer, target.entryId(), MAX_ID);
    }

    static ElarionConfigEditTarget readTarget(PacketByteBuf buffer) {
        return new ElarionConfigEditTarget(
                ElarionPacketCodecs.readString(buffer, MAX_ID),
                ElarionPacketCodecs.readString(buffer, MAX_ID),
                ElarionPacketCodecs.readString(buffer, MAX_ID));
    }

    static void writeControl(PacketByteBuf buffer, ElarionConfigEditControl control) {
        writeTarget(buffer, control.target());
        ElarionPacketCodecs.writeString(buffer, control.label(), MAX_LABEL);
        ElarionPacketCodecs.writeString(buffer, control.description(), MAX_DESCRIPTION);
        ElarionPacketCodecs.writeString(buffer, control.path(), MAX_PATH);
        buffer.writeEnumConstant(control.valueType());
        ElarionPacketCodecs.writeString(buffer, control.currentDisplayValue(), MAX_VALUE);
        ElarionPacketCodecs.writeString(buffer, control.defaultDisplayValue(), MAX_VALUE);
        writeStringList(buffer, control.choices(), MAX_CHOICES, MAX_VALUE);
        ElarionPacketCodecs.writeString(buffer, control.minimum(), MAX_VALUE);
        ElarionPacketCodecs.writeString(buffer, control.maximum(), MAX_VALUE);
        buffer.writeBoolean(control.runtimeReloadable());
        buffer.writeBoolean(control.restartRequired());
        buffer.writeEnumConstant(control.readPermission());
        buffer.writeEnumConstant(control.writePermission());
        buffer.writeBoolean(control.inputEditable());
        buffer.writeBoolean(control.applyAvailable());
        ElarionPacketCodecs.writeString(buffer, control.disabledReason(), MAX_MESSAGE);
        ElarionPacketCodecs.writeString(buffer, control.applyDisabledReason(), MAX_MESSAGE);
    }

    static ElarionConfigEditControl readControl(PacketByteBuf buffer) {
        ElarionConfigEditTarget target = readTarget(buffer);
        String label = ElarionPacketCodecs.readString(buffer, MAX_LABEL);
        String description = ElarionPacketCodecs.readString(buffer, MAX_DESCRIPTION);
        String path = ElarionPacketCodecs.readString(buffer, MAX_PATH);
        ElarionConfigCodec.ValueType valueType = ElarionPacketCodecs.readEnumOrDefault(
                buffer, ElarionConfigCodec.ValueType.class, ElarionConfigCodec.ValueType.STRING);
        String current = ElarionPacketCodecs.readString(buffer, MAX_VALUE);
        String defaultValue = ElarionPacketCodecs.readString(buffer, MAX_VALUE);
        List<String> choices = readStringList(buffer, MAX_CHOICES, MAX_VALUE);
        String minimum = ElarionPacketCodecs.readString(buffer, MAX_VALUE);
        String maximum = ElarionPacketCodecs.readString(buffer, MAX_VALUE);
        boolean runtimeReloadable = buffer.readBoolean();
        boolean restartRequired = buffer.readBoolean();
        ElarionConfigPermission readPermission = ElarionPacketCodecs.readEnumOrDefault(
                buffer, ElarionConfigPermission.class, ElarionConfigPermission.OPERATOR);
        ElarionConfigPermission writePermission = ElarionPacketCodecs.readEnumOrDefault(
                buffer, ElarionConfigPermission.class, ElarionConfigPermission.OPERATOR);
        boolean inputEditable = buffer.readBoolean();
        boolean applyAvailable = buffer.readBoolean();
        String disabledReason = ElarionPacketCodecs.readString(buffer, MAX_MESSAGE);
        String applyDisabledReason = ElarionPacketCodecs.readString(buffer, MAX_MESSAGE);
        return new ElarionConfigEditControl(target, label, description, path, valueType,
                current, defaultValue, choices, minimum, maximum, runtimeReloadable,
                restartRequired, readPermission, writePermission, inputEditable,
                applyAvailable, disabledReason, applyDisabledReason);
    }

    static void writeError(PacketByteBuf buffer, ElarionConfigChangeError error) {
        buffer.writeEnumConstant(error.code());
        ElarionPacketCodecs.writeString(buffer, error.path(), MAX_PATH);
        ElarionPacketCodecs.writeString(buffer, error.message(), MAX_MESSAGE);
    }

    static ElarionConfigChangeError readError(PacketByteBuf buffer) {
        ElarionConfigChangeError.Code code = ElarionPacketCodecs.readEnumOrDefault(
                buffer, ElarionConfigChangeError.Code.class, ElarionConfigChangeError.Code.INTERNAL_ERROR);
        String path = ElarionPacketCodecs.readString(buffer, MAX_PATH);
        String message = ElarionPacketCodecs.readString(buffer, MAX_MESSAGE);
        return ElarionConfigChangeError.of(code, path, message);
    }

    static void writeStringList(PacketByteBuf buffer, List<String> values, int maxCount, int maxLength) {
        List<String> safe = values == null ? List.of() : values;
        int count = Math.min(safe.size(), maxCount);
        buffer.writeVarInt(count);
        for (int index = 0; index < count; index++) {
            ElarionPacketCodecs.writeString(buffer, safe.get(index), maxLength);
        }
    }

    static List<String> readStringList(PacketByteBuf buffer, int maxCount, int maxLength) {
        int count = ElarionPacketCodecs.readBoundedCount(buffer, maxCount);
        List<String> values = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            values.add(ElarionPacketCodecs.readString(buffer, maxLength));
        }
        return values;
    }
}
