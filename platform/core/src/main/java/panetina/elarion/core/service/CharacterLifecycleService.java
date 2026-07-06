package panetina.elarion.core.service;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import panetina.elarion.core.event.ElarionEventBus;
import panetina.elarion.core.model.CharacterArchiveRecord;
import panetina.elarion.core.model.CharacterLifecycleRecord;
import panetina.elarion.core.model.CharacterLifecycleStatus;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.ElarionDomainEvent;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.network.CharacterCreationRequirementPayload;
import panetina.elarion.core.network.CharacterRealmAssignmentPayload;
import panetina.elarion.core.storage.CharacterLifecycleState;
import panetina.elarion.core.storage.CharacterLifecycleStorage;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public final class CharacterLifecycleService {
    public static final long DEFAULT_RECREATION_DELAY_MILLIS = Duration.ofHours(24).toMillis();
    public static final int MAX_BIOGRAPHY_LENGTH = 500;

    private final Logger logger;
    private final CharacterLifecycleStorage storage;
    private final CitizenService citizens;
    private final RealmService realms;
    private final NicknameService nicknames;
    private final ElarionEventBus events;
    private final List<RegisteredResetHandler> resetHandlers = new CopyOnWriteArrayList<>();
    private CharacterLifecycleState state = new CharacterLifecycleState();
    private MinecraftServer server;
    private int ticks;
    private boolean dirty;

    public CharacterLifecycleService(
            Logger logger,
            CharacterLifecycleStorage storage,
            CitizenService citizens,
            RealmService realms,
            NicknameService nicknames,
            ElarionEventBus events,
            PlayerRestrictionService restrictions
    ) {
        this.logger = logger;
        this.storage = storage;
        this.citizens = citizens;
        this.realms = realms;
        this.nicknames = nicknames;
        this.events = events;
        restrictions.register(this::restriction);
    }

    public void bind(MinecraftServer server) {
        this.server = server;
        this.state = storage.load(server);
        normalizeState();
        for (CitizenRecord citizen : citizens.all()) {
            state.accounts.computeIfAbsent(citizen.uuid().toString(), ignored ->
                    CharacterLifecycleRecord.migration(citizen.uuid()));
        }
        dirty = true;
        save();
    }

    public void registerResetHandler(String id, ResetHandler handler) {
        String normalized = clean(id).toLowerCase(java.util.Locale.ROOT);
        if (handler == null || normalized.isBlank()) return;
        resetHandlers.removeIf(existing -> existing.id().equals(normalized));
        resetHandlers.add(new RegisteredResetHandler(normalized, handler));
        resetHandlers.sort(Comparator.comparing(RegisteredResetHandler::id));
    }

    public CharacterLifecycleRecord onJoin(ServerPlayerEntity player) {
        requireBound();
        CharacterLifecycleRecord record = state.accounts.computeIfAbsent(player.getUuidAsString(), ignored -> {
            dirty = true;
            return CharacterLifecycleRecord.newAccount(player.getUuid());
        });
        if (record.status == CharacterLifecycleStatus.RESETTING) resumeReset(record);
        refreshCooldown(record);
        sync(player, "");
        return record;
    }

    public Optional<CharacterLifecycleRecord> find(UUID accountId) {
        if (accountId == null) return Optional.empty();
        return Optional.ofNullable(state.accounts.get(accountId.toString()));
    }

    public Optional<CharacterArchiveRecord> archive(String characterId) {
        return Optional.ofNullable(state.archives.get(clean(characterId)));
    }

    public List<CharacterArchiveRecord> archives(UUID accountId) {
        if (accountId == null) return List.of();
        return state.archives.values().stream()
                .filter(archive -> accountId.toString().equals(archive.accountId))
                .sorted(Comparator.comparingLong(archive -> -archive.diedAt))
                .toList();
    }

    public boolean isActive(UUID accountId) {
        return find(accountId).map(record -> record.status == CharacterLifecycleStatus.ACTIVE).orElse(false);
    }

    public boolean requiresCreation(UUID accountId) {
        return find(accountId).map(record -> {
            refreshCooldown(record);
            return record.status == CharacterLifecycleStatus.MIGRATION_REQUIRED
                    || record.status == CharacterLifecycleStatus.CREATION_REQUIRED;
        }).orElse(true);
    }

    public synchronized void beginTrueDeath(ServerPlayerEntity player, String reason, Map<String, String> metadata) {
        requireBound();
        CitizenRecord citizen = citizens.getOrCreate(player);
        CharacterLifecycleRecord record = state.accounts.computeIfAbsent(player.getUuidAsString(), ignored ->
                CharacterLifecycleRecord.migration(player.getUuid()));
        if (record.status == CharacterLifecycleStatus.RESETTING
                || record.status == CharacterLifecycleStatus.TRUE_DEAD_COOLDOWN
                || record.status == CharacterLifecycleStatus.CREATION_REQUIRED) return;

        CharacterArchiveRecord archive = snapshot(citizen, record, reason, metadata);
        state.archives.put(archive.characterId, archive);
        String reserved = NicknameService.comparisonKey(archive.displayName);
        if (!reserved.isBlank()) state.reservedNames.add(reserved);

        record.status = CharacterLifecycleStatus.RESETTING;
        record.resetReason = clean(reason);
        record.completedResetSteps.clear();
        record.updatedAt = System.currentTimeMillis();
        dirty = true;
        save();
        resumeReset(record);
        sync(player, "");
    }

    public synchronized SubmissionResult submit(
            ServerPlayerEntity player, String nonce, String displayName, String biography
    ) {
        CharacterLifecycleRecord record = state.accounts.get(player.getUuidAsString());
        if (record == null) return SubmissionResult.failure("Character creation is not ready.");
        refreshCooldown(record);
        if (record.status != CharacterLifecycleStatus.MIGRATION_REQUIRED
                && record.status != CharacterLifecycleStatus.CREATION_REQUIRED) {
            return SubmissionResult.failure("Character creation is not currently required.");
        }
        if (!record.nonce.equals(clean(nonce))) return SubmissionResult.failure("That character session expired.");
        if (record.eligibleAt > System.currentTimeMillis()) {
            return SubmissionResult.failure("Your next character is not available yet.");
        }
        String bio = clean(biography).trim();
        if (bio.codePointCount(0, bio.length()) > MAX_BIOGRAPHY_LENGTH) {
            return SubmissionResult.failure("Biography cannot exceed " + MAX_BIOGRAPHY_LENGTH + " characters.");
        }
        NicknameService.Validation validation = nicknames.validate(player.getUuid(), displayName);
        if (!validation.valid()) return SubmissionResult.failure(validation.error());
        String nameKey = NicknameService.comparisonKey(validation.nickname());
        if (state.reservedNames.contains(nameKey)) {
            return SubmissionResult.failure("That name belongs to a dead character and is permanently reserved.");
        }

        boolean migration = record.status == CharacterLifecycleStatus.MIGRATION_REQUIRED;
        if (!migration) {
            record.activeCharacterId = UUID.randomUUID().toString();
            record.generation = Math.max(1, record.generation + 1);
        }
        record.biography = bio;
        record.status = CharacterLifecycleStatus.ACTIVE;
        record.eligibleAt = 0L;
        record.nonce = UUID.randomUUID().toString();
        record.resetReason = "";
        record.completedResetSteps.clear();
        record.updatedAt = System.currentTimeMillis();
        citizens.update(player, migration ? "character-migration-completed" : "character-created",
                citizen -> citizen.setNickname(validation.nickname()));
        dirty = true;
        save();
        Optional<CharacterRealmAssignmentPayload> assignment = assignBalancedRealmIfNeeded(player, migration);
        sync(player, "");
        assignment.ifPresent(payload -> ServerPlayNetworking.send(player, payload));
        emit("character-created", player.getUuid(), record, Map.of(
                "displayName", validation.nickname(),
                "generation", Integer.toString(record.generation),
                "migration", Boolean.toString(migration)));
        return SubmissionResult.accepted();
    }

    public synchronized void finishCooldown(UUID accountId) {
        CharacterLifecycleRecord record = state.accounts.get(accountId.toString());
        if (record == null) return;
        record.eligibleAt = 0L;
        if (record.status == CharacterLifecycleStatus.TRUE_DEAD_COOLDOWN) {
            record.status = CharacterLifecycleStatus.CREATION_REQUIRED;
            record.nonce = UUID.randomUUID().toString();
        }
        record.updatedAt = System.currentTimeMillis();
        dirty = true;
        save();
        sync(accountId, "");
    }

    public synchronized boolean forceActiveForTesting(ServerPlayerEntity player) {
        CharacterLifecycleRecord record = state.accounts.computeIfAbsent(player.getUuidAsString(), ignored ->
                CharacterLifecycleRecord.migration(player.getUuid()));
        if (record.activeCharacterId.isBlank()) record.activeCharacterId = UUID.randomUUID().toString();
        record.generation = Math.max(1, record.generation);
        record.status = CharacterLifecycleStatus.ACTIVE;
        record.eligibleAt = 0L;
        record.nonce = UUID.randomUUID().toString();
        record.resetReason = "";
        record.completedResetSteps.clear();
        record.updatedAt = System.currentTimeMillis();
        dirty = true;
        save();
        sync(player, "Character lifecycle marked active for testing.");
        return true;
    }

    public synchronized void resetForTesting(ServerPlayerEntity player) {
        CharacterLifecycleRecord record = CharacterLifecycleRecord.newAccount(player.getUuid());
        state.accounts.put(player.getUuidAsString(), record);
        dirty = true;
        save();
        sync(player, "Character state reset for testing.");
    }

    public void tick() {
        if (server == null || ++ticks % 20 != 0) return;
        boolean changed = false;
        for (CharacterLifecycleRecord record : state.accounts.values()) {
            CharacterLifecycleStatus before = record.status;
            if (before == CharacterLifecycleStatus.RESETTING) resumeReset(record);
            refreshCooldown(record);
            changed |= before != record.status;
            ServerPlayerEntity player = parseUuid(record.accountId)
                    .map(server.getPlayerManager()::getPlayer).orElse(null);
            if (player != null && before != record.status) sync(player, "");
        }
        if (changed) {
            dirty = true;
            save();
        }
    }

    public void save() {
        if (server == null || !dirty) return;
        storage.save(server, state);
        dirty = false;
    }

    private void resumeReset(CharacterLifecycleRecord record) {
        CharacterArchiveRecord archive = state.archives.get(record.activeCharacterId);
        if (archive == null) {
            logger.error("Cannot resume character reset for {}: archive {} is missing",
                    record.accountId, record.activeCharacterId);
            return;
        }
        ResetContext context = new ResetContext(
                UUID.fromString(record.accountId), archive.characterId, archive.displayName,
                archive.realmId, archive.reason, Map.copyOf(archive.metadata));
        try {
            if (!record.completedResetSteps.contains("elarion_core")) {
                resetCoreCharacter(context);
                record.completedResetSteps.add("elarion_core");
                dirty = true;
                save();
            }
            for (RegisteredResetHandler handler : resetHandlers) {
                if (record.completedResetSteps.contains(handler.id())) continue;
                handler.handler().reset(context);
                record.completedResetSteps.add(handler.id());
                dirty = true;
                save();
            }
            record.status = CharacterLifecycleStatus.TRUE_DEAD_COOLDOWN;
            record.eligibleAt = System.currentTimeMillis() + DEFAULT_RECREATION_DELAY_MILLIS;
            record.nonce = UUID.randomUUID().toString();
            record.updatedAt = System.currentTimeMillis();
            dirty = true;
            save();
            emit("character-reset-completed", context.accountId(), record, Map.of(
                    "deadCharacterId", context.characterId(), "eligibleAt", Long.toString(record.eligibleAt)));
        } catch (Exception exception) {
            logger.error("Character reset step failed for {}. It will retry safely.", record.accountId, exception);
        }
    }

    private void resetCoreCharacter(ResetContext context) {
        CitizenRecord citizen = citizens.find(context.accountId()).orElse(null);
        if (citizen == null) return;
        citizen.clearRealmAffiliation();
        citizen.setNickname("");
        citizen.clearActiveTitle();
        citizen.flags().clear();
        citizen.grantedAbilities().clear();
        citizen.unlockedTitleIds().clear();
        citizen.titleUnlockTimes().clear();
        citizens.save(citizen, "character-true-death-reset");
    }

    private CharacterArchiveRecord snapshot(
            CitizenRecord citizen, CharacterLifecycleRecord record, String reason, Map<String, String> metadata
    ) {
        CharacterArchiveRecord archive = new CharacterArchiveRecord();
        archive.characterId = record.activeCharacterId.isBlank()
                ? UUID.randomUUID().toString() : record.activeCharacterId;
        archive.accountId = citizen.uuid().toString();
        archive.generation = Math.max(1, record.generation);
        archive.displayName = citizen.nickname() == null || citizen.nickname().isBlank()
                ? citizen.lastKnownUsername() : citizen.nickname();
        archive.biography = record.biography;
        archive.realmId = citizen.realmId();
        archive.activeTitleId = citizen.activeTitleId();
        archive.unlockedTitleIds = new ArrayList<>(citizen.unlockedTitleIds());
        archive.metadata = new LinkedHashMap<>(metadata == null ? Map.of() : metadata);
        archive.createdAt = citizen.joinedAt();
        archive.diedAt = System.currentTimeMillis();
        archive.reason = clean(reason);
        return archive;
    }

    private Optional<PlayerRestrictionService.PlayerRestriction> restriction(
            ServerPlayerEntity player, String action
    ) {
        CharacterLifecycleRecord record = state.accounts.get(player.getUuidAsString());
        if (record == null || record.status == CharacterLifecycleStatus.ACTIVE) return Optional.empty();
        if (PlayerRestrictionService.CHAT.equals(action)
                || PlayerRestrictionService.PRIVATE_MESSAGE.equals(action)
                || PlayerRestrictionService.GROUP_CHAT.equals(action)
                || PlayerRestrictionService.PORTAL_TRAVEL.equals(action)
                || PlayerRestrictionService.TELEPORT.equals(action)) {
            return Optional.of(new PlayerRestrictionService.PlayerRestriction(
                    "elarion_core_characters", restrictionMessage(record)));
        }
        return Optional.empty();
    }

    private String restrictionMessage(CharacterLifecycleRecord record) {
        long now = System.currentTimeMillis();
        if (record.status == CharacterLifecycleStatus.TRUE_DEAD_COOLDOWN && record.eligibleAt > now) {
            return "Your next character is available in " + formatRemaining(record.eligibleAt - now) + ".";
        }
        if (record.status == CharacterLifecycleStatus.RESETTING) {
            return "Character cleanup is still running. Try again shortly.";
        }
        return "Complete character creation before returning to the living world.";
    }

    private void refreshCooldown(CharacterLifecycleRecord record) {
        if (record.status == CharacterLifecycleStatus.TRUE_DEAD_COOLDOWN
                && record.eligibleAt <= System.currentTimeMillis()) {
            record.status = CharacterLifecycleStatus.CREATION_REQUIRED;
            record.nonce = UUID.randomUUID().toString();
            record.updatedAt = System.currentTimeMillis();
            dirty = true;
            emit("character-creation-available", UUID.fromString(record.accountId), record, Map.of());
        }
    }

    public void sync(UUID accountId, String feedback) {
        if (server == null) return;
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(accountId);
        if (player != null) sync(player, feedback);
    }

    public void sync(ServerPlayerEntity player, String feedback) {
        CharacterLifecycleRecord record = state.accounts.get(player.getUuidAsString());
        if (record == null) return;
        refreshCooldown(record);
        boolean required = record.status == CharacterLifecycleStatus.MIGRATION_REQUIRED
                || record.status == CharacterLifecycleStatus.CREATION_REQUIRED
                || record.status == CharacterLifecycleStatus.TRUE_DEAD_COOLDOWN;
        CitizenRecord citizen = citizens.getOrCreate(player);
        String prefill = citizen.nickname() == null || citizen.nickname().isBlank()
                ? player.getGameProfile().getName() : citizen.nickname();
        ServerPlayNetworking.send(player, new CharacterCreationRequirementPayload(
                required, record.nonce, record.status.name(), record.eligibleAt,
                prefill, record.biography, clean(feedback)));
    }

    private void emit(
            String type, UUID actorId, CharacterLifecycleRecord record, Map<String, String> metadata
    ) {
        events.emitDomainEvent(ElarionDomainEvent.of(
                "elarion_core", type, actorId, "", "character", record.activeCharacterId, metadata));
    }

    private Optional<CharacterRealmAssignmentPayload> assignBalancedRealmIfNeeded(
            ServerPlayerEntity player, boolean migration
    ) {
        if (migration) return Optional.empty();
        CitizenRecord citizen = citizens.getOrCreate(player);
        if (!citizen.realmId().isBlank()) return Optional.empty();
        List<RealmDefinition> candidates = List.of("realm1", "realm2", "realm3").stream()
                .map(realms::find)
                .flatMap(Optional::stream)
                .toList();
        if (candidates.isEmpty()) return Optional.empty();

        List<CitizenRecord> existingCitizens = new ArrayList<>(citizens.all());
        Map<String, Integer> counts = CharacterRealmAssignmentPlanner.counts(candidates, existingCitizens);
        RealmDefinition selected = CharacterRealmAssignmentPlanner
                .selectStarterRealm(candidates, existingCitizens, ThreadLocalRandom.current())
                .orElse(null);
        if (selected == null) return Optional.empty();
        if (!realms.assign(player, selected.id())) return Optional.empty();

        ArrayList<CharacterRealmAssignmentPayload.Option> options = new ArrayList<>();
        for (RealmDefinition realm : candidates) {
            int population = counts.getOrDefault(realm.id(), 0) + (realm.id().equals(selected.id()) ? 1 : 0);
            options.add(new CharacterRealmAssignmentPayload.Option(
                    realm.id(), realms.officialName(realm), population, realm.id().equals(selected.id())));
        }
        return Optional.of(new CharacterRealmAssignmentPayload(
                selected.id(), realms.officialName(selected), options));
    }

    private void normalizeState() {
        if (state.accounts == null) state.accounts = new LinkedHashMap<>();
        if (state.archives == null) state.archives = new LinkedHashMap<>();
        if (state.reservedNames == null) state.reservedNames = new java.util.LinkedHashSet<>();
        state.accounts.values().forEach(record -> {
            if (record.completedResetSteps == null) record.completedResetSteps = new java.util.LinkedHashSet<>();
            if (record.nonce == null || record.nonce.isBlank()) record.nonce = UUID.randomUUID().toString();
            if (record.status == null) record.status = CharacterLifecycleStatus.MIGRATION_REQUIRED;
        });
    }

    private void requireBound() {
        if (server == null) throw new IllegalStateException("CharacterLifecycleService is not bound to a server");
    }

    private static Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static String formatRemaining(long millis) {
        long seconds = Math.max(1L, (millis + 999L) / 1000L);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remainingSeconds = seconds % 60L;
        if (hours > 0L) return hours + "h " + minutes + "m";
        if (minutes > 0L) return minutes + "m " + remainingSeconds + "s";
        return remainingSeconds + "s";
    }

    public record ResetContext(
            UUID accountId,
            String characterId,
            String displayName,
            String realmId,
            String reason,
            Map<String, String> metadata
    ) {
    }

    @FunctionalInterface
    public interface ResetHandler {
        void reset(ResetContext context) throws Exception;
    }

    private record RegisteredResetHandler(String id, ResetHandler handler) {
    }

    public record SubmissionResult(boolean success, String message) {
        public static SubmissionResult accepted() { return new SubmissionResult(true, ""); }
        public static SubmissionResult failure(String message) { return new SubmissionResult(false, message); }
    }
}
