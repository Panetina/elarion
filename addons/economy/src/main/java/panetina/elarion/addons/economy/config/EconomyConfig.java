package panetina.elarion.addons.economy.config;

import org.yaml.snakeyaml.Yaml;
import panetina.elarion.addons.economy.model.EconomyGovernorMode;
import panetina.elarion.core.api.AddonConfigFiles;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public record EconomyConfig(
        int configVersion,
        long snapshotIntervalMillis,
        boolean forceJournalWrites,
        long operationReceiptRetentionMillis,
        int operationReceiptMaxEntries,
        int queryMaxMonths,
        int queryMaxLimit,
        EconomyGovernorMode governorMode,
        int governorWindowDays,
        int governorMaxTransactions,
        boolean bankInterestEnabled,
        long bankInterestIntervalMillis,
        int bankInterestRateBasisPoints,
        long bankInterestMinimumBalance,
        long bankInterestMinimumPayout,
        int bankInterestMaxAccountsPerTick,
        int bankWithdrawalTaxBasisPoints,
        int shopSalesTaxBasisPoints
) {
    private static final String DEFAULT_CONFIG = """
            config-version: 1

            persistence:
              # Balances stay in memory; compact snapshots are written periodically.
              snapshot-interval-seconds: 300

              # Keep true for crash-safe money movements. Each transaction journal
              # append is forced to disk before balances change.
              force-journal-writes: true

            operations:
              receipt-retention-days: 30
              max-receipts: 10000

            queries:
              max-months: 12
              max-limit: 100

            governor:
              # Development begins with observation only. No automatic adjustments.
              mode: "MONITOR_ONLY"
              window-days: 7
              max-transactions: 10000

            bank:
              interest:
                enabled: false
                interval-minutes: 1440
                rate-basis-points: 25
                minimum-balance: 100
                minimum-payout: 1
                max-accounts-per-tick: 100
              withdrawal-tax-basis-points: 0

            shops:
              sales-tax-basis-points: 0
            """;

    public static EconomyConfig load() {
        Path path = AddonConfigFiles.writeDefault("economy", "economy.yml", DEFAULT_CONFIG);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml().load(reader);
            Map<?, ?> root = loaded instanceof Map<?, ?> map ? map : Map.of();
            List<String> errors = new ArrayList<>();
            int version = integer(root.get("config-version"), 1);
            Map<?, ?> persistence = child(root, "persistence");
            Map<?, ?> queries = child(root, "queries");
            Map<?, ?> governor = child(root, "governor");
            Map<?, ?> bank = child(root, "bank");
            Map<?, ?> interest = child(bank, "interest");
            Map<?, ?> shops = child(root, "shops");
            long snapshotSeconds = number(persistence.get("snapshot-interval-seconds"), 300L);
            boolean forceWrites = bool(persistence.get("force-journal-writes"), true);
            Map<?, ?> operations = child(root, "operations");
            long receiptRetentionDays = number(operations.get("receipt-retention-days"), 30L);
            int maxReceipts = integer(operations.get("max-receipts"), 10_000);
            int maxMonths = integer(queries.get("max-months"), 12);
            int maxLimit = integer(queries.get("max-limit"), 100);
            EconomyGovernorMode mode = mode(governor.get("mode"), errors);
            int windowDays = integer(governor.get("window-days"), 7);
            int maxTransactions = integer(governor.get("max-transactions"), 10_000);
            boolean interestEnabled = bool(interest.get("enabled"), false);
            long interestIntervalMinutes = number(interest.get("interval-minutes"), 1440L);
            int interestRateBasisPoints = integer(interest.get("rate-basis-points"), 25);
            long interestMinimumBalance = number(interest.get("minimum-balance"), 100L);
            long interestMinimumPayout = number(interest.get("minimum-payout"), 1L);
            int interestMaxAccountsPerTick = integer(interest.get("max-accounts-per-tick"), 100);
            int withdrawalTaxBasisPoints = integer(bank.get("withdrawal-tax-basis-points"), 0);
            int shopSalesTaxBasisPoints = integer(shops.get("sales-tax-basis-points"), 0);

            if (version != 1) errors.add("config-version: expected 1");
            if (snapshotSeconds < 10) errors.add("persistence.snapshot-interval-seconds: minimum is 10");
            if (receiptRetentionDays < 1 || receiptRetentionDays > 3650) {
                errors.add("operations.receipt-retention-days: expected 1..3650");
            }
            if (maxReceipts < 100 || maxReceipts > 1_000_000) {
                errors.add("operations.max-receipts: expected 100..1000000");
            }
            if (maxMonths < 1 || maxMonths > 120) errors.add("queries.max-months: expected 1..120");
            if (maxLimit < 1 || maxLimit > 1000) errors.add("queries.max-limit: expected 1..1000");
            if (windowDays < 1 || windowDays > 365) errors.add("governor.window-days: expected 1..365");
            if (maxTransactions < 100 || maxTransactions > 1_000_000) {
                errors.add("governor.max-transactions: expected 100..1000000");
            }
            if (interestIntervalMinutes < 1 || interestIntervalMinutes > 525_600) {
                errors.add("bank.interest.interval-minutes: expected 1..525600");
            }
            if (interestRateBasisPoints < 0 || interestRateBasisPoints > 10_000) {
                errors.add("bank.interest.rate-basis-points: expected 0..10000");
            }
            if (interestMinimumBalance < 0) errors.add("bank.interest.minimum-balance: minimum is 0");
            if (interestMinimumPayout < 1) errors.add("bank.interest.minimum-payout: minimum is 1");
            if (interestMaxAccountsPerTick < 1 || interestMaxAccountsPerTick > 10_000) {
                errors.add("bank.interest.max-accounts-per-tick: expected 1..10000");
            }
            if (withdrawalTaxBasisPoints < 0 || withdrawalTaxBasisPoints > 10_000) {
                errors.add("bank.withdrawal-tax-basis-points: expected 0..10000");
            }
            if (shopSalesTaxBasisPoints < 0 || shopSalesTaxBasisPoints > 10_000) {
                errors.add("shops.sales-tax-basis-points: expected 0..10000");
            }
            if (!errors.isEmpty()) {
                throw new IllegalStateException("Invalid Economy config " + path + ": "
                        + String.join("; ", errors));
            }
            return new EconomyConfig(version, snapshotSeconds * 1000L, forceWrites,
                    receiptRetentionDays * 86_400_000L, maxReceipts,
                    maxMonths, maxLimit, mode, windowDays, maxTransactions,
                    interestEnabled, interestIntervalMinutes * 60_000L,
                    interestRateBasisPoints, interestMinimumBalance, interestMinimumPayout,
                    interestMaxAccountsPerTick, withdrawalTaxBasisPoints, shopSalesTaxBasisPoints);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load Economy config " + path, exception);
        }
    }

    private static Map<?, ?> child(Map<?, ?> root, String key) {
        return root.get(key) instanceof Map<?, ?> map ? map : Map.of();
    }

    private static long number(Object value, long fallback) {
        return value instanceof Number number ? number.longValue() : fallback;
    }

    private static int integer(Object value, int fallback) {
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static boolean bool(Object value, boolean fallback) {
        return value instanceof Boolean bool ? bool : fallback;
    }

    private static EconomyGovernorMode mode(Object value, List<String> errors) {
        try {
            return EconomyGovernorMode.valueOf(String.valueOf(
                    value == null ? "MONITOR_ONLY" : value).trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            errors.add("governor.mode: expected OFF, MONITOR_ONLY, SUGGEST_ONLY, AUTO_LIMITED, or AUTO_FULL");
            return EconomyGovernorMode.MONITOR_ONLY;
        }
    }
}
