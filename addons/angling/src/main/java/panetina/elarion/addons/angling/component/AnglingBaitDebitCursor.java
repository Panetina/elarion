package panetina.elarion.addons.angling.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable player-file cursor paired atomically with the inventory it debits. */
public record AnglingBaitDebitCursor(Map<Identifier, Long> appliedTotals) {
    public static final int MAX_BAIT_TYPES = 64;
    public static final AnglingBaitDebitCursor EMPTY = new AnglingBaitDebitCursor(Map.of());
    public static final Codec<AnglingBaitDebitCursor> CODEC = Codec.unboundedMap(Identifier.CODEC, Codec.LONG)
            .flatXmap(AnglingBaitDebitCursor::decode, value -> DataResult.success(value.appliedTotals));

    public AnglingBaitDebitCursor {
        appliedTotals = Map.copyOf(appliedTotals);
        if (appliedTotals.size() > MAX_BAIT_TYPES) throw new IllegalArgumentException("too many bait debit cursors");
        if (appliedTotals.values().stream().anyMatch(value -> value == null || value < 0)) {
            throw new IllegalArgumentException("bait debit cursors must be non-negative");
        }
    }

    public long applied(Identifier baitId) {
        return appliedTotals.getOrDefault(baitId, 0L);
    }

    public AnglingBaitDebitCursor withApplied(Identifier baitId, long total) {
        if (total < applied(baitId)) throw new IllegalArgumentException("bait debit cursor cannot move backwards");
        LinkedHashMap<Identifier, Long> updated = new LinkedHashMap<>(appliedTotals);
        updated.put(baitId, total);
        return new AnglingBaitDebitCursor(updated);
    }

    private static DataResult<AnglingBaitDebitCursor> decode(Map<Identifier, Long> values) {
        try {
            return DataResult.success(new AnglingBaitDebitCursor(values));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }
}
