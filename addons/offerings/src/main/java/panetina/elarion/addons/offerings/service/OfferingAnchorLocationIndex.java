package panetina.elarion.addons.offerings.service;

import net.minecraft.util.math.BlockPos;
import panetina.elarion.addons.offerings.model.OfferingAnchor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

final class OfferingAnchorLocationIndex {
    private final Map<Location, LinkedHashMap<String, OfferingAnchor>> anchors = new LinkedHashMap<>();
    private int size;

    void rebuild(Collection<OfferingAnchor> values) {
        anchors.clear();
        size = 0;
        if (values != null) values.forEach(this::add);
    }

    void add(OfferingAnchor anchor) {
        if (anchor == null) return;
        OfferingAnchor previous = anchors.computeIfAbsent(Location.of(anchor), ignored -> new LinkedHashMap<>())
                .putIfAbsent(anchor.id(), anchor);
        if (previous == null) size++;
    }

    void remove(OfferingAnchor anchor) {
        if (anchor == null) return;
        Location location = Location.of(anchor);
        LinkedHashMap<String, OfferingAnchor> bucket = anchors.get(location);
        if (bucket == null || bucket.remove(anchor.id()) == null) return;
        size--;
        if (bucket.isEmpty()) anchors.remove(location);
    }

    Optional<OfferingAnchor> find(String worldId, BlockPos pos) {
        if (pos == null) return Optional.empty();
        LinkedHashMap<String, OfferingAnchor> bucket =
                anchors.get(new Location(clean(worldId), pos.getX(), pos.getY(), pos.getZ()));
        return bucket == null ? Optional.empty() : bucket.values().stream().findFirst();
    }

    int size() {
        return size;
    }

    private record Location(String worldId, int x, int y, int z) {
        static Location of(OfferingAnchor anchor) {
            return new Location(clean(anchor.worldId()), anchor.x(), anchor.y(), anchor.z());
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }
}
