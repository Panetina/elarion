# Titles Addon Contract

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

## Owns

- client-side title and leader crown rendering above players
- visual presentation hooks for Core title/identity state

Core owns title definitions, title ownership, active title, leader state,
abilities, title effects, title progress, unique title claims, and title admin
commands.

## Config

No Titles addon config yet.

Core owns title definitions in:

```text
config/elarion/core/titles.yml
```

## Runtime State

No addon-owned runtime state.

Title ownership, active title, title progress, and unique claims live in Core
runtime state.

## Commands

No standalone Titles addon commands.

Core owns title commands under `/e title ...`.

## Performance Notes

Rendering must consume already-synced identity data. Do not query server state,
parse config, or compute title eligibility from rendering code.

Keep visual-only behavior client-side. Title abilities and effects must be
checked server-side through Core.

## Rules

Do not make the leader crown a gameplay title. It is presentation for Core
leader state.

Do not duplicate title unlock logic in renderer code.
