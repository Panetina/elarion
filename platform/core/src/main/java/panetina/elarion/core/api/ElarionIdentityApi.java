package panetina.elarion.core.api;

import panetina.elarion.core.service.IdentityService;
import panetina.elarion.core.service.IdentitySyncService;
import panetina.elarion.core.service.NicknameService;
import panetina.elarion.core.service.TitleService;

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
}
