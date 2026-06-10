package panetina.elarion.addons.realms.mixin;

import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.addons.realms.service.RealmProtectionService;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow @Final private World world;

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("RETURN"))
    private void elarion$preserveProtectedWorldBlocks(CallbackInfo ci) {
        if (RealmProtectionService.protectsExplosionBlocks(world)) {
            ((Explosion) (Object) this).clearAffectedBlocks();
        }
    }
}
