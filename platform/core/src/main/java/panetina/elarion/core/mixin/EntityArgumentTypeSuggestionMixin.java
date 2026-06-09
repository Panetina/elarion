package panetina.elarion.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import panetina.elarion.core.command.AdvancedIdentitySuggestion;
import panetina.elarion.core.command.IdentitySuggestion;
import panetina.elarion.core.command.IdentitySuggestionSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import static net.minecraft.command.CommandSource.shouldSuggest;

@Mixin(EntityArgumentType.class)
public abstract class EntityArgumentTypeSuggestionMixin {
    @ModifyExpressionValue(
            method = "method_9311",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/command/CommandSource;getPlayerNames()Ljava/util/Collection;")
    )
    private Collection<String> elarion$replaceVanillaPlayerSuggestions(Collection<String> original) {
        return new ArrayList<>();
    }

    @Inject(
            method = "method_9311",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/command/CommandSource;suggestMatching(Ljava/lang/Iterable;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;)Ljava/util/concurrent/CompletableFuture;")
    )
    private void elarion$addIdentitySuggestions(
            CommandSource source,
            SuggestionsBuilder builder,
            CallbackInfo ci
    ) {
        if (!(source instanceof IdentitySuggestionSource identities)) return;

        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (IdentitySuggestion identity : identities.elarion$identitySuggestions()) {
            if (identity.aliases().stream()
                    .map(alias -> alias.toLowerCase(Locale.ROOT))
                    .noneMatch(alias -> shouldSuggest(remaining, alias))) {
                continue;
            }

            ((SuggestionsBuilderAccessor) builder).elarion$result().add(
                    new AdvancedIdentitySuggestion(
                            StringRange.between(builder.getStart(), builder.getInput().length()),
                            identity.displayName(),
                            identity.username(),
                            identity.aliases()));
        }
    }
}
