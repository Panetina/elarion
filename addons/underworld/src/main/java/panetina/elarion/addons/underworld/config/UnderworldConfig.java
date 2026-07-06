package panetina.elarion.addons.underworld.config;

import java.util.List;

public record UnderworldConfig(
        boolean enabled,
        String worldId,
        double spawnX,
        double spawnY,
        double spawnZ,
        int pveTimerMinutes,
        int pvpTimerMinutes,
        int authorityTimerMinutes,
        int extraMinutesPerUnderworldDeath,
        boolean pauseTimerOnLogout,
        boolean disableChat,
        boolean disablePortals,
        boolean hideNameplates,
        int corpseExpiresMinutes,
        boolean pvpLootEnabled,
        boolean armorDrops,
        int randomItemMin,
        int randomItemMax,
        double physicalCurrencyPercent,
        int killerExclusiveSeconds,
        boolean allowOtherPlayersAfterKillerWindow,
        boolean includeOffhandRandomLoot,
        List<String> excludedItemIds,
        List<String> excludedItemTags,
        List<String> physicalCurrencyItemIds,
        List<String> physicalCurrencyTags,
        boolean combatTagEnabled,
        int combatTagDurationSeconds,
        boolean soulEnabled,
        int maxFractures,
        boolean trueDeathAtMaxFractures,
        List<String> enabledWorlds,
        List<String> excludedWorlds
) {
    public static UnderworldConfig defaults() {
        return new UnderworldConfig(
                true,
                "elarion:underworld",
                0.5D, 80.0D, 0.5D,
                5, 10, 15, 10,
                true, true, true, true,
                120,
                true, true, 3, 5, 0.25D, 300, false, true,
                List.of(),
                List.of("elarion:soulbound", "elarion:quest_items", "elarion:authority_items", "elarion:no_pvp_loot"),
                List.of("elarion:currency"),
                List.of("elarion:physical_currency"),
                true, 30,
                true, 3, true,
                List.of("*"),
                List.of("elarion:underworld")
        );
    }
}
