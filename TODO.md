# Elarion TODO

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

This file contains actionable work only. Future design belongs in `PLANS.md`.
Permanent rules belong in `RULES.md`. Ownership, commands, config paths, and
runtime-state paths belong in `INDEX.md`.

## How To Use This File

- `Done` means implemented and reflected in the current docs.
- `Next` means concrete enough to implement soon.
- `Later` means planned but blocked by earlier systems, UI work, or missing
  infrastructure.
- If an idea is still mostly design philosophy, keep it in `PLANS.md`.

## Priority Now

1. NPC foundation: placeable NPC definitions, skin/profile configuration,
   dialogue trees, interaction GUI groundwork, and action/condition integration.
2. Contributions foundation: contribution block, durable block IDs, project
   loader, society pillars/ranks, mixed requirements, and milestone execution.
3. Portal foundation: physical portals, ticketed Nether/End access, and access
   checks through Core abilities, progression, relationships, and config.
4. Government foundation: government form loader, offices, founding/reform vote
   backend, treasury audit events, and treaty metadata.
5. Economy integration: registered reward/sink actions, Bank interaction hooks,
   and source/sink instrumentation as dependent systems become real.
6. Tests and validation around the systems above.

## Current Readiness Risks

- History commands now use bounded newest-month JSONL scans. Core writes
  compact monthly history indexes, immutable weekly Chronicle archives, and a
  public-history composition API. Rich search, newspaper feeds, ledger views,
  NPC rumors, and GUI history browsers still need their actual presentation
  consumers before they become player-facing.
- A basic GameTest command harness exists. Expand it before the Economy, NPC,
  Contributions, Government, and Portal command surfaces grow much larger.
- Addon config validation is strong only for current Core/current addon
  behavior. Every new Economy, NPC, Contributions, Government, Trade, Quest, or
  Portal config loader must validate references against registered handlers and
  runtime owners.
- Final performance confidence still requires profiling with the real modpack,
  terrain generation, mobs, backpacks, voice chat, farms, players, and host.
- Future systems must continue using Core ownership and addon APIs; do not let
  Economy, NPCs, Government, Trade, or Contributions duplicate citizen, Realm,
  title, ability, relationship, identity, reward, or history truth.

## Definition Of Ready

A TODO item is ready for implementation when it has:

- owner addon or Core owner
- config path or explicit no-config decision
- runtime state path or explicit no-runtime-state decision
- Core API, event, registry, or ability needs
- command, GUI, block, NPC, or other interaction surface when applicable
- history/progression events when applicable
- test type: unit, integration, GameTest, or manual-only with reason

## Core And Architecture

### Done

- [x] Multi-project Fabric 1.21.1 layout with Core and addons.
- [x] `ElarionApi` for addon access.
- [x] Grouped `ElarionApi` facades for identity, Realm, messaging,
  progression, and system concerns.
- [x] Current addon sources use grouped API facades where grouped facades exist.
- [x] Canonical citizen, Realm, title, nickname, status, ability, identity,
  reward, history, and progression foundations.
- [x] YAML config generation and validation for Core files.
- [x] Core config regeneration and migration policy documented.
- [x] Core cross-file reference validation for current built-in title, reward,
  ability, item, and status references.
- [x] OP level 4 `/e` admin command root.
- [x] Player/help command registration split out of the main admin command
  registrar.
- [x] Ability, reward, and history admin command groups split out of the main
  admin command registrar.
- [x] Shared registry foundation for conditions, actions, requirements, and
  milestone events exposed through `ElarionApi`.
- [x] Registry execution context/result foundation.
- [x] Initial executable Core condition, action, requirement, and milestone
  handlers.
- [x] Reward action service for messages, items, commands, titles, abilities,
  status, and history.
- [x] Shared atomic JSON state-write helper and dirty tracker for runtime state.
- [x] Targeted identity subject sync for ordinary citizen changes.
- [x] Core `ElarionTaskService` with conservative IO, compute, and bounded
  server-thread apply queues.
- [x] Task budgets load from `optimization/performance.yml`.
- [x] Task queue diagnostics report rolling apply timing, slow apply ticks, and
  rejected/completed/failed counters by task family.
- [x] Task diagnostics report IO and compute submitted, queued, active,
  completed, failed, and family counters.
- [x] Identity sync requests coalesce per tick for repeated full, viewer, and
  subject sync intents while join/bootstrap can still sync immediately.
- [x] Core config default generation split into a focused helper.
- [x] Focused contracts exist for current real-behavior addons: Core, Worlds,
  Realms, Names, Titles, Optimization, and Security.

### Next

- [ ] Extend config-reference validation as each addon registers executable
  actions, conditions, requirements, milestone events, rewards, titles, Realms,
  abilities, worlds, and tickets.
- [ ] Move large filesystem/report work to `ElarionTaskService.io`.
- [ ] Move immutable validation, planning, indexing, summary, and Chronicle
  generation work to `ElarionTaskService.compute`.
- [ ] Add migration helpers for config schema changes.
- [ ] Add generic rate-limited feedback helper.
- [ ] Add focused addon contracts when Economy, Trade, NPCs, Contributions,
  Government, Portals, Adventure Guild, Ledger, Jail, Underworld, Newspapers,
  Tablist, or Voice Chat Hooks gain real behavior.

## Realms, Chat, And Protection

### Done

- [x] Realm definitions and membership.
- [x] Realm colors through scoreboard teams.
- [x] `/rc` Realm Chat.
- [x] `/ac` direct-alliance chat.
- [x] `/pm`, `/r`, `/w`, `/yell`, local chat, and `/spy chat`.
- [x] Scoped join/leave notices.
- [x] Realm spawn fallback and assignment teleport.
- [x] Realm relationships: `ALLY`, `NEUTRAL`, `EMBARGOED`, `HOSTILE`.
- [x] `HIDDEN` as single-Realm seclusion state, not a relationship.
- [x] Diplomacy-excluded Realm flag.
- [x] Realm protection for own Realm, allied visitors, hostile visitors,
  neutral/embargoed visitors, lobby, and Worldheart.
- [x] Cached Realm-world owner lookup through Core `RealmService`.
- [x] Realm protection OP bypass defaults to disabled for normal testing.

### Next

- [ ] Route future maps, rosters, tab, nameplates, tracking, government,
  newspaper, portal, trade, mail, and chat exceptions through shared Realm
  access policies.
- [ ] Add Realm onboarding backend: welcome message, first civic quest hook,
  starter badge/title hook, online citizen list, NPC intro hook, and active
  project prompt.
- [ ] Add Realm active needs service for small useful tasks without daily login
  punishment.
- [ ] Add public recognition feed hooks for Realm notifications, newspapers,
  ledger, history, NPC rumors, monuments, and Chronicle prose.
- [ ] Add portal-aware Diplomat checks when physical portals exist.
- [ ] Add GameTests for Realm protection and visitor interaction matrix.

### Later

- [ ] Add modular `/spy` children beyond `/spy chat`.
- [ ] Add Realm relationship effects for future maps, rosters, tracking tools,
  government rules, newspaper rules, and voice chat hooks.

## Worlds And Portals

### Done

- [x] Addon-managed worlds through Elarion Worlds.
- [x] Strict `worlds.yml` definitions for seeds, templates, gamerules, spawn
  points, and per-world borders.
- [x] Lobby, Worldheart-ready world support, and Realm worlds.
- [x] Per-world border persistence.
- [x] Basic world teleport/admin commands.
- [x] Processed-chunk runtime state uses shared atomic JSON writes.
- [x] Block abundance rules are pre-resolved and cached per world-rule set.
- [x] Block abundance replacement work uses the bounded Core server-thread
  queue instead of running directly inside chunk-load events.
- [x] Block abundance replacement uses 16-block vertical slices and marks a
  chunk processed only after every slice finishes.
- [x] Queue-full retry metrics and slice completion/failure metrics for
  block-abundance replacement.

### Next

- [ ] Implement physical portal definitions and portal travel.
- [ ] Gate portal use through Core progression, abilities, Realm relationships,
  tickets, and portal config.
- [ ] Add ticketed Nether and End portal access with consumed tickets and no
  cooldowns by default.
- [ ] Disable or control vanilla Nether/End portal access.
- [ ] Add portal runtime state under `world/elarion/addon-state/portals/`.
- [ ] Add portal access history events.

### Later

- [ ] Add future ticket types for events, dungeons, boss arenas, mounts, plots,
  and temporary passes.
- [ ] Add Realm treasury subsidies for portal tickets after Economy exists.

## Titles, Stats, And Progression

### Done

- [x] Configured title definitions.
- [x] Unlocked title ownership and one active title.
- [x] Globally unique title claims.
- [x] OP title grant, revoke, inspect, active, claims, and repair commands.
- [x] Event-driven player stats with dirty saves.
- [x] `title-progression.yml`.
- [x] Registry ID/tag matching for modded entities, blocks, items, and recipes.
- [x] Entity kill, block break, crop-like harvest, block use, item use,
  crafting, advancement, region, continuous, stat-threshold, and custom
  progression events.
- [x] Named progression regions such as `maze_end`.
- [x] Active-title status effects such as `aquatic` water breathing.
- [x] OP progression inspect/event/reset/test-rule commands.
- [x] Progression rules are indexed by trigger for event handling.
- [x] Continuous progression rules are grouped by sample interval.
- [x] Progression regions are grouped by world ID so unrelated worlds skip
  region string work.

### Next

- [ ] Add contribution milestone hooks when Contributions addon is rebuilt.
- [ ] Add explicit active-effect removal hooks for future non-expiring effects.
- [ ] Add broader GameTests for live title unlocks and unique-title races.
- [ ] Add non-title stat milestone announcements/history events.

### Later

- [ ] Add `/titles` player-facing title selection GUI.
- [ ] Add NPC, block, and GUI title selection integration.
- [ ] Add hidden locked-title discovery UI behavior.

## NPCs Addon

### Done

- [ ] Not started.

### Next

- [ ] Add NPC definition loader under
  `config/elarion/addons/npcs/npcs.yml`.
- [ ] Add dialogue tree loader under
  `config/elarion/addons/npcs/dialogues/<dialogue-id>.yml`.
- [ ] Add NPC skin/profile definition support for texture IDs, player-like
  skins, static portraits, and future modded NPC adapters.
- [ ] Add placeable NPC runtime state under
  `world/elarion/addon-state/npcs/placed-npcs.json`.
- [ ] Add per-player dialogue/session runtime state under
  `world/elarion/addon-state/npcs/sessions/` or an equivalent compact state
  file.
- [ ] Add OP level 4 NPC commands for place, remove, list, inspect, move, set
  skin/profile, set dialogue, and reload.
- [ ] Add server-authoritative NPC interaction entry point.
- [ ] Add dialogue condition/action registry integration.
- [ ] Add GUI groundwork for dialogue text, avatar/portrait, selectable
  responses, and action buttons.
- [ ] Add NPC action hooks for opening bank, market entry, seller permit,
  portal ticket, quest board, government page, title selection, contribution
  project, and ledger views.
- [ ] Add validation so unknown dialogue nodes, options, conditions, actions,
  skins, NPC IDs, and target addons fail clearly on reload.
- [ ] Add history events for important NPC-driven actions, not ordinary menu
  browsing.

### Later

- [ ] Add banker, Market Gatekeeper, Market Registrar, Portal Keeper, Backpack
  Vendor, Quest Officer, Realm Registrar, Treasury Clerk, Mount Stablemaster,
  Plot Registrar, and lore/rumor NPC examples.
- [ ] Add static NPC entity renderer or controlled entity integration.
- [ ] Add NPC pathing only if truly needed; default NPCs should be static and
  event-driven for performance.
- [ ] Add optional adapters for custom villager/NPC mods.
- [ ] Add NPC rumor feed sourced from public recognition and history.
- [ ] Add NPC-driven onboarding ritual for first Realm join.

## Economy Addon

### Done

- [x] Added `elarion:sigil` with the Elarion Seal texture, item model, and
  display name.
- [x] Added in-memory player wallet and Realm treasury state with
  schema-versioned atomic snapshots.
- [x] Added physical sigil deposit/withdraw backend.
- [x] Added one audited transaction engine as the only balance mutation path.
- [x] Added explicit transaction types: transfer, deposit, withdraw, reward,
  fee, tax, sink, treasury grant, and admin adjustment.
- [x] Added immutable transaction records with account balances before/after,
  actor, reason, source system, success/failure, and metadata.
- [x] Added forced append-only transaction journaling before successful balance
  mutation, restart replay, and snapshot shutdown ordering.
- [x] Added bounded OP transaction queries by player and Realm.
- [x] Added Economy Governor monitor state and economy health calculation:
  deflationary, healthy, warm, inflationary, overheated, stagnant, and
  concentrated.
- [x] Added OP economy commands for wallet, treasury, transactions, pulse, and
  recalculation.
- [x] Added configured Governor modes with `MONITOR_ONLY` as the development
  default; automatic adjustment behavior remains disabled.
- [x] Added Economy API, diagnostics, Core history emission, unit tests, and
  command/API GameTest coverage.

### Next

- [ ] Register Economy condition, requirement, action, and milestone handlers
  when the first NPC, Quest, Contribution, Portal, or Government consumer needs
  them.
- [ ] Track market fees, ticket purchases, NPC purchases, quest payouts, and
  treasury movement through explicit `sourceSystem` values as those systems
  become real.
- [ ] Add dedicated account transaction indexes before transaction history
  becomes a high-volume player-facing GUI.

### Later

- [ ] Add conservative interest.
- [ ] Add Bank NPC/block action integration.
- [ ] Add economy reward/action handlers.
- [ ] Add bounded adaptive price and quest reward adjustment suggestions.
- [ ] Add public weekly economy report option.

## Trade And Closed Worldheart Market

### Done

- [ ] Not started.

### Next

- [ ] Add `TradeAccessPolicy`.
- [ ] Add closed Worldheart Market access policy.
- [ ] Add 1 sigil market entry fee with temporary access.
- [ ] Add 15 sigil seller session fee.
- [ ] Add manual stall session backend.
- [ ] Enforce no passive offline selling.
- [ ] Add market transaction tax.
- [ ] Add direct transfer restrictions for same Realm, ally, neutral,
  embargoed, hostile, hidden, and Worldheart contexts.
- [ ] Add no-drop/no-container-bypass rules inside closed market where
  practical.

### Later

- [ ] Add manual stall GUI.
- [ ] Add seller presence validation near stall.
- [ ] Add market reputation hooks.
- [ ] Add anti-bypass hooks for containers, hoppers, droppers, shulkers,
  bundles, backpacks, and modded storage where practical.
- [ ] Add optional limited event auction design, disabled by default.

## Contributions And Society

### Done

- [ ] Not started.

### Next

- [ ] Add contribution/progression block.
- [ ] Add durable block IDs.
- [ ] Add project definition loader.
- [ ] Add project runtime state service under
  `world/elarion/addon-state/contributions/`.
- [ ] Add mixed requirement registry integration.
- [ ] Add society pillar/rank definitions.
- [ ] Add milestone event execution.
- [ ] Add mixed contribution categories for blocks, kills, discoveries,
  crops/food, ores, votes, market deliveries, defense objectives, quests, and
  unique contributors.
- [ ] Add Realm public good project support with Realm-wide benefit unlocks.

### Later

- [ ] Add blueprint loader.
- [ ] Add hologram sync/rendering.
- [ ] Add batched placement queue.
- [ ] Add contributor totals and history.
- [ ] Add completion ceremony, monument/plaque credit, newspaper, NPC rumor,
  and Chronicle hooks for major project milestones.

## Government And Diplomacy

### Done

- [x] Government addon shell exists.

### Next

- [ ] Add government config folder loader.
- [ ] Add government form definition model.
- [ ] Add government runtime state service under
  `world/elarion/addon-state/government/`.
- [ ] Add generic Government Block landing page design.
- [ ] Add founding vote backend.
- [ ] Add reform vote backend with default 80 percent threshold.
- [ ] Add office holder runtime state beyond Core leader.
- [ ] Add leader and council term configuration with defaults: leader 3 real
  weeks, council 2 real weeks.
- [ ] Add public treasury audit event model.
- [ ] Add treasury spending proposal backend.
- [ ] Add recall vote groundwork with cooldowns and audit history.

### Later

- [ ] Add laws and tax law effects.
- [ ] Add treaty metadata beyond Core relationship enum.
- [ ] Add diplomatic summit backend requiring leader presence at a decision
  location before major treaty proposals.
- [ ] Add treaty flags such as direct trade, portal discount, shared maps,
  alliance chat, defense pact, ticket subsidy, and market tax discount.
- [ ] Add government GUI pages.

## War, Invasions, And Rivalry

### Done

- [ ] Not started.

### Next

- [ ] Add objective war definition model.
- [ ] Add scheduled invasion window model.
- [ ] Add war declaration cost and supply requirement hooks.
- [ ] Add war fatigue state and calculation.
- [ ] Add lawful war kill, unlawful kill, and duel kill event separation.
- [ ] Add defender/support contribution tracking for healing, scouting,
  supplies, objective denial, and banner recovery.

### Later

- [ ] Add capture banner objective.
- [ ] Add generated supply raid objective.
- [ ] Add temporary outpost siege objective.
- [ ] Add caravan ambush objective.
- [ ] Add relic contest objective.
- [ ] Add portal disruption objective.
- [ ] Add seasonal Realm scoreboards for helpfulness, building, trade, defense,
  public works, diplomacy, and citizen turnout.

## Adventure Guild And Quests

### Done

- [ ] Not started.

### Next

- [ ] Add quest definition loader.
- [ ] Add quest progress runtime state.
- [ ] Add quest objective registry.
- [ ] Add quest reward integration.
- [ ] Add economy-aware quest reward scaling.
- [ ] Add economy-aware quest availability scaling.
- [ ] Add reward bundles: sigils, items, ledger reputation, title progress,
  badge progress, contribution credit, ticket, discount voucher, Realm treasury
  grant.

### Later

- [ ] Add anti-abuse checks for kill/fetch/delivery/building quests.
- [ ] Add quest HUD drawer sync.
- [ ] Add Adventure Guild NPC/block integration.

## Ledger And Inactivity

### Done

- [ ] Not started.

### Next

- [ ] Add public civic stats model.
- [ ] Add badge definition loader.
- [ ] Add Ledger runtime state.
- [ ] Add Core progression/history hooks.
- [ ] Track last seen.
- [ ] Add active/inactive citizen calculation.
- [ ] Exclude inactive citizens from vote and population denominators.
- [ ] Keep mail delivery enabled for inactive citizens.
- [ ] Reactivate on login.
- [ ] Add social memory event families for contribution, offices, war,
  diplomacy, public projects, treasury spending, elections, and civic quests.

### Later

- [ ] Add Government Block ledger views.
- [ ] Add NPC ledger views.
- [ ] Add trade, quest, office, vote, and war stat integrations.
- [ ] Add public recognition feed integration.
- [ ] Add monument/plaque display integration.
- [ ] Add newspaper and NPC rumor consumption hooks.
- [ ] Emit configurable inactive/reactivated history events.
- [ ] Add once-per-server-day cleanup/audit.

## History And Chronicles

### Done

- [x] Durable JSONL history events under world save.
- [x] Chronicle-ready prose field on history events.
- [x] OP history query commands.
- [x] History writes are queued and batched by monthly JSONL file, with periodic
  flushes and shutdown drain.
- [x] `history.yml` controls history recording by category and event type.
- [x] Generated history defaults disable noisy chat recording unless explicitly
  enabled.
- [x] Live history commands/API queries scan newest monthly JSONL files first
  and stop at configured bounds.
- [x] Compact monthly history indexes are written under
  `world/elarion/history-index/` for Chronicle, search, newspaper,
  ledger, and public-memory views.
- [x] History docs define storage shape, filtering, query-scaling direction,
  and Chronicle preparation rules.
- [x] Weekly immutable Chronicle archive records are generated under
  `world/elarion/chronicles/weekly/`.
- [x] `history.yml` controls Chronicle archive generation and archive
  categories.
- [x] Public-history query APIs compose weekly Chronicle archives with live
  monthly indexes for newspaper, ledger, NPC rumor, and GUI search consumers.
- [x] Chronicle archive storage and generation are covered by unit/GameTest
  checks.
- [x] Automatic weekly Chronicle generation runs through the Core task queues
  instead of ordinary server-tick work.
- [x] Chronicle archive writes use atomic temp-file replacement.
- [x] OP Chronicle list/inspect commands exist under `/e history chronicle`.

### Next

- [ ] Add bookshelf display registration state.
- [ ] Add a Folklore volume definition loader with stable volume ID, title,
  category, text source, discovery rules, and archive presentation metadata.
- [ ] Add global unique Echo Scroll discovery claims so one configured scroll
  cannot be repeatedly generated or returned.
- [ ] Add server-verifiable lore recovery quests: discover scroll, receive a
  return objective, deliver it to the Worldheart archive, credit the finder,
  and permanently unlock the matching Folklore volume.
- [ ] Store unlocked Folklore volume IDs, discoverer UUIDs, discovery location,
  recovery time, and display assignment in compact world runtime state.
- [ ] Emit lore-discovered, lore-returned, lore-restored, and
  lore-display-assigned history events with reader-friendly prose.
- [ ] Add an archive reader API shared by static Folklore and generated
  Chronicles without merging their ownership or storage formats.
- [ ] Validate that returned Folklore opens read-only and never creates a
  stealable, droppable, movable, or duplicable book item.
- [ ] Add notification and NPC dialogue hooks for newly discovered and restored
  volumes.
- [ ] Add newspaper, ledger, NPC rumor, and GUI search consumers on top of
  `api.publicHistory()`.
- [ ] Add dedicated newspaper/search/ledger indexes only after real GUI usage
  proves the generic bounded public-history API is too broad.
- [ ] Ensure every new major system emits reader-friendly history/prose fields.

### Later

- [ ] Assign Chronicle volumes to chiseled bookshelf slots.
- [ ] Open book-reading GUI directly from bookshelf slot.
- [ ] Queue unassigned volumes when all display slots are full.
- [ ] Place configured unique Echo Scroll discoveries in ruins, structures,
  exploration loot, quests, or administrator-curated world locations.
- [ ] Add Worldheart archive catalog/search UI with locked, discovered,
  returned, displayed, and readable states.
- [ ] Add restoration ceremonies and public recognition for important
  recovered volumes.

## Notifications UI

### Done

- [x] Left-side personal and Realm notification icons.
- [x] Supplied mail/Realm 16x16 textures.
- [x] Read/unread icon states.
- [x] Click-to-chat feedback.

### Next

- [ ] Add modular notification registry.
- [ ] Add unread counts and read tracking.
- [ ] Add server-authoritative notification sync.

### Later

- [ ] Build personal notification drawer.
- [ ] Build Realm notification drawer.
- [ ] Add buttons: `Receive`, `Agree`, `Disagree`, `Open`, `Dismiss`.
- [ ] Add final animations.
- [ ] Add quest tracker icon and drawer.

## Optimization Addon

### Done

- [x] Addon shell exists.
- [x] Generates `config/elarion/addons/optimization/performance.yml`.
- [x] Registers OP level 4 `/e perf status` and `/e perf queues`.
- [x] Exposes Core task queue diagnostics.
- [x] Core task budgets load from `optimization/performance.yml`.
- [x] `/e perf` reports queue pressure and server-apply budget state.
- [x] `/e perf config` reports loaded host/profile, budgets, monitoring values,
  and fallback status.
- [x] `/e perf worlds` and `/e perf realms` report cached sampled diagnostics.
- [x] Slow-operation counters exist for Core reloads, JSON state saves, history
  writes, and server-queue application.
- [x] Realm/world entity counters use sampled collection.
- [x] Slow-operation warnings exist for storage, history, config reload, and
  queues.
- [x] Performance config reports validation warnings and safe fallback values.
- [x] Tick/headroom monitoring reports sampled average tick state.
- [x] Host profile notes exist for shared-host, dedicated-host, and local-dev
  tuning without changing gameplay rules.
- [x] Optional compatibility notes exist for Bobby and Distant Horizons.
- [x] `/e perf realm <realm>`, `/e perf hotzones`, and `/e perf security`
  exist.
- [x] `/e perf security` reads real Security addon evidence diagnostics through
  Core diagnostics.
- [x] `/e perf worlds` and `/e perf hotzones` show sampled entity categories
  and previous-sample loaded-chunk/entity deltas.
- [x] `/e perf worlds` and `/e perf hotzones` show event-tracked block entity
  counts, block entity type groups, and in-memory per-world trend windows.
- [x] Generated dev `performance.yml` files match conservative host-agnostic
  defaults.
- [x] Deeper profiler integration notes exist for final host testing.
- [x] Unit tests cover Optimization trend diagnostics.
- [x] Operations docs define TPS-drop diagnostics, queue interpretation,
  generated-file cleanup, and config regeneration policy.

### Next

- [ ] Add ticking block entities and ticking chunks only through stable hooks.
- [ ] Add GameTests or integration tests for Optimization commands once command
  testing infrastructure exists.

### Later

- [ ] Add performance snapshots under `world/elarion/addon-state/optimization/`
  only if persistent diagnostics prove worth the IO.

## Security Addon

### Done

- [x] Addon shell exists.
- [x] Generates `config/elarion/addons/security/security.yml`.
- [x] Registers `elarion.security.admin` ability.
- [x] Documents evidence-first anti-cheat and anti-AFK farm defaults.
- [x] Adds durable security evidence runtime state under
  `world/elarion/addon-state/security/evidence.json`.
- [x] Adds OP level 4 `/e security status`.

### Next

- [ ] Add event-driven evidence records for interaction-rate and packet/action
  spam checks.
- [ ] Add anti-AFK farm evidence for ender-pearl chunk loader patterns,
  unattended mob farms, redstone overload zones, and repeated action loops.

### Later

- [ ] Add movement, reach, rotation, and impossible-interaction checks.
- [ ] Add configurable admin warnings and history/audit events.
- [ ] Add optional automatic enforcement only after thresholds are tested.
- [ ] Add false-positive review tools.

## Modpack Compatibility

### Done

- [x] Documented Elarion as a Vanilla+ modpack-aware project, not a
  server-exclusive mod.
- [x] Added permanent rules requiring registry IDs, tags, config-driven
  defaults, optional adapters, and reserved performance headroom.
- [x] Performance headroom reporting exists in Optimization addon.

### Next

- [ ] Add tag/registry-driven example configs for modded crops, foods, mobs,
  blocks, crafted items, recipes, and backpacks.
- [ ] Add optional integration contracts for Farmer's Delight-style food/crop
  mods, Scholar-style knowledge mods, Exposure-style photo/artifact mods,
  Simple Voice Chat, custom villagers, terrain generation, mob, and backpack
  mods.
- [ ] Add modpack compatibility checks to config validation for progression,
  quests, trade, economy, titles, and world rules.
- [ ] Add security false-positive review tasks for modded backpacks, villagers,
  crops, mobs, storage blocks, chunk behavior, and voice/social systems.

### Later

- [ ] Add optional adapter interfaces per integration category.
- [ ] Add compatibility GameTests or stub integration tests where practical.
- [ ] Add documented compatibility profiles for the final server modpack.

## Tests

### Done

- [x] Registry foundation tests.
- [x] Atomic JSON state-write tests.
- [x] Core task queue tests.
- [x] IO/compute task diagnostics tests.
- [x] Identity sync coalescing tests.
- [x] Progression interval/world indexing tests.
- [x] History batch flush tests.
- [x] History recording policy tests.
- [x] History monthly index projection tests.
- [x] World block-rule slice tests.
- [x] Task monitoring snapshot tests.
- [x] Initial Core config-reference validation tests.
- [x] Command surface and expected permission matrix documented for future
  command integration tests.
- [x] Basic GameTest command harness for registration, removal, permission, and
  real command execution checks.

### Next

- [ ] Expand command harness coverage for help, suggestions, side effects,
  persistence, and nickname/identity output checks.
- [ ] Add broader config-reference validation tests as addon registries gain
  executable handlers.
- [x] Add optimization diagnostics tests for pure trend tracking.
- [ ] Add optimization command tests once command testing infrastructure exists.
- [ ] Add security evidence tests.
- [ ] Add progression hook GameTests.
- [ ] Add Realm protection GameTests.
- [ ] Add NPC definition/dialogue validation tests.
- [ ] Add NPC placement/session persistence tests.
- [ ] Add economy persistence and rollback tests.
- [ ] Add Economy Governor health calculation tests.
- [ ] Add market entry, seller fee, manual stall, and transaction rollback
  tests.
- [ ] Add ticket purchase and ticket consumption tests.
- [ ] Add contribution milestone tests.
- [ ] Add government founding/reform vote tests.
- [ ] Add quest objective and quest reward scaling tests.
- [ ] Add trade access policy tests.
