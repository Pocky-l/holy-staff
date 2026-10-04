package com.pockyl.holy_staff.registry;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.skill.Skill;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, HolyStaff.MOD_ID);

    /** The skill a staff casts on right click. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Skill>> SELECTED_SKILL = DATA_COMPONENTS.registerComponentType(
            "selected_skill", builder -> builder.persistent(Skill.CODEC).networkSynchronized(Skill.STREAM_CODEC));

    private ModDataComponents() {
    }

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }
}
