package panetina.elarion.addons.mounts.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import org.jetbrains.annotations.Nullable;
import panetina.elarion.addons.mounts.entity.ElarionMountEntity;
import panetina.elarion.addons.mounts.entity.ElarionMountType;

public final class ElarionMountCamera {
    private static final float BOOST_RESPONSE = 0.10F;
    private static final float FOV_RESPONSE = 0.18F;
    private static float boostStrength;
    private static double mountedBaseFov = Double.NaN;
    private static double smoothedMountedFov = Double.NaN;
    private static @Nullable Perspective savedPreMountPerspective;
    private static boolean wasMounted;

    private ElarionMountCamera() {
    }

    public static void tick(MinecraftClient client, @Nullable ElarionMountEntity mount) {
        tick(client, mount, false);
    }

    public static void tick(MinecraftClient client, @Nullable ElarionMountEntity mount, boolean localBoosting) {
        if (mount == null) {
            restorePreMountPerspective(client);
            boostStrength += (0.0F - boostStrength) * BOOST_RESPONSE;
            wasMounted = false;
            return;
        }
        if (!wasMounted) {
            savedPreMountPerspective = client.options.getPerspective();
            wasMounted = true;
        }
        Perspective mountedPerspective = forcedMountedPerspective(client.options.getPerspective());
        if (client.options.getPerspective() != mountedPerspective) {
            client.options.setPerspective(mountedPerspective);
        }
        float target = localBoosting ? 1.0F : 0.0F;
        boostStrength += (target - boostStrength) * BOOST_RESPONSE;
    }

    public static Perspective mountedPerspective(Perspective perspective) {
        return currentMount() != null ? forcedMountedPerspective(perspective) : perspective;
    }

    public static Perspective forcedMountedPerspective(Perspective perspective) {
        return Perspective.THIRD_PERSON_BACK;
    }

    public static float mountedDistance(float vanillaDistance) {
        ElarionMountEntity mount = currentMount();
        if (mount == null) {
            return vanillaDistance;
        }
        ElarionMountType.CameraProfile profile = mount.mountType().cameraProfile();
        return Math.max(vanillaDistance, profile.thirdPersonDistance() + boostStrength * profile.boostDistanceBonus());
    }

    public static double mountedFov(double vanillaFov) {
        ElarionMountEntity mount = currentMount();
        if (mount == null) {
            mountedBaseFov = Double.NaN;
            smoothedMountedFov = Double.NaN;
            return vanillaFov;
        }
        if (Double.isNaN(mountedBaseFov)) {
            mountedBaseFov = vanillaFov;
            smoothedMountedFov = vanillaFov;
        }
        double target = mountedBaseFov * (1.0D + boostStrength * mount.mountType().cameraProfile().boostFovBonus());
        smoothedMountedFov += (target - smoothedMountedFov) * FOV_RESPONSE;
        return smoothedMountedFov;
    }

    private static @Nullable ElarionMountEntity currentMount() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.player.getVehicle() instanceof ElarionMountEntity mount) {
            return mount;
        }
        return null;
    }

    private static void restorePreMountPerspective(MinecraftClient client) {
        if (savedPreMountPerspective == null) {
            return;
        }
        Perspective restore = savedPreMountPerspective;
        savedPreMountPerspective = null;
        if (client.options.getPerspective() != restore) {
            client.options.setPerspective(restore);
        }
    }
}
