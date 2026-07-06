# Quests

Admin guide for generic data-driven quest packages and runtime quest state.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

`Generic package foundation`, `Authoring needed`

Quests owns quest package definitions, scoped quest state, actor bindings,
quest actions, quest conditions, delayed consequences, Quest notifications, and
optional Shrine display-name projections.

Definitions:

```text
config/elarion/addons/quests/questlines/<quest-id>/
config/elarion/addons/quests/questlines/<quest-id>.yml
```

Runtime state:

```text
world/elarion/addon-state/quests/state.json
```

The Admin Panel Systems tab exposes the loaded `quests` config domain as a
read-only package and graph summary. Configuration is still edited in the
questline YAML files and applied with `/e quest reload`.

## Package Shape

New questlines should use a folder package:

```text
quest.yml
actors.yml
variables.yml
stages.yml
evidence.yml
endings.yml
conditions.yml
consequences.yml
authoring.yml
```

The addon creates the `questlines/` directory only. It does not generate any
lore quest by default.

## Admin Commands

```text
/e quest reload
/e quest list
/e quest inspect <quest>
/e quest state <quest> [scope-key]
/e quest reset <quest> <scope-key>
/e quest bind actor <quest> <scope-key> <actor> <npcIdOrHandle>
/e quest unbind actor <quest> <scope-key> <actor>
/e quest bindings <quest> <scope-key>
/e quest validate <quest|all>
```

Scope keys:

```text
realm:realm1
global
world:minecraft:overworld
player:<uuid>
```

Use `reset` for a single questline scope when testing from zero:

```text
/e quest reset generic_foundation realm:realm1
```

This clears that questline state, player quest records, actor bindings, and
pending quest consequences. It does not reset Shrine progress, NPC placements,
Government state, or Core character state.

## Actor Binding

Quest packages define stable actor aliases such as `guide`, `smith`, or
`witness`. Bind those aliases to live placed NPCs per scope:

```text
/e npc place worldheart_banker here
/e quest bind actor generic_foundation realm:realm1 guide worldheart_banker_1
/e quest bindings generic_foundation realm:realm1
```

The binding stores the placed NPC UUID and handle snapshot in quest runtime
state. NPCs remain the owner of placement records and repair/reconcile logic.

## Authoring Notes

NPC dialogue should drive quest state with registered actions and conditions.
NPCs should not store quest progress.

Example action:

```yaml
actions:
  - type: "elarion_quests:set_stage"
    parameters:
      quest: "generic_foundation"
      realm: "realm1"
      stage: "second_step"
```

Example memory-sensitive variant:

```yaml
variants:
  - text: "You remember what I asked."
    conditions:
      - type: "elarion_quests:variable_at_least"
        parameters:
          quest: "generic_foundation"
          realm: "realm1"
          variable: "trust"
          minimum: "1"
```

Example Shrine display projection:

```yaml
actions:
  - type: "elarion_quests:set_shrine_display"
    parameters:
      quest: "generic_foundation"
      instance: "offering_realm_realm1_1"
      title: "Sorina's Stone"
```

Lore questlines such as `red_thread_beneath_foundation` should use content
files and registered quest actions for Shrine names, evidence, endings, and
dialogue. Do not add story-specific names to generic system defaults.

## Verification

- Run `/e quest reload`.
- Run `/e quest list`.
- Run `/e quest inspect <quest>`.
- Bind an actor with `/e quest bind actor ...`.
- Trigger a dialogue action or registry action that starts the quest.
- Run `/e quest state <quest> <scope-key>`.
- Reset with `/e quest reset <quest> <scope-key>`.

## Source-Backed Notes

- Addon docs: [../../docs/addons/quests.md](../../docs/addons/quests.md)
- Commands: [../../addons/quests/src/main/java/panetina/elarion/addons/quests/command/QuestCommands.java](../../addons/quests/src/main/java/panetina/elarion/addons/quests/command/QuestCommands.java)
- Registry handlers: [../../addons/quests/src/main/java/panetina/elarion/addons/quests/service/QuestRegistryHandlers.java](../../addons/quests/src/main/java/panetina/elarion/addons/quests/service/QuestRegistryHandlers.java)
- Runtime state: [../../addons/quests/src/main/java/panetina/elarion/addons/quests/service/QuestStateService.java](../../addons/quests/src/main/java/panetina/elarion/addons/quests/service/QuestStateService.java)
