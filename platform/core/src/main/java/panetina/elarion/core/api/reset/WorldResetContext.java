package panetina.elarion.core.api.reset;

import net.minecraft.server.MinecraftServer;
import java.nio.file.Path;

public record WorldResetContext(MinecraftServer server, String worldId, String executorName, Path backupRoot) {}
