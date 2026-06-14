package panetina.elarion.addons.economy.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.economy.config.EconomyConfig;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyPulse;
import panetina.elarion.addons.economy.model.EconomyTransaction;
import panetina.elarion.addons.economy.model.EconomyTransactionType;
import panetina.elarion.addons.economy.model.TransactionResult;
import panetina.elarion.addons.economy.service.EconomyGovernorService;
import panetina.elarion.addons.economy.service.EconomyInventoryService;
import panetina.elarion.addons.economy.service.EconomyPricingService;
import panetina.elarion.addons.economy.service.EconomyTransactionService;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.command.CommandOutput;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class EconomyCommands {
    private EconomyCommands() {
    }

    public static LiteralArgumentBuilder<ServerCommandSource> create(
            ElarionApi core,
            EconomyTransactionService transactions,
            EconomyInventoryService inventory,
            EconomyGovernorService governor,
            EconomyPricingService pricing
    ) {
        return literal("economy")
                .requires(source -> source.hasPermissionLevel(4))
                .then(wallet(core, transactions, inventory))
                .then(treasury(core, transactions))
                .then(transfer(transactions))
                .then(transactionQueries(core, transactions))
                .then(literal("pulse").executes(context -> showPulse(context.getSource(), governor.pulse())))
                .then(literal("recalculate").executes(context -> showPulse(context.getSource(), governor.pulse())))
                .then(literal("reload").executes(context -> reload(
                        context.getSource(), transactions, pricing)));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> wallet(
            ElarionApi core,
            EconomyTransactionService transactions,
            EconomyInventoryService inventory
    ) {
        return literal("wallet")
                .then(literal("get")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    long balance = transactions.balance(EconomyAccount.player(player.getUuid()));
                                    CommandOutput.header(context.getSource(), "Wallet");
                                    CommandOutput.kv(context.getSource(), "Player", display(core, player.getUuid()));
                                    CommandOutput.kv(context.getSource(), "Balance",
                                            core.serverIdentity().currencyAmount(balance));
                                    return 1;
                                })))
                .then(literal("give")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> adminAdjust(
                                                context.getSource(), transactions,
                                                EconomyAccount.MINT,
                                                EconomyAccount.player(EntityArgumentType.getPlayer(
                                                        context, "player").getUuid()),
                                                LongArgumentType.getLong(context, "amount"),
                                                "Admin wallet grant")))))
                .then(literal("take")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> adminAdjust(
                                                context.getSource(), transactions,
                                                EconomyAccount.player(EntityArgumentType.getPlayer(
                                                        context, "player").getUuid()),
                                                EconomyAccount.BURN,
                                                LongArgumentType.getLong(context, "amount"),
                                                "Admin wallet deduction")))))
                .then(literal("deposit")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> report(context.getSource(), inventory.deposit(
                                                EntityArgumentType.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "amount"),
                                                "elarion:economy_admin_command"))))))
                .then(literal("withdraw")
                        .then(argument("player", EntityArgumentType.player())
                                .then(argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> report(context.getSource(), inventory.withdraw(
                                                EntityArgumentType.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "amount"),
                                                "elarion:economy_admin_command"))))));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> treasury(
            ElarionApi core,
            EconomyTransactionService transactions
    ) {
        return literal("treasury")
                .then(literal("get")
                        .then(realmArgument(core)
                                .executes(context -> {
                                    String realm = com.mojang.brigadier.arguments.StringArgumentType.getString(
                                            context, "realm");
                                    long balance = transactions.balance(EconomyAccount.realm(realm));
                                    CommandOutput.header(context.getSource(),
                                            core.serverIdentity().realmSingular() + " Treasury");
                                    CommandOutput.kv(context.getSource(), core.serverIdentity().realmSingular(), realm);
                                    CommandOutput.kv(context.getSource(), "Balance",
                                            core.serverIdentity().currencyAmount(balance));
                                    return 1;
                                })))
                .then(literal("give")
                        .then(realmArgument(core)
                                .then(argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> adminAdjust(
                                                context.getSource(), transactions,
                                                EconomyAccount.MINT,
                                                EconomyAccount.realm(com.mojang.brigadier.arguments.StringArgumentType
                                                        .getString(context, "realm")),
                                                LongArgumentType.getLong(context, "amount"),
                                                "Admin treasury grant")))))
                .then(literal("take")
                        .then(realmArgument(core)
                                .then(argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> adminAdjust(
                                                context.getSource(), transactions,
                                                EconomyAccount.realm(com.mojang.brigadier.arguments.StringArgumentType
                                                        .getString(context, "realm")),
                                                EconomyAccount.BURN,
                                                LongArgumentType.getLong(context, "amount"),
                                                "Admin treasury deduction")))));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> transfer(
            EconomyTransactionService transactions
    ) {
        return literal("transfer")
                .then(literal("player")
                        .then(argument("from", EntityArgumentType.player())
                                .then(argument("to", EntityArgumentType.player())
                                        .then(argument("amount", LongArgumentType.longArg(1))
                                                .executes(context -> {
                                                    ServerPlayerEntity from = EntityArgumentType.getPlayer(
                                                            context, "from");
                                                    ServerPlayerEntity to = EntityArgumentType.getPlayer(context, "to");
                                                    return report(context.getSource(), transactions.execute(
                                                            EconomyTransactionType.TRANSFER,
                                                            EconomyAccount.player(from.getUuid()),
                                                            EconomyAccount.player(to.getUuid()),
                                                            LongArgumentType.getLong(context, "amount"),
                                                            actor(context.getSource()),
                                                            "Admin player transfer",
                                                            "elarion:economy_admin_command",
                                                            Map.of()));
                                                })))));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> transactionQueries(
            ElarionApi core,
            EconomyTransactionService transactions
    ) {
        return literal("transactions")
                .then(literal("player")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> showTransactions(
                                        context.getSource(), transactions,
                                        EconomyAccount.player(EntityArgumentType.getPlayer(
                                                context, "player").getUuid()), 10))
                                .then(argument("limit", IntegerArgumentType.integer(1))
                                        .executes(context -> showTransactions(
                                                context.getSource(), transactions,
                                                EconomyAccount.player(EntityArgumentType.getPlayer(
                                                        context, "player").getUuid()),
                                                IntegerArgumentType.getInteger(context, "limit"))))))
                .then(literal("realm")
                        .then(realmArgument(core)
                                .executes(context -> showTransactions(
                                        context.getSource(), transactions,
                                        EconomyAccount.realm(com.mojang.brigadier.arguments.StringArgumentType
                                                .getString(context, "realm")), 10))
                                .then(argument("limit", IntegerArgumentType.integer(1))
                                        .executes(context -> showTransactions(
                                                context.getSource(), transactions,
                                                EconomyAccount.realm(com.mojang.brigadier.arguments.StringArgumentType
                                                        .getString(context, "realm")),
                                                IntegerArgumentType.getInteger(context, "limit"))))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<ServerCommandSource, String> realmArgument(
            ElarionApi core
    ) {
        return argument("realm", com.mojang.brigadier.arguments.StringArgumentType.word())
                .suggests((context, builder) -> CommandSource.suggestMatching(
                        core.realms().all().stream().map(realm -> realm.id()), builder));
    }

    private static int adminAdjust(
            ServerCommandSource source,
            EconomyTransactionService transactions,
            EconomyAccount from,
            EconomyAccount to,
            long amount,
            String reason
    ) {
        return report(source, transactions.execute(
                EconomyTransactionType.ADMIN_ADJUSTMENT, from, to, amount, actor(source),
                reason, "elarion:economy_admin_command", Map.of()));
    }

    private static int report(ServerCommandSource source, TransactionResult result) {
        if (!result.successful()) {
            source.sendError(Text.literal(result.message()));
            return 0;
        }
        EconomyTransaction transaction = result.transaction();
        CommandOutput.success(source, "Transaction completed.", true);
        CommandOutput.kv(source, "ID", transaction.id());
        CommandOutput.kv(source, "Type", transaction.type());
        CommandOutput.kv(source, "Amount", currency(transaction.amount()));
        CommandOutput.kv(source, "From", transaction.fromAccount().key());
        CommandOutput.kv(source, "To", transaction.toAccount().key());
        return 1;
    }

    private static int showTransactions(
            ServerCommandSource source,
            EconomyTransactionService transactions,
            EconomyAccount account,
            int limit
    ) {
        var values = transactions.recentFor(account, limit);
        CommandOutput.header(source, "Economy Transactions");
        CommandOutput.kv(source, "Account", account.key());
        CommandOutput.kv(source, "Shown", values.size());
        if (values.isEmpty()) {
            CommandOutput.empty(source, "No recent transactions for this account.");
            return 0;
        }
        for (EconomyTransaction transaction : values) {
            CommandOutput.section(source, "#" + transaction.sequence() + " " + transaction.type());
            CommandOutput.kv(source, "ID", transaction.id());
            CommandOutput.kv(source, "Amount", currency(transaction.amount()));
            CommandOutput.kv(source, "Route", transaction.fromAccount().key()
                    + " -> " + transaction.toAccount().key());
            CommandOutput.kv(source, "Result", transaction.success()
                    ? "SUCCESS"
                    : "FAILED: " + transaction.failure());
            CommandOutput.kv(source, "Source", transaction.sourceSystem());
            if (!transaction.reason().isBlank()) {
                CommandOutput.kv(source, "Reason", transaction.reason());
            }
        }
        return 1;
    }

    private static int showPulse(ServerCommandSource source, EconomyPulse pulse) {
        CommandOutput.header(source, "Economy Pulse");
        CommandOutput.section(source, "Health");
        CommandOutput.kv(source, "Mode", pulse.mode());
        CommandOutput.kv(source, "State", pulse.health());
        CommandOutput.section(source, "Supply");
        CommandOutput.kv(source, "Tracked supply", currency(pulse.trackedSupply()));
        CommandOutput.kv(source, "Wallets", currency(pulse.walletCurrency()));
        CommandOutput.kv(source, "Treasuries", currency(pulse.treasuryCurrency()));
        CommandOutput.section(source, "Current Window");
        CommandOutput.kv(source, "Transactions", pulse.transactionsInWindow());
        CommandOutput.kv(source, "Created", currency(pulse.createdInWindow()));
        CommandOutput.kv(source, "Destroyed", currency(pulse.destroyedInWindow()));
        CommandOutput.kv(source, "Faucet:Sink", format(pulse.faucetSinkRatio()));
        CommandOutput.kv(source, "Top 10 wallet share", format(pulse.topTenWalletShare() * 100.0D) + "%");
        return 1;
    }

    private static int reload(
            ServerCommandSource source,
            EconomyTransactionService transactions,
            EconomyPricingService pricing
    ) {
        try {
            transactions.reload(EconomyConfig.load());
            pricing.reload();
            source.sendFeedback(() -> Text.literal("Economy configuration reloaded."), true);
            return 1;
        } catch (RuntimeException exception) {
            source.sendError(Text.literal(exception.getMessage()));
            return 0;
        }
    }

    private static UUID actor(ServerCommandSource source) {
        return source.getEntity() instanceof ServerPlayerEntity player ? player.getUuid() : null;
    }

    private static String display(ElarionApi core, UUID playerId) {
        return core.citizens().find(playerId)
                .map(citizen -> citizen.nickname() == null || citizen.nickname().isBlank()
                        ? citizen.lastKnownUsername()
                        : citizen.nickname())
                .orElse(playerId.toString());
    }

    private static String currency(long amount) {
        return ElarionApi.get().serverIdentity().currencyAmount(amount);
    }

    private static String format(double value) {
        return Double.isInfinite(value) ? "INF" : String.format(Locale.ROOT, "%.3f", value);
    }
}
