# Quest System

Status: Generic package foundation implemented, authoring needed.

Purpose: provide a modular, data-driven quest package state owner that NPCs,
Shrines, rewards, Government, Chronicle, and future tools can use without
duplicating progression state.

Main classes: `ElarionQuestsAddon`, `QuestDefinitionService`,
`QuestStateService`, `QuestRegistryHandlers`, `QuestCommands`,
`QuestConfigLoader`, and `QuestStorage`.

Entry points: `addons/quests/src/main/resources/fabric.mod.json`, custom
`elarion:addon`, Core action/condition registries, NPC API for actor binding,
and `/e quest ...`.

Commands: `/e quest reload`, `/e quest list`, `/e quest inspect <quest>`,
`/e quest state <quest> [scope-key]`, `/e quest reset <quest> <scope-key>`,
`/e quest bind actor <quest> <scope-key> <actor> <npcIdOrHandle>`,
`/e quest unbind actor <quest> <scope-key> <actor>`,
`/e quest bindings <quest> <scope-key>`, and `/e quest validate <quest|all>`.

Network packets: none in V1. Quest progression is server-side and is usually
driven by NPC dialogue actions, Offering milestones, rewards, admin commands,
or future server-authoritative UIs.

Storage/persistence:

```text
config/elarion/addons/quests/questlines/<quest-id>/
config/elarion/addons/quests/questlines/<quest-id>.yml
world/elarion/addon-state/quests/state.json
```

Dependencies: Core registries, Core domain events, Core notifications, NPC API
for actor binding, and Offerings API for optional Shrine display-name
projections.

Related systems: NPCs, Offerings, Government, Core rewards, Chronicles, future
Ledger/newspapers/Atlas/dev editor.

## Ownership

Quests owns:

- quest package definitions
- shared questline flags, variables, evidence, stages, and endings
- player-scoped quest flags, variables, and seen evidence
- actor bindings from quest actor alias to placed NPC UUID
- scheduled quest consequences
- Quest-category notifications and Core domain events emitted by quest state
  transitions

Quests does not own:

- NPC placements, NPC definitions, or dialogue sessions
- Offering project progress, anchors, milestones, or Shrine blocks
- Core citizens, Realms, titles, rewards, or identity
- Government offices, laws, elections, or authority titles
- Chronicle indexes or public history archives

## Quest Packages

Folder packages are preferred for new questlines:

```text
quest.yml          # id, display name, scope, version, tags, root stage
actors.yml         # stable actor aliases and optional allowed NPC definitions
variables.yml      # shared/player variables
stages.yml         # stage/objective metadata and optional next edges
evidence.yml       # evidence metadata
endings.yml        # ending metadata and Shrine projections
conditions.yml     # reusable authoring conditions
consequences.yml   # reusable scheduled actions
authoring.yml      # editor-only graph and writer metadata
```

Legacy one-file YAML questlines still load for compatibility. The addon does
not generate lore quest content by default.

## Scope Keys

Quest definitions choose a default scope: `realm`, `global`, `world`, or
`player`. Runtime state uses explicit scope keys:

```text
realm:<realm-id>
global
world:<world-id>
player:<uuid>
```

NPCs and other callers should pass `realm`, `world`, `scope`, or `scope-key`
parameters instead of deriving scope by reading quest storage.

## Actor Binding

Quest actors are stable aliases inside quest packages. A live Realm/world run
binds an alias to a placed NPC:

```text
/e quest bind actor generic_foundation realm:realm1 guide generic_guide_1
```

The binding stores the placed NPC UUID and a handle snapshot in quest runtime
state. NPCs remain the owner of placement records and entity reconciliation.

## Registry Integration

Quests registers action IDs for starting questlines, changing stages, changing
shared/player variables and flags, collecting evidence, locking endings,
scheduling consequences, publishing Quest notifications, and requesting Shrine
display-name projections.

Quests registers condition IDs for checking stages, flags, evidence, variables,
and endings. NPC dialogue node variants and option visibility can use these
conditions to show memory-sensitive lines without NPCs storing quest state.

## Events And Notifications

Quests emits Core domain events with `sourceSystem=elarion_quests` and event
types such as:

- `quest-started`
- `quest-stage-changed`
- `quest-flag-changed`
- `quest-variable-changed`
- `quest-evidence-collected`
- `quest-ending-locked`
- `quest-consequence-scheduled`
- `quest-actor-bound`
- `quest-actor-unbound`

Quest notifications are explicit action projections through Core
notifications. The default `elarion_quests:notify` action supports `player`,
`realm`, and `world` audiences and uses the Core `QUEST` category. Ordinary
dialogue browsing does not notify.

## Performance Contract

- Definitions are parsed once into immutable maps on load/reload.
- Gameplay actions do not parse YAML.
- Runtime state uses atomic JSON writes.
- Scheduled consequences are processed from the server tick path only once per
  second and are capped to 16 due actions per interval.
- Player-facing future views must use dedicated summaries or indexes before
  they expose broad quest history/search.
