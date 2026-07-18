package panetina.elarion.core.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ElarionCollectionAction;
import panetina.elarion.core.model.ElarionCollectionEntry;
import panetina.elarion.core.model.ElarionCollectionSnapshot;
import panetina.elarion.core.model.ElarionCollectionTab;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CollectionPayloadTest {
    @Test
    void openPayloadRoundTripsTabsEntriesAndActions() {
        ElarionCollectionSnapshot snapshot = new ElarionCollectionSnapshot(
                "Collection",
                "Mounts and pets",
                "mounts",
                "Wyvern is now active.",
                List.of(new ElarionCollectionTab(
                        "mounts",
                        "Mounts",
                        "Choose the mount summoned by R.",
                        List.of(new ElarionCollectionEntry(
                                "wyvern",
                                "Wyvern",
                                "Future collection reward",
                                "Unlock through future progression.",
                                "Active",
                                "elarion_mounts:textures/gui/collection/wyvern.png",
                                true,
                                true,
                                List.of(new ElarionCollectionAction("set_active", "Active", false)),
                                0xFF5CB7E8,
                                "UNCOMMON",
                                0xFF5CB7E8)))));
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        CollectionOpenPayload.CODEC.encode(buffer, new CollectionOpenPayload(snapshot));
        CollectionOpenPayload decoded = CollectionOpenPayload.CODEC.decode(buffer);

        assertEquals(snapshot, decoded.snapshot());
        ElarionCollectionEntry entry = decoded.snapshot().tabs().getFirst().entries().getFirst();
        assertEquals("set_active", entry.actions().getFirst().id());
        assertEquals(0xFF5CB7E8, entry.accentColor());
        assertEquals("UNCOMMON", entry.rankLabel());
        assertEquals(0xFF5CB7E8, entry.rankColor());
    }

    @Test
    void actionPayloadRoundTripsSelection() {
        CollectionActionPayload payload = new CollectionActionPayload("mounts", "bee", "set_active");
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

        CollectionActionPayload.CODEC.encode(buffer, payload);
        CollectionActionPayload decoded = CollectionActionPayload.CODEC.decode(buffer);

        assertEquals(payload, decoded);
    }

    @Test
    void openPayloadCapsAllProviderCollectionsAtProtocolLimits() {
        List<ElarionCollectionAction> actions = IntStream.range(
                        0, CollectionOpenPayload.MAX_ACTIONS_PER_ENTRY + 4)
                .mapToObj(index -> new ElarionCollectionAction("action-" + index, "Action " + index, true))
                .toList();
        List<ElarionCollectionEntry> entries = IntStream.range(
                        0, CollectionOpenPayload.MAX_ENTRIES_PER_TAB + 8)
                .mapToObj(index -> new ElarionCollectionEntry(
                        "entry-" + index,
                        "Entry " + index,
                        "Subtitle",
                        "Body",
                        "Unlocked",
                        "item:minecraft:paper",
                        true,
                        false,
                        index == 0 ? actions : List.of()))
                .toList();
        List<ElarionCollectionTab> tabs = IntStream.range(0, CollectionOpenPayload.MAX_TABS + 4)
                .mapToObj(index -> new ElarionCollectionTab(
                        "tab-" + index,
                        "Tab " + index,
                        "Entries",
                        index == 0 ? entries : List.of()))
                .toList();
        ElarionCollectionSnapshot snapshot = new ElarionCollectionSnapshot(
                "Collection", "Unlockables", "tab-0", "", tabs);

        ElarionCollectionSnapshot decoded = roundTrip(snapshot);

        assertEquals(CollectionOpenPayload.MAX_TABS, decoded.tabs().size());
        assertEquals(CollectionOpenPayload.MAX_ENTRIES_PER_TAB, decoded.tabs().getFirst().entries().size());
        assertEquals(CollectionOpenPayload.MAX_ACTIONS_PER_ENTRY,
                decoded.tabs().getFirst().entries().getFirst().actions().size());
        assertEquals("tab-0", decoded.selectedTabId());
    }

    @Test
    void openPayloadFiltersUnsafeActionableIdsAndRepairsSelection() {
        String oversizedTabId = "t".repeat(65);
        String oversizedEntryId = "e".repeat(129);
        String oversizedActionId = "a".repeat(65);
        ElarionCollectionEntry validEntry = new ElarionCollectionEntry(
                "valid-entry",
                "V".repeat(140),
                "Subtitle",
                "Body",
                "Unlocked",
                "item:minecraft:paper",
                true,
                false,
                List.of(
                        new ElarionCollectionAction(oversizedActionId, "Unsafe", true),
                        new ElarionCollectionAction("valid-action", "Valid", true),
                        new ElarionCollectionAction("valid-action", "Duplicate", true)),
                0x00123456,
                "R".repeat(40),
                0x0000FFAA);
        ElarionCollectionSnapshot snapshot = new ElarionCollectionSnapshot(
                "Collection",
                "Unlockables",
                oversizedTabId,
                "",
                List.of(
                        new ElarionCollectionTab(oversizedTabId, "Unsafe", "", List.of()),
                        new ElarionCollectionTab("valid-tab", "Valid", "", List.of(
                                new ElarionCollectionEntry(oversizedEntryId, "Unsafe", "", "", "", "",
                                        true, false, List.of()),
                                validEntry,
                                validEntry))));

        ElarionCollectionSnapshot decoded = roundTrip(snapshot);

        assertEquals(1, decoded.tabs().size());
        assertEquals("valid-tab", decoded.selectedTabId());
        assertEquals(1, decoded.tabs().getFirst().entries().size());
        assertEquals("valid-entry", decoded.tabs().getFirst().entries().getFirst().id());
        assertEquals(List.of("valid-action"), decoded.tabs().getFirst().entries().getFirst().actions().stream()
                .map(ElarionCollectionAction::id)
                .toList());
        assertEquals(128, decoded.tabs().getFirst().entries().getFirst().title().length());
        assertEquals(0xFF123456, decoded.tabs().getFirst().entries().getFirst().accentColor());
        assertEquals(32, decoded.tabs().getFirst().entries().getFirst().rankLabel().length());
        assertEquals(0xFF00FFAA, decoded.tabs().getFirst().entries().getFirst().rankColor());
        assertTrue(decoded.tabs().getFirst().entries().getFirst().actions().getFirst().enabled());
    }

    @Test
    void wireSnapshotFallsBackWhenSelectedTabIsPastOutboundLimit() {
        List<ElarionCollectionTab> tabs = IntStream.range(0, CollectionOpenPayload.MAX_TABS + 1)
                .mapToObj(index -> new ElarionCollectionTab("tab-" + index, "Tab " + index, "", List.of()))
                .toList();

        ElarionCollectionSnapshot wire = CollectionOpenPayload.wireSnapshot(new ElarionCollectionSnapshot(
                "Collection", "Unlockables", "tab-32", "", tabs));

        assertEquals("tab-0", wire.selectedTabId());
        assertEquals(CollectionOpenPayload.MAX_TABS, wire.tabs().size());
    }

    private static ElarionCollectionSnapshot roundTrip(ElarionCollectionSnapshot snapshot) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        CollectionOpenPayload.CODEC.encode(buffer, new CollectionOpenPayload(snapshot));
        return CollectionOpenPayload.CODEC.decode(buffer).snapshot();
    }
}
