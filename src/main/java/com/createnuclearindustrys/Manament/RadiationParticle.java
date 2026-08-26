package com.createnuclearindustrys.Manament;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class RadiationParticle {
    public final UUID id;
    public Vec3 pos;
    public Vec3 vel;
    public final float r, g, b;
    public final float energy; // 0.0–1.0; used by radiation damage, chain reactions, etc.
    public int ticksLeft;
    public final BlockPos source;

    public RadiationParticle(UUID id, Vec3 pos, Vec3 vel, float r, float g, float b, float energy, int ticksLeft, BlockPos source) {
        this.id = id;
        this.pos = pos;
        this.vel = vel;
        this.r = r;
        this.g = g;
        this.b = b;
        this.energy = energy;
        this.ticksLeft = ticksLeft;
        this.source = source;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putDouble("px", pos.x);
        tag.putDouble("py", pos.y);
        tag.putDouble("pz", pos.z);
        tag.putDouble("vx", vel.x);
        tag.putDouble("vy", vel.y);
        tag.putDouble("vz", vel.z);
        tag.putFloat("r", r);
        tag.putFloat("g", g);
        tag.putFloat("b", b);
        tag.putFloat("energy", energy);
        tag.putInt("ticksLeft", ticksLeft);
        tag.putLong("source", source.asLong());
        return tag;
    }

    public static RadiationParticle load(CompoundTag tag) {
        return new RadiationParticle(
            tag.getUUID("id"),
            new Vec3(tag.getDouble("px"), tag.getDouble("py"), tag.getDouble("pz")),
            new Vec3(tag.getDouble("vx"), tag.getDouble("vy"), tag.getDouble("vz")),
            tag.getFloat("r"),
            tag.getFloat("g"),
            tag.getFloat("b"),
            tag.getFloat("energy"),
            tag.getInt("ticksLeft"),
            BlockPos.of(tag.getLong("source"))
        );
    }
}
