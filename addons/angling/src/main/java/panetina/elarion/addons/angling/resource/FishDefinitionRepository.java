package panetina.elarion.addons.angling.resource;

import panetina.elarion.addons.angling.loader.FishDefinitionLoader;
import panetina.elarion.addons.angling.model.FishDefinitionIndex;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class FishDefinitionRepository {
    private final FishDefinitionLoader loader;
    private final AtomicReference<FishDefinitionIndex> current;

    public FishDefinitionRepository() {
        this(new FishDefinitionLoader());
    }

    FishDefinitionRepository(FishDefinitionLoader loader) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.current = new AtomicReference<>(new FishDefinitionIndex(List.of()));
    }

    public FishDefinitionIndex current() {
        return current.get();
    }

    public void publish(FishDefinitionIndex index) {
        current.set(Objects.requireNonNull(index, "index"));
    }

    public FishDefinitionIndex reload(Map<String, String> documents) {
        FishDefinitionIndex index = loader.load(documents);
        publish(index);
        return index;
    }
}
