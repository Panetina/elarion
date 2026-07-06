package panetina.elarion.addons.offerings.config;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.offerings.model.OfferingMilestone;
import panetina.elarion.addons.offerings.model.OfferingPresentation;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.model.OfferingProjectLevel;
import panetina.elarion.addons.offerings.model.OfferingRequirement;
import panetina.elarion.addons.offerings.model.OfferingScope;
import panetina.elarion.addons.offerings.model.OfferingUiConfig;
import panetina.elarion.core.config.ElarionConfigDomain;
import panetina.elarion.core.config.ElarionConfigRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OfferingConfigDescriptorsTest {
    @Test
    void registersOfferingsDomain() {
        ElarionConfigRegistry registry = new ElarionConfigRegistry();

        OfferingConfigDescriptors.register(registry, this::projects, this::ui);

        assertTrue(registry.domain("offerings").isPresent());
        assertEquals("Offerings", registry.domain("offerings").orElseThrow().label());
    }

    @Test
    void domainExposesUiAndProjectSnapshotValues() {
        ElarionConfigDomain domain = OfferingConfigDescriptors.domain(this::projects, this::ui);

        assertEquals("offerings", domain.id());
        assertEquals("addons:offerings", domain.ownerModule());
        assertEquals("/e offerings reload", domain.reloadCommand());
        assertEquals(3, domain.categories().size());
        assertTrue(domain.files().contains("config/elarion/addons/offerings/ui.yml"));
        assertTrue(domain.files().contains("config/elarion/addons/offerings/projects/*.yml"));

        assertEquals("reserved", domain.entry("general", "society.status").orElseThrow().currentDisplayValue());
        assertEquals("2", domain.entry("general", "projects.count").orElseThrow().currentDisplayValue());
        assertEquals("council_hall, global_monument",
                domain.entry("general", "projects.ids").orElseThrow().currentDisplayValue());

        assertEquals("shrine", domain.entry("ui", "ui.theme-variant").orElseThrow().currentDisplayValue());
        assertEquals("520", domain.entry("ui", "ui.logical-width").orElseThrow().currentDisplayValue());
        assertEquals("360", domain.entry("ui", "ui.logical-width").orElseThrow().minimum());
        assertEquals("960", domain.entry("ui", "ui.logical-width").orElseThrow().maximum());

        var scope = domain.entry("projects", "projects.council_hall.scope").orElseThrow();
        assertEquals("realm", scope.currentDisplayValue());
        assertTrue(scope.choices().contains("global"));

        assertEquals("3", domain.entry("projects",
                "projects.council_hall.requirements.count").orElseThrow().currentDisplayValue());
        assertEquals("1", domain.entry("projects",
                "projects.council_hall.milestones.count").orElseThrow().currentDisplayValue());
        assertEquals("foundation_i", domain.entry("projects",
                "projects.council_hall.first-level").orElseThrow().currentDisplayValue());
        assertEquals("Foundation I", domain.entry("projects",
                "projects.council_hall.presentation.level-text").orElseThrow().currentDisplayValue());
    }

    @Test
    void projectEntriesReadCurrentSupplierValues() {
        AtomicReference<List<OfferingProjectDefinition>> current = new AtomicReference<>(projects());
        ElarionConfigDomain domain = OfferingConfigDescriptors.domain(current::get, this::ui);
        var displayName = domain.entry("projects", "projects.council_hall.display-name").orElseThrow();

        current.set(List.of(councilHall("Changed Hall"), globalMonument()));

        assertEquals("Changed Hall", displayName.currentDisplayValue());
        assertEquals("Council Hall", displayName.defaultDisplayValue());
    }

    private List<OfferingProjectDefinition> projects() {
        return List.of(councilHall("Council Hall"), globalMonument());
    }

    private OfferingProjectDefinition councilHall(String displayName) {
        List<OfferingRequirement> requirements = List.of(
                new OfferingRequirement("items", "minecraft:stone_bricks", 64L),
                new OfferingRequirement("currency", "", 25L),
                new OfferingRequirement("events", "builder_help", 3L));
        List<OfferingMilestone> milestones = List.of(new OfferingMilestone(
                "announce_completion",
                "elarion:emit_history",
                Map.of("category", "offering")));
        OfferingPresentation presentation = new OfferingPresentation(
                "Foundation I",
                "minecraft:textures/item/amethyst_shard.png");
        return new OfferingProjectDefinition(
                "council_hall",
                displayName,
                "A civic project.",
                true,
                OfferingScope.REALM,
                false,
                true,
                requirements,
                milestones,
                presentation,
                List.of(new OfferingProjectLevel(
                        "foundation_i",
                        displayName,
                        "A civic project.",
                        requirements,
                        milestones,
                        presentation)));
    }

    private OfferingProjectDefinition globalMonument() {
        return new OfferingProjectDefinition(
                "global_monument",
                "Global Monument",
                "A global project.",
                true,
                OfferingScope.GLOBAL,
                true,
                false,
                List.of(new OfferingRequirement("currency", "", 100L)),
                List.of(),
                OfferingPresentation.defaults());
    }

    private OfferingUiConfig ui() {
        return OfferingUiConfig.defaults();
    }
}
