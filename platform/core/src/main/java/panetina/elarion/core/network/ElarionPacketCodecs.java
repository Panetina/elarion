package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;

public final class ElarionPacketCodecs {
    private ElarionPacketCodecs() {
    }

    public static void writeString(PacketByteBuf buffer, String value, int maxLength) {
        String clean = value == null ? "" : stripUnsafe(value);
        if (clean.length() > maxLength) clean = clean.substring(0, maxLength);
        buffer.writeString(clean, maxLength);
    }

    public static String readString(PacketByteBuf buffer, int maxLength) {
        return stripUnsafe(buffer.readString(maxLength));
    }

    public static int readBoundedCount(PacketByteBuf buffer, int maxCount) {
        return Math.max(0, Math.min(maxCount, buffer.readVarInt()));
    }

    public static <E extends Enum<E>> E readEnumOrDefault(PacketByteBuf buffer, Class<E> type, E fallback) {
        try {
            return buffer.readEnumConstant(type);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static String stripUnsafe(String value) {
        if (value == null || value.isEmpty()) return "";
        StringBuilder builder = new StringBuilder(value.length());
        value.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .filter(codePoint -> Character.getType(codePoint) != Character.FORMAT)
                .forEach(builder::appendCodePoint);
        return builder.toString().trim();
    }
}
