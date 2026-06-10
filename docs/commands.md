# Elarion Commands

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

This is the command surface contract. Use it as the source matrix for command
integration tests and GameTests.

## Player Commands

```text
/help [command]
/rc <message>
/ac <message>
/pm <player> <message>
/r <message>
/w <message>
/yell <message>
```

Rules:

- `/help` lists player-facing commands and descriptions.
- `/rc` sends Realm Chat to the sender's Realm.
- `/ac` sends alliance chat to the sender's connected alliance group.
- `/pm` and `/r` follow Realm/relationship visibility rules.
- `/w` is local whisper chat, not private messaging.
- `/yell` is local yell chat and uses its configured cooldown.
- OP level 4 does not bypass local chat distances by default.
- `/spy chat` is the explicit OP tool for seeing chat outside normal distance
  or Realm scope.

## OP Level 4 Commands

```text
/spy chat
/list
/seed
/random value <range>
/random roll <range>
/random reset ...
/e reload
/e realm ...
/e citizen ...
/e title ...
/e ability ...
/e reward ...
/e progression ...
/e history ...
/e history chronicle list [weeks]
/e history chronicle inspect <week> [limit]
/e economy wallet get <player>
/e economy wallet give|take <player> <amount>
/e economy wallet deposit|withdraw <player> <amount>
/e economy treasury get <realm>
/e economy treasury give|take <realm> <amount>
/e economy transfer player <from> <to> <amount>
/e economy transactions player <player> [limit]
/e economy transactions realm <realm> [limit]
/e economy pulse|recalculate|reload
/e world ...
/e perf status
/e perf queues
/e perf config
/e perf worlds
/e perf realms
/e perf realm <realm>
/e perf hotzones
/e perf security
/e security status
```

All `/e ...` commands are OP level 4 unless a future feature is explicitly
approved as player-facing.

`/random` is Minecraft's vanilla random-number and named random-sequence
command. The bare `/random` root is incomplete; examples are
`/random value 1..10` and `/random roll 1..20`. Elarion gates the entire command
at OP level 4 so its vanilla permission-2 subcommands do not bypass the server
command policy.

## Current Coverage

The GameTest module includes a small command harness:

```text
tests/gametest/src/main/java/panetina/elarion/tests/CommandGameTestSupport.java
```

It currently covers:

- removed vanilla messaging commands
- player-facing chat command registration
- OP level 4 gating for `/e`, `/list`, `/seed`, and `/random`
- real execution of `/e history category ...`
- Economy API round-trip and real execution of treasury/pulse commands

## Test Matrix

Future command tests should cover:

- non-OP rejection for every OP command group
- OP level 4 acceptance for every OP command group
- player command availability for non-OP players
- `/help` entries for every player-facing command
- tab suggestions for players, Realms, titles, abilities, reward IDs, world IDs,
  progression rule IDs, and relationship values
- command error text for missing targets, invalid IDs, and failed rules
- command side effects persist after restart where runtime state is changed
- command output uses nickname/identity presentation where expected

Keep command behavior covered by focused unit tests for pure helpers and by
GameTests where Minecraft server context is required. Use manual dev-server
testing only for GUI/client presentation details that cannot yet be automated.
