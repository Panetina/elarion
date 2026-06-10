# Worlds Addon Contract

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

## Owns

- managed world definitions
- lobby/world destination resolution
- runtime world opening, unloading, removal, and teleporting
- per-world borders
- world spawn platform creation
- block abundance/scarcity replacement rules
- mob abundance rules
- processed chunk tracking for world block rules
- `/e world ...` admin command family

Core owns Realm membership, Realm spawn decisions, history storage, task queues,
and canonical citizen state.

## Config

```text
config/elarion/addons/worlds/worlds.yml
```

Command-created worlds are saved into the same `worlds.yml` file. Do not create
a separate created-worlds file.

## Runtime State

```text
world/elarion/addon-state/worlds/processed-chunks.json
```

This file stores which chunks already had block abundance rules applied. It is
runtime state, not editable config.

## Commands

```text
/e world list
/e world info <world>
/e world tp <destination>
/e world create <name> <type> [seed]
/e world remove <world>
/e world reload
```

All Worlds commands are OP level 4 through Core admin command registration.

## Performance Notes

World mutation must stay on the server thread. Heavy block-abundance work is
scheduled through Core's bounded server-thread task queue and records queue-full
metrics.

Do not process chunks every tick. Apply abundance rules once per chunk load and
save processed chunk state with bounded dirty saves.

Fantasy runtime worlds are opened with `tickWhenEmpty(false)` to avoid idle
world cost.

## Rules

Do not reintroduce duplicate world config files. `worlds.yml` is the single
editable definition file.

Do not use the vanilla overworld as an Elarion gameplay destination unless a
future design explicitly makes it one.

Do not bypass per-world border state through vanilla-global assumptions.
