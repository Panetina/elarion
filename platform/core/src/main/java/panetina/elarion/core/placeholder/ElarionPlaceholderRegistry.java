package panetina.elarion.core.placeholder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

public final class ElarionPlaceholderRegistry {
    private final Map<String, Registration> registrations = new LinkedHashMap<>();
    private final Map<String, PlaceholderAlias> aliases = new LinkedHashMap<>();

    public synchronized void register(PlaceholderDescriptor descriptor, PlaceholderResolver resolver) {
        if (descriptor == null || resolver == null) throw new IllegalArgumentException("descriptor and resolver required");
        if (registrations.containsKey(descriptor.id()) || aliases.containsKey(descriptor.id())) {
            throw new IllegalStateException("Duplicate placeholder id " + descriptor.id());
        }
        registrations.put(descriptor.id(), new Registration(descriptor, resolver));
    }

    public synchronized void registerAlias(PlaceholderAlias alias) {
        if (alias == null) throw new IllegalArgumentException("alias required");
        if (registrations.containsKey(alias.id()) || aliases.containsKey(alias.id())) {
            throw new IllegalStateException("Duplicate placeholder alias " + alias.id());
        }
        if (!registrations.containsKey(alias.targetId())) {
            throw new IllegalArgumentException("Unknown placeholder alias target " + alias.targetId());
        }
        aliases.put(alias.id(), alias);
    }

    public synchronized Optional<Registration> registration(String id) {
        return Optional.ofNullable(registrations.get(normalizeOrBlank(id)));
    }

    public synchronized Optional<PlaceholderAlias> alias(String id) {
        return Optional.ofNullable(aliases.get(normalizeOrBlank(id)));
    }

    public synchronized Collection<PlaceholderDescriptor> descriptors() {
        return registrations.values().stream().map(Registration::descriptor).toList();
    }

    public synchronized Collection<PlaceholderAlias> aliases() {
        return List.copyOf(aliases.values());
    }

    private static String normalizeOrBlank(String id) {
        try {
            return PlaceholderDescriptor.normalize(id);
        } catch (IllegalArgumentException exception) {
            return "";
        }
    }

    public record Registration(PlaceholderDescriptor descriptor, PlaceholderResolver resolver) {
    }
}
