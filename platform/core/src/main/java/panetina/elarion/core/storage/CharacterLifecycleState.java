package panetina.elarion.core.storage;

import panetina.elarion.core.model.CharacterArchiveRecord;
import panetina.elarion.core.model.CharacterLifecycleRecord;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class CharacterLifecycleState {
    public Map<String, CharacterLifecycleRecord> accounts = new LinkedHashMap<>();
    public Map<String, CharacterArchiveRecord> archives = new LinkedHashMap<>();
    public Set<String> reservedNames = new LinkedHashSet<>();
}
