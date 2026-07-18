package panetina.elarion.core.client.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;

public final class ElarionUiIcons {
    private static final String NAMESPACE = "elarion_core";
    private static final String LIBRARY = "textures/gui/library/";

    private static final Map<String, Icon> ICONS = Map.ofEntries(
            entry("mail", "documents/16x16/pack_1_open_envelope_0644.png", 16),
            entry("personal", "documents/16x16/pack_1_open_envelope_0644.png", 16),
            entry("letter", "documents/16x16/pack_1_open_envelope_0644.png", 16),
            entry("realm", "armor/32x32/pack_2_shield_0871.png", 32),
            entry("realm_name", "armor/32x32/pack_2_shield_0871.png", 32),
            entry("civic_crest", "armor/32x32/pack_2_shield_0872.png", 32),
            entry("seat_crest", "world_objects/32x32/pack_2_tower_0038.png", 32),
            entry("quest", "documents/32x32/pack_2_open_book_0104.png", 32),
            entry("world", "documents/16x16/pack_1_open_map_0660.png", 16),
            entry("portal", "documents/32x32/pack_2_ticket_0120.png", 32),
            entry("portal_ticket", "relics/16x16/pack_4_stele_crimson_completed_stele_crimson_completed_rare_collectibles_big_wander.png", 16),
            entry("nether_ticket", "relics/16x16/pack_4_stele_crimson_completed_stele_crimson_completed_rare_collectibles_big_wander.png", 16),
            entry("end_ticket", "relics/16x16/pack_4_stele_blue_completed_stele_blue_completed_rare_collectibles_big_wander.png", 16),
            entry("portal_free", "keys/32x32/pack_2_key_0071.png", 32),
            entry("portal_fee", "currency/32x32/pack_2_coin_pouch_0127.png", 32),
            entry("nether_gate", "world_objects/32x32/pack_2_purple_doorway_0036.png", 32),
            entry("end_gate", "world_objects/32x32/pack_2_stone_doorway_0024.png", 32),
            entry("shrine", "magic/32x32/pack_2_altar_candle_0083.png", 32),
            entry("offering", "magic/32x32/pack_2_altar_candle_0084.png", 32),
            entry("reward", "currency/32x32/pack_2_coin_stack_0113.png", 32),
            entry("requirement", "relics/32x32/pack_2_tablet_0094.png", 32),
            entry("history", "documents/32x32/pack_2_open_book_0098.png", 32),
            entry("archive", "documents/32x32/pack_2_open_book_0100.png", 32),
            entry("proposal", "documents/32x32/pack_2_scroll_0054.png", 32),
            entry("law", "documents/32x32/pack_2_book_0301.png", 32),
            entry("rules", "documents/32x32/pack_2_book_0302.png", 32),
            entry("notice", "documents/32x32/pack_2_blue_note_0010.png", 32),
            entry("project", "world_objects/32x32/pack_2_building_0033.png", 32),
            entry("office", "jewelry/32x32/pack_2_ring_1811.png", 32),
            entry("leader_election", "jewelry/32x32/pack_2_ring_1812.png", 32),
            entry("government_form", "relics/32x32/pack_2_tablet_0094.png", 32),
            entry("economy", "currency/32x32/pack_2_coin_pouch_0127.png", 32),
            entry("security", "armor/32x32/pack_2_shield_0875.png", 32),
            entry("culture", "documents/32x32/pack_2_open_book_0104.png", 32),
            entry("faith", "magic/32x32/pack_2_altar_candle_0084.png", 32),
            entry("infrastructure", "tools/32x32/pack_2_hammer_1563.png", 32),
            entry("current_votes", "documents/32x32/pack_2_gold_stamp_0131.png", 32),
            entry("people", "ui/32x32/pack_3_speech_bubble_0036.png", 32),
            entry("timer", "assorted/16x16/pack_4_hourglass_hourglass_rare_collectibles_big_wander.png", 16),
            entry("approve", "ui/32x32/pack_2_star_0012.png", 32),
            entry("settled", "ui/32x32/pack_2_star_0012.png", 32),
            entry("reject", "ui/32x32/pack_2_status_arrow_0611.png", 32),
            entry("view", "documents/32x32/pack_2_open_book_0100.png", 32),
            entry("back", "ui/32x32/pack_2_status_arrow_0602.png", 32),
            entry("go_to", "ui/32x32/pack_2_status_arrow_0603.png", 32),
            entry("claim", "currency/32x32/pack_2_coin_stack_0113.png", 32),
            entry("dismiss", "ui/32x32/pack_2_status_arrow_0611.png", 32),
            entry("accept", "ui/32x32/pack_2_star_0012.png", 32),
            entry("decline", "ui/32x32/pack_2_status_arrow_0611.png", 32),
            entry("profile", "documents/32x32/pack_2_book_0304.png", 32),
            entry("identity", "documents/32x32/pack_2_quill_0007.png", 32),
            entry("biography", "documents/32x32/pack_2_quill_0007.png", 32),
            entry("placement", "documents/32x32/pack_2_scroll_0294.png", 32),
            entry("collection", "gems/32x32/pack_2_blue_gem_0101.png", 32),
            entry("unlockables", "keys/32x32/pack_2_key_ring_0075.png", 32),
            entry("reputation", "relics/32x32/pack_2_tablet_0094.png", 32),
            entry("faction", "armor/32x32/pack_2_shield_0874.png", 32),
            entry("worldheart", "world_objects/32x32/pack_2_tower_0038.png", 32),
            entry("underworld", "creature_parts/32x32/pack_2_large_skull_0228.png", 32),
            entry("mounts", "world_objects/32x32/pack_2_mountain_0017.png", 32),
            entry("pets", "assorted/32x32/pack_3_heart_0037.png", 32),
            entry("titles", "jewelry/32x32/pack_2_ring_1813.png", 32),
            entry("advancements", "ui/32x32/pack_2_star_0012.png", 32),
            entry("progression", "gems/32x32/pack_2_blue_crystal_0020.png", 32),
            entry("affiliations", "armor/32x32/pack_2_shield_0874.png", 32),
            entry("lifetime", "creature_parts/32x32/pack_2_large_skull_0228.png", 32),
            entry("chronicle", "documents/32x32/pack_2_open_book_0102.png", 32),
            entry("grave", "creature_parts/32x32/pack_2_skull_0225.png", 32),
            entry("admin", "tools/32x32/pack_2_hammer_1562.png", 32),
            entry("config", "ui/32x32/pack_3_save_scroll_0034.png", 32),
            entry("bank", "currency/32x32/pack_2_coin_pouch_0127.png", 32),
            entry("trade", "world_objects/32x32/pack_2_bag_0001.png", 32),
            entry("npc", "portraits/32x32/portrait_character_portrait_icons_04_icons_04.png", 32),
            entry("warning", "ui/32x32/pack_2_question_mark_0013.png", 32),
            entry("locked", "keys/32x32/pack_2_key_0072.png", 32),
            entry("default", "documents/32x32/pack_2_parchment_0008.png", 32)
    );

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("mail_new", "mail"),
            Map.entry("mail_nonew", "mail"),
            Map.entry("realm_new", "realm"),
            Map.entry("realm_nonew", "realm"),
            Map.entry("quest_new", "quest"),
            Map.entry("quest_nonew", "quest"),
            Map.entry("world_new", "world"),
            Map.entry("world_nonew", "world"),
            Map.entry("published_record", "law"),
            Map.entry("civic_rule", "law"),
            Map.entry("public_notice", "notice"),
            Map.entry("notices", "notice"),
            Map.entry("projects", "project"),
            Map.entry("realm_project", "project"),
            Map.entry("infrastructure", "project"),
            Map.entry("government_proposal", "proposal"),
            Map.entry("citizen_proposal", "proposal"),
            Map.entry("founding_decisions", "realm"),
            Map.entry("current_votes", "current_votes"),
            Map.entry("color", "realm_color"),
            Map.entry("realm_color", "collection"),
            Map.entry("citizen", "profile"),
            Map.entry("citizen_standing", "profile"),
            Map.entry("standing", "reputation"),
            Map.entry("reputations", "reputation"),
            Map.entry("factions", "faction"),
            Map.entry("realm_faction", "realm"),
            Map.entry("worldheart_faction", "worldheart"),
            Map.entry("underworld_faction", "underworld"),
            Map.entry("lifetime_record", "lifetime"),
            Map.entry("active_title", "titles")
    );

    private ElarionUiIcons() {
    }

    public static Optional<Identifier> identifier(String rawIconId) {
        return icon(rawIconId).map(icon -> icon.identifier);
    }

    public static boolean has(String rawIconId) {
        return icon(rawIconId).isPresent();
    }

    public static void draw(DrawContext context, String rawIconId, int x, int y, int size) {
        icon(rawIconId).ifPresent(icon -> draw(context, icon, x, y, size));
    }

    public static void drawOrDefault(DrawContext context, String rawIconId, int x, int y, int size) {
        draw(context, icon(rawIconId).orElse(ICONS.get("default")), x, y, size);
    }

    public static Optional<String> texturePath(String rawIconId) {
        return icon(rawIconId).map(icon -> LIBRARY + icon.path);
    }

    private static Optional<Icon> icon(String rawIconId) {
        String key = normalize(rawIconId);
        if (key.startsWith("texture:")) {
            key = normalize(key.substring("texture:".length()));
        }
        Icon direct = ICONS.get(key);
        if (direct != null) return Optional.of(direct);
        String alias = ALIASES.get(key);
        if (alias != null) {
            Icon mapped = ICONS.get(alias);
            if (mapped != null) return Optional.of(mapped);
        }
        return Optional.empty();
    }

    private static void draw(DrawContext context, Icon icon, int x, int y, int size) {
        if (icon == null || size <= 0) return;
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0.0F);
        context.getMatrices().scale(size / (float) icon.sourceSize, size / (float) icon.sourceSize, 1.0F);
        context.drawTexture(icon.identifier, 0, 0, 0.0F, 0.0F,
                icon.sourceSize, icon.sourceSize, icon.sourceSize, icon.sourceSize);
        context.getMatrices().pop();
    }

    private static Map.Entry<String, Icon> entry(String id, String path, int sourceSize) {
        return Map.entry(id, new Icon(path, sourceSize));
    }

    public static String normalize(String rawIconId) {
        return rawIconId == null ? "" : rawIconId.trim().toLowerCase()
                .replace(' ', '_')
                .replace('-', '_')
                .replace('.', '_')
                .replace('/', '_');
    }

    private record Icon(String path, int sourceSize, Identifier identifier) {
        Icon(String path, int sourceSize) {
            this(path, sourceSize, Identifier.of(NAMESPACE, LIBRARY + path));
        }
    }
}
