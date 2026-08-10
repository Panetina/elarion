package panetina.elarion.core.api.reset;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WorldResetRegistryTest {
    @Test
    void exposesOnlyOperatorAuthoredManagedWorldSuggestions() {
        WorldResetRegistry registry = new WorldResetRegistry();
        assertEquals(List.of(), registry.worldIds());

        registry.setOperator(new WorldResetOperator() {
            @Override public boolean exists(net.minecraft.server.MinecraftServer server, String worldId) {
                return false;
            }

            @Override public java.util.concurrent.CompletionStage<Void> regenerate(
                    net.minecraft.server.MinecraftServer server, String worldId) {
                return java.util.concurrent.CompletableFuture.completedFuture(null);
            }

            @Override public java.util.concurrent.CompletionStage<Void> restore(
                    net.minecraft.server.MinecraftServer server, String worldId, java.nio.file.Path backupRoot) {
                return java.util.concurrent.CompletableFuture.completedFuture(null);
            }

            @Override public java.util.Collection<String> worldIds() {
                return List.of("lobby", "underworld");
            }
        });

        assertEquals(List.of("lobby", "underworld"), registry.worldIds());
    }
}
