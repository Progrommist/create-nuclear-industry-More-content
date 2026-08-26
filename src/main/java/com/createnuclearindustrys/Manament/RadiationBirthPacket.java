package com.createnuclearindustrys.Manament;

import com.createnuclearindustrys.CreateNuclearIndustrys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record RadiationBirthPacket(
    UUID id,
    double px, double py, double pz,
    double vx, double vy, double vz,
    float r, float g, float b,
    float energy,
    int lifetime,
    long sourcePos
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RadiationBirthPacket> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CreateNuclearIndustrys.MODID, "radiation_birth"));

    public static final StreamCodec<FriendlyByteBuf, RadiationBirthPacket> STREAM_CODEC = StreamCodec.of(
        (buf, p) -> {
            buf.writeUUID(p.id);
            buf.writeDouble(p.px); buf.writeDouble(p.py); buf.writeDouble(p.pz);
            buf.writeDouble(p.vx); buf.writeDouble(p.vy); buf.writeDouble(p.vz);
            buf.writeFloat(p.r);   buf.writeFloat(p.g);   buf.writeFloat(p.b);
            buf.writeFloat(p.energy);
            buf.writeInt(p.lifetime);
            buf.writeLong(p.sourcePos);
        },
        buf -> new RadiationBirthPacket(
            buf.readUUID(),
            buf.readDouble(), buf.readDouble(), buf.readDouble(),
            buf.readDouble(), buf.readDouble(), buf.readDouble(),
            buf.readFloat(),  buf.readFloat(),  buf.readFloat(),
            buf.readFloat(),
            buf.readInt(),
            buf.readLong()
        )
    );

    @Override
    public CustomPacketPayload.Type<RadiationBirthPacket> type() {
        return TYPE;
    }
}
