package panetina.elarion.addons.angling.compile;

import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Immutable ID-to-runtime-value registry used by sweetspot behaviors. */
public final class AnglingIdentifierRegistry<T> {
    private final Map<Identifier, T> values;

    private AnglingIdentifierRegistry(Map<Identifier, T> values) {
        this.values = Map.copyOf(values);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public T require(Identifier definitionId, Identifier id) {
        Objects.requireNonNull(definitionId, "definitionId");
        Objects.requireNonNull(id, "id");
        T value = values.get(id);
        if (value == null) {
            throw new AnglingDefinitionCompileException(definitionId, id, "unregistered identifier");
        }
        return value;
    }

    public Set<Identifier> registeredIds() {
        return values.keySet();
    }

    public static final class Builder<T> {
        private final Map<Identifier, T> values = new LinkedHashMap<>();
        private boolean built;

        public Builder<T> register(Identifier id, T value) {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(value, "value");
            if (built) {
                throw new IllegalStateException("Angling identifier registry builder is already built");
            }
            if (values.putIfAbsent(id, value) != null) {
                throw new IllegalArgumentException("Duplicate Angling identifier: " + id);
            }
            return this;
        }

        public AnglingIdentifierRegistry<T> build() {
            if (built) {
                throw new IllegalStateException("Angling identifier registry builder is already built");
            }
            built = true;
            return new AnglingIdentifierRegistry<>(values);
        }
    }
}
