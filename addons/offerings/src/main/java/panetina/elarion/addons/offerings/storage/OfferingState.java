package panetina.elarion.addons.offerings.storage;

import panetina.elarion.addons.offerings.model.OfferingAnchor;
import panetina.elarion.addons.offerings.model.OfferingInstance;
import panetina.elarion.addons.offerings.model.OfferingDonationRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class OfferingState {
    public Map<String, OfferingInstance> instances = new LinkedHashMap<>();
    public Map<String, OfferingAnchor> anchors = new LinkedHashMap<>();
    public Map<String, Set<String>> realmFlags = new LinkedHashMap<>();
    public Map<String, Integer> projectCounters = new LinkedHashMap<>();
    public Map<String, List<OfferingDonationRecord>> donations = new LinkedHashMap<>();

    public OfferingState copy() {
        OfferingState copy = new OfferingState();
        copy.instances = new LinkedHashMap<>(instances);
        copy.anchors = new LinkedHashMap<>(anchors);
        copy.realmFlags = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : realmFlags.entrySet()) {
            copy.realmFlags.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
        }
        copy.projectCounters = new LinkedHashMap<>(projectCounters);
        copy.donations = new LinkedHashMap<>();
        for (Map.Entry<String, List<OfferingDonationRecord>> entry : donations.entrySet()) {
            copy.donations.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
}
