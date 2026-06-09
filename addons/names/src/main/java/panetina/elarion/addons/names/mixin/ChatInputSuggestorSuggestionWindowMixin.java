package panetina.elarion.addons.names.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import panetina.elarion.core.command.AdvancedIdentitySuggestion;

@Mixin(ChatInputSuggestor.SuggestionWindow.class)
public abstract class ChatInputSuggestorSuggestionWindowMixin {
    @WrapOperation(
            method = "complete",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/suggestion/Suggestion;getText()Ljava/lang/String;",
                    remap = false)
    )
    private String elarion$useCompletionLength(Suggestion suggestion, Operation<String> original) {
        if (suggestion instanceof AdvancedIdentitySuggestion advanced) {
            return advanced.completion();
        }
        return original.call(suggestion);
    }
}
