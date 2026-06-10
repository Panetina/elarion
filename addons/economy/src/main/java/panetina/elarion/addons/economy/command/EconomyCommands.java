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
import panetina.elarion.addons.economy.service.EconomyTransactionService;
import panetina.elarion.core.api.ElarionApi;

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
            EconomyGovernorService governor
    ) {
        return literal("economy")
                .requires(source -> source.hasPermissionLevel(4))
                .then(wallet(core, transactions, inventory))
                .then(treasury(core, transactions))
                .then(transfer(transactions))
                .then(transactionQueries(core, transactions))
                .then(literal("pulse").executes(context -> showPulse(context.getSource(), governor.pulse())))
                .then(literal("recalculate").executes(context -> showPulse(context.getSource(), governor.pulse())))
                .then(literal("reload").executes(context -> reload(context.getSource(), transactions)));
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
                                    context.getSource().sendFeedback(() -> Text.literal(
                                            display(core, player.getUuid()) + " wallet: " + balance + " sigils"), false);
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
                                    context.getSource().sendFeedback(() ->
                                            Text.literal(realm + " treasury: " + balance + " sigils"), false);
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
        source.sendFeedback(() -> Text.literal("Transaction " + transaction.id()
                + " completed: " + transaction.amount() + " sigils, "
                + transaction.fromAccount().key() + " -> " + transaction.toAccount().key()), true);
        return 1;
    }

    private static int showTransactions(
            ServerCommandSource source,
            EconomyTransactionService transactions,
            EconomyAccount account,
            int limit
    ) {
        var values = transactions.recentFor(account, limit);
        source.sendFeedback(() -> Text.literal("Transactions for " + account.key()
                + ": " + values.size()), false);
        for (EconomyTransaction transaction : values) {
            source.sendFeedback(() -> Text.literal("#" + transaction.sequence()
                    + " " + transaction.type()
                    + " " + transaction.amount()
                    + " " + transaction.fromAccount().key()
                    + " -> " + transaction.toAccount().key()
                    + " " + (transaction.success() ? "SUCCESS" : "FAILED:" + transaction.failure())
                    + " source=" + transaction.sourceSystem()), false);
        }
        return 1;
    }

    private static int showPulse(ServerCommandSource source, EconomyPulse pulse) {
        source.sendFeedback(() -> Text.literal("Economy pulse: mode=" + pulse.mode()
                + " health=" + pulse.health()), false);
        source.sendFeedback(() -> Text.literal("Supply: tracked=" + pulse.trackedSupply()
                + " wallets=" + pulse.walletSigils()
                + " treasuries=" + pulse.treasurySigils()), false);
        source.sendFeedback(() -> Text.literal("Window: transactions=" + pulse.transactionsInWindow()
                + " created=" + pulse.createdInWindow()
                + " destroyed=" + pulse.destroyedInWindow()
                + " faucet:sink=" + format(pulse.faucetSinkRatio())
                + " top10Share=" + format(pulse.topTenWalletShare() * 100.0D) + "%"), false);
        return 1;
    }

    private static int reload(ServerCommandSource source, EconomyTransactionService transactions) {
        try {
            transactions.reload(EconomyConfig.load());
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

    private static String format(double value) {
        return Double.isInfinite(value) ? "INF" : String.format(Locale.ROOT, "%.3f", value);
    }
}
