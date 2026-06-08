package panetina.elarion.core.service;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.config.CoreConfigManager;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.TitleDefinition;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

public final class TitleService {
    private final CoreConfigManager config;
    private final CitizenService citizens;

    public TitleService(CoreConfigManager config, CitizenService citizens) {
        this.config = config;
        this.citizens = citizens;
    }

    public Collection<TitleDefinition> all() {
        return config.titles().values();
    }

    public Optional<TitleDefinition> find(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(config.titles().get(id.toLowerCase(Locale.ROOT)));
    }

    public Optional<TitleDefinition> forCitizen(CitizenRecord citizen) {
        return find(citizen.titleId());
    }

    public boolean assign(ServerPlayerEntity player, String titleId) {
        Optional<TitleDefinition> title = find(titleId);
        if (title.isEmpty()) return false;
        citizens.update(player, "title-assigned", citizen -> citizen.setTitleId(title.get().id()));
        return true;
    }

    public void clear(ServerPlayerEntity player) {
        citizens.update(player, "title-cleared", citizen -> citizen.setTitleId(null));
    }
}
