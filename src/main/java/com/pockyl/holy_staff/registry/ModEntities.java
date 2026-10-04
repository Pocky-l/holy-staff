package com.pockyl.holy_staff.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.entity.BlessedGround;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, HolyStaff.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<BlessedGround>> BLESSED_GROUND = ENTITY_TYPES.register("blessed_ground",
            () -> EntityType.Builder.<BlessedGround>of(BlessedGround::new, MobCategory.MISC)
                    .sized(1.0F, 0.1F)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(HolyStaff.id("blessed_ground").toString()));

    private ModEntities() {
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
