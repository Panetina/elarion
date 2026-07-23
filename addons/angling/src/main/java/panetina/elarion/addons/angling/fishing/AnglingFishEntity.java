package panetina.elarion.addons.angling.fishing;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import panetina.elarion.addons.angling.component.AnglingDataComponents;
import panetina.elarion.addons.angling.component.AnglingSingleStackComponent;
import panetina.elarion.addons.angling.registry.AnglingItems;

/** One generic fish entity whose authoritative caught item carries species/quality components. */
public final class AnglingFishEntity extends FishEntity {
    private static final String FISH_NBT = "ElarionAnglingFish";
    private static final TrackedData<ItemStack> FISH = DataTracker.registerData(
            AnglingFishEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    public AnglingFishEntity(EntityType<? extends AnglingFishEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0D);
    }

    public void setFish(ItemStack stack) {
        if (stack == null || stack.isEmpty()) throw new IllegalArgumentException("Fish entity item cannot be empty");
        dataTracker.set(FISH, stack.copyWithCount(1));
    }

    public ItemStack getFish() {
        return dataTracker.get(FISH).copy();
    }

    @Override
    public ItemStack getBucketItem() {
        ItemStack bucket = new ItemStack(AnglingItems.require("starcaught_bucket"));
        bucket.set(AnglingDataComponents.BUCKETED_FISH, new AnglingSingleStackComponent(getFish()));
        return bucket;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_TROPICAL_FISH_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_TROPICAL_FISH_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_TROPICAL_FISH_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.ENTITY_TROPICAL_FISH_FLOP;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(FISH, ItemStack.EMPTY);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (!getWorld().isClient && !getFish().isEmpty()) {
            dropStack(getFish());
            dataTracker.set(FISH, ItemStack.EMPTY);
        }
        super.onDeath(damageSource);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        ItemStack fish = getFish();
        if (!fish.isEmpty()) nbt.put(FISH_NBT, fish.encode(getRegistryManager()));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains(FISH_NBT)) {
            ItemStack.fromNbt(getRegistryManager(), nbt.get(FISH_NBT)).ifPresent(this::setFish);
        }
    }
}
