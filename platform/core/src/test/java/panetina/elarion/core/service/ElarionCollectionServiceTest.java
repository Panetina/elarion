package panetina.elarion.core.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.ElarionCollectionAction;
import panetina.elarion.core.model.ElarionCollectionEntry;
import panetina.elarion.core.model.ElarionCollectionTab;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionCollectionServiceTest {
    @Test
    void registeredTabsAppearInSnapshotsAndActionsDispatch() {
        ElarionCollectionService service = new ElarionCollectionService();
        service.registerTab(provider("mounts", "Mounts"));

        var snapshot = service.snapshot(null, "", "");
        var result = service.act(null, "mounts", "bee", "set_active");

        assertEquals("mounts", snapshot.selectedTabId());
        assertEquals(2, snapshot.tabs().size());
        assertEquals("pets", snapshot.tabs().get(1).id());
        assertTrue(result.success());
        assertEquals("bee:set_active", result.message());
    }

    @Test
    void pinnedTabsUseStableCollectionOrder() {
        ElarionCollectionService service = new ElarionCollectionService();
        service.registerTab(provider("titles", "Titles"));
        service.registerTab(provider("mounts", "Mounts"));

        var snapshot = service.snapshot(null, "", "");

        assertEquals("mounts", snapshot.tabs().get(0).id());
        assertEquals("pets", snapshot.tabs().get(1).id());
        assertEquals("titles", snapshot.tabs().get(2).id());
    }

    private static ElarionCollectionService.TabProvider provider(String id, String title) {
        return new ElarionCollectionService.TabProvider() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public ElarionCollectionTab snapshot(net.minecraft.server.network.ServerPlayerEntity player) {
                return new ElarionCollectionTab(id, title, "Choose active entry.",
                        List.of(new ElarionCollectionEntry("bee", "Bee", "", "", "Unlocked", "", true,
                                false, List.of(new ElarionCollectionAction("set_active", "Set as active", true)))));
            }

            @Override
            public ElarionCollectionService.ActionResult act(
                    net.minecraft.server.network.ServerPlayerEntity player,
                    String entryId,
                    String actionId
            ) {
                return ElarionCollectionService.ActionResult.success(entryId + ":" + actionId);
            }
        };
    }
}
