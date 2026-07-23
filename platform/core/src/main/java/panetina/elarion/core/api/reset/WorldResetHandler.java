package panetina.elarion.core.api.reset;

import net.minecraft.server.MinecraftServer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Domain-owned cleanup for a managed-world regeneration. */
public interface WorldResetHandler {
    String id();
    Map<String, Long> preview(MinecraftServer server, String worldId);
    List<Path> backupTargets(MinecraftServer server, String worldId);
    WorldResetResult reset(WorldResetContext context) throws Exception;
}
