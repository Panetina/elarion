package panetina.elarion.addons.government.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.ArrayList;
import java.util.List;

/** Economy-authored tax values projected through the Government Seat UI. */
public record GovernmentTaxPolicySnapshotPayload(
        String realmId, long revision, String destinationLabel, List<Entry> entries
) implements CustomPayload {
    public static final Id<GovernmentTaxPolicySnapshotPayload> ID =
            new Id<>(Identifier.of("elarion_government", "tax_policy_snapshot"));
    public static final PacketCodec<PacketByteBuf, GovernmentTaxPolicySnapshotPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.realmId, 128);
                buffer.writeVarLong(payload.revision);
                ElarionPacketCodecs.writeString(buffer, payload.destinationLabel, 128);
                buffer.writeVarInt(payload.entries.size());
                for (Entry entry : payload.entries) entry.write(buffer);
            },
            buffer -> {
                String realmId = ElarionPacketCodecs.readString(buffer, 128);
                long revision = buffer.readVarLong();
                String destination = ElarionPacketCodecs.readString(buffer, 128);
                int count = ElarionPacketCodecs.readBoundedCount(buffer, 8);
                List<Entry> entries = new ArrayList<>(count);
                for (int index = 0; index < count; index++) entries.add(Entry.read(buffer));
                return new GovernmentTaxPolicySnapshotPayload(realmId, revision, destination, entries);
            });

    public GovernmentTaxPolicySnapshotPayload {
        realmId = realmId == null ? "" : realmId;
        destinationLabel = destinationLabel == null ? "" : destinationLabel;
        entries = entries == null ? List.of() : List.copyOf(entries);
        if (revision < 0L || entries.size() > 8) {
            throw new IllegalArgumentException("Government tax policy snapshot is invalid.");
        }
    }

    public int basisPoints(String categoryId) {
        return entries.stream().filter(entry -> entry.categoryId.equals(categoryId)).findFirst()
                .map(Entry::basisPoints).orElse(0);
    }

    @Override public Id<? extends CustomPayload> getId() { return ID; }

    public record Entry(String categoryId, String label, int basisPoints) {
        public Entry {
            categoryId = categoryId == null ? "" : categoryId;
            label = label == null ? "" : label;
            if (basisPoints < 0 || basisPoints > 2500) {
                throw new IllegalArgumentException("Tax rate must be between 0 and 2500 basis points.");
            }
        }

        void write(PacketByteBuf buffer) {
            ElarionPacketCodecs.writeString(buffer, categoryId, 48);
            ElarionPacketCodecs.writeString(buffer, label, 96);
            buffer.writeVarInt(basisPoints);
        }

        static Entry read(PacketByteBuf buffer) {
            return new Entry(ElarionPacketCodecs.readString(buffer, 48),
                    ElarionPacketCodecs.readString(buffer, 96), buffer.readVarInt());
        }
    }
}
