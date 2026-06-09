package panetina.elarion.core.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import panetina.elarion.core.api.ElarionApi;

import java.util.List;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin {
    @Shadow @Final private String playerName;

    @Inject(method = "getPlayer", at = @At("HEAD"), cancellable = true)
    private void elarion$resolveNickname(
            ServerCommandSource source,
            CallbackInfoReturnable<ServerPlayerEntity> cir
    ) throws CommandSyntaxException {
        if (playerName == null) return;

        ServerPlayerEntity canonical = source.getServer().getPlayerManager().getPlayer(playerName);
        if (canonical != null && !ElarionApi.get().identities().canSee(source, canonical)) {
            throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        }
        if (canonical != null) return;

        ElarionApi.get().identities().resolveVisiblePlayer(source, playerName).ifPresent(cir::setReturnValue);
    }

    @Inject(method = "getPlayers", at = @At("HEAD"), cancellable = true)
    private void elarion$resolveNicknameList(
            ServerCommandSource source,
            CallbackInfoReturnable<List<ServerPlayerEntity>> cir
    ) {
        if (playerName == null) return;

        ServerPlayerEntity canonical = source.getServer().getPlayerManager().getPlayer(playerName);
        if (canonical != null && !ElarionApi.get().identities().canSee(source, canonical)) {
            cir.setReturnValue(List.of());
            return;
        }
        if (canonical != null) return;

        ElarionApi.get().identities().resolveVisiblePlayer(source, playerName)
                .ifPresent(player -> cir.setReturnValue(List.of(player)));
    }
}
