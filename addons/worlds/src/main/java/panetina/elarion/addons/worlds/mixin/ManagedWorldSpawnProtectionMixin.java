package panetina.elarion.addons.worlds.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import panetina.elarion.addons.worlds.api.ElarionWorldsApi;

@Mixin(MinecraftServer.class)
abstract class ManagedWorldSpawnProtectionMixin {
    @Inject(method = "isSpawnProtected", at = @At("HEAD"), cancellable = true)
    private void elarion$useRealmProtectionInManagedWorlds(
            ServerWorld world,
            BlockPos pos,
            PlayerEntity player,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (ElarionWorldsApi.isManaged(world)) {
            cir.setReturnValue(false);
        }
    }
}
