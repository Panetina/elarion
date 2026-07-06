# Upstream Reference

The local directory `reference/upstream-starcatcher-neoforge-1.21.1/` is an
ignored checkout of:

- Repository: <https://github.com/wdiscute/starcatcher>
- Branch: `2.4-neoforge-1.21.1`

It exists only as an engineering reference for the NeoForge-to-Fabric port.
It must not be added to commits, release archives, published source bundles,
or Elarion JARs.

## Licensing Boundary

Upstream's `license_code` applies the MIT License to source code and requires
preservation of its copyright and permission notice.

Upstream's README states that its assets are All Rights Reserved unless
specifically stated otherwise. The fishing rod asset also has a separate
custom license. Therefore:

- Code may be studied and adapted under the MIT terms.
- Upstream textures, models, sounds, icons, animations, fonts, screenshots,
  prose, lore, names, and other creative assets must not ship with Elarion.
- Files in the ignored checkout are not approved Elarion inputs.
- Only independently created Elarion assets may enter tracked source folders.

The exact checked-out commit must be recorded here after each deliberate
reference update.

Current reference commit: `06b2bd98c8db30f9eacfebfab04aa070e28a4e8b`

## Refresh Procedure

Run these commands only when intentionally updating the reference baseline:

```powershell
git -C reference/upstream-starcatcher-neoforge-1.21.1 fetch origin 2.4-neoforge-1.21.1
git -C reference/upstream-starcatcher-neoforge-1.21.1 checkout 2.4-neoforge-1.21.1
git -C reference/upstream-starcatcher-neoforge-1.21.1 pull --ff-only
git -C reference/upstream-starcatcher-neoforge-1.21.1 rev-parse HEAD
```

Update the recorded commit and reassess upstream licensing before using code
from a newer revision.
