package panetina.elarion.addons.npcs.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import panetina.elarion.core.network.ElarionPacketCodecs;

public record NpcTradeOfferPayload(
        String id,
        String direction,
        String label,
        String subtitle,
        long price,
        int quantity,
        int maxQuantity,
        long subtotal,
        int taxBasisPoints,
        long tax,
        long total,
        long policyRevision,
        String taxAuthorityLabel,
        boolean enabled,
        String disabledReason,
        int stockRemaining,
        ItemStack preview
) {
    public static void write(NpcTradeOfferPayload offer, RegistryByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, offer.id(), 128);
        ElarionPacketCodecs.writeString(buffer, offer.direction(), 16);
        ElarionPacketCodecs.writeString(buffer, offer.label(), 128);
        ElarionPacketCodecs.writeString(buffer, offer.subtitle(), 128);
        buffer.writeVarLong(Math.max(0L, offer.price()));
        buffer.writeVarInt(offer.quantity());
        buffer.writeVarInt(offer.maxQuantity());
        buffer.writeVarLong(Math.max(0L, offer.subtotal()));
        buffer.writeVarInt(offer.taxBasisPoints());
        buffer.writeVarLong(Math.max(0L, offer.tax()));
        buffer.writeVarLong(Math.max(0L, offer.total()));
        buffer.writeVarLong(Math.max(0L, offer.policyRevision()));
        ElarionPacketCodecs.writeString(buffer, offer.taxAuthorityLabel(), 64);
        buffer.writeBoolean(offer.enabled());
        ElarionPacketCodecs.writeString(buffer, offer.disabledReason(), 256);
        buffer.writeVarInt(offer.stockRemaining());
        ItemStack.PACKET_CODEC.encode(buffer, offer.preview());
    }

    public static NpcTradeOfferPayload read(RegistryByteBuf buffer) {
        return new NpcTradeOfferPayload(
                ElarionPacketCodecs.readString(buffer, 128),
                ElarionPacketCodecs.readString(buffer, 16),
                ElarionPacketCodecs.readString(buffer, 128),
                ElarionPacketCodecs.readString(buffer, 128),
                Math.max(0L, buffer.readVarLong()),
                buffer.readVarInt(),
                buffer.readVarInt(),
                Math.max(0L, buffer.readVarLong()),
                buffer.readVarInt(),
                Math.max(0L, buffer.readVarLong()),
                Math.max(0L, buffer.readVarLong()),
                Math.max(0L, buffer.readVarLong()),
                ElarionPacketCodecs.readString(buffer, 64),
                buffer.readBoolean(),
                ElarionPacketCodecs.readString(buffer, 256),
                buffer.readVarInt(),
                ItemStack.PACKET_CODEC.decode(buffer));
    }

    public NpcTradeOfferPayload {
        id = id == null ? "" : id;
        direction = direction == null ? "buy" : direction;
        label = label == null ? "" : label;
        subtitle = subtitle == null ? "" : subtitle;
        quantity = Math.max(1, quantity);
        maxQuantity = Math.max(1, Math.min(64, maxQuantity));
        taxBasisPoints = Math.max(0, Math.min(10_000, taxBasisPoints));
        taxAuthorityLabel = taxAuthorityLabel == null ? "" : taxAuthorityLabel;
        disabledReason = disabledReason == null ? "" : disabledReason;
        stockRemaining = stockRemaining < 0 ? -1 : stockRemaining;
        preview = preview == null ? ItemStack.EMPTY : preview.copy();
    }
}
