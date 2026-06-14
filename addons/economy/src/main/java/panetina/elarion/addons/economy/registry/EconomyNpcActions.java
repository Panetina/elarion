package panetina.elarion.addons.economy.registry;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.TransactionResult;
import panetina.elarion.addons.economy.service.EconomyInventoryService;
import panetina.elarion.addons.economy.service.EconomyTransactionService;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.ServerIdentityConfig;
import panetina.elarion.core.registry.ActionHandler;
import panetina.elarion.core.registry.ActionType;
import panetina.elarion.core.registry.RegistryExecutionResult;

public final class EconomyNpcActions {
    private static final String OWNER = "elarion_economy";
    private static final String SOURCE = "elarion:economy_npc";

    private EconomyNpcActions() {
    }

    public static RegistryExecutionResult withdrawResult(TransactionResult result, int amount) {
        if (amount < 1) return RegistryExecutionResult.failure("Withdrawal amount must be positive.");
        return result(result, "Withdrew " + currency(amount) + ".");
    }

    public static RegistryExecutionResult depositAllResult(TransactionResult result, int amount) {
        if (amount < 1) {
            return RegistryExecutionResult.failure("You have no physical "
                    + identity().currencyPlural().toLowerCase(java.util.Locale.ROOT)
                    + " to deposit.");
        }
        return result(result, "Deposited " + currency(amount) + ".");
    }

    public static RegistryExecutionResult depositResult(TransactionResult result, int amount) {
        if (amount < 1) return RegistryExecutionResult.failure("Deposit amount must be positive.");
        return result(result, "Deposited " + currency(amount) + ".");
    }

    public static void register(
            ElarionApi api,
            EconomyTransactionService transactions,
            EconomyInventoryService inventory
    ) {
        register(api, "elarion:economy_wallet_balance",
                "Shows the actor's wallet and physical currency balance.",
                context -> {
                    ServerPlayerEntity player = context.execution().actor();
                    if (player == null) return RegistryExecutionResult.failure("This action requires a player.");
                    long wallet = transactions.balance(EconomyAccount.player(player.getUuid()));
                    int physical = inventory.countCurrency(player);
                    return RegistryExecutionResult.ok("Balance: " + currency(wallet) + ". Physical: "
                            + currency(physical) + ".");
                });
        register(api, "elarion:economy_bank_balance",
                "Returns the actor's deposited currency balance for NPC service badges.",
                context -> {
                    ServerPlayerEntity player = context.execution().actor();
                    if (player == null) return RegistryExecutionResult.failure("This action requires a player.");
                    return RegistryExecutionResult.ok(Long.toString(
                            transactions.balance(EconomyAccount.player(player.getUuid()))));
                });
        register(api, "elarion:economy_deposit_all_currency",
                "Deposits every physical currency item carried by the actor.",
                context -> {
                    ServerPlayerEntity player = context.execution().actor();
                    if (player == null) return RegistryExecutionResult.failure("This action requires a player.");
                    int amount = inventory.countCurrency(player);
                    return depositAllResult(inventory.deposit(player, amount, SOURCE), amount);
                });
        register(api, "elarion:economy_deposit_currency_amount",
                "Deposits a submitted amount of physical currency carried by the actor.",
                context -> {
                    ServerPlayerEntity player = context.execution().actor();
                    if (player == null) return RegistryExecutionResult.failure("This action requires a player.");
                    int amount = amount(context.parameters().get("amount"));
                    return depositResult(inventory.deposit(player, amount, SOURCE), amount);
                });
        register(api, "elarion:economy_withdraw_currency",
                "Withdraws a configured amount of currency from the actor's wallet.",
                context -> {
                    ServerPlayerEntity player = context.execution().actor();
                    if (player == null) return RegistryExecutionResult.failure("This action requires a player.");
                    int amount = amount(context.parameters().get("amount"));
                    return withdrawResult(inventory.withdraw(player, amount, SOURCE), amount);
                });
        register(api, "elarion:economy_withdraw_currency_amount",
                "Withdraws a submitted amount of currency from the actor's bank balance.",
                context -> {
                    ServerPlayerEntity player = context.execution().actor();
                    if (player == null) return RegistryExecutionResult.failure("This action requires a player.");
                    int amount = amount(context.parameters().get("amount"));
                    return withdrawResult(inventory.withdraw(player, amount, SOURCE), amount);
                });
    }

    private static void register(ElarionApi api, String id, String description, ActionHandler handler) {
        api.registries().actions().register(new ActionType(id, OWNER, description));
        api.registries().registerActionHandler(id, handler);
    }

    private static RegistryExecutionResult result(TransactionResult result, String success) {
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

    private static int amount(String raw) {
        if (raw == null || raw.isBlank()) return 0;
        try {
            long parsed = Long.parseLong(raw);
            if (parsed > Integer.MAX_VALUE) return 0;
            return (int) parsed;
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
