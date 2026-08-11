package panetina.elarion.core.mixin;

import java.util.Map;
import java.util.function.Function;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@link net.minecraft.structure.StructureTemplate.PalettedBlockInfoList} caches
 * block lookups in a mutable {@link java.util.HashMap}. Structure generation can
 * ask the same template for different chunks concurrently, so the vanilla
 * {@code computeIfAbsent} is not safe here. Keep the lock scoped to one template
 * palette; it only covers cache population, never structure placement.
 */
@Mixin(targets = "net.minecraft.structure.StructureTemplate$PalettedBlockInfoList")
abstract class StructureTemplatePaletteCacheMixin {
    @Redirect(
            method = "getAllOf",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"))
    private Object elarion$synchronizePaletteCache(
            Map<Object, Object> cache, Object key, Function<? super Object, ? extends Object> factory) {
        synchronized (cache) {
            return cache.computeIfAbsent(key, factory);
        }
    }
}
