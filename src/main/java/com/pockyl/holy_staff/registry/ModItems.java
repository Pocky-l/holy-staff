package com.pockyl.holy_staff.registry;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.item.HolyStaffItem;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HolyStaff.MOD_ID);

    public static final RegistryObject<HolyStaffItem> HOLY_STAFF = ITEMS.register("holy_staff",
            () -> new HolyStaffItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), false));

    /** Creative only: not craftable, no cooldowns, double heals, double beam range. */
    public static final RegistryObject<HolyStaffItem> CREATIVE_HOLY_STAFF = ITEMS.register("creative_holy_staff",
            () -> new HolyStaffItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), true));

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(HOLY_STAFF);
            event.accept(CREATIVE_HOLY_STAFF);
        }
    }
}
