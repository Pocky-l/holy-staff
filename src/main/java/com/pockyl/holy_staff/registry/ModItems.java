package com.pockyl.holy_staff.registry;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.item.HolyStaffItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HolyStaff.MOD_ID);

    public static final DeferredItem<HolyStaffItem> HOLY_STAFF = ITEMS.registerItem("holy_staff", HolyStaffItem::new,
            new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    public static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(HOLY_STAFF);
        }
    }
}
