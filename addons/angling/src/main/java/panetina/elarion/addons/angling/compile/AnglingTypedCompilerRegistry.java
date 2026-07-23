package panetina.elarion.addons.angling.compile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.definition.AnglingTypedNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable dispatch registry that compiles bounded reload nodes into typed
 * runtime objects. JSON parsing is confined to snapshot construction.
 */
public final class AnglingTypedCompilerRegistry<T> {
    private final Map<Identifier, Codec<? extends T>> codecs;

    private AnglingTypedCompilerRegistry(Map<Identifier, Codec<? extends T>> codecs) {
        this.codecs = Map.copyOf(codecs);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public T compile(Identifier definitionId, AnglingTypedNode node) {
        Objects.requireNonNull(definitionId, "definitionId");
        Objects.requireNonNull(node, "node");
        Codec<? extends T> codec = codecs.get(node.type());
        if (codec == null) {
            throw new AnglingDefinitionCompileException(definitionId, node.type(), "unregistered node type");
        }
        DataResult<? extends T> result = codec.parse(JsonOps.INSTANCE, node.copySource());
        return result.result().orElseThrow(() -> new AnglingDefinitionCompileException(
                definitionId,
                node.type(),
                result.error().map(DataResult.Error::message).orElse("typed codec rejected node")
        ));
    }

    public List<T> compileAll(Identifier definitionId, List<AnglingTypedNode> nodes) {
        Objects.requireNonNull(nodes, "nodes");
        List<T> compiled = new ArrayList<>(nodes.size());
        for (AnglingTypedNode node : nodes) {
            compiled.add(compile(definitionId, node));
        }
        return List.copyOf(compiled);
    }

    public Set<Identifier> registeredIds() {
        return codecs.keySet();
    }

    public static final class Builder<T> {
        private final Map<Identifier, Codec<? extends T>> codecs = new LinkedHashMap<>();
        private boolean built;

        public Builder<T> register(Identifier id, Codec<? extends T> codec) {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(codec, "codec");
            if (built) {
                throw new IllegalStateException("Angling typed compiler registry builder is already built");
            }
            if (codecs.putIfAbsent(id, codec) != null) {
                throw new IllegalArgumentException("Duplicate Angling typed compiler: " + id);
            }
            return this;
        }

        public AnglingTypedCompilerRegistry<T> build() {
            if (built) {
                throw new IllegalStateException("Angling typed compiler registry builder is already built");
            }
            built = true;
            return new AnglingTypedCompilerRegistry<>(codecs);
        }
    }
}
