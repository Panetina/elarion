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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class IdentityService {
    private static final Identifier ICON_FONT = Identifier.of("elarion_core", "icons");
    private static final String CROWN_GLYPH = "\ue000";
    private final CitizenService citizens;
    private final RealmService realms;
    private final TitleService titles;
    private final List<Function<ServerPlayerEntity, String>> chatPrefixProviders = new CopyOnWriteArrayList<>();
    private final List<BiPredicate<String, ServerPlayerEntity>> authorityMarkerProviders = new CopyOnWriteArrayList<>();
    private RealmGovernanceService governance;
    private PlayerRestrictionService restrictions;

    public IdentityService(CitizenService citizens, RealmService realms, TitleService titles) {
        this.citizens = citizens;
        this.realms = realms;
        this.titles = titles;
    }

    public void setGovernance(RealmGovernanceService governance) {
        this.governance = governance;
    }

    public void setRestrictions(PlayerRestrictionService restrictions) {
        this.restrictions = restrictions;
    }

    public void registerChatPrefixProvider(Function<ServerPlayerEntity, String> provider) {
        if (provider != null) chatPrefixProviders.add(provider);
    }

    public void registerAuthorityMarkerProvider(BiPredicate<String, ServerPlayerEntity> provider) {
        if (provider != null) authorityMarkerProviders.add(provider);
    }

    public PlayerIdentity resolve(ServerPlayerEntity player) {
        CitizenRecord citizen = citizens.getOrCreate(player);
        RealmDefinition realm = realms.forCitizen(citizen).orElse(null);
        TitleDefinition title = titles.forCitizen(citizen).orElse(null);
        Formatting color = realm == null ? Formatting.WHITE : color(realms.color(realm));
        String baseName = citizen.nickname() == null || citizen.nickname().isBlank()
                ? player.getGameProfile().getName()
                : citizen.nickname();
        String prefix = realm == null ? "" : realms.prefix(realm);
        String suffix = title == null ? "" : title.suffix();
        boolean authorityMarked = realm != null && isAuthorityMarked(realm.id(), player);
        MutableText display = Text.literal(baseName).formatted(color);
        if (!suffix.isBlank()) display.append(Text.literal(" " + suffix));
        MutableText chatName = Text.empty();
        String externalPrefix = externalPrefix(player);
        if (!externalPrefix.isBlank()) chatName.append(Text.literal(externalPrefix + " ").formatted(Formatting.GRAY));
        if (authorityMarked) chatName.append(crown()).append(Text.literal(" "));
        chatName.append(Text.literal(baseName).formatted(color));
        if (!suffix.isBlank()) chatName.append(Text.literal(" " + suffix));
        Text titleText = title != null && title.visibleUnderUsername()
                ? Text.literal(title.displayName())
                : Text.empty();
        Text leaderText = authorityMarked ? crown() : Text.empty();
        VisibilityScope scope = realm == null ? VisibilityScope.REALM : realm.visibilityScope();
        MutableText tabName = Text.empty();
        if (!externalPrefix.isBlank()) tabName.append(Text.literal(externalPrefix + " ").formatted(Formatting.GRAY));
        if (authorityMarked) tabName.append(crown()).append(Text.literal(" "));
        tabName.append(display.copy());
        return new PlayerIdentity(display, chatName, tabName, titleText, leaderText,
                prefix, suffix, color, scope);
    }

    public boolean canSee(ServerPlayerEntity viewer, ServerPlayerEntity subject) {
        if (viewer.getUuid().equals(subject.getUuid()) || viewer.hasPermissionLevel(4)) return true;
        if (restrictions != null && restrictions.isRestricted(subject, PlayerRestrictionService.NAMEPLATE)) return false;

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

    private String externalPrefix(ServerPlayerEntity player) {
        for (Function<ServerPlayerEntity, String> provider : chatPrefixProviders) {
            try {
                String value = provider.apply(player);
                if (value != null && !value.isBlank()) return value.trim();
            } catch (RuntimeException ignored) {
                // Presentation hooks must not break identity rendering.
            }
        }
        return "";
    }

    private boolean isAuthorityMarked(String realmId, ServerPlayerEntity player) {
        if (player == null || realmId == null || realmId.isBlank()) return false;
        CitizenRecord citizen = citizens.getOrCreate(player);
        if (citizen.isRealmLeader()) return true;
        for (BiPredicate<String, ServerPlayerEntity> provider : authorityMarkerProviders) {
            try {
                if (provider.test(realmId, player)) return true;
            } catch (RuntimeException ignored) {
                // Presentation hooks must not break identity rendering.
            }
        }
        return false;
    }
}
