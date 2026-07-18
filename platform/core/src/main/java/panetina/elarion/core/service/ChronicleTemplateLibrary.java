package panetina.elarion.core.service;

import panetina.elarion.core.model.ChronicleTemplateFamily;
import panetina.elarion.core.model.PublicHistoryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ChronicleTemplateLibrary {
    private final List<ChronicleTemplateFamily> families = new CopyOnWriteArrayList<>();

    public void register(ChronicleTemplateFamily family) {
        if (family == null) throw new IllegalArgumentException("Chronicle template family is required.");
        families.add(family);
    }

    public Optional<ChronicleTemplateFamily> find(PublicHistoryEntry entry) {
        if (entry == null) return Optional.empty();
        for (ChronicleTemplateFamily family : families) {
            if (family.supports(entry)) return Optional.of(family);
        }
        return Optional.empty();
    }

    public List<ChronicleTemplateFamily> families() {
        return List.copyOf(families);
    }

    public List<ChronicleTemplateFamily> libraryReadyFamilies() {
        List<ChronicleTemplateFamily> ready = new ArrayList<>();
        for (ChronicleTemplateFamily family : families) {
            if (family.isLibraryReady()) ready.add(family);
        }
        return List.copyOf(ready);
    }
}
