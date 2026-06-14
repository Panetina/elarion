package panetina.elarion.addons.government.storage;

import panetina.elarion.addons.government.model.RealmGovernmentState;
import panetina.elarion.addons.government.model.GovernmentVoteState;

import java.util.LinkedHashMap;
import java.util.Map;

public final class GovernmentState {
    public Map<String, RealmGovernmentState> realms = new LinkedHashMap<>();
    public Map<String, GovernmentVoteState> votes = new LinkedHashMap<>();

    public GovernmentState copy() {
        GovernmentState copy = new GovernmentState();
        copy.realms = new LinkedHashMap<>(realms);
        copy.votes = new LinkedHashMap<>(votes);
        return copy;
    }
}
