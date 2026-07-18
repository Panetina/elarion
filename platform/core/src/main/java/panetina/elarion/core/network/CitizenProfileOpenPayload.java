package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.model.ElarionCollectionSnapshot;
import panetina.elarion.core.model.profile.CitizenProfileSnapshot;

public record CitizenProfileOpenPayload(
        ElarionCollectionSnapshot collection,
        CitizenProfileSnapshot profile
) implements CustomPayload {
    public static final Id<CitizenProfileOpenPayload> ID =
            new Id<>(Identifier.of("elarion_core", "citizen_profile_open"));
    public static final PacketCodec<PacketByteBuf, CitizenProfileOpenPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                CollectionOpenPayload.writeSnapshot(payload.collection(), buffer);
                CitizenProfileSnapshotPayload.writeSnapshot(payload.profile(), buffer);
            },
            buffer -> new CitizenProfileOpenPayload(
                    CollectionOpenPayload.readSnapshot(buffer),
                    CitizenProfileSnapshotPayload.readSnapshot(buffer)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
