package com.pockyl.holy_staff.registry;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.pockyl.holy_staff.HolyStaff;

/** Sounds are built from CC0 sources (OpenGameArt "Cure Magic", Kenney) by the workspace's sound generator. */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HolyStaff.MOD_ID);

    public static final RegistryObject<SoundEvent> SKILL_SWITCH = register("skill_switch");
    public static final RegistryObject<SoundEvent> BLESSED_CAST = register("blessed_cast");
    public static final RegistryObject<SoundEvent> BLESSED_BURST = register("blessed_burst");
    public static final RegistryObject<SoundEvent> BEAM_START = register("beam_start");
    public static final RegistryObject<SoundEvent> BEAM_LOOP = register("beam_loop");
    public static final RegistryObject<SoundEvent> BEAM_END = register("beam_end");
    public static final RegistryObject<SoundEvent> SANCTUARY_PLANT = register("sanctuary_plant");
    public static final RegistryObject<SoundEvent> SANCTUARY_PULSE = register("sanctuary_pulse");
    public static final RegistryObject<SoundEvent> HEAL = register("heal");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(HolyStaff.id(name)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
