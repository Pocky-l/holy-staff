package com.pockyl.holy_staff;

import net.minecraftforge.common.ForgeConfigSpec;

/** Gameplay numbers (COMMON) and visuals (CLIENT). Accessors fall back to defaults while a config is not loaded. */
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue HEAL_MONSTERS = BUILDER
            .comment("Whether the staff heals hostile mobs (zombies, skeletons, ...).")
            .translation("holy_staff.configuration.healMonsters")
            .define("healMonsters", false);

    static {
        BUILDER.translation("holy_staff.configuration.blessedGround").push("blessedGround");
    }

    private static final ForgeConfigSpec.DoubleValue BLESSED_HEAL = BUILDER
            .comment("Health restored by Blessed Ground to every ally inside when it bursts.")
            .translation("holy_staff.configuration.blessedHeal")
            .defineInRange("amount", 7.0, 0.5, 100.0);
    private static final ForgeConfigSpec.DoubleValue BLESSED_RADIUS = BUILDER
            .comment("Radius of Blessed Ground in blocks.")
            .translation("holy_staff.configuration.blessedRadius")
            .defineInRange("radius", 3.0, 1.0, 16.0);
    private static final ForgeConfigSpec.DoubleValue BLESSED_DELAY = BUILDER
            .comment("Seconds between placing Blessed Ground and its burst (the circle fills up meanwhile).")
            .translation("holy_staff.configuration.blessedDelay")
            .defineInRange("delay", 0.6, 0.0, 10.0);
    private static final ForgeConfigSpec.DoubleValue BLESSED_RANGE = BUILDER
            .comment("How far away (in blocks) Blessed Ground can be placed.")
            .translation("holy_staff.configuration.blessedRange")
            .defineInRange("range", 20.0, 2.0, 64.0);
    private static final ForgeConfigSpec.DoubleValue BLESSED_COOLDOWN = BUILDER
            .comment("Blessed Ground cooldown in seconds.")
            .translation("holy_staff.configuration.blessedCooldown")
            .defineInRange("cooldown", 3.0, 0.0, 600.0);

    static {
        BUILDER.pop().translation("holy_staff.configuration.holyBeam").push("holyBeam");
    }

    private static final ForgeConfigSpec.DoubleValue BEAM_HEAL = BUILDER
            .comment("Health per second restored by Holy Beam to its target.")
            .translation("holy_staff.configuration.beamHeal")
            .defineInRange("healPerSecond", 8.0, 0.5, 200.0);
    private static final ForgeConfigSpec.DoubleValue BEAM_DURATION = BUILDER
            .comment("Maximum channel time of Holy Beam in seconds.")
            .translation("holy_staff.configuration.beamDuration")
            .defineInRange("duration", 3.0, 0.5, 30.0);
    private static final ForgeConfigSpec.DoubleValue BEAM_RANGE = BUILDER
            .comment("Range of Holy Beam in blocks.")
            .translation("holy_staff.configuration.beamRange")
            .defineInRange("range", 16.0, 2.0, 64.0);
    private static final ForgeConfigSpec.DoubleValue BEAM_SLOWDOWN = BUILDER
            .comment("Movement speed multiplier while channelling Holy Beam.")
            .translation("holy_staff.configuration.beamSlowdown")
            .defineInRange("movementMultiplier", 0.3, 0.0, 1.0);
    private static final ForgeConfigSpec.DoubleValue BEAM_COOLDOWN = BUILDER
            .comment("Holy Beam cooldown in seconds (starts when the channel starts).")
            .translation("holy_staff.configuration.beamCooldown")
            .defineInRange("cooldown", 10.0, 0.0, 600.0);

    static {
        BUILDER.pop().translation("holy_staff.configuration.sanctuary").push("sanctuary");
    }

    private static final ForgeConfigSpec.DoubleValue SANCTUARY_HEAL = BUILDER
            .comment("Health restored to every ally in the area by each Sanctuary pulse.")
            .translation("holy_staff.configuration.sanctuaryHeal")
            .defineInRange("healPerPulse", 2.5, 0.5, 100.0);
    private static final ForgeConfigSpec.DoubleValue SANCTUARY_PULSE = BUILDER
            .comment("Seconds between Sanctuary pulses.")
            .translation("holy_staff.configuration.sanctuaryPulse")
            .defineInRange("pulseInterval", 0.5, 0.1, 10.0);
    private static final ForgeConfigSpec.DoubleValue SANCTUARY_DURATION = BUILDER
            .comment("How long the caster channels Sanctuary, in seconds. The caster cannot move meanwhile.")
            .translation("holy_staff.configuration.sanctuaryDuration")
            .defineInRange("duration", 3.0, 0.5, 30.0);
    private static final ForgeConfigSpec.DoubleValue SANCTUARY_RADIUS = BUILDER
            .comment("Radius of the healing area in blocks.")
            .translation("holy_staff.configuration.sanctuaryRadius")
            .defineInRange("radius", 7.0, 1.0, 32.0);
    private static final ForgeConfigSpec.DoubleValue SANCTUARY_KNOCKBACK_RADIUS = BUILDER
            .comment("Enemies within this radius are thrown back when Sanctuary starts.")
            .translation("holy_staff.configuration.sanctuaryKnockbackRadius")
            .defineInRange("knockbackRadius", 5.0, 0.0, 32.0);
    private static final ForgeConfigSpec.DoubleValue SANCTUARY_KNOCKBACK = BUILDER
            .comment("Knockback strength of Sanctuary.")
            .translation("holy_staff.configuration.sanctuaryKnockback")
            .defineInRange("knockbackStrength", 1.5, 0.0, 10.0);
    private static final ForgeConfigSpec.DoubleValue SANCTUARY_COOLDOWN = BUILDER
            .comment("Sanctuary cooldown in seconds (starts when the channel starts).")
            .translation("holy_staff.configuration.sanctuaryCooldown")
            .defineInRange("cooldown", 20.0, 0.0, 3600.0);

    static {
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue SHOW_HEAL_NUMBERS = CLIENT_BUILDER
            .comment("Show floating numbers above healed entities.")
            .translation("holy_staff.configuration.showHealNumbers")
            .define("showHealNumbers", true);
    private static final ForgeConfigSpec.BooleanValue SHOW_SKILL_HUD = CLIENT_BUILDER
            .comment("Show the skill bar next to the hotbar while holding the staff.")
            .translation("holy_staff.configuration.showSkillHud")
            .define("showSkillHud", true);

    public static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    private Config() {
    }

    public static boolean healMonsters() {
        return get(SPEC, HEAL_MONSTERS);
    }

    public static float blessedHeal() {
        return get(SPEC, BLESSED_HEAL).floatValue();
    }

    public static float blessedRadius() {
        return get(SPEC, BLESSED_RADIUS).floatValue();
    }

    public static int blessedDelay() {
        return ticks(get(SPEC, BLESSED_DELAY));
    }

    public static double blessedRange() {
        return get(SPEC, BLESSED_RANGE);
    }

    public static int blessedGroundCooldown() {
        return ticks(get(SPEC, BLESSED_COOLDOWN));
    }

    public static float beamHealPerSecond() {
        return get(SPEC, BEAM_HEAL).floatValue();
    }

    public static int beamDuration() {
        return ticks(get(SPEC, BEAM_DURATION));
    }

    public static double beamRange() {
        return get(SPEC, BEAM_RANGE);
    }

    public static float beamSlowdown() {
        return get(SPEC, BEAM_SLOWDOWN).floatValue();
    }

    public static int beamCooldown() {
        return ticks(get(SPEC, BEAM_COOLDOWN));
    }

    public static float sanctuaryHeal() {
        return get(SPEC, SANCTUARY_HEAL).floatValue();
    }

    public static int sanctuaryPulse() {
        return Math.max(1, ticks(get(SPEC, SANCTUARY_PULSE)));
    }

    public static int sanctuaryDuration() {
        return ticks(get(SPEC, SANCTUARY_DURATION));
    }

    public static float sanctuaryRadius() {
        return get(SPEC, SANCTUARY_RADIUS).floatValue();
    }

    public static double sanctuaryKnockbackRadius() {
        return get(SPEC, SANCTUARY_KNOCKBACK_RADIUS);
    }

    public static double sanctuaryKnockback() {
        return get(SPEC, SANCTUARY_KNOCKBACK);
    }

    public static int sanctuaryCooldown() {
        return ticks(get(SPEC, SANCTUARY_COOLDOWN));
    }

    public static boolean showHealNumbers() {
        return get(CLIENT_SPEC, SHOW_HEAL_NUMBERS);
    }

    public static boolean showSkillHud() {
        return get(CLIENT_SPEC, SHOW_SKILL_HUD);
    }

    private static int ticks(double seconds) {
        return (int) Math.round(seconds * 20);
    }

    private static <T> T get(ForgeConfigSpec spec, ForgeConfigSpec.ConfigValue<T> value) {
        return spec.isLoaded() ? value.get() : value.getDefault();
    }
}
