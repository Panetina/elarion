package panetina.elarion.addons.npcs.config;

public final class NpcConfigDefaults {
    private NpcConfigDefaults() {
    }

    public static final String NPCS = """
            # Elarion NPC definitions.
            # NPCs are static and event-driven by default. They do not own Economy,
            # Quest, Government, Ledger, Portal, Title, or Realm state.
            # skin controls future in-world body/profile presentation.
            # portrait controls the dialogue portrait shown in the NPC GUI.
            config-version: 1
            npcs:
              worldheart_banker:
                display-name: "Mara, Keeper of %currency_plural%"
                description: "A %capital% banker example. Bank actions are registered later by Economy."
                skin: dunk_banker
                portrait: mara_portrait
                dialogue: worldheart_banker
                tags:
                  - bank
                  - worldheart
                  - service
                required-ability: ""
                # 0 means use ui.yml default-interaction-range-blocks.
                interaction-range-blocks: 0
                enabled: true
            """;

    public static final String SKINS = """
            # Skin/profile definitions used by visible in-world NPC presentation.
            # Supported types: placeholder, texture, player_body.
            config-version: 1
            skins:
              mara_skin:
                display-name: "Mara Placeholder Body"
                type: "placeholder"
                texture: ""
                player-name: ""
                fallback-type: "placeholder"
                fallback-texture: ""
                adapter: ""
              mara_player_body:
                display-name: "Mara Player Body"
                type: "player_body"
                texture: ""
                player-name: "Panyel"
                fallback-type: "placeholder"
                fallback-texture: ""
                adapter: ""
              dunk_banker:
                display-name: "Dunk Banker"
                type: "texture"
                texture: "elarion:textures/entity/npc/dunk_banker.png"
                player-name: ""
                fallback-type: "placeholder"
                fallback-texture: ""
                adapter: ""
            """;

    public static final String PORTRAITS = """
            # Portraits shown inside NPC dialogue screens.
            # Supported types: placeholder, texture, player_head.
            config-version: 1
            portraits:
              mara_portrait:
                display-name: "Skin Head Fallback"
                type: "placeholder"
                texture: ""
                player-name: ""
                fallback-type: "placeholder"
                fallback-texture: ""
              player_head:
                display-name: "Configured Player Head"
                type: "player_head"
                texture: ""
                player-name: "Panyel"
                fallback-type: "placeholder"
                fallback-texture: ""
            """;

    public static final String UI = """
            # NPC dialogue GUI presentation.
            # Colors use ARGB hex. Example: 0xFF101010.
            config-version: 1
            panel-width: 420
            min-panel-height: 250
            max-panel-height: 340
            # The panel uses these values as its logical canvas and scales as one
            # unit when the client window is smaller.
            minimum-ui-scale-percent: 60
            option-row-height: 18
            visible-option-rows: 6
            scrollbar-width: 6
            padding: 16
            button-height: 20
            compact-button-height: 16
            button-gap: 4
            content-gap: 8
            npc-row-height: 76
            player-row-height: 56
            option-columns-wide: 2
            show-portrait-reference: true
            show-relation-bar: true
            show-action-feedback-in-gui: true
            also-send-action-feedback-to-chat: false
            default-interaction-range-blocks: 6
            portrait-size: 64
            player-portrait-size: 36
            typing-enabled: true
            typing-characters-per-second: 45
            typing-click-completes: true
            typing-sound-enabled: false
            typing-sound-interval-characters: 4
            # Colors and textures come from core/ui_theme.yml variant "npc".
            """;

    public static final String BANKER_DIALOGUE = """
            config-version: 1
            id: worldheart_banker
            root: intro
            nodes:
              intro:
                text: "Welcome to the %treasury%. %currency_plural% bear the %seal% and are recognized by every %realm_term%."
                sound: "minecraft:entity.villager.yes"
                voice: ""
                options:
                  # Normal option: sends the player to another dialogue node.
                  # Use button-text for the clickable label and player-text for
                  # what appears in the player dialogue bubble after selection.
                  - id: deposit
                    button-text: "Deposit %currency_plural%."
                    player-text: "I would like to Deposit %currency_plural%."
                    sound: "minecraft:ui.button.click"
                    voice: ""
                    prompt:
                      type: "number"
                      question: "How many %currency_plural% would you like to deposit?"
                      action: "elarion:economy_deposit_currency_amount"
                      max-digits: 10
                      min-amount: 1
                    next: intro
                  - id: withdraw
                    button-text: "Withdraw %currency_plural%."
                    player-text: "I would like to Withdraw %currency_plural%."
                    sound: "minecraft:ui.button.click"
                    voice: ""
                    prompt:
                      type: "number"
                      question: "How many %currency_plural% would you like to withdraw?"
                      action: "elarion:economy_withdraw_currency_amount"
                      max-digits: 10
                      min-amount: 1
                    next: intro
                  - id: lore
                    button-text: "What are %currency_plural%?"
                    player-text: "What are %currency_plural%?"
                    next: currency
              currency:
                text: "The %treasury% mints official currency known as %currency_plural%. Every %currency% bears the %seal%, the ancient mark recognized by every %realm_term%."
                sound: "minecraft:entity.villager.yes"
                voice: ""
                options:
                  - id: back
                    button-text: "Back."
                    player-text: "I understand. Back to banking."
                    next: intro
            """;
}
