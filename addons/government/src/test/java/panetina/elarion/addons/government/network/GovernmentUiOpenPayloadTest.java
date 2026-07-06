package panetina.elarion.addons.government.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GovernmentUiOpenPayloadTest {
    @Test
    void codecRoundTripsGovernmentUiSnapshot() {
        GovernmentUiOpenPayload payload = new GovernmentUiOpenPayload(
                "civic_forum",
                "Civic Forum",
                "Founding path for Realm 1",
                "realm1",
                "Realm 1",
                "default",
                520,
                360,
                60,
                false,
                true,
                false,
                0L,
                "Ready.",
                "propose_name",
                "session-1",
                "civic_forum",
                "",
                "civic_forum",
                "Civic Forum",
                true,
                false,
                List.of(new GovernmentUiOpenPayload.Row(
                        "name_vote", "Realm Name", "Choose a name.", "Unlocked", true, false,
                        true, 3L, "expandable")),
                List.of(new GovernmentUiOpenPayload.Row(
                        "republic", "Republic", "Elected leadership.", "Available", true, false, "choice")),
                List.of(new GovernmentUiOpenPayload.Row(
                        "president", "President", "Matie", "1 holder(s)", true, true, "static")),
                List.of(new GovernmentUiOpenPayload.Row(
                        "laws", "Laws", "Future law module.", "Future", false, false, "navigation")));

        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        GovernmentUiOpenPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, GovernmentUiOpenPayload.CODEC.decode(buffer));
    }

    @Test
    void rowCodecKeepsVoteMetadataAndClampsLongState() {
        String longState = "x".repeat(180);
        GovernmentUiOpenPayload.Row row = new GovernmentUiOpenPayload.Row(
                "dark_green", "Dark Green", "Choose the Realm color.", longState,
                true, false, true, 3L, "choice");
        GovernmentUiOpenPayload payload = new GovernmentUiOpenPayload(
                "civic_color", "Realm Color", "", "realm1", "Realm 1", "default",
                520, 360, 60, false, true, true, 0L, "", "",
                "session-1", "civic_color", "", "civic_color", "Realm Color", true, false,
                List.of(), List.of(row), List.of(), List.of());

        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        GovernmentUiOpenPayload.CODEC.encode(buffer, payload);
        GovernmentUiOpenPayload decoded = GovernmentUiOpenPayload.CODEC.decode(buffer);
        GovernmentUiOpenPayload.Row decodedRow = decoded.formRows().getFirst();

        assertTrue(decodedRow.selectedByViewer());
        assertEquals(3L, decodedRow.voteCount());
        assertEquals(128, decodedRow.state().length());
    }

    @Test
    void codecRoundTripsGovernmentUiSemanticMetadata() {
        GovernmentUiOpenPayload.Row row = new GovernmentUiOpenPayload.Row(
                "proposal-1",
                "Harbor Law",
                "Law - Build docks and market access.",
                "Review",
                true,
                false,
                true,
                7L,
                "expandable",
                "law",
                "Law",
                "Tester Nick",
                "Authority review",
                5L,
                2L,
                4L,
                12345L);
        GovernmentUiOpenPayload payload = new GovernmentUiOpenPayload(
                "seat_module_proposals", "Proposal Review", "Authority work.", "realm1", "Realm 1", "default",
                760, 500, 55, false, true, false, 0L, "", "",
                "session-2", "seat_module_proposals", "seat_of_rule", "seat_of_rule", "Proposal Review",
                true, true, "seat_of_rule", "review", "Republic", "President Tester Nick", "Council",
                "dark_green", "seat_crest", "proposal-1", List.of(row), List.of(), List.of(), List.of());

        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        GovernmentUiOpenPayload.CODEC.encode(buffer, payload);
        GovernmentUiOpenPayload decoded = GovernmentUiOpenPayload.CODEC.decode(buffer);
        GovernmentUiOpenPayload.Row decodedRow = decoded.stageRows().getFirst();

        assertEquals("seat_of_rule", decoded.screenFamily());
        assertEquals("review", decoded.activeTabId());
        assertEquals("Republic", decoded.governmentFormLabel());
        assertEquals("President Tester Nick", decoded.authorityLabel());
        assertEquals("Council", decoded.roleLabel());
        assertEquals("dark_green", decoded.realmColor());
        assertEquals("seat_crest", decoded.crestIconId());
        assertEquals("proposal-1", decoded.selectedRowId());
        assertEquals("law", decodedRow.iconId());
        assertEquals("Law", decodedRow.category());
        assertEquals("Tester Nick", decodedRow.actorName());
        assertEquals("Authority review", decodedRow.metricLabel());
        assertEquals(5L, decodedRow.approveCount());
        assertEquals(2L, decodedRow.rejectCount());
        assertEquals(4L, decodedRow.threshold());
        assertEquals(12345L, decodedRow.createdAt());
    }

    @Test
    void feedbackPayloadRoundTripsAndClampsMessage() {
        GovernmentUiFeedbackPayload payload = new GovernmentUiFeedbackPayload("Finish founding first.");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        GovernmentUiFeedbackPayload.CODEC.encode(buffer, payload);

        assertEquals(payload, GovernmentUiFeedbackPayload.CODEC.decode(buffer));

        GovernmentUiFeedbackPayload longPayload = new GovernmentUiFeedbackPayload("x".repeat(1400));
        assertEquals(1024, longPayload.message().length());
    }
}
