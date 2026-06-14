package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.ElarionUiTheme;
import panetina.elarion.core.model.ElarionUiThemeVariant;

import java.util.LinkedHashMap;
import java.util.Map;

public record UiThemeSyncPayload(ElarionUiTheme theme) implements CustomPayload {
    public static final Id<UiThemeSyncPayload> ID =
            new Id<>(Identifier.of("elarion_core", "ui_theme_sync"));
    public static final PacketCodec<PacketByteBuf, UiThemeSyncPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> writeTheme(payload.theme(), buffer),
            buffer -> new UiThemeSyncPayload(readTheme(buffer)));

    private static void writeTheme(ElarionUiTheme theme, PacketByteBuf buffer) {
        buffer.writeVarInt(theme.logicalWidth());
        buffer.writeVarInt(theme.logicalHeight());
        buffer.writeVarInt(theme.minimumScalePercent());
        buffer.writeVarInt(theme.padding());
        buffer.writeVarInt(theme.gap());
        buffer.writeVarInt(theme.rowHeight());
        buffer.writeVarInt(theme.buttonHeight());
        buffer.writeVarInt(theme.scrollbarWidth());
        buffer.writeVarInt(theme.variants().size());
        theme.variants().values().forEach(variant -> writeVariant(variant, buffer));
    }

    private static ElarionUiTheme readTheme(PacketByteBuf buffer) {
        int width = buffer.readVarInt();
        int height = buffer.readVarInt();
        int scale = buffer.readVarInt();
        int padding = buffer.readVarInt();
        int gap = buffer.readVarInt();
        int row = buffer.readVarInt();
        int button = buffer.readVarInt();
        int scrollbar = buffer.readVarInt();
        int count = buffer.readVarInt();
        Map<String, ElarionUiThemeVariant> variants = new LinkedHashMap<>();
        for (int index = 0; index < count; index++) {
            ElarionUiThemeVariant variant = readVariant(buffer);
            variants.put(variant.id(), variant);
        }
        return new ElarionUiTheme(width, height, scale, padding, gap, row, button, scrollbar, variants);
    }

    private static void writeVariant(ElarionUiThemeVariant value, PacketByteBuf buffer) {
        buffer.writeString(value.id());
        buffer.writeInt(value.panelColor());
        buffer.writeInt(value.headerColor());
        buffer.writeInt(value.insetColor());
        buffer.writeInt(value.borderColor());
        buffer.writeInt(value.bevelHighlightColor());
        buffer.writeInt(value.bevelShadowColor());
        buffer.writeInt(value.backgroundOverlayColor());
        buffer.writeInt(value.titleColor());
        buffer.writeInt(value.textColor());
        buffer.writeInt(value.mutedColor());
        buffer.writeInt(value.successColor());
        buffer.writeInt(value.warningColor());
        buffer.writeInt(value.errorColor());
        buffer.writeInt(value.disabledColor());
        buffer.writeInt(value.buttonColor());
        buffer.writeInt(value.buttonHoverColor());
        buffer.writeInt(value.cardColor());
        buffer.writeInt(value.progressBackgroundColor());
        buffer.writeInt(value.progressFillColor());
        buffer.writeInt(value.progressCompleteColor());
        buffer.writeInt(value.scrollbarTrackColor());
        buffer.writeInt(value.scrollbarThumbColor());
        buffer.writeString(value.panelTexture());
        buffer.writeString(value.cardTexture());
        buffer.writeString(value.textureMode());
        buffer.writeInt(value.textureTint());
    }

    private static ElarionUiThemeVariant readVariant(PacketByteBuf buffer) {
        return new ElarionUiThemeVariant(
                buffer.readString(64),
                buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readInt(), buffer.readInt(), buffer.readInt(),
                buffer.readString(256), buffer.readString(256), buffer.readString(16), buffer.readInt());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
