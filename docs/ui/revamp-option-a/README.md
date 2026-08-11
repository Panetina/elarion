# Option A - Civic Ledger UI Reference Set

Status: approved visual direction on 2026-07-07.

These boards define the target look for custom Elarion screens that have not
yet received a full reference-aligned redesign. They extend the Civic Forum's
dark brown, subdued gold, compact pixel-art language. They are implementation
references, not drop-in textures and not evidence that the depicted behavior
already exists.

Generate and curate implementation-ready PNG assets only in the screen slice
that consumes them. Do not promote concept-board crops into game resources.

### Runtime asset rules

- Use screen-sized, reviewed batches; never a speculative asset dump.
- Keep source/reference art in this directory until code consumes a final PNG.
- Promote final assets into module resources through semantic shared catalogs,
  not direct per-screen texture paths.
- Keep pixel-readable fixed source sizes: 16x16 controls, 24x24 emphasized
  controls, 32x32 semantic/row icons, 64x64 popup stamps, 128x128 or 192x192
  optional portraits, and tileable or 9-slice chrome.
- Preserve the civic brown/gold token language across domain screens. Assets
  are a feature dependency, not a standalone production program.

## Reference Boards

| File | Target surface |
| --- | --- |
| `00-option-a-overview.png` | Original approved direction board |
| `01-citizen-ledger-profile.png` | General player profile and civic summary |
| `02-citizen-ledger-unlockables.png` | Mounts, titles, rewards, and future unlock categories |
| `03-character-creation.png` | Character identity and biography step |
| `04-balanced-realm-placement.png` | Server-forced Realm placement after character creation |
| `05-shrine-offerings.png` | Current Shrine layout with civic art and UI polish |
| `06-npc-quest-dialogue.png` | Simple narrative/quest NPC conversation |
| `07-npc-banker.png` | Compact banker deposit/withdraw interaction |
| `08-npc-trader-dialogue.png` | NPC entry surface with Talk, Quests, and Trade |
  | `09-npc-trade-screen.png` | Original dense dedicated item/currency trading UI |
  | `09-npc-trade-screen-v2.png` | Approved compact Buy/Sell trade direction |
| `10-portal-ticket-popups.png` | Scheduled ticket, neutral passage, and fee ticket pop-ups |
| `11-grave-recovery.png` | Separate grave recovery menu |
| `12-admin-panel.png` | Dense civic-style administration surface |
| `13-generic-event-popups.png` | Mount/title/quest/reputation/reward event feedback |

Detailed boards use a `1536x1024` source canvas. The overview retains its
original `1672x941` selection-board canvas.

## Design Contract

- Use compact, bounded layouts with dark brown surfaces, restrained gold
  borders, green selection/success, red destructive states, and readable
  metadata hierarchy.
- Preserve Minecraft pixel readability. Reconstruct frames, controls, icons,
  spacing, and text using shared UI primitives and native assets rather than
  embedding a whole concept board as a texture.
- Treat `Character Menu` as the player-facing shell label and `/charactermenu`
  command alias for the current unlockables surface. Preserve Collection
  internals until a separately approved structural migration exists.
- Core owns the Character Menu shell and server-authoritative profile
  aggregation. Addons retain their canonical data and contribute bounded,
  visibility-filtered sections and actions through public contracts.
- Character creation and Realm placement are separate steps. Character
  identity/biography completes first; the server then assigns or constrains
  Realm placement according to authoritative population-balance rules.
- Portals use compact interaction pop-ups, not a route-browser menu. Support
  scheduled/gated tickets, neutral no-fee passage, and fee/currency tickets.
- Grave Recovery remains a separate full menu.
- Shrine keeps its current header/progress, left project/reward summary, right
  Contribute/History tabs, requirement rows, and bottom close-action layout.
  Revamp its art, framing, hierarchy, and controls without replacing its
  information architecture or adding dashboards, project browsers, filters,
  contributor rankings, or milestone timelines.
- Quest dialogue stays compact: NPC identity, one current dialogue area, up to
  three choices, and an optional small quest strip. Do not add relationship,
  biography, location, schedule, dialogue-history, or session dashboards.
- Banker UI stays compact and distinct from quest dialogue: balances,
  Deposit/Withdraw, amount controls, fee/total, confirmation, and a short
  recent-transaction list. Do not add treasury, account-detail, security, or
  administrative sidebars.
- Trader dialogue and dedicated trade remain distinct role-specific surfaces.
  Do not grow one universal NPC screen.
- Bank, trade, unlock, Realm, Offering, grave, and portal mutations are
  requests only on the client and are validated and applied by the server.
- Generic event pop-ups are presentation surfaces over existing events and
  notifications. They must not introduce another inbox or persistence owner.

## Implementation Order

Do not migrate every screen at once. Begin with a bounded audit of the current
Collection shell, provider/action contracts, profile data owners, and naming.
Define the Character Menu aggregation boundary before adding profile fields.
Migrate one screen family per approved slice and perform live screenshot QA at
Minecraft GUI scales after each family.
