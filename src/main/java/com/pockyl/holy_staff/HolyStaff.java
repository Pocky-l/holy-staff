package com.pockyl.holy_staff;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import com.pockyl.holy_staff.client.HolyStaffClient;
import com.pockyl.holy_staff.network.ModNetwork;
import com.pockyl.holy_staff.registry.ModAttachments;
import com.pockyl.holy_staff.registry.ModEntities;
import com.pockyl.holy_staff.registry.ModItems;
import com.pockyl.holy_staff.registry.ModParticles;
import com.pockyl.holy_staff.registry.ModSounds;
import com.pockyl.holy_staff.registry.PockyModsTab;

@Mod(HolyStaff.MOD_ID)
public final class HolyStaff {
    public static final String MOD_ID = "holy_staff";
    public static final Logger LOGGER = LogUtils.getLogger();

    // The no-argument constructor with FMLJavaModLoadingContext.get() also works on NeoForge 1.20.1.
    public HolyStaff() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modBus);
        ModEntities.register(modBus);
        ModParticles.register(modBus);
        ModAttachments.register(modBus);
        ModSounds.register(modBus);
        ModNetwork.register();
        modBus.addListener(ModItems::addToVanillaTabs);
        PockyModsTab.register(modBus, () -> new ItemStack(ModItems.HOLY_STAFF.get()), output -> {
            output.accept(ModItems.HOLY_STAFF.get());
            output.accept(ModItems.CREATIVE_HOLY_STAFF.get());
        });

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);

        // HolyStaffClient is only loaded when this branch runs, so a dedicated server never sees client classes.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            HolyStaffClient.init(modBus);
        }
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
