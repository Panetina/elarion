package panetina.elarion.addons.portals.service;

import panetina.elarion.core.model.RealmPresentation;
import panetina.elarion.core.placeholder.ElarionPlaceholderService;
import panetina.elarion.core.placeholder.PlaceholderRenderContext;

import java.util.Map;

final class PortalRealmText {
    private PortalRealmText() {
    }

    static String format(String raw, RealmPresentation realm, ElarionPlaceholderService placeholders) {
        if (raw == null || raw.isBlank() || realm == null) return raw == null ? "" : raw;
        return placeholders.resolvePublic(raw, PlaceholderRenderContext.UI, Map.of(
                "realm_display", realm.displayName(),
                "realm_official", realm.officialName(),
                "realm_tag", realm.shortName(),
                "realm_short", realm.shortName()));
    }
}
