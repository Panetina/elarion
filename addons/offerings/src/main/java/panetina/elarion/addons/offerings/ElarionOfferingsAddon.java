package panetina.elarion.addons.offerings;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.offerings.api.ElarionOfferingsApi;
import panetina.elarion.addons.offerings.command.OfferingCommands;
import panetina.elarion.addons.offerings.config.OfferingConfigDescriptors;
import panetina.elarion.addons.offerings.model.OfferingAnchor;
import panetina.elarion.addons.offerings.model.OfferingInstance;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.model.OfferingProjectLevel;
import panetina.elarion.addons.offerings.model.OfferingUiProgress;
import panetina.elarion.addons.offerings.network.ShrineUiOpenPayload;
import panetina.elarion.addons.offerings.network.ShrineContributionSubmitPayload;
import panetina.elarion.addons.offerings.service.OfferingDefinitionService;
import panetina.elarion.addons.offerings.service.OfferingAdminPanelProvider;
import panetina.elarion.addons.offerings.service.OfferingService;
import panetina.elarion.addons.offerings.storage.OfferingStorage;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.registry.ActionType;
import panetina.elarion.core.registry.RegistryExecutionResult;

public final class ElarionOfferingsAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_offerings");

    @Override
    public void initialize(ElarionApi api) {
        PayloadTypeRegistry.playS2C().register(ShrineUiOpenPayload.ID, ShrineUiOpenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(
                ShrineContributionSubmitPayload.ID, ShrineContributionSubmitPayload.CODEC);
        OfferingsBlocks.register();
        OfferingDefinitionService definitions = new OfferingDefinitionService(api);
        definitions.load();
        OfferingService service = new OfferingService(LOGGER, api, definitions, new OfferingStorage(LOGGER));
        api.system().adminPanel().registerProvider(new OfferingAdminPanelProvider(service));
        new ElarionOfferingsApi(definitions, service);
        OfferingConfigDescriptors.register(api.system().configs(), definitions::all, definitions::ui);

        api.system().abilities().register("elarion.offering.manage");
        registerActions(api, service);
        registerShrineInteractions(api, definitions, service);
        registerShrineContributions(api, definitions, service);
        registerShrineRemoval(service);

        api.progressionApi().rewards().registerHandler("offering-event", (context, action) -> {
            api.system().events().emitProgression(new panetina.elarion.core.event.ElarionEventBus.ProgressionEvent(
                    action.parameters().getOrDefault("event", "offering.event"),
                    context.player().getUuid(),
                    action.parameters().getOrDefault("project", context.rewardId())));
            return true;
        });
        api.system().commands().registerAdminSubcommand(
                () -> OfferingCommands.create(api, definitions, service));
        api.system().commands().registerTestSubcommand(
                () -> OfferingCommands.testCommands(api, service));
        api.system().commands().registerTestSubcommand(
                () -> OfferingCommands.realmTestCommands(api, service));
        api.system().commands().registerHelpDescription(
                "/e offerings ...", "Manage offering definitions, instances, and linked Shrines.");
        api.system().commands().registerHelpDescription(
                "/e test shrine reset [realm]", "Reset Shrine progression and Foundation flags for testing.");

        ServerLifecycleEvents.SERVER_STARTED.register(service::bind);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> service.save());
        ServerTickEvents.END_SERVER_TICK.register(service::tick);
        LOGGER.info("Elarion offerings initialized with {} project definitions", definitions.all().size());
    }

    private static void registerShrineRemoval(OfferingService service) {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!state.isOf(OfferingsBlocks.SHRINE_OF_FOUNDATION)
                    || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return true;
            }
            BlockPos origin = ShrineOfFoundationBlock.origin(pos, state);
            String worldId = world.getRegistryKey().getValue().toString();
            service.deleteLinkedInstanceAt(worldId, origin, serverPlayer);
            return true;
        });
    }

    private static void registerShrineInteractions(
            ElarionApi api,
            OfferingDefinitionService definitions,
            OfferingService service
    ) {
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            var state = world.getBlockState(hit.getBlockPos());
            if (!state.isOf(OfferingsBlocks.SHRINE_OF_FOUNDATION)) return ActionResult.PASS;
            BlockPos origin = ShrineOfFoundationBlock.origin(hit.getBlockPos(), state);
            String worldId = world.getRegistryKey().getValue().toString();
            var anchor = service.findAnchorAt(worldId, origin);
            if (anchor.isEmpty()) {
                if (serverPlayer.hasPermissionLevel(4)) {
                    serverPlayer.sendMessage(Text.literal(
                            "Unlinked " + api.serverIdentity().shrineOfFoundation()
                                    + ". Use /e offerings shrine link <instance> while looking at it."), false);
                } else {
                    serverPlayer.sendMessage(Text.literal("This " + api.serverIdentity().shrineOfFoundation()
                            + " has not awakened."), false);
                }
                return ActionResult.SUCCESS;
            }
            openShrineUi(api, serverPlayer, definitions, service, anchor.get(), "", false);
            return ActionResult.SUCCESS;
        });
    }

    private static void registerShrineContributions(
            ElarionApi api,
            OfferingDefinitionService definitions,
            OfferingService service
    ) {
        ServerPlayNetworking.registerGlobalReceiver(ShrineContributionSubmitPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    ServerPlayerEntity player = context.player();
                    long amount;
                    try {
                        if (!payload.rawAmount().matches("[0-9]{1,10}")) {
                            throw new NumberFormatException("invalid amount");
                        }
                        amount = Long.parseLong(payload.rawAmount());
                    } catch (NumberFormatException exception) {
                        sendShrineSnapshotForInstance(
                                api, player, definitions, service, payload.instanceId(),
                                "Enter a positive whole number.", true);
                        return;
                    }
                    var result = service.contributePlayer(
                            payload.instanceId(), payload.requirementKey(), amount, player);
                    sendShrineSnapshotForInstance(
                            api, player, definitions, service, payload.instanceId(),
                            result.message(), !result.success());
                }));
    }

    private static void sendShrineSnapshotForInstance(
            ElarionApi api,
            ServerPlayerEntity player,
            OfferingDefinitionService definitions,
            OfferingService service,
            String instanceId,
            String message,
            boolean error
    ) {
        var instance = service.findInstance(instanceId);
        if (instance.isEmpty() || instance.get().anchorId().isBlank()) {
            player.sendMessage(Text.literal(message.isBlank() ? "The Shrine is no longer available." : message), false);
            return;
        }
        var anchor = service.findAnchor(instance.get().anchorId());
        if (anchor.isEmpty()) {
            player.sendMessage(Text.literal("The Shrine link is no longer valid."), false);
            return;
        }
        openShrineUi(api, player, definitions, service, anchor.get(), message, error);
    }

    private static void openShrineUi(
            ElarionApi api,
            ServerPlayerEntity player,
            OfferingDefinitionService definitions,
            OfferingService service,
            OfferingAnchor anchor,
            String resultMessage,
            boolean resultError
    ) {
        var instance = service.findInstance(anchor.instanceId())
                .orElseThrow(() -> new IllegalArgumentException("Missing linked offering " + anchor.instanceId()));
        var project = definitions.find(instance.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Missing offering project " + instance.projectId()));
        var level = project.level(instance.activeLevelId()).orElse(project.firstLevel());
        var progress = service.progress(instance.id());
        java.util.List<ShrineUiOpenPayload.RequirementRow> rows = new java.util.ArrayList<>();
        for (var row : progress.rows()) {
            var requirement = level.requirements().stream()
                    .filter(candidate -> candidate.key().equals(row.key()))
                    .findFirst().orElse(null);
            String type = requirement == null ? "unknown" : requirement.type();
            String id = requirement == null ? row.key() : requirement.id();
            rows.add(new ShrineUiOpenPayload.RequirementRow(
                    row.key(), type, id, requirementLabel(api, type, id), requirementIcon(type, id),
                    row.current(), row.required(), row.complete()));
        }
        OfferingUiProgress totals = OfferingUiProgress.from(progress.rows());
        String status = progress.complete() || instance.completed()
                ? "Complete"
                : "Awaiting " + api.serverIdentity().offeringPlural().toLowerCase(java.util.Locale.ROOT);
        String scopeLabel = instance.scope().name().toLowerCase(java.util.Locale.ROOT);
        String realmLabel = instance.realmId().isBlank()
                ? ""
                : api.realms().find(instance.realmId())
                .map(api.realms()::officialName)
                .orElse(instance.realmId());
        String subtitle = api.serverIdentity().shrineOfFoundation() + " - "
                + scopeLabel
                + (realmLabel.isBlank() ? "" : " - " + realmLabel);
        var ui = definitions.ui();
        java.util.List<ShrineUiOpenPayload.DisplayRow> rewards = milestoneRewards(api, level.milestones(),
                instance.completedMilestones());
        java.util.List<ShrineUiOpenPayload.DonationRow> history = service.recentDonations(
                        instance.id(), level.id(), 20).stream()
                .map(record -> {
                    var citizen = record.contributorId() == null
                            ? java.util.Optional.<panetina.elarion.core.model.CitizenRecord>empty()
                            : api.citizens().find(record.contributorId());
                    String contributor = citizen.map(value -> value.nickname() == null || value.nickname().isBlank()
                                    ? value.lastKnownUsername() : value.nickname())
                            .filter(value -> value != null && !value.isBlank())
                            .orElse(record.contributorName());
                    int contributorColor = citizen.flatMap(value -> api.realms().find(value.realmId()))
                            .map(realm -> realmTextColor(realm.color()))
                            .orElse(0xFFFFFFFF);
                    int offeringColor = "currency".equals(record.type()) ? 0xFF9A9CFF : 0xFFAAAAAA;
                    return new ShrineUiOpenPayload.DonationRow(
                            Long.toString(record.createdAt()), contributor, contributorColor,
                            record.amount(), donationLabel(api, record.type(), record.requirementKey()),
                            offeringColor, java.time.Instant.ofEpochMilli(record.createdAt()).toString());
                })
                .toList();
        boolean completed = progress.complete() || instance.completed();
        ServerPlayNetworking.send(player, new ShrineUiOpenPayload(
                instance.id(), project.id(), shrineTitle(instance, project, level), subtitle, level.description(), status,
                level.presentation().levelText(), level.presentation().icon(), ui.themeVariant(),
                ui.logicalWidth(), ui.logicalHeight(), ui.minimumScalePercent(), ui.summaryWidth(),
                ui.tabHeight(), ui.rowHeight(), ui.iconSize(), ui.closeButtonWidth(),
                totals.current(), totals.required(), rows, rewards, history,
                ui.rewardsPlaceholder(), ui.historyPlaceholder(), ui.contributionPlaceholder(),
                resultMessage == null ? "" : resultMessage, resultError, completed,
                ui.eventTitle(), ui.eventBody(), ui.eventLockedBody(), false));
    }

    private static String donationLabel(ElarionApi api, String type, String requirementKey) {
        if ("currency".equals(type)) return api.serverIdentity().currencyPlural();
        if ("items".equals(type) && requirementKey.startsWith("item:")) {
            return requirementLabel(api, "items", requirementKey.substring("item:".length()));
        }
        return api.serverIdentity().offeringPlural().toLowerCase(java.util.Locale.ROOT);
    }

    private static int realmTextColor(String colorName) {
        net.minecraft.util.Formatting formatting = net.minecraft.util.Formatting.byName(colorName);
        Integer color = formatting == null ? null : formatting.getColorValue();
        return color == null ? 0xFFFFFFFF : 0xFF000000 | color;
    }

    private static java.util.List<ShrineUiOpenPayload.DisplayRow> milestoneRewards(
            ElarionApi api,
            java.util.List<panetina.elarion.addons.offerings.model.OfferingMilestone> milestones,
            java.util.Set<String> completedMilestones
    ) {
        java.util.List<ShrineUiOpenPayload.DisplayRow> rows = new java.util.ArrayList<>();
        for (var milestone : milestones) {
            boolean completed = completedMilestones.contains(milestone.id());
            if ("elarion:run_reward".equals(milestone.type())) {
                String rewardId = milestone.parameters().getOrDefault(
                        "reward", milestone.parameters().getOrDefault("id", ""));
                int actionIndex = 0;
                for (var action : api.rewards().actions(rewardId)) {
                    String rowId = milestone.id() + "_" + actionIndex++;
                    switch (action.type()) {
                        case "currency-reward" -> rows.add(new ShrineUiOpenPayload.DisplayRow(
                                rowId, "item", api.serverIdentity().currencyPlural(), "",
                                "item:elarion:currency", positiveInt(action.parameters().get("amount")),
                                "", completed));
                        case "item" -> {
                            String itemId = action.parameters().getOrDefault("id", "");
                            Identifier identifier = Identifier.tryParse(itemId);
                            String label = identifier != null && Registries.ITEM.containsId(identifier)
                                    ? Registries.ITEM.get(identifier).getName().getString() : itemId;
                            rows.add(new ShrineUiOpenPayload.DisplayRow(
                                    rowId, "item", label, "", "item:" + itemId,
                                    positiveInt(action.parameters().get("count")),
                                    action.parameters().getOrDefault(
                                            "enchants", action.parameters().getOrDefault("enchantments", "")),
                                    completed));
                        }
                        default -> {
                            String label = action.parameters().getOrDefault("display-label", "");
                            if (!label.isBlank()) {
                                rows.add(new ShrineUiOpenPayload.DisplayRow(
                                        rowId, "event", label,
                                        action.parameters().getOrDefault("display-body", ""),
                                        action.parameters().getOrDefault("display-icon", ""), 1, "", completed));
                            }
                        }
                    }
                }
                continue;
            }
            String label = milestone.parameters().getOrDefault("display-label", "");
            if (!label.isBlank()) {
                rows.add(new ShrineUiOpenPayload.DisplayRow(
                        milestone.id(), "event", label,
                        milestone.parameters().getOrDefault("display-body", ""),
                        milestone.parameters().getOrDefault("display-icon", ""), 1, "", completed));
            }
        }
        return java.util.List.copyOf(rows);
    }

    private static int positiveInt(String raw) {
        try {
            return Math.max(1, Math.min(999_999, Integer.parseInt(raw == null ? "1" : raw)));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private static String requirementLabel(ElarionApi api, String type, String id) {
        return switch (type) {
            case "currency" -> api.serverIdentity().currencyPlural();
            case "items" -> {
                String raw = id.startsWith("#") ? id.substring(1) : id;
                Identifier identifier = Identifier.tryParse(raw);
                yield identifier != null && Registries.ITEM.containsId(identifier)
                        ? Registries.ITEM.get(identifier).getName().getString()
                        : id;
            }
            case "events" -> id.replace('_', ' ');
            default -> id;
        };
    }

    private static String requirementIcon(String type, String id) {
        if ("currency".equals(type)) return "item:elarion:currency";
        if ("items".equals(type) && !id.startsWith("#")) return "item:" + id;
        return "texture:minecraft:textures/item/amethyst_shard.png";
    }

    private static void registerActions(ElarionApi api, OfferingService service) {
        register(api, "elarion:offering_add_event",
                "Credits event progress into an offering project instance.",
                context -> {
                    String instance = context.parameters().getOrDefault("instance", "");
                    String event = context.parameters().getOrDefault("event", "");
                    long amount = amount(context.parameters().getOrDefault("amount", "1"));
                    if (instance.isBlank() || event.isBlank()) {
                        return RegistryExecutionResult.failure("instance and event are required");
                    }
                    service.contributeEvent(instance, event, amount, context.execution().actor());
                    return RegistryExecutionResult.ok(api.serverIdentity().offeringSingular() + " event credited.");
                });
        register(api, "elarion:offering_start_realm_project",
                "Starts a realm-scoped offering project instance.",
                context -> {
                    String realm = context.parameters().getOrDefault("realm", context.execution().targetRealmId());
                    String project = context.parameters().getOrDefault("project", "");
                    if (realm.isBlank() || project.isBlank()) {
                        return RegistryExecutionResult.failure("realm and project are required");
                    }
                    service.startRealm(realm, project, context.execution().actor());
                    return RegistryExecutionResult.ok(api.serverIdentity().offeringSingular() + " "
                            + api.serverIdentity().realmSingular().toLowerCase(java.util.Locale.ROOT)
                            + " project started.");
                });
        register(api, "elarion:offering_start_global_project",
                "Starts a global offering project instance.",
                context -> {
                    String project = context.parameters().getOrDefault("project", "");
                    if (project.isBlank()) return RegistryExecutionResult.failure("project is required");
                    service.startGlobal(project, context.execution().actor());
                    return RegistryExecutionResult.ok(api.serverIdentity().offeringSingular() + " global project started.");
                });
        register(api, "elarion:offering_complete_project",
                "Forces an offering project instance to complete.",
                context -> {
                    String instance = context.parameters().getOrDefault("instance", "");
                    if (instance.isBlank()) return RegistryExecutionResult.failure("instance is required");
                    service.complete(instance, context.execution().actor(), true);
                    return RegistryExecutionResult.ok(api.serverIdentity().offeringSingular() + " project completed.");
                });
        register(api, "elarion:offering_set_display_name",
                "Overrides an offering project instance display name.",
                context -> {
                    String instance = context.parameters().getOrDefault("instance", "");
                    String title = context.parameters().getOrDefault("title",
                            context.parameters().getOrDefault("display-name", ""));
                    if (instance.isBlank() || title.isBlank()) {
                        return RegistryExecutionResult.failure("instance and title are required");
                    }
                    service.setDisplayNameOverride(instance, title, context.execution().actor());
                    return RegistryExecutionResult.ok(api.serverIdentity().shrineOfFoundation()
                            + " display name updated.");
                });
    }

    static String shrineTitle(
            OfferingInstance instance,
            OfferingProjectDefinition project,
            OfferingProjectLevel level
    ) {
        if (!instance.displayNameOverride().isBlank()) {
            return instance.displayNameOverride();
        }
        if (!level.presentation().levelText().isBlank()) {
            return level.presentation().levelText();
        }
        if (!level.displayName().isBlank()) {
            return level.displayName();
        }
        return project.displayName();
    }

    private static void register(
            ElarionApi api,
            String id,
            String description,
            panetina.elarion.core.registry.ActionHandler handler
    ) {
        api.registries().actions().register(new ActionType(id, "elarion_offerings", description));
        api.registries().registerActionHandler(id, handler);
    }

    private static long amount(String raw) {
        try {
            long parsed = Long.parseLong(raw);
            return Math.max(1L, parsed);
        } catch (NumberFormatException exception) {
            return 1L;
        }
    }
}
