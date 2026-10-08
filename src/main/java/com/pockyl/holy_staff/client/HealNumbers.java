package com.pockyl.holy_staff.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.pockyl.holy_staff.Config;
import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.network.HealPopupPayload;
import com.pockyl.holy_staff.registry.ModParticles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Green "+3.5" numbers flying out of healed entities: they pop out of the body in an arc (up and to a random side),
 * follow the entity, and fade out. Bigger heals get bigger numbers. Heals of the local player go to {@link SelfHealHud}.
 */
@Mod.EventBusSubscriber(modid = HolyStaff.MOD_ID, value = Dist.CLIENT)
public final class HealNumbers {
    private static final int LIFETIME = 30;
    private static final int MAX_POPUPS = 64;
    private static final float TEXT_SCALE = 0.035F;
    private static final int GREEN = 0x55FF55;
    private static final int OUTLINE = 0x0B3D0B;
    private static final double GRAVITY = 0.018;
    private static final double DRAG = 0.92;
    private static final RandomSource RANDOM = RandomSource.create();
    private static final List<Popup> POPUPS = new ArrayList<>();

    private HealNumbers() {
    }

    public static void handle(HealPopupPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(payload.entityId());
        if (entity == null) {
            return;
        }
        ClientSkillEffects.burst(minecraft.level, entity, ModParticles.HOLY_SPARKLE.get(), 3 + Math.min(12, Math.round(payload.amount() * 1.5F)));
        if (!Config.showHealNumbers()) {
            return;
        }
        if (entity == minecraft.player) {
            // Your own heals are shown next to your health bar instead.
            SelfHealHud.add(format(payload.amount()), payload.amount());
            return;
        }
        if (POPUPS.size() >= MAX_POPUPS) {
            POPUPS.remove(0);
        }
        Vec3 offset = new Vec3((RANDOM.nextDouble() - 0.5) * entity.getBbWidth() * 0.6, entity.getBbHeight() * 0.85,
                (RANDOM.nextDouble() - 0.5) * entity.getBbWidth() * 0.6);
        double angle = RANDOM.nextDouble() * Math.PI * 2;
        double speed = 0.05 + RANDOM.nextDouble() * 0.05;
        Vec3 velocity = new Vec3(Math.cos(angle) * speed, 0.2 + RANDOM.nextDouble() * 0.06, Math.sin(angle) * speed);
        float size = 1.0F + Math.min(10.0F, payload.amount()) * 0.04F;
        POPUPS.add(new Popup(entity, offset, velocity, entity.position(), format(payload.amount()), size));
    }

    static String format(float amount) {
        float rounded = Math.round(amount * 10) / 10.0F;
        return rounded == Math.floor(rounded) ? "+" + (int) rounded : String.format(Locale.ROOT, "+%.1f", rounded);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (Minecraft.getInstance().level == null) {
            POPUPS.clear();
            return;
        }
        Iterator<Popup> iterator = POPUPS.iterator();
        while (iterator.hasNext()) {
            Popup popup = iterator.next();
            popup.age++;
            popup.previousOffset = popup.offset;
            popup.offset = popup.offset.add(popup.velocity);
            popup.velocity = popup.velocity.scale(DRAG).subtract(0, GRAVITY, 0);
            if (popup.entity.isAlive()) {
                popup.lastPos = popup.entity.position();
            }
            if (popup.age >= LIFETIME) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || POPUPS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        float partialTick = event.getPartialTick();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        Font font = minecraft.font;

        for (Popup popup : POPUPS) {
            float age = popup.age + partialTick;
            float life = age / LIFETIME;
            Vec3 base = popup.entity.isAlive() ? popup.entity.getPosition(partialTick) : popup.lastPos;
            // A quick pop on spawn and a fade over the last third.
            float pop = age < 4 ? 1.7F - 0.7F * (age / 4) : 1.0F;
            float alpha = life < 0.66F ? 1.0F : Mth.clamp((1 - life) / 0.34F, 0, 1);
            if (alpha <= 0.02F) {
                continue;
            }
            float scale = TEXT_SCALE * pop * popup.size;
            Vec3 offset = popup.previousOffset.lerp(popup.offset, partialTick);
            Vec3 pos = base.add(offset).subtract(cameraPos);

            poseStack.pushPose();
            poseStack.translate(pos.x, pos.y, pos.z);
            poseStack.mulPose(camera.rotation());
            poseStack.scale(scale, -scale, scale);
            FormattedCharSequence text = Component.literal(popup.text).getVisualOrderText();
            int a = Math.max(4, Math.round(alpha * 255)) << 24;
            font.drawInBatch8xOutline(text, -font.width(text) / 2.0F, -4, a | GREEN, a | OUTLINE,
                    poseStack.last().pose(), buffers, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }
        buffers.endBatch();
    }

    private static final class Popup {
        private final Entity entity;
        private final String text;
        private final float size;
        private Vec3 offset;
        private Vec3 previousOffset;
        private Vec3 velocity;
        private Vec3 lastPos;
        private int age;

        private Popup(Entity entity, Vec3 offset, Vec3 velocity, Vec3 lastPos, String text, float size) {
            this.entity = entity;
            this.offset = offset;
            this.previousOffset = offset;
            this.velocity = velocity;
            this.lastPos = lastPos;
            this.text = text;
            this.size = size;
        }
    }
}
