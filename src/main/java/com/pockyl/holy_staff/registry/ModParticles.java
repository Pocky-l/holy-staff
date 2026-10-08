package com.pockyl.holy_staff.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.pockyl.holy_staff.HolyStaff;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, HolyStaff.MOD_ID);

    /** Four-pointed golden star that twinkles and fades. */
    public static final RegistryObject<SimpleParticleType> HOLY_SPARKLE = PARTICLE_TYPES.register("holy_sparkle",
            () -> new SimpleParticleType(false));
    /** Soft glowing dot used for beams and rising light. */
    public static final RegistryObject<SimpleParticleType> HOLY_GLOW = PARTICLE_TYPES.register("holy_glow",
            () -> new SimpleParticleType(false));

    private ModParticles() {
    }

    public static void register(IEventBus modBus) {
        PARTICLE_TYPES.register(modBus);
    }
}
