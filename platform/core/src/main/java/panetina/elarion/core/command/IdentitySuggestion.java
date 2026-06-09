package panetina.elarion.core.command;

import java.util.List;

public record IdentitySuggestion(String username, String displayName, List<String> aliases) {
    public IdentitySuggestion {
        aliases = List.copyOf(aliases);
    }
}
