package panetina.elarion.addons.angling.fishing;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.definition.AnglingCatchDefinition;
import panetina.elarion.addons.angling.definition.AnglingCatchOutput;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshot;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshotRepository;
import panetina.elarion.addons.angling.definition.AnglingCatchType;
import panetina.elarion.addons.angling.definition.AnglingDifficultyDefinition;
import panetina.elarion.addons.angling.definition.AnglingItemReference;
import panetina.elarion.addons.angling.definition.AnglingRarity;
import panetina.elarion.addons.angling.definition.AnglingSizeWeightDefinition;
import panetina.elarion.addons.angling.restriction.AnglingRestriction;
import panetina.elarion.addons.angling.modifier.AnglingCompiledModifier;
import panetina.elarion.addons.angling.modifier.AnglingEquipmentModifiers;
import panetina.elarion.addons.angling.modifier.AnglingModifierValue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingCatchSelectorTest {
    @Test
    void firstAvailableNonFishOverridesWeightedFishLikeFrozenPipeline() {
        Map<Identifier, AnglingCatchDefinition> definitions = new LinkedHashMap<>();
        definitions.put(id("z_fish"), definition(AnglingCatchType.FISH, 1));
        definitions.put(id("b_trophy"), definition(AnglingCatchType.TROPHY, 1));
        definitions.put(id("a_secret"), definition(AnglingCatchType.SECRET, 1));
        AnglingCatchSnapshot snapshot = new AnglingCatchSnapshotRepository().compileAndPublish(definitions);

        var selected = new AnglingCatchSelector().select(snapshot, context(), new FixedRandom(), true);

        assertEquals(id("a_secret"), selected.orElseThrow().id());
    }

    @Test
    void weightedSelectionDoesNotMaterializeChanceCopiesAndCanReturnEmpty() {
        Map<Identifier, AnglingCatchDefinition> definitions = Map.of(
                id("large"), definition(AnglingCatchType.FISH, 1_000_000),
                id("blocked"), definition(AnglingCatchType.FISH, 0));
        AnglingCatchSnapshot snapshot = new AnglingCatchSnapshotRepository().compileAndPublish(definitions);
        AnglingCatchSelector selector = new AnglingCatchSelector();

        assertEquals(id("large"), selector.select(snapshot, context(), new FixedRandom(), true).orElseThrow().id());
        assertTrue(selector.select(new AnglingCatchSnapshotRepository().compileAndPublish(Map.of(
                id("none"), definition(AnglingCatchType.FISH, 0))), context(), new FixedRandom(), true).isEmpty());
    }

    @Test
    void postSelectionAwardIdentityIsServerResolved() {
        AnglingCatchSnapshot snapshot = new AnglingCatchSnapshotRepository().compileAndPublish(Map.of(
                id("fish"), definition(AnglingCatchType.FISH, 1)));
        var modifiers = new AnglingEquipmentModifiers.Resolved(List.of(new AnglingCompiledModifier(
                id("modify_award_fish"), new AnglingModifierValue.AwardFish(id("guide_identity"), ""))));

        var selected = new AnglingCatchSelector().select(
                snapshot, context(), new FixedRandom(), true, modifiers, 0.0D);

        assertEquals(id("guide_identity"), selected.orElseThrow().id());
        assertEquals(id("output"), selected.orElseThrow().definition().source().catchInfo().item().id());
    }

    @Test
    void inlinePoolDefinitionGetsStableBoundedIdentityWithoutExpandingWeights() {
        AnglingCatchDefinition inline = definition(AnglingCatchType.FISH, 0);
        var modifiers = new AnglingEquipmentModifiers.Resolved(List.of(new AnglingCompiledModifier(
                id("add_to_available_pool"), new AnglingModifierValue.AddToPool(
                Optional.of(inline), Optional.empty(), 5, ""))));

        var selected = new AnglingCatchSelector().select(
                new AnglingCatchSnapshotRepository().compileAndPublish(Map.of()), context(),
                new FixedRandom(), true, modifiers, 0.0D);

        assertEquals(id("inline/output"), selected.orElseThrow().id());
        assertEquals(0, selected.orElseThrow().definition().source().baseChance());
    }

    private static AnglingCatchDefinition definition(AnglingCatchType type, int chance) {
        return new AnglingCatchDefinition(
                1,
                new AnglingCatchOutput(new AnglingItemReference(id("output"), 1), Optional.empty(),
                        Optional.empty(), false, Optional.empty(), type),
                chance,
                new AnglingSizeWeightDefinition(1, 0, 1, 0, 0),
                AnglingRarity.COMMON,
                List.of(new panetina.elarion.addons.angling.definition.AnglingTypedNode(
                        id("empty"), "{\"type\":\"elarion_angling:empty\"}")),
                new AnglingDifficultyDefinition(1, 1, 0, 0, List.of(), List.of()),
                false, true, id("texture"));
    }

    private static AnglingCatchEvaluationContext context() {
        return new AnglingCatchEvaluationContext(
                Identifier.ofVanilla("overworld"), Set.of(), Identifier.ofVanilla("plains"), Set.of(),
                Identifier.ofVanilla("air"), Set.of(Identifier.ofVanilla("water")), 64, 0, 0, 0,
                AnglingRestriction.Season.ALL, Set.of(), Map.of(), Map.of(), () -> 0.0D);
    }

    private static Identifier id(String path) {
        return Identifier.of("elarion_angling", path);
    }

    private static final class FixedRandom implements RandomGenerator {
        @Override
        public long nextLong() {
            return 0L;
        }
    }
}
