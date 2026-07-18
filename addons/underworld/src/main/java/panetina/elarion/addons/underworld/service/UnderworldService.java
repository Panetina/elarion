package panetina.elarion.addons.underworld.service;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.slf4j.Logger;
import panetina.elarion.addons.underworld.block.TombstoneVariant;
import panetina.elarion.addons.underworld.block.UnderworldBlocks;
import panetina.elarion.addons.underworld.block.UnderworldTombBlock;
import panetina.elarion.addons.underworld.block.UnderworldTombBlockEntity;
import panetina.elarion.addons.underworld.config.UnderworldConfig;
import panetina.elarion.addons.underworld.config.UnderworldConfigLoader;
import panetina.elarion.addons.underworld.model.CombatTag;
import panetina.elarion.addons.underworld.model.CorpseRecord;
import panetina.elarion.addons.underworld.model.ElarionDeathType;
import panetina.elarion.addons.underworld.model.SoulState;
import panetina.elarion.addons.underworld.model.StoredItemStack;
import panetina.elarion.addons.underworld.model.UnderworldSession;
import panetina.elarion.addons.underworld.network.UnderworldStatusSyncPayload;
import panetina.elarion.addons.underworld.network.GraveOpenPayload;
import panetina.elarion.addons.underworld.storage.UnderworldState;
import panetina.elarion.addons.underworld.storage.UnderworldStorage;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.ElarionDomainEvent;
import panetina.elarion.core.service.PlayerRestrictionService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class UnderworldService {
    public static final String LIFETIME_DEATHS_STAT = "underworld_lifetime_deaths";
    private static final String ACCESS_PROTECTED = "protected";
    private static final String ACCESS_LOOTABLE = "lootable";
    private static final String ACCESS_KILLER = "killer";
    private static final String ACCESS_OWNER = "owner";
    private static final String ACCESS_MESSAGE = "message";
    private static final int MAIN_INVENTORY_SIZE = 36;
    private static final int MAX_TOMB_DISPLAY_UPDATES_PER_SECOND = 64;
    private static final int MAX_CORPSE_EXPIRATIONS_PER_SECOND = 64;
    private static final long PROTECTED_EXPIRY_RETRY_MILLIS = 1_000L;

    private final Logger logger;
    private final ElarionApi api;
    private final UnderworldStorage storage;
    private UnderworldConfig config;
    private UnderworldState state = new UnderworldState();
    private final Map<UUID, CombatTag> combatTags = new LinkedHashMap<>();
    private final UnderworldExpiryQueue expiryQueue = new UnderworldExpiryQueue();
    private final ArrayDeque<String> tombDisplayQueue = new ArrayDeque<>();
    private final Set<String> queuedTombDisplays = new HashSet<>();
    private final UnderworldTombService tombs;
    private MinecraftServer server;
    private int ticks;
    private boolean dirty;

    public UnderworldService(Logger logger, ElarionApi api, UnderworldStorage storage, UnderworldConfig config) {
        this.logger = logger;
        this.api = api;
        this.storage = storage;
        this.config = config;
        this.tombs = new UnderworldTombService(logger);
    }

    public void registerEvents() {
        api.system().restrictions().register(this::restriction);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(this::trackCombatTag);
        ServerLivingEntityEvents.ALLOW_DEATH.register(this::captureFatalDeath);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (activeSession(newPlayer.getUuid()).isPresent()) {
                api.tasks().enqueueServer("underworld-respawn-" + newPlayer.getUuidAsString(),
                        () -> sendToUnderworld(newPlayer, false));
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            UUID id = handler.getPlayer().getUuid();
            activeSession(id).ifPresent(session -> {
                if (session.paused) {
                    session.paused = false;
                    session.pausedAt = 0L;
                    dirty = true;
                }
                sendToUnderworld(handler.getPlayer(), false);
            });
            syncStatus(handler.getPlayer());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            activeSession(handler.getPlayer().getUuid()).ifPresent(session -> {
                if (config.pauseTimerOnLogout()) {
                    session.paused = true;
                    session.pausedAt = System.currentTimeMillis();
                    dirty = true;
                    save();
                }
            });
        });
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        this.state = storage.load(server);
        if (state.recoveryVaults == null) state.recoveryVaults = new LinkedHashMap<>();
        api.characters().registerResetHandler("elarion_underworld", context -> resetCharacter(context.accountId()));
        migrateCorpseLifecycle();
        reconcileGraves();
        rebuildRuntimeIndexes();
        dirty = true;
        save();
    }

    public void reload() {
        this.config = UnderworldConfigLoader.reload(logger, config);
    }

    public UnderworldConfig config() {
        return config;
    }

    public void tick() {
        if (server == null) return;
        ticks++;
        if (ticks % 20 == 0) {
            tickSessions();
            flushTombDisplays();
            expireDueCorpses();
        }
        if (dirty && ticks % 100 == 0) save();
    }

    public void shutdown() {
        save();
    }

    public boolean handleTombInteraction(ServerPlayerEntity player, BlockPos pos, Hand hand) {
        if (hand != Hand.MAIN_HAND) return false;
        BlockState state = player.getWorld().getBlockState(pos);
        if (!state.isOf(UnderworldBlocks.TOMB)) return false;
        BlockPos origin = UnderworldTombBlock.origin(pos, state);
        if (!(player.getWorld().getBlockEntity(origin) instanceof UnderworldTombBlockEntity tomb)) return false;
        CorpseRecord corpse = this.state.corpses.get(tomb.corpseId());
        if (corpse == null) {
            openMessage(player, tomb.corpseId(), "Grave unavailable", "This tomb is no longer bound to a corpse.", true);
            return true;
        }
        openCorpse(player, corpse, "");
        return true;
    }

    public Optional<UnderworldSession> activeSession(UUID playerId) {
        return Optional.ofNullable(state.sessions.get(playerId.toString()));
    }

    public Optional<SoulState> soul(UUID playerId) {
        return Optional.ofNullable(state.souls.get(playerId.toString()));
    }

    public List<CorpseRecord> corpses() {
        return List.copyOf(state.corpses.values());
    }

    public Optional<CorpseRecord> corpse(String corpseId) {
        return Optional.ofNullable(state.corpses.get(corpseId));
    }

    private Optional<UnderworldSession> activeCorpseSession(CorpseRecord corpse) {
        if (corpse == null || corpse.victimId == null || corpse.victimId.isBlank()) return Optional.empty();
        UnderworldSession session = state.sessions.get(corpse.victimId);
        return session != null && corpse.corpseId.equals(session.corpseId) ? Optional.of(session) : Optional.empty();
    }

    private boolean protectedByUnderworldSession(CorpseRecord corpse) {
        return activeCorpseSession(corpse).isPresent();
    }

    private long protectedUntil(CorpseRecord corpse, long now) {
        return activeCorpseSession(corpse)
                .map(session -> now + Math.max(0L, session.remainingMillis))
                .orElse(0L);
    }

    private void markCorpsePublic(String corpseId, long now) {
        if (corpseId == null || corpseId.isBlank()) return;
        CorpseRecord corpse = state.corpses.get(corpseId);
        if (corpse == null) return;
        if (corpse.publicLootStartedAt <= 0L) {
            corpse.publicLootStartedAt = now;
            corpse.decaysAt = now + config.corpseExpiresMinutes() * 60_000L;
            dirty = true;
        } else if (corpse.decaysAt <= 0L) {
            corpse.decaysAt = corpse.publicLootStartedAt + config.corpseExpiresMinutes() * 60_000L;
            dirty = true;
        }
        expiryQueue.schedule(corpse.corpseId, corpse.decaysAt);
        queueTombDisplay(corpse.corpseId);
    }

    private void migrateCorpseLifecycle() {
        long now = System.currentTimeMillis();
        for (CorpseRecord corpse : state.corpses.values()) {
            if (corpse.selectedHotbarSlot < -1 || corpse.selectedHotbarSlot > 8) {
                corpse.selectedHotbarSlot = -1;
                dirty = true;
            }
            if (!protectedByUnderworldSession(corpse) && corpse.publicLootStartedAt <= 0L) {
                corpse.publicLootStartedAt = now;
                corpse.decaysAt = now + config.corpseExpiresMinutes() * 60_000L;
                dirty = true;
            } else if (corpse.publicLootStartedAt > 0L && corpse.decaysAt <= 0L) {
                corpse.decaysAt = corpse.publicLootStartedAt + config.corpseExpiresMinutes() * 60_000L;
                dirty = true;
            }
        }
    }

    public void sendPlayerToUnderworld(ServerPlayerEntity player, int minutes, ElarionDeathType type) {
        UnderworldSession session = new UnderworldSession();
        session.playerId = player.getUuidAsString();
        session.corpseId = "";
        session.startedAt = System.currentTimeMillis();
        session.remainingMillis = Math.max(1L, minutes) * 60_000L;
        session.deathType = type;
        state.sessions.put(session.playerId, session);
        dirty = true;
        sendToUnderworld(player, true);
        syncStatus(player);
        emit("player-sent-to-underworld", player.getUuid(), "", Map.of("deathType", type.name()));
    }

    public boolean returnPlayer(ServerPlayerEntity player) {
        UnderworldSession removed = state.sessions.remove(player.getUuidAsString());
        if (removed == null) {
            syncStatus(player);
            return false;
        }
        markCorpsePublic(removed.corpseId, System.currentTimeMillis());
        dirty = true;
        returnToLivingWorld(player, removed);
        syncStatus(player);
        emit("player-returned-from-underworld", player.getUuid(), "", Map.of("corpseId", removed.corpseId));
        return true;
    }

    public void forceReturnPlayer(ServerPlayerEntity player) {
        UnderworldSession removed = state.sessions.remove(player.getUuidAsString());
        dirty = true;
        if (removed == null) {
            api.realmSpawns().teleportAfterRealmAssignment(player);
            player.sendMessage(Text.literal("Returned to the living world.").formatted(Formatting.GREEN), false);
        } else {
            markCorpsePublic(removed.corpseId, System.currentTimeMillis());
            returnToLivingWorld(player, removed);
            emit("player-returned-from-underworld", player.getUuid(), "", Map.of("corpseId", removed.corpseId));
        }
        syncStatus(player);
    }

    public SoulState addFracture(ServerPlayerEntity player) {
        SoulState soul = state.souls.computeIfAbsent(player.getUuidAsString(), ignored -> {
            SoulState created = new SoulState();
            created.playerId = player.getUuidAsString();
            return created;
        });
        soul.fractures++;
        soul.lastFractureAt = System.currentTimeMillis();
        dirty = true;
        emit("soul-fractured", player.getUuid(), "", Map.of("fractures", Integer.toString(soul.fractures)));
        if (config.soulEnabled() && config.trueDeathAtMaxFractures()
                && soul.fractures >= config.maxFractures() && !soul.trueDeath) {
            soul.trueDeath = true;
            archiveTrueDeath(player, soul);
            api.characters().beginTrueDeath(player, "soul-fractures", Map.of(
                    "fractures", Integer.toString(soul.fractures)));
            recordTrueDeathChronicle(player, soul.fractures);
            emit("true-death", player.getUuid(), "", Map.of("fractures", Integer.toString(soul.fractures)));
            if (server != null) {
                server.getPlayerManager().broadcast(Text.literal(
                        player.getGameProfile().getName()
                                + "'s soul has shattered. Their character has suffered True Death.")
                        .formatted(Formatting.DARK_RED), false);
            }
        }
        syncStatus(player);
        return soul;
    }

    public SoulState removeFracture(ServerPlayerEntity player) {
        SoulState soul = state.souls.computeIfAbsent(player.getUuidAsString(), ignored -> {
            SoulState created = new SoulState();
            created.playerId = player.getUuidAsString();
            return created;
        });
        soul.fractures = Math.max(0, soul.fractures - 1);
        if (soul.fractures < config.maxFractures()) soul.trueDeath = false;
        soul.lastFractureAt = System.currentTimeMillis();
        dirty = true;
        syncStatus(player);
        return soul;
    }

    public void clearFractures(UUID playerId) {
        SoulState soul = state.souls.computeIfAbsent(playerId.toString(), ignored -> {
            SoulState created = new SoulState();
            created.playerId = playerId.toString();
            return created;
        });
        soul.fractures = 0;
        soul.trueDeath = false;
        soul.lastFractureAt = System.currentTimeMillis();
        dirty = true;
        syncStatus(playerId);
    }

    public void resetPlayer(UUID playerId) {
        state.sessions.remove(playerId.toString());
        state.souls.remove(playerId.toString());
        state.corpses.values().removeIf(corpse -> {
            if (!playerId.toString().equals(corpse.victimId)) return false;
            discardTomb(corpse);
            forgetCorpseRuntimeState(corpse.corpseId);
            return true;
        });
        dirty = true;
        save();
        syncStatus(playerId);
    }

    public void resetAll() {
        state.sessions.clear();
        state.souls.clear();
        for (CorpseRecord corpse : state.corpses.values()) discardTomb(corpse);
        state.corpses.clear();
        expiryQueue.clear();
        tombDisplayQueue.clear();
        queuedTombDisplays.clear();
        combatTags.clear();
        dirty = true;
        save();
        if (server != null) {
            server.getPlayerManager().getPlayerList().forEach(this::syncStatus);
        }
    }

    public void save() {
        if (server == null || !dirty) return;
        storage.save(server, state);
        dirty = false;
    }

    private Optional<PlayerRestrictionService.PlayerRestriction> restriction(
            ServerPlayerEntity player, String action
    ) {
        if (activeSession(player.getUuid()).isEmpty()) return Optional.empty();
        if (config.disableChat() && (PlayerRestrictionService.CHAT.equals(action)
                || PlayerRestrictionService.PRIVATE_MESSAGE.equals(action)
                || PlayerRestrictionService.GROUP_CHAT.equals(action))) {
            return Optional.of(new PlayerRestrictionService.PlayerRestriction(
                    "elarion_underworld", "Your soul cannot speak to the living from the Underworld."));
        }
        if (config.disablePortals() && PlayerRestrictionService.PORTAL_TRAVEL.equals(action)) {
            return Optional.of(new PlayerRestrictionService.PlayerRestriction(
                    "elarion_underworld", "Your soul cannot use portals from the Underworld."));
        }
        if (config.hideNameplates() && PlayerRestrictionService.NAMEPLATE.equals(action)) {
            return Optional.of(new PlayerRestrictionService.PlayerRestriction("elarion_underworld", ""));
        }
        return Optional.empty();
    }

    private void trackCombatTag(
            LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked
    ) {
        if (!config.combatTagEnabled() || !(entity instanceof ServerPlayerEntity victim)) return;
        UUID attacker = playerAttacker(source).map(ServerPlayerEntity::getUuid).orElse(null);
        if (attacker != null && !attacker.equals(victim.getUuid())) {
            combatTags.put(victim.getUuid(), new CombatTag(attacker, System.currentTimeMillis()));
        }
    }

    private boolean captureFatalDeath(LivingEntity entity, DamageSource source, float amount) {
        if (!config.enabled() || !(entity instanceof ServerPlayerEntity player)) return true;
        if (!appliesTo(player.getWorld())) return true;
        if (activeSession(player.getUuid()).isPresent() || isUnderworld(player.getWorld())) {
            captureUnderworldDeath(player);
            return true;
        }
        captureLivingDeath(player, source);
        return true;
    }

    private void captureLivingDeath(ServerPlayerEntity player, DamageSource source) {
        UUID killer = resolveKiller(player, source).orElse(null);
        ElarionDeathType type = deathType(player, source, killer);
        CorpseRecord corpse = buildCorpse(player, killer, type);
        state.corpses.put(corpse.corpseId, corpse);
        placeTomb(corpse, player.getHorizontalFacing().getOpposite());

        UnderworldSession session = new UnderworldSession();
        session.playerId = player.getUuidAsString();
        session.corpseId = corpse.corpseId;
        session.startedAt = System.currentTimeMillis();
        session.remainingMillis = timerMinutes(type) * 60_000L;
        session.deathType = type;
        session.wasAuthority = false;
        state.sessions.put(session.playerId, session);
        dirty = true;
        incrementLifetimeDeaths(player);
        recordDeathChronicle(player, corpse, type);
        clearInventory(player);
        emit("corpse-created", player.getUuid(), corpse.corpseId, Map.of("deathType", type.name()));
        emit("player-sent-to-underworld", player.getUuid(), corpse.corpseId, Map.of("deathType", type.name()));
        player.sendMessage(Text.literal("Your body falls in the living world. Your soul descends into the Underworld.")
                .formatted(Formatting.DARK_PURPLE), false);
    }

    private void captureUnderworldDeath(ServerPlayerEntity player) {
        clearInventory(player);
        UnderworldSession session = activeSession(player.getUuid()).orElse(null);
        if (session != null) {
            session.remainingMillis += config.extraMinutesPerUnderworldDeath() * 60_000L;
            dirty = true;
        }
        incrementLifetimeDeaths(player);
        addFracture(player);
        emit("underworld-death", player.getUuid(), session == null ? "" : session.corpseId, Map.of());
        player.sendMessage(Text.literal("Your soul fractures. Too many fractures will cause True Death.")
                .formatted(Formatting.RED), false);
    }

    private void incrementLifetimeDeaths(ServerPlayerEntity player) {
        api.playerStats().increment(player.getUuid(), LIFETIME_DEATHS_STAT, 1L);
    }

    private CorpseRecord buildCorpse(ServerPlayerEntity player, UUID killer, ElarionDeathType deathType) {
        CorpseRecord corpse = new CorpseRecord();
        corpse.corpseId = UUID.randomUUID().toString();
        corpse.victimId = player.getUuidAsString();
        corpse.victimName = playerDisplayName(player);
        corpse.killerId = killer == null ? "" : killer.toString();
        corpse.worldId = worldId(player.getWorld());
        corpse.victimRealmId = victimRealmId(player);
        corpse.tombstoneVariant = TombstoneVariant.forRealm(corpse.victimRealmId, corpse.corpseId).id();
        corpse.x = player.getX();
        corpse.y = player.getY();
        corpse.z = player.getZ();
        corpse.createdAt = System.currentTimeMillis();
        corpse.deathType = deathType;
        corpse.killerExclusiveUntil = corpse.createdAt + config.killerExclusiveSeconds() * 1000L;
        corpse.selectedHotbarSlot = player.getInventory().selectedSlot;

        List<StoredItemStack> protectedItems = captureStacks(player);
        List<StoredItemStack> pvpLoot = new ArrayList<>();
        if (deathType == ElarionDeathType.PVP && config.pvpLootEnabled()) {
            selectPvpLoot(protectedItems, pvpLoot);
        }
        corpse.protectedVictimItems = protectedItems;
        corpse.pvpLootItems = pvpLoot;
        return corpse;
    }

    private List<StoredItemStack> captureStacks(ServerPlayerEntity player) {
        List<StoredItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < MAIN_INVENTORY_SIZE; slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isEmpty()) {
                stacks.add(StoredItemStack.fromInventory(stack.copy(), server.getRegistryManager(), slot));
            }
        }
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack stack = player.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                stacks.add(StoredItemStack.fromArmor(stack.copy(), server.getRegistryManager(), slot.getName()));
            }
        }
        ItemStack offhand = player.getOffHandStack();
        if (!offhand.isEmpty()) {
            stacks.add(StoredItemStack.fromOffhand(offhand.copy(), server.getRegistryManager()));
        }
        return stacks;
    }

    private void selectPvpLoot(List<StoredItemStack> protectedItems, List<StoredItemStack> pvpLoot) {
        if (config.armorDrops()) {
            Iterator<StoredItemStack> iterator = protectedItems.iterator();
            while (iterator.hasNext()) {
                StoredItemStack stored = iterator.next();
                ItemStack stack = stored.toStack(server.getRegistryManager());
                if (stack.getItem() instanceof net.minecraft.item.ArmorItem && !excluded(stack)) {
                    pvpLoot.add(stored);
                    iterator.remove();
                }
            }
        }
        takePhysicalCurrency(protectedItems, pvpLoot);
        List<StoredItemStack> eligible = new ArrayList<>();
        for (StoredItemStack stored : protectedItems) {
            ItemStack stack = stored.toStack(server.getRegistryManager());
            if (!excluded(stack) && !physicalCurrency(stack)) eligible.add(stored);
        }
        Collections.shuffle(eligible);
        int max = Math.max(config.randomItemMin(), config.randomItemMax());
        int min = Math.min(config.randomItemMin(), config.randomItemMax());
        int desired = eligible.isEmpty() ? 0 : Math.min(eligible.size(), min + (int) (Math.random() * (max - min + 1)));
        for (int i = 0; i < desired; i++) {
            StoredItemStack selected = eligible.get(i);
            pvpLoot.add(selected);
            protectedItems.remove(selected);
        }
    }

    private void takePhysicalCurrency(List<StoredItemStack> protectedItems, List<StoredItemStack> pvpLoot) {
        int total = protectedItems.stream()
                .map(stored -> stored.toStack(server.getRegistryManager()))
                .filter(this::physicalCurrency)
                .mapToInt(ItemStack::getCount)
                .sum();
        if (total <= 0) return;
        int toTake = Math.max(1, (int) Math.floor(total * config.physicalCurrencyPercent()));
        Iterator<StoredItemStack> iterator = protectedItems.iterator();
        while (iterator.hasNext() && toTake > 0) {
            StoredItemStack stored = iterator.next();
            ItemStack stack = stored.toStack(server.getRegistryManager());
            if (!physicalCurrency(stack)) continue;
            int taken = Math.min(toTake, stack.getCount());
            ItemStack split = stack.copy();
            split.setCount(taken);
            pvpLoot.add(StoredItemStack.from(split, server.getRegistryManager(), "", -1, ""));
            stack.decrement(taken);
            toTake -= taken;
            if (stack.isEmpty()) iterator.remove();
            else {
                stored.count = stack.getCount();
                StoredItemStack remaining = StoredItemStack.from(stack, server.getRegistryManager(),
                        stored.sourceType, stored.slotIndex, stored.equipmentSlot);
                remaining.sourceId = stored.sourceId;
                remaining.sourceLabel = stored.sourceLabel;
                stored.stackNbt = remaining.stackNbt;
            }
        }
    }

    private void clearInventory(ServerPlayerEntity player) {
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            player.getInventory().setStack(slot, ItemStack.EMPTY);
        }
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            player.equipStack(slot, ItemStack.EMPTY);
        }
    }

    private void recoverOrLoot(ServerPlayerEntity player, CorpseRecord corpse) {
        UUID playerId = player.getUuid();
        UUID victimId = UUID.fromString(corpse.victimId);
        boolean victim = playerId.equals(victimId);
        boolean killer = !corpse.killerId.isBlank() && playerId.equals(UUID.fromString(corpse.killerId));
        boolean protectedSession = protectedByUnderworldSession(corpse);
        boolean publicLoot = !protectedSession && corpse.publicLootStartedAt > 0L;
        if (publicLoot && !victim) {
            corpse.protectedVictimItems = transferStored(player, corpse.protectedVictimItems, false, -1);
            corpse.pvpLootItems = transferStored(player, corpse.pvpLootItems, false, -1);
            corpse.victimRecovered = corpse.protectedVictimItems.isEmpty();
            corpse.pvpLootClaimed = corpse.pvpLootItems.isEmpty();
            dirty = true;
            save();
            player.sendMessage(Text.literal("You looted the public tomb.").formatted(Formatting.GOLD), false);
            cleanupIfEmpty(corpse);
            return;
        }
        if (killer && !corpse.pvpLootClaimed && !corpse.pvpLootItems.isEmpty()) {
            List<StoredItemStack> remaining = transferStored(player, corpse.pvpLootItems, false, -1);
            corpse.pvpLootItems = remaining;
            corpse.pvpLootClaimed = remaining.isEmpty();
            dirty = true;
            save();
            player.sendMessage(Text.literal("You claimed the PvP loot from this corpse.").formatted(Formatting.GOLD), false);
            emit("pvp-loot-claimed", player.getUuid(), corpse.corpseId, Map.of("victim", corpse.victimId));
            cleanupIfEmpty(corpse);
            return;
        }
        if (victim && !corpse.victimRecovered) {
            if (!corpse.pvpLootItems.isEmpty() && System.currentTimeMillis() < corpse.killerExclusiveUntil) {
                player.sendMessage(Text.literal("Your protected items can be recovered, but the killer still has exclusive PvP loot access.")
                        .formatted(Formatting.YELLOW), false);
            }
            List<StoredItemStack> remaining = transferStored(player, corpse.protectedVictimItems, true, corpse.selectedHotbarSlot);
            corpse.protectedVictimItems = remaining;
            corpse.victimRecovered = remaining.isEmpty();
            dirty = true;
            save();
            player.sendMessage(Text.literal("You recovered your protected corpse items.").formatted(Formatting.GREEN), false);
            emit("corpse-recovered", player.getUuid(), corpse.corpseId, Map.of());
            cleanupIfEmpty(corpse);
            return;
        }
        if (victim && corpse.expiredToRecovery) {
            player.sendMessage(Text.literal("This corpse expired into recovery storage; use /e death corpse recover if needed.")
                    .formatted(Formatting.YELLOW), false);
            return;
        }
        player.sendMessage(Text.literal("You cannot recover anything from this corpse.").formatted(Formatting.RED), false);
    }

    public void recoverFromUi(ServerPlayerEntity player, String corpseId) {
        CorpseRecord corpse = state.corpses.get(corpseId);
        if (corpse == null || !inRange(player, corpse)) {
            openMessage(player, corpseId, "Grave unavailable", "The grave is missing or out of range.", true);
            return;
        }
        recoverOrLoot(player, corpse);
        CorpseRecord updated = state.corpses.get(corpseId);
        if (updated == null) {
            openMessage(player, corpseId, "Grave recovered", "All available items were recovered.", false);
        } else {
            openCorpse(player, updated, "Inventory full. Unmoved items remain in the grave.");
        }
    }

    private void openCorpse(ServerPlayerEntity player, CorpseRecord corpse, String feedback) {
        if (!inRange(player, corpse)) {
            openMessage(player, corpse.corpseId, "Grave unavailable", "Move closer to the grave.", true);
            return;
        }
        boolean victim = player.getUuidAsString().equals(corpse.victimId);
        boolean killer = player.getUuidAsString().equals(corpse.killerId);
        boolean protectedSession = protectedByUnderworldSession(corpse);
        boolean publicLoot = !protectedSession && corpse.publicLootStartedAt > 0L;
        List<StoredItemStack> visible;
        String title = "Grave of " + corpseOwnerName(corpse);
        if (victim && !corpse.protectedVictimItems.isEmpty()) {
            visible = corpse.protectedVictimItems;
        } else if (killer && !corpse.pvpLootItems.isEmpty()) {
            visible = corpse.pvpLootItems;
        } else if (publicLoot && (!corpse.protectedVictimItems.isEmpty() || !corpse.pvpLootItems.isEmpty())) {
            visible = new ArrayList<>();
            visible.addAll(corpse.protectedVictimItems);
            visible.addAll(corpse.pvpLootItems);
        } else {
            openMessage(player, corpse.corpseId, "Grave", "Nothing in this grave belongs to you.", true);
            return;
        }
        long now = System.currentTimeMillis();
        String accessState = publicLoot ? ACCESS_LOOTABLE : victim ? ACCESS_OWNER : killer ? ACCESS_KILLER : ACCESS_PROTECTED;
        List<GraveOpenPayload.Entry> entries = visible.stream()
                .limit(256)
                .map(stack -> new GraveOpenPayload.Entry(stack.itemId, stack.count,
                        stack.stackNbt == null ? "" : stack.stackNbt,
                        stack.sourceType,
                        sourceId(stack),
                        sourceLabel(stack),
                        stack.slotIndex,
                        stack.equipmentSlot))
                .toList();
        ServerPlayNetworking.send(player, new GraveOpenPayload(
                corpse.corpseId, title, feedback.isBlank() ? defaultGraveBody(accessState) : feedback,
                corpseOwnerName(corpse),
                !feedback.isBlank(), accessState, protectedUntil(corpse, now), corpse.publicLootStartedAt,
                corpse.decaysAt, totalItemCount(visible), entries));
    }

    private String corpseOwnerName(CorpseRecord corpse) {
        if (corpse == null) return "Unknown";
        if (corpse.victimName != null && !corpse.victimName.isBlank()) return corpse.victimName;
        if (corpse.victimId != null && !corpse.victimId.isBlank()) return corpse.victimId;
        return "Unknown";
    }

    private String playerDisplayName(ServerPlayerEntity player) {
        return api.citizens().find(player.getUuid())
                .map(citizen -> citizen.nickname() == null || citizen.nickname().isBlank()
                        ? citizen.lastKnownUsername()
                        : citizen.nickname())
                .filter(name -> name != null && !name.isBlank())
                .orElse(player.getGameProfile().getName());
    }

    private String sourceId(StoredItemStack stack) {
        if (stack.sourceId != null && !stack.sourceId.isBlank()) return stack.sourceId;
        return StoredItemStack.defaultSourceId(stack.sourceType);
    }

    private String sourceLabel(StoredItemStack stack) {
        if (stack.sourceLabel != null && !stack.sourceLabel.isBlank()) return stack.sourceLabel;
        return StoredItemStack.defaultSourceLabel(stack.sourceType, stack.slotIndex, stack.equipmentSlot);
    }

    private String defaultGraveBody(String accessState) {
        return switch (accessState) {
            case ACCESS_LOOTABLE -> "Protection has ended. Remaining items can be looted until the tomb decays.";
            case ACCESS_KILLER -> "Killer-exclusive PvP loot is available.";
            case ACCESS_OWNER -> "Recover All restores original empty slots first, then uses inventory space.";
            default -> "Items remain server protected until the soul returns.";
        };
    }

    private void openMessage(ServerPlayerEntity player, String corpseId, String title, String body, boolean error) {
        ServerPlayNetworking.send(player, new GraveOpenPayload(corpseId, title, body, "",
                error, ACCESS_MESSAGE, 0L, 0L, 0L, 0, List.of()));
    }

    private boolean inRange(ServerPlayerEntity player, CorpseRecord corpse) {
        return worldId(player.getWorld()).equals(corpse.worldId)
                && player.squaredDistanceTo(corpse.x, corpse.y, corpse.z) <= 64.0D;
    }

    private List<StoredItemStack> transferStored(
            ServerPlayerEntity player,
            List<StoredItemStack> stored,
            boolean restoreOriginalSlots,
            int selectedHotbarSlot
    ) {
        List<StoredItemStack> remaining = new ArrayList<>();
        for (StoredItemStack storedStack : stored) {
            ItemStack stack = storedStack.toStack(server.getRegistryManager());
            if (stack.isEmpty()) continue;
            if (restoreOriginalSlots) {
                tryOriginalSlot(player, storedStack, stack);
            }
            player.getInventory().insertStack(stack);
            if (!stack.isEmpty()) {
                remaining.add(StoredItemStack.from(stack, server.getRegistryManager(),
                        storedStack.sourceType, storedStack.slotIndex, storedStack.equipmentSlot));
                StoredItemStack kept = remaining.getLast();
                kept.sourceId = storedStack.sourceId;
                kept.sourceLabel = storedStack.sourceLabel;
            }
        }
        if (restoreOriginalSlots && selectedHotbarSlot >= 0 && selectedHotbarSlot < 9) {
            player.getInventory().selectedSlot = selectedHotbarSlot;
        }
        return remaining;
    }

    private void tryOriginalSlot(ServerPlayerEntity player, StoredItemStack storedStack, ItemStack stack) {
        if (stack.isEmpty()) return;
        String source = storedStack.sourceType == null ? "" : storedStack.sourceType;
        if (StoredItemStack.SOURCE_INVENTORY.equals(source)) {
            if (storedStack.slotIndex >= 0 && storedStack.slotIndex < MAIN_INVENTORY_SIZE) {
                mergeIntoSlot(player.getInventory().getStack(storedStack.slotIndex), stack,
                        updated -> player.getInventory().setStack(storedStack.slotIndex, updated));
            }
            return;
        }
        if (StoredItemStack.SOURCE_OFFHAND.equals(source)) {
            mergeIntoSlot(player.getOffHandStack(), stack, updated -> player.equipStack(EquipmentSlot.OFFHAND, updated));
            return;
        }
        if (StoredItemStack.SOURCE_ARMOR.equals(source)) {
            EquipmentSlot slot = equipmentSlot(storedStack.equipmentSlot);
            if (slot != null) {
                mergeIntoSlot(player.getEquippedStack(slot), stack, updated -> player.equipStack(slot, updated));
            }
        }
    }

    private void mergeIntoSlot(ItemStack existing, ItemStack incoming, java.util.function.Consumer<ItemStack> setter) {
        if (incoming.isEmpty()) return;
        if (existing.isEmpty()) {
            setter.accept(incoming.copy());
            incoming.setCount(0);
            return;
        }
        if (!ItemStack.areItemsAndComponentsEqual(existing, incoming)) return;
        int limit = Math.min(existing.getMaxCount(), incoming.getMaxCount());
        int moved = Math.min(incoming.getCount(), Math.max(0, limit - existing.getCount()));
        if (moved <= 0) return;
        existing.increment(moved);
        incoming.decrement(moved);
    }

    private EquipmentSlot equipmentSlot(String name) {
        if (name == null) return null;
        return switch (name) {
            case "head" -> EquipmentSlot.HEAD;
            case "chest" -> EquipmentSlot.CHEST;
            case "legs" -> EquipmentSlot.LEGS;
            case "feet" -> EquipmentSlot.FEET;
            case "offhand" -> EquipmentSlot.OFFHAND;
            default -> null;
        };
    }

    private int totalItemCount(List<StoredItemStack> items) {
        int total = 0;
        for (StoredItemStack item : items) {
            total += Math.max(0, item.count);
        }
        return total;
    }

    public void adminRecover(String corpseId, ServerPlayerEntity player) {
        CorpseRecord corpse = state.corpses.get(corpseId);
        if (corpse == null) throw new IllegalArgumentException("Unknown corpse: " + corpseId);
        corpse.protectedVictimItems = transferStored(player, corpse.protectedVictimItems, true, corpse.selectedHotbarSlot);
        corpse.pvpLootItems = transferStored(player, corpse.pvpLootItems, false, -1);
        corpse.victimRecovered = corpse.protectedVictimItems.isEmpty();
        corpse.pvpLootClaimed = corpse.pvpLootItems.isEmpty();
        dirty = true;
        cleanupIfEmpty(corpse);
        save();
    }

    private void cleanupIfEmpty(CorpseRecord corpse) {
        if (!corpse.protectedVictimItems.isEmpty() || !corpse.pvpLootItems.isEmpty()) {
            syncTombDisplay(corpse, System.currentTimeMillis());
            return;
        }
        discardTomb(corpse);
        state.corpses.remove(corpse.corpseId);
        forgetCorpseRuntimeState(corpse.corpseId);
        dirty = true;
        save();
    }

    private void placeTomb(CorpseRecord corpse, Direction facing) {
        world(corpse.worldId).ifPresent(world -> {
            if (tombs.place(world, corpse, facing)) {
                dirty = true;
                syncTombDisplay(corpse, System.currentTimeMillis());
            }
        });
    }

    private void reconcileGraves() {
        for (CorpseRecord corpse : state.corpses.values()) {
            if (corpse.expiredToRecovery) continue;
            if (corpse.victimRealmId == null) corpse.victimRealmId = "";
            if (corpse.tombstoneVariant == null || corpse.tombstoneVariant.isBlank()) {
                corpse.tombstoneVariant = TombstoneVariant.forRealm(corpse.victimRealmId, corpse.corpseId).id();
                dirty = true;
            }
            ServerWorld world = world(corpse.worldId).orElse(null);
            if (world == null) continue;
            if (!tombs.valid(world, corpse)) {
                discardTomb(corpse);
                placeTomb(corpse, Direction.NORTH);
            } else {
                syncTombDisplay(corpse, System.currentTimeMillis());
            }
        }
    }

    private void discardTomb(CorpseRecord corpse) {
        world(corpse.worldId).ifPresent(world -> tombs.remove(world, corpse));
    }

    private void rebuildRuntimeIndexes() {
        expiryQueue.clear();
        tombDisplayQueue.clear();
        queuedTombDisplays.clear();
        for (CorpseRecord corpse : state.corpses.values()) {
            queueTombDisplay(corpse.corpseId);
            if (!protectedByUnderworldSession(corpse) && corpse.decaysAt > 0L) {
                expiryQueue.schedule(corpse.corpseId, corpse.decaysAt);
            }
        }
    }

    private void queueTombDisplay(String corpseId) {
        if (corpseId == null || corpseId.isBlank() || !queuedTombDisplays.add(corpseId)) return;
        tombDisplayQueue.addLast(corpseId);
    }

    private void flushTombDisplays() {
        long now = System.currentTimeMillis();
        int processed = 0;
        while (processed++ < MAX_TOMB_DISPLAY_UPDATES_PER_SECOND && !tombDisplayQueue.isEmpty()) {
            String corpseId = tombDisplayQueue.removeFirst();
            queuedTombDisplays.remove(corpseId);
            syncTombDisplay(state.corpses.get(corpseId), now);
        }
    }

    private void forgetCorpseRuntimeState(String corpseId) {
        expiryQueue.cancel(corpseId);
        queuedTombDisplays.remove(corpseId);
    }

    private void syncTombDisplay(CorpseRecord corpse, long now) {
        if (corpse == null || !corpse.hasTombPosition()) return;
        world(corpse.worldId).ifPresent(world -> {
            BlockPos origin = corpse.tombOrigin();
            if (world.getBlockEntity(origin) instanceof UnderworldTombBlockEntity tomb) {
                boolean protectedSession = protectedByUnderworldSession(corpse);
                String access = protectedSession ? ACCESS_PROTECTED : ACCESS_LOOTABLE;
                tomb.setDisplay(corpseOwnerName(corpse), access, protectedUntil(corpse, now), corpse.publicLootStartedAt,
                        corpse.decaysAt, totalItemCount(corpse.protectedVictimItems) + totalItemCount(corpse.pvpLootItems));
            }
        });
    }

    private String victimRealmId(ServerPlayerEntity player) {
        String citizenRealm = api.citizens().find(player.getUuid())
                .map(citizen -> citizen.realmId())
                .orElse("");
        if (!citizenRealm.isBlank()) return citizenRealm;
        return api.realms().ownerForWorld(worldId(player.getWorld()))
                .map(realm -> realm.id())
                .orElse("");
    }

    private void sendToUnderworld(ServerPlayerEntity player, boolean message) {
        ServerWorld world = world(config.worldId()).orElse(null);
        if (world == null) {
            player.sendMessage(Text.literal("Underworld world is unavailable: " + config.worldId())
                    .formatted(Formatting.RED), false);
            return;
        }
        player.teleport(world, config.spawnX(), config.spawnY(), config.spawnZ(), Set.of(), player.getYaw(), player.getPitch());
        syncStatus(player);
        if (message) {
            player.sendMessage(Text.literal("Your soul descends into the Underworld.").formatted(Formatting.DARK_PURPLE), false);
        }
    }

    private void returnToLivingWorld(ServerPlayerEntity player, UnderworldSession session) {
        CorpseRecord corpse = state.corpses.get(session.corpseId);
        if (corpse != null) {
            world(corpse.worldId).ifPresentOrElse(world ->
                            player.teleport(world, corpse.x + 0.5D, corpse.y, corpse.z + 0.5D, Set.of(), player.getYaw(), player.getPitch()),
                    () -> api.realmSpawns().teleportAfterRealmAssignment(player));
        } else {
            api.realmSpawns().teleportAfterRealmAssignment(player);
        }
        player.sendMessage(Text.literal("Your soul returns to the living world.").formatted(Formatting.GREEN), false);
    }

    private void tickSessions() {
        long step = 1000L;
        List<UUID> toReturn = new ArrayList<>();
        for (UnderworldSession session : state.sessions.values()) {
            queueTombDisplay(session.corpseId);
            if (session.paused) continue;
            session.remainingMillis = Math.max(0L, session.remainingMillis - step);
            if (session.remainingMillis <= 0L) toReturn.add(UUID.fromString(session.playerId));
        }
        if (!toReturn.isEmpty()) dirty = true;
        for (UnderworldSession session : state.sessions.values()) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(UUID.fromString(session.playerId));
            if (player != null) syncStatus(player);
        }
        for (UUID playerId : toReturn) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            if (player != null) returnPlayer(player);
        }
    }

    private void expireDueCorpses() {
        long now = System.currentTimeMillis();
        for (String corpseId : expiryQueue.pollDue(now, MAX_CORPSE_EXPIRATIONS_PER_SECOND)) {
            CorpseRecord corpse = state.corpses.get(corpseId);
            if (corpse == null) continue;
            if (protectedByUnderworldSession(corpse)) {
                expiryQueue.schedule(corpse.corpseId, now + PROTECTED_EXPIRY_RETRY_MILLIS);
                continue;
            }
            if (corpse.publicLootStartedAt <= 0L) {
                markCorpsePublic(corpse.corpseId, now);
                continue;
            }
            if (corpse.decaysAt <= 0L) {
                corpse.decaysAt = corpse.publicLootStartedAt + config.corpseExpiresMinutes() * 60_000L;
                dirty = true;
                expiryQueue.schedule(corpse.corpseId, corpse.decaysAt);
                continue;
            }
            if (now < corpse.decaysAt) {
                expiryQueue.schedule(corpse.corpseId, corpse.decaysAt);
                continue;
            }
            List<StoredItemStack> vault = state.recoveryVaults.computeIfAbsent(
                    corpse.victimId, ignored -> new ArrayList<>());
            vault.addAll(corpse.protectedVictimItems);
            vault.addAll(corpse.pvpLootItems);
            discardTomb(corpse);
            state.corpses.remove(corpse.corpseId);
            forgetCorpseRuntimeState(corpse.corpseId);
            dirty = true;
        }
    }

    public int recoverVault(ServerPlayerEntity player) {
        List<StoredItemStack> stored = state.recoveryVaults.get(player.getUuidAsString());
        if (stored == null || stored.isEmpty()) return 0;
        int before = stored.size();
        List<StoredItemStack> remaining = transferStored(player, stored, false, -1);
        if (remaining.isEmpty()) state.recoveryVaults.remove(player.getUuidAsString());
        else state.recoveryVaults.put(player.getUuidAsString(), remaining);
        dirty = true;
        save();
        return before - remaining.size();
    }

    private void resetCharacter(UUID accountId) {
        String id = accountId.toString();
        state.sessions.remove(id);
        state.souls.remove(id);
        state.recoveryVaults.remove(id);
        state.corpses.values().removeIf(corpse -> {
            if (!id.equals(corpse.victimId)) return false;
            discardTomb(corpse);
            forgetCorpseRuntimeState(corpse.corpseId);
            return true;
        });
        combatTags.remove(accountId);
        dirty = true;
        save();
        syncStatus(accountId);
    }

    private Optional<ServerPlayerEntity> playerAttacker(DamageSource source) {
        Entity attacker = source.getAttacker();
        if (attacker instanceof ServerPlayerEntity player) return Optional.of(player);
        Entity direct = source.getSource();
        if (direct instanceof ServerPlayerEntity player) return Optional.of(player);
        return Optional.empty();
    }

    private Optional<UUID> resolveKiller(ServerPlayerEntity player, DamageSource source) {
        Optional<ServerPlayerEntity> direct = playerAttacker(source);
        if (direct.isPresent() && !direct.get().getUuid().equals(player.getUuid())) {
            return direct.map(ServerPlayerEntity::getUuid);
        }
        CombatTag tag = combatTags.get(player.getUuid());
        if (tag == null) return Optional.empty();
        long age = System.currentTimeMillis() - tag.lastHitAt();
        return age <= config.combatTagDurationSeconds() * 1000L ? Optional.of(tag.attackerId()) : Optional.empty();
    }

    private ElarionDeathType deathType(ServerPlayerEntity player, DamageSource source, UUID killer) {
        if (selfInflicted(player, source) && killer == null) return ElarionDeathType.SUICIDE;
        if (killer != null) return ElarionDeathType.PVP;
        if (player.getY() < player.getWorld().getBottomY() - 8) return ElarionDeathType.VOID;
        return ElarionDeathType.PVE;
    }

    private boolean selfInflicted(ServerPlayerEntity player, DamageSource source) {
        Entity attacker = source.getAttacker();
        Entity direct = source.getSource();
        return attacker != null && attacker.getUuid().equals(player.getUuid())
                || direct != null && direct.getUuid().equals(player.getUuid());
    }

    private void recordDeathChronicle(ServerPlayerEntity player, CorpseRecord corpse, ElarionDeathType type) {
        String eventType = switch (type) {
            case PVP -> "death-pvp";
            case SUICIDE -> "death-suicide";
            case VOID -> "death-void";
            default -> "death-pve";
        };
        Map<String, String> metadata = new java.util.LinkedHashMap<>();
        metadata.put("deathType", type.name());
        metadata.put("corpseId", corpse.corpseId);
        if (corpse.killerId != null && !corpse.killerId.isBlank()) {
            metadata.put("killer", corpse.killerId);
        }
        recordUnderworldChronicle(player, eventType, "corpse", corpse.corpseId, metadata,
                player.getGameProfile().getName() + " died and was sent to the Underworld.");
    }

    private void recordTrueDeathChronicle(ServerPlayerEntity player, int fractures) {
        recordUnderworldChronicle(player, "true-death", "player", player.getUuidAsString(),
                Map.of("fractures", Integer.toString(fractures)),
                player.getGameProfile().getName() + " suffered True Death.");
    }

    private void recordUnderworldChronicle(
            ServerPlayerEntity player,
            String type,
            String subjectType,
            String subjectId,
            Map<String, String> metadata,
            String fallbackText
    ) {
        try {
            api.history().recordChronicle("underworld", type, player.getUuid(), subjectType, subjectId,
                    api.citizens().find(player.getUuid()).map(citizen -> citizen.realmId()).orElse(""),
                    metadata, fallbackText);
        } catch (RuntimeException exception) {
            logger.warn("Failed to record Underworld Chronicle event {} for {}", type,
                    player.getGameProfile().getName(), exception);
        }
    }

    private long timerMinutes(ElarionDeathType type) {
        return switch (type) {
            case PVP -> config.pvpTimerMinutes();
            case ADMIN, COMMAND -> 1;
            default -> config.pveTimerMinutes();
        };
    }

    private boolean physicalCurrency(ItemStack stack) {
        Identifier id = net.minecraft.registry.Registries.ITEM.getId(stack.getItem());
        if (config.physicalCurrencyItemIds().contains(id.toString())) return true;
        for (String tagId : config.physicalCurrencyTags()) {
            Identifier parsed = Identifier.tryParse(tagId);
            if (parsed != null && stack.isIn(TagKey.of(RegistryKeys.ITEM, parsed))) return true;
        }
        return false;
    }

    private boolean excluded(ItemStack stack) {
        Identifier id = net.minecraft.registry.Registries.ITEM.getId(stack.getItem());
        if (config.excludedItemIds().contains(id.toString())) return true;
        for (String tagId : config.excludedItemTags()) {
            Identifier parsed = Identifier.tryParse(tagId);
            if (parsed != null && stack.isIn(TagKey.of(RegistryKeys.ITEM, parsed))) return true;
        }
        return false;
    }

    private boolean appliesTo(World world) {
        String id = worldId(world);
        if (config.excludedWorlds().contains(id)) return false;
        return config.enabledWorlds().contains("*") || config.enabledWorlds().contains(id);
    }

    private boolean isUnderworld(World world) {
        return worldId(world).equals(config.worldId());
    }

    private Optional<ServerWorld> world(String id) {
        if (server == null) return Optional.empty();
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null) return Optional.empty();
        return Optional.ofNullable(server.getWorld(RegistryKey.of(RegistryKeys.WORLD, identifier)));
    }

    private static String worldId(World world) {
        return world.getRegistryKey().getValue().toString();
    }

    private void archiveTrueDeath(ServerPlayerEntity player, SoulState soul) {
        if (server == null) return;
        Path archive = storage.root(server)
                .resolve("true-death-archive")
                .resolve(player.getUuidAsString() + "-" + System.currentTimeMillis() + ".txt");
        try {
            Files.createDirectories(archive.getParent());
            Files.writeString(archive,
                    "player=" + player.getUuidAsString() + "\n"
                            + "name=" + player.getGameProfile().getName() + "\n"
                            + "fractures=" + soul.fractures + "\n"
                            + "happenedAt=" + Instant.now() + "\n",
                    StandardCharsets.UTF_8);
        } catch (IOException exception) {
            logger.error("Failed to archive true death for {}", player.getGameProfile().getName(), exception);
        }
    }

    private void emit(String type, UUID actor, String subjectId, Map<String, String> metadata) {
        api.system().events().emitDomainEvent(ElarionDomainEvent.of(
                "elarion_underworld",
                type,
                actor,
                api.citizens().find(actor).map(citizen -> citizen.realmId()).orElse(""),
                "underworld",
                subjectId == null ? "" : subjectId,
                metadata));
    }

    private void syncStatus(UUID playerId) {
        if (server == null) return;
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
        if (player != null) syncStatus(player);
    }

    private void syncStatus(ServerPlayerEntity player) {
        UnderworldSession session = activeSession(player.getUuid()).orElse(null);
        SoulState soul = soul(player.getUuid()).orElse(null);
        UnderworldStatusSyncPayload payload = new UnderworldStatusSyncPayload(
                session != null,
                session == null ? 0L : session.remainingMillis,
                session == null || session.deathType == null ? "" : session.deathType.name(),
                soul == null ? 0 : soul.fractures,
                config.maxFractures(),
                soul != null && soul.trueDeath);
        ServerPlayNetworking.send(player, payload);
    }
}
