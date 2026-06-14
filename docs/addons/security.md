# Security Addon Contract

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

## Owns

- security evidence runtime state
- `/e security status`
- future evidence-first anti-cheat records
- future anti-AFK farm detection
- future optional enforcement policy

## Config

```text
config/elarion/addons/security/security.yml
```

## Runtime State

```text
world/elarion/addon-state/security/
```

Current file:

```text
world/elarion/addon-state/security/evidence.json
```

## Commands

```text
/e security status
```

## Diagnostics

Security publishes a Core diagnostics provider named `security`. The
Optimization addon reads that provider for `/e perf security`.

## Web Bridge Security

The website/backend bridge should treat secrets and identities as server-side
only. Bridge endpoints that accept server sync traffic must:

- require bearer authentication
- hash any stored API secret material
- log accepted and rejected calls
- avoid exposing `DATABASE_URL`, secret values, or raw admin tokens
- keep whitelist and account intake auditable

## Rules

Detection must be event-driven or sampled. Automatic enforcement requires
explicit config, tested thresholds, admin visibility, and audit/history records.
Modded backpacks, farms, villagers, crops, mobs, and chunk behavior must be
treated as potentially legitimate until evidence says otherwise.
