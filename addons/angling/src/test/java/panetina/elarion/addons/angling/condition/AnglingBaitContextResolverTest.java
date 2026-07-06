package panetina.elarion.addons.angling.condition;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import panetina.elarion.addons.angling.AnglingItems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AnglingBaitContextResolverTest {
    @Test
    void detectsPlaceholderBaitFromOffhandFirst() {
        assertEquals(
                AnglingItems.PLACEHOLDER_BAIT_ITEM_ID,
                AnglingBaitContextResolver.resolve(
                                AnglingItems.PLACEHOLDER_BAIT_ITEM_ID,
                                Identifier.of("minecraft", "fishing_rod"))
                        .orElseThrow());
    }

    @Test
    void detectsPlaceholderBaitFromMainHandFallback() {
        assertEquals(
                AnglingItems.PLACEHOLDER_BAIT_ITEM_ID,
                AnglingBaitContextResolver.resolve(
                                Identifier.of("minecraft", "air"),
                                AnglingItems.PLACEHOLDER_BAIT_ITEM_ID)
                        .orElseThrow());
    }

    @Test
    void ignoresUnrelatedItems() {
        assertTrue(AnglingBaitContextResolver.resolve(
                        Identifier.of("minecraft", "string"),
                        Identifier.of("minecraft", "fishing_rod"))
                .isEmpty());
    }
}
