package com.pockyl.holy_staff.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.pockyl.holy_staff.HolyStaff;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, HolyStaff.MOD_ID);

    /** Four-pointed golden star that twinkles and fades. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HOLY_SPARKLE = PARTICLE_TYPES.register("holy_sparkle",
            () -> new SimpleParticleType(false));
    /** Soft glowing dot used for beams and rising light. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HOLY_GLOW = PARTICLE_TYPES.register("holy_glow",
            () -> new SimpleParticleType(false));

    private ModParticles() {
    }

    public static void register(IEventBus modBus) {
        PARTICLE_TYPES.register(modBus);
    }
}
