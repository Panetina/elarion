package panetina.elarion.core.model.profile;

import panetina.elarion.core.model.CitizenRecord;

import java.util.List;

public interface CitizenProfileContributor {
    String id();

    List<CitizenProfileSection> sections(CitizenProfileRequestContext context, CitizenRecord target);
}
