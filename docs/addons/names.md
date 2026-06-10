# Names Addon Contract

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

## Owns

- client/server presentation hooks for Core nicknames
- nickname-aware command suggestions
- player list nickname display hooks
- player entity identity hooks needed for nickname presentation

Core owns nickname validation, canonical nickname state, identity derivation,
and nickname admin commands.

## Config

No Names addon config yet.

Core owns nickname rules in:

```text
config/elarion/core/identity.yml
```

## Runtime State

No addon-owned runtime state.

Nicknames live in Core citizen state.

## Commands

No standalone Names addon commands.

Core owns nickname commands under `/e citizen ...`.

## Performance Notes

Presentation hooks must read cached/derived identity data. Do not add server
lookups, config parsing, or world scans from client rendering or suggestion
hooks.

## Rules

Do not store duplicate nickname truth in the addon.

Do not make suggestion rendering mutate citizen state.

Keep multi-word nickname behavior compatible with original username command
execution.
