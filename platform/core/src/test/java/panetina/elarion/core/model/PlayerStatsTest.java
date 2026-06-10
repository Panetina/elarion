package panetina.elarion.core.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PlayerStatsTest {
    @Test
    void incrementsPrimitiveAndCustomCounters() {
        PlayerStats stats = new PlayerStats(UUID.randomUUID());

        assertEquals(3, stats.increment("zombie_kills", 3));
        assertEquals(1, stats.increment("dragon_kills", 1));
        assertEquals(1000, stats.increment("modded_goblin_kills", 1000));

        assertEquals(3, stats.zombieKills());
        assertEquals(1, stats.dragonKills());
        assertEquals(1000, stats.customCounters().get("modded_goblin_kills"));
    }
}
