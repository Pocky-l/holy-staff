package com.pockyl.holy_staff.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import com.pockyl.holy_staff.registry.ModSounds;
import com.pockyl.holy_staff.skill.Skill;

/** The Holy Beam hum: follows the caster, fades in when the beam starts and out when it ends. */
public final class BeamSoundInstance extends AbstractTickableSoundInstance {
    private static final float MAX_VOLUME = 0.7F;
    private static final float FADE_IN = 0.15F;
    private static final float FADE_OUT = 0.12F;

    private final Player player;
    private final boolean own;

    public BeamSoundInstance(Player player) {
        super(ModSounds.BEAM_LOOP.get(), SoundSource.PLAYERS, player.getRandom());
        this.player = player;
        // A source exactly at the listener has no direction and flips between the ears when turning: play our own
        // beam centred instead.
        this.own = player == Minecraft.getInstance().player;
        this.relative = own;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        updatePosition();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        boolean active = !player.isRemoved() && ClientChannels.skillOf(player) == Skill.HOLY_BEAM;
        if (active) {
            volume = Math.min(MAX_VOLUME, volume + FADE_IN);
        } else {
            volume -= FADE_OUT;
            if (volume <= 0.0F) {
                stop();
                return;
            }
        }
        updatePosition();
    }

    private void updatePosition() {
        if (own) {
            x = 0;
            y = 0;
            z = 0;
        } else {
            x = player.getX();
            y = player.getEyeY();
            z = player.getZ();
        }
    }
}
