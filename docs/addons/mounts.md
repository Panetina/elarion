# Mounts Addon

Last reviewed: 2026-07-05

`addons/mounts` owns the native Fabric mount foundation for Elarion.

## Status

Implemented V5 collection foundation, focused tests passing.

## Ownership

Mounts owns:

- registered entity type `elarion_mounts:mount`
- deprecated legacy whistle item ids/icons
- `Elarion Mounts` creative tab for legacy icon references
- converted geo, animation, and texture assets for all seven V1 mounts
- tinted horn whistle item icons
- native Fabric mount movement, ownership, dismiss, and rendering behavior
- GeckoLib mesh, UV, and animation rendering for converted geo assets
- active summoned mount sessions for disconnect/restart/teleport recovery
- persistent per-player mount unlock and active-mount state
- the Mounts tab in Core's modular Collection menu
- fixed Realm vendor assignment for Airship, Hot Air Balloon, and Ghast

Mounts does not own:

- MythicMobs, MCPets, ModelEngine, or any plugin runtime
- Core citizens, permissions, titles, or Realm identity
- Economy pricing or NPC sales
- NPC sales, stables, broader pet/collection ownership, or market logic

## Source

- `../../addons/mounts/src/main/java/panetina/elarion/addons/mounts/`
- `../../addons/mounts/src/main/resources/assets/elarion_mounts/geo/`
- `../../addons/mounts/src/main/resources/assets/elarion_mounts/animations/`
- `../../addons/mounts/src/main/resources/assets/elarion_mounts/textures/entity/`
- `../../addons/mounts/src/main/resources/assets/elarion_mounts/textures/item/`

Runtime state:

- `world/elarion/addon-state/mounts/sessions.json`
- `world/elarion/addon-state/mounts/collection.json`

## Collection Menu And Active Mount

Core owns the modular Collection menu shell. Mounts contributes the first tab,
`Mounts`, through `ElarionApi.system().collections()`.

Players open the menu with the `C` key or `/collection`; pressing `C` while it
is already open closes it. The Collection menu uses top tabs and includes a
placeholder `Pets` tab. The Mounts tab lists all seven V1 mounts with a whistle
placeholder icon, name, compact obtain text, muted locked styling, a selected
detail panel, a client-only animated mount model preview for unlocked entries,
and a server-authoritative `Set as active` action.
The landing view shows six balanced rows with equal top/bottom padding and
hidden overflow scrolling for the seventh or future entries. Mount unlocks are
locked by default and are granted by admin/dev commands or future progression/NPC vendor logic.
Pressing `R` while unmounted asks the server to summon or remount the player's
active unlocked mount; pressing `R` while riding an Elarion mount dismisses it.

Editable Collection text lives in:

```text
config/elarion/addons/mounts/collection.yml
```

Each mount entry can define `locked-row`, `unlocked-row`, `locked-detail`, and
`unlocked-detail`. `{realm}` is replaced with the assigned Realm id for Realm
vendor mounts. Future mount or pet collection providers should use the same
config-driven text pattern instead of hard-coding lore in UI code.

`MountConfigDescriptors` registers the read-only `mounts` config domain using
the same loaded `MountCollectionTextConfig` snapshot used by Collection
rendering. It exposes four text fields for each of the seven registered mount
types. The addon has no live config reload command, so every descriptor is
restart-required and non-runtime-reloadable. Admin discovery does not expose
or mutate unlocks, active mounts, sessions, or Collection actions.

The selected-entry preview is implemented through Core's generic
`ElarionCollectionPreviewRegistry` and reusable
`ElarionMenuEntityPreviewRenderer`. Mounts registers a client-only provider
that creates cached local preview entities and renders only the selected
unlocked mount in the Collection detail frame at an angled idle pose. Preview
scale plus horizontal and vertical centering are derived from each converted
geo model's visual bounds, render scale, and passenger anchor so long or tall
models fit the same detail frame without hand-tuned per-frame allocations.
Chinese Dragon and Sci-Fi Bike also have small Collection-only art calibration
offsets because their final rendered Gecko mesh sits left of the simplified
geo bounds. Wyvern has a small Collection-only zoom boost so it reads better
inside the shared square. The Collection preview uses a slower client-only
animation time scale than live mounts so idle motion reads calmly in the menu.
The cache is bounded to one local entity per mount type and cleared when the
client world changes. These preview entities are not spawned on the server, are
not added to sessions, and are not persisted.

Admin commands:

```text
/e mounts grant <player> <type>
/e mounts revoke <player> <type>
/e mounts set-active <player> <type>
/e mounts list <player>
```

Valid mount types are `airship`, `bee`, `chinese_dragon`, `ghast`,
`hot_air_balloon`, `scifi_bike`, and `wyvern`.

Realm vendor placeholders are fixed and fair:

- `realm1` -> Airship
- `realm2` -> Hot Air Balloon
- `realm3` -> Ghast

Those three Realm vendor mounts use identical movement profiles for speed,
acceleration, boost, turn response, vertical motion, drag, and terrain
clearance. Their visuals, camera profiles, seats, and animation assets remain
unique.

Collection icons currently reuse the legacy whistle textures as placeholders
until final mount portrait art is chosen. The UI treats those as presentation
assets only; summon behavior still goes through the active mount collection
state and the `R` action.

## Legacy Items

The creative tab still exposes one legacy whistle icon per converted mount for
asset/id continuity:

- `elarion_mounts:airship_whistle`
- `elarion_mounts:bee_whistle`
- `elarion_mounts:chinese_dragon_whistle`
- `elarion_mounts:ghast_whistle`
- `elarion_mounts:hot_air_balloon_whistle`
- `elarion_mounts:scifi_bike_whistle`
- `elarion_mounts:wyvern_whistle`

Legacy whistle icons are recolored vanilla horn silhouettes and currently also
serve as temporary Collection placeholders:

- Airship: wood brown `#906040`
- Bee: golden yellow `#E0B030`
- Chinese Dragon: crimson `#C04020`
- Ghast: spectral blue `#4090E0`
- Hot Air Balloon: dark red `#902010`
- Sci-Fi Bike: dark teal `#304050`
- Wyvern: amber orange `#E09030`

Whistles are deprecated and no longer perform normal gameplay summoning. Use
the Collection menu and `R` active-mount action instead.

## Entity Behavior

The V4 mount entity is a native Fabric vehicle-style entity:

- flying/no gravity
- silent
- invulnerable
- non-pushing/non-colliding
- one rider
- owner-only remount and dismiss by default
- no idle cleanup timer while the mount is active
- explicit owner dismount/dismiss removes the active mount
- one active summoned mount session is persisted per owner
- when a mounted owner disconnects, the loaded entity is parked; reconnecting
  restores/remounts the session
- on world change or same-world teleport dismounts, the service performs a
  delayed bounded restore near the player and removes nearby stale owned mounts
- intentional dismiss clears the session

All seven mounts use the same control contract:

- forward accelerates toward that mount's flight cap
- backward brakes and can reverse
- no forward/back input eases speed toward zero
- mouse yaw turns the mount gradually and applies lean animation state from
  mouse-turn intent only after a deadzone
- yaw steering is smoothed from the client look direction to avoid camera/model
  feedback jitter
- jump ascends
- sneak descends
- sprint/Control boosts forward flight while held
- `R` performs an explicit dismiss while mounted, or summons/remounts the active
  unlocked mount while unmounted
- sneak-right-click while unmounted and grounded dismisses/removes an owned
  mount

Most mounts have a separate movement profile:

- Bee: small, nimble, fast turning
- Chinese Dragon: large and smooth
- Sci-Fi Bike: fastest compact mount
- Wyvern: fast agile flying creature
- Airship, Ghast, and Hot Air Balloon: equal Realm vendor stats for fairness

Each movement profile also owns an explicit boost multiplier. Boost is not a
global speed hack; it is per-mount tuning so heavy mounts can feel powerful
without becoming twitchy, and fast mounts can feel thrilling without ignoring
their role.

All mounts enforce a three-block terrain-clearance floor while flying, matching
the original Chinese Dragon safety rule. This keeps large animated bodies from
visually sinking into terrain and avoids small mounts clipping into hills while
descending.

The six non-Chinese converted mounts render at model-true Blockbench scale
(`1 Blockbench pixel = 1/16 block`). Every mount uses a typed rider-seat
profile that separates the stable server/camera seat from the visible rider
correction. The server/camera seat stays stable for the chase camera and does
not have to match the animated visual rider exactly. The visual rider
calibration remains attached to `p_passenger` for third-person presentation.
The visible third-person rider is redrawn from the animated GeckoLib
`p_passenger` bone with per-mount local calibration; `p_passenger` is treated
as the authored seat, not as a rough marker that receives a large generic
offset. The fake rider uses direct mount-local forward orientation after the
statue renderer's coordinate correction; no global 180-degree yaw flip is
applied. It is rendered as a mount-owned statue by drawing the player model
directly with a fixed seated pose. The normal
`PlayerEntityRenderer.render(...)` path is intentionally not used for the
statue body because it consumes live player yaw, pitch, limb, and interpolation
state. Compatible player feature layers are still rendered from the frozen
model so skin, armor, and held items stay visible without letting player mouse
movement move the rider independently. Name/title labels are invoked
separately from the same player renderer so identity integrations remain
visible without using the live body render path.
During the fake-rider pass, the player's render yaw, pitch, body yaw, and head
yaw are snapshotted, frozen, and restored immediately so feature renderers
cannot leak live mouse movement back into the mount-owned statue.

Chinese Dragon keeps its manually verified larger scale and dedicated
third-person presentation tuning. Its visible rider calibration is lowered by
six Minecraft pixels from the previous seat pass so the rider touches the
animated mount more closely.

Mount riding remembers the player's pre-mount F5 perspective, forces rear
third-person chase while mounted, and restores the saved first-person,
rear-third-person, or front-third-person perspective after dismount. First
person and front-third-person are intentionally unavailable during V4 riding
because large mount models block too much view and require a dedicated camera
experience.

The mounted camera is a tuned chase camera:

- vanilla collision clipping remains active through the normal camera path
- per-mount third-person distance lives in `CameraProfile`
- boost smoothly increases follow distance and FOV
- mounted FOV uses a stable mount-time baseline plus smoothed boost; it does
  not reuse vanilla's per-frame dynamic FOV while riding, so Space/vertical
  flight cannot create zoom pulsing
- boost strength interpolates client-side so pressing/releasing Control does
  not snap the camera
- camera tuning is separate from rider-seat tuning; changing camera feel must
  not move the visible third-person rider

The client sends compact held-input snapshots to the server while riding.
Custom mount input is the only movement input source for Elarion mounts;
server movement does not fall back to vanilla `forwardSpeed`,
`sidewaysSpeed`, horse jump, or sneak dismount state. The same small flight
controller runs client-side for local prediction and server-side for
authoritative movement, so the local chase camera follows smooth predicted
motion instead of waiting for server correction packets. The client sends the
full held key state every tick, including explicit released/zero states, and
the controller eases speed and vertical motion down normally after release.
Elarion mounts suppress vanilla mounted jump/sneak handling during the vanilla
movement packet path, while still reading Space and Shift directly for the
custom Elarion packet. This keeps Space as ascend and Shift as descend without
horse-jump charge or camera/FOV tremble. Common and client mixins block vanilla
sneak dismount while riding so Shift remains descent-only.
Active summon/dismiss is available through the Elarion Mounts key, bound to `R`
by default.

## Rendering And Animation

Mounts ship only runtime geo, animation, and texture assets. `.bbmodel`,
ModelEngine, and plugin-pack folders are external authoring/reference material
and should not be copied into mod resources.

Runtime files:

- `mount_airship.geo.json`, `mount_airship.animation.json`, `flight_airship.png`
- `mount_bee.geo.json`, `mount_bee.animation.json`, `flight_bee.png`
- `mount_chinesedragon.geo.json`, `mount_chinesedragon.animation.json`,
  `flight_chinesedragon_body.png`
- `mount_ghast.geo.json`, `mount_ghast.animation.json`, `flight_ghast.png`
- `mount_hotairballoon.geo.json`, `mount_hotairballoon.animation.json`,
  `flight_hotairballoon.png`
- `mount_scifibike.geo.json`, `mount_scifibike.animation.json`,
  `flight_scifibike.png`
- `mount_wyvern.geo.json`, `mount_wyvern.animation.json`, `flight_wyvern.png`

The Mounts renderer:

- resolves model, animation, and texture assets from `ElarionMountType`
- uses GeckoLib for mesh, UV, and animation rendering from converted geo assets
- ignores utility bones such as hitboxes, shadows, passenger anchors, and
  helper bones through the Gecko model layer
- plays included animations such as `spawn`, `idle`, `walk`, `lean_left`,
  `lean_right`, `ascend`, and `descend`
- smooths raw forward, turn, vertical, and boost input before movement or
  animation consumes it, so one missed packet does not restart or cancel a
  mount pose
- uses GeckoLib for mesh/bone ownership, then Elarion applies a deterministic
  Blockbench-style blended pose to the final Gecko bones before render
- keeps base locomotion and overlays independent: `idle`/`walk` are blended as
  the base pose, while `lean_left`/`lean_right` and `ascend`/`descend` fade in
  and out as weighted overlays
- does not use competing Gecko overlay controllers for locomotion, because
  those controllers can overwrite each other on shared body bones and create
  snaps that Blockbench preview does not show
- keeps animation timelines continuous so adding or removing lean/vertical
  input changes only overlay weight, not the base `idle`/`walk` clock
- allows combined visual states such as walk plus lean, walk plus ascend, and
  walk plus lean plus ascend/descend without one layer cancelling another
- uses each model's `p_passenger` marker as the visual seat origin
- redraws the visible rider from a Gecko render layer attached to the animated
  `p_passenger` bone, so rider placement follows Gecko's final model pose
- renders the rider as a frozen mount-owned player statue through direct
  `PlayerEntityModel` rendering, preserving player skin, armor, and held items
  while removing independent mouse yaw, pitch, walking, biped model-part
  rotation, full entity-renderer interpolation, and hand-swing motion
- uses vanilla horse-riding limb angles for the frozen statue so the rider
  reads as seated without reintroducing live player animation
- invokes player name/title label rendering separately after the static body
  draw so nickname, title, leader marker, and Realm color integrations remain
  visible
- applies mount-specific X/Y/Z rider calibration in `RiderSeatProfile`; these
  values are visual seating data and should not be replaced with generic math
- uses separate server/camera seat offsets in `RiderSeatProfile` so chase
  camera tuning can change without moving the third-person rider model
- suppresses the stale vanilla rider render and redraws the rider from the
  mount renderer at the stable seat
- hides this fake rider render in first-person view
- uses the custom model parser only for tests, static seat anchors, diagnostics,
  and bounds checks, not for final mesh/UV rendering or animated rider placement
- does not require ModelEngine, MythicMobs, or MCPets at runtime; GeckoLib is
  the required converted-geo rendering dependency

This is a Fabric reinterpretation of the plugin behavior, not direct plugin
execution.

## Renderer History

The first native renderer used Elarion's custom geo parser for mesh, UVs,
animation sampling, and rider placement. That path made the Chinese Dragon
rider behavior easier to tune and rendered zero-thickness planes as single
faces, which reduced flicker, but it did not interpret every converted Gecko
UV exactly and produced broken Wyvern wing textures.

The GeckoLib renderer fixed mesh, UV, and animation playback for converted
assets, but the first Gecko pass still positioned the fake rider with the old
custom parser. That split made the rider drift because Gecko and the parser did
not produce the same final animated `p_passenger` transform.

Current rule: GeckoLib owns the mount runtime bones and mesh, while Elarion owns
the final locomotion pose blend. The visible rider must attach through Gecko's
animated `p_passenger` bone after Elarion applies the blended pose, and the
rider-seat profile owns only small camera/visual corrections. Rider placement,
seat offsets, and camera tuning are independent from animation tuning and must
not be changed just to adjust mount body motion. The rider body is not rendered
through the full live player renderer; it is a direct, static player-model draw
attached to the mount. Runtime geo assets must not ship exact zero-thickness
visual cubes; thin visual planes are expanded by a tiny centered thickness to
avoid z-fighting while preserving the authored shape.

## Events And Notifications

Mounts currently emits no Core domain events and publishes no notifications.
Ordinary collection browsing, active selection, summoning, riding, and
dismissing are routine actions and should stay silent.

Future meaningful lifecycle events may include mount unlocks, ownership
changes, stable purchases, rare mount discoveries, or death/escape events if
mounts become persistent gameplay entities.

## Verification

Status: complete for V5 collection foundation.

Manual verification previously completed for all seven V1 mount entities:
legacy whistle icons, summon/session behavior, converted GeckoLib
meshes/textures/animations, movement profiles, Chinese Dragon tuning
preservation, rider placement, third-person camera behavior, boost camera/FOV
feedback, reconnect/restart and portal/dimension/same-world teleport session
recovery, and multiplayer motion presentation.

Run:

```text
.\gradlew.bat :addons:mounts:compileJava
.\gradlew.bat :addons:mounts:test
.\gradlew.bat build
```

Test commands:

```text
/e test mounts summon airship
/e test mounts summon bee
/e test mounts summon chinese_dragon
/e test mounts summon ghast
/e test mounts summon hot_air_balloon
/e test mounts summon scifi_bike
/e test mounts summon wyvern
/e test mounts debug
/e test mounts clear-nearby
```

Manual checks:

- `C` opens the Collection menu
- pressing `C` again closes the Collection menu
- top tabs show `Mounts` and placeholder `Pets`
- Mounts tab has six balanced visible rows, hidden scroll for overflow, and no cut-off row
- unlocked selected mounts render an angled animated model preview in the detail panel
- Mount rows use whistle placeholder icons until final portrait art exists
- locked mounts cannot be set active
- `/e mounts grant <player> <type>` unlocks a mount
- `Set as active` updates the active mount
- `R` summons/remounts the active unlocked mount
- `R` dismisses while mounted
- creative tab contains all seven legacy whistle icons
- every whistle has a distinct recolored horn icon
- legacy whistles show the deprecation message instead of summoning
- each mount renders with its converted geo model and texture
- each mount uses its own movement profile
- owner can ride only their own mount
- non-owner cannot ride or dismiss
- forward/back controls work continuously
- mouse yaw turns gradually, ignores tiny lean nudges, and leans past the
  configured turn deadzone
- jump ascends
- sneak descends
- Control/sprint boosts forward speed, smoothly zooms out the chase camera,
  and adds a small FOV kick from local held input
- `R` dismisses/despawns the mount
- spawn, idle, movement, lean, ascend, and descend animations visibly change
- riding survives reconnect
- riding survives server restart
- riding survives portal/dimension travel
- riding survives same-world `/tp`
- other players see smooth enough mount motion
