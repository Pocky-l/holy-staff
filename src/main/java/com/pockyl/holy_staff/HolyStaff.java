package com.pockyl.holy_staff;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

import com.pockyl.holy_staff.network.ModNetwork;
import com.pockyl.holy_staff.registry.ModAttachments;
import com.pockyl.holy_staff.registry.ModDataComponents;
import com.pockyl.holy_staff.registry.ModEntities;
import com.pockyl.holy_staff.registry.ModItems;
import com.pockyl.holy_staff.registry.ModParticles;
import com.pockyl.holy_staff.registry.ModSounds;
import com.pockyl.holy_staff.registry.PockyModsTab;

@Mod(HolyStaff.MOD_ID)
public final class HolyStaff {
    public static final String MOD_ID = "holy_staff";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HolyStaff(IEventBus modBus, ModContainer container) {
        ModDataComponents.register(modBus);
        ModItems.register(modBus);
        ModEntities.register(modBus);
        ModParticles.register(modBus);
        ModAttachments.register(modBus);
        ModSounds.register(modBus);
        modBus.addListener(ModNetwork::register);
        modBus.addListener(ModItems::addToVanillaTabs);
        PockyModsTab.register(modBus, () -> new ItemStack(ModItems.HOLY_STAFF.get()), output -> {
            output.accept(ModItems.HOLY_STAFF);
            output.accept(ModItems.CREATIVE_HOLY_STAFF);
        });

        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
