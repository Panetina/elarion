package panetina.elarion.addons.government.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;

/** Dedicated fixed-size protocol; heraldry never travels through text action fields. */
public record GovernmentHeraldrySavePayload(String realmId, String sessionId, byte[] pixels) implements CustomPayload {
    public static final Id<GovernmentHeraldrySavePayload> ID = new Id<>(Identifier.of("elarion_government", "save_heraldry"));
    public static final PacketCodec<PacketByteBuf, GovernmentHeraldrySavePayload> CODEC = PacketCodec.of(
            (p,b) -> { ElarionPacketCodecs.writeString(b,p.realmId,128); ElarionPacketCodecs.writeString(b,p.sessionId,64); b.writeBytes(p.pixels); },
            b -> { String realm=ElarionPacketCodecs.readString(b,128); String session=ElarionPacketCodecs.readString(b,64); byte[] pixels=new byte[1024]; b.readBytes(pixels); return new GovernmentHeraldrySavePayload(realm,session,pixels); });
    public GovernmentHeraldrySavePayload { realmId=realmId==null?"":realmId; sessionId=sessionId==null?"":sessionId; pixels=pixels==null?new byte[0]:pixels.clone(); if(pixels.length!=1024) throw new IllegalArgumentException("Heraldry must be 32x32 pixels."); }
    @Override public byte[] pixels(){return pixels.clone();}
    @Override public Id<? extends CustomPayload> getId(){return ID;}
}
