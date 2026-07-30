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
        if (instances != null) {
            instances.forEach((id, instance) -> {
                if (validKey(id) && instance != null) copy.instances.put(id, instance);
            });
        }
        if (anchors != null) {
            anchors.forEach((id, anchor) -> {
                if (validKey(id) && anchor != null) copy.anchors.put(id, anchor);
            });
        }
        if (realmFlags != null) {
            realmFlags.forEach((realmId, flags) -> {
                if (!validKey(realmId) || flags == null) return;
                LinkedHashSet<String> validFlags = new LinkedHashSet<>();
                flags.forEach(flag -> {
                    if (validKey(flag)) validFlags.add(flag);
                });
                copy.realmFlags.put(realmId, validFlags);
            });
        }
        if (projectCounters != null) {
            projectCounters.forEach((projectId, counter) -> {
                if (validKey(projectId) && counter != null) copy.projectCounters.put(projectId, counter);
            });
        }
        if (donations != null) {
            donations.forEach((instanceId, records) -> {
                if (!validKey(instanceId) || records == null) return;
                ArrayList<OfferingDonationRecord> validRecords = new ArrayList<>();
                records.forEach(record -> {
                    if (record != null) validRecords.add(record);
                });
                copy.donations.put(instanceId, validRecords);
            });
        }
        return copy;
    }

    private static boolean validKey(String key) {
        return key != null && !key.isBlank();
    }
}
