package panetina.elarion.addons.angling.component;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import panetina.elarion.core.model.CatchSpeciesSummary;
import panetina.elarion.addons.angling.definition.AnglingTypedNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingPortableComponentBoundsTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void messageLimitsAreEnforcedByConstructionAndCodec() {
        AnglingLetterMessageComponent valid = new AnglingLetterMessageComponent(
                UUID.randomUUID(), "seventeen-chars!", Identifier.ofVanilla("overworld"), List.of("x".repeat(40)), false);
        assertTrue(AnglingLetterMessageComponent.CODEC.encodeStart(JsonOps.INSTANCE, valid).result().isPresent());
        assertThrows(IllegalArgumentException.class, () -> new AnglingLetterMessageComponent(
                UUID.randomUUID(), "x".repeat(18), Identifier.ofVanilla("overworld"), List.of(), false));
        assertThrows(IllegalArgumentException.class, () -> new AnglingLetterMessageComponent(
                UUID.randomUUID(), "sender", Identifier.ofVanilla("overworld"), List.of("x".repeat(41)), false));
        assertThrows(IllegalArgumentException.class, () -> new AnglingLetterMessageComponent(
                UUID.randomUUID(), "sender", Identifier.ofVanilla("overworld"),
                Collections.nCopies(16, "line"), false));
    }

    @Test
    void storedStacksAreCopiedAndTackleBoxIsBounded() {
        ItemStack original = ItemStack.EMPTY.copy();
        AnglingSingleStackComponent single = new AnglingSingleStackComponent(original);
        assertTrue(single.stack().isEmpty());

        AnglingTackleBoxComponent atLimit = new AnglingTackleBoxComponent(
                Collections.nCopies(AnglingTackleBoxComponent.MAX_FISH_STACKS, ItemStack.EMPTY));
        assertEquals(AnglingTackleBoxComponent.MAX_FISH_STACKS, atLimit.size());
        assertThrows(IllegalArgumentException.class, () -> new AnglingTackleBoxComponent(
                Collections.nCopies(AnglingTackleBoxComponent.MAX_FISH_STACKS + 1, ItemStack.EMPTY)));
    }

    @Test
    void allSecretNoteIdentitiesDecode() {
        for (AnglingSecretNoteComponent note : AnglingSecretNoteComponent.values()) {
            assertEquals(note, AnglingSecretNoteComponent.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(note.asString()))
                    .getOrThrow());
        }
    }

    @Test
    void signedGuideIsAnImmutableBoundedCoreProjectionSnapshot() {
        Identifier fish = Identifier.of("elarion_angling", "aloe_bream");
        CatchSpeciesSummary summary = new CatchSpeciesSummary(
                3, 1_725_000_000_000L, 80, 300, 3, 450, 1_200, 250, 1, 2, 1);
        AnglingSignedGuideComponent component = new AnglingSignedGuideComponent(
                UUID.randomUUID(), Map.of(fish, summary), "River Keeper", 1_725_000_001_000L);

        AnglingSignedGuideComponent decoded = AnglingSignedGuideComponent.CODEC
                .parse(JsonOps.INSTANCE, AnglingSignedGuideComponent.CODEC.encodeStart(JsonOps.INSTANCE, component)
                        .getOrThrow())
                .getOrThrow();
        assertEquals(component, decoded);
        assertThrows(UnsupportedOperationException.class, () -> decoded.species().clear());
        assertThrows(IllegalArgumentException.class, () -> new AnglingSignedGuideComponent(
                UUID.randomUUID(), Map.of(), "x".repeat(AnglingSignedGuideComponent.MAX_SIGNATURE_LENGTH + 1), 1));
        assertThrows(IllegalArgumentException.class, () -> new AnglingSignedGuideComponent(
                UUID.randomUUID(), Map.of(), "valid", -1));
    }

    @Test
    void persistedModifiersCompileOnceAndRoundTripWithoutMutableLists() {
        AnglingTypedNode node = AnglingTypedNode.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {"type":"elarion_angling:adjust_handle_speed","multiplier":1.5,"translation_override":""}
                """)).getOrThrow();
        AnglingModifierComponent component = new AnglingModifierComponent(List.of(node));
        assertEquals(1, component.compiled().size());

        AnglingModifierComponent decoded = AnglingModifierComponent.CODEC.parse(
                JsonOps.INSTANCE, AnglingModifierComponent.CODEC.encodeStart(JsonOps.INSTANCE, component).getOrThrow())
                .getOrThrow();
        assertEquals(component, decoded);
        assertThrows(UnsupportedOperationException.class, () -> decoded.compiled().clear());
        assertThrows(IllegalArgumentException.class, () -> new AnglingModifierComponent(
                Collections.nCopies(AnglingModifierComponent.MAX_MODIFIERS + 1, node)));
    }
}
