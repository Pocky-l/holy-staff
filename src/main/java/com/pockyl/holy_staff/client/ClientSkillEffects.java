package com.pockyl.holy_staff.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import com.pockyl.holy_staff.network.SkillFxPayload;
import com.pockyl.holy_staff.registry.ModParticles;
import com.pockyl.holy_staff.skill.Skill;

/** One-off particle effects of cast skills, driven by {@link SkillFxPayload}. */
public final class ClientSkillEffects {
    private static final double STREAM_STEP = 0.35;

    private ClientSkillEffects() {
    }

    public static void handle(SkillFxPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity caster = level.getEntity(payload.casterId());
        Vec3 pos = new Vec3(payload.pos());
        if (Skill.byId(payload.skill()) == Skill.BLESSED_GROUND && caster instanceof LivingEntity living) {
            stream(level, ChannelEffects.staffTip(living, 1.0F), pos.add(0, 0.3, 0));
        }
    }

    /** Light flowing from the staff to the target spot: glowing dots drifting along the line, arriving one by one. */
    private static void stream(ClientLevel level, Vec3 from, Vec3 to) {
        Vec3 delta = to.subtract(from);
        int steps = Math.max(2, (int) (delta.length() / STREAM_STEP));
        RandomSource random = level.random;
        for (int i = 0; i <= steps; i++) {
            Vec3 p = from.add(delta.scale((double) i / steps));
            Vec3 drift = delta.normalize().scale(0.05);
            level.addParticle(ModParticles.HOLY_GLOW.get(), p.x, p.y, p.z,
                    drift.x + (random.nextDouble() - 0.5) * 0.02, drift.y + 0.01, drift.z + (random.nextDouble() - 0.5) * 0.02);
            if (i % 4 == 0) {
                level.addParticle(ModParticles.HOLY_SPARKLE.get(), p.x, p.y, p.z, 0, 0.02, 0);
            }
        }
    }

    /** Random particles over the entity's body, drifting up. Used for every heal. */
    static void burst(ClientLevel level, Entity entity, ParticleOptions particle, int count) {
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            double x = entity.getX() + (random.nextDouble() - 0.5) * entity.getBbWidth() * 1.4;
            double y = entity.getY() + random.nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (random.nextDouble() - 0.5) * entity.getBbWidth() * 1.4;
            level.addParticle(particle, x, y, z, 0, 0.03 + random.nextDouble() * 0.03, 0);
        }
    }
}
