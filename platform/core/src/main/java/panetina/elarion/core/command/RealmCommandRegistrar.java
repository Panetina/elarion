package panetina.elarion.core.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.RealmDecision;
import panetina.elarion.core.model.RealmDecisionType;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.RealmRelationship;

import java.util.List;
import java.util.Locale;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static panetina.elarion.core.command.CommandSupport.displayCitizen;
import static panetina.elarion.core.command.CommandSupport.suggestDiplomacyRealms;
import static panetina.elarion.core.command.CommandSupport.validateDiplomacyRealms;
import static panetina.elarion.core.command.CommandSupport.value;

final class RealmCommandRegistrar {
    private RealmCommandRegistrar() {
    }

    static LiteralArgumentBuilder<ServerCommandSource> register(
            ElarionApi api,
            CommandRegistryAccess registryAccess
    ) {
        return literal("realm")
                .then(literal("add")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("realm", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            api.realms().all().forEach(value -> builder.suggest(value.id()));
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            String id = StringArgumentType.getString(context, "realm");
                                            if (!api.realms().assign(player, id)) {
                                                context.getSource().sendError(Text.literal("Unknown realm: " + id));
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Assigned " + player.getGameProfile().getName()
                                                            + " to " + id), true);
                                            return 1;
                                        }))))
                .then(literal("remove")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    api.realms().remove(player);
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("Removed " + player.getGameProfile().getName()
                                                    + " from their realm"), true);
                                    return 1;
                                })))
                .then(literal("list").executes(context -> {
                    String values = api.realms().all().stream()
                            .map(RealmDefinition::id)
                            .sorted()
                            .reduce((left, right) -> left + ", " + right)
                            .orElse("(none)");
                    CommandOutput.header(context.getSource(), "Realms");
                    CommandOutput.kv(context.getSource(), "Available", values);
                    return 1;
                }))
                .then(literal("reward")
                        .then(argument("realm", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    api.realms().all().forEach(value -> builder.suggest(value.id()));
                                    return builder.buildFuture();
                                })
                                .then(argument("reward", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            api.rewards().rewardIds().forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String realm = StringArgumentType.getString(context, "realm");
                                            String reward = StringArgumentType.getString(context, "reward");
                                            if (!api.realmDeliveries().rewardRealm(realm, reward,
                                                    context.getSource().getEntity() instanceof ServerPlayerEntity actor
                                                            ? actor.getUuid()
                                                            : null)) {
                                                context.getSource().sendError(Text.literal("Could not deliver reward."));
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Queued/delivered reward " + reward
                                                            + " to " + realm), true);
                                            return 1;
                                        }))))
                .then(literal("give")
                        .then(argument("realm", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    api.realms().all().forEach(value -> builder.suggest(value.id()));
                                    return builder.buildFuture();
                                })
                                .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                        .then(argument("count", IntegerArgumentType.integer(1))
                                                .executes(context -> {
                                                    String realm = StringArgumentType.getString(context, "realm");
                                                    String item = Registries.ITEM.getId(
                                                            ItemStackArgumentType.getItemStackArgument(context, "item")
                                                                    .getItem()).toString();
                                                    int count = IntegerArgumentType.getInteger(context, "count");
                                                    if (!api.realmDeliveries().giveItemRealm(realm, item, count,
                                                            context.getSource().getEntity() instanceof ServerPlayerEntity actor
                                                                    ? actor.getUuid()
                                                                    : null)) {
                                                        context.getSource().sendError(Text.literal("Could not give item reward."));
                                                        return 0;
                                                    }
                                                    context.getSource().sendFeedback(
                                                            () -> Text.literal("Queued/delivered " + count + "x " + item
                                                                    + " to " + realm), true);
                                                    return 1;
                                                })))))
                .then(literal("announce")
                        .then(argument("realm", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    api.realms().all().forEach(value -> builder.suggest(value.id()));
                                    return builder.buildFuture();
                                })
                                .then(argument("message", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String realm = StringArgumentType.getString(context, "realm");
                                            if (!api.realmDeliveries().announceRealm(realm,
                                                    StringArgumentType.getString(context, "message"),
                                                    context.getSource().getEntity() instanceof ServerPlayerEntity actor
                                                            ? actor.getUuid()
                                                            : null)) {
                                                context.getSource().sendError(Text.literal("Could not send announcement."));
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Sent announcement to " + realm), true);
                                            return 1;
                                        }))))
                .then(literal("mail")
                        .then(argument("realm", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    api.realms().all().forEach(value -> builder.suggest(value.id()));
                                    return builder.buildFuture();
                                })
                                .then(argument("message", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            String realm = StringArgumentType.getString(context, "realm");
                                            if (!api.realmDeliveries().mailRealm(realm,
                                                    StringArgumentType.getString(context, "message"),
                                                    context.getSource().getEntity() instanceof ServerPlayerEntity actor
                                                            ? actor.getUuid()
                                                            : null)) {
                                                context.getSource().sendError(Text.literal("Could not send mail."));
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Sent mail to " + realm), true);
                                            return 1;
                                        }))))
                .then(leaderCommands(api))
                .then(relationshipCommands(api));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> leaderCommands(ElarionApi api) {
        return literal("leader")
                .then(literal("set")
                        .then(argument("realm", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    api.realms().all().forEach(value -> builder.suggest(value.id()));
                                    return builder.buildFuture();
                                })
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(context -> {
                                            String realm = StringArgumentType.getString(context, "realm");
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            api.citizens().getOrCreate(player);
                                            if (!api.governance().setLeader(realm, player.getUuid(),
                                                    context.getSource().getEntity() instanceof ServerPlayerEntity actor
                                                            ? actor.getUuid()
                                                            : null)) {
                                                context.getSource().sendError(Text.literal("Could not set leader for realm: " + realm));
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("Set " + player.getGameProfile().getName()
                                                            + " as leader of " + realm), true);
                                            return 1;
                                        }))))
                .then(literal("get")
                        .then(argument("realm", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    api.realms().all().forEach(value -> builder.suggest(value.id()));
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String realm = StringArgumentType.getString(context, "realm");
                                    String leader = api.governance().leader(realm)
                                            .map(uuid -> displayCitizen(api, uuid))
                                            .orElse("(none)");
                                    CommandOutput.header(context.getSource(), "Realm Leader");
                                    CommandOutput.kv(context.getSource(), "Realm", realm);
                                    CommandOutput.kv(context.getSource(), "Leader", leader);
                                    return 1;
                                })));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> relationshipCommands(ElarionApi api) {
        return literal("relationship")
                .then(literal("get")
                        .then(argument("first", StringArgumentType.word())
                                .suggests((context, builder) -> suggestDiplomacyRealms(api, builder))
                                .then(argument("second", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestDiplomacyRealms(api, builder))
                                        .executes(context -> {
                                            String first = StringArgumentType.getString(context, "first");
                                            String second = StringArgumentType.getString(context, "second");
                                            if (!validateDiplomacyRealms(context.getSource(), api, first, second)) {
                                                return 0;
                                            }
                                            RealmRelationship relationship = api.governance().relationship(first, second);
                                            CommandOutput.header(context.getSource(), "Realm Relationship");
                                            CommandOutput.kv(context.getSource(), "First Realm", first);
                                            CommandOutput.kv(context.getSource(), "Second Realm", second);
                                            CommandOutput.kv(context.getSource(), "Relationship", relationship);
                                            return 1;
                                        }))))
                .then(literal("set")
                        .then(argument("first", StringArgumentType.word())
                                .suggests((context, builder) -> suggestDiplomacyRealms(api, builder))
                                .then(argument("second", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestDiplomacyRealms(api, builder))
                                        .then(argument("relationship", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    for (RealmRelationship relationship : RealmRelationship.values()) {
                                                        if (relationship == RealmRelationship.HIDDEN) continue;
                                                        builder.suggest(relationship.name().toLowerCase(Locale.ROOT));
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> setRelationship(context.getSource(), api,
                                                        StringArgumentType.getString(context, "first"),
                                                        StringArgumentType.getString(context, "second"),
                                                        StringArgumentType.getString(context, "relationship")))))))
                .then(literal("decision")
                        .then(literal("list").executes(context -> {
                            List<RealmDecision> pending = api.governance().pending();
                            if (pending.isEmpty()) {
                                CommandOutput.empty(context.getSource(), "No pending realm decisions.");
                                return 1;
                            }
                            CommandOutput.header(context.getSource(), "Pending Realm Decisions");
                            pending.forEach(decision -> context.getSource().sendFeedback(
                                    () -> Text.literal(" - " + decision.id()
                                            + " | " + decision.type()
                                            + " | " + decision.declaringRealmId()
                                            + " -> " + value(decision.receivingRealmId())), false));
                            return pending.size();
                        }))
                        .then(literal("propose")
                                .then(argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            for (RealmDecisionType type : RealmDecisionType.values()) {
                                                builder.suggest(type.name().toLowerCase(Locale.ROOT));
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(argument("declaring", StringArgumentType.word())
                                                .suggests((context, builder) -> suggestDiplomacyRealms(api, builder))
                                                .then(argument("receiving", StringArgumentType.word())
                                                        .suggests((context, builder) -> suggestDiplomacyRealms(api, builder))
                                                        .executes(context -> proposeDecision(
                                                                context.getSource(),
                                                                api,
                                                                StringArgumentType.getString(context, "type"),
                                                                StringArgumentType.getString(context, "declaring"),
                                                                StringArgumentType.getString(context, "receiving"))))))));
    }

    private static int setRelationship(
            ServerCommandSource source,
            ElarionApi api,
            String first,
            String second,
            String relationshipName
    ) {
        RealmRelationship relationship;
        try {
            relationship = RealmRelationship.valueOf(relationshipName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            source.sendError(Text.literal("Unknown relationship."));
            return 0;
        }
        if (relationship == RealmRelationship.HIDDEN) {
            source.sendError(Text.literal("HIDDEN is a self-Realm state, not a relationship."));
            return 0;
        }
        if (!api.governance().setRelationship(first, second, relationship,
                source.getEntity() instanceof ServerPlayerEntity actor ? actor.getUuid() : null,
                "admin-command")) {
            source.sendError(Text.literal("Could not set relationship."));
            return 0;
        }
        CommandOutput.success(source, "Realm relationship updated.", true);
        CommandOutput.kv(source, "First Realm", first);
        CommandOutput.kv(source, "Second Realm", second);
        CommandOutput.kv(source, "Relationship", relationship);
        return 1;
    }

    private static int proposeDecision(
            ServerCommandSource source,
            ElarionApi api,
            String typeName,
            String declaring,
            String receiving
    ) {
        if (!validateDiplomacyRealms(source, api, declaring, receiving)) {
            return 0;
        }
        RealmDecisionType type;
        try {
            type = RealmDecisionType.valueOf(typeName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            source.sendError(Text.literal("Unknown decision type."));
            return 0;
        }
        RealmDecision decision;
        try {
            decision = api.governance().propose(
                    type,
                    declaring,
                    receiving,
                    source.getEntity() instanceof ServerPlayerEntity actor ? actor.getUuid() : null);
        } catch (IllegalArgumentException exception) {
            source.sendError(Text.literal(exception.getMessage()));
            return 0;
        }
        CommandOutput.success(source, "Realm decision created.", true);
        CommandOutput.kv(source, "ID", decision.id());
        CommandOutput.kv(source, "Type", decision.type());
        CommandOutput.kv(source, "Declaring Realm", decision.declaringRealmId());
        CommandOutput.kv(source, "Receiving Realm", value(decision.receivingRealmId()));
        return 1;
    }
}
