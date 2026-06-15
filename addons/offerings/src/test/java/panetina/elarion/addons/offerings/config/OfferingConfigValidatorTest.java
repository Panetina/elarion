package panetina.elarion.addons.offerings.config;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.offerings.model.OfferingMilestone;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.model.OfferingRequirement;
import panetina.elarion.addons.offerings.model.OfferingScope;
import panetina.elarion.addons.offerings.model.OfferingPresentation;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class OfferingConfigValidatorTest {
    @Test
    void acceptsBuiltInOfferingMilestones() {
        OfferingProjectDefinition definition = definition(
                List.of(new OfferingRequirement("items", "minecraft:stone", 1)),
                List.of(
                        new OfferingMilestone("history", "elarion:emit_history", Map.of()),
                        new OfferingMilestone("realm-notice", "elarion:notify_realm", Map.of()),
                        new OfferingMilestone("world-notice", "elarion:notify_world", Map.of()))
        );

        assertDoesNotThrow(() -> OfferingConfigValidator.validate(definition, Path.of("project.yml"),
                id -> false, id -> false));
    }

    @Test
    void rejectsUnknownRequirementTypes() {
        OfferingProjectDefinition definition = definition(
                List.of(new OfferingRequirement("mystery", "x", 1)),
                List.of()
        );

        assertThrows(OfferingConfigException.class,
                () -> OfferingConfigValidator.validate(definition, Path.of("project.yml"),
                        id -> false, id -> false));
    }

    @Test
    void rejectsUnknownMilestones() {
        OfferingProjectDefinition definition = definition(
                List.of(new OfferingRequirement("events", "x", 1)),
                List.of(new OfferingMilestone("bad", "elarion:missing", Map.of()))
        );

        assertThrows(OfferingConfigException.class,
                () -> OfferingConfigValidator.validate(definition, Path.of("project.yml"),
                        id -> false, id -> false));
    }

    private static OfferingProjectDefinition definition(
            List<OfferingRequirement> requirements,
            List<OfferingMilestone> milestones
    ) {
        return new OfferingProjectDefinition("project", "Project", "", true, OfferingScope.REALM,
                false, true, requirements, milestones, OfferingPresentation.defaults());
    }
}
