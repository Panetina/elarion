# PLAN

Approved roadmap. `TODO.md` is the only current action list. Every milestone
ships as focused, documented, tested, pushed commits.

## P0 — Stable Foundation and Release

| Milestone | Outcome | Exit gate |
| --- | --- | --- |
| P0.0 | Recover and integrate WIP by domain. | Backups, focused commits, build, GameTests, dedicated smoke test. |
| P0.1 | Make the Modpack release-safe. | Recoverable World Reset, Backpack/Trinkets, Guild/Chat, Government/NPC bridge and local QA. |
| P0.2 | Make Platform staging usable. | Launcher-only website identity, simple whitelist, wiki/media and staging QA. |
| P0.3 | Promote the website production surface. | Explicit owner approval, backup/rollback rehearsal, production verification. |
| P0.4 | Ship stable pack and Launcher 1.0. | Signed pack plus clean Windows install/repair/launch/connect; Angling stays disabled. |

### P0 contracts

- Modpack owns Minecraft/Fabric compatibility and the canonical release
  manifest. Platform owns launcher versioning, installers and signed release
  pointers. The website reads release metadata and never hardcodes pack copies
  or Minecraft identifiers.
- Website identity uses a hidden Mojang UUID as the canonical key and a
  verified Minecraft username as immutable visible identity until Fabric
  projects the chosen character name. The launcher proves ownership through a
  short-lived Mojang `join/hasJoined` challenge; access tokens never reach the
  website.
- Production deploys, live-server restarts and public releases each require an
  explicit owner confirmation. Secrets, runtime worlds, caches and `Private`
  never enter Git.

## P1 — Social Experience and Chronicle Policy

- Complete Guild ranks, hierarchy-safe permissions, progression, donations,
  announcements, tags/icons, owner transfer and secrecy lifecycle.
- Core owns extensible player context actions; Guilds contributes only its
  validated invite action.
- Add Chronicle allowlist-first destinations (`WEBSITE`, `LIBRARY_BOOK`,
  `NEWSPAPER`, `ADMIN_ONLY`), bounded dedupe/rate policy and filters by
  category, family, Realm, period and destination.
- Add the shared player-link/profile-open contract and apply Option A assets
  only with a consuming screen.

## P2 — Gameplay and Institutions

- Deliver Angling vertically and open its public gate only after dedicated and
  client QA.
- Build Ashen lifecycle with 90-day private recovery retention and audited
  exceptional restoration.
- Implement the 2 Supporter / 1 normal admission scheduler, signed monthly
  entitlement, online-only Jail sentences, physical Marketplace stalls and
  admin-placed public Chronicle Libraries with weekly virtual volumes.

## P3 — Atlas and Polish

- Build Atlas in order: Core contracts, bounded persisted regions/discovery,
  masked viewport UI, NPC/HUD markers, heraldry, then website read model.
- Add the Elarion title screen/Character Menu hub, stable Mount/Pet hooks,
  full text-input caret selection and dual-client QA on a capable host.

## Non-Negotiable Engineering Gates

- Core retains canonical shared truth; addons use APIs, events and bounded
  projections rather than another owner's storage.
- Persistence changes include round-trip, restart, rollback and replay tests.
- Packets are typed, bounded and revalidated server-side.
- Player-facing history/search never scans raw JSONL, world regions or all
  player files; it uses dedicated indexes or bounded summaries.
- Full exports are release/cross-module operations. Feature work exports only
  changed module artifacts after hash verification.
