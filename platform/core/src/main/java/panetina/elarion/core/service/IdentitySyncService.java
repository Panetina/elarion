package panetina.elarion.core.service;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.CommunityDefinition;
import panetina.elarion.core.model.PlayerIdentity;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.network.IdentitySyncPayload;

public final class IdentitySyncService {
    private final CitizenService citizens;
    private final CommunityService communities;
    private final TitleService titles;
    private final IdentityService identities;

    public IdentitySyncService(
            CitizenService citizens,
            CommunityService communities,
            TitleService titles,
            IdentityService identities
    ) {
        this.citizens = citizens;
        this.communities = communities;
        this.titles = titles;
        this.identities = identities;
    }

    public void syncAll(MinecraftServer server) {
        for (ServerPlayerEntity viewer : server.getPlayerManager().getPlayerList()) {
            for (ServerPlayerEntity subject : server.getPlayerManager().getPlayerList()) {
                sync(viewer, subject);
            }
        }
    }

    public void syncViewer(ServerPlayerEntity viewer) {
        MinecraftServer server = viewer.getServer();
        if (server == null) return;
        for (ServerPlayerEntity subject : server.getPlayerManager().getPlayerList()) {
            sync(viewer, subject);
        }
    }

    private void sync(ServerPlayerEntity viewer, ServerPlayerEntity subject) {
        CitizenRecord citizen = citizens.getOrCreate(subject);
        PlayerIdentity identity = identities.resolve(subject);
        CommunityDefinition community = communities.forCitizen(citizen).orElse(null);
        TitleDefinition title = titles.forCitizen(citizen).orElse(null);
        boolean visible = identities.canSee(viewer, subject);

        ServerPlayNetworking.send(viewer, new IdentitySyncPayload(
                subject.getUuid(),
                subject.getGameProfile().getName(),
                visible && citizen.nickname() != null ? citizen.nickname() : "",
                visible ? identity.prefix() : "",
                visible ? identity.suffix() : "",
                visible && title != null && title.visibleUnderUsername() ? title.displayName() : "",
                visible ? identity.color().getName() : "white",
                visible && community != null ? community.id() : "",
                visible));
    }
}
