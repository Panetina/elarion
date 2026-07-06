package panetina.elarion.addons.economy.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import panetina.elarion.addons.economy.model.EconomyAccount;
import panetina.elarion.addons.economy.model.EconomyAccountType;
import panetina.elarion.addons.economy.model.EconomyTransaction;
import panetina.elarion.core.service.ElarionPerformanceMonitor;
import panetina.elarion.core.storage.JsonStateStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class EconomyStorage {
    private static final int STATE_SCHEMA_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC);
    private final Logger logger;
    private final Path fixedRoot;

    public EconomyStorage(Logger logger) {
        this(logger, null);
    }

    public EconomyStorage(Logger logger, Path fixedRoot) {
        this.logger = logger;
        this.fixedRoot = fixedRoot;
    }

    public EconomyState load(MinecraftServer server) {
        EconomyState state = JsonStateStorage.read(stateFile(server), PRETTY_GSON, StoredState.class,
                EconomyState::new, StoredState::toState, logger, "Economy balance state");
        List<EconomyTransaction> pending = transactionsAfter(server, state.lastAppliedSequence());
        pending.forEach(transaction -> apply(state, transaction));
        return state;
    }

    public void save(MinecraftServer server, EconomyState state) {
        JsonStateStorage.writeAtomic(stateFile(server), PRETTY_GSON, StoredState.from(state),
                logger, "Economy balance state");
    }

    public void append(MinecraftServer server, EconomyTransaction transaction, boolean force) throws IOException {
        long started = System.nanoTime();
        Path file = transactionFile(server, transaction.timestamp());
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(transaction) + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (force) {
                try (FileChannel channel = FileChannel.open(file, StandardOpenOption.WRITE)) {
                    channel.force(false);
                }
            }
        } finally {
            ElarionPerformanceMonitor.record("economy-transaction-journal", System.nanoTime() - started);
        }
    }

    public List<EconomyTransaction> queryRecent(
            MinecraftServer server,
            Predicate<EconomyTransaction> filter,
            int limit,
            int maxMonths
    ) {
        List<Path> files = transactionFiles(server, maxMonths);
        List<EconomyTransaction> result = new ArrayList<>();
        for (Path file : files) {
            collectRecent(file, filter, limit, result);
            if (result.size() >= limit) break;
        }
        return List.copyOf(result);
    }

    private List<EconomyTransaction> transactionsAfter(MinecraftServer server, long sequence) {
        List<EconomyTransaction> result = new ArrayList<>();
        for (Path file : transactionFiles(server, Integer.MAX_VALUE)) {
            List<EconomyTransaction> entries = readFile(file);
            boolean reachedSnapshot = false;
            for (EconomyTransaction entry : entries) {
                if (entry.sequence() > sequence) result.add(entry);
                else reachedSnapshot = true;
            }
            if (reachedSnapshot) break;
        }
        result.sort(Comparator.comparingLong(EconomyTransaction::sequence));
        return result;
    }

    private List<Path> transactionFiles(MinecraftServer server, int maxMonths) {
        Path directory = root(server).resolve("transactions");
        if (Files.notExists(directory)) return List.of();
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.filter(path -> path.getFileName().toString().endsWith(".jsonl"))
                    .sorted(Comparator.comparing((Path path) -> path.getFileName().toString()).reversed())
                    .limit(maxMonths)
                    .toList();
        } catch (IOException exception) {
            logger.error("Failed to list Economy transaction files", exception);
            return List.of();
        }
    }

    private List<EconomyTransaction> readFile(Path file) {
        List<EconomyTransaction> result = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    EconomyTransaction transaction = GSON.fromJson(line, EconomyTransaction.class);
                    if (transaction != null) result.add(transaction);
                } catch (RuntimeException exception) {
                    logger.warn("Skipping invalid Economy transaction in {}", file, exception);
                }
            }
        } catch (IOException exception) {
            logger.error("Failed to read Economy transactions {}", file, exception);
        }
        return result;
    }

    private void collectRecent(
            Path file,
            Predicate<EconomyTransaction> filter,
            int limit,
            List<EconomyTransaction> result
    ) {
        int remaining = limit - result.size();
        if (remaining <= 0) return;
        ArrayDeque<EconomyTransaction> newestInFile = new ArrayDeque<>(remaining);
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    EconomyTransaction transaction = GSON.fromJson(line, EconomyTransaction.class);
                    if (transaction != null && filter.test(transaction)) {
                        if (newestInFile.size() == remaining) newestInFile.removeFirst();
                        newestInFile.addLast(transaction);
                    }
                } catch (RuntimeException exception) {
                    logger.warn("Skipping invalid Economy transaction in {}", file, exception);
                }
            }
        } catch (IOException exception) {
            logger.error("Failed to read Economy transactions {}", file, exception);
        }
        List<EconomyTransaction> entries = new ArrayList<>(newestInFile);
        for (int index = entries.size() - 1; index >= 0 && result.size() < limit; index--) {
            result.add(entries.get(index));
        }
    }

    private Path stateFile(MinecraftServer server) {
        return root(server).resolve("economy-state.json");
    }

    private Path transactionFile(MinecraftServer server, long timestamp) {
        return root(server).resolve("transactions").resolve(MONTH.format(Instant.ofEpochMilli(timestamp)) + ".jsonl");
    }

    private Path root(MinecraftServer server) {
        return fixedRoot == null ? JsonStateStorage.addonStateRoot(server, "economy") : fixedRoot;
    }

    private static void apply(EconomyState state, EconomyTransaction transaction) {
        if (transaction.success()) {
            change(state, transaction.fromAccount(), -transaction.amount());
            change(state, transaction.toAccount(), transaction.amount());
        }
        state.setLastAppliedSequence(transaction.sequence());
    }

    private static void change(EconomyState state, EconomyAccount account, long delta) {
        if (account.type() == EconomyAccountType.SYSTEM) return;
        Map<String, Long> balances = account.type() == EconomyAccountType.PLAYER
                ? state.wallets()
                : state.treasuries();
        long updated = Math.addExact(balances.getOrDefault(account.id(), 0L), delta);
        if (updated == 0L) balances.remove(account.id());
        else balances.put(account.id(), updated);
    }

    private static final class StoredState {
        int schemaVersion = STATE_SCHEMA_VERSION;
        long lastAppliedSequence;
        Map<String, Long> wallets = new LinkedHashMap<>();
        Map<String, Long> treasuries = new LinkedHashMap<>();

        static StoredState from(EconomyState state) {
            StoredState stored = new StoredState();
            stored.lastAppliedSequence = state.lastAppliedSequence();
            stored.wallets.putAll(state.wallets());
            stored.treasuries.putAll(state.treasuries());
            return stored;
        }

        EconomyState toState() {
            if (schemaVersion != STATE_SCHEMA_VERSION) {
                throw new IllegalStateException("Unsupported Economy state schema " + schemaVersion);
            }
            EconomyState state = new EconomyState();
            state.setLastAppliedSequence(lastAppliedSequence);
            if (wallets != null) wallets.forEach((id, value) -> putPositive(state.wallets(), id, value));
            if (treasuries != null) treasuries.forEach((id, value) -> putPositive(state.treasuries(), id, value));
            return state;
        }

        private static void putPositive(Map<String, Long> target, String id, Long value) {
            if (id != null && !id.isBlank() && value != null && value > 0) target.put(id, value);
        }
    }
}
