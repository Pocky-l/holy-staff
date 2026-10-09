package com.pockyl.holy_staff.skill;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import com.pockyl.holy_staff.Config;
import com.pockyl.holy_staff.network.HealPopupPayload;
import com.pockyl.holy_staff.network.ModNetwork;

import java.util.UUID;

/**
 * Who the staff may heal, and healing with a floating number for everyone who can see the target. Players and
 * everything that is not hostile are allies.
 */
public final class Healing {
    private static final float MIN_SHOWN = 0.05F;

    private Healing() {
    }

    /** Whether the staff may heal the entity right now. Hostile mobs only with {@code healMonsters}. */
    public static boolean canHeal(LivingEntity entity) {
        return isAllyKind(entity) && (Config.healMonsters() || !isHostile(entity));
    }

    /**
     * Like {@link #canHeal} but ignores what a mob is doing right now (attack target, anger): only that state is not
     * synced to clients, so this gives the same answer on both sides.
     */
    public static boolean isAllyKind(LivingEntity entity) {
        if (!entity.isAlive() || entity.isSpectator() || entity instanceof ArmorStand) {
            return false;
        }
        return !(entity instanceof Enemy) || Config.healMonsters();
    }

    /**
     * Monsters, mobs attacking a player or a player's pet, and neutral mobs angry at them. Sanctuary throws exactly these
     * back. On the client only monsters count: targets and anger live on the server, which decides every heal.
     */
    public static boolean isHostile(LivingEntity entity) {
        if (entity instanceof Player) {
            return false;
        }
        if (entity instanceof Enemy) {
            return true;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return false;
        }
        if (entity instanceof Mob mob && isPlayersSide(mob.getTarget())) {
            return true;
        }
        // Anger alone is not enough: a pet fighting a zombie is angry at the zombie.
        return entity instanceof NeutralMob neutral && neutral.isAngry()
                && (neutral.isAngryAtAllPlayers(level) || isPlayersSide(angerTarget(neutral, level)));
    }

    private static boolean isPlayersSide(@Nullable Entity entity) {
        return entity instanceof Player || entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null;
    }

    @Nullable
    private static Entity angerTarget(NeutralMob mob, ServerLevel level) {
        UUID id = mob.getPersistentAngerTarget();
        return id == null ? null : level.getEntity(id);
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
