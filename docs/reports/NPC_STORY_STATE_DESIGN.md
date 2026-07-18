# NPC Story State Design

Date: 2026-07-11

Status: Phase 9 Slices 5-6 complete.

## Ownership

NPCs owns durable narrative state. Core does not store NPC flags, used choices,
endings, re-entry nodes, or relationship scores. State is keyed by player UUID
plus placed NPC UUID so two placed instances of one definition can have distinct
stories.

## Persistence

Runtime file:

- `world/elarion/addon-state/npcs/story-state.json`
- schema version `1`

Each record stores:

- player UUID
- placed NPC UUID
- stable story flags
- stable used-choice keys (`dialogue/node/option`)
- optional ending id
- optional re-entry node id
- update timestamp

Missing files default to empty state. Unsupported schema versions fail clearly
instead of silently discarding narrative progress. Mutations pass through
`NpcStoryStateService` and use checked atomic writes.

## Authoring Contract

Dialogue options may declare:

```yaml
one-time: true
```

The server hides and rejects a consumed one-time option. It marks the option
used only after every authored action succeeds.

Registered actions:

- `elarion_npcs:set_story_flag` (`flag`)
- `elarion_npcs:clear_story_flag` (`flag`)
- `elarion_npcs:set_ending` (`ending`)
- `elarion_npcs:set_reentry_node` (`node`)

Registered conditions:

- `elarion_npcs:story_flag_set` (`flag`)
- `elarion_npcs:ending_is` (`ending`)

Re-entry is opt-in. Ordinary NPC conversations continue to begin at their root
node. `set_reentry_node` changes the next conversation entry only for that
player and placed NPC. Invalid or removed stored nodes safely fall back to the
dialogue root.

`close: true` is now server-authoritative after successful actions and closes
the active conversation instead of reopening the same node.

## History And Chronicle

Ordinary choices, service use, and relationship score changes remain silent.
An action with:

```yaml
history-worthy: true
history-outcome: "an honest alliance"
```

emits one structured `npc/story-outcome` history record only after all option
actions succeed. Metadata includes actor name, NPC name/definition, outcome,
dialogue, node, option, and a persisted `chronicle.variant` id.

`NpcChronicleText` owns ten authored deterministic variants and a missing-
context fallback. The Core `history.yml` default Chronicle category set now
includes `npc`; existing installations must retain/add that category if NPC
story outcomes should appear in future archive/library views.

## Deliberately Deferred

- Persistent resume of an interrupted open conversation.
- Aggregate Realm/faction reputation.
- Character Menu NPC reputation contributor.
- NPC-screen relationship label/tier presentation.
- Quest-specific parameter validation.
- Graph visualization and localization-key validation.
- Relationship milestone Chronicle families; they require their own ten
  authored variants when promoted.
