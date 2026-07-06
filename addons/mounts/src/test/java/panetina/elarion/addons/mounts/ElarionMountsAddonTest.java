package panetina.elarion.addons.mounts;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.mounts.entity.ElarionMountType;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionMountsAddonTest {
    @Test
    void collectionIconsUseWhistlePlaceholdersUntilFinalMountPortraitsExist() {
        for (ElarionMountType type : ElarionMountType.values()) {
            String icon = ElarionMountsAddon.collectionIcon(type);

            assertTrue(icon.startsWith("elarion_mounts:textures/item/"), type.id());
            assertTrue(icon.endsWith(type.itemId() + ".png"), type.id());
            assertTrue(icon.contains("_whistle"), type.id());
        }
    }
}
