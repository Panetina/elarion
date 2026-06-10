package panetina.elarion.core.config;

import java.util.Map;

final class CoreConfigDefaultFiles {
    private CoreConfigDefaultFiles() {
    }

    static final Map<String, String> FILES = Map.ofEntries(
            Map.entry("realms.yml", """
                    config-version: 1

                    # Supported realm colors:
                    # black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple
                    # gold, gray, dark_gray, blue, green, aqua, red
                    # light_purple, yellow, white
                    #
                    # Invalid color names fall back to white.

                    realms:
                      oak:
                        display-name: "Kingdom of Oak"
                        short-name: "OAK"
                        prefix: "[OAK]"
                        color: "green"
                        visibility-scope: "REALM"
                        spawn:
                          world: "elarion:realm_world_1"
                          x: 0
                          y: 64
                          z: 0
                          yaw: 0
                          pitch: 0
                        # Optional flags:
                        # - diplomacy-excluded: keep this Realm out of war, alliance,
                        #   embargo, and Realm decision targets without hiding its members.
                        flags: []
                      sky:
                        display-name: "Kingdom of Sky"
                        short-name: "SKY"
                        prefix: "[SKY]"
                        color: "blue"
                        visibility-scope: "REALM"
                        spawn:
                          world: "elarion:realm_world_2"
                          x: 0
                          y: 64
                          z: 0
                          yaw: 0
                          pitch: 0
                        flags: [ ]
                      earth:
                        display-name: "Kingdom of Earth"
                        short-name: "EARTH"
                        prefix: "[EARTH]"
                        color: "gold"
                        visibility-scope: "REALM"
                        spawn:
                          world: "elarion:realm_world_3"
                          x: 0
                          y: 64
                          z: 0
                          yaw: 0
                          pitch: 0
                        flags: [ ]
                    """),
            Map.entry("titles.yml", """
                    config-version: 1

                    titles:
                      citizen:
                        description: "A citizen of Elarion."
                        display-name: "Citizen"
                        prefix: ""
                        suffix: ""
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
                        priority: 30
                        visible-under-username: true
                        acquisition-mode: "ADMIN_ONLY"
                        ownership-mode: "UNLIMITED"
                        hidden-from-discovery: true
                        abilities:
                          - "elarion.portal.foreign_access"
                      goblin_slayer:
                        description: "Earned after slaying enough configured goblins."
                        display-name: "Goblin Slayer"
                        prefix: ""
                        suffix: ""
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
                        priority: 50
                        visible-under-username: true
                        acquisition-mode: "PROGRESSION"
                        ownership-mode: "UNLIMITED"
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
                        - "elarion"
                    nickname-protection:
                      enabled: true
                      protect-realm-presentation: true
                      protect-title-presentation: true
                      reject-containing-protected-name: true
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
                      format: "[Local] %player% \u00bb %message%"
                    whisper-chat:
                      command: "w"
                      radius: 4
                      format: "[Local] %player% whispers: %message%"
                    yell-chat:
                      command: "yell"
                      radius: 128
                      cooldown-seconds: 300
                      format: "[Local] %player% yells: %message%"
                    realm-chat:
                      command: "rc"
                      format: "[Realm] %player% \u00bb %message%"
                    alliance-chat:
                      command: "ac"
                      format: "[Alliance:%realm_short%] %player% \u00bb %message%"
                    notices:
                      scoped-join-leave: true
                      realm-format: "%player% joined your Realm."
                      admin-format: "%player% joined realm %realm%."
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
                            text: "Welcome to Elarion."
                      starter_diamonds:
                        actions:
                          - type: "item"
                            id: "minecraft:diamond"
                            count: 3
                          - type: "message"
                            text: "Your Realm received starter diamonds."
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

                    public-query:
                      default-weeks: 8
                      default-limit: 50
                      max-limit: 200

                    # Current Core categories include:
                    # citizen, progression, realm, diplomacy, leadership,
                    # reward, world, administration, title, security
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
