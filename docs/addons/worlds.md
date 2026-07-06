# Worlds Addon

Last reviewed: 2026-07-05

Status: Implemented foundation.

## Purpose

`addons/worlds` owns managed world configuration, borders, spawn protection,
world rules, abundance rules, and processed-chunk state.

## Main Source

- `addons/worlds/src/main/java/panetina/elarion/addons/worlds/`
- `addons/worlds/src/main/java/panetina/elarion/addons/worlds/command/`
- `addons/worlds/src/main/java/panetina/elarion/addons/worlds/config/`

## Ownership

Worlds owns world-specific behavior and config/runtime processing. Core remains
the owner of Realm identity and canonical citizen/Realm truth.

## Notes

- Keep block/world mutation bounded and server-thread safe.
- Preserve config-driven rules.
- Do not use global scans where chunk indexes or queued work are sufficient.

## Config Discovery

World definitions live in:

```text
config/elarion/addons/worlds/worlds.yml
```

Worlds registers a read-only config descriptor domain named `worlds` through
`ElarionApi.system().configs()`. The descriptors read the loaded
`WorldsConfigManager` snapshot for schema, lobby routing, current world
keys/counts, and per-world identity/type/rule summaries. Admin Panel discovery
does not parse config files. Config editing, world behavior changes, reload
semantic changes, packet changes, and persistence changes remain future
approved slices.
