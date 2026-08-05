package panetina.elarion.addons.guilds;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.guilds.api.ElarionGuildsApi;
import panetina.elarion.addons.guilds.command.GuildCommands;
import panetina.elarion.addons.guilds.config.GuildConfigDescriptors;
import panetina.elarion.addons.guilds.config.GuildConfigLoader;
import panetina.elarion.addons.guilds.service.GuildService;
import panetina.elarion.addons.guilds.model.GuildPermission;
import panetina.elarion.addons.guilds.service.GuildProfileContributor;
import panetina.elarion.addons.guilds.storage.GuildStorage;
import panetina.elarion.addons.guilds.network.GuildScreenOpenPayload;
import panetina.elarion.addons.guilds.network.GuildScreenOpenRequestPayload;
import panetina.elarion.addons.guilds.network.GuildScreenActionPayload;
import panetina.elarion.addons.guilds.network.GuildScreenClosePayload;
import panetina.elarion.addons.guilds.network.GuildRegistrarOpenPayload;
import panetina.elarion.addons.guilds.network.GuildUiFeedbackPayload;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.api.reset.PlayerResetHandler;
import panetina.elarion.core.api.reset.PlayerResetResult;
import panetina.elarion.core.model.ElarionChatChannel;
import panetina.elarion.core.service.ElarionChatChannelRouter;
import panetina.elarion.core.network.ChatChannelAvailabilityPayload;
import panetina.elarion.core.registry.ActionType;
import panetina.elarion.core.registry.RegistryExecutionResult;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class ElarionGuildsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_guilds");

    @Override
    public void initialize(ElarionApi api) {
        PayloadTypeRegistry.playC2S().register(GuildScreenOpenRequestPayload.ID, GuildScreenOpenRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GuildScreenActionPayload.ID, GuildScreenActionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GuildScreenOpenPayload.ID, GuildScreenOpenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GuildScreenClosePayload.ID, GuildScreenClosePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GuildRegistrarOpenPayload.ID, GuildRegistrarOpenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GuildUiFeedbackPayload.ID, GuildUiFeedbackPayload.CODEC);
        GuildService guilds = new GuildService(api, new GuildStorage(LOGGER), GuildConfigLoader.load());
        api.system().profiles().registerContributor(new GuildProfileContributor(guilds));
        GuildConfigDescriptors.register(api.system().configs(), guilds::config);
        api.characters().registerResetHandler("elarion_guilds", context -> guilds.resetCharacter(context.accountId()));
        api.system().playerResets().register(new PlayerResetHandler() {
            @Override public String id() { return "elarion_guilds"; }
            @Override public java.util.Map<String, Long> preview(net.minecraft.server.MinecraftServer server) {
                return java.util.Map.of();
            }
            @Override public java.util.List<java.nio.file.Path> backupTargets(net.minecraft.server.MinecraftServer server) {
                return java.util.List.of(panetina.elarion.core.storage.JsonStateStorage
                        .addonStateRoot(server, "guilds").resolve("guilds.json"));
            }
            @Override public PlayerResetResult reset(panetina.elarion.core.api.reset.PlayerResetContext context) {
                return PlayerResetResult.of("guildMemberships", guilds.resetAllPlayerState());
            }
        });
        new ElarionGuildsApi(guilds);
        api.notifications().registerAction("elarion_guilds:accept_invite", context -> {
            String guildId = context.notification().metadata().getOrDefault("guildId", "");
            try {
                guilds.accept(context.player(), guildId);
                ServerPlayNetworking.send(context.player(), new ChatChannelAvailabilityPayload(
                        ElarionChatChannelRouter.available(api, context.player())));
                return panetina.elarion.core.service.ElarionNotificationService.ActionResult.success(
                        "Guild joined.", true);
            } catch (IllegalArgumentException exception) {
                return panetina.elarion.core.service.ElarionNotificationService.ActionResult.failure(
                        exception.getMessage());
            }
        });
        api.notifications().registerAction("elarion_guilds:decline_invite", context -> {
            String guildId = context.notification().metadata().getOrDefault("guildId", "");
            try {
                guilds.decline(context.player(), guildId);
                return panetina.elarion.core.service.ElarionNotificationService.ActionResult.success(
                        "Invitation declined.", true);
            } catch (IllegalArgumentException exception) {
                return panetina.elarion.core.service.ElarionNotificationService.ActionResult.failure(
                        exception.getMessage());
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(guilds::bind);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                GuildCommands.registerPlayerCommands(dispatcher, api, guilds, player -> sendScreen(api, guilds, player)));
        api.system().commands().registerAdminSubcommand(() -> GuildCommands.admin(guilds));
        api.identity().registerChatPrefixProvider(player -> guilds.tagFor(player.getUuid()));
        ElarionChatChannelRouter.register(ElarionChatChannel.GUILD, guilds::sendGuildMessage,
                player -> guilds.guildFor(player.getUuid()).isPresent());
        api.registries().actions().register(new ActionType("elarion_guilds:open_registrar", "elarion_guilds",
                "Open the Guild Registrar creation or management screen."));
        api.registries().registerActionHandler("elarion_guilds:open_registrar", context -> {
            ServerPlayerEntity player = context.execution().actor();
            if (player == null) return RegistryExecutionResult.failure("The Guild Registrar requires a player.");
            return RegistryExecutionResult.ok().withServerTask(() -> {
                if (guilds.guildFor(player.getUuid()).isPresent()) sendScreen(api, guilds, player);
                else sendRegistrar(api, guilds, player);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(GuildScreenOpenRequestPayload.ID, (payload, context) ->
                context.server().execute(() -> sendScreen(api, guilds, context.player())));
        ServerPlayNetworking.registerGlobalReceiver(GuildScreenActionPayload.ID, (payload, context) ->
                context.server().execute(() -> handleScreenAction(api, guilds, context.player(), payload)));
        LOGGER.info("Elarion Guilds addon initialized");
    }

    private static void handleScreenAction(ElarionApi api, GuildService guilds, ServerPlayerEntity player,
                                           GuildScreenActionPayload payload) {
        try {
            switch (payload.action()) {
                case "create" -> createFromRegistrar(guilds, player, payload.value());
                case "invite" -> guilds.invite(player, findOnlinePlayer(player, payload.target()));
                case "create_role" -> createRole(guilds, player, payload.value());
                case "assign_role" -> assignRole(guilds, player, payload.target(), payload.value());
                case "leave" -> guilds.leave(player);
                case "publish_announcement" -> guilds.publishAnnouncement(player, payload.value());
                case "redraw_icon" -> guilds.redrawIcon(player, payload.iconPixels());
                default -> throw new IllegalArgumentException("Unsupported guild action.");
            }
            if (guilds.guildFor(player.getUuid()).isPresent()) {
                sendScreen(api, guilds, player);
            } else {
                ServerPlayNetworking.send(player, GuildScreenClosePayload.INSTANCE);
            }
            ServerPlayNetworking.send(player, new ChatChannelAvailabilityPayload(ElarionChatChannelRouter.available(api, player)));
        } catch (IllegalArgumentException exception) {
            ServerPlayNetworking.send(player, new GuildUiFeedbackPayload(false, exception.getMessage()));
        }
    }

    private static void sendScreen(ElarionApi api, GuildService guilds, ServerPlayerEntity player) {
        guilds.guildFor(player.getUuid()).ifPresentOrElse(guild -> {
            java.util.function.Function<java.util.UUID, String> displayName = id -> api.citizens().find(id)
                        .map(citizen -> citizen.nickname().isBlank()
                                ? citizen.lastKnownUsername() : citizen.nickname())
                        .orElse(id.toString());
            java.util.List<String> permissions = guilds.permissionsFor(player.getUuid()).stream()
                    .map(Enum::name).sorted().toList();
            java.util.List<GuildScreenOpenPayload.InviteCandidate> candidates = permissions.contains(GuildPermission.INVITE.name())
                    ? player.getServer()
                    .getPlayerManager().getPlayerList().stream()
                    .filter(candidate -> !candidate.getUuid().equals(player.getUuid()))
                    .filter(candidate -> guilds.guildFor(candidate.getUuid()).isEmpty())
                    .sorted(java.util.Comparator.comparing(candidate -> displayName.apply(candidate.getUuid()),
                            String.CASE_INSENSITIVE_ORDER))
                    .limit(32)
                    .map(candidate -> new GuildScreenOpenPayload.InviteCandidate(
                            candidate.getUuid(), displayName.apply(candidate.getUuid())))
                    .toList() : java.util.List.of();
            ServerPlayNetworking.send(player, GuildScreenOpenPayload.from(
                    guild,
                    displayName,
                    permissions,
                    candidates));
        }, () -> player.sendMessage(Text.literal("You are not in a guild."), false));
    }

    private static void createFromRegistrar(GuildService guilds, ServerPlayerEntity player, String encoded) {
        String[] fields = encoded == null ? new String[0] : encoded.split("\\n", 3);
        if (fields.length != 3) throw new IllegalArgumentException("Guild Registrar submission is incomplete.");
        if (!"true".equals(fields[1]) && !"false".equals(fields[1])) {
            throw new IllegalArgumentException("Guild secrecy selection is invalid.");
        }
        guilds.createFromRegistrar(player, fields[0], fields[2], Boolean.parseBoolean(fields[1]));
    }

    private static void sendRegistrar(ElarionApi api, GuildService guilds, ServerPlayerEntity player) {
        var config = guilds.config();
        ServerPlayNetworking.send(player, new GuildRegistrarOpenPayload(
                config.enabled(),
                config.creationFee(),
                ElarionEconomyApi.get().wallet(player.getUuid()),
                api.serverIdentity().currencyPlural(),
                config.minTagLength(),
                config.maxTagLength(),
                config.maxNameLength()));
    }

    private static ServerPlayerEntity findOnlinePlayer(ServerPlayerEntity actor, java.util.UUID targetId) {
        if (targetId == null) throw new IllegalArgumentException("Select an eligible online player.");
        ServerPlayerEntity target = actor.getServer().getPlayerManager().getPlayer(targetId);
        if (target == null) throw new IllegalArgumentException("That player is no longer online.");
        return target;
    }

    private static void createRole(GuildService guilds, ServerPlayerEntity player, String encoded) {
        String[] fields = encoded == null ? new String[0] : encoded.split("\\n", 3);
        if (fields.length != 3) throw new IllegalArgumentException("Role submission is incomplete.");
        java.util.EnumSet<GuildPermission> permissions = java.util.EnumSet.noneOf(GuildPermission.class);
        if (!fields[2].isBlank()) for (String raw : fields[2].split(",")) {
            try { permissions.add(GuildPermission.valueOf(raw)); }
            catch (IllegalArgumentException ignored) { throw new IllegalArgumentException("Unknown Guild permission."); }
        }
        guilds.createRole(player, fields[0], fields[1], permissions);
    }

    private static void assignRole(GuildService guilds, ServerPlayerEntity player, java.util.UUID target, String roleId) {
        if (target == null || roleId == null || roleId.isBlank()) throw new IllegalArgumentException("Role assignment is incomplete.");
        guilds.assignRole(player, target, roleId);
    }
}
