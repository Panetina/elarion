package panetina.elarion.addons.quests.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import panetina.elarion.addons.npcs.api.ElarionNpcApi;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.addons.quests.model.QuestDefinition;
import panetina.elarion.addons.quests.service.QuestDefinitionService;
import panetina.elarion.addons.quests.service.QuestStateService;
import panetina.elarion.addons.quests.storage.QuestActorBindingScope;
import panetina.elarion.addons.quests.storage.QuestlineState;
import panetina.elarion.core.command.CommandOutput;

public final class QuestCommands {
    private QuestCommands() {
    }

    public static LiteralArgumentBuilder<ServerCommandSource> create(
            QuestDefinitionService definitions,
            QuestStateService states
    ) {
        LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("quest")
                .requires(source -> source.hasPermissionLevel(4));
        root.then(CommandManager.literal("reload").executes(ctx -> run(ctx.getSource(), () -> {
                    definitions.load();
                    CommandOutput.success(ctx.getSource(), "Quest definitions reloaded.", false);
                })));
        root.then(CommandManager.literal("list").executes(ctx -> run(ctx.getSource(), () -> {
                    CommandOutput.header(ctx.getSource(), "Questlines");
                    for (QuestDefinition quest : definitions.all()) {
                        CommandOutput.bullet(ctx.getSource(), quest.id() + " - " + quest.displayName());
                    }
                })));
        root.then(CommandManager.literal("inspect")
                        .then(CommandManager.argument("quest", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        definitions.all().stream().map(QuestDefinition::id), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> inspect(
                                        ctx.getSource(), definitions.require(StringArgumentType.getString(ctx, "quest")))))));
        root.then(CommandManager.literal("state")
                        .then(CommandManager.argument("quest", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        definitions.all().stream().map(QuestDefinition::id), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> listState(
                                        ctx.getSource(), states, StringArgumentType.getString(ctx, "quest"))))
                                .then(CommandManager.argument("scope-key", StringArgumentType.string())
                                        .executes(ctx -> run(ctx.getSource(), () -> showState(
                                                ctx.getSource(), states,
                                                StringArgumentType.getString(ctx, "quest"),
                                                StringArgumentType.getString(ctx, "scope-key")))))));
        root.then(CommandManager.literal("reset")
                        .then(CommandManager.argument("quest", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        definitions.all().stream().map(QuestDefinition::id), builder))
                                .then(CommandManager.argument("scope-key", StringArgumentType.string())
                                        .executes(ctx -> run(ctx.getSource(), () -> {
                                            String quest = StringArgumentType.getString(ctx, "quest");
                                            String scope = StringArgumentType.getString(ctx, "scope-key");
                                            int removed = states.reset(quest, scope);
                                            CommandOutput.success(ctx.getSource(),
                                                    "Reset quest state for " + quest + " at " + scope
                                                            + " (" + removed + " runtime record).", true);
                                        })))));
        root.then(CommandManager.literal("bind")
                        .then(CommandManager.literal("actor")
                                .then(CommandManager.argument("quest", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                definitions.all().stream().map(QuestDefinition::id), builder))
                                        .then(CommandManager.argument("scope-key", StringArgumentType.string())
                                                .then(CommandManager.argument("actor", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> actorSuggestions(
                                                                definitions, StringArgumentType.getString(ctx, "quest"), builder))
                                                        .then(CommandManager.argument("npc", StringArgumentType.word())
                                                                .executes(ctx -> run(ctx.getSource(), () -> bindActor(
                                                                        ctx.getSource(),
                                                                        definitions,
                                                                        states,
                                                                        StringArgumentType.getString(ctx, "quest"),
                                                                        StringArgumentType.getString(ctx, "scope-key"),
                                                                        StringArgumentType.getString(ctx, "actor"),
                                                                        StringArgumentType.getString(ctx, "npc"))))))))));
        root.then(CommandManager.literal("unbind")
                        .then(CommandManager.literal("actor")
                                .then(CommandManager.argument("quest", StringArgumentType.word())
                                        .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                definitions.all().stream().map(QuestDefinition::id), builder))
                                        .then(CommandManager.argument("scope-key", StringArgumentType.string())
                                                .then(CommandManager.argument("actor", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> actorSuggestions(
                                                                definitions, StringArgumentType.getString(ctx, "quest"), builder))
                                                        .executes(ctx -> run(ctx.getSource(), () -> unbindActor(
                                                                ctx.getSource(),
                                                                definitions,
                                                                states,
                                                                StringArgumentType.getString(ctx, "quest"),
                                                                StringArgumentType.getString(ctx, "scope-key"),
                                                                StringArgumentType.getString(ctx, "actor")))))))));
        root.then(CommandManager.literal("bindings")
                        .then(CommandManager.argument("quest", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        definitions.all().stream().map(QuestDefinition::id), builder))
                                .then(CommandManager.argument("scope-key", StringArgumentType.string())
                                        .executes(ctx -> run(ctx.getSource(), () -> showBindings(
                                                ctx.getSource(),
                                                definitions,
                                                states,
                                                StringArgumentType.getString(ctx, "quest"),
                                                StringArgumentType.getString(ctx, "scope-key")))))));
        root.then(CommandManager.literal("validate")
                        .then(CommandManager.argument("quest", StringArgumentType.word())
                                .suggests((ctx, builder) -> CommandSource.suggestMatching(
                                        java.util.stream.Stream.concat(
                                                definitions.all().stream().map(QuestDefinition::id),
                                                java.util.stream.Stream.of("all")), builder))
                                .executes(ctx -> run(ctx.getSource(), () -> validate(
                                        ctx.getSource(), definitions, StringArgumentType.getString(ctx, "quest"))))));
        return root;
    }

    private static void inspect(ServerCommandSource source, QuestDefinition quest) {
        CommandOutput.header(source, quest.displayName());
        CommandOutput.kv(source, "ID", quest.id());
        CommandOutput.kv(source, "Scope", quest.scope());
        CommandOutput.kv(source, "Root stage", quest.rootStage());
        CommandOutput.kv(source, "Stages", quest.stages().size());
        CommandOutput.kv(source, "Variables", quest.variables().size());
        CommandOutput.kv(source, "Evidence", quest.evidence().size());
        CommandOutput.kv(source, "Endings", quest.endings().size());
        CommandOutput.kv(source, "Actors", quest.actors().size());
        CommandOutput.kv(source, "Conditions", quest.conditions().size());
        CommandOutput.kv(source, "Consequences", quest.consequences().size());
        CommandOutput.section(source, "Stage IDs");
        quest.stages().keySet().forEach(stage -> CommandOutput.bullet(source, stage));
    }

    private static void listState(ServerCommandSource source, QuestStateService states, String questId) {
        CommandOutput.header(source, "Quest State " + questId);
        var lines = states.questlines(questId);
        if (lines.isEmpty()) {
            CommandOutput.empty(source, "No active state for this quest.");
            return;
        }
        for (QuestlineState line : lines) {
            CommandOutput.bullet(source, line.scopeKey + " stage=" + line.stageId
                    + (line.endingId.isBlank() ? "" : " ending=" + line.endingId));
        }
    }

    private static void showState(ServerCommandSource source, QuestStateService states, String questId, String scopeKey) {
        QuestlineState line = states.findLine(questId, scopeKey)
                .orElseThrow(() -> new IllegalArgumentException("No quest state for " + questId + " at " + scopeKey));
        CommandOutput.header(source, questId + " " + scopeKey);
        CommandOutput.kv(source, "Stage", line.stageId);
        CommandOutput.kv(source, "Ending", line.endingId.isBlank() ? "-" : line.endingId);
        CommandOutput.kv(source, "Flags", line.flags.isEmpty() ? "-" : String.join(", ", line.flags));
        CommandOutput.kv(source, "Evidence", line.evidence.isEmpty() ? "-" : String.join(", ", line.evidence));
        CommandOutput.section(source, "Variables");
        line.variables.forEach((key, value) -> CommandOutput.kv(source, key, value));
    }

    private static void bindActor(
            ServerCommandSource source,
            QuestDefinitionService definitions,
            QuestStateService states,
            String questId,
            String scopeKey,
            String actor,
            String npcIdOrHandle
    ) {
        QuestDefinition quest = definitions.require(questId);
        PlacedNpcRecord npc = ElarionNpcApi.get().placements().find(npcIdOrHandle)
                .orElseThrow(() -> new IllegalArgumentException("Unknown placed NPC " + npcIdOrHandle));
        states.bindActor(quest, scopeKey, actor, npc.id(), npc.commandId(), npc.definitionId());
        CommandOutput.success(source, "Bound actor " + actor + " to NPC " + npc.commandId() + ".", true);
    }

    private static void unbindActor(
            ServerCommandSource source,
            QuestDefinitionService definitions,
            QuestStateService states,
            String questId,
            String scopeKey,
            String actor
    ) {
        QuestDefinition quest = definitions.require(questId);
        boolean removed = states.unbindActor(quest, scopeKey, actor);
        CommandOutput.success(source, removed
                ? "Unbound actor " + actor + "."
                : "No binding existed for actor " + actor + ".", true);
    }

    private static void showBindings(
            ServerCommandSource source,
            QuestDefinitionService definitions,
            QuestStateService states,
            String questId,
            String scopeKey
    ) {
        QuestDefinition quest = definitions.require(questId);
        CommandOutput.header(source, "Quest Actor Bindings");
        CommandOutput.kv(source, "Quest", quest.id());
        CommandOutput.kv(source, "Scope", scopeKey);
        QuestActorBindingScope bindings = states.actorBindings(quest.id(), scopeKey).orElse(null);
        if (bindings == null || bindings.actors.isEmpty()) {
            CommandOutput.empty(source, "No actor bindings.");
            return;
        }
        bindings.actors.forEach((actor, binding) -> CommandOutput.bullet(source,
                actor + " -> " + (binding.handle.isBlank() ? binding.placedNpcId : binding.handle)
                        + " (" + binding.placedNpcId + ")"));
    }

    private static void validate(ServerCommandSource source, QuestDefinitionService definitions, String questId) {
        if ("all".equalsIgnoreCase(questId)) {
            CommandOutput.success(source, "All loaded quest packages are valid.", false);
            CommandOutput.kv(source, "Questlines", definitions.all().size());
            return;
        }
        QuestDefinition quest = definitions.require(questId);
        CommandOutput.success(source, "Quest package is valid: " + quest.id(), false);
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> actorSuggestions(
            QuestDefinitionService definitions,
            String questId,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder
    ) {
        return CommandSource.suggestMatching(
                definitions.find(questId).stream().flatMap(quest -> quest.actors().keySet().stream()), builder);
    }

    private static int run(ServerCommandSource source, Runnable runnable) {
        try {
            runnable.run();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            source.sendFeedback(() -> Text.literal(exception.getMessage() == null
                    ? "Quest command failed." : exception.getMessage()).formatted(Formatting.RED), false);
        }
        return Command.SINGLE_SUCCESS;
    }
}
