package panetina.elarion.addons.economy.service;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.addons.economy.config.EconomyConfig;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyAccountType;
import panetina.elarion.addons.economy.model.EconomyOperationKey;
import panetina.elarion.addons.economy.model.EconomyOperationReceipt;
import panetina.elarion.addons.economy.model.EconomyTransaction;
import panetina.elarion.addons.economy.model.EconomyTransactionType;
import panetina.elarion.addons.economy.model.TransactionResult;
import panetina.elarion.addons.economy.model.TransactionStatus;
import panetina.elarion.addons.economy.storage.EconomyState;
import panetina.elarion.addons.economy.storage.EconomyStorage;
import panetina.elarion.core.service.ElarionDiagnostics;
import panetina.elarion.core.service.ElarionTaskService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class EconomyTransactionService {
    private static final int RECEIPT_PRUNE_BATCH = 100;
    private static final long RECEIPT_PRUNE_INTERVAL_MILLIS = 60_000L;
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
    private long nextInterestAt;
    private long nextReceiptPruneAt;
    private List<String> interestQueue = List.of();
    private int interestIndex;
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
        this.nextInterestAt = System.currentTimeMillis() + config.bankInterestIntervalMillis();
        pruneExpiredReceipts(System.currentTimeMillis(), Integer.MAX_VALUE);
        enforceReceiptLimit();
        this.nextReceiptPruneAt = System.currentTimeMillis() + RECEIPT_PRUNE_INTERVAL_MILLIS;
    }

    public synchronized void reload(EconomyConfig config) {
        this.config = config;
        this.nextSnapshotAt = System.currentTimeMillis() + config.snapshotIntervalMillis();
        this.nextInterestAt = System.currentTimeMillis() + config.bankInterestIntervalMillis();
        this.interestQueue = List.of();
        this.interestIndex = 0;
        pruneExpiredReceipts(System.currentTimeMillis(), Integer.MAX_VALUE);
        enforceReceiptLimit();
    }

    public synchronized void tick() {
        if (!bound) return;
        long now = System.currentTimeMillis();
        processInterest(now);
        if (now >= nextReceiptPruneAt) {
            pruneExpiredReceipts(now, RECEIPT_PRUNE_BATCH);
            nextReceiptPruneAt = now + RECEIPT_PRUNE_INTERVAL_MILLIS;
        }
        if (now < nextSnapshotAt || !pendingSnapshot.isDone()) return;
        EconomyState snapshot = state.copy();
        nextSnapshotAt = now + config.snapshotIntervalMillis();
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

    public int resetAllPlayerState() {
        pendingSnapshot.join();
        synchronized (this) {
            int changed = state.wallets().size();
            state.wallets().clear();
            state.operationReceipts().clear();
            interestQueue = List.of();
            interestIndex = 0;
            if (bound) storage.save(server, state.copy());
            return changed;
        }
    }

    public synchronized long balance(EconomyAccount account) {
        if (account == null || account.type() == EconomyAccountType.SYSTEM) return 0L;
        if (account.type() == EconomyAccountType.WORLDHEART) return state.worldheartTreasury();
        return balances(account).getOrDefault(account.id(), 0L);
    }

    public synchronized long calculateBankWithdrawalTax(long amount) {
        return calculateBasisPointAmount(amount, config.bankWithdrawalTaxBasisPoints());
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

            Map<String, String> recordedMetadata = operationMessage(metadata, "Transaction completed.");
            EconomyTransaction transaction = transaction(type, from, to, amount, actor, reason,
                    sourceSystem, true, "", fromBefore, fromAfter, toBefore, toAfter, recordedMetadata);
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

    public TransactionResult executeOnce(
            EconomyOperationKey operation,
            EconomyTransactionType type,
            EconomyAccount from,
            EconomyAccount to,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem,
            Map<String, String> metadata
    ) {
        if (operation == null) {
            return TransactionResult.failure(TransactionStatus.IDEMPOTENCY_CONFLICT,
                    "Operation key is required.");
        }
        Map<String, String> safeMetadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        if (containsOperationMetadata(safeMetadata)) {
            return TransactionResult.failure(TransactionStatus.IDEMPOTENCY_CONFLICT,
                    "Operation metadata keys are reserved.");
        }
        String fingerprint = fingerprint(type, from, to, amount, actor, reason, sourceSystem, safeMetadata);
        synchronized (this) {
            EconomyOperationReceipt existing = state.operationReceipts().get(operation.value());
            if (existing != null) {
                return existing.matches(fingerprint)
                        ? existing.result()
                        : TransactionResult.failure(TransactionStatus.IDEMPOTENCY_CONFLICT,
                                "Operation ID was already used for a different request.");
            }
            Map<String, String> recorded = new java.util.LinkedHashMap<>(safeMetadata);
            recorded.put(EconomyOperationReceipt.META_OWNER, operation.owner());
            recorded.put(EconomyOperationReceipt.META_ID, operation.operationId().toString());
            recorded.put(EconomyOperationReceipt.META_FINGERPRINT, fingerprint);
            TransactionResult result = execute(type, from, to, amount, actor, reason, sourceSystem, recorded);
            EconomyOperationReceipt.fromTransaction(result.transaction()).ifPresent(receipt -> {
                state.operationReceipts().put(receipt.key().value(), receipt);
                enforceReceiptLimit();
            });
            return result;
        }
    }

    public synchronized java.util.Optional<EconomyOperationReceipt> receipt(EconomyOperationKey operation) {
        if (operation == null) return java.util.Optional.empty();
        return java.util.Optional.ofNullable(state.operationReceipts().get(operation.value()));
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

    public TransactionResult rewardOnce(
            EconomyOperationKey operation,
            EconomyAccount destination,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem,
            Map<String, String> metadata
    ) {
        return executeOnce(operation, EconomyTransactionType.REWARD, EconomyAccount.MINT, destination,
                amount, actor, reason, sourceSystem, metadata);
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
        return Math.addExact(sum(state.treasuries().values()), state.worldheartTreasury());
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

    private void processInterest(long now) {
        if (!config.bankInterestEnabled() || config.bankInterestRateBasisPoints() <= 0) {
            interestQueue = List.of();
            interestIndex = 0;
            nextInterestAt = now + config.bankInterestIntervalMillis();
            return;
        }
        if (interestQueue.isEmpty()) {
            if (now < nextInterestAt) return;
            interestQueue = state.wallets().keySet().stream().sorted().toList();
            interestIndex = 0;
            nextInterestAt = now + config.bankInterestIntervalMillis();
        }
        int limit = Math.max(1, config.bankInterestMaxAccountsPerTick());
        int processed = 0;
        while (interestIndex < interestQueue.size() && processed++ < limit) {
            String accountId = interestQueue.get(interestIndex++);
            long balance = state.wallets().getOrDefault(accountId, 0L);
            if (balance < config.bankInterestMinimumBalance()) continue;
            long interest = Math.max(
                    config.bankInterestMinimumPayout(),
                    calculateBasisPointAmount(balance, config.bankInterestRateBasisPoints()));
            if (interest < 1L) continue;
            try {
                UUID playerId = UUID.fromString(accountId);
                reward(EconomyAccount.player(playerId), interest, null,
                        "Bank interest", "elarion:economy");
            } catch (IllegalArgumentException ignored) {
                // Invalid account ids are already filtered at transaction boundaries.
            }
        }
        if (interestIndex >= interestQueue.size()) {
            interestQueue = List.of();
            interestIndex = 0;
        }
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
        Map<String, String> recordedMetadata = operationMessage(metadata, message);
        EconomyTransaction transaction = transaction(
                type == null ? EconomyTransactionType.TRANSFER : type,
                from, to, amount, actor, reason, sourceSystem, false, status.name(),
                fromBalance, fromBalance, toBalance, toBalance, recordedMetadata);
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
        if (account.type() == EconomyAccountType.WORLDHEART) {
            return account.equals(EconomyAccount.WORLDHEART_TREASURY);
        }
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
            case DEPOSIT -> from.equals(EconomyAccount.PHYSICAL_CURRENCY)
                    && to.type() == EconomyAccountType.PLAYER;
            case WITHDRAW -> from.type() == EconomyAccountType.PLAYER
                    && to.equals(EconomyAccount.PHYSICAL_CURRENCY);
            case REWARD -> from.equals(EconomyAccount.MINT)
                    && (!toSystem || to.equals(EconomyAccount.PHYSICAL_CURRENCY));
            case FEE, SINK -> (!fromSystem || from.equals(EconomyAccount.PHYSICAL_CURRENCY))
                    && to.equals(EconomyAccount.BURN);
            case PUBLIC_REVENUE -> from.equals(EconomyAccount.PHYSICAL_CURRENCY)
                    && (to.type() == EconomyAccountType.REALM
                    || to.equals(EconomyAccount.WORLDHEART_TREASURY));
            case TAX -> !fromSystem
                    && (to.equals(EconomyAccount.BURN) || to.type() == EconomyAccountType.REALM
                    || to.equals(EconomyAccount.WORLDHEART_TREASURY));
            case TREASURY_GRANT -> from.type() == EconomyAccountType.REALM
                    && to.type() == EconomyAccountType.PLAYER;
            case ADMIN_ADJUSTMENT -> from.equals(EconomyAccount.MINT) && !toSystem
                    || !fromSystem && to.equals(EconomyAccount.BURN);
        };
    }

    private Map<String, Long> balances(EconomyAccount account) {
        if (account.type() == EconomyAccountType.WORLDHEART) {
            throw new IllegalArgumentException("Worldheart treasury uses dedicated balance storage");
        }
        return account.type() == EconomyAccountType.PLAYER ? state.wallets() : state.treasuries();
    }

    private void setBalance(EconomyAccount account, long balance) {
        if (account.type() == EconomyAccountType.SYSTEM) return;
        if (account.type() == EconomyAccountType.WORLDHEART) {
            state.setWorldheartTreasury(balance);
            return;
        }
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
                    "operationReceipts", Integer.toString(state.operationReceipts().size()),
                    "walletCurrency", Long.toString(walletTotal()),
                    "treasuryCurrency", Long.toString(treasuryTotal()),
                    "lastSequence", Long.toString(state.lastAppliedSequence())
            );
        }
    }

    private void pruneExpiredReceipts(long now, int limit) {
        long cutoff = now - config.operationReceiptRetentionMillis();
        int removed = 0;
        Iterator<Map.Entry<String, EconomyOperationReceipt>> iterator =
                state.operationReceipts().entrySet().iterator();
        while (iterator.hasNext() && removed < limit) {
            EconomyOperationReceipt receipt = iterator.next().getValue();
            if (receipt.createdAt() >= cutoff) break;
            iterator.remove();
            removed++;
        }
    }

    private void enforceReceiptLimit() {
        while (state.operationReceipts().size() > config.operationReceiptMaxEntries()) {
            Iterator<String> iterator = state.operationReceipts().keySet().iterator();
            if (!iterator.hasNext()) return;
            iterator.next();
            iterator.remove();
        }
    }

    private static boolean containsOperationMetadata(Map<String, String> metadata) {
        return metadata.containsKey(EconomyOperationReceipt.META_OWNER)
                || metadata.containsKey(EconomyOperationReceipt.META_ID)
                || metadata.containsKey(EconomyOperationReceipt.META_FINGERPRINT)
                || metadata.containsKey(EconomyOperationReceipt.META_MESSAGE);
    }

    private static Map<String, String> operationMessage(Map<String, String> metadata, String message) {
        if (metadata == null || !metadata.containsKey(EconomyOperationReceipt.META_OWNER)) {
            return metadata == null ? Map.of() : metadata;
        }
        Map<String, String> recorded = new java.util.LinkedHashMap<>(metadata);
        recorded.put(EconomyOperationReceipt.META_MESSAGE, message == null ? "" : message);
        return Map.copyOf(recorded);
    }

    private static String fingerprint(
            EconomyTransactionType type,
            EconomyAccount from,
            EconomyAccount to,
            long amount,
            UUID actor,
            String reason,
            String sourceSystem,
            Map<String, String> metadata
    ) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hashPart(digest, type == null ? "" : type.name());
            hashPart(digest, from == null ? "" : from.key());
            hashPart(digest, to == null ? "" : to.key());
            hashPart(digest, Long.toString(amount));
            hashPart(digest, actor == null ? "" : actor.toString());
            hashPart(digest, reason == null ? "" : reason.trim());
            hashPart(digest, sourceSystem == null ? "" : sourceSystem.trim());
            metadata.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                hashPart(digest, entry.getKey());
                hashPart(digest, entry.getValue());
            });
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static void hashPart(MessageDigest digest, String value) {
        byte[] bytes = (value == null ? "" : value).getBytes(StandardCharsets.UTF_8);
        digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
        digest.update(bytes);
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

    static long calculateBasisPointAmount(long amount, int basisPoints) {
        if (amount < 1L || basisPoints < 1) return 0L;
        try {
            long product = Math.multiplyExact(amount, basisPoints);
            if (product > Long.MAX_VALUE - 9_999L) return Long.MAX_VALUE;
            return (product + 9_999L) / 10_000L;
        } catch (ArithmeticException exception) {
            return Long.MAX_VALUE;
        }
    }

    private void requireServer() {
        if (!bound) throw new IllegalStateException("Economy is not bound to a server");
    }
}
