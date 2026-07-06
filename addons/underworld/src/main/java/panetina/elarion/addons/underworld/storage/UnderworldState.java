package panetina.elarion.addons.underworld.storage;

import panetina.elarion.addons.underworld.model.CorpseRecord;
import panetina.elarion.addons.underworld.model.SoulState;
import panetina.elarion.addons.underworld.model.UnderworldSession;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import panetina.elarion.addons.underworld.model.StoredItemStack;

public final class UnderworldState {
    public Map<String, CorpseRecord> corpses = new LinkedHashMap<>();
    public Map<String, UnderworldSession> sessions = new LinkedHashMap<>();
    public Map<String, SoulState> souls = new LinkedHashMap<>();
    public Map<String, List<StoredItemStack>> recoveryVaults = new LinkedHashMap<>();
}
