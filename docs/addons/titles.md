# Titles Addon

Status: Implemented.

## Purpose

`addons/titles` owns title rendering hooks and client presentation around
Core-owned title state.

## Main Source

- `addons/titles/src/main/java/panetina/elarion/addons/titles/`

## Ownership

Core owns title definitions, progression, and canonical title assignment.
Titles addon renders that state.

Core publishes Personal notifications when a title is granted or revoked.
`ONE_PER_PLAYER` and `GLOBALLY_UNIQUE` successful grants also publish World
announcements to citizens whose Realms have global notification access.
`UNLIMITED` grants remain Personal-only. The default Aquatic title is
`ONE_PER_PLAYER`.

Core emits `title-granted` and `title-revoked` domain events after successful
authoritative changes. These events are integration signals for future
Chronicle, newspaper, NPC rumor, website, and diagnostics consumers; they do
not automatically create notifications.
Grants whose ownership mode is `ONE_PER_PLAYER` or `GLOBALLY_UNIQUE` also
publish a World announcement to citizens of globally connected Realms.
Unlimited titles and title revocations remain Personal-only. Selecting an
already-owned active title does not create notification noise.

## Rendering

The client renderer reads active-title presentation from Core
`IdentitySyncPayload`. The payload includes the configured title ARGB color, so
titles such as Monarch, Heir, President, Officer, and progression
titles render above the player's head with the same project-wide color used by
Character Menu title rows and previews. Do not hard-code rank/title colors in
the Titles addon renderer; update Core title definitions/config if a title
color should change.
