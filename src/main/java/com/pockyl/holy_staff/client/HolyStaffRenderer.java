package com.pockyl.holy_staff.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.item.HolyStaffItem;
import com.pockyl.holy_staff.skill.Skill;

/**
 * Renders assets/holy_staff/{geo,animations,textures}/item/holy_staff.*; the crystal glows via the _glowmask texture.
 * The Creative Holy Staff shares model and animations and uses creative_holy_staff(_glowmask).png.
 * While the holder channels a skill, the staff is re-oriented to match the channel arm pose ({@link StaffPoses}).
 */
public final class HolyStaffRenderer extends GeoItemRenderer<HolyStaffItem> {
    public HolyStaffRenderer(boolean creative) {
        super(creative
                ? new DefaultedItemGeoModel<HolyStaffItem>(HolyStaff.id("holy_staff")).withAltTexture(HolyStaff.id("creative_holy_staff"))
                : new DefaultedItemGeoModel<>(HolyStaff.id("holy_staff")));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers,
            int packedLight, int packedOverlay) {
        Skill channel = channelOf(context);
        if (channel == null) {
            super.renderByItem(stack, context, poseStack, buffers, packedLight, packedOverlay);
            return;
        }
        poseStack.pushPose();
        StaffPoses.applyItemTransform(poseStack, context, channel);
        super.renderByItem(stack, context, poseStack, buffers, packedLight, packedOverlay);
        poseStack.popPose();
    }

    @Nullable
    private static Skill channelOf(ItemDisplayContext context) {
        LivingEntity holder;
        if (context.firstPerson()) {
            holder = Minecraft.getInstance().player;
        } else if (context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            holder = Holder.current;
        } else {
            return null;
        }
        if (holder == null || !(holder.getMainHandItem().getItem() instanceof HolyStaffItem)) {
            return null;
        }
        return ClientChannels.skillOf(holder);
    }

    /** The living entity being rendered right now, so held items can know who holds them. */
    @EventBusSubscriber(modid = HolyStaff.MOD_ID, value = Dist.CLIENT)
    public static final class Holder {
        @Nullable
        private static LivingEntity current;

        private Holder() {
        }

        @SubscribeEvent
        public static void onRenderPre(RenderLivingEvent.Pre<?, ?> event) {
            current = event.getEntity();
        }

        @SubscribeEvent
        public static void onRenderPost(RenderLivingEvent.Post<?, ?> event) {
            current = null;
        }
    }
}
