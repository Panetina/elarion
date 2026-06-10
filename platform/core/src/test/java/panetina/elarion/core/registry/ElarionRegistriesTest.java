package panetina.elarion.core.registry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionRegistriesTest {
    @Test
    void builtInRegistryEntriesAreAvailable() {
        ElarionRegistries registries = new ElarionRegistries();

        assertTrue(registries.conditions().contains("has_realm"));
        assertTrue(registries.actions().contains("run_reward"));
        assertTrue(registries.requirements().contains("items"));
        assertTrue(registries.milestoneEvents().contains("elarion:grant_title"));
    }

    @Test
    void unknownRegistryEntryFailsClearly() {
        ElarionRegistries registries = new ElarionRegistries();

        assertThrows(IllegalArgumentException.class,
                () -> registries.conditions().requireKnown("missing_condition", "test.yml.condition"));
    }

    @Test
    void duplicateRegistryEntryFailsClearly() {
        ElarionRegistry<ConditionType> registry = new ElarionRegistry<>("condition");
        registry.register(new ConditionType("has_realm", "test", "first"));

        assertThrows(IllegalArgumentException.class,
                () -> registry.register(new ConditionType("has_realm", "test", "duplicate")));
    }
}
