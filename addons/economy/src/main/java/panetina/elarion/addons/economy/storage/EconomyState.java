package panetina.elarion.addons.economy.storage;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EconomyState {
    private long lastAppliedSequence;
    private final Map<String, Long> wallets = new LinkedHashMap<>();
    private final Map<String, Long> treasuries = new LinkedHashMap<>();

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

    public EconomyState copy() {
        EconomyState copy = new EconomyState();
        copy.lastAppliedSequence = lastAppliedSequence;
        copy.wallets.putAll(wallets);
        copy.treasuries.putAll(treasuries);
        return copy;
    }
}
