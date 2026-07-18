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
            description: "Embers elect a president and council through public votes."
            official-name-template: "Republic of %realm%"
            enabled: true
            authority-offices:
              - president
              - council_member
              - officer

            offices:
              - id: president
                display-name: "President"
                max-holders: 1
                description: "The elected head of a republic."
              - id: council_member
                display-name: "Councilor"
                max-holders: 3
                description: "Elected council authority."
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
              council_member:
                - authority_chat
              officer:
                - authority_chat

            transitions:
              leader: remove
              offices: keep_compatible
              laws: keep_compatible
              treasury: keep
            """;

    public static final String THEOCRACY_FORM = """
            id: theocracy
            display-name: "Theocracy"
            description: "Embers first define a Realm faith, then elect one High Priest. The High Priest appoints up to three Synod Members after founding."
            official-name-template: "Holy %realm%"
            enabled: true
            authority-offices:
              - high_priest
              - synod_member
              - officer

            offices:
              - id: high_priest
                display-name: "High Priest"
                max-holders: 1
                description: "The primary spiritual authority and public voice of the Realm faith."
              - id: synod_member
                display-name: "Synod Member"
                max-holders: 3
                description: "One of up to three council members who advise, check, and represent the Realm faith."
              - id: officer
                display-name: "Temple Officer"
                max-holders: 3
                description: "Appointed authority placeholder for future law enforcement systems."

            actions:
              citizen:
                - vote
                - propose_reform
              high_priest:
                - authority_chat
                - appoint_officer
              synod_member:
                - authority_chat
              officer:
                - authority_chat

            transitions:
              leader: remove
              offices: keep_compatible
              laws: keep_compatible
              treasury: keep
            """;

    public static final String CONFEDERATION_FORM = """
            id: confederation
            display-name: "Confederation"
            description: "Registered groups elect delegates to share Realm authority."
            official-name-template: "%realm% Confederation"
            enabled: true
            confederation-delegates-represent-groups: true
            authority-offices:
              - delegate
              - officer

            offices:
              - id: delegate
                display-name: "Delegate"
                max-holders: 3
                description: "Elected representative of an eligible registered group."
              - id: officer
                display-name: "Confederation Officer"
                max-holders: 3
                description: "Appointed authority placeholder for future law enforcement systems."

            actions:
              citizen:
                - vote
                - propose_reform
              delegate:
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
