# Character Menu And Collection Architecture Audit

Date: 2026-07-07  
Status: Phase 4, Slice 17A completed; Slice 17B wire prerequisite completed  
Scope: documentation-only audit; no production code changed

## Objective

Map the current Core Collection shell and define a compatibility-safe boundary
for the approved broader player hub before implementation begins.

This audit covers naming, commands/keybinds, packets, provider/action contracts,
data ownership, visibility, and bounded query requirements. It does not add a
profile API, rename production classes, or change runtime behavior.

## Current Collection Contract

Core currently owns:

- `ElarionCollectionService` and its `TabProvider` registry.
- `ElarionCollectionSnapshot`, tab, entry, and action presentation records.
- `CollectionOpenRequestPayload`, `CollectionOpenPayload`, and
  `CollectionActionPayload`.
- `ElarionCollectionScreen`, the `C` key opener, `/charactermenu`, and the
  client-only preview registry.
- A Titles provider backed by Core `CitizenRecord` and `TitleService`.
- A synthetic empty Pets tab.

Mounts owns mount unlock/active state and registers the only addon-provided
Collection tab. The server builds all tab snapshots eagerly when the menu
opens. The client receives presentation data, sends tab/entry/action IDs, and
the server dispatches the action to the owning provider.

This authority split is sound and should remain the Unlockables subsystem.

## Existing Compatibility Surfaces

The word `Collection` currently appears in externally or operationally
meaningful surfaces:

- `/charactermenu` and its help text.
- Translation key `key.elarion_core.collection` and default `C` keybind.
- Payload identifiers `elarion_core:collection_open`,
  `elarion_core:collection_open_request`, and
  `elarion_core:collection_action`.
- Public addon registration through `ElarionApi.system().collections()` and
  `ElarionCollectionService.TabProvider`.
- Java models, service, screen, tests, and preview registry.
- Mount config `config/elarion/addons/mounts/collection.yml`.
- Mount runtime state `world/elarion/addons/mounts/collection.json`.
- Mount texture/resource paths containing `collection`.
- Technical docs, wiki pages, and test instructions.

Renaming all of these would create risk without improving ownership. The
recommended migration is user-facing first:

- Use **Character Menu** as the shell title and preferred player command name.
- Keep `/charactermenu` as preferred and `/charactermenu` as a compatibility alias.
- Use the visible keybind label `Open Character Menu` while preserving the
  translation key and default key.
- Keep Collection Java/API, packet IDs, config paths, runtime filenames, and
  resource paths stable as the internal Unlockables contract.
- Consider internal renames only in a separately approved compatibility slice
  after the broader shell is stable.

## Packet And Scale Findings

`CollectionOpenPayload` decodes at most:

- 32 tabs.
- 512 entries per tab.
- 16 actions per entry.

The encoder currently writes provider list sizes without applying those same
bounds. A future provider that exceeds a decoder count limit can cause a
custom-payload decode failure. Display strings are written through bounded
string codecs, but provider output is not validated or safely projected before
encoding; oversized values can fail encode. This is the same operational risk
class previously corrected in the Admin Panel.

The current eager snapshot also asks every provider for every entry whenever
the menu opens. That is acceptable for the current seven mounts and bounded
title definitions, but it is not an acceptable profile aggregation strategy.
Profile sections must be requested lazily and return bounded summaries.

## Profile Ownership Matrix

| Candidate field | Canonical owner | Current readiness | Required path |
| --- | --- | --- | --- |
| Character identity/status | Core `CitizenService`/identity APIs | Ready | Core self/public projection |
| Realm membership | Core `CitizenRecord`/`RealmService` | Ready | Core public projection |
| Active/public title | Core `TitleService` | Ready | Core public projection |
| Unlocked titles | Core title state | Ready for self | Keep in Unlockables |
| Mount unlocks/active mount | Mounts | Ready for self | Existing Collection provider |
| Recent history | Core public-history indexes/archives | Bounded API exists | Use `ElarionPublicHistoryApi.ledger`, never raw JSONL |
| Completed quests | Quests + Core player stats | Ready for self/admin from Slice 8 onward | `QuestStateService` increments `quests_completed` when a player actor locks the first ending for an active questline scope; `QuestProfileContributor` contributes `quests/quests-completed` with `SELF` visibility |
| Portal journeys | Portals + Core player stats | Ready for self/admin from Slice 9 onward | `PortalRouteService` increments `portal_journeys` after successful authoritative travel; `PortalProfileContributor` contributes `portals/journeys` with `SELF` visibility |
| NPC reputation | NPCs | Not implemented | Current dialogue always projects Neutral/0; add NPC-owned persistence first |
| Offering score | Offerings + Core player stats | Ready for self/admin from Slice 7 onward | `OfferingService` increments `offerings_score` after successful direct player item/currency contributions; `OfferingProfileContributor` contributes `offerings/offering-score` with `SELF` visibility |
| Current civic offices | Government | Data exists | Add Government-owned bounded contributor projection |
| Civic history | Government/Core history | Partial | Use bounded structured history or Government summary API |
| Group membership | Groups | Domain-owned | Add Groups contributor after visibility rules are defined |
| Death count | Underworld + Core player stats | Ready for self/admin from Slice 6 onward | `UnderworldService` increments `underworld_lifetime_deaths`; `UnderworldProfileContributor` contributes `underworld/deaths` with `SELF` visibility |
| Soul/Underworld status | Underworld | Self-sensitive | Add self/admin contributor; do not expose publicly by default |
| Economy balance | Economy | Available but sensitive | Self/admin only, opt-in contributor |

## Target Boundary

Character Menu should be a Core-owned navigation and aggregation shell with two
initial page families:

1. **Profile**: read-only, server-filtered summary sections.
2. **Unlockables**: the existing Collection tabs, entries, previews, and
   server-authoritative actions.

Do not force Profile into `ElarionCollectionEntry`. Profile sections need
typed fields/cards, visibility, ordering, source ownership, and no implied
unlock/action semantics.

The eventual profile contracts should provide equivalents of:

- `CitizenProfileService`
- `CitizenProfileContributor`
- `CitizenProfileRequestContext`
- `CitizenProfileSnapshot`
- `CitizenProfileSection`
- `ProfileVisibility`
- bounded `ProfileField`/`ProfileCard` presentation records

Core should register contributors without depending on addon implementation
classes. Each contributor receives the authenticated viewer and subject and
returns only authorized fields. The server must filter before synchronization;
the client must never receive private data merely to hide it.

Default visibility remains conservative:

- Public: canonical identity, public Realm membership, active public title.
- Self/admin: unlocks, quest summaries, NPC relationships, Offering score,
  death/Underworld status, and economy information.
- Realm/group/official: absent until an owning contributor explicitly defines
  and tests that audience.

## Query And Performance Rules

- Open only the selected top-level page; do not aggregate every profile and
  unlockable section in one payload.
- Request profile sections lazily or as one small bounded self-profile summary.
- Cap section count, fields/cards per section, strings, and payload bytes before
  encoding, with matching encoder/decoder limits.
- Contributors must read cached state or dedicated summaries. They must not
  parse files, scan raw JSONL, enumerate all historical records, or mutate
  state during snapshot creation.
- Recent history uses `PublicHistoryConsumer.LEDGER`, which already reads
  bounded archives/monthly indexes and applies configured limits.
- Quest completion, Offering score, and lifetime death totals are now
  owner-maintained through Core player-stat keys: `quests_completed`,
  `offerings_score`, and `underworld_lifetime_deaths`.
- Optional addon absence produces an omitted section, not an error or empty
  Core-owned duplicate.

## Architecture Risks

1. **Resolved in Slice 17B: encoder/decoder count mismatch.** Collection now
   bounds provider lists to decoder limits and filters unsafe actionable IDs.
2. **High: profile privacy.** No profile visibility/authorization model exists.
3. **High: false summary semantics.** Quest endings are line-scoped and NPC
   reputation is currently placeholder data. The Underworld lifetime death
   counter is now backed by an owner-updated stat for new captures; old deaths
   before the counter existed are not backfilled.
4. **Medium: eager aggregation.** Every Collection provider is evaluated on
   each open; this must not become the profile pattern.
5. **Medium: naming churn.** Structural renames would break APIs, files,
   resources, commands, and docs for little immediate value.
6. **Medium: silent provider replacement.** Registering a duplicate Collection
   provider ID currently replaces the previous map value without an explicit
   duplicate policy.

## Recommended Implementation Sequence

1. Harden the existing Collection wire boundary.
2. Perform current Collection live screenshot QA and preserve existing unlock
   behavior as a regression baseline.
3. Introduce the Character Menu user-facing shell name and `/charactermenu` alias
   while preserving Collection internals.
4. Add Core-only self-profile presentation records and conservative visibility
   tests without addon contributors.
5. Add lazy profile section requests and bounded packet tests.
6. Add one low-risk contributor, preferably Core identity/Realm/title.
7. Add addon contributors only after each owner has a bounded summary API.
8. Rebuild the shell against `01-citizen-ledger-profile.png` and
   `02-citizen-ledger-unlockables.png`, then run live screenshot QA.

## Next Slice Proposal

### Objective

Make Collection snapshots safe to encode/decode when an addon provider returns
oversized lists or text.

### Approved Boundaries

Include:

- Matching outbound and inbound limits for tabs, entries, and actions.
- Safe handling of oversized display strings and invalid/oversized IDs.
- Selection fallback when a selected tab is omitted by bounds/validation.
- Focused service/payload tests.

Exclude:

- Character Menu rename or `/charactermenu` command.
- Profile contracts or fields.
- Collection screen redesign.
- Addon storage, unlock, action, or persistence changes.

### Expected Files

- `platform/core/src/main/java/panetina/elarion/core/network/CollectionOpenPayload.java`
- A small Core Collection snapshot/wire policy helper if needed.
- `platform/core/src/main/java/panetina/elarion/core/service/ElarionCollectionService.java`
  only if selection normalization belongs before send.
- `platform/core/src/test/java/panetina/elarion/core/network/CollectionPayloadTest.java`
- `platform/core/src/test/java/panetina/elarion/core/service/ElarionCollectionServiceTest.java`
- Affected GUI/network docs and handoff files.

### Architecture And Compatibility

- Ownership remains Core shell plus addon-owned providers/actions.
- No public API or packet identifier changes.
- No persistence, config, command, or addon dependency changes.
- The server continues to validate all actions.
- Existing valid snapshots remain byte-semantically equivalent.

### Verification

- Round-trip snapshots at every count limit.
- Oversized provider counts decode without disconnect.
- Oversized/invalid identifiers cannot become actionable client rows.
- Display strings are safely bounded.
- Selected-tab fallback remains valid.
- Run focused Core Collection service/network tests and `:platform:core:test`.

Classification: **SMALL**.

## Slice 17B Result

Completed on 2026-07-07.

- `CollectionOpenPayload` now projects a wire-safe snapshot before encoding.
- Outbound limits match decoder limits: 32 tabs, 512 entries per tab, and 16
  actions per entry.
- Blank, whitespace-mutated, unsafe-control, oversized, and duplicate IDs are
  omitted at their relevant scope so the client cannot send an ID that was
  changed by string truncation.
- Display strings continue using `ElarionPacketCodecs`, which strips unsafe
  control/format characters and truncates to the field maximum.
- Selected-tab state falls back to the first transmitted tab when the requested
  tab is invalid or beyond the outbound limit.
- Existing APIs, packet identifiers, provider dispatch, valid snapshots,
  unlock state, storage, config, and commands were not changed.

Verification passed:

- Focused `CollectionPayloadTest` and `ElarionCollectionServiceTest`.
- Full `:platform:core:test`.

The next recommended slice is a live Collection regression baseline before the
Character Menu shell changes: open the current authoritative Mounts/Pets/Titles
flow, exercise selection/actions/previews, and capture comparison screenshots.

## Slice 17C Result

Completed on 2026-07-07.

Live QA path:

- Started `runServer` and `runClientOne`.
- Joined the saved `localhost` multiplayer server as `ElarionAdmin`.
- Opened the current Collection menu with `/charactermenu`.
- Captured evidence under `build/ui-qa/slice-17c/`.

Captured baseline:

- `11-collection-mounts.png`: Mounts tab with the Airship row selected and the
  live model preview visible.
- `12-collection-mount-active.png`: Set Active action refreshed the row/detail
  state and marked Airship active.
- `13-collection-mount-scroll.png`: hidden mount-list scrolling exposed later
  rows without breaking the selected Airship detail state.
- `14-collection-pets.png`: Core-owned empty Pets tab rendered.
- `15-collection-titles.png`: Titles tab rendered selection/detail state.

Findings:

- No Elarion Collection/custom-payload encode/decode crash occurred during the
  QA flow.
- Client errors matched unrelated resource-pack model rotation issues for
  `excalibur/carved_pumpkin/*` JSON models.
- The current shell is stable enough to serve as a regression baseline but
  remains visually older than the approved Character Menu boards: it still says
  `Collection`, empty/detail panels are sparse, and row/detail styling should
  be rebuilt in a future UI slice.
- The QA action changed the test player's active mount to `airship` in
  `dev/run/world/elarion/addon-state/mounts/collection.json`. There was no
  pre-click runtime-state backup, so the change is documented rather than
  guessed back to an unknown previous value.
- The live Windows client can present a white/tiny framebuffer after startup
  until the Minecraft window is toggled with `F11` or otherwise forced to
  recreate/maximize the framebuffer. This was not shader-related in this run;
  Iris runtime state was restored after the temporary check.

Recommended next slice:

- Add the `Character Menu` user-facing shell label and `/charactermenu` command alias
  while preserving Collection internals, packet IDs, provider/action contracts,
  runtime files, and `/charactermenu` compatibility.
- Defer Profile aggregation, full shell redesign, and art-asset promotion.

## Slice 17D Result

Completed on 2026-07-07.

Implementation:

- `ElarionCollectionService` now uses `Character Menu` as the server-authored
  snapshot title while keeping the current truthful subtitle,
  `Mounts, pets, and titles.`
- `ElarionCollectionScreen` uses `Character Menu` for its client screen title.
- `/charactermenu` opens the same server-authoritative Collection snapshot as the
  existing command.
- `/charactermenu` remains as a compatibility alias.
- Command help now documents `/charactermenu` as preferred and `/charactermenu` as the
  compatibility alias.
- The keybind label now reads `Open Character Menu`.

Compatibility:

- Collection Java/API names, packet identifiers, packet models, provider/action
  contracts, resource paths, config paths, runtime filenames, storage, and
  action semantics were not changed.
- No Profile fields, profile contributors, shell redesign, art assets, or addon
  state changes were introduced.

Verification:

- Focused Core tests passed for Collection service, Collection payloads, and
  Collection screen layout.

Recommended next slice:

- Define the Core profile aggregation/presentation boundary with conservative
  server-side visibility, starting with Core-only records and no addon
  contributors.
- Defer visual shell redesign and asset promotion until the profile contract is
  stable.

## Slice 17E Result

Completed on 2026-07-07.

Implementation:

- Added Core profile presentation records under
  `panetina.elarion.core.model.profile`:
  `CitizenProfileRequestContext`, `CitizenProfileSnapshot`,
  `CitizenProfileSection`, `CitizenProfileField`, `CitizenProfileCard`,
  `CitizenProfileContributor`, and `ProfileVisibility`.
- Added `CitizenProfileService`, exposed through `ElarionApi.profiles()` and
  `ElarionApi.system().profiles()`.
- Built the first Core-only profile projection from canonical Core state:
  identity, Realm, and active title.
- Added conservative server-side visibility filtering:
  `PUBLIC`, `SELF`, and `ADMIN`.
- Added hard caps for aggregation output: 16 sections, 24 fields per section,
  and 8 cards per section.
- Added explicit contributor registration with duplicate contributor IDs
  rejected.
- `snapshot(context)` resolves exactly one target citizen by UUID through
  `CitizenService.find`; it does not enumerate all citizens and does not read
  addon storage.

Compatibility:

- No Collection packet, UI, command, storage, config, or unlock behavior
  changed.
- No profile network packets, Profile tab UI, addon contributor, persistence
  format, Chronicle/history query, or art asset was added.
- Quest completion, Offering score, NPC reputation, deaths, economy, groups,
  and other addon profile data remain unavailable until each owning addon has
  a bounded summary API and explicit visibility rules.

Verification:

- Focused `CitizenProfileServiceTest` passed.

Recommended next slice:

- Add a lazy/bounded profile transport proposal for Character Menu Profile
  requests and snapshots, or first add bounded Core profile packet records with
  tests. Do not render the Profile tab or register addon contributors until
  the transport contract is bounded.

## Slice 17F Result

Completed on 2026-07-07.

Implementation:

- Added unregistered profile transport records:
  `CitizenProfileRequestPayload` and `CitizenProfileSnapshotPayload`.
- `CitizenProfileRequestPayload` carries one target citizen UUID and an
  optional section id for future lazy section requests.
- `CitizenProfileSnapshotPayload` serializes one bounded
  `CitizenProfileSnapshot`.
- Snapshot encoding applies the same output caps as `CitizenProfileService`:
  16 sections, 24 fields per section, and 8 cards per section.
- Snapshot strings are clamped at explicit codec limits for section/field/card
  ids, titles, labels, values, bodies, and source system ids.

Compatibility:

- Payload types are not registered with Fabric yet and no receivers were added.
- No Profile tab UI, client state, server dispatch, addon contributor,
  Collection behavior, persistence, config, command, or art asset changed.
- The future server receiver must derive the viewer from the connection and
  build filtered snapshots through `CitizenProfileService`; the client must
  remain read-only.

Verification:

- Focused `CitizenProfilePayloadTest` and `CitizenProfileServiceTest` passed.

Superseded next slice:

- Register the profile payload types only, or pair type registration with a
  tiny server-authoritative receiver/client cache proposal. Keep UI rendering,
  addon contributors, and raw storage scans excluded. Completed by Slice 17G.

## Slice 17G Result

Completed on 2026-07-07.

Implementation:

- Registered `CitizenProfileRequestPayload` as C2S and
  `CitizenProfileSnapshotPayload` as S2C.
- Added server-authoritative profile request handling in Core. The receiver
  derives the viewer from the connection, resolves zero target UUID to the
  connected player, builds through `CitizenProfileService`, optionally narrows
  the response to one requested visible section, and sends a bounded snapshot.
- Added `CitizenProfileClientState` and a client receiver that stores the
  latest server-authored snapshot, clearing it on join/disconnect.

Compatibility:

- Profile remains read-only on the client.
- No Profile tab UI, addon contributor, raw storage scan, persistence, config,
  command, Collection action behavior, or art asset changed.

Verification:

- Focused `CitizenProfileServiceTest`, `CitizenProfilePayloadTest`, and
  `CitizenProfileClientStateTest` passed.

Recommended next slice:

- Add the Core-only Character Menu Profile tab surface using the live profile
  request/cache path. Render identity, Realm, and active-title sections only;
  keep addon profile contributors and broad shell redesign deferred.

## Slice 17H Result

Completed on 2026-07-07.

Implementation:

- Added a fixed Core `Profile` tab to the Character Menu snapshot.
- Preserved `/charactermenu` default selection on `Mounts` so existing unlockable
  behavior remains stable when the menu opens.
- Updated the Character Menu subtitle to include Profile.
- Added a read-only Profile tab surface to `ElarionCollectionScreen`.
- The Profile tab sends `CitizenProfileRequestPayload`, reads the latest
  `CitizenProfileClientState` snapshot, and renders visible Core identity,
  Realm, and active-title sections.
- The first Profile tab rendering used left-side section rows and a right
  detail panel. Live review rejected that as too menu-like.
- The corrected Profile tab renders one composed civic sheet with a
  name/identity strip plus Realm, office/title, and record panels.

Compatibility:

- No addon profile contributors were registered.
- Completed quests, Offering scores, NPC reputation, and lifetime death totals
  remain deferred until owner-maintained bounded summary APIs exist.
- No persistence, config, command, packet schema, art asset, or Collection
  action behavior changed.

Verification:

- Focused `ElarionCollectionServiceTest`,
  `ElarionCollectionScreenLayoutTest`, and `CitizenProfileClientStateTest`
  passed.

Recommended next slice:

- Phase 4, Slice 17I completed the corrected Profile sheet live QA.

## Slice 17I Result

Completed on 2026-07-07.

Live QA:

- Restarted the dev client, joined the saved localhost multiplayer server, and
  opened `/charactermenu`.
- Captured the current Mounts default at
  `build/ui-qa/slice-17j-profile-sheet/10-ledger-default.png`.
- Captured the corrected Profile sheet at
  `build/ui-qa/slice-17j-profile-sheet/11-ledger-profile-sheet.png`.
- Verified the live Profile request/snapshot path did not cause a custom
  payload crash.

Design notes:

- The corrected Profile tab is now a single civic record sheet with identity,
  Realm, office/title, and record panels.
- The broader Character Menu shell and unlockable tabs still need the approved
  Option A art/component migration.

Recommended next slice:

- Propose Phase 4, Slice 17J for a bounded Character Menu shell/art pass using
  the approved Option A references and the live Profile screenshot.

## Slice 17J Result

Completed on 2026-07-07.

Implementation:

- Expanded the Character Menu to a wider bounded civic shell while preserving
  the existing four-tab Collection contract.
- Rebuilt Profile as a portrait-led dossier based on the approved Option A
  reference: identity/Realm/title banner plus Citizen Standing, Progression,
  Collection, Affiliations, Lifetime Record, and Chronicle panels.
- The portrait uses the target player's live player-list skin with a neutral
  fallback.
- Added cheap Core-owned citizenship, civic-standing, granted-ability count,
  and unlocked-title count fields. Mount ownership is derived from the already
  bounded server-authored Collection snapshot.
- Added explicit future contributor slots with stable source/field ids; missing
  addon summaries remain visibly unrecorded and no fake domain state is added.

Verification:

- Full `:platform:core:test` passed.
- Live multiplayer `/charactermenu` QA passed without a custom-payload crash.
- Final capture:
  `build/ui-qa/slice-17j-profile-redesign/18-profile-final.png`.

Recommended next slice:

- Migrate Mounts/Titles/Pets unlockable presentation to the same Option A
  Character Menu shell while preserving provider actions, previews, hidden
  scrolling, and server authority. Keep addon profile summary implementation
  in separate owner-specific slices.

## Slice 17K Result

Completed on 2026-07-07.

Implementation:

- Migrated Mounts, Pets, and Titles into the Option A Character Menu visual
  system without changing Collection providers, packets, or actions.
- Added semantic tab icons, unlocked/total header summaries, compact 44-pixel
  rows, explicit active/owned/locked states, a large showcase panel, bounded
  record text, and designed empty states.
- Preserved six-row hidden scrolling and expanded live entity preview space.
- Fixed a live-discovered 16x16 texture scaling defect that tiled row, tab,
  empty-state, and fallback-preview icons when drawn larger than source size.

Verification:

- Full `:platform:core:test` passed.
- Live Mounts, Pets, and Titles screenshots passed under
  `build/ui-qa/slice-17k-unlockables/`.
- A clean-runtime title action changed Citizen to active and refreshed the
  server-authored snapshot; Monarch was then restored as active.
- The earlier action attempt during an in-place Core recompilation hit a
  transient Fabric classloader `ZipFile invalid LOC header`; restarting the
  server/client from the completed build removed it and confirmed this was QA
  process ordering, not a product defect.

Recommended next slice:

- Return to the project-wide UI revamp order and choose the next bounded screen
  family from the approved Option A references. Keep generated runtime asset
  promotion and owner-specific profile contributors as separate slices.

## Slice 17L Result

Completed on 2026-07-07.

Implementation:

- Added provider-owned Collection rank/accent metadata to the entry model and
  open payload. Core bounds and renders it as row/detail accents and compact
  rank badges without taking ownership of addon unlock semantics.
- Added config-backed Core title colors with `#RRGGBB` validation, read-only
  descriptors, defaults for built-in titles, and migration for known titles
  that lack a color. Custom admin colors are preserved.
- Titles now use configured colors in identity rendering and Character Menu
  title rows/previews. The unlocked title preview renders selected title,
  username, and the live player model.
- Mounts now provide Common/Uncommon/Legendary rank metadata from
  `ElarionMountType`: Realm baseline mounts are Common, future reward mounts
  are Uncommon, and Sci-Fi Bike is Legendary for the future full-advancement
  route.
- Core progression contributes exact completed-advancement count to the
  profile; Government contributes active office display names when the citizen
  holds an office.

Verification:

- Full `:platform:core:test` passed.
- Full `:addons:mounts:test` passed.
- Focused config/packet/rank tests passed.
- Focused Government contributor test was blocked by existing Groups/Offerings
  compile failures pulled in through Government dependencies.

Deferred:

- Live screenshot QA for title colors and rank badges.
- Player-name double-click profile links in Chronicle/history/menus.
- Generic reward hooks for mount/pet unlocks.
- NPC and Chronicle profile summaries.

## Slice 17M Result

Completed on 2026-07-07.

Implementation:

- Added Core `ElarionCollectionRank` as the shared project-wide rank palette.
  Common, Uncommon, Rare, Epic, Legendary, Sovereign, Heir, Council, Synod,
  Officer, and Trusted colors should now be reused by Titles, Mounts, Pets,
  rewards, and future Collection/Profile providers.
- Moved Title and Mount rank colors to the shared rank palette.
- Changed Profile advancement display to count visible completed Minecraft
  advancements only; hidden/internal advancement records are excluded.
- Polished the Profile header so it does not repeat the same title twice when
  active title and active civic office resolve to the same label.

Verification:

- Passed focused Core rank and Collection packet tests.
- Passed focused Mount rank tests.
- Passed full `:platform:core:test :addons:mounts:test`.
- Live pre-patch QA evidence was captured at
  `build/ui-qa/slice-17m-ledger-rank-qa/08-ledger-open.png`,
  `09-ledger-profile.png`, and `10-ledger-titles.png`. Final post-patch
  screenshot was blocked by a Minecraft client `Invalid Session` reconnect
  dialog; the QA server/client processes were stopped afterward.

Deferred:

- Rerun post-patch live Ledger screenshot QA after a clean client session.
- Player-name double-click profile links in Chronicle/history/menus.
- Generic reward hooks for mount/pet unlocks.
- Quest, Offering, NPC, Underworld, Portal, and Chronicle profile summaries.

## Phase 7 Slice 5 Result

Completed on 2026-07-10.

Implementation:

- Added Core `CitizenProfileSummaryFields` as the canonical source/field-id
  contract for the Character Menu dossier summary slots.
- Moved Core progression and Government profile contributors to the shared
  constants for `progression`, `government`, `advancements-completed`,
  `active-office`, and `office-history`.
- Moved Character Menu Profile lookups to the same constants for Offering
  score, completed quests, NPC reputation, groups, Government office history,
  Underworld deaths, portal journeys, progression milestones, and recent
  history.
- Added focused tests that assert the reserved source/field IDs exist and stay
  normalized.

Verification:

- Passed
  `.\gradlew.bat :platform:core:test --tests panetina.elarion.core.model.profile.CitizenProfileSummaryFieldsTest --tests panetina.elarion.core.service.CitizenProfileServiceTest --tests panetina.elarion.core.network.CitizenProfilePayloadTest --tests panetina.elarion.core.client.ElarionCollectionScreenLayoutTest :addons:government:test --tests panetina.elarion.addons.government.service.GovernmentProfileContributorTest :platform:core:compileJava :addons:government:compileJava`.

Deferred:

- No new owner summaries were added. Completed quests, Offering score, NPC
  reputation, lifetime deaths, portal journeys, milestones, and Chronicle
  recent-summary still needed owner-maintained bounded summary APIs before
  they could render real values.
- No live screenshot QA was run in this Medium slice.

## Phase 7 Slice 6 Result

Completed on 2026-07-10.

Implementation:

- Added `UnderworldProfileContributor`, which contributes the existing
  `underworld/deaths` Character Menu summary slot with `SELF` visibility.
- Registered the contributor from `ElarionUnderworldAddon` through
  `ElarionApi.system().profiles()`.
- Added the bounded Core player-stat key
  `UnderworldService.LIFETIME_DEATHS_STAT` / `underworld_lifetime_deaths`.
- Incremented that stat at the authoritative living-world death capture path
  and repeat Underworld death capture path. Admin send/return commands do not
  increment the counter.

Compatibility:

- No Underworld state schema, corpse/session/vault persistence, packets,
  commands, config, or UI geometry changed.
- No historical backfill is attempted; existing players start at the stored
  stat value, normally `0`, until new captured deaths occur.
- The death count is self/admin profile data, not public profile data.

Verification:

- Passed
  `.\gradlew.bat :addons:underworld:test --tests panetina.elarion.addons.underworld.service.UnderworldProfileContributorTest --tests panetina.elarion.addons.underworld.client.GraveRecoveryScreenLayoutTest :platform:core:test --tests panetina.elarion.core.model.profile.CitizenProfileSummaryFieldsTest --tests panetina.elarion.core.service.CitizenProfileServiceTest :addons:underworld:compileJava`.

Deferred:

- Completed quests, Offering score, NPC reputation, portal journeys,
  milestones, and Chronicle recent-summary remained pending owner-maintained
  summaries after this slice.
- No live screenshot QA was run in this Medium slice.

## Phase 7 Slice 7 Result

Completed on 2026-07-10.

Implementation:

- Added `OfferingProfileContributor`, which contributes the existing
  `offerings/offering-score` Character Menu summary slot with `SELF`
  visibility.
- Registered the contributor from `ElarionOfferingsAddon` through
  `ElarionApi.system().profiles()`.
- Added the bounded Core player-stat key
  `OfferingService.OFFERING_SCORE_STAT` / `offerings_score`.
- Incremented that stat only after a direct item or currency player
  contribution has been validated, persisted, and accepted by `OfferingService`.

Compatibility:

- No Offering state schema, project definition format, Shrine UI geometry,
  packets, commands, config, or milestone behavior changed.
- Event/admin progress injections do not count as personal Offering score.
- No historical backfill is attempted; existing players start at the stored
  stat value, normally `0`, until new direct contributions occur.
- The Offering score is self/admin profile data, not public profile data.

Verification:

- Passed
  `.\gradlew.bat :addons:offerings:test --tests panetina.elarion.addons.offerings.service.OfferingProfileContributorTest --tests panetina.elarion.addons.offerings.ElarionOfferingsAddonTest --tests panetina.elarion.addons.offerings.model.OfferingInstanceTest :platform:core:test --tests panetina.elarion.core.model.profile.CitizenProfileSummaryFieldsTest --tests panetina.elarion.core.service.CitizenProfileServiceTest :addons:offerings:compileJava`.

Deferred:

- Completed quests, NPC reputation, portal journeys, milestones, and Chronicle
  recent-summary remained pending owner-maintained summaries after this slice.
- No live screenshot QA was run in this Medium slice.

## Phase 7 Slice 8 Result

Completed on 2026-07-10.

Implementation:

- Added `QuestProfileContributor`, which contributes the existing
  `quests/quests-completed` Character Menu summary slot with `SELF`
  visibility.
- Registered the contributor from `ElarionQuestsAddon` through
  `ElarionApi.system().profiles()`.
- Added the bounded Core player-stat key
  `QuestStateService.COMPLETED_QUESTS_STAT` / `quests_completed`.
- Incremented that stat only when a player actor locks an ending on a
  questline scope that did not already have one.

Compatibility:

- No Quest state schema, quest package definition format, commands, config,
  packets, scheduled-consequence behavior, or notification behavior changed.
- Existing quest endings are not backfilled.
- Repeated ending locks on the same active scope do not add another count.
- Shared Realm/global/world quest endings without a player actor do not count
  as personal completed quests.
- The completed quest count is self/admin profile data, not public profile
  data.

Verification:

- Passed
  `.\gradlew.bat :addons:quests:test --tests panetina.elarion.addons.quests.service.QuestProfileContributorTest --tests panetina.elarion.addons.quests.storage.QuestStorageTest --tests panetina.elarion.addons.quests.config.QuestConfigDescriptorsTest :platform:core:test --tests panetina.elarion.core.model.profile.CitizenProfileSummaryFieldsTest --tests panetina.elarion.core.service.CitizenProfileServiceTest :addons:quests:compileJava`.

Deferred:

- NPC reputation, portal journeys, milestones, and Chronicle recent-summary
  remained pending owner-maintained summaries after this slice.
- No live screenshot QA was run in this Medium slice.

## Phase 7 Slice 9 Result

Completed on 2026-07-10.

Implementation:

- Added `PortalProfileContributor`, which contributes the existing
  `portals/journeys` Character Menu summary slot with `SELF` visibility.
- Registered the contributor from `ElarionPortalsAddon` through
  `ElarionApi.system().profiles()`.
- Added the bounded Core player-stat key
  `PortalRouteService.PORTAL_JOURNEYS_STAT` / `portal_journeys`.
- Incremented that stat after successful server-authoritative portal travel,
  after payment/ticket/return handling and travel event recording.

Compatibility:

- No Portal state schema, route definition format, tickets, payments,
  entitlements, commands, config, packets, or UI geometry changed.
- Existing travel history is not backfilled.
- The portal journey count is self/admin profile data, not public profile data.

Verification:

- Passed
  `.\gradlew.bat :addons:portals:test --tests panetina.elarion.addons.portals.service.PortalProfileContributorTest --tests panetina.elarion.addons.portals.storage.PortalStorageTest --tests panetina.elarion.addons.portals.service.PortalFreePassagePolicyTest --tests panetina.elarion.addons.portals.network.PortalTravelPromptPayloadTest :platform:core:test --tests panetina.elarion.core.model.profile.CitizenProfileSummaryFieldsTest --tests panetina.elarion.core.service.CitizenProfileServiceTest :addons:portals:compileJava`.

Deferred:

- NPC reputation and Chronicle recent-summary remain pending because they need
  NPC-owned relationship persistence and Phase 8 Chronicle summary/index work.
- No live screenshot QA was run in this Medium slice.

## Exact Files Inspected

- `RULES.md`
- `AGENTS.md`
- `CODEX.md`
- `docs/architecture/PROJECT_STRUCTURE.md`
- `docs/architecture/DEPENDENCY_GRAPH.md`
- `docs/systems/GUI.md`
- `docs/reports/PROJECT_REVAMP_AUDIT.md`
- Core Collection service/models/payloads/screen/client initializer/commands/tests
- Core citizen, title, identity, Realm, and public-history contracts
- Mount Collection provider and storage
- Quest state/API records
- Offering service/API/instance contribution records
- NPC API/dialogue projection
- Government API/state ownership
- Underworld Soul/session service contracts
