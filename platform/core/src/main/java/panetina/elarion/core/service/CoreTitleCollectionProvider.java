package panetina.elarion.core.service;

import net.minecraft.server.network.ServerPlayerEntity;
import panetina.elarion.core.model.CitizenRecord;
import panetina.elarion.core.model.ElarionCollectionAction;
import panetina.elarion.core.model.ElarionCollectionEntry;
import panetina.elarion.core.model.ElarionCollectionTab;
import panetina.elarion.core.model.TitleDefinition;
import panetina.elarion.core.model.TitleOwnershipMode;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class CoreTitleCollectionProvider implements ElarionCollectionService.TabProvider {
    private static final String TAB_ID = "titles";
    private static final String SET_ACTIVE = "set_active";
    private static final String ICON = "minecraft:textures/item/name_tag.png";

    private final CitizenService citizens;
    private final TitleService titles;

    public CoreTitleCollectionProvider(CitizenService citizens, TitleService titles) {
        this.citizens = citizens;
        this.titles = titles;
    }

    @Override
    public String id() {
        return TAB_ID;
    }

    @Override
    public ElarionCollectionTab snapshot(ServerPlayerEntity player) {
        CitizenRecord citizen = citizens.getOrCreate(player);
        List<ElarionCollectionEntry> entries = titles.all().stream()
                .filter(title -> !title.hiddenFromDiscovery() || citizen.hasUnlockedTitle(title.id()))
                .sorted(Comparator.comparingInt(TitleDefinition::priority).reversed()
                        .thenComparing(TitleDefinition::id))
                .map(title -> entry(citizen, title))
                .toList();
        return new ElarionCollectionTab(
                TAB_ID,
                "Titles",
                "Choose the title shown on your character.",
                entries);
    }

    @Override
    public ElarionCollectionService.ActionResult act(ServerPlayerEntity player, String entryId, String actionId) {
        if (!SET_ACTIVE.equals(actionId)) {
            return ElarionCollectionService.ActionResult.failure("Unknown title action.");
        }
        TitleService.TitleOperation result = titles.setActive(player, entryId, player.getUuid(), "collection-menu");
        return result.success()
                ? ElarionCollectionService.ActionResult.success("")
                : ElarionCollectionService.ActionResult.failure(result.message());
    }

    private ElarionCollectionEntry entry(CitizenRecord citizen, TitleDefinition title) {
        boolean unlocked = citizen.hasUnlockedTitle(title.id());
        boolean active = title.id().equals(citizen.activeTitleId());
        return new ElarionCollectionEntry(
                title.id(),
                title.displayName(),
                unlocked ? unlockedSubtitle(title) : lockedSubtitle(title),
                body(title, unlocked),
                unlocked ? "Unlocked" : "Locked",
                ICON,
                unlocked,
                active,
                active ? List.of() : List.of(new ElarionCollectionAction(SET_ACTIVE, "Set as active", unlocked)));
    }

    private static String unlockedSubtitle(TitleDefinition title) {
        return title.ownershipMode() == TitleOwnershipMode.GLOBALLY_UNIQUE
                ? "Unique title."
                : "Ready to display.";
    }

    private static String lockedSubtitle(TitleDefinition title) {
        return switch (title.acquisitionMode()) {
            case DEFAULT -> "Default title.";
            case ADMIN_ONLY -> "Granted by staff.";
            case DISCOVERABLE -> "Discovered in the world.";
            case PROGRESSION -> "Unlocked by progression.";
            case ADDON -> "Unlocked by another system.";
        };
    }

    private static String body(TitleDefinition title, boolean unlocked) {
        String description = title.description().isBlank() ? "No title description configured." : title.description();
        StringBuilder body = new StringBuilder(description);
        body.append("\n\n");
        body.append(unlocked ? "You have unlocked this title." : "Unlock: ");
        if (!unlocked) {
            body.append(lockedSubtitle(title).toLowerCase(Locale.ROOT));
        }
        if (title.ownershipMode() == TitleOwnershipMode.GLOBALLY_UNIQUE) {
            body.append("\n\nOnly one living character can hold this title.");
        } else if (title.ownershipMode() == TitleOwnershipMode.ONE_PER_PLAYER) {
            body.append("\n\nThis title has one active owner per player.");
        }
        return body.toString();
    }
}
