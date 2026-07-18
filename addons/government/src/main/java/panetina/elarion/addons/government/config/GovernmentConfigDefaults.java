package panetina.elarion.addons.government.config;

public final class GovernmentConfigDefaults {
    private GovernmentConfigDefaults() {
    }

    public static final String GOVERNMENT = """
            config-version: 1
            enabled: true
            default-reform-threshold-percent: 80
            default-reform-duration: "3d"

            authority:
              inactivity-days: 7
              inactivity-check-interval-seconds: 600
            """;

    public static final String MONARCHY_FORM = """
            id: monarchy
            display-name: "Monarchy"
            description: "One elected monarch rules the Realm. Succession and heirs expand later."
            official-name-template: "Kingdom of %realm%"
            enabled: true
            authority-offices:
              - monarch
              - heir
              - officer

            offices:
              - id: monarch
                display-name: "Monarch"
                max-holders: 1
                description: "The ruling authority of a monarchy."
              - id: heir
                display-name: "Heir"
                max-holders: 1
                description: "Reserved successor placeholder. Succession mechanics are future work."
              - id: officer
                display-name: "Crown Officer"
                max-holders: 3
                description: "Appointed authority placeholder for future law enforcement systems."

            actions:
              citizen:
                - vote
                - propose_reform
              monarch:
                - authority_chat
                - appoint_officer
              heir:
                - authority_chat
              officer:
                - authority_chat

            transitions:
              leader: remove
              offices: dissolve
              laws: keep_compatible
              treasury: keep
            """;

    public static final String REPUBLIC_FORM = """
            id: republic
            display-name: "Republic"
            description: "Embers elect one president. The President drafts laws, and citizens ratify them with Yes or No votes."
            official-name-template: "Republic of %realm%"
            enabled: true
            authority-offices:
              - president
              - officer

            offices:
              - id: president
                display-name: "President"
                max-holders: 1
                description: "The elected head of the Republic. Drafts laws for citizen ratification."
              - id: officer
                display-name: "Civic Officer"
                max-holders: 3
                description: "Appointed authority placeholder for future law enforcement systems."

            actions:
              citizen:
                - vote
                - propose_reform
              president:
                - authority_chat
                - appoint_officer
              officer:
                - authority_chat

            transitions:
              leader: remove
              offices: keep_compatible
              laws: keep_compatible
              treasury: keep
            """;

}
