package panetina.elarion.addons.underworld.service;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.model.ElarionAdminPanelAction;
import panetina.elarion.core.model.ElarionAdminPanelRow;
import panetina.elarion.core.service.ElarionAdminPanelProvider;
import panetina.elarion.core.service.ElarionAdminPanelService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class UnderworldAdminPanelProvider implements ElarionAdminPanelProvider {
    private final UnderworldService service;

    public UnderworldAdminPanelProvider(UnderworldService service) {
        this.service = service;
    }

    @Override
    public String id() {
        return "underworld";
    }

    @Override
    public String title() {
        return "Underworld";
    }

    @Override
    public List<ElarionAdminPanelRow> systemRows(ElarionAdminPanelService.Context context) {
        return List.of(ElarionAdminPanelRow.card("underworld",
                "Underworld",
                "Death sessions, graves, and soul state",
                "Reset all Underworld runtime state, including sessions, corpses, fractures, and combat tags.",
                service.corpses().size() + " corpses",
                "item:minecraft:echo_shard",
                List.of(ElarionAdminPanelAction.danger(id(), "reset_all",
                        "Reset Underworld", "Confirm Underworld Reset",
                        "Clear all Underworld sessions, fractures, corpses, graves, and combat tags."))));
    }

    @Override
    public List<ElarionAdminPanelAction> playerActions(
            ElarionAdminPanelService.Context context,
            ServerPlayerEntity target
    ) {
        return List.of(
                ElarionAdminPanelAction.normal(id(), "return_player", "Return From Underworld"),
                ElarionAdminPanelAction.normal(id(), "clear_player", "Clear Death State"));
    }

    @Override
    public boolean supportsRuntimeReset() {
        return true;
    }

    @Override
    public String runtimeResetDescription() {
        return "Underworld sessions, Soul Fractures, corpses, graves, and combat tags";
    }

    @Override
    public ElarionAdminPanelService.ActionResult runtimeReset(ElarionAdminPanelService.Context context) {
        service.resetAll();
        return ElarionAdminPanelService.ActionResult.success("Cleared all Underworld runtime state.");
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
            case "return_player" -> {
                ServerPlayerEntity target = requirePlayer(context, targetId);
                service.forceReturnPlayer(target);
                yield ElarionAdminPanelService.ActionResult.success("Force-returned " + target.getGameProfile().getName() + ".");
            }
            case "clear_player" -> {
                ServerPlayerEntity target = requirePlayer(context, targetId);
                service.resetPlayer(target.getUuid());
                yield ElarionAdminPanelService.ActionResult.success("Cleared Underworld state for " + target.getGameProfile().getName() + ".");
            }
            default -> ElarionAdminPanelService.ActionResult.failure("Unknown Underworld panel action.");
        };
    }

    private static ServerPlayerEntity requirePlayer(ElarionAdminPanelService.Context context, String targetId) {
        UUID uuid = UUID.fromString(targetId);
        ServerPlayerEntity player = context.admin().getServer().getPlayerManager().getPlayer(uuid);
        if (player == null) throw new IllegalArgumentException("Target player is not online.");
        return player;
    }
}
