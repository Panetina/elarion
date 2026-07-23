package panetina.elarion.addons.angling.fishing;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingSingleStackComponent;
import panetina.elarion.addons.angling.definition.AnglingCatchDefinition;
import panetina.elarion.addons.angling.definition.AnglingCatchOutput;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshot;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshotRepository;
import panetina.elarion.addons.angling.definition.AnglingCatchType;
import panetina.elarion.addons.angling.definition.AnglingDifficultyDefinition;
import panetina.elarion.addons.angling.definition.AnglingItemReference;
import panetina.elarion.addons.angling.definition.AnglingRarity;
import panetina.elarion.addons.angling.definition.AnglingSizeWeightDefinition;
import panetina.elarion.addons.angling.definition.AnglingTypedNode;
import panetina.elarion.addons.angling.registry.AnglingItems;
import panetina.elarion.addons.angling.modifier.AnglingCompiledModifier;
import panetina.elarion.addons.angling.modifier.AnglingEquipmentModifiers;
import panetina.elarion.addons.angling.modifier.AnglingModifierValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.random.RandomGenerator;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingCatchOutcomeGeneratorTest {
    @BeforeAll
    static void bootstrapRegistries() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void frozenPercentileDistributionUsesServerBasisPoints() {
        assertEquals(119, AnglingCatchOutcomeGenerator.sample(100, 20, 0));
        assertEquals(100, AnglingCatchOutcomeGenerator.sample(100, 20, 5_000));
        assertEquals(80, AnglingCatchOutcomeGenerator.sample(100, 20, 9_999));
    }

    @Test
    void nativeBucketPreservesExactFishIdentityAndAuthoritativeComponents() {
        AnglingItems.initialize();
        Identifier fishId = Identifier.of("elarion_angling", "obsidonti");
        AnglingCatchSnapshot.NativeCatch selected = snapshot(fishId, true);
        ItemStack rod = new ItemStack(Items.STICK);
        rod.set(AnglingDataComponents.BAIT, new AnglingSingleStackComponent(new ItemStack(Items.BUCKET)));

        AnglingCatchOutcome outcome = new AnglingCatchOutcomeGenerator().generate(
                selected, rod, new AnglingCatchEvaluationContext.SpeciesProgress(0, false),
                true, true, 80, 6, new FixedRandom(5_000, 0.0F));

        assertTrue(outcome.item().isOf(AnglingItems.require("starcaught_bucket")));
        ItemStack stored = outcome.item().getOrDefault(
                AnglingDataComponents.BUCKETED_FISH, AnglingSingleStackComponent.EMPTY).stack();
        assertTrue(stored.isOf(AnglingItems.require("obsidonti")));
        assertEquals(selected.id(), stored.get(AnglingDataComponents.CAUGHT_FISH_INFO).definitionId());
        assertTrue(outcome.golden());
        assertEquals(5_000, outcome.percentileBasisPoints());

        AnglingCatchCommit commit = new AnglingCatchCommitFactory().create(outcome,
                new AnglingCatchCommitFactory.Facts(
                        UUID.randomUUID(), 1_780_000_000_000L, UUID.randomUUID(),
                        Identifier.ofVanilla("overworld"), Identifier.ofVanilla("overworld"),
                        Identifier.ofVanilla("plains"), null, Identifier.of("elarion_angling", "rod"),
                        Identifier.of("elarion_angling", "bobber"), null, Identifier.ofVanilla("water"),
                        null, null, new AnglingCatchReward.RewardPosition(10, 64, 10)), 1);
        ItemStack reconstructed = AnglingCatchDeliveryService.itemStack(
                AnglingCatchDeliveryService.actions(commit).getFirst().parameters());
        ItemStack reconstructedFish = reconstructed.getOrDefault(
                AnglingDataComponents.BUCKETED_FISH, AnglingSingleStackComponent.EMPTY).stack();
        assertTrue(reconstructed.isOf(AnglingItems.require("starcaught_bucket")));
        assertTrue(reconstructedFish.isOf(AnglingItems.require("obsidonti")));
        assertEquals(outcome.item().get(AnglingDataComponents.CAUGHT_FISH_INFO),
                reconstructed.get(AnglingDataComponents.CAUGHT_FISH_INFO));
        assertEquals(stored.get(AnglingDataComponents.CAUGHT_FISH_INFO),
                reconstructedFish.get(AnglingDataComponents.CAUGHT_FISH_INFO));
    }

    @Test
    void goldenSpeciesCannotRollGoldenTwiceWithoutAnExplicitModifier() {
        AnglingItems.initialize();
        AnglingCatchSnapshot.NativeCatch selected = snapshot(
                Identifier.of("elarion_angling", "obsidonti"), false);
        AnglingCatchOutcome outcome = new AnglingCatchOutcomeGenerator().generate(
                selected, new ItemStack(Items.STICK),
                new AnglingCatchEvaluationContext.SpeciesProgress(10, true),
                false, false, 0, 0, new FixedRandom(0, 0.0F));
        assertFalse(outcome.golden());
    }

    @Test
    void perfectOnlyExtraCatchesAreNonBucketRewardsAndBaseRemovalRemainsDeliverable() {
        AnglingItems.initialize();
        AnglingCatchSnapshot.NativeCatch selected = snapshot(
                Identifier.of("elarion_angling", "obsidonti"), true);
        ItemStack rod = new ItemStack(Items.STICK);
        rod.set(AnglingDataComponents.BAIT, new AnglingSingleStackComponent(new ItemStack(Items.BUCKET)));
        var modifiers = new AnglingEquipmentModifiers.Resolved(List.of(
                new AnglingCompiledModifier(Identifier.of("elarion_angling", "extra_base_catch"),
                        new AnglingModifierValue.CountAndPerfect(2, true, "")),
                new AnglingCompiledModifier(Identifier.of("elarion_angling", "remove_base_fished_item"),
                        new AnglingModifierValue.TranslationOnly(""))));

        AnglingCatchOutcome missedPerfect = new AnglingCatchOutcomeGenerator().generate(
                selected, rod, new AnglingCatchEvaluationContext.SpeciesProgress(0, false),
                false, false, 40, 2, new FixedRandom(5_000, 1.0F), modifiers, List.of(new ItemStack(Items.STRING)));
        assertTrue(missedPerfect.item().isEmpty());
        assertEquals(1, missedPerfect.additionalItems().size());

        AnglingCatchOutcome perfect = new AnglingCatchOutcomeGenerator().generate(
                selected, rod, new AnglingCatchEvaluationContext.SpeciesProgress(0, false),
                true, false, 40, 2, new FixedRandom(5_000, 1.0F), modifiers, List.of(new ItemStack(Items.STRING)));
        assertTrue(perfect.item().isEmpty());
        assertEquals(3, perfect.additionalItems().size());
        assertTrue(perfect.additionalItems().get(1).isOf(AnglingItems.require("obsidonti")));
        assertFalse(perfect.additionalItems().get(1).contains(AnglingDataComponents.BUCKETED_FISH));
    }

    @Test
    void goldenModifiersRemainServerRolledAndCancelWinsLast() {
        AnglingItems.initialize();
        AnglingCatchSnapshot.NativeCatch selected = snapshot(
                Identifier.of("elarion_angling", "obsidonti"), false);
        var extra = new AnglingEquipmentModifiers.Resolved(List.of(new AnglingCompiledModifier(
                Identifier.of("elarion_angling", "extra_golden_chance"),
                new AnglingModifierValue.ExtraGolden(1.0F, true, ""))));
        AnglingCatchOutcome golden = new AnglingCatchOutcomeGenerator().generate(
                selected, new ItemStack(Items.STICK),
                new AnglingCatchEvaluationContext.SpeciesProgress(0, false),
                true, false, 20, 1, new FixedRandom(5_000, 0.5F), extra, List.of());
        assertTrue(golden.golden());

        var canceled = new AnglingEquipmentModifiers.Resolved(List.of(
                extra.modifiers().getFirst(),
                new AnglingCompiledModifier(Identifier.of("elarion_angling", "cancel_golden"),
                        new AnglingModifierValue.TranslationOnly(""))));
        assertFalse(new AnglingCatchOutcomeGenerator().generate(
                selected, new ItemStack(Items.STICK),
                new AnglingCatchEvaluationContext.SpeciesProgress(0, false),
                true, false, 20, 1, new FixedRandom(5_000, 0.5F), canceled, List.of()).golden());
    }

    private static AnglingCatchSnapshot.NativeCatch snapshot(Identifier outputId, boolean bucket) {
        Identifier definitionId = Identifier.of("elarion_angling", "test_fish");
        AnglingCatchOutput output = new AnglingCatchOutput(
                new AnglingItemReference(outputId, 1),
                bucket ? Optional.of(new AnglingItemReference(Identifier.ofVanilla("cod_bucket"), 1))
                        : Optional.empty(),
                Optional.empty(), false, Optional.empty(), AnglingCatchType.FISH);
        AnglingCatchDefinition definition = new AnglingCatchDefinition(
                1, output, 1, new AnglingSizeWeightDefinition(10, 2, 100, 20, 1),
                AnglingRarity.COMMON,
                List.of(new AnglingTypedNode(Identifier.of("elarion_angling", "empty"),
                        "{\"type\":\"elarion_angling:empty\"}")),
                new AnglingDifficultyDefinition(1, 1, 0, 0, List.of(), List.of()),
                false, true, Identifier.of("elarion_angling", "texture"));
        return new AnglingCatchSnapshotRepository().compileAndPublish(Map.of(definitionId, definition))
                .find(definitionId).orElseThrow();
    }

    private record FixedRandom(int value, float chance) implements RandomGenerator {
        @Override
        public long nextLong() {
            return value;
        }

        @Override
        public int nextInt(int bound) {
            return Math.floorMod(value, bound);
        }

        @Override
        public float nextFloat() {
            return chance;
        }
    }
}
