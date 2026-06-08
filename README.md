# Elarion

Elarion is a Fabric 1.21.1 platform mod organized as one canonical Core plus optional addons.

## Modules

- `platform/core`: communities, citizens, titles, abilities, identity, `/cc`, rewards, storage, and addon APIs.
- `addons/*`: feature modules that consume Core state and never duplicate canonical citizen identity.

## Commands

- `/cc <message>`: community chat; available to regular players.
- `/e ...`: administration root; permission level 4 is required.

Initial administration commands include:

- `/e community add|remove|list`
- `/e citizen info`
- `/e citizen nickname set|clear`
- `/e title set|clear|list`
- `/e ability check|grant|revoke`
- `/e reward run`
- `/e reload`

## Data

Editable YAML is generated under `config/elarion/`. Mutable citizen records are stored under
the active world save in `elarion/citizens/`.

## Addon API

Use `ElarionApi.get()` to access Core services. Addons can register abilities, reward action
handlers, progression listeners, citizen listeners, and OP-only `/e` subcommands.

Addon bootstrap code must implement `ElarionAddon` and register under the custom
`elarion:addon` entrypoint in `fabric.mod.json`. Core invokes these entrypoints only after
`ElarionApi` is ready. Addons must not register their Core-dependent initializer under
Fabric's ordinary `main` entrypoint.

Use `.\gradlew.bat runClient` or `.\gradlew.bat runServer` from the repository root. These
tasks use the `dev` module and load Core with every addon in one shared development runtime.
