package panetina.elarion.addons.underworld.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraveOpenPayloadTest {
    @Test
    void codecRoundTripsTimersAccessAndSlotMetadata() {
        GraveOpenPayload payload = new GraveOpenPayload(
                "corpse-1",
                "Recover Your Grave",
                "body",
                "Panyel",
                false,
                "lootable",
                1000L,
                2000L,
                3000L,
                64,
                List.of(new GraveOpenPayload.Entry(
                        "minecraft:diamond_sword",
                        1,
                        "encoded",
                        "inventory",
                        "minecraft:inventory",
                        "Hotbar slot 1",
                        0,
                        "")));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        GraveOpenPayload.CODEC.encode(buffer, payload);
        GraveOpenPayload decoded = GraveOpenPayload.CODEC.decode(buffer);

        assertEquals(payload.corpseId(), decoded.corpseId());
        assertEquals(payload.ownerName(), decoded.ownerName());
        assertEquals(payload.accessState(), decoded.accessState());
        assertEquals(payload.protectedUntil(), decoded.protectedUntil());
        assertEquals(payload.publicLootStartedAt(), decoded.publicLootStartedAt());
        assertEquals(payload.decaysAt(), decoded.decaysAt());
        assertEquals(payload.totalItemCount(), decoded.totalItemCount());
        assertEquals(payload.items().getFirst().sourceType(), decoded.items().getFirst().sourceType());
        assertEquals(payload.items().getFirst().sourceId(), decoded.items().getFirst().sourceId());
        assertEquals(payload.items().getFirst().sourceLabel(), decoded.items().getFirst().sourceLabel());
        assertEquals(payload.items().getFirst().slotIndex(), decoded.items().getFirst().slotIndex());
        assertEquals(payload.items().getFirst().equipmentSlot(), decoded.items().getFirst().equipmentSlot());
    }

    @Test
    void clampsLongGraveUiText() {
        String longText = "x".repeat(2000);
        GraveOpenPayload payload = new GraveOpenPayload(
                longText, longText, longText, longText, false, longText,
                1000L, 2000L, 3000L, 1,
                List.of(new GraveOpenPayload.Entry(longText, 1, longText, longText, longText, longText, 0, longText)));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        GraveOpenPayload.CODEC.encode(buffer, payload);
        GraveOpenPayload decoded = GraveOpenPayload.CODEC.decode(buffer);

        assertEquals(64, decoded.corpseId().length());
        assertEquals(128, decoded.title().length());
        assertEquals(512, decoded.body().length());
        assertEquals(128, decoded.ownerName().length());
        assertEquals(256, decoded.items().getFirst().itemId().length());
        assertEquals(64, decoded.items().getFirst().sourceType().length());
    }
}
