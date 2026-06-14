package panetina.elarion.addons.offerings.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

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
        List<DisplayRow> historyRows,
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
                buffer.writeString(payload.instanceId());
                buffer.writeString(payload.projectId());
                buffer.writeString(payload.title());
                buffer.writeString(payload.subtitle());
                buffer.writeString(payload.description());
                buffer.writeString(payload.status());
                buffer.writeString(payload.levelText());
                buffer.writeString(payload.icon());
                buffer.writeString(payload.themeVariant());
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
                writeRows(payload.historyRows(), buffer);
                buffer.writeString(payload.rewardsPlaceholder());
                buffer.writeString(payload.historyPlaceholder());
                buffer.writeString(payload.contributionPlaceholder());
                buffer.writeString(payload.resultMessage());
                buffer.writeBoolean(payload.resultError());
                buffer.writeBoolean(payload.completed());
                buffer.writeString(payload.eventTitle());
                buffer.writeString(payload.eventBody());
                buffer.writeString(payload.eventLockedBody());
                buffer.writeBoolean(payload.eventsUnlocked());
            },
            buffer -> {
                String instanceId = buffer.readString(128);
                String projectId = buffer.readString(128);
                String title = buffer.readString(256);
                String subtitle = buffer.readString(256);
                String description = buffer.readString(4096);
                String status = buffer.readString(64);
                String level = buffer.readString(128);
                String icon = buffer.readString(256);
                String variant = buffer.readString(64);
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
                int requirementCount = buffer.readVarInt();
                List<RequirementRow> requirements = new ArrayList<>();
                for (int index = 0; index < requirementCount; index++) {
                    requirements.add(RequirementRow.read(buffer));
                }
                return new ShrineUiOpenPayload(
                        instanceId, projectId, title, subtitle, description, status, level, icon, variant,
                        width, height, scale, summaryWidth, tabHeight, rowHeight, iconSize, closeWidth,
                        current, required, requirements, readRows(buffer), readRows(buffer),
                        buffer.readString(512), buffer.readString(512), buffer.readString(512),
                        buffer.readString(512), buffer.readBoolean(), buffer.readBoolean(),
                        buffer.readString(256), buffer.readString(512), buffer.readString(512),
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
        int count = buffer.readVarInt();
        List<DisplayRow> rows = new ArrayList<>();
        for (int index = 0; index < count; index++) rows.add(DisplayRow.read(buffer));
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
            buffer.writeString(row.key());
            buffer.writeString(row.type());
            buffer.writeString(row.id());
            buffer.writeString(row.label());
            buffer.writeString(row.icon());
            buffer.writeVarLong(row.current());
            buffer.writeVarLong(row.required());
            buffer.writeBoolean(row.complete());
        }

        static RequirementRow read(PacketByteBuf buffer) {
            return new RequirementRow(
                    buffer.readString(256), buffer.readString(32), buffer.readString(256), buffer.readString(256),
                    buffer.readString(256), buffer.readVarLong(), buffer.readVarLong(), buffer.readBoolean());
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
            buffer.writeString(row.id());
            buffer.writeString(row.kind());
            buffer.writeString(row.label());
            buffer.writeString(row.body());
            buffer.writeString(row.icon());
            buffer.writeVarInt(row.count());
            buffer.writeString(row.enchantments());
            buffer.writeBoolean(row.disabled());
        }

        static DisplayRow read(PacketByteBuf buffer) {
            return new DisplayRow(buffer.readString(128), buffer.readString(32),
                    buffer.readString(256), buffer.readString(1024), buffer.readString(256),
                    buffer.readVarInt(), buffer.readString(1024), buffer.readBoolean());
        }
    }
}
