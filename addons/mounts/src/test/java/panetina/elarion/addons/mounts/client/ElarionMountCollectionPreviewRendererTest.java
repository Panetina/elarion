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
    void calibratedPreviewFramesRemainVisible() {
        int width = 102;
        int height = 98;

        for (ElarionMountType type : ElarionMountType.values()) {
            ElarionMountCollectionPreviewRenderer.PreviewFrame frame =
                    ElarionMountCollectionPreviewRenderer.previewFrame(type, width, height);

            assertTrue(frame.width() > 0, type.id() + " width " + frame);
            assertTrue(frame.height() > 0, type.id() + " height " + frame);
            assertTrue(frame.right() > 0, type.id() + " intersects left edge " + frame);
            assertTrue(frame.left() < width, type.id() + " intersects right edge " + frame);
            assertTrue(frame.bottom() > 0, type.id() + " intersects top edge " + frame);
            assertTrue(frame.top() < height, type.id() + " intersects bottom edge " + frame);
        }
    }

    @Test
    void previewOffsetsStayBoundedInsideCollectionFrame() {
        int height = 98;
        for (ElarionMountType type : ElarionMountType.values()) {
            int size = ElarionMountCollectionPreviewRenderer.previewSize(type, 102, height);
            int xOffset = ElarionMountCollectionPreviewRenderer.previewXOffset(type, size);
            int offset = ElarionMountCollectionPreviewRenderer.previewYOffset(type, height, size);

            assertTrue(xOffset > -180, type.id());
            assertTrue(xOffset < 180, type.id());
            assertTrue(offset > -150, type.id());
            assertTrue(offset < 150, type.id());
        }
    }

    @Test
    void largeConvertedModelsUseBoundsAwarePreviewScale() {
        int dragon = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.CHINESE_DRAGON, 102, 98);
        int bee = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.BEE, 102, 98);

        assertTrue(dragon < bee, "dragon should scale down from converted model bounds");
    }

    @Test
    void longDragonPreviewStaysCenteredWithoutTailClipping() {
        int size = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.CHINESE_DRAGON, 102, 98);
        int offset = ElarionMountCollectionPreviewRenderer.previewXOffset(ElarionMountType.CHINESE_DRAGON, size);

        assertTrue(offset > -80, "dragon preview should keep the tail visible");
        assertTrue(offset < 140, "dragon preview correction should remain bounded");
    }

    @Test
    void scifiBikePreviewUsesArtCalibrationToStayCentered() {
        int size = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.SCIFI_BIKE, 102, 98);
        int offset = ElarionMountCollectionPreviewRenderer.previewXOffset(ElarionMountType.SCIFI_BIKE, size);

        assertTrue(offset > -80, "scifi bike should keep the front visible");
        assertTrue(offset < 180, "scifi bike correction should remain bounded inside the preview frame");
    }

    @Test
    void wyvernPreviewGetsArtZoom() {
        int wyvern = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.WYVERN, 102, 98);
        int dragon = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.CHINESE_DRAGON, 102, 98);

        assertTrue(wyvern > dragon, "wyvern should stay visibly larger than the long dragon preview");
    }

    @Test
    void convertedLongModelsUseExplicitVisualCalibration() {
        int bikeSize = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.SCIFI_BIKE, 102, 98);
        int bikeOffset = ElarionMountCollectionPreviewRenderer.previewXOffset(ElarionMountType.SCIFI_BIKE, bikeSize);
        int wyvernSize = ElarionMountCollectionPreviewRenderer.previewSize(ElarionMountType.WYVERN, 102, 98);
        int wyvernXOffset = ElarionMountCollectionPreviewRenderer.previewXOffset(ElarionMountType.WYVERN, wyvernSize);
        ElarionMountCollectionPreviewRenderer.PreviewFrame dragonFrame =
                ElarionMountCollectionPreviewRenderer.previewFrame(ElarionMountType.CHINESE_DRAGON, 102, 98);
        ElarionMountCollectionPreviewRenderer.PreviewFrame wyvernFrame =
                ElarionMountCollectionPreviewRenderer.previewFrame(ElarionMountType.WYVERN, 102, 98);

        assertTrue(dragonFrame.bottom() > 0 && dragonFrame.top() < 98,
                "dragon needs visible vertical presence in the frame");
        assertTrue(bikeOffset > -80, "sci-fi bike needs bounded live-visual correction");
        assertTrue(wyvernXOffset > -100, "wyvern needs bounded live-visual correction");
        assertTrue(wyvernFrame.bottom() > 0 && wyvernFrame.top() < 98,
                "wyvern needs visible vertical presence in the frame");
    }
}
