package panetina.elarion.core.service;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.model.ElarionCollectionAction;
import panetina.elarion.core.model.ElarionCollectionEntry;
import panetina.elarion.core.model.ElarionCollectionSnapshot;
import panetina.elarion.core.model.ElarionCollectionTab;
import panetina.elarion.core.network.CollectionOpenPayload;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ElarionCollectionService {
    private static final Map<String, Integer> PINNED_TAB_ORDER = Map.of(
            "mounts", 0,
            "pets", 1,
            "titles", 2);
    private static final ElarionCollectionTab PETS_TAB = new ElarionCollectionTab(
            "pets",
            "Pets",
            "Pets will live here later.",
            List.of());

    private final Map<String, TabProvider> providers = new LinkedHashMap<>();

    public void registerTab(TabProvider provider) {
        if (provider == null || provider.id().isBlank()) {
            throw new IllegalArgumentException("Collection tab provider must have an id");
        }
        providers.put(provider.id(), provider);
    }

    public ElarionCollectionSnapshot snapshot(ServerPlayerEntity player, String selectedTabId, String message) {
        List<ElarionCollectionTab> tabs = new ArrayList<>();
        Map<String, Integer> originalOrder = new HashMap<>();
        for (TabProvider provider : providers.values()) {
            ElarionCollectionTab tab = provider.snapshot(player);
            originalOrder.putIfAbsent(tab.id(), originalOrder.size());
            tabs.add(tab);
        }
        if (tabs.stream().noneMatch(tab -> "pets".equals(tab.id()))) {
            originalOrder.putIfAbsent(PETS_TAB.id(), originalOrder.size());
            tabs.add(PETS_TAB);
        }
        tabs.sort(Comparator.comparingInt((ElarionCollectionTab tab) ->
                        PINNED_TAB_ORDER.getOrDefault(tab.id(), 1000))
                .thenComparingInt(tab -> originalOrder.getOrDefault(tab.id(), 1000)));
        String selected = normalize(selectedTabId);
        String requested = selected;
        if (selected.isBlank() || tabs.stream().noneMatch(tab -> tab.id().equals(requested))) {
            selected = tabs.isEmpty() ? "" : tabs.getFirst().id();
        }
        return new ElarionCollectionSnapshot(
                "Collection",
                "Mounts, pets, and titles.",
                selected,
                message,
                tabs);
    }

    public void open(ServerPlayerEntity player) {
        open(player, "", "");
    }

    public void open(ServerPlayerEntity player, String selectedTabId, String message) {
        ServerPlayNetworking.send(player, new CollectionOpenPayload(snapshot(player, selectedTabId, message)));
    }

    public ActionResult act(ServerPlayerEntity player, String tabId, String entryId, String actionId) {
        TabProvider provider = providers.get(normalize(tabId));
        if (provider == null) {
            return ActionResult.failure("Unknown collection tab.");
        }
        return provider.act(player, normalize(entryId), normalize(actionId));
    }

    public Optional<ElarionCollectionEntry> entry(ServerPlayerEntity player, String tabId, String entryId) {
        TabProvider provider = providers.get(normalize(tabId));
        if (provider == null) return Optional.empty();
        return provider.snapshot(player).entries().stream()
                .filter(entry -> entry.id().equals(normalize(entryId)))
                .findFirst();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public interface TabProvider {
        String id();
        ElarionCollectionTab snapshot(ServerPlayerEntity player);
        ActionResult act(ServerPlayerEntity player, String entryId, String actionId);
    }

    public record ActionResult(boolean success, String message) {
        public static ActionResult success(String message) {
            return new ActionResult(true, message);
        }

        public static ActionResult failure(String message) {
            return new ActionResult(false, message);
        }
    }
}
