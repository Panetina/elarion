package panetina.elarion.addons.worlds.mixin;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.WorldBorderCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldBorderCommand.class)
abstract class WorldBorderCommandMixin {
    @Redirect(
            method = {
                    "executeBuffer", "executeDamage", "executeWarningTime", "executeWarningDistance",
                    "executeGet", "executeCenter", "executeSet"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;getWorldBorder()"
                            + "Lnet/minecraft/world/border/WorldBorder;"
            )
    )
    private static WorldBorder elarion$useCommandWorld(ServerWorld ignored, ServerCommandSource source) {
        return source.getWorld().getWorldBorder();
    }
}
