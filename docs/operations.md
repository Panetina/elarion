# Elarion Operations

Last reviewed: 2026-06-10

Author: Panyel  
Team: Panetina Team

This is the practical server-operator workflow for performance and generated
files.

## When TPS Drops

1. Run:

   ```text
   /e perf status
   /e perf worlds
   /e perf hotzones
   /e perf queues
   ```

2. Check headroom:

   - `HEALTHY`: no immediate Elarion action needed.
   - `WARM`: avoid raising budgets; watch queue and hotzone trends.
   - `PRESSURE`: profile before adding features or increasing worker counts.
   - `OVERLOADED`: reduce load, inspect hotzones, and run a profiler.

3. Check queues:

   - server queue rejected tasks means gameplay application work is overloaded.
   - IO queue growth means saves/reports/history are falling behind.
   - compute queue growth means validation/planning/archive work is too heavy.

4. Check hotzones:

   - high entity count suggests mobs, item entities, villagers, or modded mobs.
   - high block entity count suggests storage, machines, hoppers, or modded
     blocks.
   - rising trends matter more than one isolated sample.

5. Use a spark-style profiler on the final host if `/e perf` shows pressure.
   Compare profiler hotspots with Elarion diagnostics before changing budgets.

## Budget Rule

Do not raise worker counts or queue budgets just because the server feels slow.
First identify whether the problem is Elarion, another mod, world generation,
entities, block entities, disk IO, networking, or shared-host CPU scheduling.

## Generated Files

Do not commit:

```text
*.log
*.log.gz
logs/
**/logs/
dev/run/
dev/client1/
dev/client2/
```

If generated logs become tracked, remove them from Git and keep the ignore
policy intact. Do not delete user-authored config or world state as part of log
cleanup.

## Config Regeneration Policy

Generated defaults are starter files, not runtime state.

- Missing config files may be regenerated from defaults.
- Existing config files should be migrated or rejected with clear errors.
- Development-only config can be deleted/regenerated when explicitly approved.
- Production config should be backed up before schema migration.
- Runtime state under `world/elarion/` must not be silently replaced by config
  regeneration.
