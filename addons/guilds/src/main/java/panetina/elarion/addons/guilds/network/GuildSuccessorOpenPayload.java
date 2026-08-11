package panetina.elarion.addons.guilds.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Server-authorized, bounded successor choices shown only after Guildmaster dialogue. */
public record GuildSuccessorOpenPayload(String guildName, List<Candidate> candidates) implements CustomPayload {
    public static final Id<GuildSuccessorOpenPayload> ID = new Id<>(Identifier.of("elarion_guilds", "successor_open"));
    public static final PacketCodec<PacketByteBuf, GuildSuccessorOpenPayload> CODEC = PacketCodec.of(
            (payload, buffer) -> { ElarionPacketCodecs.writeString(buffer, payload.guildName, 96); buffer.writeVarInt(payload.candidates.size()); for (Candidate candidate : payload.candidates) candidate.write(buffer); },
            buffer -> { String guild = ElarionPacketCodecs.readString(buffer, 96); int count = ElarionPacketCodecs.readBoundedCount(buffer, 255); List<Candidate> candidates = new ArrayList<>(count); for (int i = 0; i < count; i++) candidates.add(Candidate.read(buffer)); return new GuildSuccessorOpenPayload(guild, candidates); });
    public GuildSuccessorOpenPayload { guildName = guildName == null ? "" : guildName; candidates = candidates == null ? List.of() : List.copyOf(candidates); if (candidates.size() > 255) throw new IllegalArgumentException("Successor selection exceeds its bounded contract."); }
    @Override public Id<? extends CustomPayload> getId() { return ID; }
    public record Candidate(UUID id, String name) { public Candidate { name = name == null ? "" : name; } void write(PacketByteBuf b) { b.writeUuid(id); ElarionPacketCodecs.writeString(b, name, 128); } static Candidate read(PacketByteBuf b) { return new Candidate(b.readUuid(), ElarionPacketCodecs.readString(b, 128)); } }
}
