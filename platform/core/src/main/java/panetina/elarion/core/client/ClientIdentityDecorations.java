package panetina.elarion.core.client;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public final class ClientIdentityDecorations {
    private static final List<Function<UUID, Text>> TAB_SUFFIXES = new CopyOnWriteArrayList<>();

    private ClientIdentityDecorations() {
    }

    public static void registerTabSuffix(Function<UUID, Text> provider) {
        if (provider != null) {
            TAB_SUFFIXES.add(provider);
        }
    }

    public static Text tabSuffix(UUID uuid) {
        MutableText text = Text.empty();
        for (Function<UUID, Text> provider : TAB_SUFFIXES) {
            Text suffix = provider.apply(uuid);
            if (suffix != null) {
                text.append(suffix);
            }
        }
        return text;
    }
}
