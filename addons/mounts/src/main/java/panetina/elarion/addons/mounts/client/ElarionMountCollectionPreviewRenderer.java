package panetina.elarion.addons.mounts.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import panetina.elarion.addons.mounts.entity.ElarionMountEntities;
import panetina.elarion.addons.mounts.entity.ElarionMountEntity;
import panetina.elarion.addons.mounts.entity.ElarionMountType;
import panetina.elarion.addons.mounts.model.GeoModelDefinition;
import panetina.elarion.core.client.ElarionCollectionPreviewRegistry;
import panetina.elarion.core.client.ElarionMenuEntityPreviewRenderer;
import panetina.elarion.core.client.ui.ElarionScaledLayout;
import panetina.elarion.core.client.ui.ElarionUiStyle;
import panetina.elarion.core.model.ElarionCollectionEntry;

import java.util.EnumMap;
import java.util.Map;

public final class ElarionMountCollectionPreviewRenderer implements ElarionCollectionPreviewRegistry.PreviewRenderer {
    private static final float PREVIEW_YAW_DEGREES = -40.0F;
    private static final double PREVIEW_PADDING = 1.18D;
    private static final double PREVIEW_ANIMATION_SPEED = 0.6D;

    private final Map<ElarionMountType, ElarionMountEntity> previews = new EnumMap<>(ElarionMountType.class);
    private ClientWorld previewWorld;

    @Override
    public boolean render(
            DrawContext context,
            ElarionScaledLayout layout,
            String tabId,
            ElarionCollectionEntry entry,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY,
            float delta,
            ElarionUiStyle style
    ) {
        if (!"mounts".equals(tabId) || !entry.unlocked()) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return false;
        if (previewWorld != client.world) {
            previews.clear();
            previewWorld = client.world;
        }
        ElarionMountType type = ElarionMountType.byId(entry.id());
        if (GeoModelCache.forType(type) == null) return false;
        ElarionMountEntity mount = previews.computeIfAbsent(type, ignored -> {
            ElarionMountEntity entity = new ElarionMountEntity(ElarionMountEntities.MOUNT, client.world);
            entity.setMountType(type);
            entity.setPose(EntityPose.STANDING);
            entity.setNoGravity(true);
            entity.setSilent(true);
            entity.setAnimationTimeScale(PREVIEW_ANIMATION_SPEED);
            return entity;
        });
        if (mount.getWorld() != client.world) {
            previews.remove(type);
            return false;
        }
        mount.setMountType(type);
        mount.setAnimationTimeScale(PREVIEW_ANIMATION_SPEED);
        mount.age = (int) client.world.getTime();
        mount.setYaw(45.0F);
        mount.prevYaw = 45.0F;
        mount.bodyYaw = 45.0F;
        mount.prevBodyYaw = 45.0F;
        mount.headYaw = 45.0F;
        mount.prevHeadYaw = 45.0F;

        int centerX = x + width / 2;
        int bottomY = y + height - 2;
        int size = previewSize(type, width, height);
        int xOffset = previewXOffset(type, size);
        int yOffset = previewYOffset(type, height, size);
        ElarionMenuEntityPreviewRenderer.render(
                context,
                layout,
                mount,
                x,
                y,
                width,
                height,
                size,
                PREVIEW_YAW_DEGREES,
                0.0F,
                xOffset,
                yOffset);
        context.fill(centerX - width / 3, bottomY, centerX + width / 3, bottomY + 1, 0x55331F12);
        return true;
    }

    int cachedPreviewCount() {
        return previews.size();
    }

    static int previewSize(ElarionMountType type, int width, int height) {
        PreviewBounds bounds = previewBounds(type);
        if (bounds == null) {
            return fallbackPreviewSize(width, height);
        }
        double fitWidth = width / (Math.max(0.5D, bounds.width()) * PREVIEW_PADDING);
        double fitHeight = height / (Math.max(0.5D, bounds.height()) * PREVIEW_PADDING);
        int size = (int) Math.floor(Math.min(fitWidth, fitHeight) * previewArtScale(type));
        return Math.max(4, Math.min(Math.max(4, Math.min(width, height)), size));
    }

    static int previewXOffset(ElarionMountType type, int size) {
        PreviewBounds bounds = previewBounds(type);
        if (bounds == null) {
            return 0;
        }
        return (int) Math.round(-bounds.centerX() * size) + previewArtXOffset(type);
    }

    static int previewYOffset(ElarionMountType type, int height, int size) {
        PreviewBounds bounds = previewBounds(type);
        if (bounds == null) {
            return 0;
        }
        return (int) Math.round(-height * 0.5D + 6.0D + bounds.centerY() * size);
    }

    private static PreviewBounds previewBounds(ElarionMountType type) {
        GeoModelDefinition definition = GeoModelCache.forType(type);
        if (definition == null) {
            return null;
        }
        GeoModelDefinition.Bounds bounds = definition.bounds();
        GeoModelDefinition.Vec3 anchor = previewAnchor(type, definition);
        double renderScale = type.renderScale();
        double minY = (bounds.minY() - anchor.y()) / 16.0D * renderScale;
        double maxY = (bounds.maxY() - anchor.y()) / 16.0D * renderScale;
        double yawRadians = Math.toRadians(PREVIEW_YAW_DEGREES);
        double cos = Math.cos(yawRadians);
        double sin = Math.sin(yawRadians);
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double[] xs = {bounds.minX(), bounds.maxX()};
        double[] zs = {bounds.minZ(), bounds.maxZ()};
        for (double rawX : xs) {
            for (double rawZ : zs) {
                double localX = (rawX - anchor.x()) / 16.0D * renderScale;
                double localZ = (rawZ - anchor.z()) / 16.0D * renderScale;
                double projectedX = localX * cos + localZ * sin;
                minX = Math.min(minX, projectedX);
                maxX = Math.max(maxX, projectedX);
            }
        }
        return new PreviewBounds(minX, maxX, minY, maxY);
    }

    private static GeoModelDefinition.Vec3 previewAnchor(ElarionMountType type, GeoModelDefinition definition) {
        GeoModelDefinition.Vec3 anchor = definition.passengerAnchor();
        return new GeoModelDefinition.Vec3(
                type.renderAnchorX(anchor.x()),
                anchor.y(),
                type.renderAnchorZ(anchor.z()));
    }

    private static int fallbackPreviewSize(int width, int height) {
        return Math.max(4, Math.min(Math.max(4, Math.min(width, height)), Math.round(Math.min(width, height) * 0.7F)));
    }

    private static int previewArtXOffset(ElarionMountType type) {
        return switch (type) {
            case CHINESE_DRAGON -> 16;
            case SCIFI_BIKE -> 34;
            default -> 0;
        };
    }

    private static double previewArtScale(ElarionMountType type) {
        return switch (type) {
            case WYVERN -> 1.22D;
            default -> 1.0D;
        };
    }

    private record PreviewBounds(double minX, double maxX, double minY, double maxY) {
        double width() {
            return maxX - minX;
        }

        double height() {
            return maxY - minY;
        }

        double centerX() {
            return (minX + maxX) * 0.5D;
        }

        double centerY() {
            return (minY + maxY) * 0.5D;
        }
    }
}
