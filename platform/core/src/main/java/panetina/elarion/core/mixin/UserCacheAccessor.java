package panetina.elarion.core.mixin;

import net.minecraft.util.UserCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(UserCache.class)
public interface UserCacheAccessor {
    @Accessor("byName")
    Map<?, ?> elarion$byName();

    @Accessor("byUuid")
    Map<?, ?> elarion$byUuid();

    @Accessor("pendingRequests")
    Map<?, ?> elarion$pendingRequests();
}
