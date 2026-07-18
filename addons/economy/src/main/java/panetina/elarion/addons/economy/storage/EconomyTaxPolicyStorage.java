package panetina.elarion.addons.economy.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import panetina.elarion.addons.economy.model.EconomyTaxAuthorityKind;
import panetina.elarion.addons.economy.model.EconomyTaxCategory;
import panetina.elarion.core.storage.JsonStateStorage;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EconomyTaxPolicyStorage {
    private static final int SCHEMA_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path fixedRoot;

    public EconomyTaxPolicyStorage() {
        this(null);
    }

    public EconomyTaxPolicyStorage(Path fixedRoot) {
        this.fixedRoot = fixedRoot;
    }

    public StoredPolicies load(MinecraftServer server) {
        Path file = file(server);
        if (Files.notExists(file)) return new StoredPolicies(0L, Map.of());
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            StoredFile stored = GSON.fromJson(reader, StoredFile.class);
            if (stored == null) throw new IllegalStateException("Economy tax policy state is empty: " + file);
            if (stored.schemaVersion != SCHEMA_VERSION) {
                throw new IllegalStateException("Unsupported Economy tax policy schema " + stored.schemaVersion);
            }
            Map<String, Integer> rates = new LinkedHashMap<>();
            for (StoredRate rate : stored.rates == null ? List.<StoredRate>of() : stored.rates) {
                EconomyTaxAuthorityKind kind = EconomyTaxAuthorityKind.valueOf(required(rate.authorityKind, "authorityKind").toUpperCase(Locale.ROOT));
                String authorityId = required(rate.authorityId, "authorityId").toLowerCase(Locale.ROOT);
                EconomyTaxCategory category = EconomyTaxCategory.fromId(required(rate.category, "category"));
                if (rate.basisPoints < 0 || rate.basisPoints > 10_000) {
                    throw new IllegalStateException("Tax basis points must be between 0 and 10000");
                }
                String key = key(kind, authorityId, category);
                if (rates.putIfAbsent(key, rate.basisPoints) != null) {
                    throw new IllegalStateException("Duplicate Economy tax policy " + key);
                }
            }
            return new StoredPolicies(Math.max(0L, stored.revision), Map.copyOf(rates));
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Unable to load Economy tax policies " + file, exception);
        }
    }

    public void save(MinecraftServer server, long revision, Map<String, Integer> rates) {
        List<StoredRate> storedRates = rates.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> StoredRate.from(entry.getKey(), entry.getValue())).toList();
        try {
            JsonStateStorage.writeAtomicChecked(file(server), GSON,
                    new StoredFile(SCHEMA_VERSION, revision, storedRates), "Economy tax policies");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to save Economy tax policies", exception);
        }
    }

    public static String key(EconomyTaxAuthorityKind kind, String authorityId, EconomyTaxCategory category) {
        return kind.name().toLowerCase(Locale.ROOT) + ":" + authorityId.toLowerCase(Locale.ROOT) + ":" + category.id();
    }

    private Path file(MinecraftServer server) {
        Path root = fixedRoot == null ? JsonStateStorage.addonStateRoot(server, "economy") : fixedRoot;
        return root.resolve("tax-policies.json");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalStateException(field + " is required");
        return value.trim();
    }

    public record StoredPolicies(long revision, Map<String, Integer> rates) {
    }

    private record StoredFile(int schemaVersion, long revision, List<StoredRate> rates) {
    }

    private record StoredRate(String authorityKind, String authorityId, String category, int basisPoints) {
        private static StoredRate from(String key, int basisPoints) {
            String[] parts = key.split(":", 3);
            if (parts.length != 3) throw new IllegalStateException("Invalid Economy tax policy key " + key);
            return new StoredRate(parts[0], parts[1], parts[2], basisPoints);
        }
    }
}
