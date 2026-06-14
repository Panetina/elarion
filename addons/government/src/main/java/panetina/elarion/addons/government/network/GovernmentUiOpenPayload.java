package panetina.elarion.addons.government.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record GovernmentUiOpenPayload(
        String screenType,
        String title,
        String subtitle,
        String realmId,
        String realmName,
        String themeVariant,
        int logicalWidth,
        int logicalHeight,
        int minimumScalePercent,
        boolean locked,
        boolean eligible,
        boolean voted,
        long voteEndsAt,
        String message,
        String primaryAction,
        List<Row> stageRows,
        List<Row> formRows,
        List<Row> officeRows,
        List<Row> moduleRows
) implements CustomPayload {
    public static final Id<GovernmentUiOpenPayload> ID =
            new Id<>(Identifier.of("elarion_government", "government_ui_open"));

    public static final PacketCodec<PacketByteBuf, GovernmentUiOpenPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                buffer.writeString(payload.screenType());
                buffer.writeString(payload.title());
                buffer.writeString(payload.subtitle());
                buffer.writeString(payload.realmId());
                buffer.writeString(payload.realmName());
                buffer.writeString(payload.themeVariant());
                buffer.writeVarInt(payload.logicalWidth());
                buffer.writeVarInt(payload.logicalHeight());
                buffer.writeVarInt(payload.minimumScalePercent());
                buffer.writeBoolean(payload.locked());
                buffer.writeBoolean(payload.eligible());
                buffer.writeBoolean(payload.voted());
                buffer.writeVarLong(payload.voteEndsAt());
                buffer.writeString(payload.message());
                buffer.writeString(payload.primaryAction());
                writeRows(payload.stageRows(), buffer);
                writeRows(payload.formRows(), buffer);
                writeRows(payload.officeRows(), buffer);
                writeRows(payload.moduleRows(), buffer);
            },
            buffer -> new GovernmentUiOpenPayload(
                    buffer.readString(64),
                    buffer.readString(256),
                    buffer.readString(512),
                    buffer.readString(128),
                    buffer.readString(256),
                    buffer.readString(64),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readVarLong(),
                    buffer.readString(1024),
                    buffer.readString(64),
                    readRows(buffer),
                    readRows(buffer),
                    readRows(buffer),
                    readRows(buffer)
            ));

    public GovernmentUiOpenPayload {
        message = message == null ? "" : message;
        primaryAction = primaryAction == null ? "" : primaryAction;
        stageRows = stageRows == null ? List.of() : List.copyOf(stageRows);
        formRows = formRows == null ? List.of() : List.copyOf(formRows);
        officeRows = officeRows == null ? List.of() : List.copyOf(officeRows);
        moduleRows = moduleRows == null ? List.of() : List.copyOf(moduleRows);
    }

    private static void writeRows(List<Row> rows, PacketByteBuf buffer) {
        buffer.writeVarInt(rows.size());
        rows.forEach(row -> Row.write(row, buffer));
    }

    private static List<Row> readRows(PacketByteBuf buffer) {
        int count = buffer.readVarInt();
        List<Row> rows = new ArrayList<>();
        for (int index = 0; index < count; index++) rows.add(Row.read(buffer));
        return List.copyOf(rows);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record Row(
            String id,
            String title,
            String body,
            String state,
            boolean unlocked,
            boolean complete
    ) {
        static void write(Row row, PacketByteBuf buffer) {
            buffer.writeString(row.id());
            buffer.writeString(row.title());
            buffer.writeString(row.body());
            buffer.writeString(row.state());
            buffer.writeBoolean(row.unlocked());
            buffer.writeBoolean(row.complete());
        }

        static Row read(PacketByteBuf buffer) {
            return new Row(
                    buffer.readString(128),
                    buffer.readString(256),
                    buffer.readString(1024),
                    buffer.readString(64),
                    buffer.readBoolean(),
                    buffer.readBoolean());
        }
    }
}
