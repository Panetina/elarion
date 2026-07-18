# NPC Narrative Readiness Audit

Date: 2026-07-11

Status: Phase 9 complete. Slices 1-4 audited and hardened graphs/relationships;
Slices 5-6 added durable NPC story state, one-time choices, endings, re-entry,
and structured Chronicle-ready story outcomes.

## Scope

Inspected the current NPC addon architecture for long-term narrative readiness:
definitions, dialogue graphs, conditions/actions, sessions, placement state,
trade/bank service screens, validation, quest hooks, relationship placeholders,
and persistence boundaries.

Primary source files inspected:

- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/api/ElarionNpcApi.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/ElarionNpcsAddon.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigLoader.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/config/NpcConfigValidator.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/DialogueDefinition.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/DialogueNode.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/DialogueOption.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/DialogueCondition.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/DialogueAction.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/DialogueTextVariant.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/model/PlacedNpcRecord.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcInteractionService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/service/NpcPlacementService.java`
- `addons/npcs/src/main/java/panetina/elarion/addons/npcs/storage/NpcPlacementStorage.java`
- `addons/npcs/src/test/java/panetina/elarion/addons/npcs/config/NpcConfigValidatorTest.java`
- `addons/quests/src/main/java/panetina/elarion/addons/quests/storage/QuestPlayerState.java`
- `docs/systems/NPCs.md`
- `docs/addons/npcs.md`

## Verified Architecture

- NPC definitions are data-driven and loaded from config. Definitions own display
  name, skin, portrait, dialogue id, tags, required ability, trade catalog, and
  tax-jurisdiction policy.
- Dialogue content is split into `DialogueDefinition`, `DialogueNode`,
  `DialogueOption`, `DialogueCondition`, `DialogueAction`, and
  `DialogueTextVariant`.
- Dialogue conditions and actions are resolved through Core registries. This is
  the right extension point for Quest, Government, Economy, titles, Realm, and
  future story systems.
- Client screens display server-authored snapshots and submit typed requests.
  `NpcInteractionService` revalidates active session, NPC id, node id,
  placement range, option visibility, prompt amount, bank/trade presentation,
  and action handlers before any mutation.
- Placement runtime state is persisted in
  `world/elarion/addon-state/npcs/placed-npcs.json` through
  `NpcPlacementStorage` schema v2. Placement state includes world id, position,
  visual/dialogue overrides, and resolved tax jurisdiction.
- NPC bank/trade screens are service presentations reached from normal dialogue
  nodes. Economy integration is optional and loaded through provider adapters,
  not a hard dependency.
- Quest actor binding references placed NPCs through the NPC public API. Quest
  state remains quest-owned.
- Current docs already state that aggregate NPC/Realm reputation belongs in the
  Character Menu, while an NPC screen may show only the relationship with the
  specific NPC once NPC-owned relationship data exists.
- NPC relationship V1 now persists per-player/per-placed-NPC integer scores in
  NPC-owned addon state. Dialogue/quest content can use NPC-owned registry
  handlers instead of reading storage directly.

## Strengths

- Server authority is clear for dialogue selection, prompts, bank quotes, trade
  quotes, and trade purchase/sell mutations.
- Trade and bank behavior are not hard-coded into the root NPC conversation UI;
  they are reached through server-authored service nodes.
- Existing validation catches many reference errors: missing skin/portrait,
  missing dialogue, missing root node, broken option `next`, unreachable nodes,
  duplicate option ids, duplicate variant ids, no-exit service presentation
  nodes, unknown condition types, unknown action types, invalid prompt bounds,
  invalid trade rows, and trade-presentation actions/prompts.
- NPC placement and trade persistence use schema/versioned storage and atomic
  writes where current storage helpers allow it.
- Optional Economy absence degrades through provider stubs, so NPC config can
  load without making Economy a required compile/runtime dependency.

## Gaps

- NPC-owned relationship persistence/service exists for V1 score state. The UI
  relation label/value is still intentionally empty until a separate
  server-authored presentation slice maps scores to labels.
- NPC story state now persists flags, one-time choice use, endings, and opt-in
  re-entry nodes per player and placed NPC.
- Dialogue sessions are in-memory only. That is acceptable for short
  conversations, but not enough for durable story state or one-time choices.
- Dialogue graph validation now checks reachability from the root, unreachable
  nodes, duplicate option ids within a node, duplicate variant ids, and no-exit
  service presentation nodes.
- Dialogue graph validation does not yet identify named endings, intentional
  circular branches, or circular branches that prevent completion.
- YAML duplicate node keys are likely overwritten by map parsing before the
  validator can report them. This should be addressed only if the current YAML
  parser exposes key events cleanly.
- `DialogueAction.historyWorthy` is parsed from config but not currently used by
  `NpcInteractionService` to emit history or Chronicle events.
- `DialogueOption.close` is parsed, but option handling currently advances to
  `next` or reopens the same node. Player close remains ESC/footer close. If
  config-authored close behavior is desired, it needs a narrow server-side
  behavior slice and tests.
- Validation does not yet perform parameter-specific checks for known
  conditions/actions. For example, a future `quest_state` condition should
  validate quest id/stage through the Quest public API, and a future
  `relationship_at_least` condition should validate NPC relationship ids and
  thresholds.
- There is no NPC profile contributor yet. That should wait until NPC-owned
  relationship summaries exist so Core does not duplicate addon state.

## Recommended Phase 9 Order

1. **Phase 9 Slice 2 - NPC Graph Validation V1**
   - Status: complete.
   - Added pure config validation for root reachability, unreachable nodes,
     duplicate option ids, duplicate variant ids, and service-node exit safety.
   - Runtime behavior and persistence remain unchanged.

2. **Phase 9 Slice 3 - NPC Relationship State Design**
   - Status: complete.
   - Defined NPC-owned records, visibility, history/Chronicle rules, registry
     contracts, and Character Menu projection boundaries in
     `docs/reports/NPC_RELATIONSHIP_STATE_DESIGN.md`.

3. **Phase 9 Slice 4 - NPC Relationship Persistence V1**
   - Status: complete.
   - Added `NpcRelationshipStorage`, `NpcRelationshipService`,
     `NpcRelationshipRegistryHandlers`, and tests for round-trip persistence,
     score clamping, actions, conditions, and missing context handling.

4. **Phase 9 Slice 5 - Story Flags And One-Time Choices**
   - Status: complete.
   - Added the design and V1 persistence/runtime contract recorded in
     `docs/reports/NPC_STORY_STATE_DESIGN.md`.

5. **Phase 9 Slice 6 - NPC History/Chronicle Integration**
   - Status: complete.
   - Explicit history-worthy outcomes emit structured `npc/story-outcome`
     records with persisted variant ids and ten authored Chronicle variants.

## Deferred Work

- Localization-key validation.
- NPC graph visualization/dev UI.
- Rich relationship UI in the NPC dialogue screen.
- Aggregate NPC/Realm reputation in Character Menu.
- Quest-specific condition/action parameter validation.
- Persistent conversation resume after disconnect/restart.
