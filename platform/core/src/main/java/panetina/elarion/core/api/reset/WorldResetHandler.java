package panetina.elarion.core.api.reset;

import net.minecraft.server.MinecraftServer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Domain-owned cleanup and runtime restoration for a managed-world regeneration. */
public interface WorldResetHandler {
    String id();
    Map<String, Long> preview(MinecraftServer server, String worldId);
    List<Path> backupTargets(MinecraftServer server, String worldId);
    WorldResetResult reset(WorldResetContext context) throws Exception;
    /** Core restored this handler's declared backup targets; refresh in-memory state. */
    void restore(WorldResetContext context) throws Exception;
}
