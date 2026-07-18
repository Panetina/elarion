package panetina.elarion.addons.npcs.service;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.addons.npcs.model.NpcBankQuote;

public interface NpcBankQuoteProvider {
    NpcBankQuote quote(ServerPlayerEntity player, String mode, int amount);

    static NpcBankQuoteProvider unavailable() {
        return (player, mode, amount) -> new NpcBankQuote(
                mode, Math.max(0, amount), 0L, 0, 0, 0L, 0L,
                false, "Economy services are unavailable.");
    }
}
