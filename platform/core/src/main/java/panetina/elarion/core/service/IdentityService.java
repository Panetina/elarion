package panetina.elarion.core.service;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.CommunityDefinition;
import panetina.elarion.core.model.PlayerIdentity;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.model.VisibilityScope;

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
        MutableText display = Text.empty();
        if (!prefix.isBlank()) display.append(Text.literal(prefix + " ").formatted(color));
        display.append(Text.literal(baseName).formatted(color));
        if (!suffix.isBlank()) display.append(Text.literal(" " + suffix));
        MutableText chatName = Text.literal(baseName).formatted(color);
        if (!suffix.isBlank()) chatName.append(Text.literal(" " + suffix));
        Text titleText = title != null && title.visibleUnderUsername()
                ? Text.literal(title.displayName())
                : Text.empty();
        VisibilityScope scope = community == null ? VisibilityScope.COMMUNITY : community.visibilityScope();
        return new PlayerIdentity(display, chatName, display.copy(), titleText, prefix, suffix, color, scope);
    }

    private static Formatting color(String value) {
        Formatting formatting = Formatting.byName(value);
        return formatting == null ? Formatting.WHITE : formatting;
    }
}
