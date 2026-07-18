package panetina.elarion.addons.groups.service;

import panetina.elarion.addons.groups.model.GroupRecord;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.profile.CitizenProfileContributor;
import panetina.elarion.core.model.profile.CitizenProfileField;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSummaryFields;
import panetina.elarion.core.model.profile.ProfileVisibility;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public final class GroupProfileContributor implements CitizenProfileContributor {
    private final Function<UUID, Optional<GroupRecord>> groups;

    public GroupProfileContributor(GroupService groups) {
        this(Objects.requireNonNull(groups, "groups")::groupFor);
    }

    GroupProfileContributor(Function<UUID, Optional<GroupRecord>> groups) {
        this.groups = Objects.requireNonNull(groups, "groups");
    }

    @Override
    public String id() {
        return CitizenProfileSummaryFields.SOURCE_GROUPS;
    }

    @Override
    public List<CitizenProfileSection> sections(CitizenProfileRequestContext context, CitizenRecord target) {
        GroupRecord group = groups.apply(target.uuid()).orElse(null);
        if (group == null) return List.of();
        String role = target.uuid().equals(group.leaderId()) ? "Leader" : "Member";
        return List.of(new CitizenProfileSection(
                "groups.membership",
                "Group",
                CitizenProfileSummaryFields.SOURCE_GROUPS,
                ProfileVisibility.PUBLIC,
                List.of(new CitizenProfileField(
                        CitizenProfileSummaryFields.FIELD_MEMBERSHIPS,
                        "Membership",
                        group.displayName() + " - " + role,
                        ProfileVisibility.PUBLIC))));
    }
}
