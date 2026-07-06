package panetina.elarion.core.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CharacterArchiveRecord {
    public String characterId = "";
    public String accountId = "";
    public int generation;
    public String displayName = "";
    public String biography = "";
    public String realmId = "";
    public String activeTitleId = "";
    public List<String> unlockedTitleIds = new ArrayList<>();
    public Map<String, String> metadata = new LinkedHashMap<>();
    public long createdAt;
    public long diedAt;
    public String reason = "";
}
