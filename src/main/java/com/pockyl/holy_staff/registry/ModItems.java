package com.pockyl.holy_staff.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.pockyl.holy_staff.HolyStaff;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HolyStaff.MOD_ID);

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
