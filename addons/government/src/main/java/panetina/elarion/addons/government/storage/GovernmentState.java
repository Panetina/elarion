package panetina.elarion.addons.government.storage;

import panetina.elarion.addons.government.model.RealmGovernmentState;
import panetina.elarion.addons.government.model.GovernmentLawRecord;
import panetina.elarion.addons.government.model.GovernmentOfficeTermRecord;
import panetina.elarion.addons.government.model.GovernmentProposalRecord;
import panetina.elarion.addons.government.model.GovernmentVoteState;

import java.util.LinkedHashMap;
import java.util.Map;

public final class GovernmentState {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public int schemaVersion = CURRENT_SCHEMA_VERSION;
    public Map<String, RealmGovernmentState> realms = new LinkedHashMap<>();
    public Map<String, GovernmentVoteState> votes = new LinkedHashMap<>();
    public Map<String, GovernmentProposalRecord> proposals = new LinkedHashMap<>();
    public Map<String, GovernmentLawRecord> laws = new LinkedHashMap<>();
    public Map<String, GovernmentOfficeTermRecord> officeTerms = new LinkedHashMap<>();
    public Map<String, String> authorityTitleRestores = new LinkedHashMap<>();

    public GovernmentState copy() {
        GovernmentState copy = new GovernmentState();
        copy.schemaVersion = schemaVersion;
        copy.realms = realms == null ? new LinkedHashMap<>() : new LinkedHashMap<>(realms);
        copy.votes = votes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(votes);
        copy.proposals = proposals == null ? new LinkedHashMap<>() : new LinkedHashMap<>(proposals);
        copy.laws = laws == null ? new LinkedHashMap<>() : new LinkedHashMap<>(laws);
        copy.officeTerms = officeTerms == null ? new LinkedHashMap<>() : new LinkedHashMap<>(officeTerms);
        copy.authorityTitleRestores = authorityTitleRestores == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(authorityTitleRestores);
        return copy;
    }
}
