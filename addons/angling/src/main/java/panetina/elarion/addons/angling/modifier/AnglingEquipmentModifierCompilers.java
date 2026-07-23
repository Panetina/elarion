package panetina.elarion.addons.angling.modifier;

import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;
import panetina.elarion.addons.angling.compile.AnglingTypedCompilerRegistry;
import panetina.elarion.addons.angling.minigame.AnglingNativeModifier;

/** Exact typed dispatch table for every modifier present in the transformed item/effect maps. */
public final class AnglingEquipmentModifierCompilers {
    public static final int REGISTERED_COUNT = 52;

    private AnglingEquipmentModifierCompilers() {
    }

    public static AnglingTypedCompilerRegistry<AnglingModifierValue> create() {
        var builder = AnglingTypedCompilerRegistry.<AnglingModifierValue>builder();
        register(builder, AnglingModifierValue.TranslationOnly.CODEC,
                "anglers_hat", "survives_lava", "no_gravity", "fish_messages", "base", "deep_dark",
                "cancel_golden", "extra_exp_based_on_performance", "flip_every_hit", "prevent_frozen",
                "pull_down", "bounce_back", "bigger_green_sweetspots", "stop_decay_on_hit",
                "remove_base_fished_item", "trigger_skip_minigame", "skip_minigame_if_trigger_found",
                "empty", "move_sweetspots_on_miss", "never_lose", "skip_minigame");
        register(builder, AnglingModifierValue.Multiplier.CODEC,
                "adjust_vanishing_rate", "adjust_handle_speed", "adjust_penalty_rate", "adjust_decay_rate",
                "adjust_moving_sweetspots", "bost_thrown_speed");
        register(builder, AnglingModifierValue.Weighted.CODEC,
                "force_fish_entity", "hide_catch", "ignore_weather_restrictions",
                "ignore_daytime_restrictions", "award_treasure_on_perfect_catch");
        builder.register(id("extra_golden_chance"), AnglingModifierValue.ExtraGolden.CODEC)
                .register(id("adjust_lure_time"), AnglingModifierValue.AdjustLureTime.CODEC)
                .register(id("add_basic_sweetspot"), AnglingModifierValue.AddBasicSweetspot.CODEC)
                .register(id("add_leaves_spots"), AnglingModifierValue.AddLeaves.CODEC)
                .register(id("luck_attribute_modifier"), AnglingModifierValue.LuckByRarity.CODEC)
                .register(id("new_catch_increase"), AnglingModifierValue.IntegerValue.codec("increase", 0, 1_000_000))
                .register(id("spawn_treasure_on_hit_x"),
                        AnglingModifierValue.IntegerValue.codec("hits_to_spawn_treasure", 1, 1_000_000))
                .register(id("extra_base_catch"), AnglingModifierValue.CountAndPerfect.CODEC)
                .register(id("burn_on_miss"), AnglingModifierValue.BurnOnMiss.CODEC)
                .register(id("freeze_on_miss"), AnglingModifierValue.FreezeOnMiss.CODEC)
                .register(id("add_to_available_pool"), AnglingModifierValue.AddToPool.CODEC)
                .register(id("add_loot_table_to_fishing_loot"), AnglingModifierValue.LootTable.CODEC)
                .register(id("modify_award_fish"), AnglingModifierValue.AwardFish.CODEC)
                .register(id("override_fish_caught"), AnglingModifierValue.OverrideCatch.CODEC)
                .register(id("spawn_sweetspots"), AnglingModifierValue.SpawnSweetspots.CODEC)
                .register(id("disable_hit_sounds"), AnglingNativeModifier.DisableHitSounds.CODEC)
                .register(id("disable_miss_sounds"), AnglingNativeModifier.DisableMissSounds.CODEC)
                .register(id("flip_sweetspots_on_miss"), AnglingNativeModifier.FlipSweetspotsOnMiss.CODEC)
                .register(id("multi_layer_modifier"), AnglingNativeModifier.MultiLayer.CODEC)
                .register(id("teleport"), AnglingNativeModifier.Teleport.CODEC);
        AnglingTypedCompilerRegistry<AnglingModifierValue> registry = builder.build();
        if (registry.registeredIds().size() != REGISTERED_COUNT) {
            throw new IllegalStateException("Equipment modifier compiler count drifted");
        }
        return registry;
    }

    private static void register(
            AnglingTypedCompilerRegistry.Builder<AnglingModifierValue> builder,
            Codec<? extends AnglingModifierValue> codec,
            String... paths
    ) {
        for (String path : paths) builder.register(id(path), codec);
    }

    private static Identifier id(String path) {
        return Identifier.of(ElarionAnglingAddon.MOD_ID, path);
    }
}
