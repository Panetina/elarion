# Realms Addon

Last reviewed: 2026-07-05

Status: Implemented foundation.

## Purpose

`addons/realms` extends Core Realm data with protection and interaction policy.

## Main Source

- `addons/realms/src/main/java/panetina/elarion/addons/realms/`

## Ownership

Core owns Realm definitions, membership, relationships, identity presentation,
and citizen truth. Realms addon owns protection behavior only.

## Notes

- Do not duplicate Core Realm state.
- Protection checks should consume Core APIs and remain bounded.

## Configuration

```text
config/elarion/addons/realms/protection.yml
```

`RealmProtectionConfig` owns shared-world IDs, OP bypass, explosion block
protection, denial-feedback cooldown, and extra mechanism/container block IDs.
The addon loads this file once during initialization; it has no live reload
command. If the file is malformed at startup, the addon logs the parse failure
and uses safe defaults instead of aborting initialization; the file is not
rewritten or silently repaired.

`RealmConfigDescriptors` registers the read-only `realms` domain through
`ElarionApi.system().configs()`. The Admin Panel displays the loaded snapshot
and shipped defaults. Every entry is marked restart-required and
non-runtime-reloadable; discovery does not change protection behavior or read
the file again.
