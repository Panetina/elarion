package panetina.elarion.core.api.reset;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public record PlayerResetContext(MinecraftServer server, String executor, Path backupDirectory) {
}
