# Voice Chat Hooks Addon

Status: Shell.

## Purpose

`addons/voicechat-hooks` is reserved for future optional integration with voice
chat or proximity communication systems.

The current shell has no hard third-party voice-chat dependency. Its isolated
server startup without a provider is covered by
`dev/tools/optional-addon-qa.ps1`; any future adapter must preserve that
fallback or deliberately revise the loader contract and documentation.

## Main Source

- `addons/voicechat-hooks/src/main/java/panetina/elarion/addons/voicechat/`

## Ownership

This addon should only bridge external voice systems to Elarion state. It should
not own chat, Government authority, Realm membership, or identity state.
