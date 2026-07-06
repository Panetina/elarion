package panetina.elarion.core.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record CharacterRealmAssignmentPayload(
        String assignedRealmId,
        String assignedRealmName,
        List<Option> options
) implements CustomPayload {
    public static final Id<CharacterRealmAssignmentPayload> ID =
            new Id<>(Identifier.of("elarion_core", "character_realm_assignment"));
    public static final PacketCodec<PacketByteBuf, CharacterRealmAssignmentPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> {
                ElarionPacketCodecs.writeString(buffer, payload.assignedRealmId(), 64);
                ElarionPacketCodecs.writeString(buffer, payload.assignedRealmName(), 128);
                buffer.writeVarInt(payload.options().size());
                for (Option option : payload.options()) {
                    ElarionPacketCodecs.writeString(buffer, option.realmId(), 64);
                    ElarionPacketCodecs.writeString(buffer, option.displayName(), 128);
                    buffer.writeVarInt(option.population());
                    buffer.writeBoolean(option.assigned());
                }
            },
            buffer -> {
                String assignedRealmId = ElarionPacketCodecs.readString(buffer, 64);
                String assignedRealmName = ElarionPacketCodecs.readString(buffer, 128);
                int count = ElarionPacketCodecs.readBoundedCount(buffer, 16);
                ArrayList<Option> options = new ArrayList<>(count);
                for (int index = 0; index < count; index++) {
                    options.add(new Option(
                            ElarionPacketCodecs.readString(buffer, 64),
                            ElarionPacketCodecs.readString(buffer, 128),
                            buffer.readVarInt(),
                            buffer.readBoolean()));
                }
                return new CharacterRealmAssignmentPayload(assignedRealmId, assignedRealmName, options);
            });

    public CharacterRealmAssignmentPayload {
        assignedRealmId = clean(assignedRealmId);
        assignedRealmName = clean(assignedRealmName);
        options = List.copyOf(options == null ? List.of() : options);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public record Option(String realmId, String displayName, int population, boolean assigned) {
        public Option {
            realmId = clean(realmId);
            displayName = clean(displayName);
            population = Math.max(0, population);
        }
    }
}
