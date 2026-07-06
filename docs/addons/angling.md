# Angling Addon Contract

Last reviewed: 2026-06-20

Author: Panyel  
Team: Panetina Team

`addons/angling` owns the Fabric addon shell and definition-loading foundation
for future fishing gameplay. Current work intentionally contains no custom
items, entities, blocks, commands, UI, or addon-owned persistence.

## Ownership

Angling will own:

- fishing content definitions and validated definition indexes
- active fishing session behavior while a player is fishing
- catch resolution and reward requests
- fishing-specific commands or UI added in later phases

Core remains authoritative for:

- citizens, titles, abilities, rewards, history, and progression
- durable catch history intended for titles, Chronicle, archives, or dashboards
- rarity summaries and player-facing progression state derived from catches

Angling must publish catch outcomes through a future Core-owned API or event
surface instead of writing title, progression, or history state directly.

## Reference Boundary

The pinned NeoForge 1.21.1 reference remains under
`addons/angling/reference/**`. It is ignored, not a Gradle source root, and must
not be packaged.

Current repository policy: ignore this reference tree for ordinary Elarion
audits, navigation, architecture decisions, and search work unless Angling
porting is explicitly resumed.

Only MIT-licensed code patterns may be selectively adapted. Upstream branding,
creative content, item rosters, textures, models, sounds, prose, and other
assets are not production inputs.

Angling porting decisions are tracked in the Angling-local Markdown files such
as `addons/angling/INDEX.md`, `addons/angling/TODO.md`, and
`addons/angling/PORTING_LOG.md`. Every future port slice must update
`addons/angling/PORTING_LOG.md` with changed files, checks, failures, and the
next action.

All future player-facing names, item names, fish names, lore, UI copy, guide
text, sounds, models, and textures must use placeholders until Panyel supplies
original replacements. Placeholder IDs must not reuse upstream names because
IDs can become persistence and compatibility contracts.

## Current Behavior

- Fabric metadata registers `elarion_angling`.
- The addon initializes through Core's `elarion:addon` entrypoint.
- Placeholder fish definition model types exist.
- A pure JSON-to-index loader exists for future fish definition resources.
- A Fabric server-data reload listener and atomic snapshot repository exist.
- Future fish definition resources are expected under
  `data/elarion_angling/angling/fish/*.json`.
- Core exposes an immutable catch telemetry event contract that Angling may
  emit after future server-authoritative catch resolution.
- Core durably journals emitted telemetry and exposes immutable direct-player
  summaries through `api.catchTelemetry()`.
- Angling has an internal immutable catch-result and resolution service that
  validates loaded definitions, assigns event identity/time once, and emits
  technical Core telemetry.
- Angling condition evaluation uses immutable server context, a bounded pure
  evaluator registry, fail-closed unknown IDs, and deterministic weighted
  candidate selection from the current reload snapshot.
- Angling registers Elarion-owned built-in technical condition evaluators for
  fluid, dimension, weather, time, elevation, biome, and bait presence. These
  adapt only neutral reference restriction categories, not upstream IDs,
  rosters, names, or code.
- Angling sessions allow one active session per player, retain the selected
  fish and rarity across reloads, use bounded priority-queue expiry, and remove
  completed state only after Core telemetry emission succeeds.
- The first server bobber fishing-logic tick at the water position starts a
  direct-player session, and unload cancels it. One narrow required mixin also
  completes telemetry immediately before vanilla successful-catch loot
  generation, so Core acceptance precedes reward creation.
- Candidate selection is attempted once per bobber. Placeholder-only accepted
  and unavailable action-bar feedback is rate-limited per player and
  presentation failures are nonfatal.
- Player disconnect is a hard fishing cancellation boundary. Angling cancels
  the ephemeral session, removes the linked vanilla fishing bobber, clears the
  player hook, and clears feedback cooldowns. Casts are not preserved across
  reconnect.
- The next custom reward boundary is specified in
  `addons/angling/docs/REWARD_DELIVERY_SPEC.md`: Angling will enqueue a
  deterministic Core deferred reward grant from an accepted catch result, then
  suppress vanilla loot. The fishing hook will not directly insert or drop
  items.
- One placeholder catch reward item is registered and delivered through Core
  deferred rewards after catch telemetry is accepted. Current known fish now
  grant fish-specific reward items with original working names and temporary
  Angling-owned icons; the generic catch item remains a fallback for unknown
  fish IDs. Vanilla fishing loot is suppressed only after the custom completion
  succeeds, and Angling explicitly removes the vanilla bobber because the
  vanilla loot branch is cancelled.
- The aggregate `dev` runtime includes Angling. Dedicated-server startup
  verifies addon initialization, datapack discovery, and application of
  `FishingBobberEntityMixin`.
- No config files are generated yet.
- Angling persists no addon-owned runtime state yet.
- No commands are registered yet.

## Future Integration

Core now owns the immutable event, accepted-catch journal, replay checkpoints,
per-player summary indexes, lifecycle persistence, and read-only query API.
Title and Chronicle consumers must read these Core-owned summaries rather than
scanning Angling or journal storage.

The data model skeleton, pure JSON loader, Fabric reload foundation, one
placeholder fish resource, immutable Core telemetry transport, and the
decision-complete processing architecture in
`docs/systems/CatchTelemetry.md` are complete.

Telemetry now has a stable event ID and occurrence time, a versioned Core
accepted-record model, an explicit JSONL codec/path contract, and a registered
Core player/month append, bounded queued replay, immutable
per-player summaries, dirty-save lifecycle, and `api.catchTelemetry()` queries.

Manual smoke confirmed that accepted catches suppress vanilla loot, publish a
Core reward notification, grant the placeholder item on claim, return the rod
immediately, and keep full-inventory claims pending until room is made. The
packaged placeholder fish data now uses neutral placeholder definitions
that exercise built-in water, overworld, river, rain, thunder, night, and
underground conditions without upstream names, roster identity, assets, or UI.
Angling also has a technical placeholder bait item and hand resolver that feeds
`condition/bait_present` into selection. The bait ID is captured in the
ephemeral cast session and one matching bait item is consumed after a
successful custom catch has been accepted by Core and queued as a reward.
Current visible fish names are original working names in `en_us.json`; the
technical fish IDs remain placeholder IDs until final content/schema decisions
are stable. `addons/angling/VISUAL_ASSET_REDIRECT.md` tracks every current
visual resource and future visual replacement surface.
Claimed catch rewards use the current fish display name as the custom name on
the placeholder catch item, so the reward identifies the caught fish without
creating fish-specific item IDs yet.
The catch and bait items now use original working item names and Angling-owned
temporary icons. Current fish reward items also use original working item names
and Angling-owned temporary icons. Replacement paths are indexed in
`addons/angling/VISUAL_ASSET_REDIRECT.md`.
The next approved work is validating unavailable-selection, telemetry-failure
retry, bait-consumption, and fish-specific reward smoke scenarios before adding
another gameplay foundation.
