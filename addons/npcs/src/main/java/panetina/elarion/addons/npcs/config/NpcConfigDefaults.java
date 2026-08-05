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
                display-name: "Worldheart Banker"
                description: "A %capital% banker example. Bank actions are registered later by Economy."
                skin: worldheart_banker
                portrait: worldheart_banker_portrait
                dialogue: worldheart_banker
                faction: worldheart
                tax-jurisdiction: "world:elarion:worldheart"
                tags:
                  - bank
                  - worldheart
                  - service
                required-ability: ""
                # 0 means use ui.yml default-interaction-range-blocks.
                interaction-range-blocks: 0
                enabled: true
              worldheart_trader:
                display-name: "Worldheart Trader"
                description: "A %capital% trader example with server-authored stock and prices."
                skin: worldheart_trader
                portrait: worldheart_trader_portrait
                dialogue: worldheart_trader
                faction: worldheart
                trade-catalog: worldheart_trader
                tax-jurisdiction: "world:elarion:worldheart"
                tags:
                  - trade
                  - worldheart
                  - service
                required-ability: ""
                interaction-range-blocks: 0
                enabled: true
              guildmaster:
                display-name: "Guildmaster"
                description: "The Worldheart registrar for new Guild charters."
                skin: guildmaster
                portrait: guildmaster_portrait
                dialogue: guildmaster
                faction: worldheart
                tax-jurisdiction: "world:elarion:worldheart"
                tags:
                  - guild
                  - service
                  - worldheart
                required-ability: ""
                interaction-range-blocks: 0
                enabled: true
            """;

    public static final String TRADES = """
            # NPC merchant catalogs. Rows are server-authored; clients only
            # request quotes and confirmations.
            config-version: 1
            trades:
              worldheart_trader:
                offers:
                  - id: nether_gate_ticket_bundle
                    direction: buy
                    label: "Nether Gate Ticket"
                    subtitle: "2 ticket bundle"
                    item: "elarion:portal_ticket"
                    count: 2
                    custom-model-data: 1
                    price-key: "portal.ticket.nether"
                    custom-name: "Nether Gate Ticket"
                    lore:
                      - "Grants one Nether Gate passage."
                    price: 25
                    stock-limit: 12
                    restock-amount: 4
                    restock-interval-seconds: 1800
                    enabled: true
                  - id: end_gate_ticket_bundle
                    direction: buy
                    label: "End Gate Ticket"
                    subtitle: "2 ticket bundle"
                    item: "elarion:portal_ticket"
                    count: 2
                    custom-model-data: 2
                    price-key: "portal.ticket.end"
                    custom-name: "End Gate Ticket"
                    lore:
                      - "Grants one End Gate passage."
                    price: 40
                    stock-limit: 8
                    restock-amount: 2
                    restock-interval-seconds: 3600
                    enabled: true
                  - id: cobblestone
                    direction: buy
                    label: "Cobblestone"
                    subtitle: "1 block"
                    item: "minecraft:cobblestone"
                    count: 1
                    price: 1
                    stock-limit: 256
                    restock-amount: 64
                    restock-interval-seconds: 900
                    enabled: true
                  - id: cobblestone_buyback
                    direction: sell
                    label: "Cobblestone"
                    subtitle: "Trader buys clean stone"
                    item: "minecraft:cobblestone"
                    count: 1
                    price-key: "npc.sell.cobblestone"
                    price: 1
                    sell-match: exact_item
                    component-policy: vanilla_only
                    max-quantity: 64
                    stock-destination: placed_npc
                    destination-offer: cobblestone
                    enabled: true
                  - id: protection_armor
                    direction: buy
                    label: "Protection IV Armor"
                    subtitle: "Diamond chestplate"
                    item: "minecraft:diamond_chestplate"
                    count: 1
                    enchantments:
                      - id: "minecraft:protection"
                        level: 4
                    price: 250
                    stock-limit: 4
                    restock-amount: 1
                    restock-interval-seconds: 7200
                    enabled: true
                  - id: bulwark_of_the_gate
                    direction: buy
                    label: "Bulwark of the Gate"
                    subtitle: "Named armour with lore"
                    item: "minecraft:diamond_chestplate"
                    count: 1
                    custom-name: "Bulwark of the Gate"
                    lore:
                      - "A merchant-marked plate with old Nether ash in the seams."
                    enchantments:
                      - id: "minecraft:protection"
                        level: 4
                    price: 375
                    stock-limit: 1
                    restock-amount: 1
                    restock-interval-seconds: 14400
                    enabled: true
            """;

    public static final String SKINS = """
            # Skin/profile definitions used by visible in-world NPC presentation.
            # Supported types: placeholder, texture, player_body.
            config-version: 1
            skins:
              placeholder_body:
                display-name: "Placeholder Body"
                type: "placeholder"
                texture: ""
                player-name: ""
                fallback-type: "placeholder"
                fallback-texture: ""
                adapter: ""
              guildmaster:
                display-name: "Guildmaster"
                type: "texture"
                texture: "elarion_npcs:textures/entity/npc/guildmaster.png"
                player-name: ""
                fallback-type: "placeholder"
                fallback-texture: ""
                adapter: ""
              configured_player_body:
                display-name: "Configured Player Body"
                type: "player_body"
                texture: ""
                player-name: "Panyel"
                fallback-type: "placeholder"
                fallback-texture: ""
                adapter: ""
              worldheart_banker:
                display-name: "Worldheart Banker"
                type: "texture"
                texture: "elarion:textures/entity/npc/worldheart_banker.png"
                player-name: ""
                fallback-type: "texture"
                fallback-texture: "elarion:textures/entity/npc/worldheart_banker.png"
                adapter: ""
              worldheart_trader:
                display-name: "Worldheart Trader"
                type: "texture"
                texture: "elarion:textures/entity/npc/worldheart_trader.png"
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
              placeholder_portrait:
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
              worldheart_banker_portrait:
                display-name: "Worldheart Banker Portrait"
                type: "texture"
                texture: "elarion_core:textures/gui/library/portraits/32x32/portrait_character_portrait_icons_03_icons_03.png"
                player-name: ""
                fallback-type: "placeholder"
                fallback-texture: ""
              worldheart_trader_portrait:
                display-name: "Worldheart Trader Portrait"
                type: "texture"
                texture: "elarion_core:textures/gui/library/portraits/32x32/portrait_character_portrait_icons_27_icons_27.png"
                player-name: ""
                fallback-type: "placeholder"
                fallback-texture: ""
              guildmaster_portrait:
                display-name: "Guildmaster Portrait"
                type: "texture"
                texture: "elarion_npcs:textures/gui/portraits/guildmaster.png"
                player-name: ""
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
                  - id: open_bank
                    button-text: "Open Bank"
                    player-text: "I would like to use the bank."
                    presentation-role: "open_bank"
                    next: bank
                  - id: lore
                    button-text: "What are %currency_plural%?"
                    player-text: "What are %currency_plural%?"
                    next: currency
              bank:
                presentation: bank
                text: "Deposit carried %currency_plural% or withdraw from your account."
                sound: "minecraft:entity.villager.yes"
                voice: ""
                options:
                  - id: deposit
                    button-text: "Deposit %currency_plural%."
                    player-text: "I would like to Deposit %currency_plural%."
                    presentation-role: "deposit"
                    sound: "minecraft:ui.button.click"
                    voice: ""
                    prompt:
                      type: "number"
                      question: "How many %currency_plural% would you like to deposit?"
                      action: "elarion:economy_deposit_currency_amount"
                      max-digits: 10
                      min-amount: 1
                    next: bank
                  - id: withdraw
                    button-text: "Withdraw %currency_plural%."
                    player-text: "I would like to Withdraw %currency_plural%."
                    presentation-role: "withdraw"
                    sound: "minecraft:ui.button.click"
                    voice: ""
                    prompt:
                      type: "number"
                      question: "How many %currency_plural% would you like to withdraw?"
                      action: "elarion:economy_withdraw_currency_amount"
                      max-digits: 10
                      min-amount: 1
                    next: bank
                  - id: back
                    button-text: "Back to Conversation"
                    player-text: "Let us speak instead."
                    presentation-role: "back"
                    next: intro
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

    public static final String TRADER_DIALOGUE = """
            config-version: 1
            id: worldheart_trader
            root: intro
            nodes:
              intro:
                text: "I keep a small table of useful goods for travelers. Browse stock, buy what you need, or sell clean materials."
                sound: "minecraft:entity.villager.trade"
                voice: ""
                options:
                  - id: open_trade
                    button-text: "Trade"
                    player-text: "Show me what you trade."
                    presentation-role: "open_trade"
                    next: trade
                  - id: ask_goods
                    button-text: "What do you sell?"
                    player-text: "What kind of goods do you keep?"
                    next: goods
              trade:
                presentation: trade
                text: "Browse goods or prepare items to sell. Stock, prices, and trades stay server-authoritative."
                sound: "minecraft:entity.villager.trade"
                voice: ""
                options:
                  - id: buy
                    button-text: "Buy Goods"
                    player-text: "I want to buy goods."
                    presentation-role: "buy"
                    next: trade
                  - id: sell
                    button-text: "Sell Goods"
                    player-text: "I want to sell goods."
                    presentation-role: "sell"
                    next: trade
                  - id: back
                    button-text: "Back to Conversation"
                    player-text: "Let us speak instead."
                    presentation-role: "back"
                    next: intro
              goods:
                text: "My stall shows stock, prices, and accepted buyback items. The Worldheart ledger records every trade."
                sound: "minecraft:entity.villager.yes"
                voice: ""
                options:
                  - id: open_trade
                    button-text: "Open Trade"
                    player-text: "Let me see the stall."
                    presentation-role: "open_trade"
                    next: trade
                  - id: back
                    button-text: "Back."
                    player-text: "Back to the start."
                    next: intro
            """;

    public static final String GUILDMASTER_DIALOGUE = """
            config-version: 1
            id: guildmaster
            root: welcome
            nodes:
              welcome:
                text: "I can register a public or secret Guild, or open your existing Guild records. The Registrar will show the current charter fee before you confirm."
                sound: "minecraft:entity.villager.yes"
                voice: ""
                options:
                  - id: open_registrar
                    button-text: "Register or manage my Guild"
                    player-text: "I want to register or manage a Guild."
                    actions:
                      - type: "elarion_guilds:open_registrar"
                    close: true
                  - id: charter
                    button-text: "What is a Guild charter?"
                    player-text: "What does a charter mean?"
                    next: charter
              charter:
                text: "A charter records your Guild's name and leadership. Secret Guilds remain hidden from public projections."
                sound: "minecraft:entity.villager.ambient"
                voice: ""
                options:
                  - id: back
                    button-text: "Back"
                    player-text: "I understand."
                    next: welcome
            """;
}
