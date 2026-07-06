package panetina.elarion.core.config;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ElarionConfigRegistry {
    private final Map<String, ElarionConfigDomain> domains = new ConcurrentHashMap<>();

    public void registerDomain(ElarionConfigDomain domain) {
        if (domain == null) throw new IllegalArgumentException("Config domain is required");
        ElarionConfigDomain previous = domains.putIfAbsent(domain.id(), domain);
        if (previous != null) {
            throw new IllegalArgumentException("Config domain already registered: " + domain.id());
        }
    }

    public Optional<ElarionConfigDomain> domain(String domainId) {
        String normalized = domainId == null ? "" : domainId.trim().toLowerCase(java.util.Locale.ROOT);
        return Optional.ofNullable(domains.get(normalized));
    }

    public List<ElarionConfigDomain> domains() {
        return domains.values().stream()
                .sorted(Comparator.comparing(ElarionConfigDomain::id))
                .toList();
    }
}

