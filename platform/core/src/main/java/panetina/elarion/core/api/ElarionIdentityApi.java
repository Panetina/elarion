package panetina.elarion.core.api;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.service.IdentityService;
import panetina.elarion.core.service.IdentitySyncService;
import panetina.elarion.core.service.NicknameService;
import panetina.elarion.core.service.TitleService;

import java.util.function.BiPredicate;
import java.util.function.Function;

public final class ElarionIdentityApi {
    private final IdentityService identities;
    private final IdentitySyncService sync;
    private final NicknameService nicknames;
    private final TitleService titles;

    ElarionIdentityApi(
            IdentityService identities,
            IdentitySyncService sync,
            NicknameService nicknames,
            TitleService titles
    ) {
        this.identities = identities;
        this.sync = sync;
        this.nicknames = nicknames;
        this.titles = titles;
    }

    public IdentityService identities() {
        return identities;
    }

    public IdentitySyncService sync() {
        return sync;
    }

    public NicknameService nicknames() {
        return nicknames;
    }

    public TitleService titles() {
        return titles;
    }

    public void registerChatPrefixProvider(Function<ServerPlayerEntity, String> provider) {
        identities.registerChatPrefixProvider(provider);
    }

    public void registerNameplateTitleProvider(Function<ServerPlayerEntity, String> provider) {
        identities.registerNameplateTitleProvider(provider);
    }

    public void registerAuthorityMarkerProvider(BiPredicate<String, ServerPlayerEntity> provider) {
        identities.registerAuthorityMarkerProvider(provider);
    }
}
