package panetina.elarion.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ElarionAdminPanelAction;
import panetina.elarion.core.model.ElarionAdminPanelRow;
import panetina.elarion.core.model.ElarionAdminPanelSnapshot;
import panetina.elarion.core.model.ElarionAdminPanelTab;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AdminPanelPayloadTest {
    @Test
    void openPayloadRoundTripsRowsActionsAndConfirmationMetadata() {
        ElarionAdminPanelSnapshot snapshot = new ElarionAdminPanelSnapshot(
                "Admin Panel",
                "Validated controls",
                "danger",
                "runtime_reset_all",
                "Ready",
                List.of(new ElarionAdminPanelTab("danger", "Danger Zone", "Runtime reset",
                        List.of(ElarionAdminPanelRow.danger("runtime_reset_all", "Reset Everything",
                                "Runtime only", "Reset runtime state only.", "2 systems", "item:minecraft:tnt",
                                List.of(ElarionAdminPanelAction.danger("core", "runtime_reset_all",
                                        "Reset Everything", "Confirm", "Runtime only.")))))));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        AdminPanelOpenPayload.CODEC.encode(buffer, new AdminPanelOpenPayload(snapshot));
        AdminPanelOpenPayload decoded = AdminPanelOpenPayload.CODEC.decode(buffer);

        assertEquals(snapshot, decoded.snapshot());
        ElarionAdminPanelAction action = decoded.snapshot().tabs().getFirst().rows().getFirst().actions().getFirst();
        assertTrue(action.requiresConfirmation());
        assertEquals("danger", action.style());
    }

    @Test
    void openPayloadRoundTripsActionSuggestions() {
        ElarionAdminPanelSnapshot snapshot = new ElarionAdminPanelSnapshot(
                "Admin Panel",
                "Validated controls",
                "players",
                "player-id",
                "",
                List.of(new ElarionAdminPanelTab("players", "Players", "Online",
                        List.of(ElarionAdminPanelRow.card("player-id", "Player",
                                "realm1", "Admin actions.", "Ready", "item:minecraft:player_head",
                                List.of(ElarionAdminPanelAction.input("core", "set_realm",
                                        "Set Realm", "value", "Realm id", "realm1",
                                        List.of("realm1", "realm2"))))))));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        AdminPanelOpenPayload.CODEC.encode(buffer, new AdminPanelOpenPayload(snapshot));
        AdminPanelOpenPayload decoded = AdminPanelOpenPayload.CODEC.decode(buffer);

        ElarionAdminPanelAction action = decoded.snapshot().tabs().getFirst().rows().getFirst().actions().getFirst();
        assertEquals(List.of("realm1", "realm2"), action.parameterSuggestions());
    }

    @Test
    void openPayloadCapsRowsAtProtocolLimit() {
        List<ElarionAdminPanelRow> rows = IntStream.range(0, AdminPanelOpenPayload.MAX_ROWS_PER_TAB + 8)
                .mapToObj(index -> ElarionAdminPanelRow.card("row-" + index, "Row " + index,
                        "Subtitle", "Body", "Ready", "item:minecraft:paper", List.of()))
                .toList();
        ElarionAdminPanelSnapshot snapshot = new ElarionAdminPanelSnapshot(
                "Admin Panel", "Validated controls", "configs", "", "",
                List.of(new ElarionAdminPanelTab("configs", "Config", "Descriptors", rows)));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        AdminPanelOpenPayload.CODEC.encode(buffer, new AdminPanelOpenPayload(snapshot));
        AdminPanelOpenPayload decoded = AdminPanelOpenPayload.CODEC.decode(buffer);

        assertEquals(AdminPanelOpenPayload.MAX_ROWS_PER_TAB,
                decoded.snapshot().tabs().getFirst().rows().size());
        assertEquals("row-0", decoded.snapshot().tabs().getFirst().rows().getFirst().id());
    }

    @Test
    void actionPayloadRoundTripsParametersAndConfirmation() {
        AdminPanelActionPayload payload = new AdminPanelActionPayload(
                "players", "core", "player-id", "grant_title", Map.of("value", "citizen"), true);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        AdminPanelActionPayload.CODEC.encode(buffer, payload);
        AdminPanelActionPayload decoded = AdminPanelActionPayload.CODEC.decode(buffer);

        assertEquals(payload, decoded);
        assertEquals("citizen", decoded.parameters().get("value"));
    }
}
