package panetina.elarion.addons.realms.service;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import panetina.elarion.addons.realms.config.RealmProtectionConfig;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.RealmRelationship;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RealmProtectionService {
    private static volatile RealmProtectionService instance;
    private final ElarionApi api;
    private final RealmProtectionConfig config;
    private final Map<UUID, Long> lastFeedback = new ConcurrentHashMap<>();

    public RealmProtectionService(ElarionApi api, RealmProtectionConfig config) {
        this.api = api;
        this.config = config;
        instance = this;
    }

    public void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) ->
                !(player instanceof ServerPlayerEntity serverPlayer)
                        || canBreak(serverPlayer, world));
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }
            Block block = world.getBlockState(hit.getBlockPos()).getBlock();
            if (canUseBlock(serverPlayer, world, block, hit.getBlockPos(), hand)) {
                return ActionResult.PASS;
            }
            deny(serverPlayer, "You cannot use or place blocks in this Realm.");
            return ActionResult.FAIL;
        });
        AttackEntityCallback.EVENT.register((player, world, hand, target, hit) -> {
            if (world.isClient || !(player instanceof ServerPlayerEntity serverPlayer)
                    || !(target instanceof LivingEntity)) {
                return ActionResult.PASS;
            }
            if (canAttack(serverPlayer, world, target)) return ActionResult.PASS;
            deny(serverPlayer, target instanceof PlayerEntity
                    ? "PvP is not allowed here."
                    : "You cannot attack creatures in this Realm.");
            return ActionResult.FAIL;
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity victim)
                    || !(damageSource.getAttacker() instanceof ServerPlayerEntity killer)) {
                return;
            }
            recordDiplomatDeath(victim, killer);
        });
    }

    public static boolean protectsExplosionBlocks(World world) {
        RealmProtectionService service = instance;
        return service != null && service.config.protectExplosionBlocks()
                && (service.owner(world).isPresent() || service.isShared(world));
    }

    private boolean canBreak(ServerPlayerEntity player, World world) {
        Optional<RealmDefinition> owner = owner(world);
        if (bypass(player)) return true;
        if (owner.isEmpty()) {
            boolean allowed = !isShared(world);
            if (!allowed) deny(player, "You cannot break blocks in this protected world.");
            return allowed;
        }
        RealmAccessPolicy policy = policy(player, owner.get());
        boolean allowed = policy.canBreak();
        if (!allowed) deny(player, "You cannot break blocks in another Realm.");
        return allowed;
    }

    private boolean canUseBlock(
            ServerPlayerEntity player,
            World world,
            Block block,
            net.minecraft.util.math.BlockPos pos,
            net.minecraft.util.Hand hand
    ) {
        Optional<RealmDefinition> owner = owner(world);
        if (bypass(player)) return true;
        if (owner.isEmpty()) return !isShared(world);
        RealmAccessPolicy policy = policy(player, owner.get());
        if (policy.owner()) return true;
        boolean container = isContainer(world, pos, block);
        boolean mechanism = isMechanism(block);
        if (container) return policy.canUseContainer();
        if (mechanism) return policy.canUseMechanism();
        return player.getStackInHand(hand).getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof LadderBlock
                && policy.canPlaceLadder();
    }

    private boolean canAttack(ServerPlayerEntity attacker, World world, net.minecraft.entity.Entity target) {
        if (bypass(attacker)) return true;
        if (target instanceof ServerPlayerEntity targetPlayer) {
            if (isShared(world)) return false;
            Optional<RealmDefinition> owner = owner(world);
            if (owner.isEmpty()) return true;
            String attackerRealm = api.realm().citizens().getOrCreate(attacker).realmId();
            String targetRealm = api.realm().citizens().getOrCreate(targetPlayer).realmId();
            String ownerRealm = owner.get().id();
            if (!ownerRealm.equals(attackerRealm) && !ownerRealm.equals(targetRealm)) {
                return false;
            }
            if (attackerRealm.isBlank() || targetRealm.isBlank() || attackerRealm.equals(targetRealm)) {
                return false;
            }
            RealmRelationship relationship = api.realm().governance().relationship(attackerRealm, targetRealm);
            if (relationship == RealmRelationship.HOSTILE) {
                return ownerRealm.equals(attackerRealm) || ownerRealm.equals(targetRealm);
            }
            return ownerRealm.equals(attackerRealm) && isDiplomat(targetPlayer);
        }

        Optional<RealmDefinition> owner = owner(world);
        if (owner.isEmpty()) return true;
        return policy(attacker, owner.get()).canAttackCreature();
    }

    private Optional<RealmDefinition> owner(World world) {
        return api.realm().realms().ownerForWorld(worldId(world));
    }

    private boolean isShared(World world) {
        return config.sharedWorldIds().contains(worldId(world));
    }

    private boolean bypass(ServerPlayerEntity player) {
        return config.operatorBypass() && player.hasPermissionLevel(4);
    }

    private RealmAccessPolicy policy(ServerPlayerEntity player, RealmDefinition owner) {
        CitizenRecord citizen = api.realm().citizens().getOrCreate(player);
        if (owner.id().equals(citizen.realmId())) return RealmAccessPolicy.forOwner();
        return RealmAccessPolicy.visitor(
                api.realm().governance().relationship(citizen.realmId(), owner.id()));
    }

    private boolean isDiplomat(ServerPlayerEntity player) {
        return api.system().abilities().has(api.realm().citizens().getOrCreate(player),
                "elarion.portal.foreign_access");
    }

    private void recordDiplomatDeath(ServerPlayerEntity victim, ServerPlayerEntity killer) {
        if (!isDiplomat(victim)) return;
        CitizenRecord victimCitizen = api.realm().citizens().getOrCreate(victim);
        CitizenRecord killerCitizen = api.realm().citizens().getOrCreate(killer);
        if (victimCitizen.realmId().isBlank()
                || killerCitizen.realmId().isBlank()
                || victimCitizen.realmId().equals(killerCitizen.realmId())) {
            return;
        }
        String victimName = api.identity().identities().resolve(victim).displayName().getString();
        String killerName = api.identity().identities().resolve(killer).displayName().getString();
        String text = "A Diplomat of " + victimCitizen.realmId()
                + ", " + victimName
                + ", was slain by " + killerName
                + " of " + killerCitizen.realmId() + ".";
        api.progressionApi().history().recordChronicle("realm", "diplomat-killed", killer.getUuid(),
                "player", victim.getUuidAsString(), victimCitizen.realmId(), Map.of(
                        "victim", victimName,
                        "victimRealm", victimCitizen.realmId(),
                        "killer", killerName,
                        "killerRealm", killerCitizen.realmId()
                ), text);
    }

    private boolean isMechanism(Block block) {
        return block instanceof DoorBlock
                || block instanceof TrapdoorBlock
                || block instanceof FenceGateBlock
                || block instanceof ButtonBlock
                || block instanceof LeverBlock
                || config.extraAllyInteractableBlocks().contains(Registries.BLOCK.getId(block).toString());
    }

    private boolean isContainer(World world, net.minecraft.util.math.BlockPos pos, Block block) {
        return world.getBlockEntity(pos) instanceof Inventory
                || block instanceof AbstractChestBlock<?>
                || block instanceof BarrelBlock
                || block instanceof ShulkerBoxBlock
                || block instanceof HopperBlock
                || block instanceof DispenserBlock
                || config.extraContainerBlocks().contains(Registries.BLOCK.getId(block).toString());
    }

    private void deny(ServerPlayerEntity player, String message) {
        long now = System.currentTimeMillis();
        long previous = lastFeedback.getOrDefault(player.getUuid(), 0L);
        if (now - previous < config.feedbackCooldownMillis()) return;
        lastFeedback.put(player.getUuid(), now);
        player.sendMessage(Text.literal(message).formatted(Formatting.RED), true);
    }

    private static String worldId(World world) {
        Identifier id = world.getRegistryKey().getValue();
        return id.toString();
    }
}
