package panetina.elarion.addons.underworld.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.BlockPos;

public final class CorpseRecord {
    public String corpseId = "";
    public String victimId = "";
    public String victimName = "";
    public String killerId = "";
    public String worldId = "";
    public String victimRealmId = "";
    public String tombstoneVariant = "";
    public int tombX;
    public int tombY;
    public int tombZ;
    public double x;
    public double y;
    public double z;
    public long createdAt;
    public long publicLootStartedAt;
    public long decaysAt;
    public int selectedHotbarSlot = -1;
    public ElarionDeathType deathType = ElarionDeathType.UNKNOWN;
    public List<StoredItemStack> protectedVictimItems = new ArrayList<>();
    public List<StoredItemStack> pvpLootItems = new ArrayList<>();
    public boolean victimRecovered;
    public boolean pvpLootClaimed;
    public long killerExclusiveUntil;
    public boolean expiredToRecovery;

    public boolean hasTombPosition() {
        return !tombstoneVariant.isBlank();
    }

    public BlockPos tombOrigin() {
        return new BlockPos(tombX, tombY, tombZ);
    }
}
