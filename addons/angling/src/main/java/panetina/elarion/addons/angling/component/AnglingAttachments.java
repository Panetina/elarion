package panetina.elarion.addons.angling.component;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;
import panetina.elarion.addons.angling.ElarionAnglingAddon;

/** Persistent player-local projections that must save atomically with vanilla inventory state. */
public final class AnglingAttachments {
    public static final AttachmentType<AnglingBaitDebitCursor> BAIT_DEBIT_CURSOR = AttachmentRegistry
            .<AnglingBaitDebitCursor>builder()
            .persistent(AnglingBaitDebitCursor.CODEC)
            .copyOnDeath()
            .initializer(() -> AnglingBaitDebitCursor.EMPTY)
            .buildAndRegister(Identifier.of(ElarionAnglingAddon.MOD_ID, "bait_debit_cursor"));

    private AnglingAttachments() {
    }

    public static void initialize() {
        // Class initialization performs registration.
    }
}
