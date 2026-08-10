# Guilds System

Purpose: reusable public player organizations with one public guild membership
per player in V1.

Status: Implemented foundation, Registrar, and bounded management UI.

## Main Classes

- `addons/guilds/src/main/java/panetina/elarion/addons/guilds/ElarionGuildsAddon.java`
- `addons/guilds/src/main/java/panetina/elarion/addons/guilds/service/GuildService.java`
- `addons/guilds/src/main/java/panetina/elarion/addons/guilds/command/GuildCommands.java`
- `addons/guilds/src/main/java/panetina/elarion/addons/guilds/storage/GuildStorage.java`
- `addons/guilds/src/main/java/panetina/elarion/addons/guilds/api/ElarionGuildsApi.java`

## Commands

Player: `G` opens the server-authoritative Guild surface. Non-members receive a
read-only empty state; creation remains a Registrar NPC action. Guild chat uses
the Core chat selector, not a player command.

Admin:

- `/e guild reload`
- `/e guild list`
- `/e guild inspect <guild>`
- `/e guild delete <guild>`
- `/e guild transfer <guild> <player>`

## Storage / Persistence

Config:

- `config/elarion/addons/guilds/guilds.yml`

Runtime:

- `world/elarion/addon-state/guilds/guilds.json`

## Dependencies

- Core: citizens, identity, history, command hooks.
- Economy: creation fee sink.

## Player UI Contract

- The NPC action opens a Guild-owned Registrar screen for non-members and the
  management screen for members.
- Creation terms and physical-inventory affordability are server-authored. The player never
  enters a storage ID; Guilds generates a collision-safe internal ID.
- The management surface contains Overview, Members, News, Invites, Roles, and
  Emblem tabs. Viewer permissions and at most 32 eligible online invite targets
  are projected by the server; all mutations are revalidated by `GuildService`.
- An online invitation opens a small central Accept/Deny prompt. It is only a
  client presentation of a server-created invite: the decision payload contains
  the guild ID and the server revalidates membership, inviter, expiry and
  permissions. The persistent notification action remains the fallback.
- Hold Sneak and right-click a nearby player for Core's contextual menu. Guilds
  contributes `Invite to Guild` only when the actor has its canonical `INVITE`
  permission and the target has no membership; the Guild service validates the
  same invariants again before creating the invite.
- Successful membership exit closes the stale screen. Snapshot refreshes update
  the current screen in place so the selected tab is preserved.

## Related Systems

- Identity: public tag rendering uses a Core chat prefix provider.
- Economy: guild creation fee payment.
- Government: no active founding office depends on Guilds after the two-form
  Government simplification.

## Extension Points

- `ElarionGuildsApi`
- secret-Guild presentation and Registrar creation flow
- future guild visibility
- additional Guild UI actions only after their server authority contract exists

## Risks

- Guild tags are public identity. Validation must stay config-driven and
  collision-safe.
- Do not add secret/criminal/revolutionary behavior into V1 public guild
  commands. Add guild types and visibility first.

## Do Not Duplicate This System By Creating

- another guild/guild/city membership manager
- Government-owned guild membership storage
- an NPC-owned guild identity store
- a Ledger-owned guild roster
