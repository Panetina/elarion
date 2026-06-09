package panetina.elarion.core.service;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.CommunityDefinition;
import panetina.elarion.core.model.PlayerIdentity;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.model.VisibilityScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class IdentityService {
    private final CitizenService citizens;
    private final CommunityService communities;
    private final TitleService titles;

    public IdentityService(CitizenService citizens, CommunityService communities, TitleService titles) {
        this.citizens = citizens;
        this.communities = communities;
        this.titles = titles;
    }

    public PlayerIdentity resolve(ServerPlayerEntity player) {
        CitizenRecord citizen = citizens.getOrCreate(player);
        CommunityDefinition community = communities.forCitizen(citizen).orElse(null);
        TitleDefinition title = titles.forCitizen(citizen).orElse(null);
        Formatting color = community == null ? Formatting.WHITE : color(community.color());
        String baseName = citizen.nickname() == null || citizen.nickname().isBlank()
                ? player.getGameProfile().getName()
                : citizen.nickname();
        String prefix = community == null ? "" : community.prefix();
        String suffix = title == null ? "" : title.suffix();
        MutableText display = Text.literal(baseName).formatted(color);
        if (!suffix.isBlank()) display.append(Text.literal(" " + suffix));
        MutableText chatName = Text.literal(baseName).formatted(color);
        if (!suffix.isBlank()) chatName.append(Text.literal(" " + suffix));
        Text titleText = title != null && title.visibleUnderUsername()
                ? Text.literal(title.displayName())
                : Text.empty();
        VisibilityScope scope = community == null ? VisibilityScope.COMMUNITY : community.visibilityScope();
        return new PlayerIdentity(display, chatName, display.copy(), titleText, prefix, suffix, color, scope);
    }

    public boolean canSee(ServerPlayerEntity viewer, ServerPlayerEntity subject) {
        if (viewer.getUuid().equals(subject.getUuid()) || viewer.hasPermissionLevel(4)) return true;

        CitizenRecord viewerCitizen = citizens.getOrCreate(viewer);
        CitizenRecord subjectCitizen = citizens.getOrCreate(subject);
        if (viewerCitizen.communityId() == null || subjectCitizen.communityId() == null) return true;

        return switch (resolve(subject).visibilityScope()) {
            case GLOBAL -> true;
            case COMMUNITY, ALLIES -> subjectCitizen.communityId().equals(viewerCitizen.communityId());
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
}
