package panetina.elarion.core.api.reset;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface PlayerResetHandler {
    String id();

    default Map<String, Long> preview(MinecraftServer server) {
        return Map.of();
    }

    default List<Path> backupTargets(MinecraftServer server) {
        return List.of();
    }

    PlayerResetResult reset(PlayerResetContext context) throws Exception;
}
