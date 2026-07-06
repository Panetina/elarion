package panetina.elarion.addons.government.service;

import panetina.elarion.core.model.ElarionAdminPanelAction;
import panetina.elarion.core.model.ElarionAdminPanelRow;
import panetina.elarion.core.service.ElarionAdminPanelProvider;
import panetina.elarion.core.service.ElarionAdminPanelService;

import java.util.List;
import java.util.Map;

public final class GovernmentAdminPanelProvider implements ElarionAdminPanelProvider {
    private final GovernmentStateService states;

    public GovernmentAdminPanelProvider(GovernmentStateService states) {
        this.states = states;
    }

    @Override
    public String id() {
        return "government";
    }

    @Override
    public String title() {
        return "Government";
    }

    @Override
    public List<ElarionAdminPanelRow> systemRows(ElarionAdminPanelService.Context context) {
        return List.of(ElarionAdminPanelRow.card("government",
                "Government",
                "Founding votes, offices, and civic records",
                "Reset all Realm Government runtime state, or use the Realms tab for scoped resets and vote-window advances.",
                states.realms().size() + " Realm states",
                "item:minecraft:lectern",
                List.of(ElarionAdminPanelAction.danger(id(), "reset_all",
                        "Reset All Government", "Confirm Government Reset",
                        "Reset Government founding state, votes, proposals, offices, and civic records for every Realm."))));
    }

    @Override
    public List<ElarionAdminPanelRow> realmRows(ElarionAdminPanelService.Context context, String realmId) {
        return List.of(ElarionAdminPanelRow.card("government:" + realmId,
                "Government",
                "Realm Government controls",
                "Reset this Realm's Government runtime state or advance its current Civic Forum timing window.",
                "Ready",
                "item:minecraft:lectern",
                List.of(
                        ElarionAdminPanelAction.danger(id(), "reset_realm",
                                "Reset Government", "Confirm Realm Government Reset",
                                "Reset this Realm's Government founding state, votes, proposals, offices, and civic records."),
                        ElarionAdminPanelAction.normal(id(), "advance_realm", "Advance Vote Window"))));
    }

    @Override
    public boolean supportsRuntimeReset() {
        return true;
    }

    @Override
    public String runtimeResetDescription() {
        return "Government founding state, votes, offices, proposals, and civic records";
    }

    @Override
    public ElarionAdminPanelService.ActionResult runtimeReset(ElarionAdminPanelService.Context context) {
        int count = states.resetAllRealms();
        return ElarionAdminPanelService.ActionResult.success("Reset " + count + " Realm Government state(s).");
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
                states.resetRealm(targetId);
                yield ElarionAdminPanelService.ActionResult.success("Reset Government state for " + targetId + ".");
            }
            case "advance_realm" -> ElarionAdminPanelService.ActionResult.success(states.advanceCurrentWindow(targetId));
            default -> ElarionAdminPanelService.ActionResult.failure("Unknown Government panel action.");
        };
    }
}
