# Performance Distribution Validation

## Result

The Fabric 1.21.1 performance distribution passed its dedicated-server,
joined-client, managed-world travel, restart, cache-persistence, ZGC, and
controlled server A/B checks. Distant Horizons and Bobby are client-only; the
46-jar server export contains neither mod.

This is not a claim that third-party software is mathematically “100% stable.”
The narrower result is that the performance stack has no known runtime fault in
the exercised paths. The complete client resource load still reports eleven
legacy Excalibured/Polytone colormap-target errors, so the whole pack does not
yet satisfy a literal zero-error release gate.

## Two-hour joined soak

Primary evidence:
`build/performance-validation/20260718T222304Z-soak/report.json`.

- Full requested 120-minute joined period completed in Peaceful difficulty.
- Client Two joined the dedicated server and completed 12 automatic rotations
  through Lobby, Worldheart, and Realm 1.
- 239 timed samples were captured at 30-second intervals.
- Server and client both exited with code `0`; every dimension saved cleanly.
- Server problem lines: `0`.
- Distant Horizons `GL_INVALID_OPERATION` lines: `0`.
- Player deaths and unexpected disconnects during the valid Peaceful run: `0`.
- Server working set: 3104.5 MiB first, 2930.3 MiB last, 3353.8 MiB peak.
- Client working set: 3076.8 MiB first, 4381.7 MiB last, 5134.8 MiB peak.
  Dimension and resource loading caused the initial rise; over the final
  30 minutes the fitted client trend was only +1.9 MiB/hour and private memory
  was unchanged at 5540.4 MiB.
- Bobby cache grew from 3.265 to 4.189 MiB. DH storage grew from 10.383 to
  10.480 MiB. Both changes were bounded by the exercised travel.

The run began on G1 so that the original DH warning could be evaluated without
changing the JVM mid-test. It exposed two repeatable Bobby 5.2.4 Windows
cleanup errors against non-empty `last_access` directories. The canonical
Bobby default now sets `delete-unused-regions-after-days=-1`; explicit launcher
maintenance owns cache pruning instead of Bobby's faulty automatic path.

## Restart and final client policy

Evidence:

- `build/performance-validation/20260719T003728Z-soak/report.json`
- `build/performance-validation/20260719T004322Z-soak/report.json`

The first dev-only restart attempt hit Fabric Loader's transient runtime-remap
`ClosedFileSystemException` before Minecraft opened. Stopping the stale Gradle
daemon removed it; this remap path is not used by launcher-distributed jars.
Subsequent reconnects completed with clean exit codes and preserved DH/Bobby
data.

Java 21 Generational ZGC is now the client launcher and IntelliJ policy:
`-XX:+UseZGC -XX:+ZGenerational`. The server keeps its default collector. The
final joined smoke verified both flags on the live client and produced:

- server problems: `0`;
- Bobby errors: `0`;
- DH OpenGL errors: `0`;
- DH G1 warnings: `0`;
- clean client/server exits: `0` / `0`.

The client manifest recommends a 6144 MiB maximum heap; the server manifest
recommends 4096 MiB. Both require Java major version 21. Java 21's official
Generational ZGC contract is documented by [OpenJDK JEP 439](https://openjdk.org/jeps/439)
and the [Oracle Java 21 ZGC guide](https://docs.oracle.com/en/java/javase/21/gctuning/z-garbage-collector.html).

## Controlled server A/B

Evidence:
`build/performance-validation/20260719T004932Z-server-ab/report.json`.

The reusable `dev/tools/benchmark-performance-server.ps1` harness used ABBA
ordering. Its baseline temporarily disabled only Lithium 0.15.3, FerriteCore
7.0.3, and ModernFix 5.25.1. All four trials had zero server problem lines and
exit code `0`; the three exact jars were restored and their SHA-512 values
revalidated afterward.

| Metric | Optimized mean | Baseline mean | Result |
|---|---:|---:|---:|
| Wall startup | 47.135 s | 49.619 s | 2.484 s / 5.01% faster |
| Minecraft-reported final startup phase | 0.263 s | 1.796 s | 1.533 s faster |

The 20-second post-start memory readings varied too widely between optimized
trials to support a memory-reduction claim. The two-hour time series, not this
short A/B sample, is the authoritative bounded-memory evidence.

## Defects found and resolved

- DH 2.3.0-b produced 22,084 per-frame OpenGL errors in 96 seconds. It was
  replaced by official Fabric 1.21.1 DH 3.2.0-b and the managed v4 config.
- Bobby's automatic Windows cleanup repeatedly failed; it is now disabled in
  launcher defaults and all IntelliJ client profiles.
- Redirected Gradle console input prefixed the first server command with a BOM
  and stale `latest.log` content could satisfy readiness checks. The soak
  harness now constructs ASCII stdin before process start and resets only the
  exact generated log files before launch.
- Excalibured's active CIT layer was migrated separately and
  `verifyExcaliburedCit` now validates 267 definitions with zero CIT errors on
  fresh later loads.

## Remaining blocker

Eleven Polytone errors remain for legacy OptiFine colormap PNGs in
`Elarion Excalibured v1.zip` (`crops`, `redstone_disabled`, `sky0`, `biomes`,
`fog0`, `pine_disabled`, `swampgrass`, `water_fog`, `swampfoliage`, and
`uncolor`). They are resource-pack target-definition defects, not performance
mod crashes. The pack needs explicit Polytone target migration before the
complete client can truthfully claim zero log errors.
