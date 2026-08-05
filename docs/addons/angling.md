# Elarion Angling

Status: server-authoritative fishing/persistence foundation and partial native
content runtime. Public fishing and Delight remain release-gated, and both modules are
excluded from canonical exports until the port and release-readiness
requirements are complete.

## Fixed Port Baseline

* Target: Minecraft 1.21.1, Fabric Loader, Fabric API, Yarn, and Java 21.
* Main mod ID: `elarion_angling`.
* Delight mod ID: `elarion_angling_delight`.
* Frozen local NeoForge reference revision:
  `016161dfc2d556d20fa641cd275e18c539256d4d`.
* The local reference is the owner-authorized source for the Fabric port,
  including its code, content, edited artwork, models, sounds, text, and other
  resources. Authorized files may be adapted, renamed, and copied into the two
  Elarion production modules.
* The reference directory itself is not a Gradle source root. The raw checkout,
  Git metadata, NeoForge build files, and unrelated development files must not
  be placed inside a jar.
* The tracked baseline contains 384 Java, 1,526 JSON, 619 PNG, and 11 OGG
  files; 148 native catches; and 315 conditioned compatibility catches.
  The earlier 322 compatibility figure counted seven rarity-tag/treasure-map
  JSON files as catches; the itemized audit proves 463 actual catch-definition
  JSON files and keeps those seven non-catch data files separately tracked.
* `addons/angling/porting/parity-manifest.json` is the machine-readable audit
  baseline. Later slices must give every audited unit an allowed disposition.

## Implemented Port Foundation

`addons/angling/porting/inventory/source-inventory.json` records all 2,603
authorized reference files outside ephemeral `.git`, `.gradle`, `build`,
`run`, and log directories. Entries are deterministically sorted and record
module ownership, source area, byte size, SHA-256, and an explicit parity
disposition. Regeneration preserves reviewed dispositions by path.

The main module now owns immutable, versioned codecs for catch output, rarity,
size/weight distribution, restrictions, minigame modifiers, sweetspots,
difficulty, and the complete catch-definition envelope. Polymorphic nodes are
bounded reload DTOs. All 16 restriction types, all nine modifier types used by
native catches, and all nine concrete sweetspot behavior IDs compile into
immutable runtime values before an atomic snapshot is published. They are
never parsed in a gameplay path.

All 12 reference item-component identities are registered: copy-safe bucketed-fish, bobber,
bait, and hook slots; bounded secret-note and player-letter values; caught-fish
presentation; the Netherite-upgrade marker; and bounded tackle-box contents.
Letters enforce the reference UI limits of 15 lines, 40 characters per line,
and 17-character sender names. Tackle-box storage is capped at 900 copied
stacks. Signed guides store at most 512 immutable Core species projections and
the reference 20-character signature; these are historical presentation
snapshots, never canonical progression. Tackle skins persist only a stable ID.
Persistent modifier lists are capped at 64 and compile once during decode or
crafting so gameplay never reparses polymorphic JSON.

`elarion_angling:caught_fish_info` stores the definition ID and
server-computed size, weight, percentile, rarity, golden, and perfect
presentation values with persistent and bounded packet codecs. No item
component is canonical catch truth or can grant catches, metrics, titles,
rewards, or tournament credit.

Verification includes:

* all 463 catch-definition JSON files in the frozen local reference decoding
  through the Fabric schema;
* immutable collection and bounded polymorphic-node checks;
* caught-fish persistent and packet round trips;
* tracked inventory structure and exact local-reference hash comparison.

## Ported Native Substrate

The approved identity contract contains 124 unique normal-fish renames. The
12 extras, seven secrets, and five trophies keep their technical IDs. The
transformer produces exactly 148 unique native catch definitions under the
`elarion_angling` namespace with no NeoForge conditions or source IDs.

The server-data reload path compiles and indexes all native definitions:

* 640 restriction instances through 16 typed schemas;
* 68 native minigame modifier instances through nine typed schemas;
* 352 sweetspots resolved through nine behavior identities;
* O(1) ID lookup and precomputed rarity/catch-type indexes;
* transactional publication that preserves the last valid snapshot when a
  reload fails.

The runtime asset port tracks 1,010 files. All 550 PNG and 11 OGG files are
byte-for-byte identical to the authorized reference; 449 JSON, shader, and
metadata files receive deterministic namespace/ID transformations. Two `.ase`
authoring files are intentionally not packaged.

Fish IDs are replaced in one token-bounded pass, so a target ID can never be
renamed again by a later source rule. English fish display names are derived
from the approved target IDs, visible source branding is replaced with
Elarion Angling, and verification rejects stale source IDs, case-variant source
branding, and missing target translations.

The Fabric item registry now contains 198 entries: the completed basic,
template, ordinary/fireproof fish, bait/hook/bobber, raw/cooked food, 48
bucketable-fish, generic fish-bucket, and all 15 rod identities. Rod defaults
preserve epic/fireproof/single-stack behavior and exact hook/bobber/bait data
components. Guide, letters/notes, hats, and block items remain incomplete. Rod
public use stays behind an explicit false release gate until the client
minigame, renderers, remaining catch modifiers, and parity scenarios pass.

Fabric now registers all 11 frozen sound events and all three server-safe
particle types. Client-only factories preserve the reference bite motion for
water/lava and the rising/fading notification animation without loading client
classes on a dedicated server.

The secure minigame networking foundation replaces the upstream client-result
shape with a typed input-edge payload containing only session ID, bobber ID,
sequence, and press/release/abandon action. A bounded per-session server gate
rejects wrong actors, sessions, and bobbers; negative, replayed, skipped, or
impossible sequences; stale server time; expired/closed sessions; and excessive
per-tick or sliding-window input. Abandonment is terminal, preventing the
upstream success-then-close failure submission race.

The client screen renders only server-provided session state and sends only
sequenced press/release, layer-selection, and abandon edges. Stale or
wrong-session snapshots are ignored, and server terminal closure cannot submit
a later failure.

Core catch telemetry is schema 2: an accepted catch may carry typed
server-derived output, catch type, size, weight, percentile, minigame timing,
hits/outcomes, equipment, fluid, realm, and tournament facts. Core materializes
direct per-species count/performance summaries and migrates schema-1 records;
Angling must use this path rather than persisting guide counters.

All 18 approved Angling metric descriptors are frozen, registered with Core,
and uniqueness-tested. They cover catch counts/performance, tournament
totals/records, and milestone state with only the approved overall, fish,
rarity, realm, and event materializations. Core persists them through a
bounded append-first worker, atomic snapshots, restart replay, and a read-only
ranking API. First durable application emits a versioned metric event, while
exact retries do not. Metric title rules are indexed by metric ID and
reconcile bounded current projections on join/reload.

Native server data adds 121 recipe advancements, 122 recipes, 47 loot tables,
and 57 tags. This corrects the earlier 200 recipe-advancement estimate against
the frozen snapshot. Two Curios resources are dependency-unavailable and one
global loot modifier remains pending.

The five NeoForge data-map files are now deterministically transformed into
domain-specific Elarion resources containing 89 explicit values: aquarium
interactions, item/effect modifiers, tackle skins, and catch treasures. All 89
values compile together into one atomic immutable reload snapshot: 13 aquarium
interactions, eight tackle skins, 92 modifier nodes across 57 selectors using
43 active types, and 11 bounded treasure definitions. The dispatch registry
also validates all nine currently dormant modifier types, covering all 52
reference schemas. Catch selection now applies implemented rarity/luck,
weather/time bypass, new-catch preference, pool addition, and post-selection
overrides. Treasure selection and added loot tables are bounded server-side;
remaining modifier types and compatibility catch-tag membership are pending.

The authority-critical minigame foundation is now server-owned. Fabric has
typed bounded start, input, and correction payloads; the input packet contains
only a session UUID, bobber entity ID, monotonic sequence, and hit/layer/
abandon edge. The receiver executes on the server thread, resolves the entity
in the sender's current world, requires matching bobber ownership, and then
passes the input through bounded session/replay/rate/transition validation.
The deterministic bobber-owned simulation controls pointer movement, seeded
sweetspot placement, progress, perfect status, hit counts, treasure, layers,
native/equipment modifiers and behaviors, and terminal success/failure.
Equipment execution includes lure/throw multipliers, bounce-back, never-lose,
freeze prevention, flip-on-hit, move-on-miss, configured treasure thresholds,
timed/leaf spawning, steady spots, and rarity decay grace. A separate tested
state machine preserves flying, bobbing, biting, and fishing transitions plus
the strict 80-tick bite window. The live nonpersistent Fabric bobber, O(1)
player-to-bobber index, server catch-context capture, weighted selection,
server outcome generation, and rod-to-commit runtime are implemented. The
authoritative client minigame screen foundation is active. Exact bobber/fish
renderers, remaining modifier behavior, and full parity QA are incomplete, so
the gameplay release gate stays false.

Server parity tests also cover the reference low-progress bounce-back trigger
and treasure sweetspot flip behavior. The upstream `sudoku_vanish` field is
retained in the typed schema but is demonstrably dormant in the frozen source;
it does not invent Fabric-only behavior.

Accepted catches use a forced transaction journal. Recovery reuses the same
event UUID/source sequence and commits telemetry, bounded metrics,
deterministic Core reward, accepted-catch event, then a delivered marker.
Exact component-bearing item stacks and bounded additional stacks persist in
the journal; Core's per-action completion indexes make partial multi-item
claims restart-safe. Bucket contents/components and forced-entity ID/position
are stored in the request. Full inventories remain claimable. A transaction
failure closes later admission until restart so source sequences cannot
overtake it.

Bait consumption is the first durable grant action. A lazy per-player,
append-first Angling projection records cumulative bait costs by bait ID and
checkpoints only that player's bounded state. Fabric stores the applied cursor
with the same player file as vanilla inventory and copies it across death.
Join reconciliation reapplies only the difference, closing crash windows
without synchronous gameplay-thread disk I/O. Runtime kill/restart GameTests
remain a release gate. The headless mock-player GameTest already verifies atomic
rod debit, the persisted player cursor, exact retry, and missing-bait refusal;
it does not claim restart coverage.

The last regenerated master inventory records 1,518 ported files, 1,083 pending
files, and two dependency-unavailable files. The mod is not release-ready:
custom content registries, fishing gameplay, screens, tournaments,
compatibility catches, and Delight remain incomplete.

The current focused Core/Angling checks and the 215-task root build pass. The
two generated Angling jars were scanned and contain no raw reference checkout,
Git, NeoForge, or source-brand paths. This is foundation evidence only, not
release-ready or gameplay-parity evidence.

Do not update the frozen source revision or reinterpret the selected port
without explicit owner approval.

## Ownership

Angling owns catch definitions, selection rules, fishing sessions, bobbers,
server minigame runtime, equipment and blocks, active tournaments, tournament
archives, and guide presentation preferences.

Core owns accepted catches, citizen identity, rewards, titles, history,
metrics, rankings, and cross-addon event delivery. Guide counters and future
title/leaderboard/event integrations must consume Core projections rather than
copying catch state into Angling.

Economy owns pricing, balances, settlements, and transactions. The future
selling bin must use Economy's public contract.

Angling Delight owns only its foods, recipes, tags, and Farmer's Delight
presentation. It cannot own catch, quality, achievement, or ranking state.

## Foundation Rules

1. Port in bounded vertical slices; do not bulk-copy NeoForge architecture
   into production source.
2. Gameplay outcomes are server-authoritative. Clients send bounded input or
   action requests and never declare catches, scores, perfect results, rewards,
   achievements, or tournament placements.
3. Tick work is owned by an active bobber, session, block entity, or scheduled
   deadline. No global player/entity/world/definition scans are allowed.
4. Definitions parse and validate during load/reload, publish through an atomic
   immutable snapshot, and expose precomputed indexes to runtime code.
5. Hot paths perform no disk IO, broad parsing, or history reads. Growing
   player-facing data requires bounded summaries or indexes before exposure.
6. Networking uses typed Fabric payloads with direction, ownership, range,
   length, collection-size, and sequence validation.
7. Persistent state is domain-owned, schema-versioned, atomic, restart-safe,
   and tested for round-trip and replay behavior.
8. Optional integrations remain isolated and fail only their own adapter with
   actionable diagnostics.
9. Meaningful accepted outcomes publish through Core contracts. Routine ticks,
   UI opens, and internal diagnostics do not become history or notifications.
10. Every slice updates the parity manifest, source/docs index, tests, and the
    narrowest verification command in the same change.

The build tasks `verifyAnglingFoundation`, `verifyAnglingPortInventory`,
`verifyAnglingTransformedData`, `verifyAnglingPortAssets`,
`verifyAnglingNativeServerData`, and `verifyAnglingDelightFoundation` enforce
identity, hashes, transformed content, ownership, and release boundaries. Run
the matching `compareAngling*` tasks when the ignored reference is present.

## Authorized Port Source and Release Boundary

The owner has declared the files supplied under
`C:/Users/Panyel/Desktop/Modding/`, including the edited Angling and Angling
Delight resources inside the local reference tree, authorized for Elarion to
use, adapt, rename, package, and distribute as part of this port. Repository
work therefore treats those supplied files as approved port inputs and does
not require a separate replacement-art or asset-license phase.

This authorization covers individual code, content, and resource files used by
the port. It does not mean the raw reference checkout should be embedded in a
jar. Ported files must live under the correct Fabric Java/data/resource paths,
use Elarion namespaces and identities, and exclude `.git`, NeoForge build
metadata, caches, and unrelated local files.

Both modules remain excluded from `rebuildExportMods`,
`prepareLiveServerRelease`, and live deployment only because the Fabric port
and gameplay implementation are incomplete.

Once the technical port is complete, the modules may be included in canonical
exports, SMP releases, and production deployment after all of the following
release gates pass:

* native parity and dependency manifests are complete;
* focused and cross-module verification passes;
* dedicated-server and client classloading checks pass;
* persistence and networking protocol versions are documented;
* the packaged resources match the owner-authorized local port source;
* no local reference checkout or reference-development marker is packaged;
* no unfinished, placeholder, or reference-only implementation remains.

Completion of the port does not require another general replacement-asset or
asset-license review. A new review is needed only if future work introduces
files from outside the owner-authorized `Modding` tree or the owner explicitly
changes this authorization.

When every technical release gate passes, the completion slice must set
`releaseReady` to `true` and `excludedFromCanonicalExports` to `false` in both
foundation contracts, remove both module paths from `foundationOnlyModPaths`,
add Angling Delight and its required Farmer's Delight runtime to the aggregate
development/runtime set, rebuild canonical exports, and run the guarded live
release verification. No additional permission prompt is required merely to
include the completed jars in Elarion's mod folders.

## Resume On Another Computer

The Git repository does not contain the ignored reference checkout, Gradle
caches, development worlds, or local deployment settings. When moving the
project by USB:

1. Copy the repository plus the complete
   `addons/angling/reference/upstream-starcatcher-neoforge-1.21.1/` directory.
   The nested `Starcatcher-Delight-1.21/` directory must be copied too; it is
   not recoverable merely by cloning the main upstream repository.

2. Do not rely on copied `.gradle/`, `build/`, `run/`, or `dev/run/` folders.
   They are disposable and may be omitted from the USB copy.

3. Copy `.elarion-deploy.local.psd1` separately only when the new machine is
   authorized to deploy. Treat it as a local secret and never commit it.

4. Install a Java 21 JDK and Git. Use the repository Gradle wrapper; do not
   install a different global Gradle version for the project.

5. From the repository root, verify the transfer:

   ```powershell
   git status --short
   git -C addons/angling/reference/upstream-starcatcher-neoforge-1.21.1 rev-parse HEAD
   .\gradlew.bat :addons:angling:verifyAnglingFoundation :addons:angling-delight:verifyAnglingDelightFoundation
   .\gradlew.bat :addons:angling:compareAnglingPortInventory
   .\gradlew.bat :addons:angling:compareAnglingTransformedData `
     :addons:angling:compareAnglingPortAssets `
     :addons:angling:compareAnglingNativeServerData
   .\gradlew.bat :addons:angling:test :addons:angling-delight:test
   ```

   The reference command must return
   `016161dfc2d556d20fa641cd275e18c539256d4d`. Preserve any unrelated dirty
   worktree changes shown by the first command.

6. Start a new Codex task with this handoff:

   > Read `AGENTS.md`, `RULES.md`, `docs/ai/CURRENT_STATUS.md`, and the Angling
   > route in `docs/ai/routes.json`. Inspect `git status` before editing. The
   > local reference is the owner-authorized source for the full Fabric port,
   > including its code, content, edited art, models, sounds, and text. These
   > files may be adapted, renamed, copied into the Elarion modules, packaged,
   > and distributed without another general replacement-asset review. Do not
   > package the raw checkout or restore the deleted placeholder Angling
   > implementation. Run both foundation verification tasks, then continue only
   > the next bounded parity slice documented in `TODO.md`.

7. Generate the bounded context capsule when needed:

   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File dev/tools/ai-context.ps1 `
     -Task "Continue the Elarion Angling Fabric port" -Mode change -BudgetTokens 12000
   ```

If the reference directory or nested Delight source is missing, stop. Restore
the exact USB copy before porting rather than substituting a newer upstream
revision.

## Next Slice

Complete exact bobber/fish rendering, remaining catch/minigame modifiers,
compatibility catch-tag membership, the Fabric loot-hook replacement, and bait
debit kill/restart GameTests. Then continue blocks, guide,
tournaments, economy integration, Delight, and full parity verification before
player-facing gameplay is enabled.
