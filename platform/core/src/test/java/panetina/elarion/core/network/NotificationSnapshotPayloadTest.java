package panetina.elarion.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.ElarionNotificationEntry;
import panetina.elarion.core.model.ElarionNotificationSnapshot;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NotificationSnapshotPayloadTest {
    @Test
    void roundTripsWorldVisibility() {
        NotificationSnapshotPayload payload = new NotificationSnapshotPayload(
                new ElarionNotificationSnapshot(List.of(new ElarionNotificationEntry(
                        "world-1",
                        ElarionNotificationCategory.WORLD,
                        "World Event",
                        "Body",
                        "",
                        "item:minecraft:ender_eye",
                        true,
                        List.of())), true));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        NotificationSnapshotPayload.CODEC.encode(buffer, payload);
        NotificationSnapshotPayload decoded = NotificationSnapshotPayload.CODEC.decode(buffer);

        assertTrue(decoded.snapshot().worldVisible());
        assertEquals(payload.snapshot().entries(), decoded.snapshot().entries());
    }
}
