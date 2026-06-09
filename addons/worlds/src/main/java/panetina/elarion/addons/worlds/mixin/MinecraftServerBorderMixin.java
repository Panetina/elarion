package panetina.elarion.addons.worlds.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerBorderMixin {
    @Redirect(
            method = "createWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/border/WorldBorder;addListener"
                            + "(Lnet/minecraft/world/border/WorldBorderListener;)V"
            )
    )
    private void elarion$disableVanillaCrossWorldSync(
            WorldBorder border,
            WorldBorderListener listener
    ) {
    }
}
