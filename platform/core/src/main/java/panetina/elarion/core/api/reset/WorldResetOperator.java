package panetina.elarion.core.api.reset;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface WorldResetOperator {
    boolean exists(MinecraftServer server, String worldId);
    /** Completes only after the old persistent world is deleted and a new one is open. */
    CompletionStage<Void> regenerate(MinecraftServer server, String worldId) throws Exception;

    /** Replaces the managed world from Core's backup and reopens it. */
    CompletionStage<Void> restore(MinecraftServer server, String worldId, Path backupRoot) throws Exception;

    /** Stable managed-world names used only for server-authored command completion. */
    default Collection<String> worldIds() {
        return List.of();
    }

    /** Persistent world files that must be copied before regeneration. */
    default List<Path> backupTargets(MinecraftServer server, String worldId) {
        return List.of();
    }
}
