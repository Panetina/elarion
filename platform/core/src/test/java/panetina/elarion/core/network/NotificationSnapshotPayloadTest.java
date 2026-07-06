package panetina.elarion.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationEntry;
import panetina.elarion.core.model.ElarionNotificationRewardPreview;
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
                        List.of(),
                        List.of(new ElarionNotificationRewardPreview(
                                "Sword", "item:minecraft:diamond_sword", 1, List.of("Sharpness 5"))),
                        123L)), true));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        NotificationSnapshotPayload.CODEC.encode(buffer, payload);
        NotificationSnapshotPayload decoded = NotificationSnapshotPayload.CODEC.decode(buffer);

        assertTrue(decoded.snapshot().worldVisible());
        assertEquals(payload.snapshot().entries(), decoded.snapshot().entries());
        assertEquals(List.of("Sharpness 5"),
                decoded.snapshot().entries().getFirst().rewards().getFirst().tooltipLines());
    }

    @Test
    void clampsLongStringsBeforeEncoding() {
        String longText = "x".repeat(1500);
        NotificationSnapshotPayload payload = new NotificationSnapshotPayload(
                new ElarionNotificationSnapshot(List.of(new ElarionNotificationEntry(
                        longText,
                        ElarionNotificationCategory.GOVERNMENT,
                        longText,
                        longText,
                        longText,
                        longText,
                        true,
                        List.of(new ElarionNotificationAction(longText, longText, true))))));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        NotificationSnapshotPayload.CODEC.encode(buffer, payload);
        NotificationSnapshotPayload decoded = NotificationSnapshotPayload.CODEC.decode(buffer);

        ElarionNotificationEntry entry = decoded.snapshot().entries().getFirst();
        assertEquals(256, entry.id().length());
        assertEquals(256, entry.title().length());
        assertEquals(1024, entry.body().length());
        assertEquals(256, entry.status().length());
        assertEquals(256, entry.icon().length());
        assertEquals(128, entry.actions().getFirst().id().length());
        assertEquals(128, entry.actions().getFirst().label().length());
    }
}
