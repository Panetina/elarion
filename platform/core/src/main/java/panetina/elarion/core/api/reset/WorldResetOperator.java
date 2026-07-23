package panetina.elarion.core.api.reset;

import net.minecraft.server.MinecraftServer;

public interface WorldResetOperator {
    boolean exists(MinecraftServer server, String worldId);
    void regenerate(MinecraftServer server, String worldId) throws Exception;
}
