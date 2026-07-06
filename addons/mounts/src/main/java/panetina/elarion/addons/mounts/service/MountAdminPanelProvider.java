package panetina.elarion.addons.mounts.service;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.mounts.entity.ElarionMountType;
import panetina.elarion.core.model.ElarionAdminPanelAction;
import panetina.elarion.core.model.ElarionAdminPanelRow;
import panetina.elarion.core.service.ElarionAdminPanelProvider;
import panetina.elarion.core.service.ElarionAdminPanelService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;

public final class MountAdminPanelProvider implements ElarionAdminPanelProvider {
    private final MountCollectionService collections;
    private final MountSessionService sessions;

    public MountAdminPanelProvider(MountCollectionService collections, MountSessionService sessions) {
        this.collections = collections;
        this.sessions = sessions;
    }

    @Override
    public String id() {
        return "mounts";
    }

    @Override
    public String title() {
        return "Mounts";
    }

    @Override
    public List<ElarionAdminPanelRow> systemRows(ElarionAdminPanelService.Context context) {
        return List.of(ElarionAdminPanelRow.card("mounts",
                "Mount Sessions",
                "Runtime mount cleanup",
                "Clear nearby Elarion mount entities and their remembered sessions around the admin.",
                "Ready",
                "item:minecraft:saddle",
                List.of(ElarionAdminPanelAction.normal(id(), "clear_nearby", "Clear Nearby Mounts"))));
    }

    @Override
    public List<ElarionAdminPanelAction> playerActions(
            ElarionAdminPanelService.Context context,
            ServerPlayerEntity target
    ) {
        List<String> mountIds = mountIds();
        String placeholder = mountIds.stream().findFirst().orElse("bee");
        return List.of(
                ElarionAdminPanelAction.input(id(), "grant_mount", "Grant Mount", "value", "Mount id", placeholder,
                        mountIds),
                ElarionAdminPanelAction.input(id(), "revoke_mount", "Revoke Mount", "value", "Mount id", placeholder,
                        mountIds),
                ElarionAdminPanelAction.input(id(), "set_active_mount", "Set Active Mount", "value", "Mount id",
                        placeholder, mountIds));
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
            case "clear_nearby" -> {
                sessions.clearNearby(context.admin(), 32.0D);
                yield ElarionAdminPanelService.ActionResult.success("Cleared nearby Elarion mounts.");
            }
            case "grant_mount" -> {
                ServerPlayerEntity target = requirePlayer(context, targetId);
                ElarionMountType type = mount(parameters);
                boolean changed = collections.unlock(target, type);
                yield ElarionAdminPanelService.ActionResult.success((changed ? "Granted " : "Already had ") + type.label() + ".");
            }
            case "revoke_mount" -> {
                ServerPlayerEntity target = requirePlayer(context, targetId);
                ElarionMountType type = mount(parameters);
                boolean changed = collections.revoke(target.getUuid(), type);
                yield ElarionAdminPanelService.ActionResult.success((changed ? "Revoked " : "Did not have ") + type.label() + ".");
            }
            case "set_active_mount" -> {
                ServerPlayerEntity target = requirePlayer(context, targetId);
                ElarionMountType type = mount(parameters);
                boolean changed = collections.setActive(target, type);
                yield changed
                        ? ElarionAdminPanelService.ActionResult.success("Set active mount to " + type.label() + ".")
                        : ElarionAdminPanelService.ActionResult.failure("Player has not unlocked " + type.label() + ".");
            }
            default -> ElarionAdminPanelService.ActionResult.failure("Unknown Mount panel action.");
        };
    }

    private static ElarionMountType mount(Map<String, String> parameters) {
        return ElarionMountType.byId(parameters.getOrDefault("value", ""));
    }

    private static List<String> mountIds() {
        return Arrays.stream(ElarionMountType.values())
                .map(ElarionMountType::id)
                .sorted()
                .toList();
    }

    private static ServerPlayerEntity requirePlayer(ElarionAdminPanelService.Context context, String targetId) {
        UUID uuid = UUID.fromString(targetId);
        ServerPlayerEntity player = context.admin().getServer().getPlayerManager().getPlayer(uuid);
        if (player == null) throw new IllegalArgumentException("Target player is not online.");
        return player;
    }
}
