package panetina.elarion.addons.government.service;

import panetina.elarion.addons.government.model.GovernmentFormDefinition;
import panetina.elarion.addons.government.model.GovernmentOfficeDefinition;
import panetina.elarion.addons.government.model.RealmGovernmentState;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.profile.CitizenProfileContributor;
import panetina.elarion.core.model.profile.CitizenProfileField;
import panetina.elarion.core.model.profile.CitizenProfileRequestContext;
import panetina.elarion.core.model.profile.CitizenProfileSection;
import panetina.elarion.core.model.profile.CitizenProfileSummaryFields;
import panetina.elarion.core.model.profile.ProfileVisibility;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class GovernmentProfileContributor implements CitizenProfileContributor {
    private final GovernmentDefinitionService definitions;
    private final GovernmentStateService states;

    public GovernmentProfileContributor(GovernmentDefinitionService definitions, GovernmentStateService states) {
        this.definitions = Objects.requireNonNull(definitions, "definitions");
        this.states = Objects.requireNonNull(states, "states");
    }

    @Override
    public String id() {
        return CitizenProfileSummaryFields.SOURCE_GOVERNMENT;
    }

    @Override
    public List<CitizenProfileSection> sections(CitizenProfileRequestContext context, CitizenRecord target) {
        List<String> roles = currentRoles(target);
        List<String> history = states.officeTermsFor(target.uuid(), 8).stream()
                .map(this::termLabel)
                .toList();
        if (roles.isEmpty() && history.isEmpty()) return List.of();
        return List.of(new CitizenProfileSection(
                "government.office",
                "Government Office",
                CitizenProfileSummaryFields.SOURCE_GOVERNMENT,
                ProfileVisibility.PUBLIC,
                List.of(
                        new CitizenProfileField(CitizenProfileSummaryFields.FIELD_ACTIVE_OFFICE,
                                "Active Office", roles.isEmpty() ? "None" : roles.getFirst(), ProfileVisibility.PUBLIC),
                        new CitizenProfileField(CitizenProfileSummaryFields.FIELD_OFFICE_HISTORY,
                                "Office History", history.isEmpty() ? "No former terms" : String.join(", ", history),
                                ProfileVisibility.PUBLIC))));
    }

    private List<String> currentRoles(CitizenRecord target) {
        if (target.realmId().isBlank()) return List.of();
        RealmGovernmentState state = states.realm(target.realmId());
        GovernmentFormDefinition form = definitions.form(state.activeGovernmentFormId()).orElse(null);
        return form == null ? List.of() : roleLabels(state, form, target.uuid());
    }

    private String termLabel(panetina.elarion.addons.government.model.GovernmentOfficeTermRecord term) {
        RealmGovernmentState state = states.realm(term.realmId());
        GovernmentFormDefinition form = definitions.form(state.activeGovernmentFormId()).orElse(null);
        String office = form == null ? titleCase(term.officeId()) : form.offices().stream()
                .filter(value -> value.id().equals(term.officeId()))
                .map(GovernmentOfficeDefinition::displayName)
                .findFirst()
                .orElseGet(() -> titleCase(term.officeId()));
        return office + " (" + states.realmDisplayName(term.realmId()) + ")";
    }

    private static String titleCase(String value) {
        String clean = value == null ? "" : value.replace('_', ' ').trim();
        return clean.isBlank() ? "Unknown" : Character.toUpperCase(clean.charAt(0)) + clean.substring(1);
    }

    static List<String> roleLabels(RealmGovernmentState state, GovernmentFormDefinition form, UUID citizenId) {
        if (state == null || form == null || citizenId == null) return List.of();
        return form.offices().stream()
                .filter(office -> state.officeHolders().getOrDefault(office.id(), java.util.Set.of()).contains(citizenId))
                .map(GovernmentOfficeDefinition::displayName)
                .toList();
    }
}
