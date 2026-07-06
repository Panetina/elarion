# Team And Identity Presentation

Purpose: present Realm/player identity consistently across names, chat, tablist, titles, and future GUI/HUD surfaces.

Main classes: `IdentityService`, `IdentitySyncService`, `ChatService`, `TitleService`, Names/Titles/Tablist addons.

Entry points: Core identity sync and client presentation mixins.

Commands: `/e citizen ...`, `/e title ...`, chat commands.

Network packets: identity sync request/payloads.

GUI/screens: none central; presentation consumed by chat, tablist, nameplates, and future screens.

Storage/persistence: citizen records, title config, Realm config.

Dependencies: Core citizens, Realms, titles, server identity config.

Related systems: Realms, permissions/abilities, chat, public history.

Extension points: identity facade, title renderer, nickname validation.

Risks: hard-coded names; addon-specific identity formatting; duplicate team storage.

Do not duplicate this system by creating: a new team manager, separate title state, or direct username display formatting.
