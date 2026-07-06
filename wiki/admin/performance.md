# Performance And Security

Admin guide for runtime diagnostics, TPS triage, and host/modpack profiling.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

`Implemented`, `Admin-only`, `Manual verification needed`

Optimization exposes live diagnostics. Security exposes an evidence/status foundation. Final confidence still requires testing with the full modpack and host.

The Admin Panel Systems tab exposes the loaded `optimization` config domain as
a read-only view of Core task budgets and monitoring thresholds. Changes still
require editing `config/elarion/addons/optimization/performance.yml` and
restarting the server.

## Commands

```text
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

## TPS Drop Workflow

1. Run `/e perf status`.
2. Run `/e perf queues`.
3. Run `/e perf worlds` and `/e perf hotzones`.
4. Check `/e perf security` and `/e security status`.
5. Check server logs for slow Elarion operations.
6. If needed, profile with the final host and modpack using a profiler workflow.

## Current Active Risks

- Full Vanilla+ modpack load can change performance behavior.
- Terrain generation, mobs, backpacks, farms, and voice chat need live profiling.
- Portal mounted travel and pet following need a bounded design before enabling.
- Rich Chronicle, Ledger, Newspaper, and search views need indexes/read models before becoming player-facing.

## Source-Backed Notes

- Performance docs: [../../docs/performance.md](../../docs/performance.md)
- Optimization addon docs: [../../docs/addons/optimization.md](../../docs/addons/optimization.md)
- Security addon docs: [../../docs/addons/security.md](../../docs/addons/security.md)
