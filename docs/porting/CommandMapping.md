# Command Mapping

NeoForge feature: command registration through NeoForge/mod lifecycle hooks.

Fabric equivalent: Brigadier registration through Fabric command callbacks or Elarion command registry.

Minecraft classes: `CommandManager`, `ServerCommandSource`, Brigadier builders and arguments.

Fabric API classes: `CommandRegistrationCallback`.

Porting difficulty: Low.

Notes:

- Map server-owner/admin commands under `/e <system>` when possible.
- Use OP level 4 for admin systems.
- Add command docs and tests.

Example source locations:

- NeoForge: command usage in mod examples and event docs.
- Fabric: `external/fabric-api/fabric-command-api-v2`
- Elarion: `platform/core/.../command`, addon command packages.
