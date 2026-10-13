package com.pockyl.holy_staff.skill;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
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
import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.network.HealPopupPayload;
import com.pockyl.holy_staff.network.ModNetwork;

import java.util.UUID;

/**
 * Who the staff may heal or smite, and healing / holy damage with a floating number for everyone who can see the
 * target. Players and everything that is not hostile are allies; undead mobs take holy damage instead of healing.
 */
public final class Healing {
    /** Holy damage: ignores armor, no knockback (the beam hits every few ticks). */
    public static final ResourceKey<DamageType> HOLY_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, HolyStaff.id("holy"));
    private static final float MIN_SHOWN = 0.05F;

    private Healing() {
    }

    /** Whether the staff may heal the entity right now. Hostile mobs only with {@code healMonsters}. */
    public static boolean canHeal(LivingEntity entity) {
        return !canSmite(entity) && isAllyKind(entity) && (Config.healMonsters() || !isHostile(entity));
    }

    /**
     * Whether the skills hurt the entity instead of healing it: undead mobs (everything that instant health harms) that
     * are not a player's pet, while {@code holyDamage} is on. Same answer on both sides.
     */
    public static boolean canSmite(LivingEntity entity) {
        return Config.holyDamage() && entity.isAlive() && !(entity instanceof Player) && entity.isInvertedHealAndHarm()
                && !(entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null);
    }

    /** What the skills can be aimed at: allies to heal and undead to smite. */
    public static boolean canTarget(LivingEntity entity) {
        return canSmite(entity) || canHeal(entity);
    }

    /** What the caster's skills can be aimed at: {@link #canTarget} without the caster's enemies. */
    public static boolean canTarget(LivingEntity entity, @Nullable Entity caster) {
        return canSmite(entity) || canHeal(entity) && !isEnemyOf(entity, caster);
    }

    /**
     * Players and pets of another scoreboard team than the caster's, and whoever hurt the caster in the last 5 seconds,
     * unless {@code healEnemies} is on. Teams are synced to clients; recent attackers are known only on the server.
     */
    public static boolean isEnemyOf(LivingEntity entity, @Nullable Entity caster) {
        if (caster == null || entity == caster || Config.healEnemies()) {
            return false;
        }
        if (caster instanceof LivingEntity living && living.getLastHurtByMob() == entity) {
            return true;
        }
        // Pets report their owner's team.
        return entity.getTeam() != null && caster.getTeam() != null && !caster.isAlliedTo(entity);
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

    /**
     * Hurts an undead target with holy damage credited to the caster (kills drop experience) and returns the health
     * actually taken. {@code amount} is the heal the skill would give; {@code holyDamageMultiplier} scales it.
     */
    public static float smite(LivingEntity target, float amount, Skill skill, @Nullable Entity direct, @Nullable Entity caster) {
        if (!(target.level() instanceof ServerLevel level)) {
            return 0;
        }
        DamageSource source = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(HOLY_DAMAGE),
                direct, caster);
        float before = target.getHealth();
        // Skills hit every few ticks; vanilla hurt cooldown would swallow every second beam step.
        target.invulnerableTime = 0;
        if (!target.hurt(source, amount * Config.holyDamageMultiplier())) {
            return 0;
        }
        float dealt = Math.max(0, before - Math.max(0, target.getHealth()));
        if (dealt >= MIN_SHOWN) {
            ModNetwork.sendToNearby(target, new HealPopupPayload(target.getId(), -dealt, skill.ordinal()));
        }
        return dealt;
    }

    /** Heals an ally or smites an undead target, whichever the target is. */
    public static void apply(LivingEntity target, float amount, Skill skill, @Nullable Entity direct, @Nullable Entity caster) {
        if (canSmite(target)) {
            smite(target, amount, skill, direct, caster);
        } else {
            heal(target, amount, skill);
        }
    }
}
