package panetina.elarion.addons.offerings.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ShrineUiOpenPayloadTest {
    @Test
    void roundTripsTypedShrineSnapshot() {
        ShrineUiOpenPayload.RequirementRow requirement = new ShrineUiOpenPayload.RequirementRow(
                "item:minecraft:stone", "items", "minecraft:stone",
                "Stone", "item:minecraft:stone", 12, 64, false);
        ShrineUiOpenPayload.DisplayRow reward = new ShrineUiOpenPayload.DisplayRow(
                "rank", "event", "Foundation Rank", "Unlocked at completion", "", 1, "", true);
        ShrineUiOpenPayload.DonationRow history = new ShrineUiOpenPayload.DonationRow(
                "recent", "Citizen", 0xFF55FF55, 12, "Stone", 0xFFAAAAAA, "2026-06-14T00:00:00Z");
        ShrineUiOpenPayload payload = new ShrineUiOpenPayload(
                "offering_realm_oak_1", "oak_foundation", "Shrine of Foundation",
                "Realm of Oak", "A shared foundation project.", "Active", "Foundation I",
                "minecraft:textures/item/amethyst_shard.png", "shrine",
                640, 420, 60, 150, 22, 24, 48, 100,
                12, 64, List.of(requirement), List.of(reward), List.of(history),
                "No rewards.", "No history.", "Direct offerings are locked.",
                "", false, false,
                "Upcoming Event", "No shrine event active.", "Events are locked.", false);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        ShrineUiOpenPayload.CODEC.encode(buffer, payload);
        ShrineUiOpenPayload decoded = ShrineUiOpenPayload.CODEC.decode(buffer);

        assertEquals(payload, decoded);
    }
}
