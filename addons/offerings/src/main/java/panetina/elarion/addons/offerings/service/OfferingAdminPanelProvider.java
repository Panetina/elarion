package panetina.elarion.addons.offerings.service;

import panetina.elarion.core.model.ElarionAdminPanelAction;
import panetina.elarion.core.model.ElarionAdminPanelRow;
import panetina.elarion.core.service.ElarionAdminPanelProvider;
import panetina.elarion.core.service.ElarionAdminPanelService;

import java.util.List;
import java.util.Map;

public final class OfferingAdminPanelProvider implements ElarionAdminPanelProvider {
    private final OfferingService service;

    public OfferingAdminPanelProvider(OfferingService service) {
        this.service = service;
    }

    @Override
    public String id() {
        return "offerings";
    }

    @Override
    public String title() {
        return "Shrines";
    }

    @Override
    public List<ElarionAdminPanelRow> systemRows(ElarionAdminPanelService.Context context) {
        return List.of(ElarionAdminPanelRow.card("offerings",
                "Shrines And Offerings",
                "Foundation progression and donations",
                "Reset Shrine progression globally, while preserving Shrine blocks, links, and instance ids.",
                service.instances().size() + " instances",
                "item:minecraft:amethyst_shard",
                List.of(ElarionAdminPanelAction.danger(id(), "reset_all",
                        "Reset All Shrines", "Confirm Shrine Reset",
                        "Reset all Shrine progression, donations, and Offering-owned Realm flags. Blocks and links are preserved."))));
    }

    @Override
    public List<ElarionAdminPanelRow> realmRows(ElarionAdminPanelService.Context context, String realmId) {
        return List.of(ElarionAdminPanelRow.card("offerings:" + realmId,
                "Shrines",
                "Realm Shrine progression",
                "Reset Offering progression and Foundation flags for this Realm.",
                "Ready",
                "item:minecraft:amethyst_shard",
                List.of(ElarionAdminPanelAction.danger(id(), "reset_realm",
                        "Reset Shrine", "Confirm Realm Shrine Reset",
                        "Reset Shrine progression, donations, and Foundation flags for this Realm."))));
    }

    @Override
    public boolean supportsRuntimeReset() {
        return true;
    }

    @Override
    public String runtimeResetDescription() {
        return "Shrine progression, donations, Foundation flags, and Offering-owned global flags";
    }

    @Override
    public ElarionAdminPanelService.ActionResult runtimeReset(ElarionAdminPanelService.Context context) {
        int count = service.resetAllProgression(context.admin());
        return ElarionAdminPanelService.ActionResult.success("Reset " + count + " Shrine instance(s).");
    }

    @Override
    public ElarionAdminPanelService.ActionResult act(
            ElarionAdminPanelService.Context context,
            String actionId,
            String targetId,
            Map<String, String> parameters,
            boolean confirmed
    ) {
        return switch (actionId) {
            case "reset_all" -> {
                if (!confirmed) yield ElarionAdminPanelService.ActionResult.failure("Confirmation required.");
                yield runtimeReset(context);
            }
            case "reset_realm" -> {
                if (!confirmed) yield ElarionAdminPanelService.ActionResult.failure("Confirmation required.");
                int count = service.resetRealmProgression(targetId, context.admin());
                yield ElarionAdminPanelService.ActionResult.success(
                        "Reset Shrine progression for " + targetId + " across " + count + " instance(s).");
            }
            default -> ElarionAdminPanelService.ActionResult.failure("Unknown Shrine panel action.");
        };
    }
}
