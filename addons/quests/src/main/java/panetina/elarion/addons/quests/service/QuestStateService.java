package panetina.elarion.addons.quests.service;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import panetina.elarion.addons.offerings.api.ElarionOfferingsApi;
import panetina.elarion.addons.quests.model.QuestDefinition;
import panetina.elarion.addons.quests.model.QuestEndingDefinition;
import panetina.elarion.addons.quests.model.QuestVariableDefinition;
import panetina.elarion.addons.quests.model.QuestVariableScope;
import panetina.elarion.addons.quests.storage.QuestActorBindingRecord;
import panetina.elarion.addons.quests.storage.QuestActorBindingScope;
import panetina.elarion.addons.quests.storage.QuestPlayerState;
import panetina.elarion.addons.quests.storage.QuestRuntimeState;
import panetina.elarion.addons.quests.storage.QuestScheduledConsequence;
import panetina.elarion.addons.quests.storage.QuestStorage;
import panetina.elarion.addons.quests.storage.QuestlineState;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.ElarionDomainEvent;
import panetina.elarion.core.model.ElarionNotificationAction;
import panetina.elarion.core.model.ElarionNotificationCategory;
import panetina.elarion.core.registry.ActionContext;
import panetina.elarion.core.registry.RegistryExecutionContext;
import panetina.elarion.core.registry.RegistryExecutionResult;
import panetina.elarion.core.service.ElarionNotificationService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class QuestStateService {
    public static final String COMPLETED_QUESTS_STAT = "quests_completed";
    private static final int CONSEQUENCE_INTERVAL_TICKS = 20;
    private static final int MAX_CONSEQUENCES_PER_INTERVAL = 16;
    private static final Set<String> SCHEDULE_RESERVED_KEYS = Set.of(
            "quest", "scope", "scope-key", "realm", "world", "player", "action",
            "delay-seconds", "delay-ticks", "delay-millis");

    private final Logger logger;
    private final ElarionApi api;
    private final QuestDefinitionService definitions;
    private final QuestStorage storage;
    private QuestRuntimeState state = new QuestRuntimeState();
    private MinecraftServer server;
    private int ticks;

    public QuestStateService(
            Logger logger,
            ElarionApi api,
            QuestDefinitionService definitions,
            QuestStorage storage
    ) {
        this.logger = logger;
        this.api = api;
        this.definitions = definitions;
        this.storage = storage;
    }

    public synchronized void bind(MinecraftServer server) {
        this.server = server;
        state = storage.load(server);
    }

    public synchronized void tick(MinecraftServer server) {
        if (this.server != server || state.scheduled.isEmpty()) return;
        ticks++;
        if (ticks % CONSEQUENCE_INTERVAL_TICKS != 0) return;
        processConsequences(System.currentTimeMillis());
    }

    public synchronized void save() {
        if (server != null) storage.save(server, state);
    }

    public synchronized Optional<QuestlineState> findLine(String questId, String scopeKey) {
        return Optional.ofNullable(state.questlines.get(lineKey(questId, scopeKey))).map(QuestlineState::copy);
    }

    public synchronized List<QuestlineState> questlines(String questId) {
        return state.questlines.values().stream()
                .filter(line -> questId == null || questId.isBlank() || questId.equals(line.questId))
                .map(QuestlineState::copy)
                .sorted(Comparator.comparing(line -> line.scopeKey))
                .toList();
    }

    public synchronized int reset(String questId, String scopeKey) {
        int removed = 0;
        String key = lineKey(questId, scopeKey);
        if (state.questlines.remove(key) != null) removed++;
        state.players.entrySet().removeIf(entry -> {
            QuestPlayerState player = entry.getValue();
            return questId.equals(player.questId) && scopeKey.equals(player.scopeKey);
        });
        if (state.actorBindings.remove(lineKey(questId, scopeKey)) != null) removed++;
        state.scheduled.removeIf(entry -> questId.equals(entry.questId) && scopeKey.equals(entry.scopeKey));
        save();
        return removed;
    }

    public synchronized QuestActorBindingScope bindActor(
            QuestDefinition quest,
            String scopeKey,
            String actor,
            UUID placedNpcId,
            String handle,
            String definitionId
    ) {
        if (!quest.actors().containsKey(actor)) {
            throw new IllegalArgumentException("Unknown quest actor " + actor);
        }
        if (placedNpcId == null) {
            throw new IllegalArgumentException("Placed NPC UUID is required.");
        }
        QuestActorBindingScope bindings = ensureBindings(quest.id(), scopeKey);
        bindings.actors.put(actor, new QuestActorBindingRecord(
                actor, placedNpcId, handle, definitionId, System.currentTimeMillis()));
        bindings.updatedAt = System.currentTimeMillis();
        save();
        emit("actor-bound", null, quest.id(), scopeKey,
                Map.of("actor", actor, "npc", placedNpcId.toString(), "definition", safe(definitionId)));
        return bindings.copy();
    }

    public synchronized boolean unbindActor(QuestDefinition quest, String scopeKey, String actor) {
        if (!quest.actors().containsKey(actor)) {
            throw new IllegalArgumentException("Unknown quest actor " + actor);
        }
        QuestActorBindingScope bindings = state.actorBindings.get(lineKey(quest.id(), scopeKey));
        if (bindings == null) return false;
        boolean removed = bindings.actors.remove(actor) != null;
        if (removed) {
            bindings.updatedAt = System.currentTimeMillis();
            if (bindings.actors.isEmpty()) state.actorBindings.remove(lineKey(quest.id(), scopeKey));
            save();
            emit("actor-unbound", null, quest.id(), scopeKey, Map.of("actor", actor));
        }
        return removed;
    }

    public synchronized Optional<QuestActorBindingScope> actorBindings(String questId, String scopeKey) {
        return Optional.ofNullable(state.actorBindings.get(lineKey(questId, scopeKey)))
                .map(QuestActorBindingScope::copy);
    }

    public synchronized RegistryExecutionResult start(ActionContext context) {
        QuestDefinition quest = definition(context);
        String scopeKey = resolveScopeKey(quest, context.execution(), context.parameters());
        ensureLine(quest, scopeKey);
        if (context.execution().actorId() != null) {
            ensurePlayer(quest, scopeKey, context.execution().actorId());
        }
        save();
        emit("started", context.execution().actorId(), quest.id(), scopeKey, Map.of());
        return RegistryExecutionResult.ok("Questline started: " + quest.displayName());
    }

    public synchronized RegistryExecutionResult setStage(ActionContext context) {
        QuestDefinition quest = definition(context);
        String stage = context.parameters().getOrDefault("stage", "");
        if (!quest.stages().containsKey(stage)) return RegistryExecutionResult.failure("Unknown quest stage " + stage);
        String scopeKey = resolveScopeKey(quest, context.execution(), context.parameters());
        QuestlineState line = ensureLine(quest, scopeKey);
        line.stageId = stage;
        line.updatedAt = System.currentTimeMillis();
        save();
        emit("stage-changed", context.execution().actorId(), quest.id(), scopeKey, Map.of("stage", stage));
        return RegistryExecutionResult.ok("Quest stage set to " + stage + ".");
    }

    public synchronized RegistryExecutionResult setFlag(ActionContext context) {
        QuestDefinition quest = definition(context);
        String flag = context.parameters().getOrDefault("flag", context.parameters().getOrDefault("id", ""));
        if (flag.isBlank()) return RegistryExecutionResult.failure("flag is required");
        String scopeKey = resolveScopeKey(quest, context.execution(), context.parameters());
        boolean enabled = parseBoolean(context.parameters().getOrDefault("enabled",
                context.parameters().getOrDefault("value", "true")));
        if (playerTarget(context.parameters(), null)) {
            QuestPlayerState player = ensureActorPlayer(quest, scopeKey, context.execution());
            if (enabled) player.flags.add(flag);
            else player.flags.remove(flag);
            player.updatedAt = System.currentTimeMillis();
        } else {
            QuestlineState line = ensureLine(quest, scopeKey);
            if (enabled) line.flags.add(flag);
            else line.flags.remove(flag);
            line.updatedAt = System.currentTimeMillis();
        }
        save();
        emit("flag-changed", context.execution().actorId(), quest.id(), scopeKey,
                Map.of("flag", flag, "enabled", Boolean.toString(enabled)));
        return RegistryExecutionResult.ok();
    }

    public synchronized RegistryExecutionResult setVariable(ActionContext context) {
        return changeVariable(context, false);
    }

    public synchronized RegistryExecutionResult addVariable(ActionContext context) {
        return changeVariable(context, true);
    }

    public synchronized RegistryExecutionResult collectEvidence(ActionContext context) {
        QuestDefinition quest = definition(context);
        String evidence = context.parameters().getOrDefault("evidence", context.parameters().getOrDefault("id", ""));
        if (evidence.isBlank()) return RegistryExecutionResult.failure("evidence is required");
        if (!quest.evidence().isEmpty() && !quest.evidence().containsKey(evidence)) {
            return RegistryExecutionResult.failure("Unknown quest evidence " + evidence);
        }
        String scopeKey = resolveScopeKey(quest, context.execution(), context.parameters());
        QuestlineState line = ensureLine(quest, scopeKey);
        line.evidence.add(evidence);
        line.updatedAt = System.currentTimeMillis();
        if (context.execution().actorId() != null) {
            QuestPlayerState player = ensurePlayer(quest, scopeKey, context.execution().actorId());
            player.evidenceSeen.add(evidence);
            player.updatedAt = System.currentTimeMillis();
        }
        save();
        emit("evidence-collected", context.execution().actorId(), quest.id(), scopeKey, Map.of("evidence", evidence));
        return RegistryExecutionResult.ok("Evidence collected: " + evidence + ".");
    }

    public synchronized RegistryExecutionResult lockEnding(ActionContext context) {
        QuestDefinition quest = definition(context);
        String ending = context.parameters().getOrDefault("ending", context.parameters().getOrDefault("id", ""));
        if (ending.isBlank()) return RegistryExecutionResult.failure("ending is required");
        if (!quest.endings().containsKey(ending)) return RegistryExecutionResult.failure("Unknown quest ending " + ending);
        String scopeKey = resolveScopeKey(quest, context.execution(), context.parameters());
        QuestlineState line = ensureLine(quest, scopeKey);
        String previousEnding = line.endingId;
        line.endingId = ending;
        line.variables.put("final_ending", ending);
        line.updatedAt = System.currentTimeMillis();
        save();
        if ((previousEnding == null || previousEnding.isBlank()) && context.execution().actorId() != null) {
            incrementCompletedQuests(context.execution().actorId());
        }
        emit("ending-locked", context.execution().actorId(), quest.id(), scopeKey, Map.of("ending", ending));
        return RegistryExecutionResult.ok("Quest ending locked: " + quest.endings().get(ending).displayName());
    }

    private void incrementCompletedQuests(UUID playerId) {
        api.playerStats().increment(playerId, COMPLETED_QUESTS_STAT, 1L);
    }

    public synchronized RegistryExecutionResult scheduleConsequence(ActionContext context) {
        QuestDefinition quest = definition(context);
        String action = context.parameters().getOrDefault("action", "");
        if (action.isBlank()) return RegistryExecutionResult.failure("action is required");
        if (!api.registries().actions().contains(action)) {
            return RegistryExecutionResult.failure("Unknown scheduled action " + action);
        }
        String scopeKey = resolveScopeKey(quest, context.execution(), context.parameters());
        long dueAt = System.currentTimeMillis() + delayMillis(context.parameters());
        Map<String, String> parameters = new LinkedHashMap<>();
        context.parameters().forEach((key, value) -> {
            if (!SCHEDULE_RESERVED_KEYS.contains(key)) parameters.put(key, value);
        });
        parameters.putIfAbsent("quest", quest.id());
        parameters.putIfAbsent("scope-key", scopeKey);
        state.scheduled.add(new QuestScheduledConsequence(
                "quest_" + Long.toUnsignedString(System.nanoTime(), 36),
                quest.id(),
                scopeKey,
                context.execution().actorId(),
                dueAt,
                action,
                parameters));
        save();
        emit("consequence-scheduled", context.execution().actorId(), quest.id(), scopeKey,
                Map.of("action", action, "dueAt", Long.toString(dueAt)));
        return RegistryExecutionResult.ok("Quest consequence scheduled.");
    }

    public synchronized RegistryExecutionResult notify(ActionContext context) {
        QuestDefinition quest = definition(context);
        String scopeKey = resolveScopeKey(quest, context.execution(), context.parameters());
        String audience = context.parameters().getOrDefault("audience", "player").toLowerCase(Locale.ROOT);
        String title = context.parameters().getOrDefault("title", quest.displayName());
        String body = context.parameters().getOrDefault("body", "");
        String status = context.parameters().getOrDefault("status", "Quest");
        String icon = context.parameters().getOrDefault("icon", "item:minecraft:paper");
        Map<String, String> metadata = Map.of("quest", quest.id(), "scopeKey", scopeKey);
        List<ElarionNotificationAction> actions = List.of(new ElarionNotificationAction(
                ElarionNotificationService.DISMISS, "Dismiss", true));
        switch (audience) {
            case "realm" -> {
                String realm = realmId(scopeKey, context.execution(), context.parameters());
                if (realm.isBlank()) return RegistryExecutionResult.failure("realm notification needs a Realm");
                api.notifications().publishRealm(realm, ElarionNotificationCategory.QUEST,
                        "elarion_quests", "quest-notification", quest.id() + ":" + scopeKey + ":" + title,
                        title, body, status, icon, actions, metadata, api.notifications().defaultExpiry());
            }
            case "world" -> api.notifications().publishWorld("elarion_quests", "quest-notification",
                    quest.id() + ":" + scopeKey + ":" + title, title, body, status, icon, actions,
                    metadata, api.notifications().defaultExpiry());
            default -> {
                UUID recipient = context.execution().actorId();
                if (recipient == null) return RegistryExecutionResult.failure("player notification needs an actor");
                api.notifications().publishPersonal(recipient, ElarionNotificationCategory.QUEST,
                        "elarion_quests", "quest-notification", quest.id() + ":" + scopeKey + ":" + title,
                        title, body, status, icon, actions, metadata, api.notifications().defaultExpiry());
            }
        }
        return RegistryExecutionResult.ok();
    }

    public synchronized RegistryExecutionResult setShrineDisplay(ActionContext context) {
        QuestDefinition quest = definition(context);
        String instance = context.parameters().getOrDefault("instance", "");
        if (instance.isBlank()) return RegistryExecutionResult.failure("instance is required");
        String title = context.parameters().getOrDefault("title", "");
        String ending = context.parameters().getOrDefault("ending", "");
        if (title.isBlank() && !ending.isBlank()) {
            title = quest.endings().getOrDefault(ending, new QuestEndingDefinition("", "", "", Map.of()))
                    .shrineDisplayNames().getOrDefault("default", "");
        }
        if (title.isBlank()) return RegistryExecutionResult.failure("title is required");
        ElarionOfferingsApi.get().setDisplayNameOverride(instance, title, context.execution().actor());
        return RegistryExecutionResult.ok("Shrine display name set to " + title + ".");
    }

    public synchronized boolean stageIs(String questId, String scopeKey, String stage) {
        return findLine(questId, scopeKey).map(line -> stage.equals(line.stageId)).orElse(false);
    }

    public synchronized boolean hasFlag(String questId, String scopeKey, UUID playerId, String flag, boolean playerTarget) {
        if (playerTarget && playerId != null) {
            QuestPlayerState player = state.players.get(playerKey(questId, scopeKey, playerId));
            return player != null && player.flags.contains(flag);
        }
        QuestlineState line = state.questlines.get(lineKey(questId, scopeKey));
        return line != null && line.flags.contains(flag);
    }

    public synchronized boolean hasEvidence(String questId, String scopeKey, String evidence) {
        QuestlineState line = state.questlines.get(lineKey(questId, scopeKey));
        return line != null && line.evidence.contains(evidence);
    }

    public synchronized boolean variableEquals(
            QuestDefinition quest,
            String scopeKey,
            UUID playerId,
            String variable,
            String expected
    ) {
        return expected.equals(variableValue(quest, scopeKey, playerId, variable));
    }

    public synchronized boolean variableAtLeast(
            QuestDefinition quest,
            String scopeKey,
            UUID playerId,
            String variable,
            int minimum
    ) {
        try {
            return Integer.parseInt(variableValue(quest, scopeKey, playerId, variable)) >= minimum;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public synchronized boolean endingIs(String questId, String scopeKey, String ending) {
        QuestlineState line = state.questlines.get(lineKey(questId, scopeKey));
        return line != null && ending.equals(line.endingId);
    }

    public String resolveScopeKeyForRegistry(QuestDefinition quest, RegistryExecutionContext execution, Map<String, String> parameters) {
        return resolveScopeKey(quest, execution, parameters);
    }

    private RegistryExecutionResult changeVariable(ActionContext context, boolean add) {
        QuestDefinition quest = definition(context);
        String variable = context.parameters().getOrDefault("variable", context.parameters().getOrDefault("id", ""));
        if (variable.isBlank()) return RegistryExecutionResult.failure("variable is required");
        QuestVariableDefinition definition = quest.variables().get(variable);
        String scopeKey = resolveScopeKey(quest, context.execution(), context.parameters());
        boolean playerTarget = playerTarget(context.parameters(), definition);
        String value = context.parameters().getOrDefault("value", context.parameters().getOrDefault("amount", "0"));
        if (playerTarget) {
            QuestPlayerState player = ensureActorPlayer(quest, scopeKey, context.execution());
            player.variables.put(variable, nextValue(definition, player.variables.get(variable), value, add));
            player.updatedAt = System.currentTimeMillis();
        } else {
            QuestlineState line = ensureLine(quest, scopeKey);
            line.variables.put(variable, nextValue(definition, line.variables.get(variable), value, add));
            line.updatedAt = System.currentTimeMillis();
        }
        save();
        emit("variable-changed", context.execution().actorId(), quest.id(), scopeKey,
                Map.of("variable", variable, "add", Boolean.toString(add)));
        return RegistryExecutionResult.ok();
    }

    private void processConsequences(long now) {
        List<QuestScheduledConsequence> due = state.scheduled.stream()
                .filter(entry -> entry.dueAt <= now)
                .sorted(Comparator.comparingLong(entry -> entry.dueAt))
                .limit(MAX_CONSEQUENCES_PER_INTERVAL)
                .toList();
        if (due.isEmpty()) return;
        state.scheduled.removeAll(due);
        save();
        for (QuestScheduledConsequence consequence : due) {
            try {
                ServerPlayerEntity player = consequence.playerId == null || server == null
                        ? null : server.getPlayerManager().getPlayer(consequence.playerId);
                RegistryExecutionContext execution = new RegistryExecutionContext(
                        api, server, player, consequence.playerId, realmFromScope(consequence.scopeKey),
                        null, realmFromScope(consequence.scopeKey), "", "elarion_quests",
                        Map.of("quest", consequence.questId, "scopeKey", consequence.scopeKey));
                RegistryExecutionResult result = api.registries().execute(
                        new ActionContext(execution, consequence.action, consequence.parameters));
                result.serverTasks().forEach(Runnable::run);
                if (!result.success()) {
                    logger.warn("quest consequence {} failed: {}", consequence.id, result.message());
                }
            } catch (RuntimeException exception) {
                logger.warn("quest consequence {} failed", consequence.id, exception);
            }
        }
    }

    private QuestDefinition definition(ActionContext context) {
        return definitions.require(context.parameters().getOrDefault("quest", ""));
    }

    private QuestlineState ensureLine(QuestDefinition quest, String scopeKey) {
        return state.questlines.computeIfAbsent(lineKey(quest.id(), scopeKey),
                ignored -> withDefaultVariables(new QuestlineState(
                        quest.id(), scopeKey, quest.defaultStage(), System.currentTimeMillis()), quest));
    }

    private QuestPlayerState ensurePlayer(QuestDefinition quest, String scopeKey, UUID playerId) {
        return state.players.computeIfAbsent(playerKey(quest.id(), scopeKey, playerId),
                ignored -> withDefaultVariables(new QuestPlayerState(
                        quest.id(), scopeKey, playerId, System.currentTimeMillis()), quest));
    }

    private QuestPlayerState ensureActorPlayer(QuestDefinition quest, String scopeKey, RegistryExecutionContext execution) {
        if (execution.actorId() == null) throw new IllegalArgumentException("player quest state needs an actor");
        return ensurePlayer(quest, scopeKey, execution.actorId());
    }

    private QuestActorBindingScope ensureBindings(String questId, String scopeKey) {
        return state.actorBindings.computeIfAbsent(lineKey(questId, scopeKey),
                ignored -> new QuestActorBindingScope(questId, scopeKey, System.currentTimeMillis()));
    }

    private static QuestlineState withDefaultVariables(QuestlineState line, QuestDefinition quest) {
        for (QuestVariableDefinition variable : quest.variables().values()) {
            if (variable.scope() == QuestVariableScope.SHARED) {
                line.variables.putIfAbsent(variable.id(), variable.defaultValue());
            }
        }
        return line;
    }

    private static QuestPlayerState withDefaultVariables(QuestPlayerState player, QuestDefinition quest) {
        for (QuestVariableDefinition variable : quest.variables().values()) {
            if (variable.scope() == QuestVariableScope.PLAYER) {
                player.variables.putIfAbsent(variable.id(), variable.defaultValue());
            }
        }
        return player;
    }

    private String variableValue(QuestDefinition quest, String scopeKey, UUID playerId, String variable) {
        QuestVariableDefinition definition = quest.variables().get(variable);
        if (definition != null && definition.scope() == QuestVariableScope.PLAYER) {
            QuestPlayerState player = playerId == null ? null : state.players.get(playerKey(quest.id(), scopeKey, playerId));
            return player == null ? definition.defaultValue() : player.variables.getOrDefault(variable, definition.defaultValue());
        }
        QuestlineState line = state.questlines.get(lineKey(quest.id(), scopeKey));
        String fallback = definition == null ? "" : definition.defaultValue();
        return line == null ? fallback : line.variables.getOrDefault(variable, fallback);
    }

    private static String nextValue(QuestVariableDefinition definition, String current, String rawValue, boolean add) {
        if (!add) return definition == null ? safe(rawValue) : definition.type().normalize(rawValue);
        try {
            int base = Integer.parseInt(current == null || current.isBlank()
                    ? definition == null ? "0" : definition.defaultValue() : current);
            int amount = Integer.parseInt(rawValue == null || rawValue.isBlank() ? "0" : rawValue);
            return Integer.toString(base + amount);
        } catch (NumberFormatException exception) {
            return current == null ? "" : current;
        }
    }

    private static boolean playerTarget(Map<String, String> parameters, QuestVariableDefinition variable) {
        String raw = parameters.getOrDefault("state-scope", parameters.getOrDefault("target", ""));
        if (!raw.isBlank()) return raw.equalsIgnoreCase("player") || raw.equalsIgnoreCase("personal");
        return variable != null && variable.scope() == QuestVariableScope.PLAYER;
    }

    private String resolveScopeKey(QuestDefinition quest, RegistryExecutionContext execution, Map<String, String> parameters) {
        String direct = parameters.getOrDefault("scope-key", "");
        if (!direct.isBlank()) return direct;
        String scope = parameters.getOrDefault("scope", quest.scope()).toLowerCase(Locale.ROOT);
        return switch (scope) {
            case "global" -> "global";
            case "world" -> {
                String world = parameters.getOrDefault("world", execution.worldId());
                if (world.isBlank()) throw new IllegalArgumentException("world-scoped quest needs a world");
                yield "world:" + world;
            }
            case "player" -> {
                UUID actor = execution.actorId();
                if (actor == null) throw new IllegalArgumentException("player-scoped quest needs an actor");
                yield "player:" + actor;
            }
            default -> {
                String realm = realmId("", execution, parameters);
                if (realm.isBlank()) throw new IllegalArgumentException("realm-scoped quest needs a Realm");
                yield "realm:" + realm;
            }
        };
    }

    private static String realmId(String scopeKey, RegistryExecutionContext execution, Map<String, String> parameters) {
        String realm = parameters.getOrDefault("realm", "");
        if (!realm.isBlank()) return realm;
        if (scopeKey != null && scopeKey.startsWith("realm:")) return scopeKey.substring("realm:".length());
        if (execution.targetRealmId() != null && !execution.targetRealmId().isBlank()) return execution.targetRealmId();
        return execution.actorRealmId();
    }

    private static String realmFromScope(String scopeKey) {
        return scopeKey != null && scopeKey.startsWith("realm:") ? scopeKey.substring("realm:".length()) : "";
    }

    private static long delayMillis(Map<String, String> parameters) {
        if (parameters.containsKey("delay-millis")) return positiveLong(parameters.get("delay-millis"), 0L);
        if (parameters.containsKey("delay-ticks")) return positiveLong(parameters.get("delay-ticks"), 0L) * 50L;
        return positiveLong(parameters.getOrDefault("delay-seconds", "0"), 0L) * 1000L;
    }

    private static long positiveLong(String raw, long fallback) {
        try {
            return Math.max(0L, Long.parseLong(raw == null ? "" : raw));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static boolean parseBoolean(String raw) {
        return raw == null || raw.isBlank() || Boolean.parseBoolean(raw);
    }

    private void emit(String type, UUID actorId, String questId, String scopeKey, Map<String, String> metadata) {
        Map<String, String> data = new LinkedHashMap<>(metadata);
        data.put("quest", questId);
        data.put("scopeKey", scopeKey);
        api.system().events().emitDomainEvent(ElarionDomainEvent.of(
                "elarion_quests",
                "quest-" + type,
                actorId,
                realmFromScope(scopeKey),
                "questline",
                questId,
                data));
    }

    private static String lineKey(String questId, String scopeKey) {
        return safe(questId) + "::" + safe(scopeKey);
    }

    private static String playerKey(String questId, String scopeKey, UUID playerId) {
        return lineKey(questId, scopeKey) + "::" + playerId;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
