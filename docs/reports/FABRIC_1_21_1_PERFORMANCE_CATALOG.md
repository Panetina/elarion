# Fabric 1.21.1 Performance Mod Catalog

## Reading the catalog

This is the bounded set of serious Fabric 1.21.1 performance candidates, not
every project carrying an optimization tag. `Ship` is the stable Elarion core;
`Audit` means compatible builds exist but the mod is withheld until it proves
a measurable benefit and passes all release gates. Visual-parity and gameplay
dependencies are documented separately in
`FABULOUSLY_OPTIMIZED_ADAPTATION.md` and `distribution/mods.json`.

## Client-only

| Mod / source | Mechanism | Decision and compatibility boundary |
|---|---|---|
| [Sodium 0.6.13](https://modrinth.com/mod/sodium) | Chunk renderer | **Ship.** Canonical renderer. Do not combine with VulkanMod. |
| [Sodium Extra 0.6.0](https://modrinth.com/mod/sodium-extra) | Renderer settings | **Ship.** No independent hot-path replacement. |
| [Reese's Sodium Options 1.8.3](https://modrinth.com/mod/reeses-sodium-options) | Sodium settings layout | **Ship, support.** The release metadata covers MC 1.21-1.21.5 and clears Sodium Extra's recommended dependency. |
| [ImmediatelyFast 1.6.10](https://modrinth.com/mod/immediatelyfast) | GUI/entity/immediate batching | **Ship.** Compatible with Sodium 0.6. |
| [Entity Culling 1.10.2](https://modrinth.com/mod/entityculling) | Asynchronous occlusion tests | **Ship.** Rendering only; Elarion mounts safelisted. |
| [More Culling 1.0.7](https://modrinth.com/mod/moreculling) | Face/leaf/weather/item-frame culling | **Ship.** Owns leaf culling; unknown modded blocks disabled. |
| [Enhanced Block Entities 0.10.2](https://modrinth.com/mod/ebe) | Baked block-entity models | **Ship.** Forced resource-pack compatibility. |
| [BadOptimizations 2.4.1](https://modrinth.com/mod/badoptimizations) | Targeted render/client hot paths | **Ship.** Release build; validate mixins on every pin change. |
| [Particle Core 0.3.3](https://modrinth.com/mod/particle-core) | Particle storage/render paths | **Ship.** Does not reduce player-visible particle counts. |
| [Dynamic FPS 3.11.4](https://modrinth.com/mod/dynamic-fps) | Inactive-client throttling | **Ship.** Only affects unfocused/invisible clients. |
| [FastQuit 3.0.0](https://modrinth.com/mod/fastquit) | Asynchronous integrated save/quit | **Ship.** Multiplayer server persistence is unaffected. |
| [Distant Horizons 3.2.0-b](https://modrinth.com/mod/distanthorizons/version/ZpKb4kZp) | LOD terrain rendering | **Ship, required beta.** Client-only; all distant generation and automatic installation are disabled. Replaces 2.3.0-b, which produced a confirmed per-frame OpenGL error and related memory risk. |
| [Bobby 5.2.4](https://modrinth.com/mod/bobby) | Full-chunk client cache | **Ship, required.** Bounded to 32; buggy Windows automatic cleanup is disabled and launcher maintenance owns explicit pruning. |
| [Nvidium](https://modrinth.com/mod/nvidium) | NVIDIA mesh shaders | **Audit.** NVIDIA-only and not a universal launcher baseline. |
| [VulkanMod](https://modrinth.com/mod/vulkanmod) | Replaces OpenGL renderer with Vulkan | **Reject baseline.** Replaces Sodium/render compatibility paths. |
| [Exordium](https://modrinth.com/mod/exordium) | Caches GUI/HUD rendering | **Audit.** Can make animated Elarion UI stale; requires screen-by-screen QA. |
| [Sodium Leaf Culling](https://modrinth.com/mod/sodiumleafculling) | Sodium-specific leaf culling | **Reject stack.** Overlaps More Culling. |
| [Cull Less Leaves](https://modrinth.com/mod/cull-less-leaves) | Selective leaf-face culling | **Reject stack.** Overlaps More Culling. |
| [Iris](https://modrinth.com/mod/iris) | Shader loader | **Optional future feature, not performance core.** Adds no benefit without shaders. |
| [ThreadTweak](https://modrinth.com/mod/threadtweak) | Thread count/priority tuning | **Audit.** Host- and CPU-dependent; can starve DH or server threads. |
| [Smooth Boot (Reloaded)](https://modrinth.com/mod/smooth-boot-reloaded) | Startup thread scheduling | **Audit.** Benefit is hardware/JVM dependent and overlaps ModernFix concerns. |

## Server-only

| Mod / source | Mechanism | Decision and compatibility boundary |
|---|---|---|
| [Alternate Current](https://modrinth.com/mod/alternate-current) | Reimplements redstone update scheduling | **Audit.** High value for redstone loads, but semantics need Elarion contraption QA. |
| [Noisium](https://modrinth.com/mod/noisium) | World-generation noise caching | **Audit only.** Project availability/maintenance is unstable; world parity must be proven. |
| [Structure Layout Optimizer](https://modrinth.com/mod/structure-layout-optimizer) | Structure template/layout caching | **Audit.** Promising bounded startup/worldgen gain; not needed without measurements. |
| [C2ME](https://modrinth.com/mod/c2me-fabric) | Parallel chunk IO/generation/ticking | **Reject stable core.** Fabric 1.21.1 line is alpha and expands concurrency risk. |
| [VMP](https://modrinth.com/mod/vmp-fabric) | Multiplayer tracking/network optimizations | **Reject stable core.** Described upstream as early development. |
| [Krypton](https://modrinth.com/mod/krypton) | Network stack optimization | **Reject stable core.** Upstream calls it work in progress without stability guarantees. |
| [ServerCore](https://modrinth.com/mod/servercore) | Server tick, mob, chunk, and command tuning | **Reject stable core.** 1.21.1 build is beta and several options alter gameplay. |
| [ScalableLux](https://modrinth.com/mod/scalablelux) | Parallel lighting engine | **Audit only.** Concurrency-heavy and must be tested against world generation. |
| [FastNoise](https://modrinth.com/mod/fastnoise) | Replaces/accelerates noise generation | **Reject stable core.** Beta; conflicts/overlaps Noisium and raises DH/world parity risk. |
| [Chunky](https://modrinth.com/plugin/chunky) | Offline world pregeneration | **Operations tool only.** Useful before release, not an always-on performance mod. |
| [Let Me Despawn](https://modrinth.com/mod/lmd) | Changes mob persistence/despawn rules | **Reject performance core.** Observable gameplay behavior change. |

## Both client and server

| Mod / source | Mechanism | Decision and compatibility boundary |
|---|---|---|
| [Lithium 0.15.3](https://modrinth.com/mod/lithium) | Game-logic and data-structure optimization | **Ship.** Semantics-preserving primary logic optimizer. |
| [FerriteCore 7.0.3](https://modrinth.com/mod/ferrite-core) | Deduplicates model/block-state memory | **Ship.** Low-risk memory reduction. |
| [ModernFix 5.25.1](https://modrinth.com/mod/modernfix) | Startup, memory, DFU/resource fixes | **Ship.** Keep default mixin set unless a verified conflict appears. |
| [Debugify](https://modrinth.com/mod/debugify) | Broad Mojang bug-fix mixin set | **Audit.** Correctness utility rather than guaranteed performance; mixin overlap gate required. |
| [Ksyxis](https://modrinth.com/mod/ksyxis) | Avoids synchronous spawn-chunk loading | **Audit.** Changes initial chunk-loading timing and needs restart/portal QA. |
| [Memory Leak Fix](https://modrinth.com/mod/memoryleakfix) | Patches known leak sites | **Audit.** Only adopt if its 1.21.1 patches remain relevant and do not overlap ModernFix. |

## Release gates

No additional candidate enters `distribution/mods.json` because a list or
modpack recommends it. Adoption requires:

1. exact Fabric 1.21.1 pin, official origin, license, SHA-512, and dependency
   closure;
2. clean dedicated-server startup, controlled shutdown, restart, and existing
   world persistence;
3. independent Client One/Client Two startup and join, with concurrent QA when
   the host's native GLFW limitation permits it;
4. Excalibured CIT/CTM/CEM/random/emissive/colormap, Rechiseled/Fusion,
   GeckoLib mounts, portals, UI, and managed-dimension visual checks;
5. Distant Horizons/Bobby travel, reconnect, dimension, cache-growth,
   shutdown, and recovery checks;
6. an A/B improvement in average FPS, 1% lows, frame spikes, heap, startup, or
   server tick time that is material for Elarion;
7. a two-hour soak with no new error, mixin failure, render corruption,
   gameplay-state divergence, unbounded disk growth, or shutdown regression.

“100% stability” cannot be truthfully guaranteed for third-party software.
The Elarion release claim is narrower: zero known issues after these gates.

The executed gate evidence is in
`docs/reports/PERFORMANCE_DISTRIBUTION_VALIDATION.md`. The performance stack
passed; eleven legacy Excalibured/Polytone colormap-target errors remain a
whole-client zero-error blocker.
