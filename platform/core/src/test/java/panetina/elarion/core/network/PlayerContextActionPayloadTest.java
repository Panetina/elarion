package panetina.elarion.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlayerContextActionPayloadTest {
    @Test void snapshotRoundTripsServerAuthoredEntries() {
        PlayerContextActionSnapshotPayload payload = new PlayerContextActionSnapshotPayload(
                UUID.randomUUID(), "Panyel", List.of(new PlayerContextActionSnapshotPayload.Entry(
                        "elarion_guilds:invite", "Invite to Guild")));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        PlayerContextActionSnapshotPayload.CODEC.encode(buffer, payload);
        assertEquals(payload, PlayerContextActionSnapshotPayload.CODEC.decode(buffer));
    }

    @Test void executeRoundTripsTargetAndActionId() {
        PlayerContextActionExecutePayload payload = new PlayerContextActionExecutePayload(
                UUID.randomUUID(), "elarion_guilds:invite");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        PlayerContextActionExecutePayload.CODEC.encode(buffer, payload);
        assertEquals(payload, PlayerContextActionExecutePayload.CODEC.decode(buffer));
    }
}
