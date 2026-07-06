package panetina.elarion.addons.mounts.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import org.joml.Matrix4d;
import org.joml.Vector4d;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class GeoModelDefinition {
    private static final Identifier CHINESE_DRAGON_TEXTURE =
            Identifier.of("elarion_mounts", "textures/entity/flight_chinesedragon_body.png");

    private final Identifier texture;
    private final int textureWidth;
    private final int textureHeight;
    private final List<Bone> roots;
    private final Map<String, Animation> animations;
    private final Map<String, BoneDraft> drafts;
    private final Bounds bounds;
    private final Vec3 passengerAnchor;
    private final String passengerAnchorBone;

    private GeoModelDefinition(
            Identifier texture,
            int textureWidth,
            int textureHeight,
            List<Bone> roots,
            Map<String, Animation> animations,
            Map<String, BoneDraft> drafts,
            Bounds bounds,
            Vec3 passengerAnchor,
            String passengerAnchorBone
    ) {
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.roots = roots;
        this.animations = animations;
        this.drafts = drafts;
        this.bounds = bounds;
        this.passengerAnchor = passengerAnchor;
        this.passengerAnchorBone = passengerAnchorBone;
    }

    public static GeoModelDefinition parse(Reader geometryReader, Reader animationReader) {
        return parse(geometryReader, animationReader, CHINESE_DRAGON_TEXTURE);
    }

    public static GeoModelDefinition parse(Reader geometryReader, Reader animationReader, Identifier texture) {
        JsonObject root = JsonParser.parseReader(geometryReader).getAsJsonObject();
        JsonObject geometry = root.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        JsonObject description = object(geometry, "description");
        int textureWidth = integer(description, "texture_width", 128);
        int textureHeight = integer(description, "texture_height", 128);
        List<BoneDraft> drafts = parseBoneDrafts(array(geometry, "bones"));
        Map<String, BoneDraft> byName = new LinkedHashMap<>();
        for (BoneDraft draft : drafts) {
            byName.put(draft.name(), draft);
        }
        List<Bone> roots = new ArrayList<>();
        for (BoneDraft draft : drafts) {
            if (draft.utility()) {
                continue;
            }
            String parent = draft.parent();
            if (parent == null || parent.isBlank() || !byName.containsKey(parent) || byName.get(parent).utility()) {
                roots.add(buildBone(draft, drafts));
            }
        }
        Bounds bounds = Bounds.from(roots);
        BoneDraft passengerDraft = drafts.stream()
                .filter(draft -> draft.name().equals("p_passenger") || draft.name().toLowerCase(Locale.ROOT).contains("passenger"))
                .findFirst()
                .orElse(null);
        Vec3 passengerAnchor = passengerDraft == null
                ? new Vec3(bounds.centerX(), bounds.minY(), bounds.centerZ())
                : passengerDraft.pivot();
        String passengerAnchorBone = passengerDraft == null ? "" : passengerDraft.name();
        Map<String, Animation> animations = animationReader == null
                ? Map.of()
                : parseAnimations(JsonParser.parseReader(animationReader).getAsJsonObject());
        return new GeoModelDefinition(
                texture,
                textureWidth,
                textureHeight,
                List.copyOf(roots),
                animations,
                Map.copyOf(byName),
                bounds,
                passengerAnchor,
                passengerAnchorBone);
    }

    public Identifier texture() {
        return texture;
    }

    public int textureWidth() {
        return textureWidth;
    }

    public int textureHeight() {
        return textureHeight;
    }

    public List<Bone> roots() {
        return roots;
    }

    public Bounds bounds() {
        return bounds;
    }

    public Vec3 passengerAnchor() {
        return passengerAnchor;
    }

    public Vec3 animatedPassengerAnchor(String baseAnimation, String overlayAnimation, double seconds) {
        return animatedPassengerAnchor(baseAnimation, overlayAnimation, seconds, "");
    }

    public Vec3 animatedPassengerAnchor(
            String baseAnimation,
            String overlayAnimation,
            double seconds,
            String suppressedPositionBone
    ) {
        if (passengerAnchorBone == null || passengerAnchorBone.isBlank()) {
            return passengerAnchor;
        }
        BoneDraft target = drafts.get(passengerAnchorBone);
        if (target == null) {
            return passengerAnchor;
        }

        List<BoneDraft> path = new ArrayList<>();
        BoneDraft current = target;
        while (current != null) {
            path.add(0, current);
            String parent = current.parent();
            current = parent == null || parent.isBlank() ? null : drafts.get(parent);
        }

        Matrix4d transform = new Matrix4d().identity();
        for (int i = 0; i < path.size() - 1; i++) {
            applyBoneTransform(transform, path.get(i), baseAnimation, overlayAnimation, seconds, suppressedPositionBone);
        }

        Vec3 position = sample(baseAnimation, overlayAnimation, target.name(), "position", seconds);
        if (target.name().equals(suppressedPositionBone)) {
            position = Vec3.ZERO;
        }
        Vector4d marker = new Vector4d(
                target.pivot().x() + position.x(),
                target.pivot().y() + position.y(),
                target.pivot().z() + position.z(),
                1.0D);
        transform.transform(marker);
        return new Vec3(marker.x, marker.y, marker.z);
    }

    public Vec3 animatedPassengerRotation(String baseAnimation, String overlayAnimation, double seconds) {
        if (passengerAnchorBone == null || passengerAnchorBone.isBlank()) {
            return Vec3.ZERO;
        }
        BoneDraft target = drafts.get(passengerAnchorBone);
        if (target == null) {
            return Vec3.ZERO;
        }

        Vec3 rotation = Vec3.ZERO;
        BoneDraft current = target;
        List<BoneDraft> path = new ArrayList<>();
        while (current != null) {
            path.add(0, current);
            String parent = current.parent();
            current = parent == null || parent.isBlank() ? null : drafts.get(parent);
        }
        for (int i = 0; i < path.size() - 1; i++) {
            BoneDraft draft = path.get(i);
            rotation = rotation.add(draft.rotation())
                    .add(sample(baseAnimation, overlayAnimation, draft.name(), "rotation", seconds));
        }
        return rotation;
    }

    public Animation animation(String name) {
        return animations.get(name);
    }

    public boolean hasAnimation(String name) {
        return animations.containsKey(name);
    }

    public Set<String> animatedBoneNames(String... animationNames) {
        Set<String> names = new HashSet<>();
        for (String animationName : animationNames) {
            Animation animation = animation(animationName);
            if (animation != null) {
                names.addAll(animation.channels().keySet());
            }
        }
        return Set.copyOf(names);
    }

    public double animationLength(String name) {
        Animation animation = animation(name);
        return animation == null ? 0.0D : animation.length();
    }

    public Vec3 samplePosition(String animationName, String bone, double seconds) {
        return sample(animation(animationName), bone, "position", seconds);
    }

    public Vec3 sampleRotation(String animationName, String bone, double seconds) {
        return sample(animation(animationName), bone, "rotation", seconds);
    }

    public Vec3 sampleScale(String animationName, String bone, double seconds) {
        return sampleScale(animation(animationName), bone, seconds);
    }

    private void applyBoneTransform(
            Matrix4d matrix,
            BoneDraft draft,
            String baseAnimation,
            String overlayAnimation,
            double seconds
    ) {
        applyBoneTransform(matrix, draft, baseAnimation, overlayAnimation, seconds, "");
    }

    private void applyBoneTransform(
            Matrix4d matrix,
            BoneDraft draft,
            String baseAnimation,
            String overlayAnimation,
            double seconds,
            String suppressedPositionBone
    ) {
        Vec3 pivot = draft.pivot();
        Vec3 position = sample(baseAnimation, overlayAnimation, draft.name(), "position", seconds);
        if (draft.name().equals(suppressedPositionBone)) {
            position = Vec3.ZERO;
        }
        Vec3 rotation = draft.rotation()
                .add(sample(baseAnimation, overlayAnimation, draft.name(), "rotation", seconds));
        Vec3 scale = sampleScale(baseAnimation, overlayAnimation, draft.name(), seconds);

        matrix.translate(position.x(), position.y(), position.z());
        matrix.translate(pivot.x(), pivot.y(), pivot.z());
        if (rotation.z() != 0.0D) {
            matrix.rotateZ(Math.toRadians(rotation.z()));
        }
        if (rotation.y() != 0.0D) {
            matrix.rotateY(Math.toRadians(rotation.y()));
        }
        if (rotation.x() != 0.0D) {
            matrix.rotateX(Math.toRadians(rotation.x()));
        }
        matrix.scale(
                Math.max(0.01D, scale.x()),
                Math.max(0.01D, scale.y()),
                Math.max(0.01D, scale.z()));
        matrix.translate(-pivot.x(), -pivot.y(), -pivot.z());
    }

    private Vec3 sample(String baseAnimation, String overlayAnimation, String bone, String channel, double seconds) {
        Vec3 base = sample(animation(baseAnimation), bone, channel, seconds);
        if (overlayAnimation == null || overlayAnimation.equals("none")) {
            return base;
        }
        Vec3 value = base;
        for (String overlay : overlayAnimations(overlayAnimation)) {
            value = value.add(sample(animation(overlay), bone, channel, seconds));
        }
        return value;
    }

    private Vec3 sampleScale(String baseAnimation, String overlayAnimation, String bone, double seconds) {
        Vec3 base = sampleScale(animation(baseAnimation), bone, seconds);
        if (overlayAnimation == null || overlayAnimation.equals("none")) {
            return base;
        }
        Vec3 value = base;
        for (String overlay : overlayAnimations(overlayAnimation)) {
            value = value.multiply(sampleScale(animation(overlay), bone, seconds));
        }
        return value;
    }

    private static List<String> overlayAnimations(String overlayAnimation) {
        List<String> overlays = new ArrayList<>();
        for (String part : overlayAnimation.split("\\+")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty() && !trimmed.equals("none")) {
                overlays.add(trimmed);
            }
        }
        return overlays;
    }

    private Vec3 sample(Animation animation, String bone, String channel, double seconds) {
        return animation == null ? Vec3.ZERO : animation.sample(bone, channel, seconds);
    }

    private Vec3 sampleScale(Animation animation, String bone, double seconds) {
        if (animation == null) {
            return Vec3.ONE;
        }
        Vec3 sampled = animation.sampleOrNull(bone, "scale", seconds);
        return sampled == null ? Vec3.ONE : sampled;
    }

    private static List<BoneDraft> parseBoneDrafts(JsonArray array) {
        List<BoneDraft> drafts = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject json = element.getAsJsonObject();
            String name = string(json, "name", "");
            List<Cube> cubes = new ArrayList<>();
            for (JsonElement cubeElement : array(json, "cubes")) {
                JsonObject cubeJson = cubeElement.getAsJsonObject();
                Map<String, Face> faces = new HashMap<>();
                JsonObject uv = object(cubeJson, "uv");
                for (String direction : uv.keySet()) {
                    JsonObject face = uv.getAsJsonObject(direction);
                    faces.put(direction, new Face(direction, vec2(array(face, "uv")), vec2(array(face, "uv_size"))));
                }
                cubes.add(new Cube(
                        vec3(array(cubeJson, "origin")),
                        vec3(array(cubeJson, "size")),
                        vec3(array(cubeJson, "pivot")),
                        vec3(array(cubeJson, "rotation")),
                        faces));
            }
            drafts.add(new BoneDraft(
                    name,
                    string(json, "parent", ""),
                    vec3(array(json, "pivot")),
                    vec3(array(json, "rotation")),
                    cubes,
                    utilityBone(name)));
        }
        return drafts;
    }

    private static Bone buildBone(BoneDraft draft, List<BoneDraft> drafts) {
        List<Bone> children = new ArrayList<>();
        for (BoneDraft child : drafts) {
            if (!child.utility() && draft.name().equals(child.parent())) {
                children.add(buildBone(child, drafts));
            }
        }
        return new Bone(
                draft.name(),
                draft.pivot(),
                draft.rotation(),
                List.copyOf(draft.cubes()),
                List.copyOf(children));
    }

    private static Map<String, Animation> parseAnimations(JsonObject root) {
        Map<String, Animation> animations = new HashMap<>();
        JsonObject animationRoot = object(root, "animations");
        for (String name : animationRoot.keySet()) {
            JsonObject json = animationRoot.getAsJsonObject(name);
            Map<String, Map<String, List<Keyframe>>> channels = new HashMap<>();
            JsonObject bones = object(json, "bones");
            for (String bone : bones.keySet()) {
                JsonObject boneJson = bones.getAsJsonObject(bone);
                for (String channel : List.of("position", "rotation", "scale")) {
                    JsonElement channelElement = boneJson.get(channel);
                    if (channelElement == null || channelElement.isJsonNull()) {
                        continue;
                    }
                    List<Keyframe> frames = parseChannel(channelElement);
                    if (!frames.isEmpty()) {
                        channels.computeIfAbsent(bone, ignored -> new HashMap<>()).put(channel, frames);
                    }
                }
            }
            String normalizedName = name.replace("animation.", "");
            animations.put(normalizedName, new Animation(
                    normalizedName,
                    number(json, "animation_length", 0.0D),
                    bool(json, "loop", defaultLoop(normalizedName)),
                    channels));
        }
        return animations;
    }

    private static boolean defaultLoop(String animationName) {
        return animationName.equals("idle")
                || animationName.equals("walk")
                || animationName.equals("glide")
                || animationName.equals("lean_left")
                || animationName.equals("lean_right")
                || animationName.equals("ascend")
                || animationName.equals("descend");
    }

    private static List<Keyframe> parseChannel(JsonElement element) {
        List<Keyframe> frames = new ArrayList<>();
        if (element.isJsonArray()) {
            frames.add(new Keyframe(0.0D, vec3(element.getAsJsonArray()), "linear"));
            return frames;
        }
        JsonObject object = element.getAsJsonObject();
        JsonElement vector = object.get("vector");
        if (vector != null && vector.isJsonArray()) {
            frames.add(new Keyframe(0.0D, vec3(vector.getAsJsonArray()), "linear"));
            return frames;
        }
        for (String key : object.keySet()) {
            JsonElement keyframeElement = object.get(key);
            Vec3 value = Vec3.ZERO;
            String interpolation = "linear";
            if (keyframeElement.isJsonObject()) {
                JsonObject keyframe = keyframeElement.getAsJsonObject();
                interpolation = string(keyframe, "lerp_mode", "linear").toLowerCase(Locale.ROOT);
                JsonObject post = object(keyframe, "post");
                JsonObject pre = object(keyframe, "pre");
                if (post.has("vector")) {
                    value = vec3(post.getAsJsonArray("vector"));
                } else if (pre.has("vector")) {
                    value = vec3(pre.getAsJsonArray("vector"));
                } else if (keyframe.has("vector")) {
                    value = vec3(keyframe.getAsJsonArray("vector"));
                }
            } else if (keyframeElement.isJsonArray()) {
                value = vec3(keyframeElement.getAsJsonArray());
            }
            frames.add(new Keyframe(parseNumber(key), value, interpolation));
        }
        frames.sort(Comparator.comparingDouble(Keyframe::time));
        return frames;
    }

    private static boolean utilityBone(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        return normalized.equals("hitbox")
                || normalized.equals("shadow")
                || normalized.equals("g_altitude")
                || normalized.startsWith("p_")
                || normalized.contains("passenger");
    }

    private static JsonArray array(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonArray() ? value.getAsJsonArray() : new JsonArray();
    }

    private static JsonObject object(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : new JsonObject();
    }

    private static String string(JsonObject object, String key, String fallback) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? fallback : value.getAsString();
    }

    private static boolean bool(JsonObject object, String key, boolean fallback) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? fallback : value.getAsBoolean();
    }

    private static int integer(JsonObject object, String key, int fallback) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? fallback : value.getAsInt();
    }

    private static double number(JsonObject object, String key, double fallback) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? fallback : parseNumber(value.getAsString());
    }

    private static Vec2 vec2(JsonArray array) {
        return new Vec2(value(array, 0), value(array, 1));
    }

    private static Vec3 vec3(JsonArray array) {
        return new Vec3(value(array, 0), value(array, 1), value(array, 2));
    }

    private static double value(JsonArray array, int index) {
        return array.size() > index ? parseNumber(array.get(index).getAsString()) : 0.0D;
    }

    private static double parseNumber(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return 0.0D;
        }
    }

    private record BoneDraft(
            String name,
            String parent,
            Vec3 pivot,
            Vec3 rotation,
            List<Cube> cubes,
            boolean utility
    ) {
    }

    public record Vec2(double x, double y) {
    }

    public record Vec3(double x, double y, double z) {
        public static final Vec3 ZERO = new Vec3(0.0D, 0.0D, 0.0D);
        public static final Vec3 ONE = new Vec3(1.0D, 1.0D, 1.0D);

        public Vec3 add(Vec3 other) {
            return new Vec3(x + other.x, y + other.y, z + other.z);
        }

        public Vec3 subtract(Vec3 other) {
            return new Vec3(x - other.x, y - other.y, z - other.z);
        }

        public Vec3 multiply(double scalar) {
            return new Vec3(x * scalar, y * scalar, z * scalar);
        }

        public Vec3 multiply(Vec3 other) {
            return new Vec3(x * other.x, y * other.y, z * other.z);
        }
    }

    public record Face(String direction, Vec2 uv, Vec2 uvSize) {
    }

    public record Cube(Vec3 origin, Vec3 size, Vec3 pivot, Vec3 rotation, Map<String, Face> faces) {
    }

    public record Bone(String name, Vec3 pivot, Vec3 rotation, List<Cube> cubes, List<Bone> children) {
    }

    public record Keyframe(double time, Vec3 value, String interpolation) {
    }

    public record Animation(
            String name,
            double length,
            boolean loop,
            Map<String, Map<String, List<Keyframe>>> channels
    ) {
        public Vec3 sample(String bone, String channel, double seconds) {
            Vec3 sampled = sampleOrNull(bone, channel, seconds);
            return sampled == null ? Vec3.ZERO : sampled;
        }

        public Vec3 sampleOrNull(String bone, String channel, double seconds) {
            Map<String, List<Keyframe>> byChannel = channels.get(bone);
            if (byChannel == null) {
                return null;
            }
            List<Keyframe> frames = byChannel.get(channel);
            if (frames == null || frames.isEmpty()) {
                return null;
            }
            if (frames.size() == 1 || length <= 0.0D) {
                return frames.get(frames.size() - 1).value();
            }
            double t = loop ? seconds % length : Math.min(seconds, length);
            int previousIndex = 0;
            int nextIndex = frames.size() - 1;
            for (int index = 0; index < frames.size(); index++) {
                Keyframe frame = frames.get(index);
                if (frame.time() <= t) {
                    previousIndex = index;
                }
                if (frame.time() >= t) {
                    nextIndex = index;
                    break;
                }
            }
            Keyframe previous = frames.get(previousIndex);
            Keyframe next = frames.get(nextIndex);
            if (previous == next || next.time() <= previous.time()) {
                return previous.value();
            }
            double progress = (t - previous.time()) / (next.time() - previous.time());
            if (next.interpolation().equals("catmullrom") || previous.interpolation().equals("catmullrom")) {
                Vec3 p0 = frames.get(Math.max(0, previousIndex - 1)).value();
                Vec3 p1 = previous.value();
                Vec3 p2 = next.value();
                Vec3 p3 = frames.get(Math.min(frames.size() - 1, nextIndex + 1)).value();
                return new Vec3(
                        catmullRom(p0.x, p1.x, p2.x, p3.x, progress),
                        catmullRom(p0.y, p1.y, p2.y, p3.y, progress),
                        catmullRom(p0.z, p1.z, p2.z, p3.z, progress));
            }
            return new Vec3(
                    lerp(previous.value().x, next.value().x, progress),
                    lerp(previous.value().y, next.value().y, progress),
                    lerp(previous.value().z, next.value().z, progress));
        }

        private double catmullRom(double p0, double p1, double p2, double p3, double progress) {
            double t2 = progress * progress;
            double t3 = t2 * progress;
            return 0.5D * ((2.0D * p1)
                    + (-p0 + p2) * progress
                    + (2.0D * p0 - 5.0D * p1 + 4.0D * p2 - p3) * t2
                    + (-p0 + 3.0D * p1 - 3.0D * p2 + p3) * t3);
        }

        private double lerp(double a, double b, double progress) {
            return a + (b - a) * progress;
        }
    }

    public record Bounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public static Bounds from(List<Bone> roots) {
            MutableBounds bounds = new MutableBounds();
            for (Bone root : roots) {
                collect(root, bounds);
            }
            return bounds.toBounds();
        }

        private static void collect(Bone bone, MutableBounds bounds) {
            for (Cube cube : bone.cubes()) {
                bounds.include(cube.origin());
                bounds.include(cube.origin().add(cube.size()));
            }
            for (Bone child : bone.children()) {
                collect(child, bounds);
            }
        }

        public double centerX() {
            return (minX + maxX) * 0.5D;
        }

        public double centerZ() {
            return (minZ + maxZ) * 0.5D;
        }
    }

    private static final class MutableBounds {
        private double minX = Double.POSITIVE_INFINITY;
        private double minY = Double.POSITIVE_INFINITY;
        private double minZ = Double.POSITIVE_INFINITY;
        private double maxX = Double.NEGATIVE_INFINITY;
        private double maxY = Double.NEGATIVE_INFINITY;
        private double maxZ = Double.NEGATIVE_INFINITY;

        void include(Vec3 vec) {
            minX = Math.min(minX, vec.x());
            minY = Math.min(minY, vec.y());
            minZ = Math.min(minZ, vec.z());
            maxX = Math.max(maxX, vec.x());
            maxY = Math.max(maxY, vec.y());
            maxZ = Math.max(maxZ, vec.z());
        }

        Bounds toBounds() {
            if (!Double.isFinite(minX)) {
                return new Bounds(-8, 0, -8, 8, 16, 8);
            }
            return new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}
