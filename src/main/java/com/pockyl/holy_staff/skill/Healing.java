package com.pockyl.holy_staff.skill;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;

import com.pockyl.holy_staff.Config;
import com.pockyl.holy_staff.network.HealPopupPayload;
import com.pockyl.holy_staff.network.ModNetwork;

/** Who the staff may heal, and healing with a floating number for everyone who can see the target. */
public final class Healing {
    private static final float MIN_SHOWN = 0.05F;

    private Healing() {
    }

    public static boolean canHeal(LivingEntity entity) {
        if (!entity.isAlive() || entity.isSpectator() || entity instanceof ArmorStand) {
            return false;
        }
        return !(entity instanceof Enemy) || Config.healMonsters();
    }

    /** Heals the target and returns the health actually restored (0 when it was already at full health). */
    public static float heal(LivingEntity target, float amount, Skill skill) {
        float before = target.getHealth();
        target.heal(amount);
        float healed = Math.max(0, target.getHealth() - before);
        if (healed >= MIN_SHOWN && !target.level().isClientSide()) {
            ModNetwork.sendToNearby(target, new HealPopupPayload(target.getId(), healed, skill.ordinal()));
        }
        return healed;
    }
}
