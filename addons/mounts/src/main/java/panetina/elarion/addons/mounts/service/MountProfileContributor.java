package panetina.elarion.addons.mounts.service;

import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.profile.CitizenProfileContributor;
import panetina.elarion.core.model.profile.CitizenProfileField;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSummaryFields;
import panetina.elarion.core.model.profile.ProfileVisibility;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.ToIntFunction;

public final class MountProfileContributor implements CitizenProfileContributor {
    private final ToIntFunction<UUID> unlockedCount;

    public MountProfileContributor(MountCollectionService collections) {
        this(playerId -> Objects.requireNonNull(collections, "collections").unlocked(playerId).size());
    }

    MountProfileContributor(ToIntFunction<UUID> unlockedCount) {
        this.unlockedCount = Objects.requireNonNull(unlockedCount, "unlockedCount");
    }

    @Override
    public String id() {
        return CitizenProfileSummaryFields.SOURCE_MOUNTS;
    }

    @Override
    public List<CitizenProfileSection> sections(CitizenProfileRequestContext context, CitizenRecord target) {
        return List.of(new CitizenProfileSection(
                "mounts.summary", "Mounts", CitizenProfileSummaryFields.SOURCE_MOUNTS,
                ProfileVisibility.PUBLIC,
                List.of(new CitizenProfileField(
                        CitizenProfileSummaryFields.FIELD_MOUNTS_UNLOCKED,
                        "Mounts Unlocked",
                        Integer.toString(Math.max(0, unlockedCount.applyAsInt(target.uuid()))),
                        ProfileVisibility.PUBLIC))));
    }
}
