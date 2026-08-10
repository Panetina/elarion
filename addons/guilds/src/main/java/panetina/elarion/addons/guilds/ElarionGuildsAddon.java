package panetina.elarion.addons.guilds;

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
import panetina.elarion.addons.guilds.network.GuildRegistrarSubmitPayload;
import panetina.elarion.addons.guilds.network.GuildEmptyScreenPayload;
import panetina.elarion.addons.guilds.network.GuildUiFeedbackPayload;
import panetina.elarion.addons.guilds.network.GuildInvitationPromptPayload;
import panetina.elarion.addons.guilds.network.GuildInvitationDecisionPayload;
import panetina.elarion.addons.guilds.network.GuildDonationPayload;
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
import panetina.elarion.core.registry.PlayerContextActionRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class ElarionGuildsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_guilds");

    @Override
    public void initialize(ElarionApi api) {
        PayloadTypeRegistry.playC2S().register(GuildScreenOpenRequestPayload.ID, GuildScreenOpenRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GuildScreenActionPayload.ID, GuildScreenActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GuildRegistrarSubmitPayload.ID, GuildRegistrarSubmitPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GuildInvitationDecisionPayload.ID, GuildInvitationDecisionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GuildDonationPayload.ID, GuildDonationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GuildScreenOpenPayload.ID, GuildScreenOpenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GuildScreenClosePayload.ID, GuildScreenClosePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GuildRegistrarOpenPayload.ID, GuildRegistrarOpenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GuildUiFeedbackPayload.ID, GuildUiFeedbackPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GuildEmptyScreenPayload.ID, GuildEmptyScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GuildInvitationPromptPayload.ID, GuildInvitationPromptPayload.CODEC);
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
        api.system().commands().registerAdminSubcommand(() -> GuildCommands.admin(guilds));
        api.identity().registerChatPrefixProvider(player -> guilds.tagFor(player.getUuid()));
        ElarionChatChannelRouter.register(ElarionChatChannel.GUILD, guilds::sendGuildMessage,
                player -> guilds.guildFor(player.getUuid()).isPresent());
        api.registries().playerContextActions().register(new PlayerContextActionRegistry.Action(
                "elarion_guilds:invite", "Invite to Guild", new PlayerContextActionRegistry.Handler() {
            @Override public boolean available(ServerPlayerEntity actor, ServerPlayerEntity target) {
                return target != null && guilds.guildFor(target.getUuid()).isEmpty()
                        && guilds.permissionsFor(actor.getUuid()).contains(GuildPermission.INVITE);
            }
            @Override public RegistryExecutionResult execute(ServerPlayerEntity actor, ServerPlayerEntity target) {
                try {
                    guilds.invite(actor, target);
                    sendInvitationPrompt(guilds, actor, target);
                    return RegistryExecutionResult.ok();
                } catch (IllegalArgumentException exception) {
                    return RegistryExecutionResult.failure(exception.getMessage());
                }
            }
        }));
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
                context.server().execute(() -> sendScreenOrEmpty(api, guilds, context.player())));
        ServerPlayNetworking.registerGlobalReceiver(GuildScreenActionPayload.ID, (payload, context) ->
                context.server().execute(() -> handleScreenAction(api, guilds, context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(GuildRegistrarSubmitPayload.ID, (payload, context) ->
                context.server().execute(() -> handleRegistrarSubmit(api, guilds, context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(GuildInvitationDecisionPayload.ID, (payload, context) ->
                context.server().execute(() -> handleInvitationDecision(api, guilds, context.player(), payload)));
        ServerPlayNetworking.registerGlobalReceiver(GuildDonationPayload.ID, (payload, context) ->
                context.server().execute(() -> handleDonation(api, guilds, context.player(), payload)));
        LOGGER.info("Elarion Guilds addon initialized");
    }

    private static void handleScreenAction(ElarionApi api, GuildService guilds, ServerPlayerEntity player,
                                           GuildScreenActionPayload payload) {
        try {
            switch (payload.action()) {
                case "invite" -> {
                    ServerPlayerEntity target = findOnlinePlayer(player, payload.target());
                    guilds.invite(player, target);
                    sendInvitationPrompt(guilds, player, target);
                }
                case "create_role" -> createRole(guilds, player, payload.value());
                case "assign_role" -> assignRole(guilds, player, payload.target(), payload.value());
                case "transfer_leadership" -> guilds.transfer(player, findOnlinePlayer(player, payload.target()));
                case "toggle_secret" -> guilds.setSecret(player, Boolean.parseBoolean(payload.value()));
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
                    guilds.config().progression(),
                    permissions,
                    candidates));
        }, () -> player.sendMessage(Text.literal("You are not in a guild."), false));
    }

    private static void sendScreenOrEmpty(ElarionApi api, GuildService guilds, ServerPlayerEntity player) {
        if (guilds.guildFor(player.getUuid()).isPresent()) sendScreen(api, guilds, player);
        else ServerPlayNetworking.send(player, GuildEmptyScreenPayload.INSTANCE);
    }

    private static void handleRegistrarSubmit(ElarionApi api, GuildService guilds, ServerPlayerEntity player,
                                               GuildRegistrarSubmitPayload payload) {
        try {
            guilds.createFromRegistrar(player, payload.tag(), payload.name(), payload.secret());
            sendScreen(api, guilds, player);
            ServerPlayNetworking.send(player, new ChatChannelAvailabilityPayload(ElarionChatChannelRouter.available(api, player)));
        } catch (IllegalArgumentException exception) {
            ServerPlayNetworking.send(player, new GuildUiFeedbackPayload(false, exception.getMessage()));
        }
    }

    private static void handleInvitationDecision(ElarionApi api, GuildService guilds, ServerPlayerEntity player,
                                                 GuildInvitationDecisionPayload payload) {
        try {
            if (payload.accepted()) guilds.accept(player, payload.guildId());
            else guilds.decline(player, payload.guildId());
            if (guilds.guildFor(player.getUuid()).isPresent()) sendScreen(api, guilds, player);
            ServerPlayNetworking.send(player, new ChatChannelAvailabilityPayload(
                    ElarionChatChannelRouter.available(api, player)));
        } catch (IllegalArgumentException exception) {
            player.sendMessage(Text.literal(exception.getMessage()), false);
        }
    }

    private static void sendInvitationPrompt(GuildService guilds, ServerPlayerEntity inviter, ServerPlayerEntity target) {
        GuildService.GuildInvitationView invitation = guilds.invitationView(inviter, target.getUuid());
        ServerPlayNetworking.send(target, new GuildInvitationPromptPayload(
                invitation.guildId(), invitation.guildName(), invitation.guildTag(), invitation.inviterName()));
    }

    private static void sendRegistrar(ElarionApi api, GuildService guilds, ServerPlayerEntity player) {
        var config = guilds.config();
        ServerPlayNetworking.send(player, new GuildRegistrarOpenPayload(
                config.enabled(),
                config.creationFee(),
                ElarionEconomyApi.get().physicalCurrency(player),
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

    private static void handleDonation(ElarionApi api, GuildService guilds, ServerPlayerEntity player,
                                       GuildDonationPayload payload) {
        try {
            guilds.donate(player, payload.operationId(), payload.amount());
            sendScreen(api, guilds, player);
        } catch (IllegalArgumentException exception) {
            ServerPlayNetworking.send(player, new GuildUiFeedbackPayload(false, exception.getMessage()));
        }
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
