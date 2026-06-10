package panetina.elarion.core.service;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.PlayerIdentity;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.RealmRelationship;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.model.VisibilityScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class IdentityService {
    private static final Identifier ICON_FONT = Identifier.of("elarion_core", "icons");
    private static final String CROWN_GLYPH = "\ue000";
    private final CitizenService citizens;
    private final RealmService realms;
    private final TitleService titles;
    private RealmGovernanceService governance;

    public IdentityService(CitizenService citizens, RealmService realms, TitleService titles) {
        this.citizens = citizens;
        this.realms = realms;
        this.titles = titles;
    }

    public void setGovernance(RealmGovernanceService governance) {
        this.governance = governance;
    }

    public PlayerIdentity resolve(ServerPlayerEntity player) {
        CitizenRecord citizen = citizens.getOrCreate(player);
        RealmDefinition realm = realms.forCitizen(citizen).orElse(null);
        TitleDefinition title = titles.forCitizen(citizen).orElse(null);
        Formatting color = realm == null ? Formatting.WHITE : color(realm.color());
        String baseName = citizen.nickname() == null || citizen.nickname().isBlank()
                ? player.getGameProfile().getName()
                : citizen.nickname();
        String prefix = realm == null ? "" : realm.prefix();
        String suffix = title == null ? "" : title.suffix();
        MutableText display = Text.literal(baseName).formatted(color);
        if (!suffix.isBlank()) display.append(Text.literal(" " + suffix));
        MutableText chatName = Text.empty();
        if (citizen.isRealmLeader()) chatName.append(crown()).append(Text.literal(" "));
        chatName.append(Text.literal(baseName).formatted(color));
        if (!suffix.isBlank()) chatName.append(Text.literal(" " + suffix));
        Text titleText = title != null && title.visibleUnderUsername()
                ? Text.literal(title.displayName())
                : Text.empty();
        Text leaderText = citizen.isRealmLeader() ? crown() : Text.empty();
        VisibilityScope scope = realm == null ? VisibilityScope.REALM : realm.visibilityScope();
        MutableText tabName = Text.empty();
        if (citizen.isRealmLeader()) tabName.append(crown()).append(Text.literal(" "));
        tabName.append(display.copy());
        return new PlayerIdentity(display, chatName, tabName, titleText, leaderText,
                prefix, suffix, color, scope);
    }

    public boolean canSee(ServerPlayerEntity viewer, ServerPlayerEntity subject) {
        if (viewer.getUuid().equals(subject.getUuid()) || viewer.hasPermissionLevel(4)) return true;

        CitizenRecord viewerCitizen = citizens.getOrCreate(viewer);
        CitizenRecord subjectCitizen = citizens.getOrCreate(subject);
        if (viewerCitizen.realmId().isBlank() || subjectCitizen.realmId().isBlank()) return true;
        RealmRelationship relationship = governance == null
                ? RealmRelationship.NEUTRAL
                : governance.relationship(viewerCitizen.realmId(), subjectCitizen.realmId());
        boolean subjectIsHiding = governance != null
                && governance.isHidden(subjectCitizen.realmId());

        return switch (resolve(subject).visibilityScope()) {
            case GLOBAL -> !subjectIsHiding;
            case REALM -> subjectCitizen.realmId().equals(viewerCitizen.realmId());
            case ALLIES -> subjectCitizen.realmId().equals(viewerCitizen.realmId())
                    || relationship == RealmRelationship.ALLY;
            case ADMIN_ONLY, HIDDEN -> false;
        };
    }

    public boolean canSee(ServerCommandSource source, ServerPlayerEntity subject) {
        if (source.hasPermissionLevel(4) || source.getEntity() == null) return true;
        return source.getEntity() instanceof ServerPlayerEntity viewer && canSee(viewer, subject);
    }

    public Optional<ServerPlayerEntity> resolveVisiblePlayer(ServerCommandSource source, String input) {
        ServerPlayerEntity canonical = source.getServer().getPlayerManager().getPlayer(input);
        if (canonical != null && canSee(source, canonical)) return Optional.of(canonical);

        String normalized = NicknameService.comparisonKey(input);
        List<ServerPlayerEntity> matches = new ArrayList<>();
        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            if (!canSee(source, player)) continue;
            CitizenRecord citizen = citizens.getOrCreate(player);
            if (citizen.nickname() != null
                    && NicknameService.comparisonKey(citizen.nickname()).equals(normalized)) {
                matches.add(player);
                continue;
            }
            if (NicknameService.comparisonKey(resolve(player).displayName().getString()).equals(normalized)) {
                matches.add(player);
            }
        }
        return matches.size() == 1 ? Optional.of(matches.getFirst()) : Optional.empty();
    }

    private static Formatting color(String value) {
        Formatting formatting = Formatting.byName(value);
        return formatting == null ? Formatting.WHITE : formatting;
    }

    private static Text crown() {
        return Text.literal(CROWN_GLYPH)
                .styled(style -> style.withFont(ICON_FONT).withColor(Formatting.GOLD));
    }
}
