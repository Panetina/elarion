package panetina.elarion.core.model.profile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record CitizenProfileSnapshot(
        UUID targetId,
        String title,
        List<CitizenProfileSection> sections
) {
    public CitizenProfileSnapshot {
        targetId = Objects.requireNonNull(targetId, "targetId");
        title = title == null ? "" : title.trim();
        sections = sections == null ? List.of() : List.copyOf(sections.stream()
                .filter(section -> section != null)
                .toList());
    }

    public Optional<CitizenProfileSection> section(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        String normalized = id.trim().toLowerCase(java.util.Locale.ROOT);
        return sections.stream()
                .filter(section -> section.id().equals(normalized))
                .findFirst();
    }
}
