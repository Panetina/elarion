package panetina.elarion.addons.underworld.service;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import panetina.elarion.addons.underworld.block.TombstoneVariant;
import panetina.elarion.addons.underworld.block.UnderworldBlocks;
import panetina.elarion.addons.underworld.block.UnderworldTombBlock;
import panetina.elarion.addons.underworld.block.UnderworldTombBlockEntity;
import panetina.elarion.addons.underworld.model.CorpseRecord;

import java.util.Optional;

public final class UnderworldTombService {
    private final Logger logger;

    public UnderworldTombService(Logger logger) {
        this.logger = logger;
    }

    public boolean place(ServerWorld world, CorpseRecord corpse, Direction facing) {
        TombstoneVariant variant = TombstoneVariant.byId(corpse.tombstoneVariant);
        BlockPos base = BlockPos.ofFloored(corpse.x, corpse.y, corpse.z);
        BlockPos origin = findPlacement(world, base).orElse(null);
        if (origin == null) {
            logger.warn("Unable to place Underworld tomb for corpse {} at {},{},{}",
                    corpse.corpseId, corpse.x, corpse.y, corpse.z);
            return false;
        }
        UnderworldTombBlock.placeStructure(world, origin, facing, variant, corpse.corpseId);
        corpse.tombX = origin.getX();
        corpse.tombY = origin.getY();
        corpse.tombZ = origin.getZ();
        corpse.tombstoneVariant = variant.id();
        return true;
    }

    public boolean valid(ServerWorld world, CorpseRecord corpse) {
        if (!corpse.hasTombPosition()) return false;
        BlockPos origin = corpse.tombOrigin();
        BlockState lower = world.getBlockState(origin);
        BlockState upper = world.getBlockState(origin.up());
        if (!lower.isOf(UnderworldBlocks.TOMB) || !upper.isOf(UnderworldBlocks.TOMB)) return false;
        if (lower.get(UnderworldTombBlock.PART_Y) != 0 || upper.get(UnderworldTombBlock.PART_Y) != 1) return false;
        if (!(world.getBlockEntity(origin) instanceof UnderworldTombBlockEntity tomb)) return false;
        return corpse.corpseId.equals(tomb.corpseId());
    }

    public void remove(ServerWorld world, CorpseRecord corpse) {
        if (!corpse.hasTombPosition()) return;
        BlockPos origin = corpse.tombOrigin();
        BlockState state = world.getBlockState(origin);
        if (state.isOf(UnderworldBlocks.TOMB)) {
            UnderworldTombBlock.removeStructure(world, UnderworldTombBlock.origin(origin, state));
        }
    }

    private Optional<BlockPos> findPlacement(ServerWorld world, BlockPos base) {
        for (int radius = 0; radius <= 2; radius++) {
            for (int dy : new int[]{0, 1, -1}) {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (radius > 0 && Math.max(Math.abs(x), Math.abs(z)) != radius) continue;
                        BlockPos candidate = base.add(x, dy, z);
                        if (UnderworldTombBlock.canPlaceStructure(world, candidate)) {
                            return Optional.of(candidate);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }
}
