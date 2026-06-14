package panetina.elarion.addons.economy.registry;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyTransactionType;
import panetina.elarion.addons.economy.model.TransactionResult;
import panetina.elarion.addons.economy.service.EconomyTransactionService;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.RewardAction;
import panetina.elarion.core.model.ServerIdentityConfig;
import panetina.elarion.core.registry.ActionContext;
import panetina.elarion.core.registry.ActionHandler;
import panetina.elarion.core.registry.ActionType;
import panetina.elarion.core.registry.RegistryExecutionContext;
import panetina.elarion.core.registry.RegistryExecutionResult;
import panetina.elarion.core.service.RewardActionService;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class EconomyRewardActions {
    public static final Set<String> REWARD_ACTION_TYPES = Set.of(
            "currency-reward",
            "currency-sink",
            "realm-currency-reward",
            "realm-currency-sink",
            "realm-treasury-grant"
    );

    private static final String OWNER = "elarion_economy";
    private static final String REWARD_SOURCE = "elarion:economy_reward";
    private static final String REGISTRY_SOURCE = "elarion:economy_action";

    private EconomyRewardActions() {
    }

    public static void register(ElarionApi api, EconomyTransactionService transactions) {
        registerRewardHandlers(api.rewards(), transactions);
        registerRegistryActions(api, transactions);
    }

    private static void registerRewardHandlers(
            RewardActionService rewards,
            EconomyTransactionService transactions
    ) {
        rewards.registerHandler("currency-reward", (context, action) -> {
            long amount = amount(action.parameters());
            if (amount < 1) return false;
            return transactions.reward(
                    EconomyAccount.player(context.player().getUuid()),
                    amount,
                    context.player().getUuid(),
                    reason(action.parameters(), "Reward " + context.rewardId()),
                    source(action.parameters(), REWARD_SOURCE)
            ).successful();
        });
        rewards.registerHandler("currency-sink", (context, action) -> {
            long amount = amount(action.parameters());
            if (amount < 1) return false;
            return transactions.sink(
                    EconomyAccount.player(context.player().getUuid()),
                    amount,
                    context.player().getUuid(),
                    reason(action.parameters(), "Reward sink " + context.rewardId()),
                    source(action.parameters(), REWARD_SOURCE)
            ).successful();
        });
        rewards.registerHandler("realm-currency-reward", (context, action) -> {
            long amount = amount(action.parameters());
            String realm = realmId(action.parameters(), "", "");
            if (amount < 1 || realm.isBlank()) return false;
            return transactions.reward(
                    EconomyAccount.realm(realm),
                    amount,
                    context.player().getUuid(),
                    reason(action.parameters(), "Realm reward " + context.rewardId()),
                    source(action.parameters(), REWARD_SOURCE)
            ).successful();
        });
        rewards.registerHandler("realm-currency-sink", (context, action) -> {
            long amount = amount(action.parameters());
            String realm = realmId(action.parameters(), "", "");
            if (amount < 1 || realm.isBlank()) return false;
            return transactions.sink(
                    EconomyAccount.realm(realm),
                    amount,
                    context.player().getUuid(),
                    reason(action.parameters(), "Realm reward sink " + context.rewardId()),
                    source(action.parameters(), REWARD_SOURCE)
            ).successful();
        });
        rewards.registerHandler("realm-treasury-grant", (context, action) -> {
            long amount = amount(action.parameters());
            String realm = realmId(action.parameters(), "", "");
            if (amount < 1 || realm.isBlank()) return false;
            return transactions.execute(
                    EconomyTransactionType.TREASURY_GRANT,
                    EconomyAccount.realm(realm),
                    EconomyAccount.player(context.player().getUuid()),
                    amount,
                    context.player().getUuid(),
                    reason(action.parameters(), "Realm treasury grant " + context.rewardId()),
                    source(action.parameters(), REWARD_SOURCE),
                    metadata("reward", context.rewardId(), action.parameters())
            ).successful();
        });
    }

    private static void registerRegistryActions(ElarionApi api, EconomyTransactionService transactions) {
        register(api, "elarion:economy_reward_player",
                "Mints currency into a player bank account through the audited Economy transaction service.",
                context -> playerReward(context, transactions));
        register(api, "elarion:economy_sink_player",
                "Burns currency from a player bank account through the audited Economy transaction service.",
                context -> playerSink(context, transactions));
        register(api, "elarion:economy_reward_realm",
                "Mints currency into a realm treasury through the audited Economy transaction service.",
                context -> realmReward(context, transactions));
        register(api, "elarion:economy_sink_realm",
                "Burns currency from a realm treasury through the audited Economy transaction service.",
                context -> realmSink(context, transactions));
        register(api, "elarion:economy_treasury_grant",
                "Transfers currency from a realm treasury to a player bank account.",
                context -> treasuryGrant(context, transactions));
    }

    private static RegistryExecutionResult playerReward(
            ActionContext context,
            EconomyTransactionService transactions
    ) {
        Optional<UUID> player = playerId(context);
        if (player.isEmpty()) return RegistryExecutionResult.failure("This action requires a player.");
        long amount = amount(context.parameters());
        if (amount < 1) return RegistryExecutionResult.failure("Currency amount must be positive.");
        return result(transactions.execute(
                        EconomyTransactionType.REWARD,
                        EconomyAccount.MINT,
                        EconomyAccount.player(player.get()),
                        amount,
                        actor(context.execution()),
                        reason(context.parameters(), "Economy player reward"),
                        source(context.parameters(), REGISTRY_SOURCE),
                        metadata(context.actionId(), context.execution(), context.parameters())),
                "Granted " + currency(amount) + ".");
    }

    private static RegistryExecutionResult playerSink(
            ActionContext context,
            EconomyTransactionService transactions
    ) {
        Optional<UUID> player = playerId(context);
        if (player.isEmpty()) return RegistryExecutionResult.failure("This action requires a player.");
        long amount = amount(context.parameters());
        if (amount < 1) return RegistryExecutionResult.failure("Currency amount must be positive.");
        return result(transactions.execute(
                        EconomyTransactionType.SINK,
                        EconomyAccount.player(player.get()),
                        EconomyAccount.BURN,
                        amount,
                        actor(context.execution()),
                        reason(context.parameters(), "Economy player sink"),
                        source(context.parameters(), REGISTRY_SOURCE),
                        metadata(context.actionId(), context.execution(), context.parameters())),
                "Removed " + currency(amount) + ".");
    }

    private static RegistryExecutionResult realmReward(
            ActionContext context,
            EconomyTransactionService transactions
    ) {
        String realm = realmId(context.parameters(), context.execution().targetRealmId(),
                context.execution().actorRealmId());
        if (realm.isBlank()) return RegistryExecutionResult.failure("This action requires a "
                + identity().realmSingular() + ".");
        long amount = amount(context.parameters());
        if (amount < 1) return RegistryExecutionResult.failure("Currency amount must be positive.");
        return result(transactions.execute(
                        EconomyTransactionType.REWARD,
                        EconomyAccount.MINT,
                        EconomyAccount.realm(realm),
                        amount,
                        actor(context.execution()),
                        reason(context.parameters(), "Economy Realm reward"),
                        source(context.parameters(), REGISTRY_SOURCE),
                        metadata(context.actionId(), context.execution(), context.parameters())),
                "Granted " + currency(amount) + " to " + identity().realmLabel(realm) + ".");
    }

    private static RegistryExecutionResult realmSink(
            ActionContext context,
            EconomyTransactionService transactions
    ) {
        String realm = realmId(context.parameters(), context.execution().targetRealmId(),
                context.execution().actorRealmId());
        if (realm.isBlank()) return RegistryExecutionResult.failure("This action requires a "
                + identity().realmSingular() + ".");
        long amount = amount(context.parameters());
        if (amount < 1) return RegistryExecutionResult.failure("Currency amount must be positive.");
        return result(transactions.execute(
                        EconomyTransactionType.SINK,
                        EconomyAccount.realm(realm),
                        EconomyAccount.BURN,
                        amount,
                        actor(context.execution()),
                        reason(context.parameters(), "Economy Realm sink"),
                        source(context.parameters(), REGISTRY_SOURCE),
                        metadata(context.actionId(), context.execution(), context.parameters())),
                "Removed " + currency(amount) + " from " + identity().realmLabel(realm) + ".");
    }

    private static RegistryExecutionResult treasuryGrant(
            ActionContext context,
            EconomyTransactionService transactions
    ) {
        String realm = realmId(context.parameters(), context.execution().targetRealmId(),
                context.execution().actorRealmId());
        Optional<UUID> player = playerId(context);
        if (realm.isBlank()) return RegistryExecutionResult.failure("This action requires a "
                + identity().realmSingular() + ".");
        if (player.isEmpty()) return RegistryExecutionResult.failure("This action requires a player.");
        long amount = amount(context.parameters());
        if (amount < 1) return RegistryExecutionResult.failure("Currency amount must be positive.");
        return result(transactions.execute(
                        EconomyTransactionType.TREASURY_GRANT,
                        EconomyAccount.realm(realm),
                        EconomyAccount.player(player.get()),
                        amount,
                        actor(context.execution()),
                        reason(context.parameters(), "Economy treasury grant"),
                        source(context.parameters(), REGISTRY_SOURCE),
                        metadata(context.actionId(), context.execution(), context.parameters())),
                "Granted " + currency(amount) + " from " + identity().realmLabel(realm)
                        + " treasury.");
    }

    private static void register(ElarionApi api, String id, String description, ActionHandler handler) {
        api.registries().actions().register(new ActionType(id, OWNER, description));
        api.registries().registerActionHandler(id, handler);
    }

    static RegistryExecutionResult result(TransactionResult result, String success) {
        return result.successful()
                ? RegistryExecutionResult.ok(success)
                : RegistryExecutionResult.failure(result.message());
    }

    private static String currency(long amount) {
        return identity().currencyAmount(amount);
    }

    private static ServerIdentityConfig identity() {
        try {
            return ElarionApi.get().serverIdentity();
        } catch (IllegalStateException exception) {
            return ServerIdentityConfig.defaults();
        }
    }

    static long amount(Map<String, String> parameters) {
        String raw = parameters.getOrDefault("amount", "");
        if (raw.isBlank()) return 0L;
        try {
            long parsed = Long.parseLong(raw);
            return parsed < 1 ? 0L : parsed;
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private static Optional<UUID> playerId(ActionContext context) {
        String explicit = first(context.parameters(), "player", "player-id", "target-player", "target");
        if (!explicit.isBlank()) {
            try {
                return Optional.of(UUID.fromString(explicit));
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }
        }
        if (context.execution().targetId() != null) return Optional.of(context.execution().targetId());
        if (context.execution().actorId() != null) return Optional.of(context.execution().actorId());
        ServerPlayerEntity actor = context.execution().actor();
        return actor == null ? Optional.empty() : Optional.of(actor.getUuid());
    }

    private static UUID actor(RegistryExecutionContext execution) {
        if (execution.actorId() != null) return execution.actorId();
        return execution.actor() == null ? null : execution.actor().getUuid();
    }

    private static String realmId(Map<String, String> parameters, String firstFallback, String secondFallback) {
        String explicit = first(parameters, "realm", "realm-id", "target-realm");
        if (!explicit.isBlank()) return normalizeRealm(explicit);
        if (firstFallback != null && !firstFallback.isBlank()) return normalizeRealm(firstFallback);
        return secondFallback == null ? "" : normalizeRealm(secondFallback);
    }

    private static String reason(Map<String, String> parameters, String fallback) {
        return parameters.getOrDefault("reason", fallback);
    }

    private static String source(Map<String, String> parameters, String fallback) {
        return parameters.getOrDefault("source-system",
                parameters.getOrDefault("sourceSystem", fallback));
    }

    private static Map<String, String> metadata(
            String actionId,
            RegistryExecutionContext execution,
            Map<String, String> parameters
    ) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("actionId", actionId);
        metadata.put("sourceAddon", execution.sourceAddon());
        metadata.putAll(parameters);
        return metadata;
    }

    private static Map<String, String> metadata(
            String key,
            String value,
            Map<String, String> parameters
    ) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put(key, value);
        metadata.putAll(parameters);
        return metadata;
    }

    private static String first(Map<String, String> parameters, String... keys) {
        for (String key : keys) {
            String value = parameters.get(key);
            if (value != null && !value.isBlank()) return value;
        }
        return "";
    }

    private static String normalizeRealm(String realm) {
        return realm.trim().toLowerCase(Locale.ROOT);
    }
}
