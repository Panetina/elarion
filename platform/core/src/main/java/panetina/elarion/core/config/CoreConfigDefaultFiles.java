package panetina.elarion.core.config;

import java.util.Map;

final class CoreConfigDefaultFiles {
    private CoreConfigDefaultFiles() {
    }

    static final Map<String, String> FILES = Map.ofEntries(
            Map.entry("ui_theme.yml", """
                    config-version: 1

                    defaults:
                      logical-width: 480
                      logical-height: 340
                      minimum-scale-percent: 60
                      font-scale-percent: 100
                      padding: 16
                      gap: 8
                      row-height: 18
                      button-height: 18
                      scrollbar-width: 6

                    variants:
                        default:
                          colors:
                            panel: "0xFF17130F"
                            header: "0xFF2B2116"
                            inset: "0xFF211A12"
                            border: "0xFFC08A32"
                            bevel-highlight: "0xFF8F6A32"
                            bevel-shadow: "0xFF080604"
                            background-overlay: "0x66000000"
                            title: "0xFFFFD27A"
                            text: "0xFFFFFFFF"
                            muted: "0xFFC2AE82"
                            success: "0xFF80FF80"
                            warning: "0xFFFFC766"
                            error: "0xFFFF7777"
                            disabled: "0xFF262626"
                            button: "0xFF46341D"
                            button-hover: "0xFF684C25"
                            card: "0xFF261E15"
                            progress-background: "0xFF181818"
                            progress-fill: "0xFFD69A35"
                            progress-complete: "0xFF70C060"
                            scrollbar-track: "0xFF201C18"
                            scrollbar-thumb: "0xFFC08A32"
                          textures:
                            panel: "elarion:textures/gui/shared/panel_parchment.png"
                            card: ""
                            mode: "tiled"
                            tint: "0x11FFFFFF"
                        npc:
                          extends: "default"
                        shrine:
                          extends: "default"
                      """),
            Map.entry("server_identity.yml", """
                    config-version: 1

                    # Server identity and player-facing terms.
                    # Runtime text can use these placeholders:
                    # %server%, %capital%, %treasury%, %seal%
                    # %realm_term%, %realms_term%
                    # %currency%, %currency_plural%
                    # %offering%, %offerings%, %shrine_of_foundation%
                    # %local_chat%, %realm_chat%, %alliance_chat%
                    # Add _upper or _lower before the closing %, for example:
                    # %currency_upper%, %realm_term_lower%, %server_upper%.
                    #
                    # Realm display names, short tags, prefixes, colors, and
                    # spawns stay in realms.yml because they are Realm
                    # definitions, not generic server identity.
                    identity:
                      server-name: "Elarion"
                      capital-name: "Worldheart"
                      treasury-name: "Worldheart Treasury"
                      seal-name: "Elarion Seal"

                    terms:
                      realm-singular: "Realm"
                      realm-plural: "Realms"
                      currency-singular: "Sigil"
                      currency-plural: "Sigils"
                      offering-singular: "Offering"
                      offering-plural: "Offerings"
                      shrine-of-foundation: "Shrine of Foundation"

                    chat-labels:
                      local: "Local"
                      realm: "Realm"
                      alliance: "Alliance"
                    """),
            Map.entry("activity.yml", """
                    config-version: 1

                    citizens:
                      inactivity-days: 14
                    """),
            Map.entry("realms.yml", """
                    config-version: 1

                    # Supported realm colors:
                    # black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple
                    # gold, gray, dark_gray, blue, green, aqua, red
                    # light_purple, yellow, white
                    #
                    # Invalid color names fall back to white.

                    realms:
                      realm1:
                        display-name: "Wilderness I"
                        short-name: "R1"
                        prefix: "[R1]"
                        color: "green"
                        visibility-scope: "REALM"
                        spawn:
                          world: "elarion:realm_world_1"
                          x: -367
                          y: 75
                          z: 138
                          yaw: 0
                          pitch: 0
                        # Optional flags:
                        # - diplomacy-excluded: keep this Realm out of war, alliance,
                        #   embargo, and Realm decision targets without hiding its members.
                        flags: []
                      realm2:
                        display-name: "Wilderness II"
                        short-name: "R2"
                        prefix: "[R2]"
                        color: "blue"
                        visibility-scope: "REALM"
                        spawn:
                          world: "elarion:realm_world_2"
                          x: 3000
                          y: 128
                          z: 3920
                          yaw: 0
                          pitch: 0
                        flags: [ ]
                      realm3:
                        display-name: "Wilderness III"
                        short-name: "R3"
                        prefix: "[R3]"
                        color: "gold"
                        visibility-scope: "REALM"
                        spawn:
                          world: "elarion:realm_world_3"
                          x: 6061
                          y: 84
                          z: 5122
                          yaw: 0
                          pitch: 0
                        flags: [ ]
                    """),
            Map.entry("titles.yml", """
                    config-version: 1

                    titles:
                      citizen:
                        description: "An Ember of %server%."
                        display-name: "Ember"
                        prefix: ""
                        suffix: ""
                        color: "#C9C9C9"
                        priority: 0
                        visible-under-username: true
                        acquisition-mode: "DEFAULT"
                        ownership-mode: "UNLIMITED"
                        hidden-from-discovery: false
                        abilities: []
                      news_reporter:
                        description: "Trusted role for newspaper publishing."
                        display-name: "News Reporter"
                        prefix: ""
                        suffix: ""
                        color: "#9CC8FF"
                        priority: 20
                        visible-under-username: true
                        acquisition-mode: "ADMIN_ONLY"
                        ownership-mode: "UNLIMITED"
                        hidden-from-discovery: true
                        abilities:
                          - "elarion.newspaper.publish"
                      diplomat:
                        description: "Trusted role for foreign portal access."
                        display-name: "Diplomat"
                        prefix: ""
                        suffix: ""
                        color: "#9CC8FF"
                        priority: 30
                        visible-under-username: true
                        acquisition-mode: "ADMIN_ONLY"
                        ownership-mode: "UNLIMITED"
                        hidden-from-discovery: true
                        abilities:
                          - "elarion.portal.foreign_access"
                      government_monarch:
                        description: "Active authority title for a Realm monarch."
                        display-name: "Monarch"
                        prefix: ""
                        suffix: ""
                        color: "#FFD36A"
                        priority: 70
                        visible-under-username: true
                        acquisition-mode: "ADMIN_ONLY"
                        ownership-mode: "UNLIMITED"
                        hidden-from-discovery: true
                        abilities: []
                      government_heir:
                        description: "Active authority title for a Realm heir."
                        display-name: "Heir"
                        prefix: ""
                        suffix: ""
                        color: "#E6B45A"
                        priority: 60
                        visible-under-username: true
                        acquisition-mode: "ADMIN_ONLY"
                        ownership-mode: "UNLIMITED"
                        hidden-from-discovery: true
                        abilities: []
                      government_president:
                        description: "Active authority title for a Realm president."
                        display-name: "President"
                        prefix: ""
                        suffix: ""
                        color: "#FFD36A"
                        priority: 70
                        visible-under-username: true
                        acquisition-mode: "ADMIN_ONLY"
                        ownership-mode: "UNLIMITED"
                        hidden-from-discovery: true
                        abilities: []
                      government_councilor:
                        description: "Active authority title for a Realm councilor."
                        display-name: "Councilor"
                        prefix: ""
                        suffix: ""
                        color: "#58D1A5"
                        priority: 60
                        visible-under-username: true
                        acquisition-mode: "ADMIN_ONLY"
                        ownership-mode: "UNLIMITED"
                        hidden-from-discovery: true
                        abilities: []
                      government_high_cleric:
                        description: "Active authority title for a Realm theocratic leader."
                        display-name: "Holy Priest"
                        prefix: ""
                        suffix: ""
                        color: "#C084FF"
                        priority: 70
                        visible-under-username: true
                        acquisition-mode: "ADMIN_ONLY"
                        ownership-mode: "UNLIMITED"
                        hidden-from-discovery: true
                        abilities: []
                      government_synod_member:
                        description: "Active authority title for a Realm synod member."
                        display-name: "Synod Member"
                        prefix: ""
                        suffix: ""
                        color: "#C084FF"
                        priority: 60
                        visible-under-username: true
                        acquisition-mode: "ADMIN_ONLY"
                        ownership-mode: "UNLIMITED"
                        hidden-from-discovery: true
                        abilities: []
                      government_delegate:
                        description: "Active authority title for a confederation delegate."
                        display-name: "Delegate"
                        prefix: ""
                        suffix: ""
                        color: "#E6B45A"
                        priority: 70
                        visible-under-username: true
                        acquisition-mode: "ADMIN_ONLY"
                        ownership-mode: "UNLIMITED"
                        hidden-from-discovery: true
                        abilities: []
                      government_officer:
                        description: "Active authority title for an appointed Realm officer."
                        display-name: "Officer"
                        prefix: ""
                        suffix: ""
                        color: "#5CB7E8"
                        priority: 50
                        visible-under-username: true
                        acquisition-mode: "ADMIN_ONLY"
                        ownership-mode: "UNLIMITED"
                        hidden-from-discovery: true
                        abilities: []
                      goblin_slayer:
                        description: "Earned after slaying enough configured goblins."
                        display-name: "Goblin Slayer"
                        prefix: ""
                        suffix: ""
                        color: "#8DDCFF"
                        priority: 40
                        visible-under-username: true
                        acquisition-mode: "PROGRESSION"
                        ownership-mode: "UNLIMITED"
                        hidden-from-discovery: false
                        abilities: []
                      dragon_slayer:
                        description: "A globally unique title for the first qualifying dragon kill."
                        display-name: "Dragon Slayer"
                        prefix: ""
                        suffix: ""
                        color: "#FFA83D"
                        priority: 100
                        visible-under-username: true
                        acquisition-mode: "PROGRESSION"
                        ownership-mode: "GLOBALLY_UNIQUE"
                        hidden-from-discovery: true
                        abilities: []
                      aquatic:
                        description: "Earned by living underwater for the configured duration."
                        display-name: "Aquatic"
                        prefix: ""
                        suffix: ""
                        color: "#5ED1D1"
                        priority: 50
                        visible-under-username: true
                        acquisition-mode: "PROGRESSION"
                        ownership-mode: "ONE_PER_PLAYER"
                        hidden-from-discovery: false
                        abilities:
                          - "elarion.survival.aquatic"
                        active-effects:
                          - type: "status_effect"
                            id: "minecraft:water_breathing"
                            amplifier: 0
                            show-particles: false
                            show-icon: true
                      maze_runner:
                        description: "Example unique discovery title for reaching a configured maze end region."
                        display-name: "Maze Runner"
                        prefix: ""
                        suffix: ""
                        color: "#FFA83D"
                        priority: 80
                        visible-under-username: true
                        acquisition-mode: "PROGRESSION"
                        ownership-mode: "GLOBALLY_UNIQUE"
                        hidden-from-discovery: true
                        abilities: []
                    """),
            Map.entry("title-progression.yml", """
                    config-version: 1

                    # Match vanilla or modded content with exact registry IDs or tags.
                    # Examples:
                    #   entities: ["minecraft:zombie", "#c:undead", "modid:goblin"]
                    #   blocks: ["minecraft:wheat", "#c:crops"]
                    #   items: ["minecraft:diamond", "#c:ingots/iron"]
                    #   recipes: ["minecraft:diamond_sword", "modid:special_blade"]
                    #
                    # Continuous rules are sampled only for players who need them.
                    # Duration units: ticks, minecraft_days, real_minutes, real_days.

                    regions:
                      maze_end:
                        world: "elarion:worldheart"
                        min-x: -5
                        min-y: 60
                        min-z: -5
                        max-x: 5
                        max-y: 80
                        max-z: 5

                    rules:
                      goblin_slayer:
                        title: "goblin_slayer"
                        trigger: "entity-kill"
                        stat-key: "modded_goblin_kills"
                        threshold: 1000
                        amount: 1
                        entities:
                          - "#elarion:goblins"
                          - "minecraft:zombie"
                      dragon_slayer:
                        title: "dragon_slayer"
                        trigger: "entity-kill"
                        entities:
                          - "minecraft:ender_dragon"
                      aquatic:
                        title: "aquatic"
                        trigger: "continuous"
                        continuous:
                          duration: 3
                          duration-unit: "minecraft_days"
                          sample-interval-ticks: 100
                          reset-on-failure: true
                          required-metadata:
                            - "underwater"
                      maze_runner:
                        title: "maze_runner"
                        trigger: "region-enter"
                        regions:
                          - "maze_end"
                    """),
            Map.entry("abilities.yml", """
                    config-version: 1

                    abilities:
                      elarion.newspaper.publish:
                        description: "Publish and manage newspapers."
                      elarion.portal.foreign_access:
                        description: "Use portals belonging to another realm."
                    """),
            Map.entry("identity.yml", """
                    config-version: 1

                    nickname:
                      enabled: true
                      max-length: 32
                    nickname-policy:
                      # Comparison always ignores capitalization, whitespace, and
                      # common separators. Submitted nicknames may contain only
                      # letters, spaces, apostrophes, and hyphens. Every name
                      # segment is title-cased.
                      unique: true
                      reserve-player-usernames: true
                      reserved-names:
                        - "admin"
                        - "administrator"
                        - "server"
                        - "system"
                        - "console"
                        - "operator"
                        - "moderator"
                        - "%server_lower%"
                    nickname-protection:
                      enabled: true
                      protect-realm-presentation: true
                      protect-title-presentation: true
                      reject-containing-protected-name: false
                    title:
                      render-under-username: true
                    """),
            Map.entry("chat.yml", """
                    config-version: 1

                    local-chat:
                      enabled: true
                      radius: 64
                      same-world-only: true
                      # Enables the OP-only /spy chat toggle. OPs do not spy
                      # automatically.
                      admin-spy: true
                      format: "[%local_chat%] %player% \u00bb %message%"
                    whisper-chat:
                      command: "w"
                      radius: 4
                      format: "[%local_chat%] %player% whispers: %message%"
                    yell-chat:
                      command: "yell"
                      radius: 128
                      cooldown-seconds: 300
                      format: "[%local_chat%] %player% yells: %message%"
                    realm-chat:
                      command: "rc"
                      format: "[%realm_chat%] %player% \u00bb %message%"
                    alliance-chat:
                      command: "ac"
                      format: "[%alliance_chat%:%realm_short%] %player% \u00bb %message%"
                    notices:
                      scoped-join-leave: true
                      realm-format: "%player% joined your %realm_term%."
                      admin-format: "%player% joined %realm_term_lower% %realm%."
                    """),
            Map.entry("visibility.yml", """
                    config-version: 1

                    defaults:
                      scope: "REALM"
                      operators-visible: true
                    """),
            Map.entry("rewards.yml", """
                    config-version: 1

                    rewards:
                      welcome:
                        actions:
                          - type: "message"
                            text: "Welcome to %server%."
                      starter_diamonds:
                        actions:
                          - type: "item"
                            id: "minecraft:diamond"
                            count: 3
                          - type: "message"
                            text: "Your %realm_term% received starter diamonds."
                    """),
            Map.entry("history.yml", """
                    config-version: 1

                    recording:
                      enabled: true

                      # If true, categories are recorded unless listed in
                      # disabled-categories. If false, only enabled-categories
                      # are recorded.
                      default-category-enabled: true
                      enabled-categories: []
                      # Chat is noisy and can grow history quickly. Enable it
                      # only if you want chat auditing.
                      disabled-categories:
                        - "chat"

                      # If true, event types are recorded unless listed in
                      # disabled-types. If false, only enabled-types are
                      # recorded. Type filters may be plain, such as
                      # "realm-assigned", or scoped, such as
                      # "citizen:realm-assigned".
                      default-type-enabled: true
                      enabled-types: []
                      disabled-types: []

                    query:
                      # History commands and ordinary API reads scan newest
                      # monthly JSONL files first and stop after this many
                      # months. Raise only when you need older live history;
                      # weekly Chronicles and public views use archive/index data.
                      max-months-scanned: 3
                      command-limit-max: 100

                    archive:
                      enabled: true
                      max-completed-weeks-per-generation: 8
                      chronicle-categories:
                        - "realm"
                        - "realm-decision"
                        - "diplomacy"
                        - "leadership"
                        - "title"
                        - "reward"
                        - "world"
                        - "administration"
                        - "security"
                        - "npc"

                    public-query:
                      default-weeks: 8
                      default-limit: 50
                      max-limit: 200

                    # Current Core categories include:
                    # citizen, progression, realm, diplomacy, leadership,
                    # reward, world, administration, title, security, npc
                    """),
            Map.entry("commands.yml", """
                    config-version: 1

                    commands:
                      admin-root: "e"
                      admin-permission-level: 4
                      realm-chat-root: "rc"
                    """),
            Map.entry("citizens-defaults.yml", """
                    config-version: 1

                    defaults:
                      status: "ACTIVE"
                      title: "citizen"
                      flags: []
                    """)
    );
}
