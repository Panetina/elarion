# Reference Setup Report

Generated for local Fabric 1.21.1 development and NeoForge 1.21.1 porting research.

## Repositories Cloned

| Purpose | Path | Remote | Branch | Commit |
|---|---|---|---|---|
| Fabric API | `external/fabric-api` | `https://github.com/FabricMC/fabric-api` | `26.2` | `5eb838916` |
| Fabric Loom | `external/fabric-loom` | `https://github.com/FabricMC/fabric-loom` | `dev/1.17` | `1ddd6fbb` |
| Yarn mappings | `external/yarn` | `https://github.com/FabricMC/yarn` | `1.21.11` | `ee9859089` |
| Fabric example mod | `external/example-mods/fabric-example-mod` | `https://github.com/FabricMC/fabric-example-mod` | `26.1.2` | `a900c76` |
| NeoForge | `external/neoforge` | `https://github.com/neoforged/NeoForge.git` | `1.21.1` | `89dbba3a0` |

## Branch Notes

- NeoForge is on the requested `1.21.1` branch.
- Fabric API default checkout landed on `26.2`, not an explicit Minecraft `1.21.1` branch.
- Fabric example mod default checkout landed on `26.1.2`, not an explicit Minecraft `1.21.1` branch.
- Yarn default checkout landed on `1.21.11`, not `1.21.1`.
- Fabric Loom default checkout landed on `dev/1.17`. Loom is Gradle-plugin reference material, not gameplay API reference.

## Missing Dependencies

No repository clone failed. No dependency resolution was run for the external repositories.

Expected local prerequisites for Elarion development remain:

- Java 21.
- Gradle wrapper from this repository.
- Network access for Gradle dependency resolution.
- Fabric Loom/Fabric API versions declared by Elarion's own Gradle files.

## Unresolved Version Issues

- The local Fabric reference repositories are not all pinned to Minecraft 1.21.1 branches. Treat them as API-pattern references unless checked out to matching branches/tags later.
- Elarion itself remains the authoritative source for exact Fabric 1.21.1 dependency versions.
- NeoForge should remain reference-only. Do not add NeoForge dependencies to Elarion unless a separate port branch/project is explicitly created.

## Recommended Use

- Use `docs/fabric-reference/FABRIC_OVERVIEW.md` for Fabric-first implementation patterns.
- Use `docs/neoforge-reference/NEOFORGE_OVERVIEW.md` to read NeoForge concepts without adopting NeoForge architecture.
- Use `docs/porting/NEOFORGE_TO_FABRIC.md` when translating NeoForge mod code into Fabric/Elarion systems.
- Use `docs/architecture/PROJECT_STRUCTURE.md` before adding new Elarion modules, commands, networking, UI, or storage.
