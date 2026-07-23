package panetina.elarion.addons.angling.minigame;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshot;
import panetina.elarion.addons.angling.definition.AnglingRarity;
import panetina.elarion.addons.angling.definition.AnglingSweetSpotDefinition;
import panetina.elarion.addons.angling.modifier.AnglingCompiledModifier;
import panetina.elarion.addons.angling.modifier.AnglingEquipmentModifiers;
import panetina.elarion.addons.angling.modifier.AnglingModifierValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Applies the frozen equipment modifier contract once when a server minigame opens. */
public final class AnglingMinigameSpecFactory {
    public AnglingServerMinigameSpec create(
            AnglingCatchSnapshot.NativeCatch selected,
            AnglingEquipmentModifiers.Resolved equipment,
            boolean treasureAvailable
    ) {
        Objects.requireNonNull(selected, "selected");
        Objects.requireNonNull(equipment, "equipment");
        AnglingServerMinigameSpec base = AnglingServerMinigameSpec.from(
                selected.definition(),
                equipment.multiplier("adjust_handle_speed"),
                equipment.multiplier("adjust_penalty_rate"),
                equipment.multiplier("adjust_decay_rate"),
                1.0F,
                equipment.multiplier("adjust_vanishing_rate"),
                equipment.multiplier("adjust_moving_sweetspots"),
                0.0F,
                20.0F,
                treasureAvailable);
        ArrayList<AnglingNativeModifier> modifiers = new ArrayList<>(base.modifiers());
        ArrayList<AnglingServerMinigameSpec.Sweetspot> sweetspots = new ArrayList<>(base.sweetspots());
        boolean steady = equipment.has("bigger_green_sweetspots");
        if (steady) {
            sweetspots.replaceAll(AnglingMinigameSpecFactory::steady);
        }
        for (AnglingCompiledModifier compiled : equipment.modifiers()) {
            AnglingModifierValue value = compiled.value();
            String path = compiled.type().getPath();
            if (value instanceof AnglingNativeModifier nativeModifier) {
                add(modifiers, nativeModifier);
            } else if (value instanceof AnglingModifierValue.BurnOnMiss burn) {
                add(modifiers, new AnglingNativeModifier.BurnOnMiss(
                        burn.length(), burn.rampTime(), burn.extraSpeed(), burn.translationOverride()));
            } else if (value instanceof AnglingModifierValue.FreezeOnMiss freeze) {
                add(modifiers, new AnglingNativeModifier.FreezeOnMiss(
                        freeze.length(), Math.max(0, freeze.rampTime()), freeze.translationOverride()));
            } else if (value instanceof AnglingModifierValue.AddLeaves leaves) {
                add(modifiers, new AnglingNativeModifier.AddLeaves(
                        leaves.chancePerTick(), leaves.translationOverride()));
            } else if (value instanceof AnglingModifierValue.AddBasicSweetspot basic) {
                addSweetspot(sweetspots, sweetspot(basic.sweetspot()));
            } else if (value instanceof AnglingModifierValue.SpawnSweetspots spawning) {
                add(modifiers, new AnglingNativeModifier.SpawnSweetspots(
                        spawning.length(), spawning.cooldown(), spawning.chance(), sweetspot(spawning.sweetspot()),
                        spawning.sudokuVanish(), spawning.translationOverride()));
            } else if (value instanceof AnglingModifierValue.IntegerValue integer
                    && path.equals("spawn_treasure_on_hit_x")) {
                add(modifiers, new AnglingNativeModifier.SpawnTreasureOnHit(
                        integer.value(), integer.translationOverride()));
            } else if (value instanceof AnglingModifierValue.TranslationOnly translation) {
                AnglingNativeModifier nativeModifier = translation(path, translation.translationOverride(),
                        selected.rarity());
                if (nativeModifier != null) add(modifiers, nativeModifier);
            }
        }
        return new AnglingServerMinigameSpec(
                base.hitPoints(), base.pointerSpeed(), base.missPenalty(), base.decay(), base.hitDelayTicks(),
                base.initialProgress(), base.treasureAvailable(), modifiers, sweetspots);
    }

    private static AnglingNativeModifier translation(String path, String text, AnglingRarity rarity) {
        return switch (path) {
            case "bounce_back" -> new AnglingNativeModifier.BounceBack(text);
            case "deep_dark" -> new AnglingNativeModifier.DeepDark(text);
            case "disable_hit_sounds" -> new AnglingNativeModifier.DisableHitSounds(text);
            case "disable_miss_sounds" -> new AnglingNativeModifier.DisableMissSounds(text);
            case "flip_every_hit" -> new AnglingNativeModifier.FlipEveryHit(text);
            case "move_sweetspots_on_miss" -> new AnglingNativeModifier.MoveSweetspotsOnMiss(text);
            case "never_lose" -> new AnglingNativeModifier.NeverLose(text);
            case "prevent_frozen" -> new AnglingNativeModifier.PreventFrozen(text);
            case "pull_down" -> new AnglingNativeModifier.PullDown(text);
            case "stop_decay_on_hit" -> new AnglingNativeModifier.StopDecayOnHit(graceTicks(rarity), text);
            case "teleport" -> new AnglingNativeModifier.Teleport(text);
            default -> null;
        };
    }

    private static int graceTicks(AnglingRarity rarity) {
        return switch (rarity) {
            case NONE, TRASH -> 99;
            case COMMON, UNCOMMON -> 40;
            case RARE -> 30;
            case EPIC, LEGENDARY -> 10;
            case GOLDEN -> 0;
        };
    }

    private static AnglingServerMinigameSpec.Sweetspot sweetspot(AnglingSweetSpotDefinition definition) {
        if (!definition.modifiers().isEmpty()) {
            throw new IllegalStateException("Runtime equipment sweetspot on-hit modifiers are not yet compiled");
        }
        return new AnglingServerMinigameSpec.Sweetspot(
                behavior(definition.sweetspotType()), definition.texturePath(), definition.hitboxSizePixels(),
                definition.reward(), definition.flip(), definition.vanishingRate(), definition.movingRate(),
                definition.color(), List.of());
    }

    private static AnglingSweetspotBehaviorType behavior(Identifier id) {
        return switch (id.getPath()) {
            case "normal" -> AnglingSweetspotBehaviorType.NORMAL;
            case "freeze", "frozen" -> AnglingSweetspotBehaviorType.FREEZE;
            case "treasure" -> AnglingSweetspotBehaviorType.TREASURE;
            case "tnt" -> AnglingSweetspotBehaviorType.TNT;
            case "aqua" -> AnglingSweetspotBehaviorType.AQUA;
            case "leaf" -> AnglingSweetspotBehaviorType.LEAF;
            case "deep_ocean" -> AnglingSweetspotBehaviorType.DEEP_OCEAN;
            case "cloud" -> AnglingSweetspotBehaviorType.CLOUD;
            case "glowing", "glowing_sweetspot" -> AnglingSweetspotBehaviorType.GLOWING;
            default -> throw new IllegalArgumentException("Unknown equipment sweetspot behavior " + id);
        };
    }

    private static AnglingServerMinigameSpec.Sweetspot steady(AnglingServerMinigameSpec.Sweetspot spot) {
        String path = spot.texturePath().getPath();
        if (path.endsWith("/normal.png")) {
            return replaceVisual(spot, "textures/gui/minigame/spots/normal_steady.png", 33);
        }
        if (path.endsWith("/thin.png")) {
            return replaceVisual(spot, "textures/gui/minigame/spots/thin_steady.png", 20);
        }
        return spot;
    }

    private static AnglingServerMinigameSpec.Sweetspot replaceVisual(
            AnglingServerMinigameSpec.Sweetspot spot,
            String path,
            int size
    ) {
        return new AnglingServerMinigameSpec.Sweetspot(
                spot.behavior(), Identifier.of("elarion_angling", path), size, spot.reward(), spot.flip(),
                spot.vanishingRate(), spot.movingRate(), spot.color(), spot.onHitModifiers());
    }

    private static void add(List<AnglingNativeModifier> values, AnglingNativeModifier value) {
        if (values.size() >= AnglingServerMinigameSpec.MAX_MODIFIERS) {
            throw new IllegalStateException("Resolved minigame modifiers exceed protocol bound");
        }
        values.add(value);
    }

    private static void addSweetspot(
            List<AnglingServerMinigameSpec.Sweetspot> values,
            AnglingServerMinigameSpec.Sweetspot value
    ) {
        if (values.size() >= AnglingServerMinigameSpec.MAX_SWEETSPOTS) {
            throw new IllegalStateException("Resolved minigame sweetspots exceed protocol bound");
        }
        values.add(value);
    }
}
