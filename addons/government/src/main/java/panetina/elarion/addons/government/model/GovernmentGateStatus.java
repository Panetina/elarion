package panetina.elarion.addons.government.model;

public record GovernmentGateStatus(
        String realmId,
        boolean foundationI,
        boolean foundationII,
        boolean foundationIII,
        boolean nameChosen,
        boolean colorChosen,
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
        return colorChosen;
    }

    public boolean colorVoteVisible() {
        return nameChosen;
    }

    public boolean colorVoteUnlocked() {
        return nameChosen && !colorChosen;
    }

    public boolean governmentVoteUnlocked() {
        return foundationII && colorChosen && !governmentChosen;
    }

    public boolean foundingElectionUnlocked() {
        return foundationIII && governmentChosen && !foundingElectionComplete;
    }

    public boolean seatOfRuleUnlocked() {
        return foundationIII && foundingElectionComplete;
    }

    public String nameVoteLockMessage() {
        if (!foundationI) return "Locked: complete Foundation I at the Shrine before Realm naming opens.";
        if (nameChosen) return "Locked: this Realm already chose its name.";
        return "";
    }

    public String colorVoteLockMessage() {
        if (!nameChosen) return "Locked: finish the Realm name vote before choosing a color.";
        if (colorChosen) return "Locked: this Realm already chose its color.";
        return "";
    }

    public String governmentVoteLockMessage() {
        if (!foundationII) return "Locked: complete Foundation II at the Shrine before Government voting opens.";
        if (!colorChosen) return "Locked: finish Realm name and color votes before choosing Government.";
        if (governmentChosen) return "Locked: this Realm already chose its Government form.";
        return "";
    }

    public String foundingElectionLockMessage() {
        if (!foundationIII) return "Locked: complete Foundation III at the Shrine before founding elections open.";
        if (!governmentChosen) return "Locked: choose a Government form before founding elections open.";
        if (foundingElectionComplete) return "Locked: founding elections are already complete.";
        return "";
    }

    public String seatOfRuleLockMessage() {
        if (!foundationIII) return "Locked: complete Foundation III at the Shrine before the Seat of Rule opens.";
        if (!foundingElectionComplete) return "Locked: finish founding elections before the Seat of Rule opens.";
        return "";
    }

    public String voteLockMessage(GovernmentVoteType type) {
        return switch (type) {
            case REALM_NAME -> nameVoteLockMessage();
            case REALM_COLOR -> colorVoteLockMessage();
            case GOVERNMENT_FORM -> governmentVoteLockMessage();
            case FOUNDING_ELECTION -> foundingElectionLockMessage();
        };
    }

    public String screenLockMessage(GovernmentCivicScreen screen) {
        return switch (screen) {
            case REALM_NAME -> nameVoteLockMessage();
            case REALM_COLOR -> colorVoteLockMessage();
            case GOVERNMENT_FORM -> governmentVoteLockMessage();
            case FOUNDING_ELECTION -> foundingElectionLockMessage();
            case CITIZEN_FEATURES -> "";
        };
    }
}
