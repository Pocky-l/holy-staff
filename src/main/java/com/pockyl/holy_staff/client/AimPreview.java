package com.pockyl.holy_staff.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.pockyl.holy_staff.Config;
import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.item.HolyStaffItem;
import com.pockyl.holy_staff.registry.ModAttachments;
import com.pockyl.holy_staff.skill.Skill;
import com.pockyl.holy_staff.skill.SkillCaster;

/**
 * Aiming help for Blessed Ground: while it is the selected skill, a translucent circle shows where it would land
 * (the same spot the server picks). Gold when ready, grey while on cooldown.
 */
@Mod.EventBusSubscriber(modid = HolyStaff.MOD_ID, value = Dist.CLIENT)
public final class AimPreview {
    private static final int READY = 0xFFE58A;
    private static final int COOLDOWN = 0x9A9A9A;

    private AimPreview() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator() || !ClientInputHandler.holdsStaff(player)
                || HolyStaffItem.selected(player.getMainHandItem()) != Skill.BLESSED_GROUND || ClientChannels.get(player) != null) {
            return;
        }

        float partialTick = event.getPartialTick();
        long gameTime = player.level().getGameTime();
        boolean ready = ModAttachments.cooldowns(player).isReady(Skill.BLESSED_GROUND, gameTime);
        Vec3 target = SkillCaster.groundTarget(player).subtract(event.getCamera().getPosition());
        float radius = Config.blessedRadius();
        float time = gameTime + partialTick;
        int color = ready ? READY : COOLDOWN;
        float breathe = 0.85F + 0.15F * Mth.sin(time * 0.15F);

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        poseStack.pushPose();
        poseStack.translate(target.x, target.y + 0.05, target.z);
        HolyShapes.flat(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(HolyShapes.DISC)), radius, color,
                0.18F * breathe);
        poseStack.translate(0, 0.004, 0);
        HolyShapes.flat(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(HolyShapes.RING)), radius, color,
                0.75F * breathe);
        poseStack.translate(0, 0.004, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 1.5F));
        HolyShapes.flat(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(HolyShapes.RUNES)), radius * 1.05F, color,
                0.3F * breathe);
        poseStack.popPose();
        buffers.endBatch();
    }
}
