package panetina.elarion.addons.economy.service;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.addons.economy.config.EconomyConfig;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyAccountType;
import panetina.elarion.addons.economy.model.EconomyTransaction;
import panetina.elarion.addons.economy.model.EconomyTransactionType;
import panetina.elarion.addons.economy.model.TransactionResult;
import panetina.elarion.addons.economy.model.TransactionStatus;
import panetina.elarion.addons.economy.storage.EconomyState;
import panetina.elarion.addons.economy.storage.EconomyStorage;
import panetina.elarion.core.service.ElarionDiagnostics;
import panetina.elarion.core.service.ElarionTaskService;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class EconomyTransactionService {
    private final Logger logger;
    private final EconomyStorage storage;
    private final Predicate<String> realmExists;
    private final Consumer<EconomyTransaction> historyRecorder;
    private final ElarionTaskService tasks;
    private EconomyConfig config;
    private EconomyState state = new EconomyState();
    private MinecraftServer server;
    private boolean bound;
    private long nextSnapshotAt;
    private CompletableFuture<Void> pendingSnapshot = CompletableFuture.completedFuture(null);

    public EconomyTransactionService(
            Logger logger,
            EconomyStorage storage,
            EconomyConfig config,
            Predicate<String> realmExists,
            Consumer<EconomyTransaction> historyRecorder,
            ElarionTaskService tasks
    ) {
        this.logger = logger;
        this.storage = storage;
        this.config = config;
        this.realmExists = realmExists;
        this.historyRecorder = historyRecorder;
        this.tasks = tasks;
        ElarionDiagnostics.register("economy", this::diagnostics);
    }

    public synchronized void bind(MinecraftServer server) {
        this.server = server;
        this.state = storage.load(server);
        this.bound = true;
        this.nextSnapshotAt = System.currentTimeMillis() + config.snapshotIntervalMillis();
    }

    public synchronized void reload(EconomyConfig config) {
        this.config = config;
        this.nextSnapshotAt = System.currentTimeMillis() + config.snapshotIntervalMillis();
    }

    public synchronized void tick() {
        if (!bound || System.currentTimeMillis() < nextSnapshotAt) return;
        if (!pendingSnapshot.isDone()) return;
        EconomyState snapshot = state.copy();
        nextSnapshotAt = System.currentTimeMillis() + config.snapshotIntervalMillis();
        pendingSnapshot = tasks.submitIo("economy-snapshot-save", () -> storage.save(server, snapshot));
    }

    public void shutdown() {
        CompletableFuture<Void> pending;
        synchronized (this) {
            pending = pendingSnapshot;
        }
        pending.join();
        synchronized (this) {
            if (bound) storage.save(server, state.copy());
        }
    }

    public synchronized long balance(EconomyAccount account) {
        if (account == null || account.type() == EconomyAccountType.SYSTEM) return 0L;
        return balances(account).getOrDefault(account.id(), 0L);
    }

    public TransactionResult execute(
            EconomyTransactionType type,
            EconomyAccount from,
            EconomyAccount to,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem,
            Map<String, String> metadata
    ) {
        synchronized (this) {
            if (!bound) {
                return TransactionResult.failure(TransactionStatus.NOT_BOUND,
                        "Economy is not bound to a running server.");
            }
            if (from == null || to == null || from.equals(to) || !valid(from) || !valid(to)) {
                return recordFailure(type, from, to, amount, actor, reason, sourceSystem,
                        metadata, TransactionStatus.INVALID_ACCOUNT, "Invalid Economy account.");
            }
            if (type == null || !validTypeFlow(type, from, to)) {
                return recordFailure(type, from, to, amount, actor, reason, sourceSystem,
                        metadata, TransactionStatus.INVALID_TYPE_FLOW,
                        "Transaction type does not allow this account flow.");
            }
            if (amount < 1) {
                return recordFailure(type, from, to, amount, actor, reason, sourceSystem,
                        metadata, TransactionStatus.INVALID_AMOUNT, "Amount must be positive.");
            }

            long fromBefore = balance(from);
            long toBefore = balance(to);
            if (from.type() != EconomyAccountType.SYSTEM && fromBefore < amount) {
                return recordFailure(type, from, to, amount, actor, reason, sourceSystem, metadata,
                        TransactionStatus.INSUFFICIENT_FUNDS, "Insufficient funds.");
            }

            long fromAfter;
            long toAfter;
            try {
                fromAfter = from.type() == EconomyAccountType.SYSTEM
                        ? fromBefore
                        : Math.subtractExact(fromBefore, amount);
                toAfter = to.type() == EconomyAccountType.SYSTEM
                        ? toBefore
                        : Math.addExact(toBefore, amount);
            } catch (ArithmeticException exception) {
                return recordFailure(type, from, to, amount, actor, reason, sourceSystem, metadata,
                        TransactionStatus.BALANCE_OVERFLOW, "Balance limit exceeded.");
            }

            EconomyTransaction transaction = transaction(type, from, to, amount, actor, reason,
                    sourceSystem, true, "", fromBefore, fromAfter, toBefore, toAfter, metadata);
            try {
                storage.append(server, transaction, config.forceJournalWrites());
            } catch (IOException exception) {
                logger.error("Failed to persist Economy transaction {}", transaction.id(), exception);
                return TransactionResult.failure(TransactionStatus.PERSISTENCE_FAILED,
                        "Transaction journal write failed.");
            }
            setBalance(from, fromAfter);
            setBalance(to, toAfter);
            state.setLastAppliedSequence(transaction.sequence());
            try {
                historyRecorder.accept(transaction);
            } catch (RuntimeException exception) {
                logger.error("Economy transaction succeeded but history emission failed: {}",
                        transaction.id(), exception);
            }
            return TransactionResult.success(transaction);
        }
    }

    public TransactionResult transfer(
            EconomyAccount from,
            EconomyAccount to,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem
    ) {
        return execute(EconomyTransactionType.TRANSFER, from, to, amount, actor,
                reason, sourceSystem, Map.of());
    }

    public TransactionResult reward(
            EconomyAccount destination,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem
    ) {
        return execute(EconomyTransactionType.REWARD, EconomyAccount.MINT, destination,
                amount, actor, reason, sourceSystem, Map.of());
    }

    public TransactionResult sink(
            EconomyAccount source,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem
    ) {
        return execute(EconomyTransactionType.SINK, source, EconomyAccount.BURN,
                amount, actor, reason, sourceSystem, Map.of());
    }

    public synchronized List<EconomyTransaction> recent(
            Predicate<EconomyTransaction> filter,
            int limit
    ) {
        requireServer();
        int safeLimit = Math.max(1, Math.min(limit, config.queryMaxLimit()));
        return storage.queryRecent(server, filter, safeLimit, config.queryMaxMonths());
    }

    public List<EconomyTransaction> recentFor(EconomyAccount account, int limit) {
        return recent(transaction -> transaction.fromAccount().equals(account)
                || transaction.toAccount().equals(account), limit);
    }

    public synchronized long walletTotal() {
        return sum(state.wallets().values());
    }

    public synchronized long treasuryTotal() {
        return sum(state.treasuries().values());
    }

    public synchronized List<Long> walletBalancesDescending() {
        return state.wallets().values().stream().sorted(Comparator.reverseOrder()).toList();
    }

    public synchronized int walletAccountCount() {
        return state.wallets().size();
    }

    public EconomyConfig config() {
        return config;
    }

    private TransactionResult recordFailure(
            EconomyTransactionType type,
            EconomyAccount from,
            EconomyAccount to,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem,
            Map<String, String> metadata,
            TransactionStatus status,
            String message
    ) {
        if (from == null || to == null || from.equals(to)) {
            return TransactionResult.failure(status, message);
        }
        long fromBalance = balance(from);
        long toBalance = balance(to);
        EconomyTransaction transaction = transaction(
                type == null ? EconomyTransactionType.TRANSFER : type,
                from, to, amount, actor, reason, sourceSystem, false, status.name(),
                fromBalance, fromBalance, toBalance, toBalance, metadata);
        try {
            storage.append(server, transaction, config.forceJournalWrites());
            state.setLastAppliedSequence(transaction.sequence());
            return new TransactionResult(status, message, transaction);
        } catch (IOException exception) {
            logger.error("Failed to persist rejected Economy transaction {}", transaction.id(), exception);
            return TransactionResult.failure(TransactionStatus.PERSISTENCE_FAILED,
                    "Transaction journal write failed.");
        }
    }

    private EconomyTransaction transaction(
            EconomyTransactionType type,
            EconomyAccount from,
            EconomyAccount to,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem,
            boolean success,
            String failure,
            long fromBefore,
            long fromAfter,
            long toBefore,
            long toAfter,
            Map<String, String> metadata
    ) {
        return new EconomyTransaction(state.lastAppliedSequence() + 1, null, 0L, type,
                from, to, amount, actor, reason, sourceSystem, success, failure,
                fromBefore, fromAfter, toBefore, toAfter, metadata);
    }

    private boolean valid(EconomyAccount account) {
        if (account.type() == EconomyAccountType.SYSTEM) return true;
        if (account.type() == EconomyAccountType.REALM) return realmExists.test(account.id());
        try {
            UUID.fromString(account.id());
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static boolean validTypeFlow(
            EconomyTransactionType type,
            EconomyAccount from,
            EconomyAccount to
    ) {
        boolean fromSystem = from.type() == EconomyAccountType.SYSTEM;
        boolean toSystem = to.type() == EconomyAccountType.SYSTEM;
        return switch (type) {
            case TRANSFER -> !fromSystem && !toSystem;
            case DEPOSIT -> from.equals(EconomyAccount.PHYSICAL_SIGIL)
                    && to.type() == EconomyAccountType.PLAYER;
            case WITHDRAW -> from.type() == EconomyAccountType.PLAYER
                    && to.equals(EconomyAccount.PHYSICAL_SIGIL);
            case REWARD -> from.equals(EconomyAccount.MINT) && !toSystem;
            case FEE, SINK -> !fromSystem && to.equals(EconomyAccount.BURN);
            case TAX -> !fromSystem
                    && (to.equals(EconomyAccount.BURN) || to.type() == EconomyAccountType.REALM);
            case TREASURY_GRANT -> from.type() == EconomyAccountType.REALM
                    && to.type() == EconomyAccountType.PLAYER;
            case ADMIN_ADJUSTMENT -> from.equals(EconomyAccount.MINT) && !toSystem
                    || !fromSystem && to.equals(EconomyAccount.BURN);
        };
    }

    private Map<String, Long> balances(EconomyAccount account) {
        return account.type() == EconomyAccountType.PLAYER ? state.wallets() : state.treasuries();
    }

    private void setBalance(EconomyAccount account, long balance) {
        if (account.type() == EconomyAccountType.SYSTEM) return;
        Map<String, Long> balances = balances(account);
        if (balance == 0L) balances.remove(account.id());
        else balances.put(account.id(), balance);
    }

    private Map<String, String> diagnostics() {
        synchronized (this) {
            return Map.of(
                    "state", bound ? "active" : "unbound",
                    "walletAccounts", Integer.toString(state.wallets().size()),
                    "realmTreasuries", Integer.toString(state.treasuries().size()),
                    "walletSigils", Long.toString(walletTotal()),
                    "treasurySigils", Long.toString(treasuryTotal()),
                    "lastSequence", Long.toString(state.lastAppliedSequence())
            );
        }
    }

    private static long sum(Iterable<Long> values) {
        long total = 0L;
        for (Long value : values) {
            try {
                total = Math.addExact(total, value == null ? 0L : value);
            } catch (ArithmeticException exception) {
                return Long.MAX_VALUE;
            }
        }
        return total;
    }

    private void requireServer() {
        if (!bound) throw new IllegalStateException("Economy is not bound to a server");
    }
}
