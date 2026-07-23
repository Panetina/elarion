package panetina.elarion.addons.angling.fishing;

import panetina.elarion.addons.angling.definition.AnglingCatchSnapshot;
import panetina.elarion.addons.angling.definition.AnglingCatchType;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.compile.AnglingNativeDefinitionCompilers;
import panetina.elarion.addons.angling.modifier.AnglingCompiledModifier;
import panetina.elarion.addons.angling.modifier.AnglingEquipmentModifiers;
import panetina.elarion.addons.angling.modifier.AnglingModifierValue;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.random.RandomGenerator;

/** One bounded pass over the immutable reload snapshot; never expands weights into repeated objects. */
public final class AnglingCatchSelector {
    private static final panetina.elarion.addons.angling.compile.AnglingDefinitionCompilerSet<
            panetina.elarion.addons.angling.restriction.AnglingRestriction,
            panetina.elarion.addons.angling.minigame.AnglingNativeModifier,
            panetina.elarion.addons.angling.minigame.AnglingSweetspotBehaviorType> INLINE_COMPILERS =
            AnglingNativeDefinitionCompilers.create();
    public Optional<AnglingCatchSnapshot.NativeCatch> select(
            AnglingCatchSnapshot snapshot,
            AnglingCatchEvaluationContext context,
            RandomGenerator random,
            boolean includeNonFish
    ) {
        return select(snapshot, context, random, includeNonFish,
                new AnglingEquipmentModifiers.Resolved(List.of()), 0.0D);
    }

    public Optional<AnglingCatchSnapshot.NativeCatch> select(
            AnglingCatchSnapshot snapshot,
            AnglingCatchEvaluationContext context,
            RandomGenerator random,
            boolean includeNonFish,
            AnglingEquipmentModifiers.Resolved modifiers,
            double luck
    ) {
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(modifiers, "modifiers");
        if (!Double.isFinite(luck)) throw new IllegalArgumentException("luck must be finite");
        AnglingCatchSnapshot.NativeCatch special = null;
        Map<AnglingCatchSnapshot.NativeCatch, Integer> fish = new LinkedHashMap<>();

        List<AnglingCatchSnapshot.NativeCatch> definitions = snapshot.all().values().stream()
                .sorted(Comparator.comparing(value -> value.id().toString()))
                .toList();
        if (includeNonFish) {
            for (AnglingCatchSnapshot.NativeCatch candidate : definitions) {
                if (candidate.type() != AnglingCatchType.FISH
                        && chance(candidate, context, modifiers, random) > 0) {
                    special = candidate;
                    break;
                }
            }
        }
        for (AnglingCatchSnapshot.NativeCatch candidate : definitions) {
            if (candidate.type() != AnglingCatchType.FISH) continue;
            int chance = chance(candidate, context, modifiers, random);
            if (chance <= 0) continue;
            fish.put(candidate, adjustedWeight(candidate, chance, context, modifiers, luck));
        }
        addExplicitPoolEntries(snapshot, fish, modifiers);
        if (special != null) return Optional.of(afterSelection(special, modifiers));
        int totalWeight = 0;
        for (int weight : fish.values()) totalWeight = Math.addExact(totalWeight, weight);
        if (totalWeight == 0) return Optional.empty();
        int selected = random.nextInt(totalWeight);
        int cumulative = 0;
        for (var candidate : fish.entrySet()) {
            cumulative = Math.addExact(cumulative, candidate.getValue());
            if (selected < cumulative) return Optional.of(afterSelection(candidate.getKey(), modifiers));
        }
        throw new IllegalStateException("Angling weighted selection did not resolve a catch");
    }

    int chance(AnglingCatchSnapshot.NativeCatch candidate, AnglingCatchEvaluationContext context) {
        return chance(candidate, context, new AnglingEquipmentModifiers.Resolved(List.of()),
                java.util.random.RandomGenerator.getDefault());
    }

    private int chance(
            AnglingCatchSnapshot.NativeCatch candidate,
            AnglingCatchEvaluationContext context,
            AnglingEquipmentModifiers.Resolved modifiers,
            RandomGenerator random
    ) {
        boolean ignoreWeather = rolls(modifiers, "ignore_weather_restrictions", random);
        boolean ignoreDaytime = rolls(modifiers, "ignore_daytime_restrictions", random);
        int chance = candidate.definition().source().baseChance();
        for (var restriction : candidate.definition().restrictions()) {
            chance = Math.addExact(chance,
                    AnglingRestrictionEvaluator.adjustment(
                            restriction, candidate.id(), context, ignoreWeather, ignoreDaytime));
        }
        return chance;
    }

    private static int adjustedWeight(
            AnglingCatchSnapshot.NativeCatch candidate,
            int baseWeight,
            AnglingCatchEvaluationContext context,
            AnglingEquipmentModifiers.Resolved modifiers,
            double luck
    ) {
        int result = baseWeight;
        for (AnglingCompiledModifier modifier : modifiers.modifiers()) {
            if (modifier.value() instanceof AnglingModifierValue.LuckByRarity value) {
                int copiesPerEntry = Math.max(0, (int) (value.increases()
                        .getOrDefault(candidate.rarity(), 0) * luck));
                result = Math.addExact(result, Math.multiplyExact(baseWeight, copiesPerEntry));
            } else if (modifier.type().getPath().equals("new_catch_increase")
                    && modifier.value() instanceof AnglingModifierValue.IntegerValue value
                    && context.speciesProgress().getOrDefault(candidate.id(),
                    new AnglingCatchEvaluationContext.SpeciesProgress(0, false)).totalCount() == 0) {
                result = Math.addExact(result, value.value());
            }
        }
        return result;
    }

    private static void addExplicitPoolEntries(
            AnglingCatchSnapshot snapshot,
            Map<AnglingCatchSnapshot.NativeCatch, Integer> fish,
            AnglingEquipmentModifiers.Resolved modifiers
    ) {
        for (AnglingCompiledModifier modifier : modifiers.modifiers()) {
            if (!(modifier.value() instanceof AnglingModifierValue.AddToPool value) || value.quantity() == 0) {
                continue;
            }
            Optional<AnglingCatchSnapshot.NativeCatch> addition = value.definitionId().flatMap(snapshot::find);
            if (addition.isEmpty() && value.definition().isPresent()) {
                var definition = value.definition().orElseThrow();
                Identifier id = inlineDefinitionId(definition);
                addition = Optional.of(new AnglingCatchSnapshot.NativeCatch(
                        id, INLINE_COMPILERS.compile(id, definition)));
            }
            addition.ifPresent(candidate -> fish.merge(candidate, value.quantity(), Math::addExact));
        }
    }

    private static AnglingCatchSnapshot.NativeCatch afterSelection(
            AnglingCatchSnapshot.NativeCatch selected,
            AnglingEquipmentModifiers.Resolved modifiers
    ) {
        Identifier awardId = selected.id();
        var definition = selected.definition();
        for (AnglingCompiledModifier modifier : modifiers.modifiers()) {
            if (modifier.value() instanceof AnglingModifierValue.OverrideCatch override) {
                definition = INLINE_COMPILERS.compile(awardId, override.definition());
            } else if (modifier.value() instanceof AnglingModifierValue.AwardFish award) {
                awardId = award.definitionId();
            }
        }
        return new AnglingCatchSnapshot.NativeCatch(awardId, definition);
    }

    private static Identifier inlineDefinitionId(panetina.elarion.addons.angling.definition.AnglingCatchDefinition value) {
        Identifier source = value.catchInfo().entity().orElse(value.catchInfo().item().id());
        String path = source.getPath().replace('/', '_');
        return Identifier.of("elarion_angling", "inline/" + path);
    }

    private static boolean rolls(
            AnglingEquipmentModifiers.Resolved modifiers,
            String path,
            RandomGenerator random
    ) {
        for (AnglingCompiledModifier modifier : modifiers.modifiers()) {
            if (modifier.type().getPath().equals(path)
                    && modifier.value() instanceof AnglingModifierValue.Weighted value
                    && random.nextFloat() < value.weight()) return true;
        }
        return false;
    }
}
