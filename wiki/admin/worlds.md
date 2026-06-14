# Worlds

Admin guide for managed worlds, borders, and world setup checks.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

`Implemented`, `Admin-only`

Worlds owns managed world definitions, borders, spawn protection, world rules, and bounded block-abundance work.

Definitions:

```text
config/elarion/addons/worlds/worlds.yml
```

Runtime state:

```text
world/elarion/addon-state/worlds/
```

## Commands

```text
/e world ...
```

Use command autocomplete and `/help e world` in-game for the exact active subcommands.

## Verification

- Confirm managed worlds load on server startup.
- Confirm borders and gamerules match config.
- Confirm Realm worlds, lobby, and Worldheart-ready worlds exist.
- Verify portal destinations use configured loaded worlds.
- Check `/e perf worlds` for sampled world diagnostics.

## Source-Backed Notes

- Addon docs: [../../docs/addons/worlds.md](../../docs/addons/worlds.md)
- Commands: [../../addons/worlds/src/main/java/panetina/elarion/addons/worlds/command/WorldCommands.java](../../addons/worlds/src/main/java/panetina/elarion/addons/worlds/command/WorldCommands.java)
