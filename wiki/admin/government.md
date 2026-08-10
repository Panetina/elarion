# Government

Admin guide for the active Government foundation.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Status

Government is implemented as a two-form founding system:

- Monarchy
- Republic

The removed Theocracy and Confederation designs are not active gameplay forms.

## Admin Commands

```text
/e government reload
/e government forms
/e government inspect <form>
/e government state <realm>
/e government gates <realm>
/e government audience <realm>
/e government audience inspect <realm> <record>
/e government laws <realm>
/e government law archive <realm> <law>
/e government law restore <realm> <law>
/e government set-form <realm> <form>
/e government identity set <realm> <tag> <display-name...>
/e government founding complete <realm>
/e government authority cleanup
/e government reset <realm>
/e test government reset [realm]
/e test government advance <realm>
/e government block remove
/e government office assign <realm> <office> <player>
/e government office remove <realm> <office> <player>
```

Authority chat:

```text
/lc <message>
```

## Forms And Offices

- `monarchy`: `monarch`, `heir`, `officer`
- `republic`: `president`, `officer`

Government temporarily grants the matching Core title while a player holds an
office and restores the previous active title when the office is removed.

## Civic Forum Flow

- Before Foundation I: Realm Name is visible but locked.
- After Foundation I: active Embers propose one name/tag, then vote.
- After name vote: Realm Color vote opens.
- After color vote: Government Form screen appears.
- After Foundation II: citizens vote for Monarchy or Republic.
- After Foundation III: founding elections open.
- After founding completion: citizen civic modules unlock.

Republic founding elects one President.

## Seat Of Rule

Seat of Rule unlocks after Foundation III and founding completion.

Full Seat access:

- Monarchy: Monarch
- Republic: President

Officers only receive `/lc` access in V1.

Seat modules:

- Audience, for Monarchy only
- Laws
- Projects
- Offices
- Archive
- Heraldry: active authority can save a validated 32x32 Realm emblem from a
  live Seat session. The emblem is never shown in chat.
- Taxes: Economy-owned Realm service categories. The Seat slider is bounded to
  0–25% in 0.25% steps, displays the treasury destination, includes the policy
  revision, and refreshes from Economy after a save or stale-write rejection.

## Audience And Laws

Citizen law/project/rule intake is not active in V1.

Monarchy:

- Citizens may request an audience.
- Monarch may accept/reject audience requests.
- Monarch may directly add, archive, and restore laws and project records.

Republic:

- President writes a law title/body and opens a Yes/No citizen vote.
- Citizens ratify or reject the law.
- There is no council and no citizen proposal intake for Republic laws.

## Reset And Test Timing

```text
/e test government advance realm1
/e government reset realm1
/e test government reset
/e test government reset realm1
```

`advance` ends proposal/nomination windows first, then voting windows after at
least one ballot exists. Reset clears Government identity/color/form/offices,
votes, audience/vote records, and civic records. It does not remove Civic
Forum or Seat of Rule blocks and does not touch Shrine progress, Portal routes, or NPC
placements.

## Verification

- `/e government forms` lists only Monarchy and Republic.
- `/e government inspect republic` shows President and Officer.
- Civic Forum offers only Monarchy/Republic at the Government Form stage.
- Seat of Rule opens only for the active ruler.
- Republic laws are created by the President and ratified by citizens.
- `/lc` reaches same-Realm authority holders.
