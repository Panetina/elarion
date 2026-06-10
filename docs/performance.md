# Elarion Performance

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

Baseline target: unknown online host. There is a high chance the CPU is shared,
so keep headroom for the full Vanilla+ modpack, shared-host CPU scheduling,
slower disks, network variance, and other mods. Do not tune defaults around one
local desktop CPU.

## Task Budgets

Core reads task budgets from:

```text
config/elarion/addons/optimization/performance.yml
```

Defaults:

```text
hardware profile: unknown_online_host
cpu sharing risk: likely
io workers: 1
compute workers: 2
max queued server tasks: 4096
max server applies per tick: 256
max server apply budget: 2 ms
queue warning threshold: 2048
slow operation warning: 50 ms
sample interval: 30 seconds
headroom warm: 35 mspt
headroom pressure: 45 mspt
headroom overloaded: 50 mspt
```

These are conservative starter defaults. Increase them only after `/e perf`
diagnostics, spark-like profiling, or repeatable load tests show unused
headroom on the actual host.

Use:

- IO queue for filesystem-heavy saves, reports, archives, and exports.
- Compute queue for immutable validation, indexing, summaries, and planning.
- Server queue for bounded world/player/entity/inventory application.

World block-abundance replacement work is queued through the Core server-thread
queue instead of running directly in the chunk-load callback. Each chunk is
split into 16-block-tall vertical slices. A chunk is recorded as processed only
after every slice has finished. If the server queue is full while scheduling a
chunk, that chunk remains unprocessed so a later load can retry.

History writes are queued and batched by monthly JSONL file. `HistoryService`
creates the event synchronously, then disk writes flush periodically, when a
batch reaches its threshold, and during server shutdown before task workers
stop. History queries drain pending writes first so existing query behavior
stays predictable.

Weekly Chronicle archives are generated from compact monthly history indexes,
not by scanning every raw JSONL file. Automatic Chronicle generation runs
through the Core task queues instead of ordinary server-tick work.
Public-history feeds compose recent Chronicle archives with live monthly
indexes under configured limits.

Economy balances remain in memory during gameplay. Every transaction performs
one compact append to the monthly Economy journal before a successful balance
mutation. Forced journal writes are enabled by default because losing or
partially applying money movement is worse than the small bounded IO cost.
`economy-transaction-journal` timing appears in slow-operation diagnostics.
Balance snapshots are periodic, atomic, queued through the IO worker, and
joined before the final shutdown snapshot so an older queued save cannot
overwrite newer balances.

OP Economy transaction queries scan only configured recent monthly Economy
journals and stop at a bounded result count. Rich player-facing account history
must receive dedicated account indexes or summaries before release.

Identity sync uses per-tick coalescing for repeated full, viewer, and subject
sync requests. Join/bootstrap and explicit initial client requests can still
perform immediate full syncs; high-frequency citizen/title/nickname changes
collapse into one tick's worth of sync work.

Progression tick-side work is indexed:

- continuous title rules are grouped by sample interval
- progression regions are grouped by world ID
- worlds with no configured progression regions skip region checks
- region strings are not built when no matching rule can use them

`/e perf status` reports:

- host profile and CPU sharing risk
- loaded worker counts and fallback status
- sampled headroom state
- sampled average tick time
- queued, completed, failed, and rejected server-thread tasks
- queued, active, completed, and failed IO tasks
- queued, active, completed, and failed compute tasks
- last apply tick time
- rolling server-queue apply time
- max observed server-queue apply time
- configured apply budget
- queue pressure
- slow apply tick count
- slow-operation counters for Core reloads, history writes, state saves, and
  server-queue application

`/e perf queues` reports rejected, completed, and failed server-task counts by
family, plus submitted, completed, and failed IO/compute counts by family.

`/e perf config` reports loaded config values, fallback status, and validation
warnings for fields that used safe fallback values.

`/e perf worlds` reports the latest sampled loaded-world diagnostics:

- online players
- loaded chunks
- entity count
- block entity count
- broad entity categories
- block entity type groups
- one-sample deltas
- rolling in-memory trends across the last 10 samples

`/e perf realms` reports the latest sampled Realm diagnostics:

- Realm ID
- Realm world ID
- online players

Task family is derived from the task name before `:`. For example,
`world-block-rules:elarion:realm_world_1:12:4` reports under
`world-block-rules`.

Current internal performance metric names include:

```text
server-queue-apply
world-block-rules-queue-full
world-block-rules-slice-completed
world-block-rules-slice-failed
history-write-queued
history-write-batch
history-write-flush-failed
history-index-queued
history-index-write
history-index-flush-failed
chronicle-archive-write
chronicle-archive-write-failed
```

World and Realm diagnostics are sampled at the configured interval. Commands
read the cached sample instead of forcing a heavy scan on every command call.

`/e perf realm <realm>` reports one sampled Realm.

`/e perf hotzones` lists the sampled worlds with the highest entity/chunk
pressure. It includes loaded chunk count, entity count, broad entity categories,
and in-memory deltas from the previous sample. This is a warning tool, not a
full profiler.

`/e perf security` reports the Security addon's published evidence summary.
Future security checks must remain sampled or event-driven.

## Headroom States

The Optimization addon reports coarse server headroom from Minecraft's sampled
average tick time:

- `HEALTHY`: below the warm threshold.
- `WARM`: approaching pressure; avoid raising budgets.
- `PRESSURE`: close to the server tick budget; investigate before adding work.
- `OVERLOADED`: at or above the tick budget; reduce load or profile urgently.
- `UNKNOWN`: no sample yet.

Use this as an early warning signal. For deep diagnosis, still use a profiler
such as spark or equivalent host tooling.

## Profiler Workflow

For final host testing, use `/e perf` first and a profiler second:

1. Run `/e perf status`, `/e perf worlds`, and `/e perf hotzones`.
2. If headroom is `PRESSURE` or `OVERLOADED`, run a spark-style profiler on the
   host during normal gameplay.
3. Compare profiler hotspots with Elarion diagnostics before changing budgets.
4. Reduce work or fix hot systems before increasing worker counts.
5. Keep profiler reports outside source control unless a small excerpt is
   needed in an issue or design note.

Persistent performance snapshots under world state are intentionally deferred.
Add them only if live diagnostics repeatedly show that historical trend data is
worth the extra IO.

## Host Profiles

Recommended profile intent:

- `shared-host`: keep defaults, avoid extra workers, rely on sampled evidence.
- `dedicated-host`: raise budgets only after profiling shows stable headroom.
- `local-dev`: may use higher budgets for testing, but do not commit gameplay
  behavior that depends on local hardware.

The default profile remains `unknown_online_host` with `cpu-sharing-risk:
likely`.

## Compatibility Notes

Bobby and Distant Horizons are optional visual/client-side compatibility
targets. They are not authority systems.

- Bobby cache state must not affect protection, visibility, chat, portals, or
  anti-cheat.
- Distant Horizons visual LOD must not count as active gameplay chunks.
- If Distant Horizons server-side features are installed, treat them as shared
  server cost and keep Elarion diagnostics conservative.
- Elarion should not increase sync frequency because a visual LOD client is
  present.

## Hotzone Detail

Current hotzones rank sampled worlds by entity count and loaded chunks. They
also show broad entity categories such as `monster`, `creature`, `ambient`,
`water_creature`, and `misc`, event-tracked block entity type groups, per-world
deltas since the previous sample, and a bounded 10-sample in-memory trend
window.

Later, add richer detail only when needed and only through stable hooks:

- ticking block entities where stable APIs expose them
- ticking chunks
- security evidence overlays

## Forbidden Patterns

- global scans every tick
- config parsing during gameplay actions
- world mutation from background threads
- full blueprint or GUI sync every tick
- unbounded runtime writes
- one file containing many unrelated gameplay systems

## Preferred Patterns

- event-driven updates
- sampled diagnostics
- dirty saves every fixed interval and on logout/server stop
- compact runtime state
- immutable validated config definitions
- cache invalidation on source-truth changes
- rate-limited player feedback
- queue pressure reporting before increasing worker counts
- sampled world/Realm diagnostics instead of continuous scans
- slow-operation counters before persistent performance logs

## Health Checks

Run these before considering a cleanup pass complete:

```powershell
.\gradlew.bat build
rg --files -g "*.java" | ForEach-Object { [pscustomobject]@{ Lines = (Get-Content $_).Count; Path = $_ } } | Sort-Object Lines -Descending | Select-Object -First 10
Test-Path docs/api.md; Test-Path docs/performance.md; Test-Path docs/config.md
git status --short
```

Also run the canonical terminology and private branding scans from project
policy before committing documentation changes. They should produce no matches.
