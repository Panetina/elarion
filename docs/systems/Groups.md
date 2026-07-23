# Groups System

Purpose: reusable public player organizations with one public group membership
per player in V1.

Status: Implemented foundation.

## Main Classes

- `addons/groups/src/main/java/panetina/elarion/addons/groups/ElarionGroupsAddon.java`
- `addons/groups/src/main/java/panetina/elarion/addons/groups/service/GroupService.java`
- `addons/groups/src/main/java/panetina/elarion/addons/groups/command/GroupCommands.java`
- `addons/groups/src/main/java/panetina/elarion/addons/groups/storage/GroupStorage.java`
- `addons/groups/src/main/java/panetina/elarion/addons/groups/api/ElarionGroupsApi.java`

## Commands

Player:

- `/group create <id> <tag> <display-name...>`
- `/group invite <player>`
- `/group accept <group>`
- `/group kick <player>`
- `/group leave`
- `/group transfer <player>`
- `/group info [group]`
- `/gc <message>`

Admin:

- `/e groups reload`
- `/e groups list`
- `/e groups inspect <group>`
- `/e groups delete <group>`
- `/e groups transfer <group> <player>`

## Storage / Persistence

Config:

- `config/elarion/addons/groups/groups.yml`

Runtime:

- `world/elarion/addon-state/groups/groups.json`

## Dependencies

- Core: citizens, identity, history, command hooks.
- Economy: creation fee sink.

## Related Systems

- Identity: public tag rendering uses a Core chat prefix provider.
- Economy: group creation fee payment.
- Government: no active founding office depends on Groups after the two-form
  Government simplification.

## Extension Points

- `ElarionGroupsApi`
- future group types
- future group visibility
- future group permissions and metadata

## Risks

- Group tags are public identity. Validation must stay config-driven and
  collision-safe.
- Do not add secret/criminal/revolutionary behavior into V1 public group
  commands. Add group types and visibility first.

## Do Not Duplicate This System By Creating

- another group/guild/city membership manager
- Government-owned group membership storage
- an NPC-owned group identity store
- a Ledger-owned group roster
