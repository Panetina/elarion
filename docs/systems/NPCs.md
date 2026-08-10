# NPC System

Purpose: place static, server-authoritative NPCs with configurable visuals, dialogue trees, conditional text variants, prompts, service presentations, and registry-driven actions.

Main classes: `ElarionNpcsAddon`, `ElarionNpcEntity`, `NpcPlacementService`, `NpcInteractionService`, `NpcDefinitionService`, `NpcDialogueScreen`, `NpcBankScreen`, `NpcTradeScreen`.

Entry points: `addons/npcs/src/main/resources/fabric.mod.json`, custom `elarion:addon`, client initializer.

Commands: `/e npc ...`.

Network packets: `NpcDialogueOpenPayload`, `NpcDialogueSelectPayload`, `NpcDialoguePromptSubmitPayload`, `NpcTradeSnapshotPayload`, `NpcVisualSyncPayload`, bounded per-viewer `NpcQuestMarkerSyncPayload`, close/dismiss payloads.

GUI/screens: `NpcDialogueScreen`, `NpcBankScreen`, `NpcTradeScreen`, shared Core UI primitives.

Storage/persistence: `world/elarion/addon-state/npcs/placed-npcs.json`.

Dependencies: Core API, Core registries, Core UI theme, optional Economy action handlers.

Related systems: Economy, Offerings, Portals, Quests, Government, Ledger, Public History.

Extension points: dialogue actions, dialogue conditions, conditional node text variants, node presentation kinds, option presentation roles, skin/portrait profiles, NPC tags, service cards.

Risks: duplicating addon-owned state inside NPCs; client-trusted action execution; broad entity scans; hard-coded banker/shop behavior.

Do not duplicate this system by creating: a second NPC manager, hard-coded vendor entity system, or NPC-local wallet/quest/progression storage.

Quest availability markers are Quest-owned read-only projections: Quests tracks
only NPC entities already visible to a player and sends their placed IDs when
availability changes. NPCs holds the client presentation set and renders `!`;
it never evaluates quest definitions or persists marker state.

## Presentation Contract

Every NPC opens through the standard conversation surface first. Service NPCs
must expose explicit dialogue options such as `Open Bank`, `Trade`, or future
quest/service entries rather than replacing the conversation UI with a
special-case screen.

Dialogue nodes carry a server-authored `presentation` value. Current runtime
values are:

- `dialogue`: the normal NPC conversation screen.
- `bank`: the dedicated compact banking service screen.
- `trade`: the dedicated compact Buy/Sell shell. BUY purchases are
  server-authoritative through bounded `NpcTradeSnapshotPayload` /
  quote/purchase payloads, NPC-owned purchase journals, finite placed-NPC stock,
  and optional Economy settlement. SELL buybacks are server-authoritative
  through item escrow, sale replay storage, Economy wallet payout, and optional
  placed-NPC stock replenishment.

NPCs owns parsed `trades.yml` catalogs, server-authored catalog snapshots,
offer visibility, trade sessions, purchase recovery, and finite placed-NPC
stock. Economy remains currency authority through a one-way optional adapter.
Sell/buyback V1 is implemented; dynamic price/inflation policy remains future
Economy-owned work.

Dialogue options carry optional `presentation-role` values such as
`open_bank`, `open_trade`, `deposit`, `withdraw`, `buy`, `sell`, and `back`.
Clients use these stable roles to arrange controls, but the server still
validates session state, range, visibility, prompt amount, conditions, and
action handlers before mutating any state. Clients must not infer behavior from
translated labels, NPC names, or button text. Trade presentation options cannot
define prompts or executable actions; live trade mutations use the dedicated
trade request/result path.

Legacy banker dialogues that place Economy deposit/withdraw prompt actions on
the root node are projected into an in-memory `bank_service` node during load.
The source YAML is not rewritten, and unrelated root options remain on the
conversation screen.

Each NPC screen shows only that placed NPC's authoritative personal
relationship. The Character Menu has a separate Reputation tab containing
faction totals for configured Realms, Worldheart, Underworld, and custom
factions. Realm labels resolve through Core so Realm renames appear without
rewriting relationship data.

Default generated content includes `worldheart_banker` and
`worldheart_trader`. Existing config files are not overwritten by the default
writer, so older worlds need the trader definition/dialogue added explicitly or
through a future approved migration. The known legacy
`worldheart_trader.cobblestone_buyback` route is bridged in memory to
`destination-offer: cobblestone` when the field is missing; custom trader
catalogs still need explicit Sell destination IDs. The dev-run world has
`worldheart_trader_1` placed beside `worldheart_banker_1` for trade-shell QA.
`dev/tools/npc-trade-qa.ps1` can rebuild that pair after the dev client joins
the server by sending normal OP `/e npc` commands through `minecraft-qa.ps1`.
It uses `/e npc open <id>` for the initial conversation instead of a brittle
right-click, then follows the same server-authored service option route as
players. It is QA automation only and does not bypass `NpcPlacementService` or
`NpcInteractionService`.
Both generated service NPCs now use explicit texture skins and curated portrait
library images rather than placeholder/player portraits. The banker screen uses
the shared Sigil currency icon for balance/input/total presentation; the trader
preview uses the same icon for prices.

Portrait-library textures must be drawn with their real source dimensions.
The current banker/trader portraits are 32x32 full images; treating them as
64x64 sources tiles the image. Bank numeric input begins after the Sigil icon
and derives text and caret placement from the same scaled metrics, with the
caret immediately following the final digit. Trader previews retain their real
`ItemStack` for native enchantment, lore, and attribute tooltips. Trader rows
may highlight on full-row hover, but native item tooltips must only open from
the actual item icon hitbox. Price presentation uses one fixed Sigil icon
column and draws the amount directly after that icon for every row.
Configured NPC portrait images fill closer to the frame interior so portrait
cards do not show a black bottom gutter. Trader catalog rows show only title,
finite stock, and price; authored subtitles/descriptions belong in the item
tooltip or detail text, not in the compact row.

## Trade Jurisdiction And Quantity Quotes

Every future merchant placement must have one auditable Realm or world tax
registration. Definitions provide `tax-jurisdiction: auto|realm:<id>|world:<id>`
policy; placed NPC state owns the resolved registration. NPCs owns the catalog,
session, quantity request, and purchase journal. Economy alone resolves price,
tax rate, tax recipient, and payment receipts. The trader UI displays only
server-authored unit price, quantity bounds, subtotal, tax, and total.

Selecting a compact catalog row opens a bounded panel with minus/plus/Max,
quantity, subtotal, authority tax, total, and Confirm. Quantity counts offer
units and every change requests a fresh server quote. Confirm sends a bounded
server-authoritative trade request; after a successful Buy or Sell mutation the
server also sends a fresh `NpcTradeSnapshotPayload` so visible stock, revision,
and eligibility match persisted state without requiring the player to reopen
the trader.

## Narrative Readiness

The completed narrative foundation is data-driven and has NPC-owned
relationship and durable story-state services.

Graph validation V1 rejects missing roots, broken `next` targets, unreachable
nodes, duplicate option ids, duplicate text-variant ids, blank structural ids,
and service presentation nodes with no exit option. Core does not store NPC
relationship or story state; NPCs may later contribute safe summaries through
the Core profile contributor API.

NPC relationship V1 is stored by NPCs per player UUID and placed NPC UUID in
`world/elarion/addon-state/npcs/relationships.json`. Use registry handlers
instead of direct storage access:

- `elarion_npcs:set_relationship` with `value` or `score`.
- `elarion_npcs:add_relationship` with `amount` or `delta`.
- `elarion_npcs:relationship_at_least` with `minimum` or `value`.
- `elarion_npcs:faction_reputation_at_least` with a stable `faction` and
  either numeric `minimum` or standing `hated`, `disliked`, `neutral`,
  `liked`, or `loved`.

Every NPC definition has a stable `faction` (`realm:<id>`, `worldheart`,
`underworld`, or a custom id). `NpcRelationshipService` maintains in-memory
per-player/per-faction count and score totals while loading and mutating
records. `NpcReputationTabProvider` reads those bounded summaries; opening the
Character Menu does not scan relationship storage or copy NPC state into Core.
`ElarionNpcApi.reputation()` exposes the same read-only O(1) summary for
shop, quest, title, and reward integrations. Standing bands are 120 points and
the UI reports progress within the current band; domain consumers must use this
API or the registered condition instead of duplicating thresholds.

Each handler defaults to the current dialogue NPC from registry execution
metadata. Optional `npc`/`npcId` can target another placed NPC UUID, but do not
use display names or definition ids as persistence keys. V1 score changes are
silent: they do not emit history, Chronicle entries, notifications, or profile
summaries.

Durable story state lives in
`world/elarion/addon-state/npcs/story-state.json` (schema 1), keyed by player
UUID and placed NPC UUID. Options may declare `one-time: true`; successful use
is persisted under the stable `dialogue/node/option` key. Available story
handlers are `elarion_npcs:set_story_flag`, `clear_story_flag`,
`story_flag_set`, `set_ending`, `ending_is`, and `set_reentry_node`.
Unsupported schemas fail without discarding state. A stored missing re-entry
node falls back to the dialogue root; `close: true` closes the successful
conversation server-side. Interrupted conversation resume, per-NPC
relationship labels/tiers, graph visualization, localization-key validation,
and relationship-milestone Chronicle families remain future owner slices.

`history-worthy: true` requires `history-outcome`. After every option action
succeeds, NPCs records one structured `npc/story-outcome` event with a
persisted Chronicle variant id. `NpcChronicleText` provides ten authored
variants. Ordinary dialogue, relationship score changes, bank use, and trades
remain silent.
