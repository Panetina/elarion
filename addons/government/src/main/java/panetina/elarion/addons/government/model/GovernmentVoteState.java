package panetina.elarion.addons.government.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GovernmentVoteState {
    public String realmId = "";
    public GovernmentVoteType type = GovernmentVoteType.REALM_NAME;
    public int round = 1;
    public boolean runoff;
    public boolean resolved;
    public long proposalStartedAt;
    public long proposalEndsAt;
    public long startedAt;
    public long endsAt;
    public Map<String, GovernmentVoteOption> options = new LinkedHashMap<>();
    public Map<String, List<String>> ballots = new LinkedHashMap<>();
    public List<String> winnerIds = new ArrayList<>();
    public Map<String, Long> resultTotals = new LinkedHashMap<>();

    public GovernmentVoteState() {
    }

    public GovernmentVoteState(String realmId, GovernmentVoteType type) {
        this.realmId = realmId;
        this.type = type;
    }

    public boolean active(long now) {
        return !resolved && startedAt > 0L && endsAt > now;
    }

    public boolean proposalActive(long now) {
        return !resolved && proposalStartedAt > 0L && proposalEndsAt > now;
    }

    public boolean proposalEnded(long now) {
        return proposalStartedAt > 0L && proposalEndsAt <= now;
    }

    public boolean ended(long now) {
        return !resolved && startedAt > 0L && endsAt <= now;
    }

    public void startIfNeeded(long now, Duration duration) {
        if (startedAt > 0L) return;
        startedAt = now;
        endsAt = now + duration.toMillis();
    }

    public void startProposalIfNeeded(long now, Duration duration) {
        if (proposalStartedAt > 0L) return;
        proposalStartedAt = now;
        proposalEndsAt = now + duration.toMillis();
    }

    public GovernmentVoteState runoff(List<String> tiedIds, long now, Duration duration) {
        GovernmentVoteState next = new GovernmentVoteState(realmId, type);
        next.round = round + 1;
        next.runoff = true;
        next.startedAt = now;
        next.endsAt = now + duration.toMillis();
        for (String id : tiedIds) {
            GovernmentVoteOption option = options.get(id);
            if (option != null) next.options.put(id, option);
        }
        return next;
    }
}
