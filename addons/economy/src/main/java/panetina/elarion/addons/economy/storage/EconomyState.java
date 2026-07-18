package panetina.elarion.addons.economy.storage;

import panetina.elarion.addons.economy.model.EconomyOperationReceipt;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EconomyState {
    private long lastAppliedSequence;
    private final Map<String, Long> wallets = new LinkedHashMap<>();
    private final Map<String, Long> treasuries = new LinkedHashMap<>();
    private long worldheartTreasury;
    private final Map<String, EconomyOperationReceipt> operationReceipts = new LinkedHashMap<>();

    public long lastAppliedSequence() {
        return lastAppliedSequence;
    }

    public void setLastAppliedSequence(long lastAppliedSequence) {
        this.lastAppliedSequence = Math.max(0L, lastAppliedSequence);
    }

    public Map<String, Long> wallets() {
        return wallets;
    }

    public Map<String, Long> treasuries() {
        return treasuries;
    }

    public long worldheartTreasury() {
        return worldheartTreasury;
    }

    public void setWorldheartTreasury(long worldheartTreasury) {
        this.worldheartTreasury = Math.max(0L, worldheartTreasury);
    }

    public Map<String, EconomyOperationReceipt> operationReceipts() {
        return operationReceipts;
    }

    public EconomyState copy() {
        EconomyState copy = new EconomyState();
        copy.lastAppliedSequence = lastAppliedSequence;
        copy.wallets.putAll(wallets);
        copy.treasuries.putAll(treasuries);
        copy.worldheartTreasury = worldheartTreasury;
        copy.operationReceipts.putAll(operationReceipts);
        return copy;
    }
}
