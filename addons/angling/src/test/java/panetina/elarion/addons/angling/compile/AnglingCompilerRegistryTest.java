package panetina.elarion.addons.angling.compile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.definition.AnglingTypedNode;
import panetina.elarion.addons.angling.modifier.AnglingEquipmentModifierCompilers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingCompilerRegistryTest {
    private static final Identifier DEFINITION = Identifier.of("elarion_angling", "aloe_bream");
    private static final Identifier TYPE = Identifier.of("elarion_angling", "weight_test");

    @Test
    void typedNodesCompileOnceIntoImmutableRuntimeValues() {
        AnglingTypedCompilerRegistry<TestRule> registry = AnglingTypedCompilerRegistry.<TestRule>builder()
                .register(TYPE, TestRule.CODEC)
                .build();
        AnglingTypedNode node = new AnglingTypedNode(TYPE,
                "{\"type\":\"elarion_angling:weight_test\",\"weight\":7}");

        List<TestRule> compiled = registry.compileAll(DEFINITION, List.of(node));
        assertEquals(List.of(new TestRule(7)), compiled);
        assertThrows(UnsupportedOperationException.class, () -> compiled.clear());
    }

    @Test
    void unknownAndDuplicateTypesFailWithDefinitionContext() {
        AnglingTypedCompilerRegistry.Builder<TestRule> builder = AnglingTypedCompilerRegistry.builder();
        builder.register(TYPE, TestRule.CODEC);
        assertThrows(IllegalArgumentException.class, () -> builder.register(TYPE, TestRule.CODEC));

        AnglingTypedCompilerRegistry<TestRule> registry = builder.build();
        Identifier unknown = Identifier.of("elarion_angling", "unknown");
        AnglingDefinitionCompileException error = assertThrows(AnglingDefinitionCompileException.class,
                () -> registry.compile(DEFINITION,
                        new AnglingTypedNode(unknown, "{\"type\":\"elarion_angling:unknown\"}")));
        assertEquals(DEFINITION, error.definitionId());
        assertEquals(unknown, error.nodeType());
    }

    @Test
    void sweetspotRegistryIsFrozenAfterBuild() {
        AnglingIdentifierRegistry.Builder<String> builder = AnglingIdentifierRegistry.builder();
        builder.register(Identifier.of("elarion_angling", "normal"), "normal-behavior");
        AnglingIdentifierRegistry<String> registry = builder.build();
        assertEquals("normal-behavior", registry.require(DEFINITION,
                Identifier.of("elarion_angling", "normal")));
        assertThrows(IllegalStateException.class, () -> builder.register(
                Identifier.of("elarion_angling", "treasure"), "treasure-behavior"));
    }

    @Test
    void modifierDispatchCoversTheCompleteFrozenReferenceRoster() {
        var registry = AnglingEquipmentModifierCompilers.create();
        assertEquals(52, registry.registeredIds().size());
        assertTrue(registry.registeredIds().contains(Identifier.of("elarion_angling", "empty")));
        assertTrue(registry.registeredIds().contains(
                Identifier.of("elarion_angling", "flip_sweetspots_on_miss")));
        assertTrue(registry.registeredIds().contains(
                Identifier.of("elarion_angling", "multi_layer_modifier")));
    }

    private record TestRule(int weight) {
        private static final Codec<TestRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("weight").forGetter(TestRule::weight)
        ).apply(instance, TestRule::new));
    }
}
