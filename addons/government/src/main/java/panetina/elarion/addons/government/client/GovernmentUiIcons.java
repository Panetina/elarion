package panetina.elarion.addons.government.client;

import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class GovernmentUiIcons {
    public static final String NAMESPACE = "elarion_government";
    public static final String ICON_PATH = "textures/gui/icons/";

    public static final Set<String> BASE_ICON_IDS = Set.of(
            "civic_crest",
            "proposal",
            "law",
            "office",
            "history",
            "government_form",
            "people",
            "current_votes",
            "timer",
            "approve",
            "settled",
            "reject",
            "archive",
            "project",
            "notice",
            "realm_name",
            "leader_election",
            "realm_color"
    );

    public static final Set<String> REALM_COLOR_IDS = Set.of(
            "dark_red",
            "red",
            "gold",
            "yellow",
            "dark_green",
            "green",
            "aqua",
            "dark_aqua",
            "dark_blue",
            "blue",
            "light_purple",
            "dark_purple",
            "white",
            "gray",
            "dark_gray",
            "black"
    );

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("seat_crest", "civic_crest"),
            Map.entry("published_record", "law"),
            Map.entry("rules", "law"),
            Map.entry("civic_rule", "law"),
            Map.entry("public_notice", "notice"),
            Map.entry("notices", "notice"),
            Map.entry("projects", "project"),
            Map.entry("realm_project", "project"),
            Map.entry("infrastructure", "project"),
            Map.entry("approve", "settled"),
            Map.entry("color", "realm_color")
    );

    private GovernmentUiIcons() {
    }

    public static Optional<String> texturePath(String rawIconId) {
        String iconId = normalize(rawIconId);
        if (BASE_ICON_IDS.contains(iconId) || REALM_COLOR_IDS.contains(iconId)) {
            return Optional.of(ICON_PATH + iconId + ".png");
        }
        String alias = ALIASES.get(iconId);
        if (alias != null && (BASE_ICON_IDS.contains(alias) || REALM_COLOR_IDS.contains(alias))) {
            return Optional.of(ICON_PATH + alias + ".png");
        }
        return Optional.empty();
    }

    public static Optional<Identifier> identifier(String rawIconId) {
        return texturePath(rawIconId).map(path -> Identifier.of(NAMESPACE, path));
    }

    public static boolean hasTexture(String rawIconId) {
        return texturePath(rawIconId).isPresent();
    }

    static String normalize(String rawIconId) {
        return rawIconId == null ? "" : rawIconId.trim().toLowerCase().replace(' ', '_');
    }
}
