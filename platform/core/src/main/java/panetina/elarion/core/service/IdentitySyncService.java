package panetina.elarion.core.service;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.PlayerIdentity;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.network.IdentitySyncPayload;

import java.util.UUID;

public final class IdentitySyncService {
    private final CitizenService citizens;
    private final RealmService realms;
    private final TitleService titles;
    private final IdentityService identities;
    private final IdentitySyncBatcher batcher = new IdentitySyncBatcher();

    public IdentitySyncService(
            CitizenService citizens,
            RealmService realms,
            TitleService titles,
            IdentityService identities
    ) {
        this.citizens = citizens;
        this.realms = realms;
        this.titles = titles;
        this.identities = identities;
    }

    public void syncAll(MinecraftServer server) {
        batcher.requestFull();
    }

    public void syncAllNow(MinecraftServer server) {
        if (server == null) return;
        for (ServerPlayerEntity viewer : server.getPlayerManager().getPlayerList()) {
            for (ServerPlayerEntity subject : server.getPlayerManager().getPlayerList()) {
                sync(viewer, subject);
            }
        }
    }

    public void syncViewer(ServerPlayerEntity viewer) {
        if (viewer == null) return;
        batcher.requestViewer(viewer.getUuid());
    }

    public void syncViewerNow(ServerPlayerEntity viewer) {
        MinecraftServer server = viewer.getServer();
        if (server == null) return;
        for (ServerPlayerEntity subject : server.getPlayerManager().getPlayerList()) {
            sync(viewer, subject);
        }
    }

    public void syncSubject(MinecraftServer server, ServerPlayerEntity subject) {
        if (server == null || subject == null) return;
        batcher.requestSubject(subject.getUuid());
    }

    public void syncSubjectNow(MinecraftServer server, ServerPlayerEntity subject) {
        if (server == null || subject == null) return;
        for (ServerPlayerEntity viewer : server.getPlayerManager().getPlayerList()) {
            sync(viewer, subject);
        }
    }

    public void tick(MinecraftServer server) {
        IdentitySyncBatcher.Intent intent = batcher.drain();
        if (intent.empty() || server == null) return;
        if (intent.full()) {
            syncAllNow(server);
            return;
        }
        for (UUID viewerId : intent.viewers()) {
            ServerPlayerEntity viewer = server.getPlayerManager().getPlayer(viewerId);
            if (viewer != null) syncViewerNow(viewer);
        }
        for (UUID subjectId : intent.subjects()) {
            ServerPlayerEntity subject = server.getPlayerManager().getPlayer(subjectId);
            if (subject != null) syncSubjectNow(server, subject);
        }
    }

    private void sync(ServerPlayerEntity viewer, ServerPlayerEntity subject) {
        CitizenRecord citizen = citizens.getOrCreate(subject);
        PlayerIdentity identity = identities.resolve(subject);
        RealmDefinition realm = realms.forCitizen(citizen).orElse(null);
        TitleDefinition title = titles.forCitizen(citizen).orElse(null);
        boolean visible = true;
        boolean tabVisible = tabVisible(viewer, subject);

        ServerPlayNetworking.send(viewer, new IdentitySyncPayload(
                subject.getUuid(),
                subject.getGameProfile().getName(),
                visible && citizen.nickname() != null ? citizen.nickname() : "",
                visible ? identity.prefix() : "",
                visible ? identity.suffix() : "",
                visible && title != null && title.visibleUnderUsername() ? title.displayName() : "",
                visible ? identity.leaderText().getString() : "",
                visible ? identity.color().getName() : "white",
                visible && realm != null ? realms.officialName(realm) : "",
                visible && realm != null ? realm.id() : "",
                tabVisible,
                visible));
    }

    private boolean tabVisible(ServerPlayerEntity viewer, ServerPlayerEntity subject) {
        CitizenRecord viewerCitizen = citizens.getOrCreate(viewer);
        CitizenRecord subjectCitizen = citizens.getOrCreate(subject);
        return tabVisible(viewer.getUuid(), viewerCitizen.realmId(), viewer.hasPermissionLevel(4),
                subject.getUuid(), subjectCitizen.realmId());
    }

    static boolean tabVisible(
            UUID viewerId,
            String viewerRealm,
            boolean viewerAdmin,
            UUID subjectId,
            String subjectRealm
    ) {
        if (viewerId != null && viewerId.equals(subjectId)) return true;
        if (viewerAdmin) return true;
        String viewer = viewerRealm == null ? "" : viewerRealm.trim();
        String subject = subjectRealm == null ? "" : subjectRealm.trim();
        if (viewer.isBlank() || subject.isBlank()) return true;
        return viewer.equals(subject);
    }
}
