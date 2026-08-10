# Elarion Commands

Last reviewed: 2026-06-29

Author: Panyel  
Team: Panetina Team

This is the command surface contract. Use it as the source matrix for command
integration tests and GameTests.

## Output Style

Admin diagnostics and inspection commands should use the shared
`CommandOutput` helper. Prefer:

- a clear header
- short sections
- one labeled value per line
- explicit empty-state messages
- compact rows only for naturally small lists

Avoid long packed strings such as `a=1 b=2 c=3` for commands meant to be read
by server owners in chat. This applies especially to `/e economy pulse`,
`/e economy transactions`, `/e perf ...`, `/e realm relationship ...`,
`/e progression inspect`, `/e history ...`, `/e world info`, and future NPC,
Government, Offerings, Portal, Ledger, and Chronicle commands.

## Player Commands

```text
/help [command]
/rc <message>
/ac <message>
/pm <player> <message>
/r <message>
/w <message>
/yell <message>
/lc <message>
```

Rules:

- `/help` lists player-facing commands and descriptions.
- `/rc` sends Realm Chat to the sender's Realm.
- `/ac` sends alliance chat to the sender's connected alliance guild.
- `/pm` and `/r` follow Realm/relationship visibility rules.
- `/w` is local whisper chat, not private messaging.
- `/yell` is local yell chat and uses its configured cooldown.
- The Character Menu opens from the default `C` keybind. `/charactermenu` is a
  hidden client command for manual use and is intentionally
  not registered in the server command tree, `/help`, or slash recommendations.
  Core clears vanilla's default Save Hotbar Activator binding when it still
  owns `C`, so fresh clients do not shadow the Character Menu keybind.
- `G` requests the server-authoritative Guild screen. Players without membership
  receive its empty state; creation remains a configured Guild Registrar NPC
  action. Invitations, announcements, role assignment, emblem editing and Guild
  chat use typed server-checked UI actions.
- Vanilla `/say` is intentionally unavailable. Typed Local chat and every
  available channel route through Core's restriction checks.
- The `T` chat selector lists only server-authored eligible channels. Its
  selected channel persists for the connection and resets only on join or
  disconnect; Tab/Shift+Tab cycle that eligible list without entering a slash
  command, and PM choices are stable UUID requests behind displayed nicknames.
  Realm requires canonical membership, Guild/Alliance require their registered
  owner routes, and Global is hidden and rejected until the Worldheart portal
  projection makes it available.
- `/lc` sends Government authority chat to same-Realm authority holders.
- `/ac` remains alliance chat; it is not used for Government authority chat.
- OP level 4 does not bypass local chat distances by default.
- `/spy chat` is the explicit OP tool for seeing chat outside normal distance
  or Realm scope.
- `/e panel` opens the Core Admin Panel for in-game OP level 4 players. It is
  a server-authoritative GUI wrapper around validated Core/addon admin and test
  actions; console sources must use the explicit commands.

## OP Level 4 Commands

```text
/spy chat
/list
/seed
/random value <range>
/random roll <range>
/random reset ...
/e reload
/e panel
/e reset players
/e realm ...
/e realm announce <realm> <message...>
/e realm mail <realm> "<title>" <message...>
/e Ember ...
/e title ...
/e ability ...
/e reward ...
/e progression ...
/e history ...
/e history chronicle list [weeks]
/e history chronicle inspect <week> [limit]
/e economy wallet get <player>
/e economy wallet give|take <player> <amount>
/e economy wallet deposit|withdraw <player> <amount>
/e economy treasury get <realm>
/e economy treasury give|take <realm> <amount>
/e economy transfer player <from> <to> <amount>
/e economy transactions player <player> [limit]
/e economy transactions realm <realm> [limit]
/e economy pulse|recalculate|reload
/e offerings reload
/e offerings projects
/e offerings inspect <project>
/e offerings instances
/e offerings state <instance>
/e offerings start realm <realm> <project>
/e offerings start global <project>
/e offerings start location <project>
/e offerings shrine link <instance>
/e offerings shrine unlink
/e offerings shrine inspect
/e offerings shrine remove
/e offerings shrine repair
/e offerings delete <instance>
/e offerings reset <instance>
/e offerings complete <instance>
/e test shrine reset [realm]
/e test realm global <realm> on|off
/e quest reload
/e quest list
/e quest inspect <quest>
/e quest state <quest> [scope-key]
/e quest reset <quest> <scope-key>
/e quest bind actor <quest> <scope-key> <actor> <npcIdOrHandle>
/e quest unbind actor <quest> <scope-key> <actor>
/e quest bindings <quest> <scope-key>
/e quest validate <quest|all>
/e government reload
/e government forms
/e government inspect <form>
/e government state <realm>
/e government gates <realm>
/e government audience <realm>
/e government audience inspect <realm> <record>
/e government laws <realm>
/e government law archive <realm> <law>
/e government law restore <realm> <law>
/e government set-form <realm> <form>
/e government identity set <realm> <tag> <display-name...>
/e government founding complete <realm>
/e government authority cleanup
/e government reset <realm>
/e test government reset [realm]
/e test government advance <realm>
/e government block remove
/e government office assign <realm> <office> <player>
/e government office remove <realm> <office> <player>
/e guild reload
/e guild list
/e guild inspect <guild>
/e guild delete <guild>
/e guild transfer <guild> <player>
/e portal reload
/e portal wand
/e portal list
/e portal inspect <route>
/e portal guide <route>
/e portal setup enter <route> [x y z]
/e portal setup return
/e portal preview <neutral|nether|end|fee|blocked|return>
/e portal endpoint set <route> a_gate|a_arrival|b_gate|b_arrival
/e portal unlock|lock|remove <route>
/e portal repair <route>|all
/e portal window open <route> <duration>
/e portal window close <route>
/e portal entitlement inspect|grant|clear <player> <route>
/banish <player> <minutes> <reason...>
/banish <player> permanent <reason...>
/banish list
/unbanish <player>
/e death reload
/e death inspect <player>
/e death corpse list
/e death corpse inspect <corpseId>
/e death corpse recover <corpseId> <player>
/e death vault recover <player>
/e death underworld send <player> [minutes]
/e death underworld return <player>
/e death soul inspect <player>
/e death soul add-fracture <player>
/e death soul remove-fracture <player>
/e death soul clear-fractures <player>
/e character inspect <player>
/e character recreate-now <player>
/e character archive <player>
/e npc reload
/e npc open <npcId>
/e npc place <npcDefinition> [north|east|south|west|here]
/e npc place <npcDefinition> yaw <value>
/e npc remove <npcId>
/e npc remove nearest
/e npc face <npcId>
/e npc rotate <npcId> <north|east|south|west|here>
/e npc rotate <npcId> yaw <value>
/e npc repair <npcId|all>
/e npc tp <npcId>
/e npc duplicate <npcId> [north|east|south|west|here]
/e npc duplicate <npcId> yaw <value>
/e npc nearest
/e npc list [world|near]
/e npc list tag <tag>
/e npc inspect <npcId>
/e npc inspect nearest
/e npc move <npcId>
/e npc set name|skin|portrait|dialogue <npcId> ...
/e npc dialogue inspect <dialogueId>
/e mounts grant <player> <type>
/e mounts revoke <player> <type>
/e mounts set-active <player> <type>
/e mounts list <player>
/e world ...
/e perf status
/e perf queues
/e perf config
/e perf worlds
/e perf realms
/e perf realm <realm>
/e perf hotzones
/e perf security
/e security status
```

`/banish` is OP level 4 and requires an online target plus a non-empty reason.
It creates an Underworld-owned timed or permanent moderation sentence without
creating a corpse or normal death session. `/unbanish` accepts the stored name
of an online or offline banished player. Banished players retain movement only;
Core rejects block, entity, item, and combat interactions before addon-owned
NPC, Shrine, or future Underworld skyblock handlers execute.

Portal linking uses the `Portal Surveyor`. Attack a frame-facing block to mark
the adjacent first interior cell, then use the Surveyor on the opposite
frame-facing block to mark the adjacent second interior cell. The selected
cuboid must be one block thick on exactly one axis.

All `/e ...` commands are OP level 4 unless a future feature is explicitly
approved as player-facing.

`/e reset players` is a destructive, preview-first global reset with an
executor-bound confirmation token that expires after 60 seconds. Before any
deletion it disconnects online players and copies every registered reset
target into `world/elarion/backups/player-reset/<timestamp>/`. It removes
vanilla `world/playerdata`, `world/stats`, and `world/advancements`; clears
`ops.json`, `whitelist.json`, and the disk/in-memory `usercache.json`; and then
invokes each registered Core/addon handler for player-owned Elarion state.
Worlds, terrain, buildings, configured NPCs, placed Shrines, portals, Realm
and world definitions, and other shared infrastructure are preserved.

Each backup includes an atomic `manifest.json` listing the handler-owned,
backup-relative targets copied before deletion. Keep the complete timestamped
directory intact for recovery; the manifest is an inventory, not an automated
restore command.

After confirmation, no player remains whitelisted or operator. Re-establish
access from the server console or submit fresh approved access through the
signed website/bridge flow; the command does not replay historical bridge
commands. Do not run it from an in-game operator unless console or bridge
recovery is available.

`/e reset world <world>` is a separate OP-only managed-world reset. Its world
argument is completed from server-authored managed-world IDs; confirmation is
executor-bound. It first copies the Fantasy persistent-dimension directory and
world-scoped addon state into a timestamped backup with `manifest.json`, then
waits for Fantasy to delete the old runtime dimension before opening the new
one. The command reports completion only after the replacement world is open.
Definitions and configuration are preserved. If regeneration or a registered
world-scoped cleanup fails, it restores the persistent dimension and declared
addon state from that backup, reloads each affected owner, records rollback in
the audit log, then reports the original failure.

`/random` is Minecraft's vanilla random-number and named random-sequence
command. The bare `/random` root is incomplete; examples are
`/random value 1..10` and `/random roll 1..20`. Elarion gates the entire command
at OP level 4 so its vanilla permission-2 subcommands do not bypass the server
command policy.

NPC command IDs are readable placed IDs such as `worldheart_banker_1`, shown by
`/e npc place` and `/e npc list`. `/e npc open <npcId>` opens the normal
conversation for a nearby placed NPC through the same server-authoritative
session path as right-click interaction. `/e npc set skin <npcId> <skinProfile>` uses
profiles from `config/elarion/addons/npcs/skins.yml` for future in-world body
presentation metadata. `/e npc set portrait <npcId> <portraitProfile>` uses
profiles from `config/elarion/addons/npcs/portraits.yml` and affects the
dialogue portrait rendered in the NPC GUI.
`/e npc face <npcId>` stores a one-time yaw toward the executing administrator.
`/e npc rotate <npcId> ...` stores a fixed direction/yaw.
`/e npc repair <npcId|all>` reconciles missing, stale, or duplicate world
entities from canonical placement state.
`/e npc inspect <npcId>` also reports the resolved `realm:<id>` or
`world:<world-id>` tax jurisdiction stored on that placement.

Quest scope keys are explicit strings such as `realm:realm1`, `world:overworld`,
`player:<uuid>`, or `global`. `/e quest reset <quest> <scope-key>` clears only
that questline scope and its player records, actor bindings, and scheduled
consequences; it does not reset NPC placements, Offering progress, Government
state, or Core Embers. Actor binding commands map quest actor aliases to
placed NPC UUIDs through the NPC API while NPCs remain placement owners.

## Development Test Commands

All temporary reset and timing commands live under `/e test`. Do not add new
test-only command branches under feature commands such as `/e government ...`
or `/e offerings ...`.

```text
/e test shrine reset [realm]
/e test realm global <realm> on|off
/e test government reset [realm]
/e test government advance <realm>
/e test death send <player> <minutes>
/e test death return <player>
/e test death fracture <player>
/e test death fracture add <player>
/e test death fracture remove <player>
/e test death clear <player>
/e test death reset-state
/e test character finish-cooldown <player>
/e test character trigger-true-death <player>
/e test character reset <player>
/e test character force-active <player>
/e test mounts summon <type>
/e test mounts debug
/e test mounts clear-nearby
```

See [test-commands.md](test-commands.md) for what each reset clears and what it
preserves.

Portal setup uses A/B names:

- `a_gate`: portal frame A.
- `a_arrival`: where players appear on side A.
- `b_gate`: portal frame B.
- `b_arrival`: where players appear on side B.
- Travel rule: entering `a_gate` sends players to `b_arrival`; entering
  `b_gate` sends players to `a_arrival`.
- `/e portal setup enter <route> [x y z]`: OP-only authorized travel into a
  protected destination before the route is linked. Example:
  `/e portal setup enter nether 0 80 0`.
- `/e portal setup return`: returns to the position stored before setup travel.

## Current Coverage

The GameTest module includes a small command harness:

```text
tests/gametest/src/main/java/panetina/elarion/tests/CommandGameTestSupport.java
```

It currently covers:

- removed vanilla messaging commands
- player-facing chat command registration
- OP level 4 gating for `/e`, `/list`, `/seed`, and `/random`
- real execution of `/e history category ...`
- Economy API round-trip and real execution of treasury/pulse commands

## Test Matrix

Future command tests should cover:

- non-OP rejection for every OP command guild
- OP level 4 acceptance for every OP command guild
- player command availability for non-OP players
- `/help` entries for every player-facing command
- tab suggestions for players, Realms, titles, abilities, reward IDs, world IDs,
  progression rule IDs, and relationship values
- command error text for missing targets, invalid IDs, and failed rules
- command side effects persist after restart where runtime state is changed
- command output uses nickname/identity presentation where expected
- command output follows the readable header/section/key-value style for
  inspection and diagnostics commands

Keep command behavior covered by focused unit tests for pure helpers and by
GameTests where Minecraft server context is required. Use manual dev-server
testing only for GUI/client presentation details that cannot yet be automated.
