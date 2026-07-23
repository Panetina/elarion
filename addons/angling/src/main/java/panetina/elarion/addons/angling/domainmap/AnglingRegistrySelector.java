package panetina.elarion.addons.angling.domainmap;

import net.minecraft.util.Identifier;

import java.util.Objects;

/** Direct registry ID or tag selector used by immutable domain snapshots. */
public record AnglingRegistrySelector(boolean tag, Identifier id) {
    public AnglingRegistrySelector {
        Objects.requireNonNull(id, "id");
    }

    public static AnglingRegistrySelector parse(String value) {
        Objects.requireNonNull(value, "value");
        boolean tag = value.startsWith("#");
        String rawId = tag ? value.substring(1) : value;
        Identifier id = Identifier.tryParse(rawId);
        if (id == null) throw new IllegalArgumentException("Invalid registry selector " + value);
        return new AnglingRegistrySelector(tag, id);
    }

    @Override
    public String toString() {
        return (tag ? "#" : "") + id;
    }
}
