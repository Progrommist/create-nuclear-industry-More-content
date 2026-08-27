package com.createnuclearindustrys.Fluid.SteamFluid;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Rising steam particle — uses the same generic_* sprites as the vanilla
 * cloud particle, scaled up large and tinted light cyan.
 *
 * Lifetime : 10–30 seconds (200–600 ticks)
 * Motion   : sustained upward rise (negative gravity adds lift every tick)
 */
public class SteamParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    private SteamParticle(ClientLevel level, double x, double y, double z,
                          double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.setSpriteFromAge(sprites); // start on the "big" sprite

        // Light cyan tint — the generic sprites are white, so this tints them
        this.rCol = 0.88f;
        this.gCol = 0.96f;
        this.bCol = 1.00f;
        this.alpha = 0.0f; // fade in

        // Large puff, same scale range vanilla cloud uses
        this.quadSize = 0.9f + this.random.nextFloat() * 0.6f;

        // 10–30 second lifetime
        this.lifetime = 200 + this.random.nextInt(400);

        // Upward velocity — maintained by negative gravity
        this.xd = vx + (this.random.nextDouble() - 0.5) * 0.05;
        this.yd = Math.max(vy, 0.04) + this.random.nextDouble() * 0.04;
        this.zd = vz + (this.random.nextDouble() - 0.5) * 0.05;

        // Negative gravity → adds upward force each tick so it continuously rises
        this.gravity = -0.08f;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Animate through sprites (large → small) the same way vanilla cloud does
        this.setSpriteFromAge(sprites);

        // Sustained lift: gravity = -0.08 → yd += 0.08 * 0.04 = +0.0032 per tick
        this.yd -= 0.04 * this.gravity;

        this.move(this.xd, this.yd, this.zd);

        // Dampen horizontal, very light vertical damping (gravity keeps it rising)
        this.xd *= 0.96;
        this.zd *= 0.96;
        this.yd *= 0.98;

        // Grow slightly as it rises
        if (this.age < this.lifetime * 0.6f) {
            this.quadSize += 0.004f;
        }

        // Alpha: fade in 0.5 s, hold at 0.45, fade out last 2 s
        int fadeInEnd  = 10;
        int fadeOutStart = this.lifetime - 40;
        if (this.age < fadeInEnd) {
            this.alpha = (float) this.age / fadeInEnd * 0.45f;
        } else if (this.age > fadeOutStart) {
            this.alpha = 0.45f * (1f - (float)(this.age - fadeOutStart) / 40f);
        } else {
            this.alpha = 0.45f;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        public Provider(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new SteamParticle(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}
