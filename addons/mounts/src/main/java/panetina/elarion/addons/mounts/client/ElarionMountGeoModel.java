package panetina.elarion.addons.mounts.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.mounts.entity.ElarionMountEntity;
import panetina.elarion.addons.mounts.entity.ElarionMountType;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

import java.util.List;

public final class ElarionMountGeoModel extends GeoModel<ElarionMountEntity> {
    private final ElarionMountPoseBlender poseBlender = new ElarionMountPoseBlender();

    private static final List<String> UTILITY_BONES = List.of(
            "shadow",
            "hitbox",
            "g_altitude",
            "p_passenger");

    @Override
    public Identifier getModelResource(ElarionMountEntity animatable) {
        ElarionMountType type = animatable.mountType();
        return Identifier.of("elarion_mounts", "geo/" + type.geoFileName());
    }

    @Override
    public Identifier getTextureResource(ElarionMountEntity animatable) {
        ElarionMountType type = animatable.mountType();
        return Identifier.of("elarion_mounts", "textures/entity/" + type.textureFileName());
    }

    @Override
    public Identifier getAnimationResource(ElarionMountEntity animatable) {
        ElarionMountType type = animatable.mountType();
        return Identifier.of("elarion_mounts", "animations/" + type.animationFileName());
    }

    @Override
    public void setCustomAnimations(
            ElarionMountEntity animatable,
            long instanceId,
            AnimationState<ElarionMountEntity> animationState
    ) {
        for (String boneName : UTILITY_BONES) {
            getBone(boneName).ifPresent(bone -> bone.setHidden(true));
        }
        boolean firstPersonRider = isFirstPersonRider(animatable);
        for (String boneName : animatable.mountType().riderSeatProfile().firstPersonHiddenBones()) {
            getBone(boneName).ifPresent(bone -> bone.setHidden(firstPersonRider));
        }
        poseBlender.apply(this, animatable, animationState);
    }

    private boolean isFirstPersonRider(ElarionMountEntity animatable) {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null
                && client.player.getVehicle() == animatable
                && client.options.getPerspective() == Perspective.FIRST_PERSON;
    }
}
