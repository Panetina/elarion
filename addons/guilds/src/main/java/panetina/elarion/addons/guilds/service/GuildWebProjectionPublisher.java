package panetina.elarion.addons.guilds.service;

import panetina.elarion.addons.guilds.model.GuildRecord;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.integration.minecraft.MinecraftProjectionProtocol.Visibility;
import panetina.elarion.core.model.CitizenRecord;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Publishes the current guild lore owner without making website roles canonical. */
public final class GuildWebProjectionPublisher {
    public static final String GUILD_LORE_AUTHORITY = "authority.guild.lore";
    public static final String GUILD_MEMBERSHIP = "guild.membership";

    private final ElarionApi api;

    public GuildWebProjectionPublisher(ElarionApi api) {
        this.api = api;
    }

    public void publishAll(Collection<GuildRecord> guilds) {
        guilds.forEach(this::publishActive);
    }

    public void publishActive(GuildRecord guild) {
        publishAuthority(guild, true);
        if (guild == null) return;
        // The bridge has no member-targeted visibility primitive. Do not put a
        // secret Guild's membership, name, tag, or emblem in a broad web model.
        guild.members().forEach(member -> {
            if (guild.secret()) publishNoMembership(member);
            else publishMembership(guild, member, true);
        });
    }

    public void publishInactive(GuildRecord guild) {
        publishAuthority(guild, false);
        if (guild != null) guild.members().forEach(member -> publishMembership(guild, member, false));
    }

    public void publishNoMembership(java.util.UUID memberId) {
        if (memberId == null) return;
        String realmId = api.citizens().find(memberId).map(CitizenRecord::realmId).orElse("");
        api.system().webProjections().publishState(GUILD_MEMBERSHIP, memberId.toString(), realmId,
                Visibility.WHITELISTED, Map.of("active", "false"));
    }

    private void publishAuthority(GuildRecord guild, boolean active) {
        if (guild == null || guild.leaderId() == null) return;
        String realmId = api.citizens().find(guild.leaderId()).map(CitizenRecord::realmId).orElse("");
        // A secret Guild must never become discoverable through the public/authenticated lore index.
        boolean visible = active && !guild.secret();
        api.system().webProjections().publishState(
                GUILD_LORE_AUTHORITY,
                guild.id(),
                realmId,
                Visibility.AUTHENTICATED,
                authorityPayload(guild, visible));
    }

    private void publishMembership(GuildRecord guild, java.util.UUID memberId, boolean active) {
        if (guild == null || memberId == null) return;
        String realmId = api.citizens().find(memberId).map(CitizenRecord::realmId).orElse("");
        api.system().webProjections().publishState(GUILD_MEMBERSHIP, memberId.toString(), realmId,
                Visibility.WHITELISTED, membershipPayload(guild, memberId, active));
    }

    static Map<String, String> authorityPayload(GuildRecord guild, boolean active) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("active", Boolean.toString(active));
        payload.put("resourceId", guild.id());
        payload.put("ownerUuid", guild.leaderId().toString());
        payload.put("authorityRole", "leader");
        payload.put("displayName", guild.displayName());
        payload.put("iconRevision", Long.toString(guild.iconRevision()));
        payload.put("iconPaletteBase64", java.util.Base64.getEncoder().encodeToString(guild.iconPaletteIndices()));
        return Map.copyOf(payload);
    }

    static Map<String, String> membershipPayload(GuildRecord guild, java.util.UUID memberId, boolean active) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("active", Boolean.toString(active));
        if (active && guild != null && memberId != null) {
            payload.put("guildId", guild.id());
            payload.put("displayName", guild.displayName());
            payload.put("tag", guild.tag());
            payload.put("memberRole", guild.leaderId().equals(memberId) ? "Leader" : "Member");
            payload.put("iconRevision", Long.toString(guild.iconRevision()));
            payload.put("iconPaletteBase64", java.util.Base64.getEncoder().encodeToString(guild.iconPaletteIndices()));
        }
        return Map.copyOf(payload);
    }
}
