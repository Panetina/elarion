# Elarion NPCs Addon

Last reviewed: 2026-07-09

Author: Panyel  
Team: Panetina Team

`addons/npcs` owns static placeable NPCs, skin/profile definitions, dialogue
trees, dialogue sessions, and NPC interaction dispatch. It does not own
wallets, quests, titles, portals, laws, ledgers, Realm membership, or other
addon state.

The default `guildmaster` NPC uses addon-owned skin and portrait assets and
opens the Guild Registrar through `elarion_guilds:open_registrar`.

## Storage

Editable definitions:

```text
config/elarion/addons/npcs/npcs.yml
config/elarion/addons/npcs/skins.yml
config/elarion/addons/npcs/portraits.yml
config/elarion/addons/npcs/trades.yml
config/elarion/addons/npcs/ui.yml
config/elarion/addons/npcs/dialogues/<dialogue-id>.yml
```

Runtime state:

```text
world/elarion/addon-state/npcs/placed-npcs.json
```

Dialogue sessions are currently in memory only. Add persistent session files
only if reconnect-resumable NPC conversations become a real requirement.

`NpcConfigDescriptors` registers the read-only `npcs` config domain after the
first successful server definition load. It exposes loaded NPC definitions,
skin and portrait profiles, trade catalog summaries, dialogue graph summaries,
and dialogue UI settings through `ElarionApi.system().configs()`. Discovery
reads validated in-memory snapshots; it does not read placement state or change
dialogue sessions, reload behavior, config files, packets, or persistence.

The generated default definition set includes `worldheart_banker` and
`worldheart_trader`. Existing server config files are not overwritten by the
default writer; add missing generated entries and
`dialogues/worldheart_trader.yml` manually or through an approved config
migration on older worlds. As compatibility bridges, a loaded
`worldheart_trader` definition with a missing or blank `trade-catalog` is
projected to the generated `worldheart_trader` catalog in memory, and the known
legacy `worldheart_trader.cobblestone_buyback` route is projected to
`destination-offer: cobblestone` when that field is missing. Custom trader
definitions and custom Sell stock routes still need explicit catalog and
destination IDs.
Definitions may set `tax-jurisdiction: auto|realm:<id>|world:<namespaced-id>`.
The policy is editable config; each placed NPC persists the resolved
`REALM|WORLD` jurisdiction in placement schema v2. `auto` prefers Core's
canonical Realm owner for the placement world and otherwise records the world.
Explicit policies reject placement or movement in a conflicting world.

Schema-v1 placement files are backed up as
`placed-npcs.json.schema-v1.bak` and atomically migrated after every record
resolves. Unreadable placement state is quarantined as
`placed-npcs.json.corrupt-<timestamp>` and starts empty; an unsupported schema
still fails closed. `/e npc reload` validates
all placements against the candidate definition snapshot and restores previous
definitions on failure before respawning entities.
The current shipped defaults use dedicated texture skins
`elarion:textures/entity/npc/worldheart_banker.png` and
`elarion:textures/entity/npc/worldheart_trader.png`, plus curated 32x32
portrait library assets
`portrait_character_portrait_icons_03_icons_03.png` and
`portrait_character_portrait_icons_27_icons_27.png`.

## Behavior

- NPCs use the dedicated registered entity type `elarion_npcs:npc`.
- NPC entities are player-sized, invulnerable, silent, persistent, fixed,
  non-pushable, and have no active AI/pathfinding behavior.
- `placed-npcs.json` is canonical. The entity stores only the placed NPC UUID
  link and is reconciled from the placement record.
- `/e npc inspect` reports the resolved tax jurisdiction for administrative
  verification.
- NPC definitions may include tags, an optional required Core ability, and an
  optional interaction range override. Tags are for admin filtering and future
  Atlas/service grouping; they do not create gameplay ownership.
- NPC definitions are validated on server start and reload, after addons have
  had a chance to register their action/condition handlers.
- Right-click opens a server-authoritative dialogue GUI.
- Every NPC opens through the standard conversation surface first. Service
  NPCs expose server-authored options such as `Open Bank`; selecting one moves
  the same validated dialogue session into a dedicated presentation node.
- Button clicks send only NPC/node/option IDs; the server validates range,
  active session, node, option visibility, conditions, and actions.
- Dialogue actions use Core registries. Future Economy, Portals, Quests,
  Government, Ledger, Titles, and Offerings actions should register
  handlers instead of adding hard-coded NPC logic.
- Economy currently registers NPC actions for bank-balance display, depositing
  submitted physical currency amounts, withdrawing submitted currency amounts, and
  older fixed/default helpers. NPCs do not mutate wallets directly.
- NPC `skin` and `portrait` are separate. `skin` is visible in-world
  presentation metadata. `portrait` is the image/profile shown in dialogue UI.
- NPCs renders a compact `!` overhead marker only when Quests has sent the
  viewer a bounded `NpcQuestMarkerSyncPayload`; NPCs never decides quest
  availability or stores marker state.
- Skin profile types:
  - `placeholder`: use the dedicated NPC renderer's safe default player body.
  - `texture`: render a player-model body with the explicit texture ID, useful
    as a configured fallback or controlled custom skin texture.
  - `player_body`: render the visible NPC body with a Minecraft player skin
    resolved from `player-name`, with safe default or configured texture
    fallback.
- Portrait profile types:
  - `placeholder`: framed text placeholder.
  - `texture`: render an explicit texture ID.
  - `player_head`: render the configured player's head from `player-name`, with
    safe default or configured texture fallback.
- Dialogue nodes support `presentation: dialogue|bank|trade`. `trade` renders
  a dedicated Buy/Sell shell. BUY purchases are server-authoritative through
  the NPC purchase journal, finite placed-NPC stock, and optional Economy
  settlement. SELL buybacks use server-side inventory escrow, idempotent
  Economy wallet payout, and optional placed-NPC stock replenishment. Dialogue options support
  `presentation-role` for stable UI roles such as `open_bank`, `open_trade`,
  `deposit`, `withdraw`, `buy`, `sell`, and `back`; clients never infer service
  behavior from translated labels or NPC names.
- Dialogue options support `button-text` for the clickable label and
  `player-text` for the right-side player response panel. `text` remains a
  fallback for simple options.
- Dialogue options may define a `prompt` block. The first prompt type is
  `number`, used by service NPCs such as the banker. The client only collects a
  bounded digit string; the server revalidates the session, range, option,
  amount, and action before Economy or another addon executes behavior. Enter
  submits a numeric prompt; Esc cancels the prompt and returns to the dialogue.
- Dialogue nodes and options support optional `sound` and `voice` metadata.
  `sound` is played client-side as a normal Minecraft sound event when its
  speaker starts typing. `voice` is reserved for future local NPC voice-line
  assets; it is not a Simple Voice Chat integration.
- Dialogue nodes may define ordered `variants`. Each variant has replacement
  `text`, optional `sound`/`voice`, and registered Core conditions. The server
  picks the first variant whose conditions pass and falls back to the node's
  default text. Use this for quest memory, relationship, Government, or
  Offering-specific lines without storing that state in NPCs.
- Action feedback is spoken as the NPC response inside the dialogue bubble by
  default. Economy remains the owner of wallet and transaction behavior.
- The dialogue GUI uses a flat configurable dim overlay and an opaque panel by
  default, and it explicitly opts out of Minecraft's menu blur hooks so in-world
  NPC conversations remain crisp. Simple quest dialogue uses the compact Option
  A hierarchy: larger NPC title, true NPC portrait, one current conversation
  body, at most three immediately visible choices, and a bounded metadata/card
  strip. Player and NPC typing phases share that conversation body instead of
  creating a second portrait row. The logical canvas scales uniformly on
  smaller windows instead of compressing rows into overlap.
- The dialogue GUI uses shared Core UI primitives and civic helpers for the
  shell, selected option borders, header Close control, numeric prompt frame,
  and prompt input surface. NPCs still own portrait rendering, conversation phases,
  typing, prompts, relation hearts, cards, and sounds.
- Initial dialogue types the NPC response. After selecting an option, player
  text types first, then the NPC response, then inputs become active. Clicking,
  Enter, or Space completes only the active phase when `typing-click-completes`
  is enabled in the synchronized NPC UI payload. Typing is presentation only;
  server validation and action execution remain authoritative.
- Long response lists render only visible rows and preserve scroll/highlight
  position per NPC/dialogue/node. They support mouse wheel, draggable scrollbar
  thumb, track paging, Up/Down, Page Up/Page Down, and Enter selection.
- The header contains a centered-glyph Close control. ESC also closes the
  conversation.
- Bank nodes render through a dedicated compact banking screen with the NPC
  portrait, deposited balance, Deposit/Withdraw modes, bounded numeric amount,
  preset amounts, server feedback, and Back to
  Conversation. Currency is presented with the shared Sigil icon in the balance
  badge, amount input, totals, and related controls. It reuses the existing
  prompt packets and Economy-owned action handlers; NPCs never mutate balances
  or inventory. Fee/Total previews use `NpcBankQuoteRequestPayload` /
  `NpcBankQuotePayload`; the server revalidates the active bank session and
  visible Deposit/Withdraw option before asking Economy for a quote. Confirm
  remains disabled until the latest quote matches the current mode and amount.
- Trade catalogs live in `trades.yml` as NPC-owned config definitions. NPC
  definitions reference a catalog by stable ID through `trade-catalog`.
- Trade nodes render through a dedicated compact Buy/Sell shell with the NPC
  portrait, Buy/Sell modes, a server-authored read-only catalog snapshot, and
  Back to Conversation. The current default catalog shows two Nether Gate
  Tickets, two End Gate Tickets, one Cobblestone, a Protection IV chestplate,
  and a named/lore Protection IV chestplate using native item rendering and
  Sigil price icons. Ticket offers may set `custom-model-data`; the default
  Nether and End rows use the same crimson/blue stele ticket art as real Portal
  ticket stacks.
- After a successful Buy or Sell mutation, `NpcInteractionService` sends the
  purchase/sale result and then a fresh `NpcTradeSnapshotPayload`. The client
  does not guess stock changes; visible stock labels refresh from the same
  server-owned placed-NPC stock state that is persisted on disk.
- Trade offers may also set `price-key`. It is a stable future Economy pricing
  hook for taxes, inflation, and dynamic merchant pricing; the current BUY
  settlement still uses the fixed `price` value plus server-authored tax. Offers
  may set `stock-limit`, `restock-amount`, and
  `restock-interval-seconds`; zero stock limit means unlimited. Finite stock is
  tracked per placed NPC, not per definition, and lazy restocks only run when a
  trader is opened, quoted, or purchased from.
- Trade offers may set `direction: sell` for Sell/buyback definitions. Sell
  rows parse `sell-match`, `component-policy`, `max-quantity`,
  `stock-destination`, and `destination-offer`, and the read-only config
  descriptors expose those fields. When `stock-destination: placed_npc` is set,
  validation requires `destination-offer` to point to a BUY offer in the same
  catalog. Completed sales then replenish that placed NPC's configured resale
  stock idempotently by sale ID. NPC config validation rejects prompts and
  executable dialogue actions on trade-node options so purchases and buybacks
  go through the dedicated trade request path.
- NPCs owns catalog/stock/session meaning; Economy owns currency and public
  treasury settlement through its public API. Implemented
  packets include `NpcTradeSnapshotPayload`, `NpcTradeQuoteRequestPayload`,
  `NpcTradeQuotePayload`, `NpcTradePurchaseRequestPayload`, and
  `NpcTradePurchaseResultPayload`. NPCs persists purchase journal records in
  `world/elarion/addon-state/npcs/trade-purchases.json` and finite stock in
  `world/elarion/addon-state/npcs/trade-stock.json`. Sell runtime storage is
  defined at `world/elarion/addon-state/npcs/trade-sales.json`, with explicit
  sale replay states and serialized escrow stacks. Sell settlement is
  server-authoritative: the server counts matching main-inventory stacks,
  serializes exact removed stacks with encoded `ItemStack` payloads, persists
  escrow before payout, pays the seller's Economy wallet through an idempotent
  receipt, restores escrowed items on payout failure when possible, and can
  idempotently route sold quantity into a configured placed-NPC BUY stock target.
  Exact sold stacks go into durable NPC escrow before any payout, and Economy
  owns dynamic price/inflation/payout policy.
- `worldheart_trader` is the default test trader route for this shell. It opens
  through normal NPC conversation first and then transitions through `Trade`
  into the trade presentation.
- Bank amount text starts immediately after the Sigil icon. The blinking caret
  uses the rendered amount width and stays after the final digit for typed,
  preset, and backspaced values. Fee and Total each use one scaled Sigil icon
  and one right-aligned numeric value.
- Bank Deposit/Withdraw mode is remembered per NPC on the client across the
  server feedback refresh that follows an action, so a Withdraw attempt stays
  on the Withdraw tab instead of returning to Deposit.
- Hovering a trade preview row uses the underlying `ItemStack` and Minecraft's
  native tooltip renderer, preserving enchantments, custom names, lore, and
  attributes without duplicating item metadata in the UI model.
- The payload carries an explicit presentation kind and option roles. It has
  extension zones for a top-right bank currency badge,
  reward/shop/service preview cards, and future NPC relation/reputation values.
  Per-NPC relationship is shown only when NPC-owned authoritative relationship
  data is available; the former hard-coded `Neutral/0` projection is no longer
  presented as real data. Future aggregate NPC-faction/Realm reputation belongs
  in Character Menu, not conversation or bank screens. NPCs only display those
  fields; Economy, Ledger, Quest, Trade, or other systems remain the owners of
  the underlying state.
- Placed NPCs have readable command IDs such as `worldheart_banker_1`. The
  internal UUID is still stored for persistence and entity linking.
- Portrait fallback order is explicit portrait, portrait fallback texture,
  synced NPC skin head, then placeholder. A full 64x64 texture skin such as
  `worldheart_banker` therefore provides its own dialogue head when no portrait
  image exists.
- Ordinary dialogue browsing should not emit history. Only meaningful outcomes
  such as purchases, quest completion, civic registration, lore discovery, and
  government actions should emit history.

## Commands

All commands are OP level 4:

```text
/e npc reload
/e npc open <npcId>
/e npc place <npcDefinition> [north|east|south|west|here]
/e npc place <npcDefinition> yaw <value>
/e npc remove <npcId>
/e npc remove nearest
/e npc face <npcId>
/e npc rotate <npcId> <north|east|south|west|here>
/e npc rotate <npcId> yaw <value>
/e npc repair <npcId>
/e npc repair all
/e npc tp <npcId>
/e npc duplicate <npcId> [north|east|south|west|here]
/e npc duplicate <npcId> yaw <value>
/e npc nearest
/e npc list [world|near]
/e npc list tag <tag>
/e npc inspect <npcId>
/e npc inspect nearest
/e npc move <npcId>
/e npc set name <npcId> <name>
/e npc set skin <npcId> <skinProfile>
/e npc set portrait <npcId> <portraitProfile>
/e npc set dialogue <npcId> <dialogue>
/e npc dialogue inspect <dialogueId>
```

`<npcId>` is the readable placed NPC ID shown by `/e npc place` and
`/e npc list`, for example `worldheart_banker_1`. Legacy UUIDs still work.
`/e npc open <npcId>` opens the normal server-authoritative conversation for a
nearby placed NPC and is intended for admin repair/QA flows; it still respects
range, definition availability, permissions, and dialogue validity.

Examples:

```text
/e npc place worldheart_banker north
/e npc place worldheart_banker yaw -45
/e npc set skin worldheart_banker_1 configured_player_body
/e npc set portrait worldheart_banker_1 player_head
/e npc set dialogue worldheart_banker_1 worldheart_banker
/e npc duplicate worldheart_banker_1 east
/e npc tp worldheart_banker_1
/e npc open worldheart_banker_1
/e npc nearest
```

`skin` controls the visible static NPC body. `portrait` controls the dialogue
portrait. Use a player-name skin like this:

```yaml
skins:
  configured_player_body:
    display-name: "Configured Player Body"
    type: "player_body"
    player-name: "Panyel"
    texture: ""
    fallback-type: "placeholder"
    fallback-texture: ""

portraits:
  configured_player_head:
    display-name: "Configured Player Head"
    type: "player_head"
    player-name: "Panyel"
    texture: ""
    fallback-type: "placeholder"
    fallback-texture: ""
```

Then apply them:

```text
/e npc set skin worldheart_banker_1 configured_player_body
/e npc set portrait worldheart_banker_1 configured_player_head
```

The server-side entity is a dedicated static Elarion NPC anchor with a
player-sized hitbox and dedicated normal/slim player-model renderer. Normal
villagers are never targeted. `/e npc face <id>` persists a one-time orientation;
it does not continuously track players. `/e npc repair <id|all>` reconciles
missing and duplicate anchors from canonical placement state.

Facing values use Minecraft yaw conventions:

```text
south = 0
west = 90
north = 180
east = -90
here = the executing player's current yaw
```

## Dialogue Authoring

Add one file per dialogue:

```text
config/elarion/addons/npcs/dialogues/<dialogue-id>.yml
config/elarion/addons/npcs/dialogues/<quest-id>/<actor>.yml
```

Dialogue loading is recursive. If a nested dialogue file does not define an
explicit `id`, the fallback ID is its slash path under `dialogues/`, such as
`generic_foundation/guide`.

Basic option shape:

```yaml
intro:
  text: "Welcome."
  sound: "minecraft:entity.villager.yes"
  voice: ""
  options:
    - id: lore
      button-text: "What are %currency_plural%?"
      player-text: "What are %currency_plural%?"
      sound: "minecraft:ui.button.click"
      voice: ""
      next: currency
```

Service node shape:

```yaml
intro:
  presentation: dialogue
  options:
    - id: open_bank
      button-text: "Open Bank"
      presentation-role: open_bank
      next: bank
bank:
  presentation: bank
  options:
    - id: deposit
      presentation-role: deposit
      prompt:
        type: number
        action: elarion:economy_deposit_currency_amount
    - id: back
      presentation-role: back
      next: intro
```

Legacy banker graphs that place Economy deposit and withdraw prompts directly
on the root node are projected into a `bank_service` node in memory. Files are
not rewritten and unrelated customized options remain on the conversation root.

Conditional node text variant shape:

```yaml
intro:
  text: "Welcome back."
  variants:
    - text: "You remember what I asked."
      conditions:
        - type: "elarion_quests:variable_equals"
          parameters:
            quest: "generic_foundation"
            realm: "realm1"
            variable: "remembered"
            value: "true"
```

Numeric service prompt shape:

```yaml
- id: deposit
  button-text: "Deposit %currency_plural%."
  player-text: "I would like to Deposit %currency_plural%."
  prompt:
    type: "number"
    question: "How many %currency_plural% would you like to deposit?"
    action: "elarion:economy_deposit_currency_amount"
    max-digits: 10
    min-amount: 1
  next: intro
```

Use `next` to move to another node and registered actions for real behavior.
Player-controlled closing is handled by ESC or the centered footer Close
button. Do not hard-code shop, bank, quest, or government logic into NPCs.

NPC `ui.yml` controls dialogue-specific layout, typing, portraits, and
interaction behavior. Shared colors, textures, borders, buttons, cards, and
scrollbars come from Core `config/elarion/core/ui_theme.yml` variant `npc`.

The dialogue screen consumes Core UI primitives while NPC-specific portraits,
conversation phases, typing, prompts, relations, and sounds remain NPC-owned.
Trade catalog rows keep real server-authored `ItemStack` previews for native
tooltips. The row itself may highlight on hover, but enchantment/lore tooltips
must only appear over the item icon. Price rows use one fixed Sigil icon
column and a value immediately after it.

## Trade Quote Integration

NPCs optionally integrates with Economy through `NpcTradeQuoteProvider` and
`NpcTradePurchaseProvider`. Without Economy, catalog rows remain visible and
disabled. With Economy, providers map persisted NPC jurisdiction to a Realm or
Worldheart authority.

The two shipped bank amount prompt IDs remain valid config contracts when
Economy is absent, but NPCs never registers their handlers. Unavailable
server-side quote/settlement providers keep the controls disabled and prevent
wallet or inventory mutation. Loader-level absence is verified by
`dev/tools/optional-addon-qa.ps1`.

Quantity and purchase requests are bounded to 1-64 and revalidate the dialogue
session, range, trade node, catalog revision, offer ID, item availability, and
server quote. The client receives subtotal, tax, total, policy revision, and
authority label; it never computes authoritative values. Confirm sends a
client-generated purchase ID, and the server records deterministic
PREPARED/PAID/COMPLETE/FAILED purchase state before responding.

## Future Work

- Dialogue graph validation V1 runs during config validation. NPC relationship
  V1 is persisted in
  NPC-owned state and exposed through `elarion_npcs:set_relationship`,
  `elarion_npcs:add_relationship`, and
  `elarion_npcs:relationship_at_least`. Durable story flags, one-time choices,
  endings, opt-in re-entry, and structured `npc/story-outcome` Chronicle
  records are complete under the contract in
  `docs/systems/NPCs.md`. Per-NPC relationship UI remains deferred; the
  Character Menu faction-reputation summary is already an NPC-owned bounded
  projection.
- Keep the remaining narrative work as separate owner slices: localization-key
  validation, graph visualization/developer tooling, server-authored per-NPC
  relationship labels, Quest-specific action/condition parameter validation,
  and durable conversation resume after disconnect/restart.
- Add richer idle animation and explicit skin validation only if the static
  presentation needs them.
- Add NPC action handlers for market entry, portal tickets, quest boards,
  government pages, title selection, Offering projects, and ledger views.
- Add NPC rumor and onboarding content on top of public-history and Realm
  onboarding systems.
