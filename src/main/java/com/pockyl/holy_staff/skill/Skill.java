package com.pockyl.holy_staff.skill;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import com.pockyl.holy_staff.Config;
import com.pockyl.holy_staff.HolyStaff;

import java.util.function.IntSupplier;

/**
 * The three skills of the staff, all cast with right click; the selected one is stored on the staff.
 * Ids (ordinals) are used in packets and the HUD order, names in saved data: only append new skills.
 */
public enum Skill implements StringRepresentable {
    BLESSED_GROUND("blessed_ground", 0xB6FF8C, Config::blessedGroundCooldown),
    HOLY_BEAM("holy_beam", 0xFFF08A, Config::beamCooldown),
    SANCTUARY("sanctuary", 0xFFC23D, Config::sanctuaryCooldown);

    private static final Skill[] VALUES = values();

    public static final Codec<Skill> CODEC = StringRepresentable.fromEnum(Skill::values);
    public static final StreamCodec<ByteBuf, Skill> STREAM_CODEC = ByteBufCodecs.idMapper(Skill::byId, Skill::ordinal);

    private final String name;
    private final int color;
    private final IntSupplier cooldown;

    Skill(String name, int color, IntSupplier cooldown) {
        this.name = name;
        this.color = color;
        this.cooldown = cooldown;
    }

    public static Skill byId(int id) {
        return VALUES[Math.floorMod(id, VALUES.length)];
    }

    /** The skill {@code steps} places further in the list, wrapping around. */
    public Skill cycle(int steps) {
        return byId(ordinal() + steps);
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String translationKey() {
        return "skill.holy_staff." + name;
    }

    public ResourceLocation icon() {
        return HolyStaff.id("textures/gui/skill/" + name + ".png");
    }

    /** RGB colour of the heal numbers and HUD accents of this skill. */
    public int color() {
        return color;
    }

    /** Cooldown in ticks from the current config. */
    public int cooldown() {
        return cooldown.getAsInt();
    }

    /** Channelled skills keep the caster busy for a while (and restrict movement) instead of acting at once. */
    public boolean isChannelled() {
        return this != BLESSED_GROUND;
    }
}
