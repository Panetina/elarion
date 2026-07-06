# Newspapers Addon

Status: Shell.

## Purpose

`addons/newspapers` is reserved for future newspaper and public-memory
presentation.

## Main Source

- `addons/newspapers/src/main/java/panetina/elarion/addons/newspapers/`

## Ownership

Core owns history and Chronicle/public-history read models. Newspapers should
consume those APIs instead of scanning raw history files.

## Notes

Do not make newspaper/search UI player-facing on raw JSONL scans.
