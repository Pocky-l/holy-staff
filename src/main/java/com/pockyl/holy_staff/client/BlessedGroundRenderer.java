package com.pockyl.holy_staff.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.pockyl.holy_staff.entity.BlessedGround;

/**
 * Blessed Ground: a rune circle appears at once and light fills it from the centre with a bright leading edge; when
 * full, the circle flashes, a shockwave runs outwards and a wall of light rays shoots up, then everything fades.
 */
public final class BlessedGroundRenderer extends EntityRenderer<BlessedGround> {
    private static final int GOLD = 0xFFD86A;
    private static final int LIGHT = 0xFFF6D6;
    private static final int GREEN_LIGHT = 0xE8FFC8;
    private static final float COLUMN_HEIGHT = 2.6F;

    public BlessedGroundRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BlessedGround ground, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
            int packedLight) {
        float radius = ground.radius();
        float age = ground.tickCount + partialTick;
        float sinceBurst = ground.sinceBurst(partialTick);
        float fadeOut = sinceBurst < 0 ? 1.0F : Mth.clamp(1 - sinceBurst / BlessedGround.FADE_TICKS, 0, 1);
        float fadeIn = Mth.clamp(age / 3.0F, 0, 1);
        float visible = fadeIn * fadeOut;

        poseStack.pushPose();
        poseStack.translate(0, 0.03, 0);

        // Rune circle turning slowly, plus a crisp outline.
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 3.0F));
        HolyShapes.flat(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(HolyShapes.RUNES)), radius * 1.08F, GOLD,
                0.9F * visible);
        poseStack.popPose();
        poseStack.translate(0, 0.004, 0);
        HolyShapes.flat(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(HolyShapes.RING)), radius, LIGHT, visible);
        poseStack.translate(0, 0.004, 0);

        if (sinceBurst < 0) {
            // Filling up: an eased disc growing from the centre with a bright ring at its edge.
            float fill = ground.fill(partialTick);
            float eased = 1 - (1 - fill) * (1 - fill);
            HolyShapes.flat(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(HolyShapes.DISC)), radius * eased,
                    GREEN_LIGHT, (0.3F + 0.4F * fill) * fadeIn);
            poseStack.translate(0, 0.004, 0);
            HolyShapes.flat(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(HolyShapes.RING)), radius * eased, LIGHT,
                    fadeIn);
        } else {
            float t = 1 - fadeOut;
            // Flash of the whole circle and a shockwave running past the rim.
            HolyShapes.flat(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(HolyShapes.DISC)), radius, LIGHT,
                    fadeOut * fadeOut);
            poseStack.translate(0, 0.004, 0);
            HolyShapes.flat(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(HolyShapes.RING)), radius * (1 + 0.5F * t),
                    LIGHT, fadeOut);
            // Light rays shooting up from the circle, rising and thinning.
            float rise = 1 - (1 - t) * (1 - t) * (1 - t);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(age * 6.0F));
            HolyShapes.cylinder(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(HolyShapes.RAYS)), radius * 0.98F,
                    COLUMN_HEIGHT * (0.6F + 0.8F * rise), 40, LIGHT, fadeOut);
            HolyShapes.cylinder(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(HolyShapes.COLUMN)), radius * 0.6F,
                    COLUMN_HEIGHT * (0.4F + 0.6F * rise), 32, GREEN_LIGHT, 0.6F * fadeOut);
            poseStack.popPose();
        }
        poseStack.popPose();

        // A glow in the centre that brightens as the circle fills and peaks at the burst.
        float glow = sinceBurst < 0 ? ground.fill(partialTick) * 0.7F : fadeOut;
        poseStack.pushPose();
        poseStack.translate(0, 0.6, 0);
        HolyShapes.billboard(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(HolyShapes.FLARE)),
                entityRenderDispatcher.cameraOrientation(), 0.6F + 0.8F * glow, age * 4.0F, LIGHT, glow * fadeIn);
        poseStack.popPose();
        super.render(ground, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BlessedGround ground) {
        return HolyShapes.RING;
    }
}
