package panetina.elarion.addons.angling.condition;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class AnglingConditionContextTest {
    @Test
    void acceptsModdedTechnicalIdentifiersAndOptionalBait() {
        AnglingConditionContext context = context(6_000, true, false);

        assertEquals(Identifier.of("example", "placeholder_fluid"), context.fluidId());
        assertEquals(null, context.baitId());
    }

    @Test
    void rejectsInvalidTimeAndThunderWithoutRain() {
        assertThrows(IllegalArgumentException.class, () -> context(-1, false, false));
        assertThrows(IllegalArgumentException.class, () -> context(24_000, false, false));
        assertThrows(IllegalArgumentException.class, () -> context(1, false, true));
    }

    private static AnglingConditionContext context(
            long timeOfDay,
            boolean raining,
            boolean thundering
    ) {
        return new AnglingConditionContext(
                UUID.randomUUID(),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "overworld"),
                Identifier.of("minecraft", "river"),
                Identifier.of("example", "placeholder_fluid"),
                null,
                64,
                timeOfDay,
                raining,
                thundering);
    }
}
