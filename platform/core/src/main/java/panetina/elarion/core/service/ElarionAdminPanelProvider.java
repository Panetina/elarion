package panetina.elarion.core.service;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.model.ElarionAdminPanelAction;
import panetina.elarion.core.model.ElarionAdminPanelRow;

import java.util.List;
import java.util.Map;

public interface ElarionAdminPanelProvider {
    String id();
    String title();

    default List<ElarionAdminPanelRow> systemRows(ElarionAdminPanelService.Context context) {
        return List.of();
    }

    default List<ElarionAdminPanelRow> realmRows(ElarionAdminPanelService.Context context, String realmId) {
        return List.of();
    }

    default List<ElarionAdminPanelAction> playerActions(
            ElarionAdminPanelService.Context context,
            ServerPlayerEntity target
    ) {
        return List.of();
    }

    default boolean supportsRuntimeReset() {
        return false;
    }

    default String runtimeResetDescription() {
        return title();
    }

    default ElarionAdminPanelService.ActionResult runtimeReset(ElarionAdminPanelService.Context context) {
        return ElarionAdminPanelService.ActionResult.failure(title() + " has no runtime reset.");
    }

    default ElarionAdminPanelService.ActionResult act(
            ElarionAdminPanelService.Context context,
            String actionId,
            String targetId,
            Map<String, String> parameters,
            boolean confirmed
    ) {
        return ElarionAdminPanelService.ActionResult.failure("Unsupported action.");
    }
}
