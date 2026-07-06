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
                int count = ElarionPacketCodecs.readBoundedCount(buffer, 512);
                List<ElarionNotificationEntry> entries = new ArrayList<>();
                for (int index = 0; index < count; index++) entries.add(readEntry(buffer));
                return new NotificationSnapshotPayload(new ElarionNotificationSnapshot(entries, worldVisible));
            });

    public NotificationSnapshotPayload {
        snapshot = snapshot == null ? ElarionNotificationSnapshot.EMPTY : snapshot;
    }

    private static void writeEntry(ElarionNotificationEntry entry, PacketByteBuf buffer) {
        ElarionPacketCodecs.writeString(buffer, entry.id(), 256);
        ElarionPacketCodecs.writeString(buffer, entry.category().name().toLowerCase(java.util.Locale.ROOT), 64);
        ElarionPacketCodecs.writeString(buffer, entry.title(), 256);
        ElarionPacketCodecs.writeString(buffer, entry.body(), 1024);
        ElarionPacketCodecs.writeString(buffer, entry.status(), 256);
        ElarionPacketCodecs.writeString(buffer, entry.icon(), 256);
        buffer.writeBoolean(entry.unread());
        buffer.writeLong(entry.createdAt());
        buffer.writeVarInt(entry.actions().size());
        entry.actions().forEach(action -> {
            ElarionPacketCodecs.writeString(buffer, action.id(), 128);
            ElarionPacketCodecs.writeString(buffer, action.label(), 128);
            buffer.writeBoolean(action.enabled());
        });
        buffer.writeVarInt(entry.rewards().size());
        entry.rewards().forEach(reward -> {
            ElarionPacketCodecs.writeString(buffer, reward.label(), 256);
            ElarionPacketCodecs.writeString(buffer, reward.icon(), 256);
            buffer.writeVarInt(reward.count());
            buffer.writeVarInt(reward.tooltipLines().size());
            reward.tooltipLines().forEach(line -> ElarionPacketCodecs.writeString(buffer, line, 128));
        });
    }

    private static ElarionNotificationEntry readEntry(PacketByteBuf buffer) {
        String id = ElarionPacketCodecs.readString(buffer, 256);
        ElarionNotificationCategory category = ElarionNotificationCategory.parse(
                ElarionPacketCodecs.readString(buffer, 64));
        String title = ElarionPacketCodecs.readString(buffer, 256);
        String body = ElarionPacketCodecs.readString(buffer, 1024);
        String status = ElarionPacketCodecs.readString(buffer, 256);
        String icon = ElarionPacketCodecs.readString(buffer, 256);
        boolean unread = buffer.readBoolean();
        long createdAt = buffer.readLong();
        int actionCount = ElarionPacketCodecs.readBoundedCount(buffer, 32);
        List<ElarionNotificationAction> actions = new ArrayList<>();
        for (int index = 0; index < actionCount; index++) {
            actions.add(new ElarionNotificationAction(
                    ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 128),
                    buffer.readBoolean()));
        }
        int rewardCount = ElarionPacketCodecs.readBoundedCount(buffer, 128);
        List<ElarionNotificationRewardPreview> rewards = new ArrayList<>();
        for (int index = 0; index < rewardCount; index++) {
            rewards.add(new ElarionNotificationRewardPreview(
                    ElarionPacketCodecs.readString(buffer, 256),
                    ElarionPacketCodecs.readString(buffer, 256),
                    buffer.readVarInt(),
                    readTooltipLines(buffer)));
        }
        return new ElarionNotificationEntry(id, category, title, body, status, icon, unread, actions, rewards,
                createdAt);
    }

    private static List<String> readTooltipLines(PacketByteBuf buffer) {
        int count = ElarionPacketCodecs.readBoundedCount(buffer, 8);
        List<String> lines = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            lines.add(ElarionPacketCodecs.readString(buffer, 128));
        }
        return List.copyOf(lines);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
