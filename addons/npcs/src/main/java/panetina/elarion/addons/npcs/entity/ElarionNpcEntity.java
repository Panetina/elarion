package panetina.elarion.addons.npcs.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

public final class ElarionNpcEntity extends PathAwareEntity {
    private static final String PLACED_NPC_ID_KEY = "ElarionPlacedNpcId";

    private UUID placedNpcId;
    private boolean anchorConfigured;
    private double anchorX;
    private double anchorY;
    private double anchorZ;
    private float anchorYaw;

    public ElarionNpcEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
        setAiDisabled(true);
        setInvulnerable(true);
        setSilent(true);
        setNoGravity(true);
        setPersistent();
    }

    public Optional<UUID> placedNpcId() {
        return Optional.ofNullable(placedNpcId);
    }

    public void setPlacedNpcId(UUID placedNpcId) {
        this.placedNpcId = placedNpcId;
    }

    public void applyStaticState() {
        setAiDisabled(true);
        setInvulnerable(true);
        setSilent(true);
        setNoGravity(true);
        setPersistent();
        setVelocity(0.0D, 0.0D, 0.0D);
        setBodyYaw(getYaw());
        setHeadYaw(getYaw());
    }

    public void setAnchor(double x, double y, double z, float yaw) {
        anchorConfigured = true;
        anchorX = x;
        anchorY = y;
        anchorZ = z;
        anchorYaw = yaw;
        refreshPositionAndAngles(x, y, z, yaw, 0.0F);
        applyStaticState();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(net.minecraft.entity.Entity entity) {
    }

    @Override
    public void tick() {
        super.tick();
        if (anchorConfigured) {
            refreshPositionAndAngles(anchorX, anchorY, anchorZ, anchorYaw, 0.0F);
        }
        setVelocity(0.0D, 0.0D, 0.0D);
        setBodyYaw(getYaw());
        setHeadYaw(getYaw());
    }

    @Override
    public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
        return false;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (placedNpcId != null) {
            nbt.putUuid(PLACED_NPC_ID_KEY, placedNpcId);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        placedNpcId = nbt.containsUuid(PLACED_NPC_ID_KEY) ? nbt.getUuid(PLACED_NPC_ID_KEY) : null;
        anchorConfigured = false;
        applyStaticState();
    }
}
