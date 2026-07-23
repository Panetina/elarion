# PLANS

Future ideas and design direction. This file is not current implementation work.

## Confirmed Future Directions

- Complete the Atlas program described in `docs/systems/Atlas.md`; the current
  module is only a data-free `M`-key client shell.
- Rich Chronicle archives and public-history read models.
- Newspaper, ledger, NPC rumor, and search views backed by indexes or archives.
- Expanded civic systems for Government once the base founding flow stabilizes.
- More reusable UI surfaces built on the shared Core UI primitives.
- Website / bridge integration using explicit APIs and read models, not direct
  raw file reads.
- Custom client main-menu and pause-menu shell for Ashes of Elarion branding,
  direct server entry, credits, news, donation status, and local branding
  overrides.
- Notification-rail art refresh using the curated Core icon library and one
  consistent unread indicator across Personal, Realm, Quest, and World.
- Server-authored per-player quest-availability markers above placed NPCs.
- Ruler's Seat tax management menu backed by Economy-owned tax and treasury
  contracts.
- Realm and Guild 32x32 heraldry using one reusable validated pixel editor.
- Rename the complete player-organization domain from Group/Groups to
  Guild/Guilds, then add the Guild management UI and NPC creation flow.
- Underworld activity progression, Obol escape routes, and persistent personal
  Limbo islands.
- World of Warcraft-style chat channel selector for available public, Realm,
  authority, alliance, Guild, private-message, and future channels.
- Jail gameplay built from the existing addon shell.
- Chronicle archive and library surfaces backed by bounded indexes.
- Realm diplomacy presentation and enforcement built on Core's existing war,
  peace, alliance, embargo, neutrality, and hiding decision foundation.
- A deferred physical Marketplace area with entry NPC plus
  server-authoritative shop and barter UI.
- A monthly-only USD 5 `Supporter` membership built around cosmetic/community
  recognition and bounded convenience, never competitive or governing power.

## Atlas Program

Atlas is staged so the player-facing map is not exposed before its bounded
storage and authorization paths exist:

1. Add Core-owned map-feature, world-access, change-event, and fixed-palette
   pixel-asset contracts without moving canonical addon state into Core.
2. Add lazy 32x32-chunk terrain regions, validated shared/personal discovery,
   bounded persistence, and server-masked viewport networking.
3. Replace the placeholder with the full Elarion Atlas and event-driven Realm,
   Shrine, Portal, and placed-NPC projections.
4. Add one persistent personal placed-NPC target with Atlas highlighting and a
   direction/distance HUD cue.
5. Add Government-owned 32x32 ruler-published heraldry and its Seat of Rule
   editor using the shared pixel-asset contract.
6. Add batched bridge regions, dedicated indexed website map read models,
   bounded viewport APIs, `/worlds` preview, and `/worlds/map` expanded view.

The authoritative ownership, visibility, access, and performance contracts are
in `docs/systems/Atlas.md`. Economy is not an Atlas dependency.

## Future Gameplay And Social Systems

### Notification And Quest Feedback

- Replace the Personal Mail and Realm Mail rail textures with suitable icons
  selected from Core's curated art library through `ElarionUiIcons`. Quest and
  World keep distinct semantic icons; screens must not reference arbitrary
  library filenames directly.
- Render the same small outlined unread indicator on all four category icons.
  Use both shape and color so unread state does not depend on red alone, and
  derive it exclusively from the synchronized notification snapshot.
- Add a compact exclamation marker above an NPC when that NPC has an available
  quest for the viewing player. Quest ownership and eligibility remain in
  Quests; NPCs owns placed-entity presentation.
- Reuse the existing overhead label/marker layering convention used by the
  leader crown, generalized through a small reusable marker renderer rather
  than adding a second full NPC renderer or screen-space polling loop.
- Publish bounded per-viewer marker deltas on quest/placement/tracking changes
  instead of checking every quest against every NPC every render tick. The
  client renders only the server-authorized entity UUIDs currently in range and
  clears stale markers on unload, completion, disconnect, and definition
  reload.

### Government And Realm Politics

- Add a `Taxes` tab to the Seat of Rule. Government owns the block session,
  authority presentation, and permission check; Economy remains the canonical
  owner of tax authorities, categories, policy revisions, treasury accounts,
  quotes, settlement, and mutation.
- Show one validated slider plus exact numeric value for each Economy-owned tax
  category, initially NPC trade, Portal service, Marketplace, and general
  service. Rate bounds and steps come from Economy policy descriptors rather
  than hard-coded screen constants. Show a server-authored example subtotal,
  tax, total, and destination before confirmation.
- Resolve whether a transaction belongs to a Realm or Worldheart from its
  server-owned location/service authority. A Realm ruler may edit only that
  Realm's rates and cannot redirect Worldheart revenue. Worldheart policy uses
  its own authority surface. Stale policy revisions fail and refresh rather
  than overwriting newer changes.
- Add a dedicated `Heraldry` tab to the Seat of Rule. The active Monarch or
  President can draw and publish one 32x32 Realm shield/banner through the
  shared fixed-palette pixel editor. Government owns the Realm image and
  revision; Core owns only the reusable validated pixel-asset/editor contract.
- Validate palette, dimensions, payload bytes, authority, block session,
  revision, and update rate on the server. The first consumer is a small icon
  before the speaker's nickname in Local, Realm, Alliance, and Guild chat;
  later consumers may include Atlas and Character Menu projections.
- Core already owns canonical Realm relationships and decision state. Add a
  Seat-of-Rule `Diplomacy` presentation for declare war, propose alliance,
  return to neutrality, end war/make peace, embargo, and future truces/treaties
  without moving that state into Government.
- Define unilateral versus mutual decisions, vote/authority requirements,
  cooldowns, start/end timestamps, safe zones, PvP implications, embargo
  effects, notifications, Chronicle events, and restart behavior before any
  relationship begins enforcing gameplay rules.

### Guilds

- Execute one explicit `GUILD-01` migration that renames the player-facing and
  technical organization domain from Group/Groups to Guild/Guilds: UI text,
  commands, help, config descriptors, Java types/APIs, events, placeholders,
  docs, tests, module/package/mod identifiers where safe, and website/Discord
  projections. `/gc` remains the short command and means Guild Chat; `/group`
  becomes `/guild`.
- Preserve existing worlds through versioned migration of Group records,
  invites, memberships, config, projection keys, and storage paths. If a
  command compatibility alias is required, keep it for one documented release
  only; do not maintain two organization managers or two canonical stores.
- Use one Guild model with no Guild types. Creation asks only whether the Guild
  is `Secret`. Religious, criminal, cult, revolutionary, trade, military, and
  other themes remain player-authored RP expressed through the name,
  description, icon, roles, announcements, and behavior rather than type
  enums.
- A designated Guild Registrar NPC opens the creation flow, but Guilds owns
  validation, membership, persistence, roles, permissions, icon, and
  progression. Economy handles the creation fee and later upgrade charges
  through its retry-safe transaction API; NPCs stores no Guild truth.
- Add a Guild UI with Overview, Announcements, Members, Roles, and Invitations
  sections. Secret Guilds must not appear in public listings or unauthorized
  profile/search projections; direct invitations and server-authorized access
  remain possible.
- Announcements are bounded, persisted Guild records. Authorized publishers
  create one announcement that fans out through Core notifications to the
  current member audience; opening a screen must not scan notification history
  or every Guild file.
- Add custom named roles backed by a fixed Guild-owned permission registry.
  Initial checkboxes cover invite, remove member, publish announcement, assign
  eligible roles, edit Guild details, redraw icon, and manage roles. The owner
  can rename the owner role but cannot remove its invariant authority or grant
  Core/admin/Government permissions.
- Add a 32x32 Guild icon editable by the owner or a role with `redraw_icon`.
  Reuse the same validated pixel editor as Realm heraldry while Guilds owns the
  bytes and revision. The icon may prefix Guild Chat and later appear in Guild
  UI, Character Menu, and authorized projections.

### Underworld And Limbo

- Keep moderation banishment distinct from death and Jail sentences. Future
  Underworld skyblock/NPC actions must continue consulting Core's movement-only
  restriction gates so banished players can observe but cannot progress.
- When a Core admission queue exists, its pressure transition and join path
  must consume the UUID-only `queued_admission` restriction: banished accounts
  are disconnected while anyone is queued and cannot return until both free
  capacity exists and the queue is empty.
- Complete `F-01` first: Living and Afterlife inventories are separate for
  every player regardless of rank or game mode. The corpse retains living-world
  death items; the persistent Afterlife inventory retains Obols and approved
  Underworld/Limbo resources between visits and cannot leak them into living
  worlds. Underworld deaths never overwrite the living corpse snapshot.
- Disable player-versus-player damage throughout Underworld and Limbo. Banished
  movement-only players may not pick up items, collect XP, use inventory
  mutations, trade, break/place blocks, attack, or trigger progression even if
  they have operator status; explicit administrative commands remain separate.
- Target the ordinary death sentence at five minutes plus one minute per prior
  death for the current character. A death while already in the Underworld adds
  one minute to the current sentence. Confirm the counter reset/cap policy
  before changing the current configurable PvE/PvP/authority defaults.
- Add bounded activities in the village, fungal biome, Ascent, docks, quarry,
  PvE arena, and maze. These may award Underworld-owned Obols and approved
  Afterlife resources through server-authoritative events and reward actions.
  Obols can pay for an early return without bypassing corpse recovery.
- When the calculated sentence reaches 30 minutes, send one deduplicated
  personal notification directing the player to a configured NPC. That NPC can
  charge one retry-safe Obol fee to unlock the player's permanent Limbo access.
- Limbo is a shared managed world containing indexed, persistent personal
  vanilla-skyblock islands. Each island begins with an 8x8 logical boundary and
  expands through validated Obol purchases. Load island state lazily; never
  scan every island or player file on ordinary travel, border, or login paths.
- Add a custom 2x2 black-and-white Limbo Bell based on the vanilla bell: white
  metal, black supports, creative-inventory entry, server-owned block entity,
  and safe interaction bounds. An admin command registers the authoritative
  Underworld entry bell. Ringing it sends an unlocked player to their indexed
  island and island creation generates a linked return bell automatically.
- Entry/return links, island allocation, unlock/payment receipt, border
  upgrades, inventory handoff, restart reconciliation, missing/duplicate bell
  repair, and safe destination checks are Underworld-owned and tested. Worlds
  owns only the managed-world definition/protection integration.

### Jail

- Implement one canonical Jail service in the existing addon shell. V1 should
  use one Worldheart jail for the simplest consistent rules; optional Realm
  jail placements can follow later while sharing the same sentence and custody
  store rather than creating one manager per Realm.
- Arrest requires a Government-authorized officer or explicit admin path, a
  persisted reason/warrant, the target in the same world and short range, and a
  visible custody countdown that cancels when range/authority is lost. Do not
  allow silent or arbitrary remote player arrests.
- Store issuer, jurisdiction, reason, start, served-online time, absolute safety
  expiry, release state, and escape state. Use online served time with a hard
  real-time maximum so logging out neither serves the full RP sentence nor
  leaves a player jailed indefinitely.
- Default prisoners to movement, Local chat, private messages, and explicitly
  approved NPC/block interactions. Block teleport, Realm/Alliance/Guild chat,
  trade, combat, inventory exploits, and unrelated commands through Core's
  shared restrictions rather than Jail-specific command blacklists.
- Protect the physical prison with Worlds rules. A future breakout is one
  server-authored route such as lockpick, hidden passage, or bounded parkour;
  ordinary block breaking never escapes custody. Success ends confinement but
  records an `escaped/wanted` state for Government, notifications, and RP.
- Before implementation, confirm whether V1 remains Worldheart-only and whether
  breakout ships initially or after the fair custody/release loop is proven.

### Chat And Communication

- Add a compact World of Warcraft-inspired channel pill on the left side of the
  `T` chat edit box. Clicking it opens a short colored list of only the channels
  currently available to the player: Local, Realm, Guild, Alliance, and PM.
  Local is selected whenever a new `T` chat box opens.
- Preserve fast keyboard use: focus remains in the edit box, Escape closes,
  Enter sends, and Tab/Shift+Tab may cycle the server-provided eligible channel
  list without inserting slash commands. Keep each channel visually distinct
  and keep the selected PM target visible beside the channel label.
- PM opens a bounded searchable dropdown of eligible online citizen nicknames,
  respecting the existing local/global/Realm visibility policy. Display only
  the chosen nickname, send the stable UUID in a typed request, and let the
  server resolve current identity; never parse display text or autocomplete an
  account username into a client-authored command.
- Channel selection is presentation only. Core/Guild/Government services still
  validate membership, alliance, Underworld/Jail restrictions, ignore state,
  rate limits, distance, and recipients for every message. Existing `/rc`,
  `/ac`, `/gc`, `/pm`, `/w`, `/yell`, and `/r` paths may remain as validated
  keyboard fallbacks where policy permits.
- Remove vanilla `/say` from the command dispatcher and cover registration,
  help, permission, and execution/bypass tests. Normal typed Local chat and all
  fallback commands must route through Core so `/say` cannot bypass proximity,
  Underworld, banishment, or Jail restrictions.
- Chat rendering may prepend the viewer-authorized 32x32 Realm or Guild icon at
  a compact scale before the nickname. The message payload carries stable asset
  identity/revision, not raw image bytes per message.

### Chronicles, Archive, And Library

- Build Chronicle archive and library screens on dedicated bounded indexes or
  archive summaries. Support browsing and search without scanning raw JSONL or
  loading all historical records during ordinary player actions.

### Marketplace

- Keep Marketplace explicitly deferred until the tax, trader, Guild, and
  authority contracts above are stable. Do not begin it in the current slice.
- Later build a physical Marketplace area with an entry/shop NPC and
  server-authoritative shop and player-to-player barter UI. Economy owns prices,
  balances, tax authority, settlement, and transactions; Marketplace blocks,
  NPCs, barriers, or stalls contribute only their explicit presentation,
  access, and inventory contracts.

## Monthly Supporter Membership

Offer one role named `Supporter`, not `Support`, so it cannot be confused with
a staff or moderation role. The intended price is USD 5 per month with no
lifetime purchase. It exists to recognize recurring server support without
selling gameplay, economic, political, or administrative power.

### Player Benefits

- Give active Supporters queue priority only after a real admission queue
  exists. Admission must never evict an online player, and weighted/fair
  selection must guarantee that ordinary players cannot be starved forever.
- Replace unconditional AFK immunity with capacity-aware grace. Supporters may
  remain idle longer while spare capacity exists, but ordinary pressure rules
  return when players are queued; the entitlement may not enable AFK farming
  or bypass anti-abuse checks.
- Provide a toggleable Supporter title, chat/name badge or restrained name
  accent, and a synchronized Discord `Supporter` role.
- Candidate community-only recognition includes an opt-in credits entry,
  development previews, changelogs, and polls about cosmetics or community
  events. Support goals may unlock server-wide rewards for everyone.
- Do not sell capes or cape-like presentation. Do not add currency, tax, XP,
  loot, land, inventory, home, travel, marketplace, Guild, Realm diplomacy,
  moderation, punishment, or rules advantages.

### Ownership And Lifecycle

- The website and payment provider own subscription state, payment/purchase
  history, renewal, cancellation, refunds, and paid-through time. Fabric must
  never store payment credentials or call the billing provider in a login,
  admission, chat, or other gameplay hot path.
- Project one signed, revisioned, expiring Supporter entitlement linked to the
  canonical Minecraft UUID. Core owns its bounded runtime cache and exposes
  reusable ability/presentation checks; titles, chat, admission, AFK policy,
  and Discord synchronization consume that contract without inventing their
  own supporter flags.
- Payment webhooks update the website first. Bridge delivery and periodic
  bounded reconciliation must be idempotent, tolerate duplicates and
  out-of-order events, and preserve the last valid paid-through expiry during
  temporary outages.
- Cancellation retains benefits only through the paid billing period. Failed
  renewal behavior and any short grace period must be explicit. Every manual
  override requires an expiry, actor, reason, and audit record; permanent
  grants are not allowed.
- Discord role assignment/removal derives from the same entitlement and is a
  retryable external projection, never canonical permission or identity truth.

### Release Gates

- Test activation, renewal, cancellation, expiry, refunds/revocation, duplicate
  and reordered webhooks, clock boundaries, restart restore, bridge outage,
  Discord retry/removal, queue fairness, AFK capacity pressure, and denial of
  unrelated permissions.
- Publish the exact recurring price, benefits, renewal/cancellation/expiry and
  refund behavior, seller contact, purchase history, privacy handling, and the
  required unofficial-Minecraft disclaimer before purchase or server sign-in.
- Recheck the current Minecraft Usage Guidelines immediately before launch;
  queue and AFK behavior must remain noncompetitive and must not harm ordinary
  players' experience.

## Future Main Menu And Pause Menu Redesign

Build a Core-owned, client-side menu shell that replaces the vanilla-first
entry flow with an Ashes of Elarion presentation while staying modular,
configurable, and safe to maintain across Minecraft updates.

- Replace the vanilla Multiplayer-first flow with a direct primary action such
  as `Join Ashes of Elarion`, backed by configurable server address metadata
  and normal client connection handling.
- Hide or disable vanilla entry points that do not belong in the Elarion
  launcher-style experience, including `Minecraft Realms`, the default language
  icon button, and the default accessibility icon button.
- Do not remove accessibility access entirely. If the default accessibility
  button is hidden, expose an equivalent Elarion-styled accessibility/settings
  path from the redesigned menu before the slice is considered complete.
- Remove or restyle default bottom-bar text only where Fabric/Minecraft
  licensing and attribution requirements still remain satisfied. Keep required
  version, modded, copyright, and legal notices available in an appropriate
  place.
- Add a `Credits` surface with explicit categories for art sources, asset
  licenses, helpers, contributors, tools, libraries, and any external reference
  material used by the project.
- Add a bounded remote news panel on the main page, sourced from a server-hosted
  file or API. Cache it locally, cap entry counts and text length, tolerate
  offline/failure states, and never block the menu render path on network IO.
- Add a donation/hosting-goal widget to both the main menu and ESC pause menu.
  The widget should consume a small generated summary from the existing
  BuyMeACoffee script or a server-hosted bridge file, not scrape or query heavy
  sources directly from the client.
- Make the menu skin locally configurable: logo, background/resource pack
  references, window title text, colors, button labels, enabled options, news
  endpoint, donation endpoint, and credits data should come from explicit
  client config or packaged resource metadata rather than hard-coded constants.
- Keep the implementation behind stable menu/theme descriptors so future
  resource packs or server-specific branding can replace the look without
  touching connection, news, credits, or donation logic.
- Use shared Core UI primitives where practical, but isolate vanilla-screen
  mixins and menu replacement hooks from gameplay UI so Minecraft update churn
  does not destabilize in-game screens.
- Add focused client tests or snapshot-style checks for config parsing,
  fallback defaults, disabled vanilla entry points, remote-news cache bounds,
  donation-summary parsing, and credits category rendering before making this
  player-facing.

## Approved Option A UI Revamp Program

The Civic Ledger visual direction under `docs/ui/revamp-option-a/` is the
approved target for custom Elarion screens that have not received a complete
reference-aligned redesign. This is a multi-slice program, not authorization
for a broad rewrite.

- Generalize the current `Collection` into a broader player hub. `Citizen
  Ledger` is now the player-facing shell label and `/ledger` command alias,
  while Collection remains the internal API/packet/runtime name for the
  unlockables subsystem.
- Keep Profile and Unlockables as separate sections in the same modular shell.
  Candidate profile projections include completed quests, public reputation,
  Realm membership, Offering contribution score, public civic roles, deaths,
  recent history, and other visibility-safe addon summaries.
- Core owns the shell and profile aggregation contract. Quests, NPCs,
  Offerings, Government, Economy, Mounts, Underworld, and other addons keep
  canonical ownership and contribute filtered presentation sections.
- Keep character creation and Realm placement as separate onboarding stages.
  Collect identity and biography first, then let the server enforce
  population-balanced Realm placement.
- Add a Core-owned character-life path selection at any clear point in the
  existing three creation steps (name, biography, or Realm placement). `Ember`
  remains the ordinary character path and uses the normal death/Underworld
  loop. `Ashen` is an explicit opt-in hardcore path: on death, the character
  remains in the Underworld indefinitely and cannot resume living-world play.
  The player may remain there permanently or voluntarily surrender the soul to
  retire it and begin a new character.
- Ashen surrender is irreversible in ordinary play and needs a clear
  confirmation screen. Core Character Lifecycle owns the life-path value,
  death transition, retirement record, identity withdrawal, backup manifest,
  audit trail, and replacement-character eligibility; Underworld only owns the
  dead-state experience and emits the death handoff. No addon may infer or
  duplicate the path from its own storage.
- While living, an Ashen character visibly carries the `Ashen` title in place
  of the ordinary `Ember` identity title. Titles owns this presentation through
  Core's canonical life-path projection; Government remains the owner of civic
  office titles, but renders the Ashen prefix for an Ashen office holder (for
  example `Ashen Monarch` or `Ashen President`). This is identity presentation,
  not a second office or permission tier.
- Retirement must immediately replace the Ashen name with a stable censored
  label in player-facing Chronicle/history, existing and future books, website
  projections, leaderboards, chat references, and other identity renderers.
  Do not solve this with unbounded raw-history scans: add a bounded identity
  redaction resolver plus owner-maintained subject/projection indexes and
  queued rebuilds where materialized display text exists.
- Delete active character identity, progression, and player-facing projections
  through the same domain-owned reset handlers used by player reset; preserve
  encrypted/access-controlled recovery backups with a documented retention
  period and an audited owner-only restore path for verified bugs, exploits,
  or account compromise. Restoration must be explicit and must rebuild the
  redacted projections consistently.
- Keep the existing Shrine information layout and interaction structure. Its
  future work is an art, framing, spacing, and shared-control revamp only, not
  a three-column dashboard or feature expansion.
- Give quest NPCs, bankers, and traders simple role-specific interfaces. Quest
  dialogue is one current line, up to three choices, and an optional compact
  quest strip. Banker UI is a compact Deposit/Withdraw flow with balances,
  amount controls, confirmation, and a short transaction list. A trader may
  expose Talk, Quests, and Trade entry actions, but trading opens a dedicated
  server-authoritative trade surface.
- Keep Portals as compact authoritative pop-ups: scheduled/gated ticket,
  neutral no-fee passage, and fee/currency ticket. Do not add a Portal menu.
- Keep Grave Recovery as a separate full menu.
- Add bounded generic event feedback for unlocks, quest transitions,
  reputation changes, and available rewards by projecting existing Core
  events/notifications rather than creating another persistence system.
- Apply the same approved visual language to Shrine/Offerings, Admin Panel,
  NPC role surfaces, onboarding, Grave Recovery, and future player-hub views.
- Build a curated Option A asset bank before broad UI migration. The bank
  should eventually contain hundreds of implementation-ready source assets, but
  generated art must be indexed and sized for actual SMP usage rather than
  produced as untracked concepts. Initial target sizes: `32x32` semantic
  tab/row/category icons, `16/24/32` control icons where needed, compact
  ticket/popup ornaments and stamps, panel/chrome texture slices, approved
  portrait or NPC-role art, and screen-family reference sheets. Keep working
  references under `docs/ui/revamp-option-a/`; promote final PNGs into module
  resources only in the screen slice that consumes them.

Recommended dependency order:

1. Audit current Collection contracts, candidate rename impact, and profile
   data ownership.
2. Produce the first indexed Option A asset-bank manifest and priority list,
   then generate only the assets needed by the next approved screen slice.
3. Define the Core profile aggregation/presentation boundary with conservative
   server-side visibility.
4. Rebuild the shell as Profile plus Unlockables without changing addon state
   ownership.
5. Migrate onboarding, the layout-preserving Shrine reskin, simple
   role-specific NPC surfaces, Portal pop-ups,
   Grave Recovery, and generic event feedback in separate approved slices.
6. Run live screenshot comparison against the approved boards after every
   migrated family.

## Active Design Constraints

- Do not expose raw JSONL or raw runtime files as the long-term player-facing
  interface for growing history systems.
- Do not create duplicate managers, services, screens, or persistence layers.
- Keep special-case addon logic behind addon APIs or registries.
- Preserve the current separation between Core truth and addon behavior.

## Notes

- Some older "Contribution" wording has already been replaced by Shrine /
  Offering terminology in the implemented systems.
- Government, Guilds (currently implemented under the Group domain pending
  `GUILD-01`), and Portals should continue to grow as modular systems, not as
  one monolithic civic package.
