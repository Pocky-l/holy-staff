package com.pockyl.holy_staff.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.registry.ModEntities;
import com.pockyl.holy_staff.registry.ModItems;
import com.pockyl.holy_staff.registry.ModParticles;

@Mod(value = HolyStaff.MOD_ID, dist = Dist.CLIENT)
public final class HolyStaffClient {
    public HolyStaffClient(IEventBus modBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modBus.addListener(ModKeyMappings::register);
        modBus.addListener(HolyStaffClient::registerGuiLayers);
        modBus.addListener(HolyStaffClient::registerParticles);
        modBus.addListener(HolyStaffClient::registerRenderers);
        modBus.addListener(HolyStaffClient::registerClientExtensions);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, HolyStaff.id("skills"), new SkillHud());
        event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, HolyStaff.id("self_heal"), new SelfHealHud());
    }

    private static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.HOLY_SPARKLE.get(), HolyParticle.Sparkle::new);
        event.registerSpriteSet(ModParticles.HOLY_GLOW.get(), HolyParticle.Glow::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BLESSED_GROUND.get(), BlessedGroundRenderer::new);
    }

    // Rendering itself is handled by GeckoLib; this poses the arms holding the staff and while channelling.
    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return hand == InteractionHand.MAIN_HAND ? StaffPoses.armPose(entity) : StaffPoses.HOLD.getValue();
            }
        }, ModItems.HOLY_STAFF.get(), ModItems.CREATIVE_HOLY_STAFF.get());
    }
}
