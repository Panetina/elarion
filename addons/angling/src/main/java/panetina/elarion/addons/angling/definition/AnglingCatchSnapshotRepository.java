package panetina.elarion.addons.angling.definition;

import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.compile.AnglingNativeDefinitionCompilers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Transactional reload boundary. Compilation and index construction happen
 * off to the side; one atomic swap publishes the complete valid snapshot.
 */
public final class AnglingCatchSnapshotRepository {
    private final AtomicReference<AnglingCatchSnapshot> current = new AtomicReference<>(AnglingCatchSnapshot.empty());
    private final AtomicLong revision = new AtomicLong();

    public AnglingCatchSnapshot current() {
        return current.get();
    }

    public AnglingCatchSnapshot compileAndPublish(Map<Identifier, AnglingCatchDefinition> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        var compilers = AnglingNativeDefinitionCompilers.create();
        List<Map.Entry<Identifier, AnglingCatchDefinition>> sorted = definitions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
                .toList();

        Map<Identifier, AnglingCatchSnapshot.NativeCatch> byId = new LinkedHashMap<>();
        Map<AnglingRarity, List<AnglingCatchSnapshot.NativeCatch>> byRarity = new EnumMap<>(AnglingRarity.class);
        Map<AnglingCatchType, List<AnglingCatchSnapshot.NativeCatch>> byType = new EnumMap<>(AnglingCatchType.class);
        for (var entry : sorted) {
            Identifier id = Objects.requireNonNull(entry.getKey(), "definition id");
            AnglingCatchDefinition source = Objects.requireNonNull(entry.getValue(), "definition");
            var compiled = new AnglingCatchSnapshot.NativeCatch(id, compilers.compile(id, source));
            if (byId.putIfAbsent(id, compiled) != null) {
                throw new IllegalArgumentException("Duplicate Angling catch definition: " + id);
            }
            byRarity.computeIfAbsent(compiled.rarity(), ignored -> new ArrayList<>()).add(compiled);
            byType.computeIfAbsent(compiled.type(), ignored -> new ArrayList<>()).add(compiled);
        }

        long nextRevision = revision.incrementAndGet();
        AnglingCatchSnapshot snapshot = new AnglingCatchSnapshot(nextRevision, byId, byRarity, byType);
        current.set(snapshot);
        return snapshot;
    }
}
