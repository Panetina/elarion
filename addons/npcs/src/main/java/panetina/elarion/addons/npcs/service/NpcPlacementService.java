package panetina.elarion.addons.npcs.service;

import net.minecraft.entity.Entity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.slf4j.Logger;
import panetina.elarion.addons.npcs.entity.ElarionNpcEntities;
import panetina.elarion.addons.npcs.entity.ElarionNpcEntity;
import panetina.elarion.addons.npcs.model.NpcDefinition;
import panetina.elarion.addons.npcs.model.NpcPortraitProfile;
import panetina.elarion.addons.npcs.model.NpcSkinProfile;
import panetina.elarion.addons.npcs.model.PlacedNpcRecord;
import panetina.elarion.addons.npcs.network.NpcVisualSyncPayload;
import panetina.elarion.addons.npcs.storage.NpcPlacementStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class NpcPlacementService {
    private final Logger logger;
    private final NpcDefinitionService definitions;
    private final NpcPlacementStorage storage;
    private final Map<UUID, PlacedNpcRecord> placed = new LinkedHashMap<>();
    private final Set<UUID> reconciling = new HashSet<>();
    private MinecraftServer server;
    private boolean bound;
    private Consumer<UUID> removalListener = ignored -> {
    };

    public NpcPlacementService(Logger logger, NpcDefinitionService definitions, NpcPlacementStorage storage) {
        this.logger = logger;
        this.definitions = definitions;
        this.storage = storage;
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        this.bound = false;
        placed.clear();
        storage.load(server).values().forEach(record ->
                placed.put(record.id(), ensureHandle(record)));
        this.bound = true;
    }

    public void shutdown() {
        save();
        bound = false;
    }

    public Optional<MinecraftServer> server() {
        return Optional.ofNullable(server);
    }

    public void onRemoved(Consumer<UUID> listener) {
        removalListener = listener == null ? ignored -> {
        } : listener;
    }

    public Collection<PlacedNpcRecord> all() {
        return placed.values().stream()
                .sorted(Comparator.comparing(record -> record.id().toString()))
                .toList();
    }

    public Optional<PlacedNpcRecord> find(UUID id) {
        return Optional.ofNullable(placed.get(id));
    }

    public Optional<PlacedNpcRecord> find(String idOrHandle) {
        if (idOrHandle == null || idOrHandle.isBlank()) return Optional.empty();
        try {
            UUID id = UUID.fromString(idOrHandle);
            return find(id);
        } catch (IllegalArgumentException ignored) {
            return placed.values().stream()
                    .filter(record -> idOrHandle.equalsIgnoreCase(record.commandId()))
                    .findFirst();
        }
    }

    public Optional<PlacedNpcRecord> byEntity(UUID entityId) {
        return placed.values().stream()
                .filter(record -> entityId.equals(record.entityId()))
                .findFirst();
    }

    public NpcVisualSyncPayload visualSyncPayload() {
        return new NpcVisualSyncPayload(placed.values().stream()
                .map(this::visualEntry)
                .flatMap(Optional::stream)
                .toList());
    }

    public PlacedNpcRecord place(ServerPlayerEntity creator, String definitionId) {
        return place(creator, definitionId, creator.getYaw());
    }

    public PlacedNpcRecord place(ServerPlayerEntity creator, String definitionId, float yaw) {
        NpcDefinition definition = definitions.npc(definitionId)
                .filter(NpcDefinition::enabled)
                .orElseThrow(() -> new IllegalArgumentException("Unknown enabled NPC definition: " + definitionId));
        PlacedNpcRecord record = new PlacedNpcRecord(
                UUID.randomUUID(),
                nextHandle(definition.id()),
                definition.id(),
                null,
                creator.getWorld().getRegistryKey().getValue().toString(),
                creator.getX(),
                creator.getY(),
                creator.getZ(),
                yaw,
                0.0F,
                "",
                "",
                "",
                "",
                creator.getUuid(),
                System.currentTimeMillis());
        placed.put(record.id(), record);
        record = reconcile(record).record();
        placed.put(record.id(), record);
        save();
        broadcastVisuals();
        return record;
    }

    public Optional<PlacedNpcRecord> duplicate(String idOrHandle, ServerPlayerEntity creator, float yaw) {
        PlacedNpcRecord source = find(idOrHandle).orElse(null);
        if (source == null) return Optional.empty();
        PlacedNpcRecord copy = new PlacedNpcRecord(
                UUID.randomUUID(),
                nextHandle(source.definitionId()),
                source.definitionId(),
                null,
                creator.getWorld().getRegistryKey().getValue().toString(),
                creator.getX(),
                creator.getY(),
                creator.getZ(),
                yaw,
                0.0F,
                source.displayNameOverride(),
                source.skinOverride(),
                source.portraitOverride(),
                source.dialogueOverride(),
                creator.getUuid(),
                System.currentTimeMillis());
        placed.put(copy.id(), copy);
        copy = reconcile(copy).record();
        placed.put(copy.id(), copy);
        save();
        broadcastVisuals();
        return Optional.of(copy);
    }

    public boolean remove(String idOrHandle) {
        PlacedNpcRecord target = find(idOrHandle).orElse(null);
        if (target == null) return false;
        PlacedNpcRecord record = placed.remove(target.id());
        if (record == null) return false;
        discardAllAnchors(record);
        removalListener.accept(record.id());
        save();
        broadcastVisuals();
        return true;
    }

    public Optional<PlacedNpcRecord> move(String idOrHandle, ServerPlayerEntity player) {
        PlacedNpcRecord current = find(idOrHandle).orElse(null);
        if (current == null) return Optional.empty();
        discardAllAnchors(current);
        PlacedNpcRecord moved = current.moved(
                player.getWorld().getRegistryKey().getValue().toString(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYaw(),
                player.getPitch());
        placed.put(current.id(), moved);
        moved = reconcile(moved).record();
        placed.put(current.id(), moved);
        save();
        broadcastVisuals();
        return Optional.of(moved);
    }

    public Optional<PlacedNpcRecord> rename(String idOrHandle, String name) {
        PlacedNpcRecord record = find(idOrHandle).orElse(null);
        return update(record == null ? null : record.named(name));
    }

    public Optional<PlacedNpcRecord> setSkin(String idOrHandle, String skin) {
        if (definitions.skin(skin).isEmpty()) {
            String available = definitions.skins().stream()
                    .map(panetina.elarion.addons.npcs.model.NpcSkinProfile::id)
                    .sorted()
                    .reduce((first, second) -> first + ", " + second)
                    .orElse("none");
            throw new IllegalArgumentException("Unknown NPC skin/profile: " + skin
                    + ". Available profiles: " + available);
        }
        PlacedNpcRecord record = find(idOrHandle).orElse(null);
        return update(record == null ? null : record.withSkin(skin));
    }

    public Optional<PlacedNpcRecord> setPortrait(String idOrHandle, String portrait) {
        if (definitions.portrait(portrait).isEmpty()) {
            String available = definitions.portraits().stream()
                    .map(panetina.elarion.addons.npcs.model.NpcPortraitProfile::id)
                    .sorted()
                    .reduce((first, second) -> first + ", " + second)
                    .orElse("none");
            throw new IllegalArgumentException("Unknown NPC portrait/profile: " + portrait
                    + ". Available profiles: " + available);
        }
        PlacedNpcRecord record = find(idOrHandle).orElse(null);
        return update(record == null ? null : record.withPortrait(portrait));
    }

    public Optional<PlacedNpcRecord> setDialogue(String idOrHandle, String dialogue) {
        if (definitions.dialogue(dialogue).isEmpty()) {
            throw new IllegalArgumentException("Unknown NPC dialogue: " + dialogue);
        }
        PlacedNpcRecord record = find(idOrHandle).orElse(null);
        return update(record == null ? null : record.withDialogue(dialogue));
    }

    public void respawnAll() {
        if (server == null) return;
        Map<UUID, PlacedNpcRecord> updated = new LinkedHashMap<>();
        placed.forEach((id, record) -> {
            removalListener.accept(id);
            updated.put(id, reconcile(record).record());
        });
        placed.clear();
        placed.putAll(updated);
        save();
        broadcastVisuals();
    }

    public Optional<PlacedNpcRecord> face(String idOrHandle, ServerPlayerEntity player) {
        PlacedNpcRecord current = find(idOrHandle).orElse(null);
        if (current == null) return Optional.empty();
        if (!current.worldId().equals(player.getWorld().getRegistryKey().getValue().toString())) {
            throw new IllegalArgumentException("You must be in the NPC's world to face it.");
        }
        PlacedNpcRecord faced = current.facing(yawToward(
                current.x(), current.z(), player.getX(), player.getZ()));
        placed.put(faced.id(), faced);
        applyRecordToAnchor(faced);
        save();
        return Optional.of(faced);
    }

    public Optional<PlacedNpcRecord> rotate(String idOrHandle, float yaw) {
        PlacedNpcRecord current = find(idOrHandle).orElse(null);
        if (current == null) return Optional.empty();
        PlacedNpcRecord rotated = current.facing(yaw);
        placed.put(rotated.id(), rotated);
        applyRecordToAnchor(rotated);
        save();
        return Optional.of(rotated);
    }

    public Optional<PlacedNpcRecord> nearest(ServerPlayerEntity player, double maxDistanceBlocks) {
        double maxDistanceSquared = maxDistanceBlocks * maxDistanceBlocks;
        String worldId = player.getWorld().getRegistryKey().getValue().toString();
        return placed.values().stream()
                .filter(record -> worldId.equals(record.worldId()))
                .map(record -> Map.entry(record, player.squaredDistanceTo(record.x(), record.y(), record.z())))
                .filter(entry -> entry.getValue() <= maxDistanceSquared)
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public RepairResult repair(String idOrHandle) {
        PlacedNpcRecord record = find(idOrHandle).orElse(null);
        if (record == null) return RepairResult.notFound();
        removalListener.accept(record.id());
        ReconcileResult result = reconcile(record);
        placed.put(record.id(), result.record());
        save();
        broadcastVisuals();
        return new RepairResult(true, 1, result.removed(), result.spawned(), result.reused());
    }

    public RepairResult repairAll() {
        int removed = 0;
        int spawned = 0;
        int reused = 0;
        Map<UUID, PlacedNpcRecord> updated = new LinkedHashMap<>();
        for (PlacedNpcRecord record : placed.values()) {
            removalListener.accept(record.id());
            ReconcileResult result = reconcile(record);
            updated.put(record.id(), result.record());
            removed += result.removed();
            spawned += result.spawned();
            reused += result.reused();
        }
        placed.clear();
        placed.putAll(updated);
        save();
        broadcastVisuals();
        return new RepairResult(true, updated.size(), removed, spawned, reused);
    }

    public void onEntityLoaded(ElarionNpcEntity entity) {
        if (!bound) return;
        UUID npcId = entity.placedNpcId().orElse(null);
        if (npcId == null || !placed.containsKey(npcId)) {
            if (npcId != null) removalListener.accept(npcId);
            entity.discard();
            return;
        }
        PlacedNpcRecord record = placed.get(npcId);
        if (!reconciling.contains(npcId) && record.entityId() != null
                && !record.entityId().equals(entity.getUuid())) {
            entity.discard();
            return;
        }
        configure(entity, record);
    }

    public static float yawToward(double sourceX, double sourceZ, double targetX, double targetZ) {
        return (float) Math.toDegrees(Math.atan2(-(targetX - sourceX), targetZ - sourceZ));
    }

    private Optional<PlacedNpcRecord> update(PlacedNpcRecord updated) {
        if (updated == null) return Optional.empty();
        placed.put(updated.id(), updated);
        ReconcileResult result = reconcile(updated);
        updated = result.record();
        placed.put(updated.id(), updated);
        save();
        broadcastVisuals();
        return Optional.of(updated);
    }

    private ReconcileResult reconcile(PlacedNpcRecord record) {
        if (server == null) return new ReconcileResult(record, 0, 0, 0);
        NpcDefinition definition = definitions.npc(record.definitionId()).orElse(null);
        if (definition == null || !definition.enabled()) return new ReconcileResult(record, 0, 0, 0);
        ServerWorld world = world(record.worldId()).orElse(null);
        if (world == null) {
            logger.warn("Skipping NPC {} in unloaded or unknown world {}", record.id(), record.worldId());
            return new ReconcileResult(record, 0, 0, 0);
        }
        loadRecordedChunk(world, record);
        reconciling.add(record.id());
        try {
            int removed = discardLegacyAnchor(world, record);
            List<ElarionNpcEntity> anchors = anchors(world, record);
            UUID canonicalId = chooseCanonicalId(record.entityId(),
                    anchors.stream().map(Entity::getUuid).toList());
            ElarionNpcEntity canonical = anchors.stream()
                    .filter(entity -> entity.getUuid().equals(canonicalId))
                    .findFirst()
                    .orElse(null);
            for (ElarionNpcEntity anchor : anchors) {
                if (anchor != canonical) {
                    anchor.discard();
                    removed++;
                }
            }
            int spawned = 0;
            int reused = canonical == null ? 0 : 1;
            if (canonical == null) {
                canonical = ElarionNpcEntities.NPC.create(world);
                if (canonical == null) return new ReconcileResult(record, removed, 0, 0);
                canonical.setPlacedNpcId(record.id());
                configure(canonical, record);
                if (!world.spawnEntity(canonical)) {
                    return new ReconcileResult(record, removed, 0, 0);
                }
                spawned = 1;
            } else {
                configure(canonical, record);
            }
            return new ReconcileResult(record.withEntity(canonical.getUuid()), removed, spawned, reused);
        } finally {
            reconciling.remove(record.id());
        }
    }

    private Optional<NpcVisualSyncPayload.Entry> visualEntry(PlacedNpcRecord record) {
        NpcDefinition definition = definitions.npc(record.definitionId()).orElse(null);
        if (definition == null || record.entityId() == null) return Optional.empty();
        NpcSkinProfile skin = definitions.skin(record.skin(definition)).orElse(null);
        NpcPortraitProfile portrait = definitions.portrait(record.portrait(definition)).orElse(null);
        return Optional.of(new NpcVisualSyncPayload.Entry(
                record.id(),
                record.entityId(),
                record.commandId(),
                record.displayName(definition),
                skin == null ? "placeholder" : skin.type(),
                skin == null ? "" : skin.texture(),
                skin == null ? "" : skin.playerName(),
                skin == null ? "placeholder" : skin.fallbackType(),
                skin == null ? "" : skin.fallbackTexture(),
                portrait == null ? "placeholder" : portrait.type(),
                portrait == null ? "" : portrait.texture(),
                portrait == null ? "" : portrait.playerName(),
                portrait == null ? "placeholder" : portrait.fallbackType(),
                portrait == null ? "" : portrait.fallbackTexture()));
    }

    private void applyRecordToAnchor(PlacedNpcRecord record) {
        if (record == null || server == null || record.entityId() == null) return;
        ServerWorld world = world(record.worldId()).orElse(null);
        if (world == null) return;
        loadRecordedChunk(world, record);
        Entity entity = world.getEntity(record.entityId());
        if (entity instanceof ElarionNpcEntity npc) configure(npc, record);
    }

    private void discardAllAnchors(PlacedNpcRecord record) {
        if (record == null || server == null) return;
        ServerWorld world = world(record.worldId()).orElse(null);
        if (world == null) return;
        loadRecordedChunk(world, record);
        if (record.entityId() != null) {
            Entity stored = world.getEntity(record.entityId());
            if (stored != null) stored.discard();
        }
        anchors(world, record).forEach(Entity::discard);
    }

    private int discardLegacyAnchor(ServerWorld world, PlacedNpcRecord record) {
        if (record.entityId() == null) return 0;
        Entity stored = world.getEntity(record.entityId());
        if (stored != null && !(stored instanceof ElarionNpcEntity)) {
            stored.discard();
            return 1;
        }
        return 0;
    }

    private List<ElarionNpcEntity> anchors(ServerWorld world, PlacedNpcRecord record) {
        java.util.ArrayList<ElarionNpcEntity> matches = new java.util.ArrayList<>();
        int chunkX = BlockPos.ofFloored(record.x(), record.y(), record.z()).getX() >> 4;
        int chunkZ = BlockPos.ofFloored(record.x(), record.y(), record.z()).getZ() >> 4;
        Box chunkBounds = new Box(
                chunkX << 4, -2048.0D, chunkZ << 4,
                (chunkX << 4) + 16.0D, 2048.0D, (chunkZ << 4) + 16.0D);
        for (Entity entity : world.getOtherEntities(
                null, chunkBounds, candidate -> candidate instanceof ElarionNpcEntity)) {
            if (entity instanceof ElarionNpcEntity npc
                    && npc.placedNpcId().filter(record.id()::equals).isPresent()) {
                matches.add(npc);
            }
        }
        return matches;
    }

    private void configure(ElarionNpcEntity entity, PlacedNpcRecord record) {
        NpcDefinition definition = definitions.npc(record.definitionId()).orElse(null);
        if (definition == null) return;
        entity.setPlacedNpcId(record.id());
        entity.setAnchor(record.x(), record.y(), record.z(), record.yaw());
        entity.setCustomName(Text.literal(record.displayName(definition)));
        entity.setCustomNameVisible(true);
        entity.addCommandTag("elarion_npc");
        entity.addCommandTag("elarion_npc_" + record.id().toString().replace("-", ""));
        entity.applyStaticState();
    }

    private static void loadRecordedChunk(ServerWorld world, PlacedNpcRecord record) {
        world.getChunk(BlockPos.ofFloored(record.x(), record.y(), record.z()));
    }

    private Optional<ServerWorld> world(String worldId) {
        if (server == null || worldId == null || worldId.isBlank()) return Optional.empty();
        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(worldId));
        return Optional.ofNullable(server.getWorld(key));
    }

    private void save() {
        if (server != null) storage.save(server, placed);
    }

    private void broadcastVisuals() {
        if (server == null) return;
        NpcVisualSyncPayload payload = visualSyncPayload();
        server.getPlayerManager().getPlayerList().forEach(player -> ServerPlayNetworking.send(player, payload));
    }

    private PlacedNpcRecord ensureHandle(PlacedNpcRecord record) {
        return record.handle() == null || record.handle().isBlank()
                ? record.withHandle(nextHandle(record.definitionId()))
                : record;
    }

    private String nextHandle(String definitionId) {
        String base = definitionId == null || definitionId.isBlank() ? "npc" : definitionId;
        int index = 1;
        String candidate;
        do {
            candidate = base + "_" + index++;
        } while (handleExists(candidate));
        return candidate;
    }

    private boolean handleExists(String handle) {
        return placed.values().stream().anyMatch(record -> handle.equalsIgnoreCase(record.commandId()));
    }

    static UUID chooseCanonicalId(UUID preferred, List<UUID> candidates) {
        if (preferred != null && candidates.contains(preferred)) return preferred;
        return candidates.isEmpty() ? null : candidates.getFirst();
    }

    private record ReconcileResult(PlacedNpcRecord record, int removed, int spawned, int reused) {
    }

    public record RepairResult(boolean found, int checked, int removed, int respawned, int reused) {
        private static RepairResult notFound() {
            return new RepairResult(false, 0, 0, 0, 0);
        }
    }
}
