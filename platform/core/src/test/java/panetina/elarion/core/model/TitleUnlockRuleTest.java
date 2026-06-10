package panetina.elarion.core.model;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TitleUnlockRuleTest {
    @Test
    void matchesExactIdsAndTagsForModdedContent() {
        TitleUnlockRule rule = new TitleUnlockRule(
                "goblin_slayer",
                "goblin_slayer",
                "entity-kill",
                "modded_goblin_kills",
                1000,
                1,
                Set.of(
                        TitleUnlockRule.RegistryMatcher.parse("modid:goblin"),
                        TitleUnlockRule.RegistryMatcher.parse("#elarion:goblins")
                ),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Map.of(),
                null
        );

        ProgressionEvent exact = ProgressionEvent.builder("entity-kill", UUID.randomUUID())
                .entity(Identifier.of("modid:goblin"), Set.of())
                .build();
        ProgressionEvent tagged = ProgressionEvent.builder("entity-kill", UUID.randomUUID())
                .entity(Identifier.of("modid:cave_goblin"), Set.of(Identifier.of("elarion:goblins")))
                .build();
        ProgressionEvent unrelated = ProgressionEvent.builder("entity-kill", UUID.randomUUID())
                .entity(Identifier.of("minecraft:zombie"), Set.of())
                .build();

        assertTrue(rule.matches(exact));
        assertTrue(rule.matches(tagged));
        assertFalse(rule.matches(unrelated));
    }

    @Test
    void matchesConfiguredRegionsFromEventMetadata() {
        TitleUnlockRule rule = new TitleUnlockRule(
                "maze_runner",
                "maze_runner",
                "region-enter",
                "",
                1,
                1,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of("maze_end"),
                Map.of(),
                null
        );

        ProgressionEvent matching = ProgressionEvent.builder("region-enter", UUID.randomUUID())
                .metadata("regions", "spawn,maze_end")
                .build();
        ProgressionEvent unrelated = ProgressionEvent.builder("region-enter", UUID.randomUUID())
                .metadata("regions", "spawn")
                .build();

        assertTrue(rule.matches(matching));
        assertFalse(rule.matches(unrelated));
    }

    @Test
    void continuousMinecraftDaysConvertToTicks() {
        TitleUnlockRule.Continuous continuous = new TitleUnlockRule.Continuous(
                3,
                "minecraft_days",
                100,
                true,
                Set.of(),
                Set.of(),
                Set.of("underwater")
        );

        assertTrue(continuous.requiredMetadata().contains("underwater"));
        assertTrue(continuous.requiredTicks() == 72_000L);
    }
}
