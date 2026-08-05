# Client And Server Distribution

## Canonical ownership

`distribution/mods.json` is the only curated third-party artifact manifest.
It pins Minecraft/Fabric, official download URL, exact version, filename,
SHA-512, size, side, role, source, and license. `build.gradle` consumes it for
exports and IntelliJ/Loom runs. Local launcher or Modrinth profile folders are
never dependency sources.

Third-party jars are resolved directly from their official origin and verified
against the pinned SHA-512. The launcher manifest tells a launcher to download
those jars from that origin; it must not rehost jars whose license or project
policy disallows redistribution. Elarion jars, Elarion configs, and Elarion
resource packs are release-bundle files.

## Install roots

`exportMods` builds exactly two roots:

```text
build/export/client/
  mods/
  config/
  resourcepacks/
  manifest.json

build/export/server/
  mods/
  config/
  manifest.json
```

## Incremental mod export rule

Normal mod changes use an incremental export. Build the changed module, copy
only its changed JAR/resources/config into the applicable export root, compare
the resulting SHA-512/hash and report the exact changed-file list. Do not run
the full `exportMods` task for a single-mod edit, because it can rewrite
timestamps or generated metadata for unrelated files and make the upload set
unclear. A full export is required only for an explicit release, a manifest
change, or a cross-module change whose dependency closure was intentionally
selected.

The client root contains all exportable Elarion modules, both-side gameplay
dependencies, the stable client performance baseline, visual compatibility,
Distant Horizons, Bobby, LambDynamicLights, No Chat Restrictions, and support
libraries. It does not contain server-only world-generation helpers or
builder/admin tools.

The server root contains all exportable Elarion modules, both/server gameplay
dependencies, Lithium, FerriteCore, ModernFix, Axiom, and WorldEdit. Distant
Horizons and Bobby are deliberately client-only; the server does not generate,
store, or synchronize DH LODs.
LambDynamicLights is also deliberately client-only; the Underworld addon uses
its optional client entrypoint while the dedicated server loads no Lamb API or
runtime classes.

## Launcher manifest contract

Each `manifest.json` is deterministic except for `generatedAt` and contains:

- schema/release/Minecraft/Fabric/side identity;
- the required Java 21 runtime, recommended maximum heap, and side-specific JVM
  arguments;
- sorted relative path, role, size, and SHA-512 for every file;
- official source URL, project/version ids, license, and official download URL
  for third-party files;
- `official-download` or `release-bundle` delivery ownership;
- `required-exact`, `always-replace`, or `if-absent` update policy.

The repository does not contain a launcher implementation or release-signing
secret. A launcher consumes this contract later, verifies every SHA-512, and
fails closed on an unknown path, missing file, duplicate file, wrong side, or
hash mismatch. Signing and immutable release-feed publication remain external
release responsibilities.

The client contract recommends a 6144 MiB maximum heap and requires
Generational ZGC (`-XX:+UseZGC -XX:+ZGenerational`). This is client-only: it
matches Distant Horizons' concurrent-collector guidance and removes its G1
warning. The server keeps the Java 21 default collector with a 4096 MiB
recommended maximum heap. IntelliJ generic, Client One, and Client Two runs use
the same client collector flags as the launcher contract.

## Configuration ownership

Files under `distribution/client/managed/` and
`distribution/server/managed/` are safety invariants and are
`always-replace`. Current client safety invariants disable DH generation and
automatic update installation, prevent Entity Culling from changing entity
ticks, safelist Elarion mount rendering, disable speculative More Culling
behavior on unknown modded blocks, force EBE resource-pack compatibility, and
pre-create Bobby's cache root so cache-path ownership is explicit.
The managed Distant Horizons profile keeps LOD rendering but disables its
vanilla-fade hand-off and prevents LOD overdraw into the vanilla chunk envelope,
avoiding first-join Iris/Hysteria chunk compositing artifacts. The managed LambDynamicLights config uses fancy mode for smooth Iris/shader
compatibility, its culled chunk-rebuild scheduler, and adaptive 5/8-chunk
sleep thresholds. Underworld spectral players emit a low bounded luminance of
6/15 through Lamb's entity-light API.
The Bobby default disables its built-in old-region cleanup because Bobby 5.2.4
can repeatedly fail on its own non-empty `last_access` directories on Windows;
launcher maintenance owns any explicit cache pruning instead.
Both sides disable ModernFix's `paper_chunk_patches` and
`remove_biome_temperature_cache` mixins because Lithium owns those same
chunk-ticket and biome-temperature implementations. The server also disables
Lithium collision/block-cache children that require its intentionally disabled
block-tracking mixin. These explicit effective settings avoid runtime dependency
fallbacks without enabling broader per-block entity tracking.

Files under `distribution/client/defaults/` are `if-absent`. They are delivered
through YOSBR so player graphics and preference changes survive updates.
IntelliJ/Loom synchronization also copies the YOSBR templates, applies missing
defaults to existing development instances, and always refreshes safety files.

## Managed development synchronization

`syncDevRuntimeMods` installs:

- both/server/admin artifacts in `dev/run/mods`;
- both/client artifacts in generic, Client One, and Client Two;
- admin artifacts only in Client One;
- managed/default client configs and shared resource packs in every client.

Each generated mods directory has `.elarion-managed-mods.json`. Subsequent
syncs remove only files recorded by that marker plus the explicit bootstrap
set from the prior build contract. Unrelated local jars are not wildcard
deleted. The marker makes version replacement and removal deterministic.
If a local marker is unreadable, synchronization ignores it, removes the same
canonical bootstrap set, and writes a fresh marker; a damaged local marker
cannot block a dev-client launch or leave stale managed artifacts authoritative.

The same dev-only synchronization enforces `online-mode=false` and
`enforce-secure-profile=false` in `dev/run/server.properties`, because the
three Loom clients use deterministic offline test identities. This never alters
the distribution server configuration or a published server.

The root `runServer`, `runClient`, `runClientOne`, and `runClientTwo` wrappers
also order the corresponding Loom run strictly after this synchronization.
This prevents Fabric from scanning a JAR while it is being replaced.

## Verification

`verifyDistributionManifest` rejects duplicate ids/files, incomplete metadata,
wrong Minecraft/Fabric pins, invalid sides, invalid SHA-512 values, or missing
managed/default config groups. `exportMods` then verifies every copied
third-party jar against its pinned hash before writing launcher manifests.

Runtime promotion additionally follows the gates in
`docs/reports/FABRIC_1_21_1_PERFORMANCE_CATALOG.md`. Passing a build proves
artifact composition; it does not by itself prove client rendering, DH/Bobby
travel, dedicated startup/restart, soak stability, or measured performance.
`dev/tools/test-performance-distribution.ps1` runs the repeatable two-hour
server/client soak, safe managed-world travel, resource sampling, and evidence
capture used by that promotion gate.
`dev/tools/benchmark-performance-server.ps1` runs the controlled ABBA server
comparison and restores its exact temporarily staged jars in `finally`.
Executed results and remaining resource-pack blockers are recorded in
`docs/reports/PERFORMANCE_DISTRIBUTION_VALIDATION.md`.

`verifyExcaliburedCit` validates the active Minecraft 1.21.1 Excalibured CIT
layer before distribution verification. Active item definitions must target an
item, may not use removed legacy NBT predicates, and every explicit model must
resolve inside the tracked pack. The corrected pack uses 1.21 item-component
conditions for custom names, firework flight duration, painting variants, and
suspicious-stew effects.

The required DH 3.2.0-b pin remains beta software. It replaces 2.3.0-b because
runtime calibration reproduced that older build's upstream per-frame OpenGL
error 22,084 times in 96 seconds.
