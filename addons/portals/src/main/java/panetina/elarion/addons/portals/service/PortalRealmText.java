package panetina.elarion.addons.portals.service;

import panetina.elarion.core.model.RealmPresentation;

final class PortalRealmText {
    private PortalRealmText() {
    }

    static String format(String raw, RealmPresentation realm) {
        if (raw == null || raw.isBlank() || realm == null) return raw == null ? "" : raw;
        return raw
                .replace("%realm_display%", realm.displayName())
                .replace("%realm_official%", realm.officialName())
                .replace("%realm_tag%", realm.shortName());
    }
}
