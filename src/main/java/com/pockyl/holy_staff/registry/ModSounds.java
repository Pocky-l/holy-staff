package com.pockyl.holy_staff.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.pockyl.holy_staff.HolyStaff;

/** Sounds are built from CC0 sources (OpenGameArt "Cure Magic", Kenney) by the workspace's sound generator. */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, HolyStaff.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SKILL_SWITCH = register("skill_switch");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLESSED_CAST = register("blessed_cast");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLESSED_BURST = register("blessed_burst");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAM_START = register("beam_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAM_LOOP = register("beam_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAM_END = register("beam_end");
    public static final DeferredHolder<SoundEvent, SoundEvent> SANCTUARY_PLANT = register("sanctuary_plant");
    public static final DeferredHolder<SoundEvent, SoundEvent> SANCTUARY_PULSE = register("sanctuary_pulse");
    public static final DeferredHolder<SoundEvent, SoundEvent> HEAL = register("heal");

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(HolyStaff.id(name)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
