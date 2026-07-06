package panetina.elarion.addons.offerings.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.ArrayList;
import java.util.List;

public record ShrineUiOpenPayload(
        String instanceId,
        String projectId,
        String title,
        String subtitle,
        String description,
        String status,
        String levelText,
        String icon,
        String themeVariant,
        int logicalWidth,
        int logicalHeight,
        int minimumScalePercent,
        int summaryWidth,
        int tabHeight,
        int rowHeight,
        int iconSize,
        int closeButtonWidth,
        long progressCurrent,
        long progressRequired,
        List<RequirementRow> requirementRows,
        List<DisplayRow> rewardRows,
        List<DonationRow> historyRows,
        String rewardsPlaceholder,
        String historyPlaceholder,
        String contributionPlaceholder,
        String resultMessage,
        boolean resultError,
        boolean completed,
        String eventTitle,
        String eventBody,
        String eventLockedBody,
        boolean eventsUnlocked
) implements CustomPayload {
    public static final Id<ShrineUiOpenPayload> ID =
            new Id<>(Identifier.of("elarion_offerings", "shrine_ui_open"));

    public static final PacketCodec<PacketByteBuf, ShrineUiOpenPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.instanceId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.projectId(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.title(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.subtitle(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.description(), 4096);
                ElarionPacketCodecs.writeString(buffer, payload.status(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.levelText(), 128);
                ElarionPacketCodecs.writeString(buffer, payload.icon(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.themeVariant(), 64);
                buffer.writeVarInt(payload.logicalWidth());
                buffer.writeVarInt(payload.logicalHeight());
                buffer.writeVarInt(payload.minimumScalePercent());
                buffer.writeVarInt(payload.summaryWidth());
                buffer.writeVarInt(payload.tabHeight());
                buffer.writeVarInt(payload.rowHeight());
                buffer.writeVarInt(payload.iconSize());
                buffer.writeVarInt(payload.closeButtonWidth());
                buffer.writeVarLong(payload.progressCurrent());
                buffer.writeVarLong(payload.progressRequired());
                buffer.writeVarInt(payload.requirementRows().size());
                payload.requirementRows().forEach(row -> RequirementRow.write(row, buffer));
                writeRows(payload.rewardRows(), buffer);
                writeDonationRows(payload.historyRows(), buffer);
                ElarionPacketCodecs.writeString(buffer, payload.rewardsPlaceholder(), 512);
                ElarionPacketCodecs.writeString(buffer, payload.historyPlaceholder(), 512);
                ElarionPacketCodecs.writeString(buffer, payload.contributionPlaceholder(), 512);
                ElarionPacketCodecs.writeString(buffer, payload.resultMessage(), 512);
                buffer.writeBoolean(payload.resultError());
                buffer.writeBoolean(payload.completed());
                ElarionPacketCodecs.writeString(buffer, payload.eventTitle(), 256);
                ElarionPacketCodecs.writeString(buffer, payload.eventBody(), 512);
                ElarionPacketCodecs.writeString(buffer, payload.eventLockedBody(), 512);
                buffer.writeBoolean(payload.eventsUnlocked());
            },
            buffer -> {
                String instanceId = ElarionPacketCodecs.readString(buffer, 128);
                String projectId = ElarionPacketCodecs.readString(buffer, 128);
                String title = ElarionPacketCodecs.readString(buffer, 256);
                String subtitle = ElarionPacketCodecs.readString(buffer, 256);
                String description = ElarionPacketCodecs.readString(buffer, 4096);
                String status = ElarionPacketCodecs.readString(buffer, 64);
                String level = ElarionPacketCodecs.readString(buffer, 128);
                String icon = ElarionPacketCodecs.readString(buffer, 256);
                String variant = ElarionPacketCodecs.readString(buffer, 64);
                int width = buffer.readVarInt();
                int height = buffer.readVarInt();
                int scale = buffer.readVarInt();
                int summaryWidth = buffer.readVarInt();
                int tabHeight = buffer.readVarInt();
                int rowHeight = buffer.readVarInt();
                int iconSize = buffer.readVarInt();
                int closeWidth = buffer.readVarInt();
                long current = buffer.readVarLong();
                long required = buffer.readVarLong();
                int requirementCount = ElarionPacketCodecs.readBoundedCount(buffer, 256);
                List<RequirementRow> requirements = new ArrayList<>();
                for (int index = 0; index < requirementCount; index++) {
                    requirements.add(RequirementRow.read(buffer));
                }
                return new ShrineUiOpenPayload(
                        instanceId, projectId, title, subtitle, description, status, level, icon, variant,
                        width, height, scale, summaryWidth, tabHeight, rowHeight, iconSize, closeWidth,
                        current, required, requirements, readRows(buffer), readDonationRows(buffer),
                        ElarionPacketCodecs.readString(buffer, 512), ElarionPacketCodecs.readString(buffer, 512),
                        ElarionPacketCodecs.readString(buffer, 512),
                        ElarionPacketCodecs.readString(buffer, 512), buffer.readBoolean(), buffer.readBoolean(),
                        ElarionPacketCodecs.readString(buffer, 256), ElarionPacketCodecs.readString(buffer, 512),
                        ElarionPacketCodecs.readString(buffer, 512),
                        buffer.readBoolean());
            });

    public ShrineUiOpenPayload {
        requirementRows = requirementRows == null ? List.of() : List.copyOf(requirementRows);
        rewardRows = rewardRows == null ? List.of() : List.copyOf(rewardRows);
        historyRows = historyRows == null ? List.of() : List.copyOf(historyRows);
    }

    private static void writeRows(List<DisplayRow> rows, PacketByteBuf buffer) {
        buffer.writeVarInt(rows.size());
        rows.forEach(row -> DisplayRow.write(row, buffer));
    }

    private static List<DisplayRow> readRows(PacketByteBuf buffer) {
        int count = ElarionPacketCodecs.readBoundedCount(buffer, 512);
        List<DisplayRow> rows = new ArrayList<>();
        for (int index = 0; index < count; index++) rows.add(DisplayRow.read(buffer));
        return List.copyOf(rows);
    }

    private static void writeDonationRows(List<DonationRow> rows, PacketByteBuf buffer) {
        buffer.writeVarInt(rows.size());
        rows.forEach(row -> DonationRow.write(row, buffer));
    }

    private static List<DonationRow> readDonationRows(PacketByteBuf buffer) {
        int count = ElarionPacketCodecs.readBoundedCount(buffer, 512);
        List<DonationRow> rows = new ArrayList<>();
        for (int index = 0; index < count; index++) rows.add(DonationRow.read(buffer));
        return List.copyOf(rows);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record RequirementRow(
            String key,
            String type,
            String id,
            String label,
            String icon,
            long current,
            long required,
            boolean complete
    ) {
        static void write(RequirementRow row, PacketByteBuf buffer) {
            ElarionPacketCodecs.writeString(buffer, row.key(), 256);
            ElarionPacketCodecs.writeString(buffer, row.type(), 32);
            ElarionPacketCodecs.writeString(buffer, row.id(), 256);
            ElarionPacketCodecs.writeString(buffer, row.label(), 256);
            ElarionPacketCodecs.writeString(buffer, row.icon(), 256);
            buffer.writeVarLong(row.current());
            buffer.writeVarLong(row.required());
            buffer.writeBoolean(row.complete());
        }

        static RequirementRow read(PacketByteBuf buffer) {
            return new RequirementRow(
                    ElarionPacketCodecs.readString(buffer, 256), ElarionPacketCodecs.readString(buffer, 32),
                    ElarionPacketCodecs.readString(buffer, 256), ElarionPacketCodecs.readString(buffer, 256),
                    ElarionPacketCodecs.readString(buffer, 256), buffer.readVarLong(), buffer.readVarLong(),
                    buffer.readBoolean());
        }
    }

    public record DisplayRow(
            String id,
            String kind,
            String label,
            String body,
            String icon,
            int count,
            String enchantments,
            boolean disabled
    ) {
        static void write(DisplayRow row, PacketByteBuf buffer) {
            ElarionPacketCodecs.writeString(buffer, row.id(), 128);
            ElarionPacketCodecs.writeString(buffer, row.kind(), 32);
            ElarionPacketCodecs.writeString(buffer, row.label(), 256);
            ElarionPacketCodecs.writeString(buffer, row.body(), 1024);
            ElarionPacketCodecs.writeString(buffer, row.icon(), 256);
            buffer.writeVarInt(row.count());
            ElarionPacketCodecs.writeString(buffer, row.enchantments(), 1024);
            buffer.writeBoolean(row.disabled());
        }

        static DisplayRow read(PacketByteBuf buffer) {
            return new DisplayRow(ElarionPacketCodecs.readString(buffer, 128),
                    ElarionPacketCodecs.readString(buffer, 32),
                    ElarionPacketCodecs.readString(buffer, 256),
                    ElarionPacketCodecs.readString(buffer, 1024),
                    ElarionPacketCodecs.readString(buffer, 256),
                    buffer.readVarInt(), ElarionPacketCodecs.readString(buffer, 1024), buffer.readBoolean());
        }
    }

    public record DonationRow(
            String id,
            String contributor,
            int contributorColor,
            long amount,
            String offeringLabel,
            int offeringColor,
            String timestamp
    ) {
        static void write(DonationRow row, PacketByteBuf buffer) {
            ElarionPacketCodecs.writeString(buffer, row.id(), 128);
            ElarionPacketCodecs.writeString(buffer, row.contributor(), 256);
            buffer.writeInt(row.contributorColor());
            buffer.writeVarLong(row.amount());
            ElarionPacketCodecs.writeString(buffer, row.offeringLabel(), 256);
            buffer.writeInt(row.offeringColor());
            ElarionPacketCodecs.writeString(buffer, row.timestamp(), 128);
        }

        static DonationRow read(PacketByteBuf buffer) {
            return new DonationRow(
                    ElarionPacketCodecs.readString(buffer, 128), ElarionPacketCodecs.readString(buffer, 256),
                    buffer.readInt(),
                    buffer.readVarLong(), ElarionPacketCodecs.readString(buffer, 256), buffer.readInt(),
                    ElarionPacketCodecs.readString(buffer, 128));
        }
    }
}
