package panetina.elarion.addons.offerings.config;

public final class OfferingConfigDefaults {
    private OfferingConfigDefaults() {
    }

    public static final String SOCIETY = """
            # Society progression defaults are reserved for the future offering Block UI.
            # V1 loads project definitions and runtime project instances only.
            society:
              enabled: true
              ranks: []
            """;

    public static final String UI = """
            config-version: 1
            theme-variant: "shrine"
            logical-width: 520
            logical-height: 360
            minimum-scale-percent: 60
            summary-width: 150
            tab-height: 20
            row-height: 24
            icon-size: 48
            close-button-width: 92
            rewards-placeholder: "Rewards will appear here when milestones are configured."
            history-placeholder: "No recent offerings recorded."
            contribution-placeholder: "Select an incomplete item or currency requirement to make an offering."
            event-title: "Upcoming Event"
            event-body: "No shrine event active."
            event-locked-body: "Events system not unlocked yet."
            """;

    public static final String COUNCIL_HALL = """
            # Reusable project definition. Runtime instances can be Realm-owned, global, or location-based.
            id: council_hall
            display-name: "Council Hall"
            description: "A civic project used to prove the offering backend."
            enabled: true
            scope: realm
            repeatable: false
            allow-multiple-instances: true

            presentation:
              level-text: "Foundation I"
              icon: "minecraft:textures/item/amethyst_shard.png"

            requirements:
              items:
                - id: "minecraft:stone_bricks"
                  count: 64
              currency:
                amount: 25
              events:
                - id: "builder_help"
                  count: 3

            milestones:
              - id: "announce_completion"
                type: "elarion:emit_history"
                parameters:
                  category: "offering"
                  type: "project-completed"
                  subject-type: "project"
                  subject-id: "council_hall"
            """;
}
