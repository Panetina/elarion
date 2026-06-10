package panetina.elarion.addons.realms.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.core.model.RealmRelationship;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RealmAccessPolicyTest {
    @Test
    void ownerHasNormalRealmAccessButNotSameRealmPvp() {
        RealmAccessPolicy policy = RealmAccessPolicy.forOwner();
        assertTrue(policy.canBreak());
        assertTrue(policy.canUseContainer());
        assertTrue(policy.canAttackCreature());
        assertFalse(policy.canAttackPlayerFrom("earth", "earth"));
    }

    @Test
    void allyCanUseMechanismsOnly() {
        RealmAccessPolicy policy = RealmAccessPolicy.visitor(RealmRelationship.ALLY);
        assertTrue(policy.canUseMechanism());
        assertFalse(policy.canUseContainer());
        assertFalse(policy.canBreak());
        assertFalse(policy.canPlaceLadder());
        assertFalse(policy.canAttackCreature());
    }

    @Test
    void hostileCanInvadeWithoutGriefing() {
        RealmAccessPolicy policy = RealmAccessPolicy.visitor(RealmRelationship.HOSTILE);
        assertTrue(policy.canUseMechanism());
        assertTrue(policy.canUseContainer());
        assertTrue(policy.canPlaceLadder());
        assertTrue(policy.canAttackCreature());
        assertTrue(policy.canAttackPlayerFrom("earth", "oak"));
        assertFalse(policy.canBreak());
    }

    @Test
    void neutralAndEmbargoedVisitorsRemainRestricted() {
        for (RealmRelationship relationship :
                new RealmRelationship[]{RealmRelationship.NEUTRAL, RealmRelationship.EMBARGOED}) {
            RealmAccessPolicy policy = RealmAccessPolicy.visitor(relationship);
            assertFalse(policy.canUseMechanism());
            assertFalse(policy.canUseContainer());
            assertFalse(policy.canPlaceLadder());
            assertFalse(policy.canAttackCreature());
            assertFalse(policy.canBreak());
        }
    }
}
