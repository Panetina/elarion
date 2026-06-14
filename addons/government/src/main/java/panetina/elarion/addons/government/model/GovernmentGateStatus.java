package panetina.elarion.addons.government.model;

public record GovernmentGateStatus(
        String realmId,
        boolean foundationI,
        boolean foundationII,
        boolean foundationIII,
        boolean nameChosen,
        boolean governmentChosen,
        boolean foundingElectionComplete
) {
    public GovernmentGateStatus {
        realmId = realmId == null ? "" : realmId;
    }

    public boolean nameVoteVisible() {
        return true;
    }

    public boolean nameVoteUnlocked() {
        return foundationI && !nameChosen;
    }

    public boolean governmentChoicesVisible() {
        return nameChosen;
    }

    public boolean governmentVoteUnlocked() {
        return foundationII && nameChosen && !governmentChosen;
    }

    public boolean foundingElectionUnlocked() {
        return foundationIII && governmentChosen && !foundingElectionComplete;
    }

    public boolean seatOfRuleUnlocked() {
        return foundationIII && foundingElectionComplete;
    }
}
