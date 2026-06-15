package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.model.ElarionNotificationEntry;
import panetina.elarion.core.model.ElarionNotificationRewardPreview;
import panetina.elarion.core.model.ElarionNotificationSnapshot;

import java.util.ArrayList;
import java.util.List;

public record NotificationSnapshotPayload(ElarionNotificationSnapshot snapshot) implements CustomPayload {
    public static final Id<NotificationSnapshotPayload> ID =
            new Id<>(Identifier.of("elarion_core", "notification_snapshot"));

    public static final PacketCodec<PacketByteBuf, NotificationSnapshotPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                List<ElarionNotificationEntry> entries = payload.snapshot().entries();
                buffer.writeBoolean(payload.snapshot().worldVisible());
                buffer.writeVarInt(entries.size());
                entries.forEach(entry -> writeEntry(entry, buffer));
            },
            buffer -> {
                boolean worldVisible = buffer.readBoolean();
                int count = buffer.readVarInt();
                List<ElarionNotificationEntry> entries = new ArrayList<>();
                for (int index = 0; index < count; index++) entries.add(readEntry(buffer));
                return new NotificationSnapshotPayload(new ElarionNotificationSnapshot(entries, worldVisible));
            });

    public NotificationSnapshotPayload {
        snapshot = snapshot == null ? ElarionNotificationSnapshot.EMPTY : snapshot;
    }

    private static void writeEntry(ElarionNotificationEntry entry, PacketByteBuf buffer) {
        buffer.writeString(entry.id());
        buffer.writeString(entry.category().name().toLowerCase(java.util.Locale.ROOT));
        buffer.writeString(entry.title());
        buffer.writeString(entry.body());
        buffer.writeString(entry.status());
        buffer.writeString(entry.icon());
        buffer.writeBoolean(entry.unread());
        buffer.writeLong(entry.createdAt());
        buffer.writeVarInt(entry.actions().size());
        entry.actions().forEach(action -> {
            buffer.writeString(action.id());
            buffer.writeString(action.label());
            buffer.writeBoolean(action.enabled());
        });
        buffer.writeVarInt(entry.rewards().size());
        entry.rewards().forEach(reward -> {
            buffer.writeString(reward.label());
            buffer.writeString(reward.icon());
            buffer.writeVarInt(reward.count());
        });
    }

    private static ElarionNotificationEntry readEntry(PacketByteBuf buffer) {
        String id = buffer.readString(256);
        ElarionNotificationCategory category = ElarionNotificationCategory.parse(buffer.readString(64));
        String title = buffer.readString(256);
        String body = buffer.readString(1024);
        String status = buffer.readString(256);
        String icon = buffer.readString(256);
        boolean unread = buffer.readBoolean();
        long createdAt = buffer.readLong();
        int actionCount = buffer.readVarInt();
        List<ElarionNotificationAction> actions = new ArrayList<>();
        for (int index = 0; index < actionCount; index++) {
            actions.add(new ElarionNotificationAction(
                    buffer.readString(128),
                    buffer.readString(128),
                    buffer.readBoolean()));
        }
        int rewardCount = buffer.readVarInt();
        List<ElarionNotificationRewardPreview> rewards = new ArrayList<>();
        for (int index = 0; index < rewardCount; index++) {
            rewards.add(new ElarionNotificationRewardPreview(
                    buffer.readString(256),
                    buffer.readString(256),
                    buffer.readVarInt()));
        }
        return new ElarionNotificationEntry(id, category, title, body, status, icon, unread, actions, rewards,
                createdAt);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
