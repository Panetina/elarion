package panetina.elarion.addons.angling.resource;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.condition.AnglingConditionContext;
import panetina.elarion.addons.angling.condition.AnglingBuiltinConditions;
import panetina.elarion.addons.angling.condition.AnglingConditionRegistry;
import panetina.elarion.addons.angling.AnglingItems;
import panetina.elarion.addons.angling.loader.FishDefinitionLoader;
import panetina.elarion.addons.angling.model.AnglingRarity;
import panetina.elarion.addons.angling.model.FishDefinitionIndex;
import panetina.elarion.addons.angling.service.FishCandidateSelector;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlaceholderFishResourceTest {
    @Test
    void placeholderFishResourceLoadsThroughPureLoader() throws IOException {
        FishDefinitionIndex index = new FishDefinitionLoader().load(packagedFishJson());

        assertEquals(7, index.all().size());
        var definition = index.all().getFirst();
        assertEquals(Identifier.of("elarion_angling", "placeholder_fish_001"), definition.id());
        assertEquals(AnglingRarity.PLACEHOLDER_COMMON, definition.rarity());
        assertEquals(20, definition.weight());
        assertEquals(definition, index.byCondition(AnglingBuiltinConditions.FLUID_WATER).getFirst());
        assertEquals(definition, index.byCondition(AnglingBuiltinConditions.DIMENSION_OVERWORLD).getFirst());
        assertEquals(1, index.byRarity(AnglingRarity.PLACEHOLDER_COMMON).size());
        assertEquals(3, index.byRarity(AnglingRarity.PLACEHOLDER_UNCOMMON).size());
        assertEquals(2, index.byRarity(AnglingRarity.PLACEHOLDER_RARE).size());
        assertEquals(1, index.byRarity(AnglingRarity.PLACEHOLDER_EPIC).size());
    }

    @Test
    void packagedPlaceholderFishExerciseBuiltinConditionSelection() throws IOException {
        FishDefinitionRepository repository = new FishDefinitionRepository();
        repository.publish(new FishDefinitionLoader().load(packagedFishJson()));
        AnglingConditionRegistry conditions = new AnglingConditionRegistry();
        AnglingBuiltinConditions.register(conditions);
        FishCandidateSelector selector = new FishCandidateSelector(repository, conditions);

        assertEquals(
                List.of(id("placeholder_fish_001"), id("placeholder_fish_002")),
                selector.eligible(context(
                                Identifier.of("minecraft", "river"),
                                64,
                                6_000,
                                false,
                                false))
                        .stream()
                        .map(definition -> definition.id())
                        .toList());
        assertTrue(selector.eligible(context(
                        Identifier.of("minecraft", "plains"),
                        64,
                        18_000,
                        false,
                        false))
                .stream()
                .map(definition -> definition.id())
                .toList()
                .contains(id("placeholder_fish_004")));
        assertTrue(selector.eligible(context(
                        Identifier.of("minecraft", "plains"),
                        20,
                        6_000,
                        false,
                        false))
                .stream()
                .map(definition -> definition.id())
                .toList()
                .contains(id("placeholder_fish_005")));
        assertTrue(selector.eligible(context(
                        Identifier.of("minecraft", "plains"),
                        64,
                        6_000,
                        true,
                        true,
                        null))
                .stream()
                .map(definition -> definition.id())
                .toList()
                .contains(id("placeholder_fish_006")));
        assertTrue(selector.eligible(context(
                        Identifier.of("minecraft", "plains"),
                        64,
                        6_000,
                        false,
                        false,
                        AnglingItems.PLACEHOLDER_BAIT_ITEM_ID))
                .stream()
                .map(definition -> definition.id())
                .toList()
                .contains(id("placeholder_fish_007")));
    }

    @Test
    void playerFacingFishNamesUseOriginalWorkingNames() throws IOException {
        String json = readResource("/assets/elarion_angling/lang/en_us.json");

        assertTrue(json.contains(
                "\"fish.elarion_angling.placeholder_fish_001\": \"Pale Brookling\""));
        assertTrue(json.contains(
                "\"fish.elarion_angling.placeholder_fish_006\": \"Stormveil Koi\""));
        assertTrue(json.contains(
                "\"fish.elarion_angling.placeholder_fish_007\": \"Baitbright Perch\""));
        assertFalse(json.contains("fish.placeholder_fish_001.name"));
    }

    @Test
    void feedbackCopyRemainsExplicitPlaceholderCopy() throws IOException {
        String json = readResource("/assets/elarion_angling/lang/en_us.json");

        assertTrue(json.contains(
                "\"feedback.elarion_angling.catch_accepted\": \"[REPLACE: feedback.catch.accepted]\""));
        assertTrue(json.contains(
                "\"feedback.elarion_angling.catch_unavailable\": \"[REPLACE: feedback.catch.unavailable]\""));
        assertTrue(json.contains(
                "\"modmenu.descriptionTranslation.elarion_angling\": \"Fishing addon shell for future Elarion catch systems.\""));
    }

    @Test
    void workingItemNamesAreOriginalAndIndexedForReplacement() throws IOException {
        String json = readResource("/assets/elarion_angling/lang/en_us.json");

        assertTrue(json.contains(
                "\"item.elarion_angling.placeholder_catch_item\": \"Angler's Keepsake\""));
        assertTrue(json.contains(
                "\"item.elarion_angling.placeholder_bait_item\": \"Glowthread Bait\""));
        assertTrue(json.contains(
                "\"item.elarion_angling.pale_brookling\": \"Pale Brookling\""));
        assertTrue(json.contains(
                "\"item.elarion_angling.stormveil_koi\": \"Stormveil Koi\""));
        assertTrue(json.contains(
                "\"item.elarion_angling.baitbright_perch\": \"Baitbright Perch\""));
        assertFalse(json.contains("item.placeholder_catch_item.name"));
        assertFalse(json.contains("item.placeholder_bait_item.name"));
    }

    @Test
    void workingItemModelsUseOriginalAnglingTexturePaths() throws IOException {
        String json = readResource("/assets/elarion_angling/models/item/placeholder_catch_item.json");

        assertTrue(json.contains("\"parent\": \"minecraft:item/generated\""));
        assertTrue(json.contains("\"layer0\": \"elarion_angling:item/catch_item\""));
        assertTrue(resourceExists("/assets/elarion_angling/textures/item/catch_item.png"));

        String baitJson = readResource("/assets/elarion_angling/models/item/placeholder_bait_item.json");
        assertTrue(baitJson.contains("\"parent\": \"minecraft:item/generated\""));
        assertTrue(baitJson.contains("\"layer0\": \"elarion_angling:item/bait_item\""));
        assertTrue(resourceExists("/assets/elarion_angling/textures/item/bait_item.png"));

        assertFishItemModelAndTexture("pale_brookling");
        assertFishItemModelAndTexture("reedglass_darter");
        assertFishItemModelAndTexture("rainthread_minnow");
        assertFishItemModelAndTexture("moonwell_pike");
        assertFishItemModelAndTexture("cavern_siltfin");
        assertFishItemModelAndTexture("stormveil_koi");
        assertFishItemModelAndTexture("baitbright_perch");
    }

    private static void assertFishItemModelAndTexture(String path) throws IOException {
        String json = readResource("/assets/elarion_angling/models/item/" + path + ".json");
        assertTrue(json.contains("\"parent\": \"minecraft:item/generated\""));
        assertTrue(json.contains("\"layer0\": \"elarion_angling:item/fish/" + path + "\""));
        assertTrue(resourceExists("/assets/elarion_angling/textures/item/fish/" + path + ".png"));
    }

    private static String readResource(String path) throws IOException {
        try (var stream = PlaceholderFishResourceTest.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Missing test resource: " + path);
            }
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                var writer = new java.io.StringWriter();
                reader.transferTo(writer);
                return writer.toString();
            }
        }
    }

    private static boolean resourceExists(String path) throws IOException {
        try (var stream = PlaceholderFishResourceTest.class.getResourceAsStream(path)) {
            return stream != null;
        }
    }

    private static Map<String, String> packagedFishJson() throws IOException {
        Map<String, String> resources = new LinkedHashMap<>();
        for (int index = 1; index <= 7; index++) {
            String path = "placeholder_fish_%03d".formatted(index);
            resources.put(
                    "angling/fish/" + path,
                    readResource("/data/elarion_angling/angling/fish/" + path + ".json"));
        }
        return resources;
    }

    private static Identifier id(String path) {
        return Identifier.of("elarion_angling", path);
    }

    private static AnglingConditionContext context(
            Identifier biomeId,
            int blockY,
            long timeOfDay,
            boolean raining,
            boolean thundering
    ) {
        return context(biomeId, blockY, timeOfDay, raining, thundering, null);
    }

    private static AnglingConditionContext context(
            Identifier biomeId,
            int blockY,
            long timeOfDay,
            boolean raining,
            boolean thundering,
            Identifier baitId
    ) {
        return new AnglingConditionContext(
                UUID.randomUUID(),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "overworld"),
                biomeId,
                Identifier.of("minecraft", "water"),
                baitId,
                blockY,
                timeOfDay,
                raining,
                thundering);
    }
}
