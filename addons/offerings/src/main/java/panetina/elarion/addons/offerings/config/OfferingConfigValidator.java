package panetina.elarion.addons.offerings.config;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.offerings.model.OfferingMilestone;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.model.OfferingProjectLevel;
import panetina.elarion.addons.offerings.model.OfferingRequirement;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class OfferingConfigValidator {
    public static final List<String> offering_MILESTONES = List.of(
            "elarion:set_realm_flag",
            "elarion:clear_realm_flag",
            "elarion:run_reward",
            "elarion:emit_history",
            "elarion:economy_reward_realm",
            "elarion:economy_sink_realm"
    );

    private OfferingConfigValidator() {
    }

    public static void validate(
            OfferingProjectDefinition definition,
            Path file,
            Predicate<String> knownMilestone,
            Predicate<String> knownAction
    ) {
        List<String> errors = new ArrayList<>();
        if (definition.id().isBlank()) errors.add("missing id");
        if (Identifier.tryParse(definition.id()) == null && !definition.id().matches("[a-z0-9_./-]+")) {
            errors.add("id must use lowercase identifier-safe characters");
        }
        if (Identifier.tryParse(definition.presentation().icon()) == null) {
            errors.add("presentation icon must be a valid texture identifier");
        }
        for (OfferingProjectLevel level : definition.levels()) {
            if (level.requirements().isEmpty()) errors.add(level.id() + ": at least one requirement is required");
            for (OfferingRequirement requirement : level.requirements()) {
                if (!List.of("items", "currency", "events").contains(requirement.type())) {
                    errors.add(level.id() + ": unknown requirement type " + requirement.type());
                }
                if ((requirement.type().equals("items") || requirement.type().equals("events"))
                        && requirement.id().isBlank()) {
                    errors.add(level.id() + ": " + requirement.type() + " requirement needs id");
                }
            }
            for (OfferingMilestone milestone : level.milestones()) {
                if (!offering_MILESTONES.contains(milestone.type())
                        && !knownMilestone.test(milestone.type())
                        && !knownAction.test(milestone.type())) {
                    errors.add(level.id() + ": unknown milestone type " + milestone.type());
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new OfferingConfigException(file + ": " + String.join("; ", errors));
        }
    }
}
