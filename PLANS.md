# PLANS

Future ideas and design direction. This file is not current implementation work.

## Confirmed Future Directions

- Atlas / fantasy political map and minimap.
- Rich Chronicle archives and public-history read models.
- Newspaper, ledger, NPC rumor, and search views backed by indexes or archives.
- Expanded civic systems for Government once the base founding flow stabilizes.
- More reusable UI surfaces built on the shared Core UI primitives.
- Website / bridge integration using explicit APIs and read models, not direct
  raw file reads.
- Custom client main-menu and pause-menu shell for Ashes of Elarion branding,
  direct server entry, credits, news, donation status, and local branding
  overrides.

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
- Government, Groups, and Portals should continue to grow as modular systems,
  not as one monolithic civic package.
