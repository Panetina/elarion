package panetina.elarion.addons.mounts;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.addons.mounts.entity.ElarionMountEntities;
import panetina.elarion.addons.mounts.entity.ElarionMountEntity;
import panetina.elarion.addons.mounts.entity.ElarionMountType;
import panetina.elarion.addons.mounts.config.MountConfigDescriptors;
import panetina.elarion.addons.mounts.config.MountCollectionTextConfig;
import panetina.elarion.addons.mounts.item.ElarionMountItems;
import panetina.elarion.addons.mounts.network.MountInputPayload;
import panetina.elarion.addons.mounts.network.MountToggleActivePayload;
import panetina.elarion.addons.mounts.service.MountCollectionService;
import panetina.elarion.addons.mounts.service.MountAdminPanelProvider;
import panetina.elarion.addons.mounts.service.MountRealmAssignmentService;
import panetina.elarion.addons.mounts.service.MountSessionService;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.ElarionCollectionAction;
import panetina.elarion.core.model.ElarionCollectionEntry;
import panetina.elarion.core.model.ElarionCollectionTab;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.service.ElarionCollectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ElarionMountsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_mounts");
    private static final MountSessionService SESSIONS = new MountSessionService(LOGGER);
    private static final MountCollectionService COLLECTIONS = new MountCollectionService(LOGGER);
    private static final MountRealmAssignmentService REALM_ASSIGNMENTS = new MountRealmAssignmentService();
    private static MountCollectionTextConfig collectionText = MountCollectionTextConfig.defaults();

    public static MountSessionService sessions() {
        return SESSIONS;
    }

    public static MountCollectionService collections() {
        return COLLECTIONS;
    }

    public static String collectionIcon(ElarionMountType type) {
        return "elarion_mounts:textures/item/" + type.itemId() + ".png";
    }

    @Override
    public void initialize(ElarionApi api) {
        collectionText = MountCollectionTextConfig.load(LOGGER);
        MountConfigDescriptors.register(api.system().configs(), () -> collectionText);
        PayloadTypeRegistry.playC2S().register(MountInputPayload.ID, MountInputPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(MountToggleActivePayload.ID, MountToggleActivePayload.CODEC);
        ElarionMountEntities.initialize();
        ElarionMountItems.register();
        api.system().collections().registerTab(new MountCollectionTabProvider(api));
        api.system().adminPanel().registerProvider(new MountAdminPanelProvider(COLLECTIONS, SESSIONS));
        ServerTickEvents.END_SERVER_TICK.register(SESSIONS::tick);
        ServerLifecycleEvents.SERVER_STARTED.register(COLLECTIONS::bind);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            SESSIONS.captureAllAndSave(server);
            COLLECTIONS.save();
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            SESSIONS.bind(server);
            COLLECTIONS.bind(server);
            SESSIONS.scheduleRestore(handler.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                SESSIONS.captureAndPark(handler.player));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            SESSIONS.bind(player.getServer());
            SESSIONS.scheduleRestore(player);
        });
        ServerPlayNetworking.registerGlobalReceiver(MountInputPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    var entity = context.player().getWorld().getEntityById(payload.entityId());
                    if (entity instanceof ElarionMountEntity mount) {
                        mount.applyInput(context.player(), payload);
                    }
                }));
        ServerPlayNetworking.registerGlobalReceiver(MountToggleActivePayload.ID, (payload, context) ->
                context.server().execute(() -> summonActive(context.player())));
        api.system().commands().registerAdminSubcommand(() -> literal("mounts")
                .then(literal("grant")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestMountTypes(builder))
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            ElarionMountType type = mountType(context, "type");
                                            boolean changed = COLLECTIONS.unlock(target, type);
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal((changed ? "Granted " : "Already had ")
                                                            + type.label() + " for " + target.getGameProfile().getName() + "."),
                                                    true);
                                            return changed ? 1 : 0;
                                        }))))
                .then(literal("revoke")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestMountTypes(builder))
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            ElarionMountType type = mountType(context, "type");
                                            boolean changed = COLLECTIONS.revoke(target.getUuid(), type);
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal((changed ? "Revoked " : "Did not have ")
                                                            + type.label() + " for " + target.getGameProfile().getName() + "."),
                                                    true);
                                            return changed ? 1 : 0;
                                        }))))
                .then(literal("set-active")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestMountTypes(builder))
                                        .executes(context -> {
                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                            ElarionMountType type = mountType(context, "type");
                                            boolean changed = COLLECTIONS.setActive(target, type);
                                            if (!changed) {
                                                context.getSource().sendError(Text.literal(
                                                        target.getGameProfile().getName() + " has not unlocked " + type.label() + "."));
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Set active mount to " + type.label()
                                                            + " for " + target.getGameProfile().getName() + "."),
                                                    true);
                                            return 1;
                                        }))))
                .then(literal("list")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                    Set<String> unlocked = COLLECTIONS.unlocked(target.getUuid());
                                    String active = COLLECTIONS.activeMount(target.getUuid())
                                            .map(ElarionMountType::label)
                                            .orElse("none");
                                    context.getSource().sendFeedback(
                                            () -> Text.literal(target.getGameProfile().getName()
                                                    + " unlocked mounts: " + (unlocked.isEmpty() ? "none" : String.join(", ", unlocked))
                                                    + "; active: " + active + "."),
                                            false);
                                    return unlocked.size();
                                }))));
        api.system().commands().registerTestSubcommand(() -> literal("mounts")
                .then(literal("summon")
                        .then(argument("type", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (ElarionMountType type : ElarionMountType.values()) {
                                        builder.suggest(type.id());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null) {
                                        context.getSource().sendError(Text.literal("This command requires a player source."));
                                        return 0;
                                    }
                                    ElarionMountType type = ElarionMountType.byId(StringArgumentType.getString(context, "type"));
                                    ElarionMountEntity mount = ElarionMountEntities.MOUNT.create(player.getWorld());
                                    if (mount == null) {
                                        context.getSource().sendError(Text.literal("Failed to create mount entity."));
                                        return 0;
                                    }
                                    mount.setMountType(type);
                                    mount.setOwner(player.getUuid());
                                    mount.setReturnWhistleOnDismiss(false);
                                    mount.refreshPositionAndAngles(player.getX(), player.getY() + 3.0D, player.getZ(), player.getYaw(), 0.0F);
                                    mount.startSummonLift(0.75D);
                                    player.getWorld().spawnEntity(mount);
                                    if (player.startRiding(mount, true)) {
                                        mount.updatePassengerPosition(player);
                                    }
                                    SESSIONS.remember(player, mount);
                                    context.getSource().sendFeedback(() -> Text.literal("Summoned test mount " + type.id() + "."), false);
                                    return 1;
                                })))
                .then(literal("debug")
                        .executes(context -> {
                            var player = context.getSource().getPlayer();
                            if (player == null || !(player.getVehicle() instanceof ElarionMountEntity mount)) {
                                context.getSource().sendError(Text.literal("You are not riding an Elarion mount."));
                                return 0;
                            }
                            context.getSource().sendFeedback(() -> Text.literal(mount.debugSummary()), false);
                            return 1;
                        }))
                .then(literal("clear-nearby")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) {
                                context.getSource().sendError(Text.literal("This command requires a player source."));
                                return 0;
                            }
                            var mounts = player.getWorld().getEntitiesByClass(
                                    ElarionMountEntity.class,
                                    player.getBoundingBox().expand(32.0D),
                                    mount -> true);
                            SESSIONS.clearNearby(player, 32.0D);
                            context.getSource().sendFeedback(
                                    () -> Text.literal("Removed " + mounts.size() + " nearby Elarion mount(s)."),
                                    false);
                            return mounts.size();
                        })));
        api.system().commands().registerHelpDescription("/e mounts ...",
                "Manage player mount collection unlocks and active selections.");
        api.system().commands().registerHelpDescription("/e test mounts ...",
                "Development mount summon and diagnostics commands.");
    }

    private static void summonActive(ServerPlayerEntity player) {
        COLLECTIONS.bind(player.getServer());
        COLLECTIONS.activeMount(player.getUuid()).ifPresentOrElse(
                type -> {
                    if (!SESSIONS.summonOrAttach(player, type)) {
                        player.sendMessage(Text.literal("Could not summon your active mount."), false);
                    }
                },
                () -> player.sendMessage(Text.literal("No active unlocked mount. Open Collection with C."), false));
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestMountTypes(
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        for (ElarionMountType type : ElarionMountType.values()) {
            builder.suggest(type.id());
        }
        return builder.buildFuture();
    }

    private static ElarionMountType mountType(com.mojang.brigadier.context.CommandContext<?> context, String name) {
        return ElarionMountType.byId(StringArgumentType.getString(context, name));
    }

    private static final class MountCollectionTabProvider implements ElarionCollectionService.TabProvider {
        private static final String TAB_ID = "mounts";
        private static final String SET_ACTIVE = "set_active";

        private final ElarionApi api;

        private MountCollectionTabProvider(ElarionApi api) {
            this.api = api;
        }

        @Override
        public String id() {
            return TAB_ID;
        }

        @Override
        public ElarionCollectionTab snapshot(ServerPlayerEntity player) {
            List<ElarionCollectionEntry> entries = new ArrayList<>();
            UUID playerId = player.getUuid();
            String playerRealm = api.citizens().find(playerId).map(CitizenRecord::realmId).orElse("");
            String active = COLLECTIONS.activeMount(playerId).map(ElarionMountType::id).orElse("");
            for (ElarionMountType type : ElarionMountType.values()) {
                boolean unlocked = COLLECTIONS.isUnlocked(playerId, type);
                boolean isActive = active.equals(type.id());
                boolean realmExclusive = REALM_ASSIGNMENTS.isRealmExclusive(type);
                String realmId = REALM_ASSIGNMENTS.realmForMount(type).orElse("");
                MountCollectionTextConfig.Entry text = collectionText.entry(type);
                String obtain = text.row(unlocked, realmId);
                String state = isActive ? "Active" : unlocked ? "Unlocked" : "Locked";
                String body = text.detail(unlocked, realmId);
                entries.add(new ElarionCollectionEntry(
                        type.id(),
                        type.label(),
                        obtain,
                        body,
                        state,
                        ElarionMountsAddon.collectionIcon(type),
                        unlocked,
                        isActive,
                        isActive ? List.of() : List.of(new ElarionCollectionAction(
                                SET_ACTIVE,
                                "Set as active",
                                unlocked))));
            }
            return new ElarionCollectionTab(TAB_ID, "Mounts", "Choose the mount summoned by R.", entries);
        }

        @Override
        public ElarionCollectionService.ActionResult act(ServerPlayerEntity player, String entryId, String actionId) {
            if (!SET_ACTIVE.equals(actionId)) {
                return ElarionCollectionService.ActionResult.failure("Unknown mount action.");
            }
            ElarionMountType type = ElarionMountType.byId(entryId);
            if (!COLLECTIONS.setActive(player, type)) {
                return ElarionCollectionService.ActionResult.failure(type.label() + " is still locked.");
            }
            return ElarionCollectionService.ActionResult.success("");
        }

    }
}
