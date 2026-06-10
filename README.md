# Elarion

Elarion is a Fabric 1.21.1 platform mod organized as one canonical Core plus optional addons.

Author: Panyel

Team: Panetina Team

The durable product goals, architectural rules, legacy-mod findings, current
status, and roadmap are maintained in [PLAN.md](PLAN.md). Read it before making
architectural or gameplay changes.

Focused implementation contracts live under [docs/](docs/), especially
[docs/api.md](docs/api.md), [docs/performance.md](docs/performance.md), and
[docs/config.md](docs/config.md).

Canonical setting and narrative facts live in [LORE.md](LORE.md). Keep gameplay
text and future story additions consistent with it.

Numbered histories and character books from the lost first age live in
[Folklore/README.md](Folklore/README.md). The present setting is **Elarion: The
Verdant Reawakening**, where nature has reclaimed most earlier civilization
and surviving records can eventually be recovered for Worldheart's archive.

## Modules

- `platform/core`: realms, citizens, titles, abilities, identity, `/rc`, rewards, storage, task queues, and addon APIs.
- `addons/*`: feature modules that consume Core state and never duplicate canonical citizen identity.
- `addons/economy`: official sigil, wallets, Realm treasuries, audited transactions, and Economy monitoring.
- `addons/security`: security, anti-cheat, and anti-AFK farm protection foundation.
- `addons/optimization`: performance diagnostics and bounded task queue integration.

## Commands

- `/rc <message>`: Realm Chat; available to regular players.
- `/ac <message>`: Alliance Chat for your Realm and directly allied Realms.
- `/pm <player> <message>`: privately message someone in your Realm or an
  eligible member of a `GLOBAL` Realm.
- `/r <message>`: reply to the last player who privately messaged you.
- `/w <message>`: italic local whisper within 4 blocks.
- `/yell <message>`: bold local yell within 128 blocks, with a 5-minute
  cooldown.
- `/help [command]`: show commands currently available to you with explanations.
- `/e ...`: administration root; permission level 4 is required.
- `/list`: permission level 4 only.
- `/spy chat`: permission level 4 only; toggles chat moderation view. `/spy`
  is the future modular admin observation namespace.
- `/seed`: permission level 4 only and available only when `show-seed=true`
  exists in `server.properties`.

The vanilla `/msg`, `/tell`, `/teammsg`, `/tm`, and `/me` commands are removed.

Initial administration commands include:

- `/e realm add|remove|list`
- `/e citizen info`
- `/e citizen nickname set|clear`
- `/e title set|clear|list`
- `/e title grant|revoke|inspect|player`
- `/e title active set|clear`
- `/e title claims list|release`
- `/e title repair`
- `/e ability check|grant|revoke`
- `/e reward run`
- `/e perf status|queues`
- `/e reload`

## Data

Editable YAML is generated under `config/elarion/`. Mutable citizen records are stored under
the active world save in `elarion/citizens/`.

## Modpack Compatibility

Elarion is intended to run inside a larger Vanilla+ Fabric modpack. Gameplay
systems should use registry IDs, tags, config, and optional addon integrations
instead of assuming vanilla-only crops, mobs, recipes, villagers, backpacks,
terrain, storage, or voice features.

Performance budgets must leave headroom for the rest of the modpack. Elarion
diagnostics track Elarion cost; they are not a replacement for general server
performance mods.

## Addon API

Use `ElarionApi.get()` to access Core services. New addon work should prefer the
grouped facades: `identity()`, `realm()`, `messaging()`, `progressionApi()`,
and `system()`. Addons can register abilities, reward action handlers,
progression listeners, citizen listeners, task queue work, and OP-only `/e`
subcommands.

Addon bootstrap code must implement `ElarionAddon` and register under the custom
`elarion:addon` entrypoint in `fabric.mod.json`. Core invokes these entrypoints only after
`ElarionApi` is ready. Addons must not register their Core-dependent initializer under
Fabric's ordinary `main` entrypoint.

Use `.\gradlew.bat runClient` or `.\gradlew.bat runServer` from the repository root. These
tasks use the `dev` module and load Core with every addon in one shared development runtime.
