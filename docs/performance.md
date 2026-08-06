# Performance Notes

Source-backed operational notes for Elarion performance.

## Current Position

The project has already completed major optimization passes around task queues,
history batching, identity sync coalescing, Shrine/Portal bounded behavior, and
shared UI reuse. Keep future features bounded and server-authoritative.

## Active Risks

- Concurrent Client One/Client Two rendering triggers native `glfw.dll`
  failures on this Windows QA host. Each client and authority direction passes
  independently; do not add gameplay workarounds unless the fault reproduces
  outside the host runtime/capture stack.
- One startup run observed a single slow `server-queue-apply` warning while
  managed worlds opened. Profile it only if it grows or appears during ordinary
  gameplay.
- Chronicle/newspaper/ledger/search views need dedicated indexes or summaries
  before becoming rich player-facing views.
- Split large services/screens only when an evidenced ownership boundary makes
  the resulting pieces simpler; do not split merely by file size.
- `addons/angling/reference/**` is reference-only and should not be included in
  ordinary source audits.

## Operational Commands

```text
/e perf status
/e perf queues
/e perf worlds
/e perf realms
/e perf hotzones
/e perf security
/e security status
```

## Rule

Prefer event-driven, cached, indexed, queued, or bounded work. Avoid global
per-tick scans, broad JSONL reads, repeated config parsing, and client-trusted
mutations.
