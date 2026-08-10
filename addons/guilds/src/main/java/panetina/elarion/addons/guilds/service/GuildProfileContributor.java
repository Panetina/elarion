package panetina.elarion.addons.guilds.service;

import panetina.elarion.addons.guilds.model.GuildRecord;
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

public final class GuildProfileContributor implements CitizenProfileContributor {
    private final Function<UUID, Optional<GuildRecord>> guilds;

    public GuildProfileContributor(GuildService guilds) {
        this(Objects.requireNonNull(guilds, "guilds")::guildFor);
    }

    GuildProfileContributor(Function<UUID, Optional<GuildRecord>> guilds) {
        this.guilds = Objects.requireNonNull(guilds, "guilds");
    }

    @Override
    public String id() {
        return CitizenProfileSummaryFields.SOURCE_GUILDS;
    }

    @Override
    public List<CitizenProfileSection> sections(CitizenProfileRequestContext context, CitizenRecord target) {
        GuildRecord guild = guilds.apply(target.uuid()).orElse(null);
        if (guild == null) return List.of();
        String role = target.uuid().equals(guild.leaderId()) ? "Leader" : "Member";
        return List.of(new CitizenProfileSection(
                "guilds.membership",
                "Guild",
                CitizenProfileSummaryFields.SOURCE_GUILDS,
                ProfileVisibility.PUBLIC,
                List.of(new CitizenProfileField(
                        CitizenProfileSummaryFields.FIELD_MEMBERSHIPS,
                        "Membership",
                        guild.displayName() + " - " + role,
                        ProfileVisibility.PUBLIC))));
    }
}
