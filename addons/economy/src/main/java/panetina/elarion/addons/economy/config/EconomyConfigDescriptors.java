package panetina.elarion.addons.economy.config;

import panetina.elarion.addons.economy.model.EconomyGovernorMode;
import panetina.elarion.addons.economy.model.EconomyServicePrice;
import panetina.elarion.core.config.ElarionConfigCategory;
import panetina.elarion.core.config.ElarionConfigCodec;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigEntry;
import panetina.elarion.core.config.ElarionConfigPermission;
import panetina.elarion.core.config.ElarionConfigRegistry;
import panetina.elarion.core.config.ElarionConfigValidator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

public final class EconomyConfigDescriptors {
    private static final EconomyConfig DEFAULTS = new EconomyConfig(
            1,
            300_000L,
            true,
            2_592_000_000L,
            10_000,
            12,
            100,
            EconomyGovernorMode.MONITOR_ONLY,
            7,
            10_000,
            false,
            86_400_000L,
            25,
            100L,
            1L,
            100,
            0,
            0);
    private static final Map<String, EconomyServicePrice> DEFAULT_PRICES = defaultPrices();

    private EconomyConfigDescriptors() {
    }

    public static void register(
            ElarionConfigRegistry registry,
            Supplier<EconomyConfig> config,
            Supplier<Map<String, EconomyServicePrice>> prices
    ) {
        registry.registerDomain(domain(config, prices));
    }

    public static ElarionConfigDomain domain(
            Supplier<EconomyConfig> config,
            Supplier<Map<String, EconomyServicePrice>> prices
    ) {
        return new ElarionConfigDomain(
                "economy",
                "addons:economy",
                "Economy",
                "Currency persistence, bounded transaction queries, governor, and service prices.",
                List.of(
                        "config/elarion/addons/economy/economy.yml",
                        "config/elarion/addons/economy/service_prices.yml"),
                "/e economy reload",
                List.of(
                        new ElarionConfigCategory(
                                "persistence",
                                "Persistence",
                                "Snapshot and transaction-journal behavior.",
                                List.of(
                                        intEntry("config-version", "Config Version",
                                                "Economy config schema version.",
                                                "economy.yml.config-version",
                                                DEFAULTS.configVersion(), () -> config.get().configVersion(),
                                                1, 1, false),
                                        longEntry("persistence.snapshot-interval-seconds",
                                                "Snapshot Interval Seconds",
                                                "Seconds between compact balance snapshots.",
                                                "economy.yml.persistence.snapshot-interval-seconds",
                                                DEFAULTS.snapshotIntervalMillis() / 1000L,
                                                () -> config.get().snapshotIntervalMillis() / 1000L,
                                                10L, "", true),
                                        boolEntry("persistence.force-journal-writes",
                                                "Force Journal Writes",
                                                "Forces each transaction journal append before balances change.",
                                                "economy.yml.persistence.force-journal-writes",
                                                DEFAULTS.forceJournalWrites(),
                                                () -> config.get().forceJournalWrites()),
                                        longEntry("operations.receipt-retention-days",
                                                "Operation Receipt Retention Days",
                                                "Days successful or rejected idempotent operation receipts remain replayable.",
                                                "economy.yml.operations.receipt-retention-days",
                                                DEFAULTS.operationReceiptRetentionMillis() / 86_400_000L,
                                                () -> config.get().operationReceiptRetentionMillis() / 86_400_000L,
                                                1L, "3650", true),
                                        intEntry("operations.max-receipts", "Max Operation Receipts",
                                                "Maximum O(1) idempotent operation receipts retained in the Economy snapshot.",
                                                "economy.yml.operations.max-receipts",
                                                DEFAULTS.operationReceiptMaxEntries(),
                                                () -> config.get().operationReceiptMaxEntries(),
                                                100, 1_000_000, true))),
                        new ElarionConfigCategory(
                                "queries",
                                "Queries",
                                "Bounds for player-facing and admin transaction lookups.",
                                List.of(
                                        intEntry("queries.max-months", "Max Query Months",
                                                "Maximum transaction history window in months.",
                                                "economy.yml.queries.max-months",
                                                DEFAULTS.queryMaxMonths(), () -> config.get().queryMaxMonths(),
                                                1, 120, true),
                                        intEntry("queries.max-limit", "Max Query Limit",
                                                "Maximum transaction rows returned by one query.",
                                                "economy.yml.queries.max-limit",
                                                DEFAULTS.queryMaxLimit(), () -> config.get().queryMaxLimit(),
                                                1, 1000, true))),
                        new ElarionConfigCategory(
                                "governor",
                                "Governor",
                                "Economy Governor observation and future adjustment bounds.",
                                List.of(
                                        enumStringEntry("governor.mode", "Governor Mode",
                                                "Current Economy Governor mode.",
                                                "economy.yml.governor.mode",
                                                DEFAULTS.governorMode().name(),
                                                () -> config.get().governorMode().name(),
                                                governorChoices()),
                                        intEntry("governor.window-days", "Window Days",
                                                "Real-world days used for the current Governor pulse window.",
                                                "economy.yml.governor.window-days",
                                                DEFAULTS.governorWindowDays(),
                                                () -> config.get().governorWindowDays(),
                                                1, 365, true),
                                        intEntry("governor.max-transactions", "Max Transactions",
                                                "Maximum transactions examined by one Governor pulse.",
                                                "economy.yml.governor.max-transactions",
                                                DEFAULTS.governorMaxTransactions(),
                                                () -> config.get().governorMaxTransactions(),
                                                100, 1_000_000, true))),
                        new ElarionConfigCategory(
                                "bank",
                                "Bank",
                                "Bank interest and withdrawal tax policy.",
                                List.of(
                                        boolEntry("bank.interest.enabled", "Bank Interest Enabled",
                                                "When true, deposited bank balances can earn periodic interest.",
                                                "economy.yml.bank.interest.enabled",
                                                DEFAULTS.bankInterestEnabled(),
                                                () -> config.get().bankInterestEnabled()),
                                        longEntry("bank.interest.interval-minutes", "Interest Interval Minutes",
                                                "Minutes between bank interest pulses.",
                                                "economy.yml.bank.interest.interval-minutes",
                                                DEFAULTS.bankInterestIntervalMillis() / 60_000L,
                                                () -> config.get().bankInterestIntervalMillis() / 60_000L,
                                                1L, "525600", true),
                                        intEntry("bank.interest.rate-basis-points", "Interest Rate Basis Points",
                                                "Interest paid per interval. 100 basis points equals 1%.",
                                                "economy.yml.bank.interest.rate-basis-points",
                                                DEFAULTS.bankInterestRateBasisPoints(),
                                                () -> config.get().bankInterestRateBasisPoints(),
                                                0, 10_000, true),
                                        longEntry("bank.interest.minimum-balance", "Interest Minimum Balance",
                                                "Minimum deposited bank balance eligible for interest.",
                                                "economy.yml.bank.interest.minimum-balance",
                                                DEFAULTS.bankInterestMinimumBalance(),
                                                () -> config.get().bankInterestMinimumBalance(),
                                                0L, "", true),
                                        longEntry("bank.interest.minimum-payout", "Interest Minimum Payout",
                                                "Smallest interest payout minted when a balance qualifies.",
                                                "economy.yml.bank.interest.minimum-payout",
                                                DEFAULTS.bankInterestMinimumPayout(),
                                                () -> config.get().bankInterestMinimumPayout(),
                                                1L, "", true),
                                        intEntry("bank.interest.max-accounts-per-tick",
                                                "Interest Max Accounts Per Tick",
                                                "Maximum bank accounts processed by one server tick during an interest pulse.",
                                                "economy.yml.bank.interest.max-accounts-per-tick",
                                                DEFAULTS.bankInterestMaxAccountsPerTick(),
                                                () -> config.get().bankInterestMaxAccountsPerTick(),
                                                1, 10_000, true),
                                        intEntry("bank.withdrawal-tax-basis-points",
                                                "Withdrawal Tax Basis Points",
                                                "Tax charged from bank balance when withdrawing physical currency. Deposits are untaxed.",
                                                "economy.yml.bank.withdrawal-tax-basis-points",
                                                DEFAULTS.bankWithdrawalTaxBasisPoints(),
                                                () -> config.get().bankWithdrawalTaxBasisPoints(),
                                                0, 10_000, true))),
                        new ElarionConfigCategory(
                                "shops",
                                "Shops",
                                "Shop tax policy for future server-authoritative merchant purchases.",
                                List.of(
                                        intEntry("shops.sales-tax-basis-points", "Sales Tax Basis Points",
                                                "Tax rate reserved for future shop/trader purchase settlement. 100 basis points equals 1%.",
                                                "economy.yml.shops.sales-tax-basis-points",
                                                DEFAULTS.shopSalesTaxBasisPoints(),
                                                () -> config.get().shopSalesTaxBasisPoints(),
                                                0, 10_000, true))),
                        new ElarionConfigCategory(
                                "service-prices",
                                "Service Prices",
                                "Economy-owned price IDs consumed by other addons.",
                                servicePriceEntries(prices))));
    }

    private static List<ElarionConfigEntry<?>> servicePriceEntries(
            Supplier<Map<String, EconomyServicePrice>> prices
    ) {
        Map<String, EconomyServicePrice> current = safePrices(prices);
        List<String> ids = new ArrayList<>(DEFAULT_PRICES.keySet());
        for (String id : current.keySet()) {
            if (!ids.contains(id)) ids.add(id);
        }
        ids.sort(Comparator.naturalOrder());
        List<ElarionConfigEntry<?>> entries = new ArrayList<>();
        for (String id : ids) {
            EconomyServicePrice fallback = DEFAULT_PRICES.getOrDefault(id, current.get(id));
            entries.add(priceEntry(id, "base", "Base", fallback.base(), prices, EconomyServicePrice::base));
            entries.add(priceEntry(id, "minimum", "Minimum", fallback.minimum(), prices, EconomyServicePrice::minimum));
            entries.add(priceEntry(id, "maximum", "Maximum", fallback.maximum(), prices, EconomyServicePrice::maximum));
        }
        return entries;
    }

    private static ElarionConfigEntry<Long> priceEntry(
            String id,
            String field,
            String label,
            long defaultValue,
            Supplier<Map<String, EconomyServicePrice>> prices,
            ToLongFunction<EconomyServicePrice> value
    ) {
        String entryId = "service-prices." + id + "." + field;
        return new ElarionConfigEntry<>(
                entryId,
                id + " " + label,
                label + " configured currency amount for service price `" + id + "`.",
                "service_prices.yml.prices." + id + "." + field,
                ElarionConfigCodec.LONG,
                defaultValue,
                () -> priceValue(prices, id, defaultValue, value),
                ElarionConfigValidator.longMinimum("service_prices.yml.prices." + id + "." + field, 0L),
                List.of(),
                "0",
                "",
                true,
                false,
                ElarionConfigPermission.OPERATOR,
                ElarionConfigPermission.OPERATOR);
    }

    private static long priceValue(
            Supplier<Map<String, EconomyServicePrice>> prices,
            String id,
            long fallback,
            ToLongFunction<EconomyServicePrice> value
    ) {
        EconomyServicePrice price = safePrices(prices).get(id);
        return price == null ? fallback : value.applyAsLong(price);
    }

    private static ElarionConfigEntry<Boolean> boolEntry(
            String id,
            String label,
            String description,
            String path,
            boolean defaultValue,
            Supplier<Boolean> currentValue
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.BOOLEAN, defaultValue, currentValue,
                ElarionConfigValidator.pass(), List.of("true", "false"), "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Integer> intEntry(
            String id,
            String label,
            String description,
            String path,
            int defaultValue,
            Supplier<Integer> currentValue,
            int minimum,
            int maximum,
            boolean runtimeReloadable
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.INTEGER, defaultValue, currentValue,
                ElarionConfigValidator.integerRange(path, minimum, maximum), List.of(),
                Integer.toString(minimum), Integer.toString(maximum),
                runtimeReloadable, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<Long> longEntry(
            String id,
            String label,
            String description,
            String path,
            long defaultValue,
            Supplier<Long> currentValue,
            long minimum,
            String maximum,
            boolean runtimeReloadable
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.LONG, defaultValue, currentValue,
                ElarionConfigValidator.longMinimum(path, minimum), List.of(),
                Long.toString(minimum), maximum,
                runtimeReloadable, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static ElarionConfigEntry<String> enumStringEntry(
            String id,
            String label,
            String description,
            String path,
            String defaultValue,
            Supplier<String> currentValue,
            List<String> choices
    ) {
        return new ElarionConfigEntry<>(
                id, label, description, path, ElarionConfigCodec.STRING, defaultValue, currentValue,
                ElarionConfigValidator.nonBlank(path), choices, "", "",
                true, false, ElarionConfigPermission.OPERATOR, ElarionConfigPermission.OPERATOR);
    }

    private static List<String> governorChoices() {
        List<String> choices = new ArrayList<>();
        for (EconomyGovernorMode mode : EconomyGovernorMode.values()) {
            choices.add(mode.name());
        }
        return choices;
    }

    private static Map<String, EconomyServicePrice> safePrices(
            Supplier<Map<String, EconomyServicePrice>> prices
    ) {
        Map<String, EconomyServicePrice> value = prices == null ? null : prices.get();
        return value == null ? Map.of() : value;
    }

    private static Map<String, EconomyServicePrice> defaultPrices() {
        Map<String, EconomyServicePrice> prices = new LinkedHashMap<>();
        prices.put("portal_ticket.nether", new EconomyServicePrice("portal_ticket.nether", 25L, 15L, 60L));
        prices.put("portal_ticket.end", new EconomyServicePrice("portal_ticket.end", 150L, 100L, 400L));
        prices.put("ancient_gate.passage", new EconomyServicePrice("ancient_gate.passage", 5L, 1L, 15L));
        return Map.copyOf(prices);
    }
}
