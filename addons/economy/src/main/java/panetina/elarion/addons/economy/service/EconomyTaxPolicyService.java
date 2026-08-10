package panetina.elarion.addons.economy.service;

import net.minecraft.server.MinecraftServer;
import panetina.elarion.addons.economy.model.EconomyTaxAuthority;
import panetina.elarion.addons.economy.model.EconomyTaxCategory;
import panetina.elarion.addons.economy.model.EconomyTaxQuote;
import panetina.elarion.addons.economy.model.EconomyTaxPolicySnapshot;
import panetina.elarion.addons.economy.storage.EconomyTaxPolicyStorage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntSupplier;

public final class EconomyTaxPolicyService {
    public static final int MAX_QUANTITY = 64;
    private final EconomyTaxPolicyStorage storage;
    private final IntSupplier defaultNpcTradeRate;
    private final Map<String, Integer> rates = new LinkedHashMap<>();
    private MinecraftServer server;
    private boolean bound;
    private long revision;

    public EconomyTaxPolicyService(EconomyTaxPolicyStorage storage, IntSupplier defaultNpcTradeRate) {
        this.storage = storage;
        this.defaultNpcTradeRate = defaultNpcTradeRate;
    }

    public synchronized void bind(MinecraftServer server) {
        EconomyTaxPolicyStorage.StoredPolicies stored = storage.load(server);
        this.server = server;
        this.bound = true;
        this.revision = stored.revision();
        rates.clear();
        rates.putAll(stored.rates());
    }

    public synchronized int rate(EconomyTaxAuthority authority, EconomyTaxCategory category) {
        Integer explicit = rates.get(key(authority, category));
        if (explicit != null) return explicit;
        return category == EconomyTaxCategory.NPC_TRADE
                ? Math.max(0, Math.min(10_000, defaultNpcTradeRate.getAsInt())) : 0;
    }

    public synchronized long revision() {
        return revision;
    }

    public synchronized void setRate(EconomyTaxAuthority authority, EconomyTaxCategory category, int basisPoints) {
        setRates(authority, revision, Map.of(category, basisPoints));
    }

    public synchronized EconomyTaxPolicySnapshot snapshot(EconomyTaxAuthority authority) {
        Map<EconomyTaxCategory, Integer> values = new LinkedHashMap<>();
        for (EconomyTaxCategory category : EconomyTaxCategory.values()) values.put(category, rate(authority, category));
        return new EconomyTaxPolicySnapshot(authority, revision, values);
    }

    public synchronized void setRates(
            EconomyTaxAuthority authority,
            long expectedRevision,
            Map<EconomyTaxCategory, Integer> updatedRates
    ) {
        if (!bound) throw new IllegalStateException("Economy tax policy service is not bound");
        if (expectedRevision != revision) throw new IllegalStateException("Tax policy changed; reload before applying.");
        if (updatedRates == null || updatedRates.isEmpty()) return;
        for (Map.Entry<EconomyTaxCategory, Integer> entry : updatedRates.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() < 0 || entry.getValue() > 2_500) {
                throw new IllegalArgumentException("Seat tax rates must be between 0 and 2500 basis points");
            }
        }
        Map<String, Integer> candidate = new LinkedHashMap<>(rates);
        updatedRates.forEach((category, rate) -> candidate.put(key(authority, category), rate));
        long candidateRevision = revision + 1L;
        storage.save(server, candidateRevision, candidate);
        rates.clear();
        rates.putAll(candidate);
        revision = candidateRevision;
    }

    public synchronized EconomyTaxQuote quote(
            EconomyTaxAuthority authority,
            EconomyTaxCategory category,
            long unitPrice,
            int quantity,
            int maxQuantity
    ) {
        int boundedMax = Math.max(1, Math.min(MAX_QUANTITY, maxQuantity));
        if (unitPrice < 0L || quantity < 1 || quantity > boundedMax) {
            return invalid(authority, category, unitPrice, quantity, boundedMax, "Invalid trade quantity or price.");
        }
        final long subtotal;
        try {
            subtotal = Math.multiplyExact(unitPrice, quantity);
        } catch (ArithmeticException exception) {
            return invalid(authority, category, unitPrice, quantity, boundedMax, "Trade total is too large.");
        }
        int basisPoints = rate(authority, category);
        long tax = subtotal / 10_000L * basisPoints + subtotal % 10_000L * basisPoints / 10_000L;
        final long total;
        try {
            total = Math.addExact(subtotal, tax);
        } catch (ArithmeticException exception) {
            return invalid(authority, category, unitPrice, quantity, boundedMax, "Trade total is too large.");
        }
        return new EconomyTaxQuote(authority, category, unitPrice, quantity, boundedMax, subtotal,
                basisPoints, tax, total, revision, true, "");
    }

    private EconomyTaxQuote invalid(
            EconomyTaxAuthority authority, EconomyTaxCategory category, long unitPrice,
            int quantity, int maxQuantity, String message
    ) {
        return new EconomyTaxQuote(authority, category, unitPrice, quantity, maxQuantity,
                0L, rate(authority, category), 0L, 0L, revision, false, message);
    }

    private static String key(EconomyTaxAuthority authority, EconomyTaxCategory category) {
        return EconomyTaxPolicyStorage.key(authority.kind(), authority.id(), category);
    }
}
