package panetina.elarion.addons.portals.config;

public final class PortalConfigDefaults {
    public static final String ROUTES = """
            # Linked portal routes. Use scheduled_ticketed for progression gates or
            # always_open for unrestricted two-way gates.
            routes:
              nether:
                display-name: "Nether Gate"
                description: "A scheduled passage between %capital% and the Nether."
                source-dimension: "elarion:lobby"
                destination-dimension: "minecraft:the_nether"
                enabled: true
                mode: "scheduled_ticketed"
                ticket:
                  id: "nether"
                  display-name: "Nether Ticket"
                  lore: "One outward passage and one eventual return."
                  price-key: "portal_ticket.nether"
                schedule:
                  timezone: "Europe/Bucharest"
                  anchor: "2026-01-03T18:00:00Z"
                  interval: "7d"
                  duration: "4h"
                visual:
                  color: "#A82929"
                  brightness: 1.0
                  opacity: 0.82
                  frame-time: 2
                  texture: "minecraft:block/nether_portal"
                  icon-item: "elarion:portal_ticket"
                  status-icon-item: "minecraft:netherrack"

              end:
                display-name: "End Gate"
                description: "A scheduled passage between %capital% and the End."
                source-dimension: "elarion:lobby"
                destination-dimension: "minecraft:the_end"
                enabled: true
                mode: "scheduled_ticketed"
                ticket:
                  id: "end"
                  display-name: "End Ticket"
                  lore: "One outward passage and one eventual return."
                  price-key: "portal_ticket.end"
                schedule:
                  timezone: "Europe/Bucharest"
                  anchor: "2026-01-10T18:00:00Z"
                  interval: "14d"
                  duration: "4h"
                visual:
                  color: "#8A5BC7"
                  brightness: 1.0
                  opacity: 0.82
                  frame-time: 2
                  texture: "minecraft:block/nether_portal"
                  icon-item: "elarion:portal_ticket"
                  status-icon-item: "minecraft:end_stone"

              realm1:
                display-name: "Ancient Gate"
                description: "An ancient passage between %realm_official% and %capital%."
                source-dimension: "elarion:realm_world_1"
                destination-dimension: "elarion:lobby"
                enabled: true
                mode: "fee_passage"
                passage:
                  price-key: "ancient_gate.passage"
                  first-round-trip-free: true
                visual:
                  color: "#D6A84B"
                  brightness: 1.0
                  opacity: 0.82
                  frame-time: 2
                  texture: "minecraft:block/nether_portal"
                  icon-item: "elarion:currency"
                  prompt-accent-color: "#9696D1"

              realm2:
                display-name: "Ancient Gate"
                description: "An ancient passage between %realm_official% and %capital%."
                source-dimension: "elarion:realm_world_2"
                destination-dimension: "elarion:lobby"
                enabled: true
                mode: "fee_passage"
                passage:
                  price-key: "ancient_gate.passage"
                  first-round-trip-free: true
                visual:
                  color: "#D6A84B"
                  brightness: 1.0
                  opacity: 0.82
                  frame-time: 2
                  texture: "minecraft:block/nether_portal"
                  icon-item: "elarion:currency"
                  prompt-accent-color: "#9696D1"

              realm3:
                display-name: "Ancient Gate"
                description: "An ancient passage between %realm_official% and %capital%."
                source-dimension: "elarion:realm_world_3"
                destination-dimension: "elarion:lobby"
                enabled: true
                mode: "fee_passage"
                passage:
                  price-key: "ancient_gate.passage"
                  first-round-trip-free: true
                visual:
                  color: "#D6A84B"
                  brightness: 1.0
                  opacity: 0.82
                  frame-time: 2
                  texture: "minecraft:block/nether_portal"
                  icon-item: "elarion:currency"
                  prompt-accent-color: "#9696D1"

              neutral:
                display-name: "Neutral Gate"
                description: "An always-open passage available to every traveler."
                source-dimension: "elarion:lobby"
                destination-dimension: "*"
                enabled: true
                mode: "always_open"
                visual:
                  color: "#3F8FD2"
                  brightness: 1.0
                  opacity: 0.82
                  frame-time: 2
                  texture: "minecraft:block/nether_portal"
                  icon-item: ""
            """;

    public static final String UI = """
            theme-variant: "default"
            logical-width: 340
            logical-height: 190
            minimum-scale-percent: 50
            confirm-button-width: 104
            close-button-width: 104
            """;

    private PortalConfigDefaults() {
    }
}
