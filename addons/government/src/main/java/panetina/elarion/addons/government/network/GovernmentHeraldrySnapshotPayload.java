package panetina.elarion.addons.government.network;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import panetina.elarion.core.network.ElarionPacketCodecs;
public record GovernmentHeraldrySnapshotPayload(String realmId, long revision, byte[] pixels) implements CustomPayload {
 public static final Id<GovernmentHeraldrySnapshotPayload> ID=new Id<>(Identifier.of("elarion_government","heraldry_snapshot"));
 public static final PacketCodec<PacketByteBuf,GovernmentHeraldrySnapshotPayload> CODEC=PacketCodec.of((p,b)->{ElarionPacketCodecs.writeString(b,p.realmId,128);b.writeLong(p.revision);b.writeVarInt(p.pixels.length);b.writeBytes(p.pixels);},b->{String r=ElarionPacketCodecs.readString(b,128);long v=b.readLong();int n=ElarionPacketCodecs.readBoundedCount(b,1024);byte[] p=new byte[n];b.readBytes(p);return new GovernmentHeraldrySnapshotPayload(r,v,p);});
 public GovernmentHeraldrySnapshotPayload{realmId=realmId==null?"":realmId;pixels=pixels==null?new byte[0]:pixels.clone();if(pixels.length!=0&&pixels.length!=1024)throw new IllegalArgumentException("Heraldry must be 32x32 pixels.");}
 @Override public byte[] pixels(){return pixels.clone();}@Override public Id<? extends CustomPayload> getId(){return ID;}
}
