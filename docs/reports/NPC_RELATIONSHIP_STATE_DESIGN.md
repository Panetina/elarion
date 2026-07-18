# NPC Relationship State Design

Date: 2026-07-11

Status: Phase 9 Slice 3 complete; Phase 9 Slice 4 V1 implementation follows
this design.

## Goal

NPCs owns per-player relationship state with a specific placed NPC. This is not
Realm reputation, faction reputation, or a Core citizen field. The normal NPC
screen may later display this specific relationship; aggregate NPC/Realm
reputation belongs in the Character Menu only after NPCs exposes an
owner-maintained summary.

## Ownership

- Owner module: `addons/npcs`.
- Canonical key: player UUID + placed NPC UUID.
- Runtime owner: `NpcRelationshipService`.
- Persistence owner: `NpcRelationshipStorage`.
- Storage path: `world/elarion/addon-state/npcs/relationships.json`.
- Core role: provide registry, profile, history, and Chronicle infrastructure
  only. Core must not store NPC relationship state.

## V1 Data Shape

Each relationship record stores:

- player UUID
- placed NPC UUID
- integer score
- last update timestamp

Score is intentionally simple in V1. Tiers, names, per-NPC caps, decay, faction
aggregation, and UI labels are future work.

## Registry Contracts

Dialogue and quest content should use registry handlers, not direct storage
access.

V1 actions:

- `elarion_npcs:set_relationship`
  - Parameters: `value` or `score`, optional `npc`/`npcId`.
  - Defaults `npc` to current dialogue metadata.
- `elarion_npcs:add_relationship`
  - Parameters: `amount` or `delta`, optional `npc`/`npcId`.
  - Defaults `npc` to current dialogue metadata.

V1 condition:

- `elarion_npcs:relationship_at_least`
  - Parameters: `minimum` or `value`, optional `npc`/`npcId`.
  - Defaults `npc` to current dialogue metadata.

## Visibility

- Relationship state is private by default.
- Future NPC dialogue UI may display only the current player relationship with
  the specific NPC being viewed.
- Future Character Menu summary should be `SELF` visibility until a deliberate
  public/Realm-facing reputation policy exists.

## History And Chronicles

Do not emit history for every score change. Only meaningful milestones should
emit history/Chronicle entries, and each player-facing family needs 10 authored
variants under the Chronicle rule. V1 relationship score changes are silent.

## Deferred Work

- Relationship tiers and labels.
- NPC dialogue UI relation rendering from server-authored relationship values.
- Character Menu `npcs/reputation` contributor.
- Relationship milestone events and Chronicle variants.
- Faction/Realm reputation aggregation.
- Decay, caps by NPC, or config-driven score policies.

