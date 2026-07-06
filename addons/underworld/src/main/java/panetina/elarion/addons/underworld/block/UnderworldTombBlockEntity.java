package panetina.elarion.addons.underworld.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class UnderworldTombBlockEntity extends BlockEntity {
    private static final String CORPSE_ID_KEY = "CorpseId";
    private static final String OWNER_NAME_KEY = "OwnerName";
    private static final String ACCESS_STATE_KEY = "AccessState";
    private static final String PROTECTED_UNTIL_KEY = "ProtectedUntil";
    private static final String PUBLIC_LOOT_STARTED_AT_KEY = "PublicLootStartedAt";
    private static final String DECAYS_AT_KEY = "DecaysAt";
    private static final String ITEM_COUNT_KEY = "ItemCount";
    private String corpseId = "";
    private String ownerName = "";
    private String accessState = "";
    private long protectedUntil;
    private long publicLootStartedAt;
    private long decaysAt;
    private int itemCount;

    public UnderworldTombBlockEntity(BlockPos pos, BlockState state) {
        super(UnderworldBlocks.TOMB_ENTITY, pos, state);
    }

    public String corpseId() {
        return corpseId;
    }

    public void setCorpseId(String corpseId) {
        this.corpseId = corpseId == null ? "" : corpseId;
        markDirty();
    }

    public String accessState() {
        return accessState;
    }

    public String ownerName() {
        return ownerName;
    }

    public long protectedUntil() {
        return protectedUntil;
    }

    public long publicLootStartedAt() {
        return publicLootStartedAt;
    }

    public long decaysAt() {
        return decaysAt;
    }

    public int itemCount() {
        return itemCount;
    }

    public void setDisplay(String ownerName, String accessState, long protectedUntil, long publicLootStartedAt, long decaysAt, int itemCount) {
        String nextOwnerName = ownerName == null ? "" : ownerName;
        String nextState = accessState == null ? "" : accessState;
        int nextCount = Math.max(0, itemCount);
        if (this.ownerName.equals(nextOwnerName)
                && this.accessState.equals(nextState)
                && this.protectedUntil == protectedUntil
                && this.publicLootStartedAt == publicLootStartedAt
                && this.decaysAt == decaysAt
                && this.itemCount == nextCount) {
            return;
        }
        this.ownerName = nextOwnerName;
        this.accessState = nextState;
        this.protectedUntil = protectedUntil;
        this.publicLootStartedAt = publicLootStartedAt;
        this.decaysAt = decaysAt;
        this.itemCount = nextCount;
        markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putString(CORPSE_ID_KEY, corpseId);
        nbt.putString(OWNER_NAME_KEY, ownerName);
        nbt.putString(ACCESS_STATE_KEY, accessState);
        nbt.putLong(PROTECTED_UNTIL_KEY, protectedUntil);
        nbt.putLong(PUBLIC_LOOT_STARTED_AT_KEY, publicLootStartedAt);
        nbt.putLong(DECAYS_AT_KEY, decaysAt);
        nbt.putInt(ITEM_COUNT_KEY, itemCount);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        corpseId = nbt.getString(CORPSE_ID_KEY);
        ownerName = nbt.getString(OWNER_NAME_KEY);
        accessState = nbt.getString(ACCESS_STATE_KEY);
        protectedUntil = nbt.getLong(PROTECTED_UNTIL_KEY);
        publicLootStartedAt = nbt.getLong(PUBLIC_LOOT_STARTED_AT_KEY);
        decaysAt = nbt.getLong(DECAYS_AT_KEY);
        itemCount = nbt.getInt(ITEM_COUNT_KEY);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
}
