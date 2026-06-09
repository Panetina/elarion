package panetina.elarion.core.mixin;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.command.IdentitySuggestion;
import panetina.elarion.core.command.IdentitySuggestionSource;
import panetina.elarion.core.model.CitizenRecord;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mixin(ServerCommandSource.class)
public abstract class ServerCommandSourceMixin implements IdentitySuggestionSource {
    @Inject(method = "getPlayerNames", at = @At("RETURN"), cancellable = true)
    private void elarion$visiblePlayerNames(CallbackInfoReturnable<Set<String>> cir) {
        ServerCommandSource source = (ServerCommandSource) (Object) this;
        Set<String> names = new LinkedHashSet<>();
        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            if (!ElarionApi.get().identities().canSee(source, player)) continue;
            CitizenRecord citizen = ElarionApi.get().citizens().getOrCreate(player);
            if (citizen.nickname() != null
                    && !citizen.nickname().isBlank()
                    && citizen.nickname().chars().noneMatch(Character::isWhitespace)) {
                names.add(citizen.nickname());
            } else {
                names.add(player.getGameProfile().getName());
            }
        }
        cir.setReturnValue(names);
    }

    @Override
    public List<IdentitySuggestion> elarion$identitySuggestions() {
        ServerCommandSource source = (ServerCommandSource) (Object) this;
        List<IdentitySuggestion> suggestions = new ArrayList<>();
        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            if (!ElarionApi.get().identities().canSee(source, player)) continue;
            CitizenRecord citizen = ElarionApi.get().citizens().getOrCreate(player);
            String username = player.getGameProfile().getName();
            String display = citizen.nickname() == null || citizen.nickname().isBlank()
                    ? username
                    : citizen.nickname();
            List<String> aliases = citizen.nickname() == null || citizen.nickname().isBlank()
                    ? List.of(username)
                    : List.of(citizen.nickname(), username);
            suggestions.add(new IdentitySuggestion(username, display, aliases));
        }
        return suggestions;
    }
}
