# Elarion Worlds: Upstream References

Elarion Worlds is an Elarion-native implementation. Its configuration,
services, commands, history integration, ecology rules, and public API are
original to this project.

The runtime-world architecture was studied from
[IsaiahMC/multiworld](https://github.com/IsaiahMC/multiworld), licensed
LGPL-3.0. Elarion Worlds does not copy Multiworld's source or command system.
It uses the separately published Nucleoid Fantasy library for persistent
runtime dimensions.

Per-world border packet behavior was adapted from
[PotatoPresident/worldborderfixer](https://github.com/PotatoPresident/worldborderfixer),
licensed MIT. Copyright (c) 2021 Potatoboy9999.

Multiworld also depends on iCommonLib for portal collision hooks and
cross-version utility behavior. Elarion Worlds intentionally does not depend
on iCommonLib because Elarion Core and the Elarion Portals addon own those
responsibilities.
