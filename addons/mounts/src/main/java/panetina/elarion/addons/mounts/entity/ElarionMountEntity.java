package panetina.elarion.addons.mounts.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import panetina.elarion.addons.mounts.ElarionMountsAddon;
import panetina.elarion.addons.mounts.item.ElarionMountItems;
import panetina.elarion.addons.mounts.network.MountInputPayload;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ElarionMountEntity extends AbstractHorseEntity implements GeoEntity {
    private static final TrackedData<String> MOUNT_TYPE =
            DataTracker.registerData(ElarionMountEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> BASE_ANIMATION =
            DataTracker.registerData(ElarionMountEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<String> OVERLAY_ANIMATION =
            DataTracker.registerData(ElarionMountEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Boolean> BOOSTING =
            DataTracker.registerData(ElarionMountEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private static final int CLEARANCE_SCAN_BLOCKS = 24;
    private static final int SUMMON_LIFT_TICKS = 16;
    private static final int INPUT_FRESH_TICKS = 20;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private double flightSpeed;
    private UUID ownerUuid;
    private boolean returnWhistleOnDismiss;
    private float lastForwardInput;
    private float lastSidewaysInput;
    private boolean lastJumpInput;
    private boolean lastSneakInput;
    private float packetForwardInput;
    private float packetSidewaysInput;
    private boolean packetJumpInput;
    private boolean packetSneakInput;
    private boolean packetBoostInput;
    private float packetLookYaw;
    private float packetTurnIntent;
    private long lastInputAge = -100L;
    private float lastYawDelta;
    private float lastTurnIntent;
    private double smoothedForwardIntent;
    private double smoothedTurnIntent;
    private double smoothedVerticalIntent;
    private double smoothedBoostIntent;
    private double verticalSpeed;
    private int summonLiftTicks;
    private double summonLiftTargetY;
    private double animationTimeScale = 1.0D;

    public ElarionMountEntity(EntityType<? extends ElarionMountEntity> entityType, World world) {
        super(entityType, world);
        setNoGravity(true);
        setSilent(true);
        setAiDisabled(true);
        experiencePoints = 0;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return AbstractHorseEntity.createBaseHorseAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(MOUNT_TYPE, ElarionMountType.BEE.id());
        builder.add(BASE_ANIMATION, "idle");
        builder.add(OVERLAY_ANIMATION, "none");
        builder.add(BOOSTING, false);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // ElarionMountGeoModel applies a deterministic Blockbench-style blended
        // pose directly to Gecko bones. Registering competing controllers here
        // makes overlays snap or override each other on shared body bones.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    public ElarionMountType mountType() {
        return ElarionMountType.byId(dataTracker.get(MOUNT_TYPE));
    }

    public void setMountType(ElarionMountType type) {
        dataTracker.set(MOUNT_TYPE, type.id());
        setCustomName(null);
        setCustomNameVisible(false);
        calculateDimensions();
    }

    public void startSummonLift(double blocks) {
        summonLiftTicks = SUMMON_LIFT_TICKS;
        summonLiftTargetY = getY() + Math.max(0.0D, blocks);
    }

    public String baseAnimation() {
        return dataTracker.get(BASE_ANIMATION);
    }

    public String overlayAnimation() {
        return dataTracker.get(OVERLAY_ANIMATION);
    }

    public boolean isBoosting() {
        return dataTracker.get(BOOSTING);
    }

    public double animationTimeScale() {
        return animationTimeScale;
    }

    public void setAnimationTimeScale(double animationTimeScale) {
        this.animationTimeScale = Math.max(0.05D, Math.min(2.0D, animationTimeScale));
    }

    private String verticalOverlayAnimation() {
        for (String overlay : overlayAnimation().split("\\+")) {
            String trimmed = overlay.trim();
            if (trimmed.equals("ascend") || trimmed.equals("descend")) {
                return trimmed;
            }
        }
        return "none";
    }

    private String leanOverlayAnimation() {
        for (String overlay : overlayAnimation().split("\\+")) {
            String trimmed = overlay.trim();
            if (trimmed.equals("lean_left") || trimmed.equals("lean_right")) {
                return trimmed;
            }
        }
        return "none";
    }

    public Optional<UUID> ownerUuid() {
        return Optional.ofNullable(ownerUuid);
    }

    public void setOwner(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public void setReturnWhistleOnDismiss(boolean returnWhistleOnDismiss) {
        this.returnWhistleOnDismiss = returnWhistleOnDismiss;
    }

    public boolean returnWhistleOnDismiss() {
        return returnWhistleOnDismiss;
    }

    @Override
    public void tick() {
        setNoGravity(true);
        setSilent(true);
        super.tick();
        if (!getWorld().isClient()) {
            tickServerMovement();
        }
    }

    private void tickServerMovement() {
        LivingEntity controller = getControllingPassenger();
        if (controller instanceof ServerPlayerEntity player) {
            if (!isOwner(player)) {
                player.stopRiding();
                return;
            }
            tickControlledFlight(player);
            return;
        }

        lastForwardInput = 0.0F;
        lastSidewaysInput = 0.0F;
        lastJumpInput = false;
        lastSneakInput = false;
        smoothedForwardIntent = 0.0D;
        smoothedTurnIntent = 0.0D;
        smoothedVerticalIntent = 0.0D;
        smoothedBoostIntent = 0.0D;
        verticalSpeed = 0.0D;
        setBoosting(false);
        ElarionMountType.MovementProfile profile = mountType().movementProfile();
        flightSpeed *= profile.horizontalDrag();
        setVelocity(getVelocity().multiply(0.82D, profile.verticalDrag(), 0.82D));
        move(MovementType.SELF, getVelocity());
        setBaseAnimation("idle");
        setOverlayAnimation("none");
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (getControllingPassenger() instanceof PlayerEntity) {
            super.travel(Vec3d.ZERO);
            return;
        }
        super.travel(movementInput);
    }

    @Override
    public boolean canJump() {
        return false;
    }

    @Override
    public void setJumpStrength(int strength) {
        // Space is Elarion vertical flight input, not vanilla horse jump charge.
    }

    @Override
    public void startJumping(int height) {
        // Space is Elarion vertical flight input, not vanilla horse jumping.
    }

    @Override
    public void stopJumping() {
        // Space is Elarion vertical flight input, not vanilla horse jumping.
    }

    private void tickControlledFlight(ServerPlayerEntity player) {
        ElarionMountFlightInput input = age - lastInputAge <= INPUT_FRESH_TICKS
                ? storedInput()
                : ElarionMountFlightInput.neutral();
        driveControlledFlight(input);
    }

    public void applyClientPrediction(MountInputPayload payload) {
        if (!getWorld().isClient() || !(getControllingPassenger() instanceof PlayerEntity)) {
            return;
        }
        storeInput(payload);
        driveControlledFlight(storedInput());
    }

    private void driveControlledFlight(ElarionMountFlightInput input) {
        ElarionMountFlightController.State state = new ElarionMountFlightController.State(
                flightSpeed,
                verticalSpeed,
                smoothedForwardIntent,
                smoothedTurnIntent,
                smoothedVerticalIntent,
                smoothedBoostIntent);
        ElarionMountFlightController.Step step = ElarionMountFlightController.step(
                state,
                input,
                mountType().movementProfile(),
                getYaw());

        flightSpeed = step.state().flightSpeed();
        verticalSpeed = step.state().verticalSpeed();
        smoothedForwardIntent = step.state().smoothedForwardIntent();
        smoothedTurnIntent = step.state().smoothedTurnIntent();
        smoothedVerticalIntent = step.state().smoothedVerticalIntent();
        smoothedBoostIntent = step.state().smoothedBoostIntent();

        lastForwardInput = input.forward();
        lastSidewaysInput = input.sideways();
        lastJumpInput = input.jump();
        lastSneakInput = input.sneak();
        lastYawDelta = step.yawStep();
        lastTurnIntent = step.turnIntent();
        setBoosting(step.boostActive());

        if (Math.abs(step.yawStep()) > 0.03F) {
            setYaw((float) wrapDegrees(getYaw() + step.yawStep()));
        }
        setPitch(0.0F);
        setBodyYaw(getYaw());
        setHeadYaw(getYaw());

        double vertical = step.verticalSpeed();
        vertical = applySummonLift(vertical);
        vertical = applyMinimumClearance(vertical);
        if (vertical >= 0.0D && verticalSpeed < 0.0D) {
            verticalSpeed = 0.0D;
        }

        Vec3d horizontal = step.horizontalVelocity();
        setVelocity(horizontal.x, vertical, horizontal.z);
        move(MovementType.SELF, getVelocity());
        setBaseAnimation(step.baseAnimation());
        setOverlayAnimation(step.overlayAnimation());
        refreshPassengerPositions();
        velocityDirty = true;
    }

    private ElarionMountFlightInput storedInput() {
        return new ElarionMountFlightInput(
                packetForwardInput,
                packetSidewaysInput,
                packetJumpInput,
                packetSneakInput,
                packetBoostInput,
                packetTurnIntent);
    }

    private void refreshPassengerPositions() {
        for (Entity passenger : getPassengerList()) {
            updatePassengerPosition(passenger);
        }
    }

    private void setBaseAnimation(String animation) {
        if (!baseAnimation().equals(animation)) {
            dataTracker.set(BASE_ANIMATION, animation);
        }
    }

    private void setOverlayAnimation(String animation) {
        if (!overlayAnimation().equals(animation)) {
            dataTracker.set(OVERLAY_ANIMATION, animation);
        }
    }

    private void setBoosting(boolean boosting) {
        if (isBoosting() != boosting) {
            dataTracker.set(BOOSTING, boosting);
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (getWorld().isClient()) {
            return ActionResult.SUCCESS;
        }
        if (ownerUuid == null) {
            ownerUuid = player.getUuid();
        }
        if (!isOwner(player)) {
            player.sendMessage(Text.literal("This mount belongs to another rider."), true);
            return ActionResult.FAIL;
        }
        if (player.isSneaking() && !hasPassengers() && isEffectivelyGrounded()) {
            dismissTo(player);
            return ActionResult.SUCCESS;
        }
        if (!hasPassengers()) {
            if (player.startRiding(this, true)) {
                updatePassengerPosition(player);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengerList().isEmpty()
                && passenger instanceof PlayerEntity player
                && isOwner(player);
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        Entity passenger = getFirstPassenger();
        return passenger instanceof LivingEntity living ? living : null;
    }

    @Override
    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        return passengerSeatLocalOffset();
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater) {
        if (!hasPassenger(passenger)) {
            return;
        }
        Vec3d offset = passengerSeatWorldOffset();
        Vec3d seat = getPos().add(offset);
        positionUpdater.accept(passenger, seat.x, seat.y, seat.z);
    }

    private Vec3d passengerSeatLocalOffset() {
        ElarionMountType.RiderSeatProfile profile = mountType().riderSeatProfile();
        return new Vec3d(
                profile.serverXOffset(),
                profile.serverYOffset(),
                profile.serverZOffset());
    }

    private Vec3d passengerSeatWorldOffset() {
        return rotateLocalSeatOffset(passengerSeatLocalOffset());
    }

    private Vec3d rotateLocalSeatOffset(Vec3d localOffset) {
        Vec3d forward = getRotationVec(1.0F);
        forward = new Vec3d(forward.x, 0.0D, forward.z);
        if (forward.lengthSquared() < 1.0E-6D) {
            forward = Vec3d.fromPolar(0.0F, getYaw());
            forward = new Vec3d(forward.x, 0.0D, forward.z);
        }
        forward = forward.normalize();
        Vec3d right = new Vec3d(-forward.z, 0.0D, forward.x);
        return new Vec3d(
                right.x * localOffset.x + forward.x * -localOffset.z,
                localOffset.y,
                right.z * localOffset.x + forward.z * -localOffset.z);
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        double yawRadians = Math.toRadians(getYaw());
        return getPos().add(Math.cos(yawRadians) * 1.1D, 0.0D, Math.sin(yawRadians) * 1.1D);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {
    }

    @Override
    public boolean collidesWith(Entity other) {
        return false;
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public boolean canHaveStatusEffect(net.minecraft.entity.effect.StatusEffectInstance effect) {
        return false;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("MountType", mountType().id());
        if (ownerUuid != null) {
            nbt.putUuid("Owner", ownerUuid);
        }
        nbt.putBoolean("ReturnWhistleOnDismiss", returnWhistleOnDismiss);
        nbt.putDouble("FlightSpeed", flightSpeed);
        nbt.putDouble("VerticalSpeed", verticalSpeed);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setMountType(ElarionMountType.byId(nbt.getString("MountType")));
        ownerUuid = nbt.containsUuid("Owner") ? nbt.getUuid("Owner") : null;
        returnWhistleOnDismiss = nbt.getBoolean("ReturnWhistleOnDismiss");
        flightSpeed = nbt.getDouble("FlightSpeed");
        verticalSpeed = nbt.getDouble("VerticalSpeed");
        setNoGravity(true);
        setSilent(true);
        setAiDisabled(true);
    }

    public String debugSummary() {
        return "Mount " + mountType().id()
                + " passengers=" + getPassengerList().size()
                + " owner=" + ownerUuid
                + " forward=" + lastForwardInput
                + " side=" + lastSidewaysInput
                + " jump=" + lastJumpInput
                + " sneak=" + lastSneakInput
                + " boost=" + isBoosting()
                + " inputAge=" + (age - lastInputAge)
                + " packetForward=" + packetForwardInput
                + " packetJump=" + packetJumpInput
                + " packetSneak=" + packetSneakInput
                + " yawDelta=" + String.format(java.util.Locale.ROOT, "%.2f", lastYawDelta)
                + " turnIntent=" + String.format(java.util.Locale.ROOT, "%.2f", lastTurnIntent)
                + " forwardIntent=" + String.format(java.util.Locale.ROOT, "%.2f", smoothedForwardIntent)
                + " verticalIntent=" + String.format(java.util.Locale.ROOT, "%.2f", smoothedVerticalIntent)
                + " verticalSpeed=" + String.format(java.util.Locale.ROOT, "%.3f", verticalSpeed)
                + " boostIntent=" + String.format(java.util.Locale.ROOT, "%.2f", smoothedBoostIntent)
                + " base=" + baseAnimation()
                + " overlay=" + overlayAnimation()
                + " liftTicks=" + summonLiftTicks
                + " speed=" + String.format(java.util.Locale.ROOT, "%.3f", flightSpeed)
                + " velocity=" + String.format(java.util.Locale.ROOT, "%.3f,%.3f,%.3f", getVelocity().x, getVelocity().y, getVelocity().z)
                + " onGround=" + isOnGround()
                + " effectiveGround=" + isEffectivelyGrounded()
                + " yaw=" + String.format(java.util.Locale.ROOT, "%.1f", getYaw())
                + " seat=" + String.format(java.util.Locale.ROOT, "%.3f,%.3f,%.3f",
                passengerSeatWorldOffset().x,
                passengerSeatWorldOffset().y,
                passengerSeatWorldOffset().z);
    }

    public boolean isOwner(PlayerEntity player) {
        return ownerUuid == null || ownerUuid.equals(player.getUuid());
    }

    public void applyInput(ServerPlayerEntity player, MountInputPayload payload) {
        if (!isOwner(player) || player.getVehicle() != this) {
            return;
        }
        storeInput(payload);
        if (payload.dismount()) {
            requestDismount(player);
        }
    }

    private void storeInput(MountInputPayload payload) {
        packetForwardInput = clamp(payload.forward(), -1.0F, 1.0F);
        packetSidewaysInput = clamp(payload.sideways(), -1.0F, 1.0F);
        packetLookYaw = payload.lookYaw();
        packetTurnIntent = clamp(payload.turnIntent(), -30.0F, 30.0F);
        packetJumpInput = payload.jump();
        packetSneakInput = payload.sneak();
        packetBoostInput = payload.boost();
        lastInputAge = age;
    }

    public void requestDismount(ServerPlayerEntity player) {
        if (!isOwner(player) || player.getVehicle() != this) {
            return;
        }
        dismissTo(player);
    }

    private double applySummonLift(double vertical) {
        if (summonLiftTicks <= 0) {
            return vertical;
        }
        summonLiftTicks--;
        double remaining = summonLiftTargetY - getY();
        if (remaining <= 0.02D) {
            summonLiftTicks = 0;
            return Math.max(0.0D, vertical);
        }
        double lift = Math.min(0.18D, Math.max(0.06D, remaining / Math.max(1, summonLiftTicks + 1)));
        return Math.max(vertical, lift);
    }

    private double applyMinimumClearance(double vertical) {
        double clearance = mountType().movementProfile().minClearanceBlocks();
        if (clearance <= 0.0D) {
            return vertical;
        }
        double minY = minimumClearanceY();
        if (Double.isNaN(minY)) {
            return vertical;
        }
        double nextY = getY() + vertical;
        if (nextY < minY) {
            return Math.max(0.0D, minY - getY());
        }
        return vertical;
    }

    private double minimumClearanceY() {
        BlockPos.Mutable pos = new BlockPos.Mutable(getBlockX(), getBlockY(), getBlockZ());
        int bottom = getWorld().getBottomY();
        int minScanY = Math.max(bottom, getBlockY() - CLEARANCE_SCAN_BLOCKS);
        for (int y = getBlockY(); y >= minScanY; y--) {
            pos.set(getBlockX(), y, getBlockZ());
            if (!getWorld().getBlockState(pos).getCollisionShape(getWorld(), pos).isEmpty()) {
                return y + 1.0D + mountType().movementProfile().minClearanceBlocks();
            }
        }
        return Double.NaN;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public void dismissTo(PlayerEntity player) {
        if (getWorld().isClient()) {
            return;
        }
        for (Entity passenger : List.copyOf(getPassengerList())) {
            passenger.stopRiding();
        }
        if (returnWhistleOnDismiss && player instanceof ServerPlayerEntity serverPlayer
                && !serverPlayer.getAbilities().creativeMode) {
            ItemStack returned = new ItemStack(ElarionMountItems.whistle(mountType()));
            if (!serverPlayer.getInventory().insertStack(returned)) {
                dropStack(returned);
            }
        }
        if (ownerUuid != null) {
            ElarionMountsAddon.sessions().clear(ownerUuid);
        }
        discard();
    }

    private boolean isEffectivelyGrounded() {
        if (isOnGround()) {
            return true;
        }
        BlockPos below = getBlockPos().down();
        return getY() - Math.floor(getY()) < 0.35D
                && !getWorld().getBlockState(below).getCollisionShape(getWorld(), below).isEmpty();
    }

    private double wrapDegrees(double value) {
        double wrapped = value % 360.0D;
        if (wrapped >= 180.0D) wrapped -= 360.0D;
        if (wrapped < -180.0D) wrapped += 360.0D;
        return wrapped;
    }
}
