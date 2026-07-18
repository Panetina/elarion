package panetina.elarion.addons.npcs.integration;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.economy.api.ElarionEconomyApi;
import panetina.elarion.addons.economy.model.EconomyBankMode;
import panetina.elarion.addons.economy.model.EconomyBankQuote;
import panetina.elarion.addons.npcs.model.NpcBankQuote;
import panetina.elarion.addons.npcs.service.NpcBankQuoteProvider;

public final class EconomyNpcBankQuoteProvider implements NpcBankQuoteProvider {
    @Override
    public NpcBankQuote quote(ServerPlayerEntity player, String mode, int amount) {
        EconomyBankMode bankMode = EconomyBankMode.fromRole(mode).orElse(EconomyBankMode.DEPOSIT);
        EconomyBankQuote quote = ElarionEconomyApi.get().quoteBank(player, bankMode, amount);
        return new NpcBankQuote(bankMode.role(), quote.amount(), quote.balance(),
                quote.physicalCurrency(), quote.taxBasisPoints(), quote.fee(),
                quote.total(), quote.valid(), quote.message());
    }
}
