package panetina.elarion.addons.angling.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import panetina.elarion.addons.angling.compile.AnglingTypedCompilerRegistry;
import panetina.elarion.addons.angling.definition.AnglingTypedNode;
import panetina.elarion.addons.angling.modifier.AnglingCompiledModifier;
import panetina.elarion.addons.angling.modifier.AnglingEquipmentModifierCompilers;
import panetina.elarion.addons.angling.modifier.AnglingModifierValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Persistent equipment modifiers compiled once when component data is decoded or crafted.
 * Gameplay reads {@link #compiled()} and never reparses polymorphic JSON.
 */
public final class AnglingModifierComponent {
    public static final int MAX_MODIFIERS = 64;
    private static final AnglingTypedCompilerRegistry<AnglingModifierValue> COMPILERS =
            AnglingEquipmentModifierCompilers.create();
    private static final Codec<List<AnglingTypedNode>> NODE_LIST_CODEC = AnglingTypedNode.CODEC.listOf()
            .validate(values -> values.size() <= MAX_MODIFIERS
                    ? DataResult.success(values)
                    : DataResult.error(() -> "equipment modifier component exceeds " + MAX_MODIFIERS + " entries"));
    public static final Codec<AnglingModifierComponent> CODEC = NODE_LIST_CODEC.xmap(
            AnglingModifierComponent::new,
            AnglingModifierComponent::serializedNodes);

    private final List<AnglingTypedNode> serializedNodes;
    private final List<AnglingCompiledModifier> compiled;

    public AnglingModifierComponent(List<AnglingTypedNode> serializedNodes) {
        Objects.requireNonNull(serializedNodes, "serializedNodes");
        if (serializedNodes.size() > MAX_MODIFIERS) {
            throw new IllegalArgumentException("Equipment modifier component exceeds " + MAX_MODIFIERS + " entries");
        }
        this.serializedNodes = List.copyOf(serializedNodes);
        ArrayList<AnglingCompiledModifier> values = new ArrayList<>(serializedNodes.size());
        for (AnglingTypedNode node : serializedNodes) {
            AnglingModifierValue value = COMPILERS.compile(node.type(), node);
            values.add(new AnglingCompiledModifier(node.type(), value));
        }
        this.compiled = List.copyOf(values);
    }

    public List<AnglingTypedNode> serializedNodes() {
        return serializedNodes;
    }

    public List<AnglingCompiledModifier> compiled() {
        return compiled;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof AnglingModifierComponent component
                && serializedNodes.equals(component.serializedNodes);
    }

    @Override
    public int hashCode() {
        return serializedNodes.hashCode();
    }

    @Override
    public String toString() {
        return "AnglingModifierComponent" + serializedNodes;
    }
}
