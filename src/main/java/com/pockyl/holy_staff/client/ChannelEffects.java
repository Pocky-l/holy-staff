package com.pockyl.holy_staff.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;

import com.pockyl.holy_staff.Config;
import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.registry.ModParticles;
import com.pockyl.holy_staff.skill.Skill;

import java.util.Map;

/**
 * World visuals of channelled skills. Holy Beam: a throbbing ribbon of light with travelling pulses, flares at both
 * ends and a column of light around the target. Sanctuary: a turning rune circle, a dome of light, a shockwave when
 * the staff hits the ground, a ring and a dome flash with every heal pulse, rays along the rim and a pillar of light
 * at the staff.
 */
@EventBusSubscriber(modid = HolyStaff.MOD_ID, value = Dist.CLIENT)
public final class ChannelEffects {
    private static final int BEAM_OUTER = 0xFFE27A;
    private static final int BEAM_CORE = 0xFFFBEA;
    private static final float BEAM_SEGMENT = 0.35F;
    private static final int GOLD = 0xFFD45C;
    private static final int LIGHT = 0xFFF3C4;
    private static final float PULSE_TICKS = 10.0F;
    private static final float SHOCKWAVE_TICKS = 7.0F;
    private static final int SHAFTS = 8;

    private ChannelEffects() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ClientChannels.all().isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        Quaternionf cameraRotation = camera.rotation();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        long now = level.getGameTime();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();

        for (Map.Entry<Integer, ClientChannels.State> entry : ClientChannels.all().entrySet()) {
            Entity caster = level.getEntity(entry.getKey());
            ClientChannels.State state = entry.getValue();
            if (!(caster instanceof LivingEntity living)) {
                continue;
            }
            float alpha = state.alpha(now, partialTick);
            if (alpha <= 0) {
                continue;
            }
            float age = state.age(now, partialTick);
            poseStack.pushPose();
            if (state.skill() == Skill.HOLY_BEAM) {
                Entity target = level.getEntity(state.targetId());
                if (target != null) {
                    renderBeam(poseStack, buffers, cameraPos, cameraRotation, staffTip(living, partialTick), target, partialTick, age, alpha);
                }
            } else if (state.skill() == Skill.SANCTUARY) {
                Vec3 center = living.getPosition(partialTick).subtract(cameraPos);
                poseStack.translate(center.x, center.y, center.z);
                renderSanctuary(poseStack, buffers, cameraRotation, living, age, alpha);
            }
            poseStack.popPose();
        }
        buffers.endBatch();
    }

    private static VertexConsumer buffer(MultiBufferSource buffers, ResourceLocation texture) {
        return buffers.getBuffer(RenderType.entityTranslucentEmissive(texture));
    }

    private static void renderBeam(PoseStack poseStack, MultiBufferSource buffers, Vec3 cameraPos, Quaternionf cameraRotation,
            Vec3 tip, Entity target, float partialTick, float age, float alpha) {
        Vec3 a = tip.subtract(cameraPos);
        Vec3 b = target.getPosition(partialTick).add(0, target.getBbHeight() * 0.55, 0).subtract(cameraPos);
        double length = b.subtract(a).length();
        int segments = Math.max(1, (int) Math.ceil(length / BEAM_SEGMENT));
        VertexConsumer consumer = buffer(buffers, HolyShapes.BEAM);
        float throb = 1.0F + 0.15F * Mth.sin(age * 0.6F);
        for (int i = 0; i < segments; i++) {
            Vec3 p0 = a.lerp(b, (double) i / segments);
            Vec3 p1 = a.lerp(b, (double) (i + 1) / segments);
            // Bright pulses travel from the staff to the target.
            float wave0 = pulse(i, age);
            float wave1 = pulse(i + 1, age);
            HolyShapes.ribbon(poseStack, consumer, p0, p1, Vec3.ZERO, 0.6F * throb, BEAM_OUTER, alpha * (0.3F + 0.35F * wave0),
                    alpha * (0.3F + 0.35F * wave1));
            HolyShapes.ribbon(poseStack, consumer, p0, p1, Vec3.ZERO, 0.18F * throb, BEAM_CORE, alpha * (0.8F + 0.2F * wave0),
                    alpha * (0.8F + 0.2F * wave1));
        }

        // Flares at the staff and on the target.
        VertexConsumer flare = buffer(buffers, HolyShapes.FLARE);
        poseStack.pushPose();
        poseStack.translate(a.x, a.y, a.z);
        HolyShapes.billboard(poseStack, flare, cameraRotation, 0.45F * throb, age * 5, LIGHT, alpha);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(b.x, b.y, b.z);
        HolyShapes.billboard(poseStack, flare, cameraRotation, 0.7F * throb, -age * 4, LIGHT, 0.9F * alpha);
        poseStack.popPose();

        // The healed target stands in a soft column of light with a ring turning at its feet.
        Vec3 feet = target.getPosition(partialTick).subtract(cameraPos);
        float width = Math.max(0.5F, target.getBbWidth() * 0.8F);
        poseStack.pushPose();
        poseStack.translate(feet.x, feet.y + 0.02, feet.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 8));
        HolyShapes.cylinder(poseStack, buffer(buffers, HolyShapes.RAYS), width, target.getBbHeight() * 1.3F, 20, LIGHT, 0.55F * alpha);
        HolyShapes.flat(poseStack, buffer(buffers, HolyShapes.RUNES), width * 1.6F, GOLD, 0.8F * alpha);
        poseStack.popPose();
    }

    private static float pulse(int segment, float age) {
        return 0.5F + 0.5F * Mth.sin(segment * 0.9F - age * 0.8F);
    }

    private static void renderSanctuary(PoseStack poseStack, MultiBufferSource buffers, Quaternionf cameraRotation, LivingEntity caster,
            float age, float alpha) {
        float radius = Config.sanctuaryRadius();
        int pulse = Config.sanctuaryPulse();
        float sincePulse = age % pulse;
        float pulseGlow = sincePulse < PULSE_TICKS ? 1 - sincePulse / PULSE_TICKS : 0;

        // Rune circle and outline on the ground.
        poseStack.pushPose();
        poseStack.translate(0, 0.04, 0);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 1.5F));
        HolyShapes.flat(poseStack, buffer(buffers, HolyShapes.RUNES), radius * 1.05F, GOLD, 0.9F * alpha);
        poseStack.popPose();
        poseStack.translate(0, 0.004, 0);
        HolyShapes.flat(poseStack, buffer(buffers, HolyShapes.RING), radius, LIGHT, alpha);

        // A ring sweeps outwards with every heal pulse.
        if (pulseGlow > 0) {
            float t = 1 - pulseGlow;
            poseStack.translate(0, 0.004, 0);
            HolyShapes.flat(poseStack, buffer(buffers, HolyShapes.RING), radius * (0.1F + 0.9F * t), LIGHT, alpha * pulseGlow);
        }
        // Shockwave and flash when the staff hits the ground (the knockback moment).
        if (age < SHOCKWAVE_TICKS) {
            float t = age / SHOCKWAVE_TICKS;
            float knockback = (float) Config.sanctuaryKnockbackRadius();
            poseStack.translate(0, 0.004, 0);
            HolyShapes.flat(poseStack, buffer(buffers, HolyShapes.RING), Math.max(0.3F, knockback * t), 0xFFFFFF, 1 - t);
            HolyShapes.flat(poseStack, buffer(buffers, HolyShapes.DISC), Math.max(0.3F, knockback * t), LIGHT, 0.6F * (1 - t));
        }
        poseStack.popPose();

        // Dome of light, brighter for a moment with every pulse.
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-age * 0.8F));
        HolyShapes.dome(poseStack, buffer(buffers, HolyShapes.DOME), radius, 10, 48, GOLD, alpha * (0.45F + 0.4F * pulseGlow));
        poseStack.popPose();

        // Rays of light along the rim, turning slowly.
        VertexConsumer rays = buffer(buffers, HolyShapes.COLUMN);
        for (int i = 0; i < SHAFTS; i++) {
            float angle = Mth.TWO_PI * i / SHAFTS + age * 0.02F;
            poseStack.pushPose();
            poseStack.translate(Mth.cos(angle) * radius * 0.92F, 0, Mth.sin(angle) * radius * 0.92F);
            HolyShapes.cylinder(poseStack, rays, 0.12F, 2.2F + 0.6F * Mth.sin(age * 0.2F + i), 8, LIGHT, 0.7F * alpha);
            poseStack.popPose();
        }

        // Pillar of light where the staff touches the ground, with a flare at the crystal.
        Vec3 staff = plantedStaffOffset(caster);
        poseStack.pushPose();
        poseStack.translate(staff.x, 0, staff.z);
        HolyShapes.cylinder(poseStack, buffer(buffers, HolyShapes.RAYS), 0.35F, 5.0F, 16, LIGHT, 0.8F * alpha);
        HolyShapes.flat(poseStack, buffer(buffers, HolyShapes.DISC), 1.0F, LIGHT, 0.7F * alpha);
        poseStack.translate(0, 1.85, 0);
        HolyShapes.billboard(poseStack, buffer(buffers, HolyShapes.FLARE), cameraRotation, 0.6F + 0.5F * pulseGlow, age * 3, LIGHT,
                alpha);
        poseStack.popPose();
    }

    /** Where the planted staff stands, relative to the caster's feet. */
    private static Vec3 plantedStaffOffset(LivingEntity caster) {
        float yaw = caster.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw));
        Vec3 right = new Vec3(-forward.z, 0, forward.x);
        double side = mainArm(caster) == HumanoidArm.RIGHT ? 0.3 : -0.3;
        return forward.scale(0.55).add(right.scale(side));
    }

    /** Approximate world position of the staff crystal (pointed forward while casting). */
    static Vec3 staffTip(LivingEntity caster, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean firstPerson = caster == minecraft.player && minecraft.options.getCameraType().isFirstPerson();
        Vec3 look = caster.getViewVector(partialTick);
        Vec3 right = look.cross(new Vec3(0, 1, 0));
        right = right.lengthSqr() < 1.0E-6 ? new Vec3(1, 0, 0) : right.normalize();
        if (mainArm(caster) == HumanoidArm.LEFT) {
            right = right.scale(-1);
        }
        Vec3 eye = caster.getEyePosition(partialTick);
        return firstPerson
                ? eye.add(look.scale(1.0)).add(right.scale(0.3)).add(0, -0.2, 0)
                : eye.add(look.scale(1.7)).add(right.scale(0.35)).add(0, -0.3, 0);
    }

    private static HumanoidArm mainArm(LivingEntity entity) {
        return entity instanceof Player player ? player.getMainArm() : HumanoidArm.RIGHT;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || Minecraft.getInstance().isPaused()) {
            return;
        }
        RandomSource random = level.random;
        for (Map.Entry<Integer, ClientChannels.State> entry : ClientChannels.all().entrySet()) {
            Entity caster = level.getEntity(entry.getKey());
            ClientChannels.State state = entry.getValue();
            if (!(caster instanceof LivingEntity living)) {
                continue;
            }
            if (state.skill() == Skill.HOLY_BEAM) {
                Entity target = level.getEntity(state.targetId());
                if (target != null) {
                    beamParticles(level, random, staffTip(living, 1.0F), target);
                }
            } else if (state.skill() == Skill.SANCTUARY) {
                sanctuaryParticles(level, random, living);
            }
        }
    }

    private static void beamParticles(ClientLevel level, RandomSource random, Vec3 tip, Entity target) {
        ClientSkillEffects.burst(level, target, ModParticles.HOLY_GLOW.get(), 2);
        if (random.nextInt(2) == 0) {
            ClientSkillEffects.burst(level, target, ModParticles.HOLY_SPARKLE.get(), 1);
        }
        // Motes drifting along the beam towards the target.
        Vec3 end = target.position().add(0, target.getBbHeight() * 0.55, 0);
        Vec3 delta = end.subtract(tip);
        for (int i = 0; i < 2; i++) {
            Vec3 p = tip.add(delta.scale(random.nextDouble()));
            Vec3 v = delta.normalize().scale(0.15);
            level.addParticle(ModParticles.HOLY_SPARKLE.get(), p.x + (random.nextDouble() - 0.5) * 0.2, p.y + (random.nextDouble() - 0.5) * 0.2,
                    p.z + (random.nextDouble() - 0.5) * 0.2, v.x, v.y, v.z);
        }
    }

    private static void sanctuaryParticles(ClientLevel level, RandomSource random, LivingEntity caster) {
        float radius = Config.sanctuaryRadius();
        for (int i = 0; i < 5; i++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            double distance = Math.sqrt(random.nextDouble()) * radius;
            level.addParticle(i == 0 ? ModParticles.HOLY_SPARKLE.get() : ModParticles.HOLY_GLOW.get(),
                    caster.getX() + Math.cos(angle) * distance, caster.getY() + 0.1, caster.getZ() + Math.sin(angle) * distance,
                    0, 0.05 + random.nextDouble() * 0.06, 0);
        }
        Vec3 staff = caster.position().add(plantedStaffOffset(caster));
        level.addParticle(ModParticles.HOLY_SPARKLE.get(), staff.x + (random.nextDouble() - 0.5) * 0.4, staff.y + random.nextDouble() * 3,
                staff.z + (random.nextDouble() - 0.5) * 0.4, 0, 0.06, 0);
    }
}
