package panetina.elarion.addons.government.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

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
        String sessionId,
        String pageId,
        String parentPageId,
        String homePageId,
        String navigationTitle,
        boolean showClose,
        boolean showBack,
        String screenFamily,
        String activeTabId,
        String governmentFormLabel,
        String authorityLabel,
        String roleLabel,
        String realmColor,
        String crestIconId,
        String selectedRowId,
        List<Row> stageRows,
        List<Row> formRows,
        List<Row> officeRows,
        List<Row> moduleRows
) implements CustomPayload {
    private static final int SCREEN_TYPE_MAX = 64;
    private static final int TITLE_MAX = 256;
    private static final int SUBTITLE_MAX = 512;
    private static final int REALM_ID_MAX = 128;
    private static final int REALM_NAME_MAX = 256;
    private static final int THEME_MAX = 64;
    private static final int MESSAGE_MAX = 1024;
    private static final int ACTION_MAX = 64;
    private static final int SESSION_MAX = 64;
    private static final int PAGE_MAX = 128;
    private static final int META_MAX = 128;

    public static final Id<GovernmentUiOpenPayload> ID =
            new Id<>(Identifier.of("elarion_government", "government_ui_open"));

    public static final PacketCodec<PacketByteBuf, GovernmentUiOpenPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.screenType(), SCREEN_TYPE_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.title(), TITLE_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.subtitle(), SUBTITLE_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.realmId(), REALM_ID_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.realmName(), REALM_NAME_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.themeVariant(), THEME_MAX);
                buffer.writeVarInt(payload.logicalWidth());
                buffer.writeVarInt(payload.logicalHeight());
                buffer.writeVarInt(payload.minimumScalePercent());
                buffer.writeBoolean(payload.locked());
                buffer.writeBoolean(payload.eligible());
                buffer.writeBoolean(payload.voted());
                buffer.writeVarLong(payload.voteEndsAt());
                ElarionPacketCodecs.writeString(buffer, payload.message(), MESSAGE_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.primaryAction(), ACTION_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.sessionId(), SESSION_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.pageId(), PAGE_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.parentPageId(), PAGE_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.homePageId(), PAGE_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.navigationTitle(), PAGE_MAX);
                buffer.writeBoolean(payload.showClose());
                buffer.writeBoolean(payload.showBack());
                ElarionPacketCodecs.writeString(buffer, payload.screenFamily(), META_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.activeTabId(), META_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.governmentFormLabel(), META_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.authorityLabel(), META_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.roleLabel(), META_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.realmColor(), META_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.crestIconId(), META_MAX);
                ElarionPacketCodecs.writeString(buffer, payload.selectedRowId(), META_MAX);
                writeRows(payload.stageRows(), buffer);
                writeRows(payload.formRows(), buffer);
                writeRows(payload.officeRows(), buffer);
                writeRows(payload.moduleRows(), buffer);
            },
            buffer -> new GovernmentUiOpenPayload(
                    ElarionPacketCodecs.readString(buffer, SCREEN_TYPE_MAX),
                    ElarionPacketCodecs.readString(buffer, TITLE_MAX),
                    ElarionPacketCodecs.readString(buffer, SUBTITLE_MAX),
                    ElarionPacketCodecs.readString(buffer, REALM_ID_MAX),
                    ElarionPacketCodecs.readString(buffer, REALM_NAME_MAX),
                    ElarionPacketCodecs.readString(buffer, THEME_MAX),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readVarLong(),
                    ElarionPacketCodecs.readString(buffer, MESSAGE_MAX),
                    ElarionPacketCodecs.readString(buffer, ACTION_MAX),
                    ElarionPacketCodecs.readString(buffer, SESSION_MAX),
                    ElarionPacketCodecs.readString(buffer, PAGE_MAX),
                    ElarionPacketCodecs.readString(buffer, PAGE_MAX),
                    ElarionPacketCodecs.readString(buffer, PAGE_MAX),
                    ElarionPacketCodecs.readString(buffer, PAGE_MAX),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    ElarionPacketCodecs.readString(buffer, META_MAX),
                    ElarionPacketCodecs.readString(buffer, META_MAX),
                    ElarionPacketCodecs.readString(buffer, META_MAX),
                    ElarionPacketCodecs.readString(buffer, META_MAX),
                    ElarionPacketCodecs.readString(buffer, META_MAX),
                    ElarionPacketCodecs.readString(buffer, META_MAX),
                    ElarionPacketCodecs.readString(buffer, META_MAX),
                    ElarionPacketCodecs.readString(buffer, META_MAX),
                    readRows(buffer),
                    readRows(buffer),
                    readRows(buffer),
                    readRows(buffer)
            ));

    public GovernmentUiOpenPayload(
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
            String sessionId,
            String pageId,
            String parentPageId,
            String homePageId,
            String navigationTitle,
            boolean showClose,
            boolean showBack,
            List<Row> stageRows,
            List<Row> formRows,
            List<Row> officeRows,
            List<Row> moduleRows
    ) {
        this(screenType, title, subtitle, realmId, realmName, themeVariant, logicalWidth, logicalHeight,
                minimumScalePercent, locked, eligible, voted, voteEndsAt, message, primaryAction, sessionId,
                pageId, parentPageId, homePageId, navigationTitle, showClose, showBack,
                defaultScreenFamily(screenType), defaultActiveTab(screenType), "", "", defaultRoleLabel(screenType),
                "", defaultCrestIcon(screenType), "", stageRows, formRows, officeRows, moduleRows);
    }

    public GovernmentUiOpenPayload {
        screenType = clean(screenType, SCREEN_TYPE_MAX);
        title = clean(title, TITLE_MAX);
        subtitle = clean(subtitle, SUBTITLE_MAX);
        realmId = clean(realmId, REALM_ID_MAX);
        realmName = clean(realmName, REALM_NAME_MAX);
        themeVariant = clean(themeVariant, THEME_MAX);
        message = clean(message, MESSAGE_MAX);
        primaryAction = clean(primaryAction, ACTION_MAX);
        sessionId = clean(sessionId, SESSION_MAX);
        pageId = clean(pageId, PAGE_MAX);
        parentPageId = clean(parentPageId, PAGE_MAX);
        homePageId = clean(homePageId, PAGE_MAX);
        navigationTitle = clean(navigationTitle, PAGE_MAX);
        screenFamily = clean(screenFamily, META_MAX);
        if (screenFamily.isBlank()) screenFamily = defaultScreenFamily(screenType);
        activeTabId = clean(activeTabId, META_MAX);
        if (activeTabId.isBlank()) activeTabId = defaultActiveTab(screenType);
        governmentFormLabel = clean(governmentFormLabel, META_MAX);
        authorityLabel = clean(authorityLabel, META_MAX);
        if (authorityLabel.isBlank()) {
            authorityLabel = governmentFormLabel.isBlank() ? defaultAuthorityLabel(screenType) : governmentFormLabel;
        }
        roleLabel = clean(roleLabel, META_MAX);
        if (roleLabel.isBlank()) roleLabel = defaultRoleLabel(screenType);
        realmColor = clean(realmColor, META_MAX);
        crestIconId = clean(crestIconId, META_MAX);
        if (crestIconId.isBlank()) crestIconId = defaultCrestIcon(screenType);
        selectedRowId = clean(selectedRowId, META_MAX);
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
        int count = ElarionPacketCodecs.readBoundedCount(buffer, 512);
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
            boolean complete,
            boolean selectedByViewer,
            long voteCount,
            String kind,
            String iconId,
            String category,
            String actorName,
            String metricLabel,
            long approveCount,
            long rejectCount,
            long threshold,
            long createdAt
    ) {
        private static final int ID_MAX = 128;
        private static final int TITLE_MAX = 256;
        private static final int BODY_MAX = 1024;
        private static final int STATE_MAX = 128;
        private static final int KIND_MAX = 32;
        private static final int ROW_META_MAX = 128;

        public Row {
            id = clean(id, ID_MAX);
            title = clean(title, TITLE_MAX);
            body = clean(body, BODY_MAX);
            state = clean(state, STATE_MAX);
            kind = kind == null || kind.isBlank() ? "static" : kind;
            kind = clean(kind, KIND_MAX);
            voteCount = Math.max(0L, voteCount);
            iconId = clean(iconId, ROW_META_MAX);
            if (iconId.isBlank()) iconId = defaultRowIcon(id, title, kind);
            category = clean(category, ROW_META_MAX);
            actorName = clean(actorName, ROW_META_MAX);
            metricLabel = clean(metricLabel, ROW_META_MAX);
            approveCount = Math.max(0L, approveCount);
            rejectCount = Math.max(0L, rejectCount);
            threshold = Math.max(0L, threshold);
            createdAt = Math.max(0L, createdAt);
        }

        public Row(
                String id,
                String title,
                String body,
                String state,
                boolean unlocked,
                boolean complete,
                boolean selectedByViewer,
                long voteCount,
                String kind
        ) {
            this(id, title, body, state, unlocked, complete, selectedByViewer, voteCount, kind,
                    "", "", "", "", 0L, 0L, 0L, 0L);
        }

        public Row(
                String id,
                String title,
                String body,
                String state,
                boolean unlocked,
                boolean complete,
                String kind
        ) {
            this(id, title, body, state, unlocked, complete, false, 0L, kind);
        }

        static void write(Row row, PacketByteBuf buffer) {
            ElarionPacketCodecs.writeString(buffer, row.id(), ID_MAX);
            ElarionPacketCodecs.writeString(buffer, row.title(), TITLE_MAX);
            ElarionPacketCodecs.writeString(buffer, row.body(), BODY_MAX);
            ElarionPacketCodecs.writeString(buffer, row.state(), STATE_MAX);
            buffer.writeBoolean(row.unlocked());
            buffer.writeBoolean(row.complete());
            buffer.writeBoolean(row.selectedByViewer());
            buffer.writeVarLong(row.voteCount());
            ElarionPacketCodecs.writeString(buffer, row.kind(), KIND_MAX);
            ElarionPacketCodecs.writeString(buffer, row.iconId(), ROW_META_MAX);
            ElarionPacketCodecs.writeString(buffer, row.category(), ROW_META_MAX);
            ElarionPacketCodecs.writeString(buffer, row.actorName(), ROW_META_MAX);
            ElarionPacketCodecs.writeString(buffer, row.metricLabel(), ROW_META_MAX);
            buffer.writeVarLong(row.approveCount());
            buffer.writeVarLong(row.rejectCount());
            buffer.writeVarLong(row.threshold());
            buffer.writeVarLong(row.createdAt());
        }

        static Row read(PacketByteBuf buffer) {
            return new Row(
                    ElarionPacketCodecs.readString(buffer, ID_MAX),
                    ElarionPacketCodecs.readString(buffer, TITLE_MAX),
                    ElarionPacketCodecs.readString(buffer, BODY_MAX),
                    ElarionPacketCodecs.readString(buffer, STATE_MAX),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readVarLong(),
                    ElarionPacketCodecs.readString(buffer, KIND_MAX),
                    ElarionPacketCodecs.readString(buffer, ROW_META_MAX),
                    ElarionPacketCodecs.readString(buffer, ROW_META_MAX),
                    ElarionPacketCodecs.readString(buffer, ROW_META_MAX),
                    ElarionPacketCodecs.readString(buffer, ROW_META_MAX),
                    Math.max(0L, buffer.readVarLong()),
                    Math.max(0L, buffer.readVarLong()),
                    Math.max(0L, buffer.readVarLong()),
                    Math.max(0L, buffer.readVarLong()));
        }
    }

    private static String defaultScreenFamily(String screenType) {
        String safe = screenType == null ? "" : screenType;
        return "seat_of_rule".equals(safe) || safe.startsWith("seat_module_") ? "seat_of_rule" : "civic_forum";
    }

    private static String defaultActiveTab(String screenType) {
        String safe = screenType == null ? "" : screenType;
        if (safe.startsWith("seat_module_")) return safe.substring("seat_module_".length());
        if ("seat_of_rule".equals(safe)) return "review";
        if (safe.startsWith("civic_module_")) return safe.substring("civic_module_".length());
        return "current_votes";
    }

    private static String defaultRoleLabel(String screenType) {
        return "seat_of_rule".equals(defaultScreenFamily(screenType)) ? "Authority Seat" : "Ember Assembly";
    }

    private static String defaultAuthorityLabel(String screenType) {
        return "seat_of_rule".equals(defaultScreenFamily(screenType)) ? "Authority" : "Unchosen";
    }

    private static String defaultCrestIcon(String screenType) {
        return "seat_of_rule".equals(defaultScreenFamily(screenType)) ? "seat_crest" : "civic_crest";
    }

    private static String defaultRowIcon(String id, String title, String kind) {
        String value = ((id == null ? "" : id) + " " + (title == null ? "" : title) + " " + (kind == null ? "" : kind))
                .toLowerCase();
        if (value.contains("color")) return "realm_color";
        if (value.contains("form") || value.contains("republic") || value.contains("monarchy")) return "government_form";
        if (value.contains("leader") || value.contains("election") || value.contains("president")
                || value.contains("monarch") || value.contains("council")) {
            return "leader_election";
        }
        if (value.contains("law")) return "law";
        if (value.contains("office") || value.contains("heir")) return "office";
        if (value.contains("archive")) return "archive";
        if (value.contains("name")) return "realm_name";
        return "proposal";
    }

    private static String clean(String value, int maxLength) {
        String clean = value == null ? "" : value.trim();
        if (maxLength <= 0 || clean.length() <= maxLength) return clean;
        return clean.substring(0, maxLength);
    }
}
