package panetina.elarion.addons.mounts.model;

import org.junit.jupiter.api.Test;
import panetina.elarion.addons.mounts.entity.ElarionMountType;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoModelDefinitionTest {
    @Test
    void allConvertedMountModelsLoadPassengerAnchorsTexturesAndAnimations() {
        for (ElarionMountType type : ElarionMountType.values()) {
            GeoModelDefinition model = GeoMountModels.forType(type);

            assertNotNull(model, type.id());
            assertEquals("elarion_mounts:textures/entity/" + type.textureFileName(), model.texture().toString());
            assertFalse(model.roots().isEmpty(), type.id());
            assertTrue(model.bounds().maxX() > model.bounds().minX(), type.id());
            assertTrue(model.bounds().maxY() > model.bounds().minY(), type.id());
            assertTrue(model.bounds().maxZ() > model.bounds().minZ(), type.id());
            assertTrue(Double.isFinite(model.passengerAnchor().x()), type.id());
            assertTrue(Double.isFinite(model.passengerAnchor().y()), type.id());
            assertTrue(Double.isFinite(model.passengerAnchor().z()), type.id());
            assertRequiredAnimations(type, model);
        }
    }

    @Test
    void runtimeGeoDoesNotShipExactZeroThicknessVisualCubes() {
        for (ElarionMountType type : ElarionMountType.values()) {
            GeoModelDefinition model = GeoMountModels.forType(type);

            for (GeoModelDefinition.Bone root : model.roots()) {
                assertNoZeroThicknessVisualCube(type, root);
            }
        }
    }

    @Test
    void chineseDragonGeoLoadsPassengerAnchorGeometryAndAnimations() throws Exception {
        GeoModelDefinition model = loadChineseDragon();

        assertEquals(128, model.textureWidth());
        assertEquals(128, model.textureHeight());
        assertEquals("elarion_mounts:textures/entity/flight_chinesedragon_body.png", model.texture().toString());
        assertEquals(0.0D, model.passengerAnchor().x(), 0.001D);
        assertEquals(14.75D, model.passengerAnchor().y(), 0.001D);
        assertEquals(-0.5D, model.passengerAnchor().z(), 0.001D);
        assertFalse(model.roots().isEmpty());
        assertTrue(model.bounds().maxZ() - model.bounds().minZ() > 100.0D);
        assertTrue(model.hasAnimation("spawn"));
        assertTrue(model.hasAnimation("idle"));
        assertTrue(model.hasAnimation("walk"));
        assertTrue(model.hasAnimation("ascend"));
        assertTrue(model.hasAnimation("descend"));
        assertTrue(model.hasAnimation("lean_left"));
        assertTrue(model.hasAnimation("lean_right"));

        GeoModelDefinition.Vec3 animatedSeat = model.animatedPassengerAnchor("idle", "none", 0.5D);
        assertTrue(Double.isFinite(animatedSeat.x()));
        assertTrue(Double.isFinite(animatedSeat.y()));
        assertTrue(Double.isFinite(animatedSeat.z()));
        assertTrue(model.animation("idle").sample("hi_chest", "position", 0.5D).y() > 0.0D);
        assertTrue(model.animation("spawn").sample("hi_chest", "position", 0.5D).y() > 0.0D);
    }

    @Test
    void chineseDragonGeoDoesNotExposeUtilityBonesAsRoots() throws Exception {
        GeoModelDefinition model = loadChineseDragon();
        for (GeoModelDefinition.Bone root : model.roots()) {
            assertNoUtilityBone(root);
        }
    }

    @Test
    void passengerAnchorSupportsCombinedControlOverlays() throws Exception {
        GeoModelDefinition model = loadChineseDragon();

        GeoModelDefinition.Vec3 combined = model.animatedPassengerAnchor("walk", "ascend+lean_left", 0.5D);
        GeoModelDefinition.Vec3 verticalOnly = model.animatedPassengerAnchor("walk", "ascend", 0.5D);
        GeoModelDefinition.Vec3 leanOnly = model.animatedPassengerAnchor("walk", "lean_left", 0.5D);

        assertTrue(Double.isFinite(combined.x()));
        assertTrue(Double.isFinite(combined.y()));
        assertTrue(Double.isFinite(combined.z()));
        assertTrue(Math.abs(combined.x() - verticalOnly.x()) > 0.001D
                || Math.abs(combined.y() - verticalOnly.y()) > 0.001D
                || Math.abs(combined.z() - verticalOnly.z()) > 0.001D);
        assertTrue(Math.abs(combined.x() - leanOnly.x()) > 0.001D
                || Math.abs(combined.y() - leanOnly.y()) > 0.001D
                || Math.abs(combined.z() - leanOnly.z()) > 0.001D);
    }

    @Test
    void convertedAnimationsExposeBlendableBoneChannels() {
        for (ElarionMountType type : ElarionMountType.values()) {
            GeoModelDefinition model = GeoMountModels.forType(type);

            assertFalse(model.animatedBoneNames("idle", "walk").isEmpty(), type.id());
            assertFalse(model.animatedBoneNames("ascend", "descend", "lean_left", "lean_right").isEmpty(), type.id());
            assertTrue(model.animationLength("idle") > 0.0D, type.id());
            assertTrue(model.animationLength("walk") > 0.0D, type.id());
            assertTrue(Double.isFinite(model.sampleRotation("idle", model.animatedBoneNames("idle").iterator().next(), 0.25D).x()), type.id());
        }
    }

    @Test
    void passengerRotationFollowsAnimatedParentBones() throws Exception {
        GeoModelDefinition model = loadChineseDragon();

        GeoModelDefinition.Vec3 idle = model.animatedPassengerRotation("idle", "none", 0.5D);
        GeoModelDefinition.Vec3 leaning = model.animatedPassengerRotation("idle", "lean_left", 0.5D);

        assertTrue(Double.isFinite(idle.x()));
        assertTrue(Double.isFinite(idle.y()));
        assertTrue(Double.isFinite(idle.z()));
        assertTrue(Math.abs(leaning.x() - idle.x()) > 0.001D
                || Math.abs(leaning.y() - idle.y()) > 0.001D
                || Math.abs(leaning.z() - idle.z()) > 0.001D);
    }

    private GeoModelDefinition loadChineseDragon() throws Exception {
        try (var geometry = getClass().getResourceAsStream("/assets/elarion_mounts/geo/mount_chinesedragon.geo.json");
             var animation = getClass().getResourceAsStream("/assets/elarion_mounts/animations/mount_chinesedragon.animation.json")) {
            assertNotNull(geometry);
            assertNotNull(animation);
            return GeoModelDefinition.parse(
                    new InputStreamReader(geometry, StandardCharsets.UTF_8),
                    new InputStreamReader(animation, StandardCharsets.UTF_8));
        }
    }

    private void assertNoUtilityBone(GeoModelDefinition.Bone bone) {
        String name = bone.name().toLowerCase();
        assertFalse(name.equals("hitbox"));
        assertFalse(name.equals("shadow"));
        assertFalse(name.equals("g_altitude"));
        assertFalse(name.startsWith("p_"));
        assertFalse(name.contains("passenger"));
        for (GeoModelDefinition.Bone child : bone.children()) {
            assertNoUtilityBone(child);
        }
    }

    private void assertNoZeroThicknessVisualCube(ElarionMountType type, GeoModelDefinition.Bone bone) {
        for (GeoModelDefinition.Cube cube : bone.cubes()) {
            assertTrue(Math.abs(cube.size().x()) > 0.0001D, type.id() + " " + bone.name());
            assertTrue(Math.abs(cube.size().y()) > 0.0001D, type.id() + " " + bone.name());
            assertTrue(Math.abs(cube.size().z()) > 0.0001D, type.id() + " " + bone.name());
        }
        for (GeoModelDefinition.Bone child : bone.children()) {
            assertNoZeroThicknessVisualCube(type, child);
        }
    }

    private void assertRequiredAnimations(ElarionMountType type, GeoModelDefinition model) {
        for (String animation : new String[]{"spawn", "idle", "walk", "ascend", "descend", "lean_left", "lean_right"}) {
            assertTrue(model.hasAnimation(animation), type.id() + " missing " + animation);
        }
    }
}
