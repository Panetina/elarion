package panetina.elarion.addons.realms.service;

import panetina.elarion.core.model.RealmRelationship;

public record RealmAccessPolicy(boolean owner, RealmRelationship relationship) {
    public static RealmAccessPolicy forOwner() {
        return new RealmAccessPolicy(true, RealmRelationship.ALLY);
    }

    public static RealmAccessPolicy visitor(RealmRelationship relationship) {
        return new RealmAccessPolicy(false,
                relationship == null ? RealmRelationship.NEUTRAL : relationship);
    }

    public boolean canBreak() {
        return owner;
    }

    public boolean canUseMechanism() {
        return owner || relationship == RealmRelationship.ALLY
                || relationship == RealmRelationship.HOSTILE;
    }

    public boolean canUseContainer() {
        return owner || relationship == RealmRelationship.HOSTILE;
    }

    public boolean canPlaceLadder() {
        return owner || relationship == RealmRelationship.HOSTILE;
    }

    public boolean canAttackCreature() {
        return owner || relationship == RealmRelationship.HOSTILE;
    }

    public boolean canAttackPlayerFrom(String attackerRealm, String targetRealm) {
        return !attackerRealm.isBlank() && !targetRealm.isBlank()
                && !attackerRealm.equals(targetRealm)
                && relationship == RealmRelationship.HOSTILE;
    }
}
