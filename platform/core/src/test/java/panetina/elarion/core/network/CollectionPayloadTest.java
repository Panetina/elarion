package panetina.elarion.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ElarionCollectionAction;
import panetina.elarion.core.model.ElarionCollectionEntry;
import panetina.elarion.core.model.ElarionCollectionSnapshot;
import panetina.elarion.core.model.ElarionCollectionTab;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CollectionPayloadTest {
    @Test
    void openPayloadRoundTripsTabsEntriesAndActions() {
        ElarionCollectionSnapshot snapshot = new ElarionCollectionSnapshot(
                "Collection",
                "Mounts and pets",
                "mounts",
                "Wyvern is now active.",
                List.of(new ElarionCollectionTab(
                        "mounts",
                        "Mounts",
                        "Choose the mount summoned by R.",
                        List.of(new ElarionCollectionEntry(
                                "wyvern",
                                "Wyvern",
                                "Future collection reward",
                                "Unlock through future progression.",
                                "Active",
                                "elarion_mounts:textures/gui/collection/wyvern.png",
                                true,
                                true,
                                List.of(new ElarionCollectionAction("set_active", "Active", false)))))));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        CollectionOpenPayload.CODEC.encode(buffer, new CollectionOpenPayload(snapshot));
        CollectionOpenPayload decoded = CollectionOpenPayload.CODEC.decode(buffer);

        assertEquals(snapshot, decoded.snapshot());
        assertEquals("set_active", decoded.snapshot().tabs().getFirst().entries().getFirst().actions().getFirst().id());
    }

    @Test
    void actionPayloadRoundTripsSelection() {
        CollectionActionPayload payload = new CollectionActionPayload("mounts", "bee", "set_active");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        CollectionActionPayload.CODEC.encode(buffer, payload);
        CollectionActionPayload decoded = CollectionActionPayload.CODEC.decode(buffer);

        assertEquals(payload, decoded);
    }
}
