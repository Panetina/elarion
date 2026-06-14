package panetina.elarion.addons.offerings.model;

import java.util.Locale;

public enum OfferingScope {
    REALM,
    GLOBAL,
    LOCATION;

    public static OfferingScope parse(String value) {
        if (value == null || value.isBlank()) return REALM;
        return valueOf(value.trim().replace('-', '_').toUpperCase(Locale.ROOT));
    }
}
