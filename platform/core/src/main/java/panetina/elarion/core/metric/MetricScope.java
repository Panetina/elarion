package panetina.elarion.core.metric;

import net.minecraft.util.Identifier;

import java.util.Objects;

public record MetricScope(MetricScopeType type, Identifier id) {
    public MetricScope {
        Objects.requireNonNull(type, "type");
        if ((type == MetricScopeType.GLOBAL) != (id == null)) {
            throw new IllegalArgumentException("global scope has no ID; realm and event scopes require one");
        }
    }

    public static MetricScope global() {
        return new MetricScope(MetricScopeType.GLOBAL, null);
    }

    public static MetricScope realm(Identifier realmId) {
        return new MetricScope(MetricScopeType.REALM, Objects.requireNonNull(realmId, "realmId"));
    }

    public static MetricScope event(Identifier eventId) {
        return new MetricScope(MetricScopeType.EVENT, Objects.requireNonNull(eventId, "eventId"));
    }
}
