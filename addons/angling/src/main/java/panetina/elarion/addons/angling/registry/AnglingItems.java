package panetina.elarion.addons.angling.registry;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;
import panetina.elarion.addons.angling.item.AnglingFoodComponents;
import panetina.elarion.addons.angling.item.AnglingBucketableFishItem;
import panetina.elarion.addons.angling.item.AnglingFishBucketItem;
import panetina.elarion.addons.angling.item.AnglingFishingRodItem;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingSingleStackComponent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.Rarity;

/**
 * Completed basic items plus the bucketable-fish item/entity slice from the frozen registry.
 * Custom guide, rod, letter, hat, and block-item behavior is registered by later owners.
 */
public final class AnglingItems {
    private static final List<String> BASIC_ITEMS = List.of(
            "missingno", "unknown_fish", "settings", "default_minigame", "default_catch", "treasure",
            "worm", "almighty_worm", "seeking_worm", "dev_worm",
            "gunpowder_bait", "cherry_bait", "lush_bait", "sculk_bait", "dripstone_bait", "murkwater_bait",
            "legendary_bait", "meteorological_bait", "fish_bones", "pearl", "sodden_boot"
    );
    private static final List<String> SINGLE_STACK_ITEMS = List.of(
            "fish_radar", "elarion_angling_twine",
            "hook", "amethyst_hook", "shiny_hook", "gold_hook", "mossy_hook", "stone_hook", "split_hook",
            "heavy_hook", "vanilla_hook", "copper_hook", "exposed_copper_hook", "weathered_copper_hook",
            "oxidised_copper_hook", "echoing_hook", "frozen_hook",
            "bobber", "steady_bobber", "clear_bobber", "deep_ocean_bobber", "vanilla_bobber", "leaf_bobber",
            "slimey_bobber", "glowing_bobber", "golden_bobber", "cloud_bobber"
    );
    private static final List<String> TEMPLATES = List.of(
            "pearl_smithing_template", "kimbe_smithing_template", "colorful_smithing_template",
            "clear_smithing_template", "frog_smithing_template", "king_smithing_template",
            "valley_smithing_template", "survivor_smithing_template", "naturalist_skin_smithing_template",
            "iceborn_skin_smithing_template", "magmaforged_skin_smithing_template",
            "slimed_skin_smithing_template", "sharktooth_skin_smithing_template",
            "azure_crystal_skin_smithing_template", "bamboo_skin_smithing_template",
            "obsidian_skin_smithing_template", "alpha_skin_smithing_template",
            "good_old_skin_smithing_template", "boner_skin_smithing_template", "sky_skin_smithing_template",
            "lush_glowberry_skin_smithing_template", "humble_skin_smithing_template"
    );
    private static final List<String> NON_BUCKET_FISH = List.of(
            "abyssfin", "aurorafin", "bloomfish", "blossom_bass", "bluejiji", "brittle_seaweed",
            "cerulean_crystalback_minnow", "chorus_shellcrab", "chorusfin_minnow", "darkglow",
            "deepslate_fin", "dripstone_fin", "dunetail", "dusk_eel", "dusky_amethyst_snapper",
            "ender_glowfin", "fossil_angelfish", "geode_ribbon_eel", "gilded_fanfin", "icejaw_trout",
            "joelian", "mossbound_boot", "mycelfin", "ocean_bass", "pallid_bamboo_fin", "pallid_carp",
            "prismback_sturgeon", "radiant_amethyst_snapper", "rose_siamese_fin", "ruby_mackerel",
            "sagewhisker_catfish", "scorchfin", "seaglass_pike", "sporefin", "starcloud_squid", "stonefin",
            "stonegill", "summit_dweller", "sunbloom_carp", "vesanii", "violet_carp", "violet_crystalback",
            "void_jelly", "voidfang", "wardfish"
    );
    private static final Set<String> FIREPROOF_FISH = Set.of(
            "boiling_pike", "charred_bloodsucker", "cindergill", "ember_squid", "flame_trout",
            "glowstone_hunter", "glowstone_puffer", "hellhound_ray", "magma_crab", "magma_crab_claw",
            "magma_shrimp", "magmafin", "molten_slate_crab", "obsidian_ribbon_eel", "obsidian_shellcrab",
            "sun_devourer", "willowish"
    );
    private static final List<String> BUCKETABLE_FISH = List.of(
            "obsidonti", "silvermist_perch", "elderfin", "driftwing", "dusk_koi", "storm_bass",
            "stormflash_bass", "mire_catfish", "lilypad_snapper", "pallid_pinfish", "pinfin",
            "prismback_trout", "wintry_pike", "miragefin_carp", "cactusfin", "aloe_bream",
            "sunlit_sturgeon", "sunseeker_carp", "bloomdrift_carp", "rose_koi", "morganite_minnow",
            "frosttooth_sturgeon", "borealis_fin", "crystalback_borealis", "silvercrest_pike",
            "carpenjo", "willowfin_bream", "driftwater_bream", "rainfell_bream", "hollowgut_darter",
            "mistfin_chub", "icegill_chub", "prismback_minnow", "sapphire_crystalfin",
            "cobalt_herring", "steeljaw_herring", "abyssjaw_herring", "duskfin_snapper",
            "redscale_tuna", "moon_eye_tuna", "ivoryveil", "brimstone_fish", "verdant_pike",
            "radiant_mossfin", "quarryfish", "phantom_pike", "sculkfin", "charred_char"
    );
    private static final List<String> RODS = List.of(
            "elarion_angling_rod", "naturalist_rod", "iceborn_rod", "magmaforged_rod", "slimed_rod",
            "sharktooth_rod", "azure_crystal_rod", "good_old_rod", "bamboo_rod", "obsidian_rod",
            "alpha_rod", "boner_rod", "sky_rod", "lush_glowberry_rod", "humble_rod"
    );

    public static final int REGISTERED_COUNT = 198;
    private static final Map<Identifier, Item> ALL = bootstrap();

    private AnglingItems() {
    }

    public static void initialize() {
        if (ALL.size() != REGISTERED_COUNT) {
            throw new IllegalStateException("Angling vanilla-behavior item registry count drifted");
        }
    }

    public static Map<Identifier, Item> snapshot() {
        return ALL;
    }

    public static Item require(String path) {
        Item item = ALL.get(id(path));
        if (item == null) throw new IllegalArgumentException("Unregistered Angling item " + path);
        return item;
    }

    private static Map<Identifier, Item> bootstrap() {
        LinkedHashMap<Identifier, Item> items = new LinkedHashMap<>();
        BASIC_ITEMS.forEach(path -> register(items, path, new Item.Settings()));
        Item fishBones = items.get(id("fish_bones"));
        register(items, "starcaught_fish", new Item.Settings().food(AnglingFoodComponents.rawFish(fishBones)));
        register(items, "cooked_starcaught_fish",
                new Item.Settings().food(AnglingFoodComponents.cookedFish(fishBones)));
        TEMPLATES.forEach(path -> register(items, path, new Item.Settings()));
        NON_BUCKET_FISH.forEach(path -> register(items, path, new Item.Settings()));
        FIREPROOF_FISH.stream().sorted().forEach(path -> register(items, path, new Item.Settings().fireproof()));
        SINGLE_STACK_ITEMS.forEach(path -> register(items, path, new Item.Settings().maxCount(1)));
        BUCKETABLE_FISH.forEach(path -> registerBucketableFish(items, path));
        registerFishBucket(items);
        RODS.forEach(path -> registerRod(items, path));
        if (items.size() != REGISTERED_COUNT) {
            throw new IllegalStateException("Expected " + REGISTERED_COUNT + " completed Angling items, found "
                    + items.size());
        }
        return Map.copyOf(items);
    }

    private static void register(Map<Identifier, Item> items, String path, Item.Settings settings) {
        Identifier id = id(path);
        Item item = Registry.register(Registries.ITEM, id, new Item(settings));
        if (items.putIfAbsent(id, item) != null) throw new IllegalStateException("Duplicate Angling item " + id);
    }

    private static void registerBucketableFish(Map<Identifier, Item> items, String path) {
        Identifier id = id(path);
        Item item = Registry.register(Registries.ITEM, id,
                new AnglingBucketableFishItem(new Item.Settings().food(
                        AnglingFoodComponents.rawFish(items.get(id("fish_bones"))))));
        if (items.putIfAbsent(id, item) != null) throw new IllegalStateException("Duplicate Angling item " + id);
    }

    private static void registerFishBucket(Map<Identifier, Item> items) {
        Identifier id = id("starcaught_bucket");
        Item item = Registry.register(Registries.ITEM, id,
                new AnglingFishBucketItem(new Item.Settings().maxCount(1).component(
                        AnglingDataComponents.BUCKETED_FISH, AnglingSingleStackComponent.EMPTY)));
        if (items.putIfAbsent(id, item) != null) throw new IllegalStateException("Duplicate Angling item " + id);
    }

    private static void registerRod(Map<Identifier, Item> items, String path) {
        Identifier id = id(path);
        Item item = Registry.register(Registries.ITEM, id, new AnglingFishingRodItem(
                new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof()
                        .component(AnglingDataComponents.BOBBER,
                                new AnglingSingleStackComponent(new net.minecraft.item.ItemStack(
                                        items.get(id("bobber")))))
                        .component(AnglingDataComponents.BAIT, AnglingSingleStackComponent.EMPTY)
                        .component(AnglingDataComponents.HOOK,
                                new AnglingSingleStackComponent(new net.minecraft.item.ItemStack(
                                        items.get(id("hook")))))));
        if (items.putIfAbsent(id, item) != null) throw new IllegalStateException("Duplicate Angling item " + id);
    }

    private static Identifier id(String path) {
        return Identifier.of(ElarionAnglingAddon.MOD_ID, path);
    }
}
