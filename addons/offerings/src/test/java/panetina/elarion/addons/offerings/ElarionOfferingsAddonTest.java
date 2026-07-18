package panetina.elarion.addons.offerings;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.offerings.model.OfferingInstance;
import panetina.elarion.addons.offerings.model.OfferingPresentation;
import panetina.elarion.addons.offerings.model.OfferingProjectDefinition;
import panetina.elarion.addons.offerings.model.OfferingProjectLevel;
import panetina.elarion.addons.offerings.model.OfferingScope;
import panetina.elarion.addons.offerings.network.ShrineUiOpenPayload;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionOfferingsAddonTest {
    @Test
    void shrineTitleUsesOverrideBeforeFoundationLevelText() {
        OfferingProjectDefinition project = new OfferingProjectDefinition(
                "council_hall", "Council Hall", "", true, OfferingScope.REALM, false, true,
                List.of(), List.of(), OfferingPresentation.defaults(),
                List.of(new OfferingProjectLevel("foundation_i", "Council Hall", "",
                        List.of(), List.of(), new OfferingPresentation("Foundation I", ""))));
        OfferingInstance instance = base().withDisplayNameOverride("Builders' Memorial");

        assertEquals("Builders' Memorial",
                ElarionOfferingsAddon.shrineTitle(instance, project, project.firstLevel()));
    }

    @Test
    void shrineTitleFallsBackToFoundationLevelTextAfterResetClearsOverride() {
        OfferingProjectDefinition project = new OfferingProjectDefinition(
                "council_hall", "Council Hall", "", true, OfferingScope.REALM, false, true,
                List.of(), List.of(), OfferingPresentation.defaults(),
                List.of(new OfferingProjectLevel("foundation_i", "Council Hall", "",
                        List.of(), List.of(), new OfferingPresentation("Foundation I", ""))));
        OfferingInstance instance = base().withDisplayNameOverride("Builders' Memorial").reset("foundation_i");

        assertEquals("Foundation I",
                ElarionOfferingsAddon.shrineTitle(instance, project, project.firstLevel()));
    }

    @Test
    void completedShrineProjectionFillsIncompleteRequirementRows() {
        List<ShrineUiOpenPayload.RequirementRow> rows = List.of(
                row("item:minecraft:stone_bricks", "items", "minecraft:stone_bricks", 0, 256, false),
                row("currency", "currency", "", 20, 100, false));

        ElarionOfferingsAddon.ShrineProgressProjection projection =
                ElarionOfferingsAddon.shrineProgressProjection(rows, true);

        assertEquals(356, projection.current());
        assertEquals(356, projection.required());
        assertTrue(projection.rows().stream().allMatch(ShrineUiOpenPayload.RequirementRow::complete));
        assertEquals(256, projection.rows().get(0).current());
        assertEquals(100, projection.rows().get(1).current());
    }

    @Test
    void incompleteShrineProjectionKeepsActualRequirementProgress() {
        List<ShrineUiOpenPayload.RequirementRow> rows = List.of(
                row("item:minecraft:stone_bricks", "items", "minecraft:stone_bricks", 0, 256, false),
                row("currency", "currency", "", 20, 100, false));

        ElarionOfferingsAddon.ShrineProgressProjection projection =
                ElarionOfferingsAddon.shrineProgressProjection(rows, false);

        assertEquals(20, projection.current());
        assertEquals(356, projection.required());
        assertFalse(projection.rows().get(0).complete());
        assertEquals(0, projection.rows().get(0).current());
    }

    private static OfferingInstance base() {
        return new OfferingInstance("instance_1", "council_hall", OfferingScope.REALM, "realm1",
                "", 0, 0, 0, "", Map.of(), Map.of(), Set.of(), System.currentTimeMillis(), 0L);
    }

    private static ShrineUiOpenPayload.RequirementRow row(
            String key,
            String type,
            String id,
            long current,
            long required,
            boolean complete
    ) {
        return new ShrineUiOpenPayload.RequirementRow(key, type, id, key, "item:minecraft:stone",
                current, required, complete);
    }
}
