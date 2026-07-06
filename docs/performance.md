# Performance Notes

Source-backed operational notes for Elarion performance.

## Current Position

The project has already completed major optimization passes around task queues,
history batching, identity sync coalescing, Shrine/Portal bounded behavior, and
shared UI reuse. Keep future features bounded and server-authoritative.

## Active Risks

- Government vote scanning is acceptable during development, but should move to
  interval/deadline scheduling before many Realms run concurrent elections.
- Large services should split along ownership boundaries before adding new
  behavior:
  - `GovernmentStateService`
  - `PortalRouteService`
  - large client UI screens
- Chronicle/newspaper/ledger/search views need dedicated indexes or summaries
  before becoming rich player-facing views.
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
