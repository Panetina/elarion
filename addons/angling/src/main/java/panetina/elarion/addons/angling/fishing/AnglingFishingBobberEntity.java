package panetina.elarion.addons.angling.fishing;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import panetina.elarion.addons.angling.minigame.AnglingMinigameInputGate;
import panetina.elarion.addons.angling.minigame.AnglingMinigameSessionHost;
import panetina.elarion.addons.angling.minigame.AnglingServerMinigameSession;
import panetina.elarion.addons.angling.minigame.AnglingServerMinigameSnapshot;
import panetina.elarion.addons.angling.network.AnglingMinigameInputPayload;
import panetina.elarion.addons.angling.network.AnglingMinigameStatePayload;
import panetina.elarion.addons.angling.definition.AnglingCatchSnapshot;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.modifier.AnglingEquipmentModifiers;
import panetina.elarion.addons.angling.minigame.AnglingServerMinigameStatus;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.registry.tag.FluidTags;

import java.util.Objects;
import java.util.UUID;

/** Live Fabric entity shell; selection/reward activation remains release-gated. */
public final class AnglingFishingBobberEntity extends ProjectileEntity implements AnglingMinigameSessionHost {
    private static final TrackedData<Integer> STATE = DataTracker.registerData(
            AnglingFishingBobberEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> VOID = DataTracker.registerData(
            AnglingFishingBobberEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final double MAX_OWNER_DISTANCE_SQUARED = 7_024.0D;

    private UUID ownerId;
    private AnglingBobberStateMachine stateMachine;
    private AnglingServerMinigameSession minigame;
    private AnglingFishingService service;
    private ItemStack rod = ItemStack.EMPTY;
    private AnglingEquipmentModifiers.Resolved equipment;
    private AnglingCatchSnapshot.NativeCatch selectedCatch;
    private ItemStack treasure = ItemStack.EMPTY;
    private Hand hand;
    private boolean commitStarted;
    private boolean survivesLava;
    private long lastSentRevision = -1;

    public AnglingFishingBobberEntity(
            EntityType<? extends AnglingFishingBobberEntity> entityType,
            World world
    ) {
        super(entityType, world);
    }

    public void configure(
            ServerPlayerEntity owner,
            AnglingBobberStateMachine stateMachine,
            double throwSpeedMultiplier,
            AnglingFishingService service,
            ItemStack rod,
            AnglingEquipmentModifiers.Resolved equipment,
            Hand hand
    ) {
        if (!getWorld().isClient && this.stateMachine != null) {
            throw new IllegalStateException("Angling bobber is already configured");
        }
        this.ownerId = Objects.requireNonNull(owner, "owner").getUuid();
        this.stateMachine = Objects.requireNonNull(stateMachine, "stateMachine");
        this.service = Objects.requireNonNull(service, "service");
        this.rod = Objects.requireNonNull(rod, "rod");
        this.equipment = Objects.requireNonNull(equipment, "equipment");
        this.hand = Objects.requireNonNull(hand, "hand");
        if (!Double.isFinite(throwSpeedMultiplier) || throwSpeedMultiplier < 0.0D) {
            throw new IllegalArgumentException("bobber throw multiplier must be finite and non-negative");
        }
        setOwner(owner);
        survivesLava = rod.getOrDefault(AnglingDataComponents.NETHERITE_UPGRADE, false)
                || equipment.has("survives_lava");
        setNoGravity(stateMachine.noGravity());
        dataTracker.set(VOID, stateMachine.noGravity());
        float pitch = owner.getPitch();
        float yaw = owner.getYaw();
        float yawCos = MathHelper.cos(-yaw * MathHelper.RADIANS_PER_DEGREE - MathHelper.PI);
        float yawSin = MathHelper.sin(-yaw * MathHelper.RADIANS_PER_DEGREE - MathHelper.PI);
        float pitchCos = -MathHelper.cos(-pitch * MathHelper.RADIANS_PER_DEGREE);
        float pitchSin = MathHelper.sin(-pitch * MathHelper.RADIANS_PER_DEGREE);
        refreshPositionAndAngles(owner.getX() - yawSin * 0.3D, owner.getEyeY(),
                owner.getZ() - yawCos * 0.3D, yaw, pitch);
        Vec3d direction = new Vec3d(-yawSin, MathHelper.clamp(-(pitchSin / pitchCos), -5.0F, 5.0F), -yawCos);
        double base = 0.6D / direction.length();
        Vec3d launchVelocity = direction.multiply(
                (base + triangular(0.5D, 0.0103365D)) * throwSpeedMultiplier,
                (base + triangular(0.5D, 0.0103365D)) * throwSpeedMultiplier,
                (base + triangular(0.5D, 0.0103365D)) * throwSpeedMultiplier);
        setVelocity(launchVelocity);
        setYaw((float) (MathHelper.atan2(launchVelocity.x, launchVelocity.z) * 180.0D / Math.PI));
        setPitch((float) (MathHelper.atan2(launchVelocity.y, launchVelocity.horizontalLength())
                * 180.0D / Math.PI));
    }

    private double triangular(double mode, double deviation) {
        return mode + deviation * (random.nextDouble() - random.nextDouble());
    }

    public void attachMinigame(AnglingServerMinigameSession session) {
        Objects.requireNonNull(session, "session");
        if (minigame != null) throw new IllegalStateException("Bobber already owns a minigame session");
        if (getId() != session.bobberEntityId() || !anglingOwnerId().equals(session.actorId())) {
            throw new IllegalArgumentException("Minigame identity does not match bobber ownership");
        }
        minigame = session;
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) return;
        if (!(getOwner() instanceof ServerPlayerEntity owner) || shouldStop(owner)) {
            releaseAndDiscard();
            return;
        }
        if (stateMachine == null) {
            releaseAndDiscard();
            return;
        }

        BlockPos fluidPosition = BlockPos.ofFloored(getX(), getY() + 0.175D, getZ());
        var fluidState = getWorld().getFluidState(fluidPosition);
        boolean inFluid = !fluidState.isEmpty();
        if (fluidState.isIn(FluidTags.LAVA) && !survivesLava) {
            releaseAndDiscard();
            return;
        }
        boolean fluidBelow = !getWorld().getFluidState(fluidPosition.down()).isEmpty();
        boolean supported = inFluid || stateMachine.state() != AnglingBobberState.FLYING && fluidBelow;
        AnglingBobberStateMachine.Transition transition = stateMachine.tick(supported, random::nextDouble);
        dataTracker.set(STATE, stateMachine.state().ordinal());
        if (transition == AnglingBobberStateMachine.Transition.STARTED_BOBBING) {
            setVelocity(getVelocity().multiply(0.3D));
            return;
        }
        if (transition == AnglingBobberStateMachine.Transition.BITE_STARTED) {
            setPosition(getX(), getY() - 0.5D, getZ());
        }
        if (transition == AnglingBobberStateMachine.Transition.BITE_EXPIRED) {
            service.missedBite(this);
            return;
        }

        if (stateMachine.state() == AnglingBobberState.FISHING && minigame != null) {
            minigame.tick(owner.getServer().getTicks());
            AnglingServerMinigameSnapshot snapshot = minigame.snapshot();
            if (snapshot.revision() != lastSentRevision) {
                ServerPlayNetworking.send(owner, new AnglingMinigameStatePayload(snapshot));
                lastSentRevision = snapshot.revision();
            }
            if (minigame.status() == AnglingServerMinigameStatus.SUCCEEDED) {
                service.complete(owner, this, snapshot.perfect(), minigame.treasureCompleted(),
                        Math.toIntExact(snapshot.elapsedTicks()), snapshot.totalHits());
            } else if (minigame.status().terminal()) {
                releaseAndDiscard();
            }
        }
        applyPhysics(inFluid);
    }

    private void applyPhysics(boolean inFluid) {
        AnglingBobberState state = stateMachine.state();
        Vec3d velocity = getVelocity();
        if (state == AnglingBobberState.FLYING && !dataTracker.get(VOID) && velocity.y < 1.2D) {
            velocity = velocity.add(0.0D, -0.02D, 0.0D);
        }
        if (state == AnglingBobberState.BITING || state == AnglingBobberState.FISHING) {
            velocity = Vec3d.ZERO;
        } else if (state == AnglingBobberState.BOBBING) {
            if (dataTracker.get(VOID)) velocity = new Vec3d(velocity.x, velocity.y * 0.9D, velocity.z);
            else if (inFluid) velocity = velocity.add(0.0D, 0.01D, 0.0D);
            else velocity = velocity.add(0.0D, random.nextFloat() > 0.02F ? -0.03D : -0.01D, 0.0D);
        }
        setVelocity(velocity);
        move(MovementType.SELF, velocity);
        if (isOnGround() || horizontalCollision) setVelocity(Vec3d.ZERO);
        else setVelocity(getVelocity().multiply(0.92D));
    }

    @Override
    public boolean isFireImmune() {
        return survivesLava || super.isFireImmune();
    }

    private boolean shouldStop(PlayerEntity owner) {
        TagKey<Item> rods = TagKey.of(RegistryKeys.ITEM, Identifier.of("elarion_angling", "rods"));
        boolean holdingRod = owner.getMainHandStack().isIn(rods) || owner.getOffHandStack().isIn(rods);
        return owner.isRemoved() || !owner.isAlive() || !holdingRod
                || squaredDistanceTo(owner) > MAX_OWNER_DISTANCE_SQUARED;
    }

    public boolean tryReel() {
        return stateMachine != null && stateMachine.reel();
    }

    public AnglingBobberState anglingState() {
        int ordinal = dataTracker.get(STATE);
        AnglingBobberState[] states = AnglingBobberState.values();
        return ordinal >= 0 && ordinal < states.length ? states[ordinal] : AnglingBobberState.FLYING;
    }

    public void selectCatch(AnglingCatchSnapshot.NativeCatch selectedCatch) {
        if (this.selectedCatch != null) throw new IllegalStateException("Bobber catch is already selected");
        this.selectedCatch = Objects.requireNonNull(selectedCatch, "selectedCatch");
    }

    public void setTreasure(ItemStack treasure) {
        this.treasure = Objects.requireNonNull(treasure, "treasure").copy();
    }

    ItemStack treasure() {
        return treasure.copy();
    }

    AnglingCatchSnapshot.NativeCatch selectedCatch() {
        if (selectedCatch == null) throw new IllegalStateException("Bobber has no selected catch");
        return selectedCatch;
    }

    ItemStack rod() {
        return rod;
    }

    AnglingEquipmentModifiers.Resolved equipment() {
        return Objects.requireNonNull(equipment, "equipment");
    }

    boolean beginCommit() {
        if (commitStarted) return false;
        commitStarted = true;
        return true;
    }

    public void releaseAndDiscard() {
        if (isRemoved()) return;
        if (service != null) service.release(anglingOwnerId(), getUuid());
        discard();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(STATE, AnglingBobberState.FLYING.ordinal());
        builder.add(VOID, false);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        // Entity type disables saving; active fishing sessions never survive a world restart.
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        // Entity type disables saving; rewards are persisted separately before vanilla loot suppression.
    }

    @Override
    public UUID anglingOwnerId() {
        if (ownerId != null) return ownerId;
        if (getOwner() != null) return getOwner().getUuid();
        return new UUID(0L, 0L);
    }

    @Override
    public AnglingMinigameInputGate.Result acceptAnglingInput(
            UUID senderId,
            AnglingMinigameInputPayload payload,
            long serverTick
    ) {
        return minigame == null
                ? AnglingMinigameInputGate.Result.CLOSED
                : minigame.acceptInput(senderId, payload, serverTick);
    }

    @Override
    public AnglingServerMinigameSnapshot anglingMinigameSnapshot() {
        if (minigame == null) throw new IllegalStateException("Bobber has no minigame session");
        return minigame.snapshot();
    }
}
