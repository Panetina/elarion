package panetina.elarion.core.command;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import panetina.elarion.core.api.ElarionApi;
import panetina.elarion.core.model.RealmDefinition;
import panetina.elarion.core.model.TitleDefinition;

import java.util.concurrent.CompletableFuture;

final class CommandSupport {
    private CommandSupport() {
    }

    static String value(String value) {
        return value == null || value.isBlank() ? "(none)" : value;
    }

    static java.util.UUID actorId(ServerCommandSource source) {
        return source.getEntity() instanceof ServerPlayerEntity actor ? actor.getUuid() : null;
    }

    static String displayCitizen(ElarionApi api, java.util.UUID uuid) {
        return api.citizens().find(uuid)
                .map(citizen -> citizen.nickname() == null || citizen.nickname().isBlank()
                        ? citizen.lastKnownUsername()
                        : citizen.nickname())
                .orElse(uuid.toString());
    }

    static CompletableFuture<Suggestions> suggestTitles(ElarionApi api, SuggestionsBuilder builder) {
        api.titles().all().stream()
                .map(TitleDefinition::id)
                .sorted()
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    static CompletableFuture<Suggestions> suggestDiplomacyRealms(ElarionApi api, SuggestionsBuilder builder) {
        api.realms().all().stream()
                .filter(realm -> api.governance().isDiplomacyEligible(realm.id()))
                .map(RealmDefinition::id)
                .sorted()
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    static boolean validateDiplomacyRealms(ServerCommandSource source, ElarionApi api, String... realmIds) {
        for (String realmId : realmIds) {
            if (!api.governance().isDiplomacyEligible(realmId)) {
                source.sendError(Text.literal("Unknown or diplomacy-excluded Realm: " + realmId));
                return false;
            }
        }
        return true;
    }

    static CompletableFuture<Suggestions> suggestProgressionRules(ElarionApi api, SuggestionsBuilder builder) {
        api.progression().ruleIds().forEach(builder::suggest);
        return builder.buildFuture();
    }
}
