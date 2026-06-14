package panetina.elarion.core;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AddonInitializationOrderTest {
    @Test
    void placesDependenciesBeforeConsumers() {
        assertEquals(
                java.util.List.of("economy", "portals", "offerings"),
                AddonInitializationOrder.sort(Map.of(
                        "offerings", Set.of("economy", "portals"),
                        "portals", Set.of("economy"),
                        "economy", Set.of())));
    }

    @Test
    void rejectsCyclesClearly() {
        assertThrows(IllegalStateException.class, () -> AddonInitializationOrder.sort(Map.of(
                "first", Set.of("second"),
                "second", Set.of("first"))));
    }
}
