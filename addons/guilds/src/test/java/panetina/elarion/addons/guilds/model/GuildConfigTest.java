package panetina.elarion.addons.guilds.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuildConfigTest {
    @Test
    void defaultsNormalizeUnsafeValues() {
        GuildConfig config = new GuildConfig(
                true,
                -10,
                0,
                0,
                0,
                "",
                "",
                null);

        assertEquals(0, config.creationFee());
        assertEquals(1, config.minTagLength());
        assertEquals(1, config.maxTagLength());
        assertEquals(3, config.maxNameLength());
        assertEquals("[a-z0-9_-]{3,32}", config.idPattern());
        assertEquals("[A-Z0-9]{2,6}", config.tagPattern());
        assertTrue(config.blockedTags().isEmpty());
        assertEquals(java.time.Duration.ofDays(7).toMillis(), config.inviteLifetimeMillis());
    }

    @Test
    void copiesBlockedTagSet() {
        GuildConfig config = new GuildConfig(
                true,
                25,
                2,
                6,
                48,
                "[a-z]+",
                "[A-Z]+",
                Set.of("ADMIN"));

        assertEquals(Set.of("ADMIN"), config.blockedTags());
    }

    @Test
    void inviteLifetimeIsNeverShorterThanOneMinute() {
        GuildConfig config = new GuildConfig(
                true, 25, 2, 6, 48, "[a-z]+", "[A-Z]+", Set.of(), 1L);

        assertEquals(60_000L, config.inviteLifetimeMillis());
    }

    @Test
    void progressionUsesOrderedTierThresholds() {
        GuildProgressionConfig progression = GuildProgressionConfig.defaults();

        assertEquals(1, progression.levelFor(0L));
        assertEquals(1, progression.levelFor(249L));
        assertEquals(2, progression.levelFor(250L));
        assertEquals(15, progression.tierFor(250L).memberCapacity());
    }
}
