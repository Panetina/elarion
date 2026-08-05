package panetina.elarion.addons.offerings.service;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.offerings.model.OfferingInstance;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.model.OfferingProjectLevel;
import panetina.elarion.addons.offerings.model.OfferingRequirement;
import panetina.elarion.addons.offerings.model.OfferingScope;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class OfferingWebProjectionPublisherTest {
    @Test
    void completedForcedProjectProjectsConfiguredRequirementsAsFulfilled() {
        OfferingProjectLevel level = new OfferingProjectLevel("foundation", "Hall", "", List.of(
                new OfferingRequirement("items", "minecraft:stone", 64)), List.of(), null);
        OfferingProjectDefinition project = new OfferingProjectDefinition("hall", "Hall", "", true,
                OfferingScope.REALM, false, false, level.requirements(), List.of(), null, List.of(level));
        OfferingInstance instance = new OfferingInstance("hall_1", "hall", "foundation", "",
                OfferingScope.REALM, "oak", "minecraft:overworld", 0, 64, 0, "", Map.of(), Map.of(), Set.of(), 1, 2, 0);

        Map<String, String> payload = OfferingWebProjectionPublisher.progressPayload(instance, project, level, "Hall");

        assertEquals("64", payload.get("value"));
        assertEquals("64", payload.get("required"));
        assertEquals("100", payload.get("percent"));
        assertEquals("complete", payload.get("status"));
    }
}
