package panetina.elarion.addons.mounts.client;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.mounts.entity.ElarionMountType;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ElarionMountCollectionPreviewRendererTest {
    @Test
    void previewSizesStayBoundedInsideCollectionFrame() {
        int width = 102;
        int height = 98;

        for (ElarionMountType type : ElarionMountType.values()) {
            int size = ElarionMountCollectionPreviewRenderer.previewSize(type, width, height);

            assertTrue(size > 0, type.id());
            assertTrue(size <= Math.min(width, height), type.id());
        }
    }

    @Test
    void previewOffsetsStayBoundedInsideCollectionFrame() {
        int height = 98;
        for (ElarionMountType type : ElarionMountType.values()) {
            int size = ElarionMountCollectionPreviewRenderer.previewSize(type, 102, height);
            int xOffset = ElarionMountCollectionPreviewRenderer.previewXOffset(type, size);
            int offset = ElarionMountCollectionPreviewRenderer.previewYOffset(type, height, size);

            assertTrue(xOffset > -102, type.id());
            assertTrue(xOffset < 102, type.id());
            assertTrue(offset > -height, type.id());
            assertTrue(offset < height, type.id());
        }
    }

    @Test
    void largeConvertedModelsUseBoundsAwarePreviewScale() {
        int dragon = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.CHINESE_DRAGON, 102, 98);
        int bee = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.BEE, 102, 98);

        assertTrue(dragon < bee, "dragon should scale down from converted model bounds");
    }

    @Test
    void longDragonPreviewIsShiftedBackTowardFrameCenter() {
        int size = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.CHINESE_DRAGON, 102, 98);
        int offset = ElarionMountCollectionPreviewRenderer.previewXOffset(ElarionMountType.CHINESE_DRAGON, size);

        assertTrue(offset > 30, "dragon tail-heavy preview should shift right instead of clipping left");
    }

    @Test
    void scifiBikePreviewUsesArtCalibrationToAvoidLeftClip() {
        int size = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.SCIFI_BIKE, 102, 98);
        int offset = ElarionMountCollectionPreviewRenderer.previewXOffset(ElarionMountType.SCIFI_BIKE, size);

        assertTrue(offset > 20, "scifi bike preview needs explicit right shift for its rendered mesh");
    }

    @Test
    void wyvernPreviewGetsArtZoom() {
        int wyvern = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.WYVERN, 102, 98);
        int dragon = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.CHINESE_DRAGON, 102, 98);

        assertTrue(wyvern > dragon, "wyvern should stay visibly larger than the long dragon preview");
    }
}
