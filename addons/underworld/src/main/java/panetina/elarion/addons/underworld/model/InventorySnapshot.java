package panetina.elarion.addons.underworld.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A server-owned inventory boundary between the living worlds and the Afterlife.
 * It deliberately stores only vanilla inventory coordinates; addon-owned slots
 * must register their own boundary before they become available in the Afterlife.
 */
public final class InventorySnapshot {
    public List<StoredItemStack> stacks = new ArrayList<>();
    public int selectedHotbarSlot = -1;
    public int experienceLevel;
    public int totalExperience;
    public float experienceProgress;

    public boolean empty() {
        return (stacks == null || stacks.isEmpty())
                && experienceLevel <= 0 && totalExperience <= 0 && experienceProgress <= 0.0F;
    }

    public InventorySnapshot normalized() {
        if (stacks == null) stacks = new ArrayList<>();
        if (selectedHotbarSlot < -1 || selectedHotbarSlot > 8) selectedHotbarSlot = -1;
        if (experienceLevel < 0) experienceLevel = 0;
        if (totalExperience < 0) totalExperience = 0;
        if (experienceProgress < 0.0F || experienceProgress >= 1.0F) experienceProgress = 0.0F;
        return this;
    }
}
