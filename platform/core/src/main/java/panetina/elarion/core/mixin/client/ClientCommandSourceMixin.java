package panetina.elarion.core.mixin.client;

import net.minecraft.client.network.ClientCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import panetina.elarion.core.client.ClientIdentity;
import panetina.elarion.core.client.ClientIdentityCache;
import panetina.elarion.core.command.IdentitySuggestion;
import panetina.elarion.core.command.IdentitySuggestionSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

@Mixin(ClientCommandSource.class)
public abstract class ClientCommandSourceMixin implements IdentitySuggestionSource {
    @Inject(method = "getPlayerNames", at = @At("RETURN"), cancellable = true)
    private void elarion$visiblePlayerNames(CallbackInfoReturnable<Collection<String>> cir) {
        Collection<String> names = new LinkedHashSet<>();
        for (ClientIdentity identity : ClientIdentityCache.all()) {
            if (!identity.visible()) continue;
            names.add(identity.hasSimpleNickname() ? identity.nickname() : identity.username());
        }
        cir.setReturnValue(names);
    }

    @Override
    public List<IdentitySuggestion> elarion$identitySuggestions() {
        List<IdentitySuggestion> suggestions = new ArrayList<>();
        for (ClientIdentity identity : ClientIdentityCache.all()) {
            if (!identity.visible()) continue;
            String display = identity.nickname().isBlank() ? identity.username() : identity.nickname();
            List<String> aliases = identity.nickname().isBlank()
                    ? List.of(identity.username())
                    : List.of(identity.nickname(), identity.username());
            suggestions.add(new IdentitySuggestion(
                    identity.username(),
                    display,
                    aliases));
        }
        return suggestions;
    }
}
