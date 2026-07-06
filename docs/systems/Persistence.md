# Persistence System

Purpose: maintain durable configuration and runtime state with explicit ownership, validation, and bounded query paths.

Main classes: `JsonStateStorage`, `CoreConfigManager`, `CoreConfigValidator`, addon storage/config classes.

Entry points: server start/stop lifecycle and reload commands.

Commands: `/e reload` and addon reload commands.

Network packets: none directly.

GUI/screens: consume service snapshots, not files.

Storage/persistence: `config/elarion` for definitions; `world/elarion` for runtime.

Dependencies: Core config/storage helpers, Gson/YAML parsing, task service for batched history.

Related systems: every Core/addon feature.

Extension points: new addon config loaders, storage wrappers, validation tests.

Risks: config-as-state, unbounded JSONL scans, broad file parsing during gameplay, missing atomic writes.

Do not duplicate this system by creating: ad hoc file writers or gameplay code that parses config on interaction.
