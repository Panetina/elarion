# Fabulously Optimized 6.5.0 Adaptation

## Scope and conclusion

The inspected profile is
`C:\Users\Panyel\AppData\Roaming\ModrinthApp\profiles\Fabulously.Optimized-v6.5.0`.
Its 48 jars are a strong general OptiFine-parity baseline, but it is not the
best Elarion performance distribution unchanged. It includes visual parity,
accessibility, controller, LAN, screenshot, menu, and chat utilities that do
not improve frame time, while it lacks Elarion's required gameplay libraries,
Distant Horizons, Bobby, BadOptimizations, and Particle Core.

Elarion therefore uses FO as an audited input, not as an upstream runtime.
The canonical result is `distribution/mods.json`; no build or export depends
on the local FO profile.

## All inspected FO mods

`Keep` means the exact capability is in the Elarion distribution. `Replace`
means Elarion ships a different pinned version or configuration. `Exclude`
means the capability is not required for the stable performance baseline.

| Jar / source | What it does | Elarion decision |
|---|---|---|
| [Animatica 0.6.1](https://modrinth.com/mod/animatica) | Animated OptiFine resource textures | Exclude: the Elarion packs contain no dedicated Animatica assets. |
| [BetterGrassify 1.8.6](https://modrinth.com/mod/bettergrassify) | Better-grass visual parity | Exclude: cosmetic and not required by the packs. |
| [Better Mount HUD 1.2.4](https://modrinth.com/mod/better-mount-hud) | Mount HUD layout | Exclude: Elarion owns mount presentation. |
| [Capes 1.5.4](https://modrinth.com/mod/capes) | Third-party cape rendering | Exclude: cosmetic/network surface. |
| [CIT Resewn 1.2.2](https://modrinth.com/mod/cit-resewn) | Custom item textures | Keep: Excalibured contains 267 active Minecraft 1.21.1 CIT definitions, validated by `verifyExcaliburedCit`. |
| [Cloth Config 15.0.140](https://modrinth.com/mod/cloth-config) | Config UI/runtime library | Keep: required by RoadWeaver and client config screens. |
| [Continuity 3.0.0](https://modrinth.com/mod/continuity) | Connected textures | Keep: Excalibured contains 815 CTM entries. |
| [Controlify 3.0.0 LTS](https://modrinth.com/mod/controlify) | Controller support | Exclude: accessibility/QoL, not performance. |
| [Crash Assistant 1.11.9](https://modrinth.com/mod/crash-assistant) | Crash diagnostics UI | Exclude: development logs and support tooling remain authoritative. |
| [Cubes Without Borders 3.0.0](https://modrinth.com/mod/cubes-without-borders) | Borderless-window controls | Exclude: QoL only. |
| [Debugify 1.0](https://modrinth.com/mod/debugify) | Vanilla bug-fix mixins | Exclude from stable core: broad mixin surface; catalogued for QA. |
| [Dynamic FPS 3.11.4](https://modrinth.com/mod/dynamic-fps) | Reduces inactive-client use | Keep with non-destructive defaults. |
| [e4mc 6.1.1](https://modrinth.com/mod/e4mc) | Public tunnel for LAN worlds | Exclude: unnecessary network/attack surface. |
| [Enhanced Block Entities 0.10.2](https://modrinth.com/mod/ebe) | Faster block-entity models | Keep with forced resource-pack compatibility. |
| [Entity Model Features 3.2.4](https://modrinth.com/mod/entity-model-features) | OptiFine custom entity models | Keep: Excalibured contains 96 CEM definitions. |
| [Entity Texture Features 7.1](https://modrinth.com/mod/entitytexturefeatures) | Random/emissive entity textures | Keep: Excalibured contains 316 random-entity entries and emissives. |
| [Entity Culling 1.10.2](https://modrinth.com/mod/entityculling) | Occlusion-culls entities/block entities | Keep; tick culling is disabled and `elarion_mounts:mount` is safelisted. |
| [Fabric API 0.116.12](https://modrinth.com/mod/fabric-api) | Fabric runtime APIs | Replace with repository pin 0.116.13. |
| [Fabric Language Kotlin 1.13.12](https://modrinth.com/mod/fabric-language-kotlin) | Kotlin mod runtime | Keep for Particle Core/Fzzy Config. |
| [FabricSkyBoxes 0.7.4](https://modrinth.com/mod/fabricskyboxes) | Custom skyboxes | Exclude: no Excalibured OptiFine sky definitions. |
| [Fabrishot 1.14.1](https://modrinth.com/mod/fabrishot) | Large screenshots | Exclude: QoL and potentially high transient memory use. |
| [FastQuit 3.0.0](https://modrinth.com/mod/fastquit) | Asynchronous integrated-world shutdown | Keep with quiet notifications. |
| [FerriteCore 7.0.3](https://modrinth.com/mod/ferrite-core) | Reduces model/state memory | Keep on client and server. |
| [FSB Interop 1.4.0](https://modrinth.com/mod/fsb-interop) | FabricSkyBoxes/Nuit interop | Exclude with FabricSkyBoxes. |
| [ImmediatelyFast 1.6.10](https://modrinth.com/mod/immediatelyfast) | Batches immediate-mode rendering | Keep. |
| [Iris 1.8.8](https://modrinth.com/mod/iris) | Shader-pack support | Exclude from required baseline: no shader pack ships and shaders cost performance. |
| [LambDynamicLights 4.8.10](https://modrinth.com/mod/lambdynamiclights) | Client dynamic lighting and Underworld spectral light API | Keep client-only: official 1.21.1 release, Iris-compatible fancy mode, culled rebuild scheduling, adaptive ticking, and bounded 6/15 spectral-player luminance. Incompatible Sodium Dynamic Lights and RyoamicLights remain excluded. |
| [Language Reload 1.7.6](https://modrinth.com/mod/language-reload) | Faster/multiple language reload | Exclude: QoL only. |
| [Lithium 0.15.3](https://modrinth.com/mod/lithium) | Semantics-preserving game-logic optimization | Keep on client and server. |
| [Main Menu Credits 1.2.0](https://modrinth.com/mod/main-menu-credits) | Pack credits menu | Exclude: FO branding, not Elarion runtime. |
| [MixinTrace 1.1.1](https://modrinth.com/mod/mixintrace) | Adds mixin detail to stack traces | Exclude from player distribution; useful only for diagnosis. |
| [Model Gap Fix 1.6](https://modrinth.com/mod/modelfix) | Removes item/model pixel gaps | Keep for pack visual correctness. |
| [ModernFix 5.25.1](https://modrinth.com/mod/modernfix) | Startup, memory, and resource fixes | Keep on client and server. |
| [Mod Menu 11.0.4](https://modrinth.com/mod/modmenu) | Mod/config list UI | Keep on clients. |
| [More Chat History 1.3.1](https://modrinth.com/mod/morechathistory) | Longer chat history | Exclude: QoL only. |
| [More Culling 1.0.7](https://modrinth.com/mod/moreculling) | Additional block/leaf/weather culling | Keep; unknown modded blocks default to no culling. |
| [No Chat Reports 2.9.1](https://modrinth.com/mod/no-chat-reports) | Chat signing/reporting changes | Exclude: behavior/security policy, not performance. |
| [OptiGUI 2.3 beta](https://modrinth.com/mod/optigui) | Resource-pack GUI textures | Exclude: Excalibured has no OptiFine GUI definitions. |
| [Paginated Advancements 2.5.1](https://modrinth.com/mod/paginated-advancements) | Advancement screen layout | Exclude: UI QoL only. |
| [Polytone 3.7.1](https://modrinth.com/mod/polytone) | Resource-pack colormaps/properties | Keep: Excalibured has 13 colormap groups. |
| [Puzzle 2.3.0](https://modrinth.com/mod/puzzle) | FO option/branding integration | Exclude: FO-specific support. |
| [Reese's Sodium Options 1.8.3](https://modrinth.com/mod/reeses-sodium-options) | Sodium options layout | Keep: its metadata supports MC 1.21-1.21.5 and it satisfies Sodium Extra's explicit recommendation. |
| [RRLS 5.0.10](https://modrinth.com/mod/rrls) | Resource reload presentation | Exclude: hides reload duration rather than improving Elarion runtime. |
| [Sodium Extra 0.6.0](https://modrinth.com/mod/sodium-extra) | Extra renderer controls | Keep. |
| [Sodium 0.6.13](https://modrinth.com/mod/sodium) | Renderer replacement/optimization | Keep. |
| [YACL 3.8.2](https://modrinth.com/mod/yacl) | Config UI library | Exclude: no shipped mod requires it. |
| [YOSBR 0.1.2](https://modrinth.com/mod/yosbr) | First-install defaults without overwrites | Keep. |
| [Zoomify 2.15.2](https://modrinth.com/mod/zoomify) | Zoom control | Exclude: QoL only. |

## FO configuration audit and Elarion adaptation

FO stores branding and active global settings at `config/`, while most pack
defaults are under `config/yosbr/`. Its useful performance choices were fast
weather, simulation distance 6, VSync off, background GC, quiet FastQuit,
leaf/weather/item-frame culling, and restrained optional lighting/shaders.

Elarion does not copy FO branding, translations, controls, keybinds, chat
policy, shader keys, resource-pack names, or unrelated QoL configs. It ships:

- non-destructive YOSBR defaults: render distance 8, simulation distance 6,
  biome blend 0, entity distance 75%, fast weather, VSync off, and 260 FPS cap;
- managed More Culling safety: modded blocks are not culled until explicitly
  proven compatible;
- managed Entity Culling safety: render culling only, no tick culling, and the
  Elarion GeckoLib mount entity is safelisted;
- managed EBE safety: resource-pack compatibility is forced;
- managed ModernFix safety: its overlapping Paper chunk and biome-temperature
  cache patches are disabled so Lithium alone owns those implementations;
- managed Distant Horizons safety: 128 LOD chunks, two throttled threads,
  no distant/server generation, no automatic installation, biome blend 0,
  single-pass fade; DH 3.2.0-b replaces the broken 2.3.0-b renderer;
- conservative Bobby defaults: 32-chunk maximum, no single-player
  server-distance override, no cached block entities, and a pre-created cache
  root. Automatic cleanup is disabled because Bobby 5.2.4 repeatedly fails on
  its own non-empty Windows `last_access` directories; launcher maintenance
  owns explicit cache pruning.

Managed safety files are launcher `always-replace` entries. Performance and
visual preferences are `if-absent` defaults, so updates do not reset player
choices. IntelliJ/Loom runs enforce safety files and only seed preferences
that are missing.

## Replacement decisions

- Enhanced Block Entities remains preferred over the newer Optimized Block
  Entities because EBE has the longer 1.21.1 compatibility record and an
  explicit pack-compatibility mode.
- More Culling owns leaf culling; Sodium Leaf Culling and Cull Less Leaves are
  not stacked over it.
- Iris, Nvidium, VulkanMod, and Exordium are not required because alternate or
  extra rendering paths expand incompatibility and QA scope.
- Distant Horizons is the only required beta. The beta risk is contained by
  disabling all distant generation and automatic update installation. Version
  3.2.0-b fixes the reproducible per-frame OpenGL error and ByteBuffer leak in
  the original 2.3.0-b pin, but must still pass the runtime release gates.

Runtime results, restart evidence, the final Generational ZGC policy, and the
controlled server A/B are recorded in
`docs/reports/PERFORMANCE_DISTRIBUTION_VALIDATION.md`.
