# Permissions And Abilities

Purpose: gate admin commands and gameplay capabilities without creating addon-local permission managers.

Main classes: `AbilityService`, command registrars, `CitizenRecord`.

Entry points: Core command registration, ability commands, addon ability checks.

Commands: `/e ability ...`; OP level 4 gates `/e ...`.

Network packets: none central.

GUI/screens: future screens may show ability-gated actions.

Storage/persistence: citizen records and ability sets.

Dependencies: Core citizens, commands, server permission levels.

Related systems: NPC conditions, Government offices, Economy services, Offerings actions.

Extension points: Core abilities and registry conditions.

Risks: addon-local permission flags; OP bypasses hidden in service logic; missing tests for command gates.

Do not duplicate this system by creating: custom permission managers inside addons.
