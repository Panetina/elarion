# Elarion NPCs Addon

Last reviewed: 2026-07-06

Author: Panyel  
Team: Panetina Team

`addons/npcs` owns static placeable NPCs, skin/profile definitions, dialogue
trees, dialogue sessions, and NPC interaction dispatch. It does not own
wallets, quests, titles, portals, laws, ledgers, Realm membership, or other
addon state.

## Storage

Editable definitions:

```text
config/elarion/addons/npcs/npcs.yml
config/elarion/addons/npcs/skins.yml
config/elarion/addons/npcs/portraits.yml
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
skin and portrait profiles, dialogue graph summaries, and dialogue UI settings
through `ElarionApi.system().configs()`. Discovery reads validated in-memory
snapshots; it does not read placement state or change dialogue sessions,
reload behavior, config files, packets, or persistence.

## Behavior

- NPCs use the dedicated registered entity type `elarion_npcs:npc`.
- NPC entities are player-sized, invulnerable, silent, persistent, fixed,
  non-pushable, and have no active AI/pathfinding behavior.
- `placed-npcs.json` is canonical. The entity stores only the placed NPC UUID
  link and is reconciled from the placement record.
- NPC definitions may include tags, an optional required Core ability, and an
  optional interaction range override. Tags are for admin filtering and future
  Atlas/service grouping; they do not create gameplay ownership.
- NPC definitions are validated on server start and reload, after addons have
  had a chance to register their action/condition handlers.
- Right-click opens a server-authoritative dialogue GUI.
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
  NPC conversations remain crisp. It renders a two-row conversation layout: NPC
  portrait plus NPC dialogue box first, then player dialogue box plus player
  head. The whole logical canvas scales uniformly on smaller windows instead of
  compressing individual rows into overlap.
- The dialogue GUI uses shared Core UI primitives and civic helpers for the
  shell, option buttons, footer Close button, numeric prompt frame, and prompt
  input surface. NPCs still own portrait rendering, conversation phases,
  typing, prompts, relation hearts, cards, and sounds.
- Initial dialogue types the NPC response. After selecting an option, player
  text types first, then the NPC response, then inputs become active. Clicking,
  Enter, or Space completes only the active phase. Typing is presentation only;
  server validation and action execution remain authoritative.
- Long response lists render only visible rows and preserve scroll/highlight
  position per NPC/dialogue/node. They support mouse wheel, draggable scrollbar
  thumb, track paging, Up/Down, Page Up/Page Down, and Enter selection.
- The footer contains only a centered Close button. ESC also closes the
  conversation.
- The payload has extension zones for a top-right bank currency badge,
  reward/shop/service preview cards, and future NPC relation/reputation values.
  Relation is shown as theme-colored Minecraft-style hearts without a permanent
  text label. Hovering the hearts shows the current relation level in a standard
  tooltip. NPCs only display those
    fields; Economy, Ledger, Quest, Trade, or other systems remain the owners of
    the underlying state.
- Placed NPCs have readable command IDs such as `worldheart_banker_1`. The
  internal UUID is still stored for persistence and entity linking.
- Portrait fallback order is explicit portrait, portrait fallback texture,
  synced NPC skin head, then placeholder. A full 64x64 texture skin such as
  `dunk_banker` therefore provides its own dialogue head when no portrait image
  exists.
- Ordinary dialogue browsing should not emit history. Only meaningful outcomes
  such as purchases, quest completion, civic registration, lore discovery, and
  government actions should emit history.

## Commands

All commands are OP level 4:

```text
/e npc reload
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

Examples:

```text
/e npc place worldheart_banker north
/e npc place worldheart_banker yaw -45
/e npc set skin worldheart_banker_1 configured_player_body
/e npc set portrait worldheart_banker_1 player_head
/e npc set dialogue worldheart_banker_1 worldheart_banker
/e npc duplicate worldheart_banker_1 east
/e npc tp worldheart_banker_1
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

## Future Work

- Add richer idle animation and explicit skin validation only if the static
  presentation needs them.
- Add NPC action handlers for market entry, portal tickets, quest boards,
  government pages, title selection, Offering projects, and ledger views.
- Add NPC rumor and onboarding content on top of public-history and Realm
  onboarding systems.
