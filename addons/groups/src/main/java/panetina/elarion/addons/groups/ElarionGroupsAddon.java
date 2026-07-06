package panetina.elarion.addons.groups;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.groups.api.ElarionGroupsApi;
import panetina.elarion.addons.groups.command.GroupCommands;
import panetina.elarion.addons.groups.config.GroupConfigDescriptors;
import panetina.elarion.addons.groups.config.GroupConfigLoader;
import panetina.elarion.addons.groups.service.GroupService;
import panetina.elarion.addons.groups.storage.GroupStorage;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

public final class ElarionGroupsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_groups");

    @Override
    public void initialize(ElarionApi api) {
        GroupService groups = new GroupService(api, new GroupStorage(LOGGER), GroupConfigLoader.load());
        GroupConfigDescriptors.register(api.system().configs(), groups::config);
        api.characters().registerResetHandler("elarion_groups", context -> groups.resetCharacter(context.accountId()));
        new ElarionGroupsApi(groups);
        api.notifications().registerAction("elarion_groups:accept_invite", context -> {
            String groupId = context.notification().metadata().getOrDefault("groupId", "");
            try {
                groups.accept(context.player(), groupId);
                return panetina.elarion.core.service.ElarionNotificationService.ActionResult.success(
                        "Group joined.", true);
            } catch (IllegalArgumentException exception) {
                return panetina.elarion.core.service.ElarionNotificationService.ActionResult.failure(
                        exception.getMessage());
            }
        });
        api.notifications().registerAction("elarion_groups:decline_invite", context -> {
            String groupId = context.notification().metadata().getOrDefault("groupId", "");
            try {
                groups.decline(context.player(), groupId);
                return panetina.elarion.core.service.ElarionNotificationService.ActionResult.success(
                        "Invitation declined.", true);
            } catch (IllegalArgumentException exception) {
                return panetina.elarion.core.service.ElarionNotificationService.ActionResult.failure(
                        exception.getMessage());
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(groups::bind);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                GroupCommands.registerPlayerCommands(dispatcher, api, groups));
        api.system().commands().registerAdminSubcommand(() -> GroupCommands.admin(groups));
        api.identity().registerChatPrefixProvider(player -> groups.tagFor(player.getUuid()));
        LOGGER.info("Elarion Groups addon initialized");
    }
}
