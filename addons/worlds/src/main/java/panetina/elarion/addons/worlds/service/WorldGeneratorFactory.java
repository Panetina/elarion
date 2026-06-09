package panetina.elarion.addons.worlds.service;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import panetina.elarion.addons.worlds.model.ManagedWorldDefinition;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

final class WorldGeneratorFactory {
    private WorldGeneratorFactory() {
    }

    static GeneratedWorld create(MinecraftServer server, ManagedWorldDefinition definition) {
        var dimensions = server.getRegistryManager().get(RegistryKeys.DIMENSION);
        DimensionOptions template = dimensions.get(Identifier.of(definition.template()));
        if (template == null) {
            throw new IllegalArgumentException("Unknown dimension template " + definition.template());
        }

        ChunkGenerator generator = switch (definition.type()) {
            case VOID -> new VoidChunkGenerator(server, Identifier.of(definition.biome()));
            case FLAT -> new FlatChunkGenerator(FlatChunkGeneratorConfig.getDefaultConfig(
                    server.getRegistryManager().getWrapperOrThrow(RegistryKeys.BIOME),
                    server.getRegistryManager().getWrapperOrThrow(RegistryKeys.STRUCTURE_SET),
                    server.getRegistryManager().getWrapperOrThrow(RegistryKeys.PLACED_FEATURE)));
            default -> template.chunkGenerator();
        };
        return new GeneratedWorld(template, generator);
    }

    record GeneratedWorld(DimensionOptions template, ChunkGenerator generator) {
    }
}
