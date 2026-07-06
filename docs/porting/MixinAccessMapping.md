# Mixin And Access Mapping

NeoForge feature: access transformers and extra hooks.

Fabric equivalent: access wideners, accessor/invoker mixins, or normal mixins.

Minecraft classes: target Yarn-mapped classes and members.

Fabric API classes: Fabric Loader mixin support and access widener metadata.

Porting difficulty: High.

Notes:

- Prefer Fabric API events before mixins.
- Use access wideners only for narrow access gaps.
- Keep behavior in services and delegate from the mixin.
- Add comments/tests for fragile hooks.

Example source locations:

- NeoForge: access/hook code throughout `external/neoforge`.
- Fabric/Yarn: `external/yarn` for target names.
- Elarion: mixins under `addons/names`, `addons/titles`, `addons/realms`, and `addons/worlds`.
