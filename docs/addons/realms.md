# Realms Addon Contract

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

## Owns

- Realm protection hooks
- Realm visitor access policy
- shared-world PvP and explosion block protection
- ally, neutral, embargoed, hostile, owner, and Diplomat interaction rules
- rate-limited protection denial feedback

Core still owns Realm definitions, membership, relationships, abilities,
history, identity, and citizen state.

## Config

```text
config/elarion/addons/realms/protection.yml
```

## Runtime State

No addon-owned runtime state yet.

Runtime citizen, Realm, relationship, and history truth remains in Core.

## Commands

No standalone Realms addon commands yet.

Core owns `/e realm ...` commands.

## Hooks

- block break protection
- block use/place protection
- entity attack protection
- explosion block-damage protection
- Diplomat death history event emission

## Performance Notes

Realm protection runs on gameplay events only. Do not add global scans.

Feedback is rate-limited per player. Keep future denial messages and audit
events bounded.

Realm ownership lookup should continue to use Core's cached world-owner lookup.

## Rules

Do not duplicate Realm membership or relationship state in this addon. Ask Core.

Do not make OP bypass the default behavior. It hides protection bugs during
testing.

Do not make Diplomat bypass combat or protection rules. Diplomat access is a
portal/travel ability, not general immunity.
