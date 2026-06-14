package panetina.elarion.addons.economy;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.economy.command.EconomyCommands;
import panetina.elarion.addons.economy.config.EconomyConfig;
import panetina.elarion.addons.economy.registry.EconomyNpcActions;
import panetina.elarion.addons.economy.registry.EconomyRewardActions;
import panetina.elarion.addons.economy.service.EconomyGovernorService;
import panetina.elarion.addons.economy.service.EconomyInventoryService;
import panetina.elarion.addons.economy.service.EconomyPricingService;
import panetina.elarion.addons.economy.service.EconomyTransactionService;
import panetina.elarion.addons.economy.storage.EconomyStorage;
import panetina.elarion.core.api.ElarionAddon;
import panetina.elarion.core.api.ElarionApi;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ElarionEconomyAddon implements ElarionAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger("elarion_economy");

    @Override
    public void initialize(ElarionApi api) {
        EconomyItems.register();
        EconomyConfig config = EconomyConfig.load();
        EconomyTransactionService transactions =
                new EconomyTransactionService(
                        LOGGER,
                        new EconomyStorage(LOGGER),
                        config,
                        realmId -> api.realms().find(realmId).isPresent(),
                        transaction -> recordHistory(api, transaction),
                        api.system().tasks());
        EconomyInventoryService inventory = new EconomyInventoryService(transactions);
        EconomyGovernorService governor = new EconomyGovernorService(transactions);
        EconomyPricingService pricing = new EconomyPricingService();
        new ElarionEconomyApi(transactions, inventory, governor, pricing);
        EconomyNpcActions.register(api, transactions, inventory);
        EconomyRewardActions.register(api, transactions);

        api.system().abilities().register("elarion.economy.admin");
        api.system().commands().registerAdminSubcommand(
                () -> EconomyCommands.create(api, transactions, inventory, governor, pricing));
        api.system().commands().registerHelpDescription(
                "/e economy wallet ...", "Inspect or adjust player wallets.");
        api.system().commands().registerHelpDescription(
                "/e economy treasury ...", "Inspect or adjust Realm treasuries.");
        api.system().commands().registerHelpDescription(
                "/e economy transactions ...", "Inspect bounded Economy transaction history.");
        api.system().commands().registerHelpDescription(
                "/e economy pulse", "Show the monitor-only Economy Governor pulse.");

        ServerLifecycleEvents.SERVER_STARTED.register(transactions::bind);
        ServerTickEvents.END_SERVER_TICK.register(server -> transactions.tick());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> transactions.shutdown());
        LOGGER.info("Elarion Economy initialized");
    }

    private static void recordHistory(
            ElarionApi api,
            panetina.elarion.addons.economy.model.EconomyTransaction transaction
    ) {
        String realmId = transaction.toAccount().type()
                == panetina.elarion.addons.economy.model.EconomyAccountType.REALM
                ? transaction.toAccount().id()
                : transaction.fromAccount().type()
                == panetina.elarion.addons.economy.model.EconomyAccountType.REALM
                ? transaction.fromAccount().id()
                : "";
        Map<String, String> metadata = new LinkedHashMap<>(transaction.metadata());
        metadata.put("transactionId", transaction.id().toString());
        metadata.put("transactionType", transaction.type().name());
        metadata.put("fromAccount", transaction.fromAccount().key());
        metadata.put("toAccount", transaction.toAccount().key());
        metadata.put("amount", Long.toString(transaction.amount()));
        metadata.put("sourceSystem", transaction.sourceSystem());
        api.history().recordChronicle(
                "economy",
                "transaction-" + transaction.type().name().toLowerCase(java.util.Locale.ROOT),
                transaction.actor(),
                "economy_transaction",
                transaction.id().toString(),
                realmId,
                metadata,
                "A transaction of " + api.serverIdentity().currencyAmount(transaction.amount()) + " was recorded for "
                        + transaction.reason() + "."
        );
    }
}
