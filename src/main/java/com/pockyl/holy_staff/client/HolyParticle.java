package com.pockyl.holy_staff.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/** Full-bright golden particles of the staff. Velocity comes from the spawn call. */
public abstract class HolyParticle extends TextureSheetParticle {
    private static final int FULL_BRIGHT = 0xF000F0;

    private final SpriteSet sprites;
    private final float baseSize;

    protected HolyParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites,
            float size, int lifetime, float friction) {
        super(level, x, y, z);
        this.sprites = sprites;
        xd = dx;
        yd = dy;
        zd = dz;
        this.friction = friction;
        gravity = 0;
        hasPhysics = false;
        this.lifetime = lifetime + random.nextInt(Math.max(1, lifetime / 3));
        baseSize = size * (0.8F + random.nextFloat() * 0.4F);
        quadSize = baseSize;
        // Warm white to gold.
        float warmth = random.nextFloat();
        rCol = 1.0F;
        gCol = 0.85F + 0.15F * (1 - warmth);
        bCol = 0.45F + 0.5F * (1 - warmth);
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) {
            setSpriteFromAge(sprites);
            float life = (float) age / lifetime;
            alpha = life < 0.7F ? 1.0F : 1.0F - (life - 0.7F) / 0.3F;
            quadSize = baseSize * sizeFactor(life);
        }
    }

    protected abstract float sizeFactor(float life);

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }

    /** Four-pointed star that twinkles. */
    public static final class Sparkle implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Sparkle(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy,
                double dz) {
            return new HolyParticle(level, x, y, z, dx, dy, dz, sprites, 0.12F, 20, 0.9F) {
                private final float phase = random.nextFloat() * Mth.TWO_PI;

                @Override
                protected float sizeFactor(float life) {
                    return (0.75F + 0.35F * Mth.sin(age * 0.9F + phase)) * (1 - life * 0.5F);
                }
            };
        }
    }

    /** Soft glowing dot for beams, waves and rising light. */
    public static final class Glow implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Glow(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy,
                double dz) {
            return new HolyParticle(level, x, y, z, dx, dy, dz, sprites, 0.08F, 16, 0.92F) {
                @Override
                protected float sizeFactor(float life) {
                    return 1 - life * 0.6F;
                }
            };
        }
    }
}
