package panetina.elarion.addons.npcs.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class NpcReputationTierTest {
    @Test
    void tierBoundariesRemainStable() {
        assertEquals("Hated", NpcReputationTier.forScore(-121).label());
        assertEquals("Disliked", NpcReputationTier.forScore(-120).label());
        assertEquals("Disliked", NpcReputationTier.forScore(-1).label());
        assertEquals("Neutral", NpcReputationTier.forScore(0).label());
        assertEquals("Liked", NpcReputationTier.forScore(120).label());
        assertEquals("Loved", NpcReputationTier.forScore(240).label());
        assertEquals(2, NpcReputationTier.forScore(2).progress());
        assertEquals(120, NpcReputationTier.forScore(5_000).progress());
        assertEquals(120, NpcReputationTier.forScore(2).progressMaximum());
        assertEquals(120L, NpcReputationTier.minimumScore("liked"));
        assertEquals(Long.MAX_VALUE, NpcReputationTier.minimumScore("missing"));
        assertEquals("Hostile", NpcReputationTier.personalLabel(-60));
        assertEquals("Neutral", NpcReputationTier.personalLabel(0));
        assertEquals("Familiar", NpcReputationTier.personalLabel(20));
        assertEquals("Trusted", NpcReputationTier.personalLabel(50));
        assertEquals("Close", NpcReputationTier.personalLabel(80));
    }
}
