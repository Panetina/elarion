# Elarion

Elarion is a Fabric 1.21.1 platform mod organized as one canonical Core plus optional addons.

Author: Panyel

Team: Panetina Team

The durable product goals, architectural rules, legacy-mod findings, current
status, and roadmap are maintained in [PLAN.md](PLAN.md). Read it before making
architectural or gameplay changes.

## Modules

- `platform/core`: realms, citizens, titles, abilities, identity, `/rc`, rewards, storage, and addon APIs.
- `addons/*`: feature modules that consume Core state and never duplicate canonical citizen identity.

## Commands

- `/rc <message>`: Realm Chat; available to regular players.
- `/w <player> <message>`: whisper to a member of a `GLOBAL` Realm.
- `/r <message>`: reply to the last player who whispered to you.
- `/help [command]`: show commands currently available to you with explanations.
- `/e ...`: administration root; permission level 4 is required.
- `/list`: permission level 4 only.
- `/seed`: permission level 4 only and available only when `show-seed=true`
  exists in `server.properties`.

The vanilla `/msg`, `/tell`, `/teammsg`, `/tm`, and `/me` commands are removed.

Initial administration commands include:

- `/e realm add|remove|list`
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
