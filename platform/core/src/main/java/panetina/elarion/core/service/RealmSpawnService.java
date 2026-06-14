package panetina.elarion.core.service;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.ServerIdentityConfig;
import panetina.elarion.core.model.SpawnPoint;

import java.util.Map;

public final class RealmSpawnService {
    private final CitizenService citizens;
    private final RealmService realms;
    private final HistoryService history;
    private final ServerIdentityConfig serverIdentity;

    public RealmSpawnService(CitizenService citizens, RealmService realms, HistoryService history,
                             ServerIdentityConfig serverIdentity) {
        this.citizens = citizens;
        this.realms = realms;
        this.history = history;
        this.serverIdentity = serverIdentity;
    }

    public boolean teleportToRealmSpawn(ServerPlayerEntity player, String reason) {
        CitizenRecord citizen = citizens.getOrCreate(player);
        RealmDefinition realm = realms.forCitizen(citizen).orElse(null);
        if (realm == null) return false;
        boolean success = teleport(player, realm.spawn());
        if (success) {
            history.record("realm", reason, player.getUuid(), "player", player.getUuidAsString(),
                    realm.id(), Map.of("world", realm.spawn().worldId()));
        }
        return success;
    }

    public void teleportAfterRealmAssignment(ServerPlayerEntity player) {
        if (teleportToRealmSpawn(player, "realm-join-teleport")) {
            player.sendMessage(Text.literal("You were sent to your "
                    + serverIdentity.realmSingular().toLowerCase(java.util.Locale.ROOT) + " spawn."), false);
        }
    }

    public void routeRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        if (alive) return;
        if (oldPlayer.getSpawnPointPosition() != null) return;
        teleportToRealmSpawn(newPlayer, "realm-respawn");
    }

    private static boolean teleport(ServerPlayerEntity player, SpawnPoint spawn) {
        ServerWorld world = resolveWorld(player, spawn.worldId());
        if (world == null) return false;
        player.teleportTo(new TeleportTarget(
                world,
                new net.minecraft.util.math.Vec3d(spawn.x(), spawn.y(), spawn.z()),
                net.minecraft.util.math.Vec3d.ZERO,
                spawn.yaw(),
                spawn.pitch(),
                TeleportTarget.NO_OP));
        return true;
    }

    private static ServerWorld resolveWorld(ServerPlayerEntity player, String worldId) {
        Identifier identifier = Identifier.tryParse(worldId);
        if (identifier == null) return null;
        RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, identifier);
        return player.getServer().getWorld(key);
    }
}
