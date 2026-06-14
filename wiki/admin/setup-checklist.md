# Setup Checklist

Manual checks to run after server-facing changes.

[Home](../README.md) | [Admin](README.md) | [Commands](commands.md)

## Build And Startup

- Run `.\gradlew.bat build`.
- Start the dev server with `.\gradlew.bat :dev:runServer`.
- Confirm Core and all enabled addons initialize without config errors.
- Check `dev/run/logs/latest.log` for startup exceptions and repeated warnings.

## In-Game Smoke Tests

- Join with a client using the current modpack.
- Verify `/e reload` completes.
- Verify `/e economy pulse`, `/e perf status`, and `/e security status`.
- Verify core chat commands: `/rc`, `/ac`, `/pm`, `/r`, `/w`, `/yell`.

## Priority Manual Tests

- [Portals](portals.md): geometry, tickets, return entitlements, Iris rendering.
- [Offerings](offerings.md): item/currency offerings, rewards, donation history.
- [NPCs](npcs.md): placement, skin/portrait, dialogue, banker actions.
- [Performance](performance.md): queue status, hotzones, TPS-drop workflow.

## Source-Backed Notes

- Build command reference: [../../CODEX.md](../../CODEX.md)
- Current TODO priorities: [../../TODO.md](../../TODO.md)
