package panetina.elarion.addons.angling;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class AnglingItems {
    public static final Identifier PLACEHOLDER_CATCH_ITEM_ID =
            Identifier.of("elarion_angling", "placeholder_catch_item");
    public static final Identifier PLACEHOLDER_BAIT_ITEM_ID =
            Identifier.of("elarion_angling", "placeholder_bait_item");
    public static final Identifier PALE_BROOKLING_ID =
            Identifier.of("elarion_angling", "pale_brookling");
    public static final Identifier REEDGLASS_DARTER_ID =
            Identifier.of("elarion_angling", "reedglass_darter");
    public static final Identifier RAINTHREAD_MINNOW_ID =
            Identifier.of("elarion_angling", "rainthread_minnow");
    public static final Identifier MOONWELL_PIKE_ID =
            Identifier.of("elarion_angling", "moonwell_pike");
    public static final Identifier CAVERN_SILTFIN_ID =
            Identifier.of("elarion_angling", "cavern_siltfin");
    public static final Identifier STORMVEIL_KOI_ID =
            Identifier.of("elarion_angling", "stormveil_koi");
    public static final Identifier BAITBRIGHT_PERCH_ID =
            Identifier.of("elarion_angling", "baitbright_perch");
    private static final Map<Identifier, Identifier> REWARD_ITEMS_BY_FISH =
            createRewardItemsByFish();
    private static Item placeholderCatchItem;
    private static Item placeholderBaitItem;
    private static boolean registered;

    private AnglingItems() {
    }

    public static synchronized void register() {
        if (registered) return;
        placeholderCatchItem = new Item(new Item.Settings().maxCount(64));
        placeholderBaitItem = new Item(new Item.Settings().maxCount(64));
        Registry.register(Registries.ITEM, PLACEHOLDER_CATCH_ITEM_ID, placeholderCatchItem);
        Registry.register(Registries.ITEM, PLACEHOLDER_BAIT_ITEM_ID, placeholderBaitItem);
        for (Identifier itemId : REWARD_ITEMS_BY_FISH.values()) {
            Registry.register(Registries.ITEM, itemId, new Item(new Item.Settings().maxCount(64)));
        }
        registered = true;
    }

    public static Item placeholderCatchItem() {
        if (placeholderCatchItem == null) {
            throw new IllegalStateException("Angling items are not registered");
        }
        return placeholderCatchItem;
    }

    public static Item placeholderBaitItem() {
        if (placeholderBaitItem == null) {
            throw new IllegalStateException("Angling items are not registered");
        }
        return placeholderBaitItem;
    }

    public static Identifier rewardItemIdFor(Identifier fishDefinitionId) {
        return REWARD_ITEMS_BY_FISH.getOrDefault(
                Objects.requireNonNull(fishDefinitionId, "fishDefinitionId"),
                PLACEHOLDER_CATCH_ITEM_ID);
    }

    public static Map<Identifier, Identifier> rewardItemsByFish() {
        return REWARD_ITEMS_BY_FISH;
    }

    private static Map<Identifier, Identifier> createRewardItemsByFish() {
        Map<Identifier, Identifier> items = new LinkedHashMap<>();
        items.put(Identifier.of("elarion_angling", "placeholder_fish_001"), PALE_BROOKLING_ID);
        items.put(Identifier.of("elarion_angling", "placeholder_fish_002"), REEDGLASS_DARTER_ID);
        items.put(Identifier.of("elarion_angling", "placeholder_fish_003"), RAINTHREAD_MINNOW_ID);
        items.put(Identifier.of("elarion_angling", "placeholder_fish_004"), MOONWELL_PIKE_ID);
        items.put(Identifier.of("elarion_angling", "placeholder_fish_005"), CAVERN_SILTFIN_ID);
        items.put(Identifier.of("elarion_angling", "placeholder_fish_006"), STORMVEIL_KOI_ID);
        items.put(Identifier.of("elarion_angling", "placeholder_fish_007"), BAITBRIGHT_PERCH_ID);
        return Map.copyOf(items);
    }
}
