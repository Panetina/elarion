package panetina.elarion.addons.government.client;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GovernmentUiIconAssetTest {
    private static final Path ICON_ROOT = Path.of(
            "addons/government/src/main/resources/assets/elarion_government/textures/gui/icons");

    @Test
    void allGovernmentIconPngsAreExactSixteenPixels() throws IOException {
        Path root = iconRoot();
        for (String iconId : allIconIds().toList()) {
            Path file = root.resolve(iconId + ".png");
            assertTrue(Files.exists(file), "Missing Government icon: " + file);
            BufferedImage image = ImageIO.read(file.toFile());
            assertNotNull(image, "Unreadable Government icon: " + file);
            assertEquals(16, image.getWidth(), iconId + " width");
            assertEquals(16, image.getHeight(), iconId + " height");
        }
    }

    @Test
    void everyVanillaRealmColorHasASwatchIcon() throws IOException {
        Path root = iconRoot();
        for (String colorId : GovernmentUiIcons.REALM_COLOR_IDS) {
            Path file = root.resolve(colorId + ".png");
            assertTrue(Files.exists(file), "Missing Realm color swatch: " + colorId);
            BufferedImage image = ImageIO.read(file.toFile());
            assertNotNull(image);
            assertEquals(16, image.getWidth());
            assertEquals(16, image.getHeight());
        }
    }

    @Test
    void iconResolverMapsKnownIdsAndAliasesToTextures() {
        assertEquals("textures/gui/icons/civic_crest.png",
                GovernmentUiIcons.texturePath("civic_crest").orElseThrow());
        assertEquals("textures/gui/icons/dark_green.png",
                GovernmentUiIcons.texturePath("dark green").orElseThrow());
        assertEquals("textures/gui/icons/law.png",
                GovernmentUiIcons.texturePath("published_record").orElseThrow());
        assertFalse(GovernmentUiIcons.texturePath("unknown_custom_thing").isPresent());
    }

    @Test
    void civicTabIconsResolveToDedicatedTextures() {
        Set<String> paths = new HashSet<>();
        for (String iconId : Set.of("current_votes", "proposal", "law", "project", "office", "history")) {
            String path = GovernmentUiIcons.texturePath(iconId).orElseThrow();
            assertTrue(path.startsWith(GovernmentUiIcons.ICON_PATH));
            assertTrue(paths.add(path), "Repeated tab icon texture path: " + iconId);
        }
    }

    @Test
    void semanticGovernmentIconsAreNotRepeatedPlaceholders() throws IOException {
        Set<Integer> hashes = new HashSet<>();
        for (String iconId : GovernmentUiIcons.BASE_ICON_IDS) {
            BufferedImage image = ImageIO.read(iconRoot().resolve(iconId + ".png").toFile());
            int hash = 1;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    hash = 31 * hash + image.getRGB(x, y);
                }
            }
            assertTrue(hashes.add(hash), "Repeated Government icon artwork: " + iconId);
        }
    }

    @Test
    void semanticGovernmentIconsKeepReadablePixelArtMass() throws IOException {
        Set<String> simpleStateIcons = Set.of("settled", "reject");
        for (String iconId : GovernmentUiIcons.BASE_ICON_IDS) {
            if (simpleStateIcons.contains(iconId)) continue;
            BufferedImage image = ImageIO.read(iconRoot().resolve(iconId + ".png").toFile());
            Set<Integer> colors = new HashSet<>();
            int opaquePixels = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    if (((argb >>> 24) & 0xFF) == 0) continue;
                    opaquePixels++;
                    colors.add(argb);
                }
            }
            assertTrue(opaquePixels >= 60, "Government icon too visually thin: " + iconId);
            assertTrue(colors.size() >= 4, "Government icon lacks pixel-art palette depth: " + iconId);
        }
    }

    @Test
    void officeCrownIconIsHorizontallySymmetric() throws IOException {
        BufferedImage image = ImageIO.read(iconRoot().resolve("office.png").toFile());
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth() / 2; x++) {
                int mirroredX = image.getWidth() - 1 - x;
                assertEquals(image.getRGB(x, y), image.getRGB(mirroredX, y),
                        "office crown should mirror at " + x + "," + y);
            }
        }
    }

    private static Stream<String> allIconIds() {
        return Stream.concat(GovernmentUiIcons.BASE_ICON_IDS.stream(), GovernmentUiIcons.REALM_COLOR_IDS.stream());
    }

    private static Path iconRoot() {
        if (Files.exists(ICON_ROOT)) return ICON_ROOT;
        return Path.of("src/main/resources/assets/elarion_government/textures/gui/icons");
    }
}
