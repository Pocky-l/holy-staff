package com.pockyl.holy_staff.skill;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoItem;

import com.pockyl.holy_staff.Config;
import com.pockyl.holy_staff.entity.BlessedGround;
import com.pockyl.holy_staff.item.HolyStaffItem;
import com.pockyl.holy_staff.network.CooldownPayload;
import com.pockyl.holy_staff.network.ModNetwork;
import com.pockyl.holy_staff.network.SkillFxPayload;
import com.pockyl.holy_staff.registry.ModAttachments;
import com.pockyl.holy_staff.registry.ModSounds;

/** Server-side casting of the staff skills: checks, effects, cooldowns and feedback. */
public final class SkillCaster {
    private static final double AIM_TOLERANCE_DEGREES = 4.0;

    private SkillCaster() {
    }

    /**
     * Casts the skill selected on the staff in the main hand if the player is not channelling and the skill is off
     * cooldown. Returns whether it was cast.
     */
    public static boolean tryCast(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof HolyStaffItem staff) || player.isSpectator() || !player.isAlive()
                || Channels.isChannelling(player)) {
            return false;
        }
        Skill skill = HolyStaffItem.selected(stack);
        SkillCooldowns cooldowns = ModAttachments.cooldowns(player);
        long now = player.level().getGameTime();
        if (!cooldowns.isReady(skill, now)) {
            return false;
        }

        boolean cast = switch (skill) {
            case BLESSED_GROUND -> blessedGround(player, staff);
            case HOLY_BEAM -> holyBeam(player, staff);
            case SANCTUARY -> sanctuary(player, staff);
        };
        if (!cast) {
            return false;
        }

        int ticks = staff.cooldown(skill);
        cooldowns.start(skill, now, ticks);
        // Test and fake players have no client with this mod; GeckoLib would fail sending its animation packet to them.
        if (ModNetwork.sendToPlayer(player, new CooldownPayload(skill.ordinal(), ticks)) && !skill.isChannelled()) {
            staff.triggerAnim(player, GeoItem.getOrAssignId(stack, player.serverLevel()), HolyStaffItem.CAST_CONTROLLER,
                    HolyStaffItem.CAST_ANIMATION);
        }
        return true;
    }

    private static boolean blessedGround(ServerPlayer player, HolyStaffItem staff) {
        ServerLevel level = player.serverLevel();
        Vec3 pos = groundTarget(player);
        level.addFreshEntity(new BlessedGround(level, pos, Config.blessedRadius(), Config.blessedDelay(),
                Config.blessedHeal() * staff.healMultiplier()));
        ModNetwork.sendToNearby(player, new SkillFxPayload(Skill.BLESSED_GROUND.ordinal(), player.getId(),
                new Vector3f((float) pos.x, (float) pos.y, (float) pos.z)));
        level.playSound(null, pos.x, pos.y, pos.z, ModSounds.BLESSED_CAST.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static boolean holyBeam(ServerPlayer player, HolyStaffItem staff) {
        LivingEntity target = findAimedAlly(player, staff.beamRange());
        if (target == null) {
            player.displayClientMessage(Component.translatable("message.holy_staff.no_target"), true);
            return false;
        }
        Channels.start(player, staff, Skill.HOLY_BEAM, target, Config.beamDuration());
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.BEAM_START.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static boolean sanctuary(ServerPlayer player, HolyStaffItem staff) {
        Channels.start(player, staff, Skill.SANCTUARY, null, Config.sanctuaryDuration());
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SANCTUARY_PLANT.get(),
                SoundSource.PLAYERS, 1.4F, 1.0F);
        return true;
    }

    /**
     * The healable entity closest to the crosshair: anything the look ray passes through wins, otherwise the smallest
     * angle within a few degrees (aim assist for small or moving targets). Requires line of sight.
     */
    @Nullable
    public static LivingEntity findAimedAlly(Player player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(range));
        BlockHitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double maxDistance = block.getType() == HitResult.Type.MISS ? range : block.getLocation().distanceTo(eye) + 0.5;

        AABB area = player.getBoundingBox().expandTowards(look.scale(range)).inflate(3.0);
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != player && Healing.canHeal(entity))) {
            AABB box = entity.getBoundingBox().inflate(0.3);
            Vec3 toCenter = box.getCenter().subtract(eye);
            double distance = toCenter.length();
            if (distance > maxDistance + box.getXsize() || !player.hasLineOfSight(entity)) {
                continue;
            }
            double angle = 0;
            if (box.clip(eye, end).isEmpty()) {
                angle = Math.toDegrees(Math.acos(Mth.clamp(toCenter.normalize().dot(look), -1.0, 1.0)));
                double tolerance = AIM_TOLERANCE_DEGREES + Math.toDegrees(Math.atan(box.getXsize() / 2 / Math.max(distance, 0.5)));
                if (angle > tolerance) {
                    continue;
                }
            }
            double score = angle + distance * 0.05;
            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }
        return best;
    }

    /** Under the aimed ally, else on the aimed block, else under the caster; always snapped down to the ground. */
    public static Vec3 groundTarget(Player player) {
        double range = Config.blessedRange();
        LivingEntity ally = findAimedAlly(player, range);
        Vec3 pos;
        if (ally != null) {
            pos = ally.position();
        } else {
            Vec3 eye = player.getEyePosition();
            BlockHitResult hit = player.level().clip(new ClipContext(eye, eye.add(player.getViewVector(1.0F).scale(range)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() == HitResult.Type.MISS) {
                pos = player.position();
            } else if (hit.getDirection() == Direction.UP) {
                pos = hit.getLocation();
            } else {
                pos = hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getNormal()).scale(0.3));
            }
        }
        BlockHitResult ground = player.level().clip(new ClipContext(pos.add(0, 0.5, 0), pos.add(0, -8, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
        return ground.getType() == HitResult.Type.MISS ? pos : ground.getLocation();
    }
}
