package com.pockyl.holy_staff.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.IEventBus;

import com.pockyl.holy_staff.item.HolyStaffItem;
import com.pockyl.holy_staff.registry.ModEntities;
import com.pockyl.holy_staff.registry.ModParticles;

/** Client-only setup, called from the mod constructor on the physical client. */
public final class HolyStaffClient {
    private HolyStaffClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(ModKeyMappings::register);
        modBus.addListener(HolyStaffClient::registerGuiOverlays);
        modBus.addListener(HolyStaffClient::registerParticles);
        modBus.addListener(HolyStaffClient::registerRenderers);
    }

    private static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "skills", new SkillHud());
        event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "self_heal", new SelfHealHud());
    }

    private static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.HOLY_SPARKLE.get(), HolyParticle.Sparkle::new);
        event.registerSpriteSet(ModParticles.HOLY_GLOW.get(), HolyParticle.Glow::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BLESSED_GROUND.get(), BlessedGroundRenderer::new);
    }

    /** The GeckoLib renderer of the staff, and the arm poses holding it and while channelling. */
    public static IClientItemExtensions itemExtensions(HolyStaffItem item) {
        return new IClientItemExtensions() {
            private HolyStaffRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new HolyStaffRenderer(item.isCreative());
                }
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return hand == InteractionHand.MAIN_HAND ? StaffPoses.armPose(entity) : StaffPoses.HOLD;
            }
        };
    }
}
