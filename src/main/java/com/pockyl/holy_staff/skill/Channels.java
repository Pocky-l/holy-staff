package com.pockyl.holy_staff.skill;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;

import com.pockyl.holy_staff.Config;
import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.item.HolyStaffItem;
import com.pockyl.holy_staff.network.ChannelPayload;
import com.pockyl.holy_staff.network.ModNetwork;
import com.pockyl.holy_staff.registry.ModSounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Channelled skills (Holy Beam, Sanctuary) on the server. A channel runs for a fixed time, is ticked with its level
 * and ends early when the caster cancels it, dies, puts the staff away or (beam) loses the target.
 */
@Mod.EventBusSubscriber(modid = HolyStaff.MOD_ID)
public final class Channels {
    private static final int BEAM_HEAL_INTERVAL = 5;
    /** The beam survives this many ticks without line of sight (pillars, mobs walking around corners). */
    private static final int BEAM_LOST_SIGHT_TICKS = 10;
    private static final double BEAM_BREAK_EXTRA_RANGE = 4.0;

    private static final Map<UUID, Channel> ACTIVE = new HashMap<>();

    private Channels() {
    }

    public static boolean isChannelling(Player player) {
        return ACTIVE.containsKey(player.getUUID());
    }

    @Nullable
    public static Skill current(Player player) {
        Channel channel = ACTIVE.get(player.getUUID());
        return channel == null ? null : channel.skill;
    }

    static void start(ServerPlayer player, HolyStaffItem staff, Skill skill, @Nullable LivingEntity target, int duration) {
        Channel channel = new Channel(player, skill, target, duration, staff.healMultiplier(), staff.beamRange());
        ACTIVE.put(player.getUUID(), channel);
        ModNetwork.sendToNearby(player, new ChannelPayload(player.getId(), skill.ordinal(), target == null ? -1 : target.getId(), duration));
        ItemStack stack = player.getMainHandItem();
        if (stack.is(staff) && ModNetwork.hasMod(player)) {
            channel.animatedStackId = GeoItem.getOrAssignId(stack, player.serverLevel());
            staff.triggerAnim(player, channel.animatedStackId, HolyStaffItem.CAST_CONTROLLER, HolyStaffItem.channelAnimation(skill));
        }
    }

    /** Ends the channel of the player, if any. */
    public static void stop(Player player) {
        Channel channel = ACTIVE.remove(player.getUUID());
        if (channel == null || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ModNetwork.sendToNearby(serverPlayer, new ChannelPayload(serverPlayer.getId(), -1, -1, 0));
        if (channel.skill == Skill.HOLY_BEAM) {
            serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), ModSounds.BEAM_END.get(),
                    SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        if (channel.animatedStackId != null && serverPlayer.getMainHandItem().getItem() instanceof HolyStaffItem staff) {
            staff.stopTriggeredAnim(serverPlayer, channel.animatedStackId, HolyStaffItem.CAST_CONTROLLER,
                    HolyStaffItem.channelAnimation(channel.skill));
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || ACTIVE.isEmpty()) {
            return;
        }
        for (Channel channel : new ArrayList<>(ACTIVE.values())) {
            if (channel.player.level() == level || channel.player.isRemoved()) {
                tick(channel);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ACTIVE.clear();
    }

    private static void tick(Channel channel) {
        ServerPlayer player = channel.player;
        if (player.isRemoved() || !player.isAlive() || !(player.getMainHandItem().getItem() instanceof HolyStaffItem)) {
            stop(player);
            return;
        }
        boolean keepGoing = switch (channel.skill) {
            case HOLY_BEAM -> tickBeam(channel);
            case SANCTUARY -> tickSanctuary(channel);
            default -> false;
        };
        channel.age++;
        if (!keepGoing || channel.age >= channel.duration) {
            stop(player);
        }
    }

    private static boolean tickBeam(Channel channel) {
        LivingEntity target = channel.target;
        ServerPlayer player = channel.player;
        double maxRange = channel.beamRange + BEAM_BREAK_EXTRA_RANGE;
        if (target == null || !target.isAlive() || !Healing.canHeal(target) || target.level() != player.level()
                || target.distanceToSqr(player) > maxRange * maxRange) {
            return false;
        }
        channel.lostSight = player.hasLineOfSight(target) ? 0 : channel.lostSight + 1;
        if (channel.lostSight > BEAM_LOST_SIGHT_TICKS) {
            return false;
        }
        if (channel.age % BEAM_HEAL_INTERVAL == 0) {
            Healing.heal(target, Config.beamHealPerSecond() * BEAM_HEAL_INTERVAL / 20.0F * channel.healMultiplier, Skill.HOLY_BEAM);
            if (channel.age % (BEAM_HEAL_INTERVAL * 2) == 0) {
                player.level().playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.HEAL.get(),
                        SoundSource.PLAYERS, 0.6F, 0.9F + player.getRandom().nextFloat() * 0.2F);
            }
        }
        return true;
    }

    private static boolean tickSanctuary(Channel channel) {
        ServerPlayer player = channel.player;
        if (channel.age == 0) {
            knockBackEnemies(player);
        }
        if (channel.age % Config.sanctuaryPulse() == 0) {
            float radius = Config.sanctuaryRadius();
            for (LivingEntity ally : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius, 3, radius),
                    entity -> Healing.canHeal(entity) && horizontalDistanceSqr(entity, player) <= radius * radius)) {
                Healing.heal(ally, Config.sanctuaryHeal() * channel.healMultiplier, Skill.SANCTUARY);
            }
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SANCTUARY_PULSE.get(),
                    SoundSource.PLAYERS, 1.2F, 1.0F);
        }
        return true;
    }

    /** Throws back hostile mobs and mobs that target the caster. Returns how many were hit. */
    public static int knockBackEnemies(ServerPlayer player) {
        double radius = Config.sanctuaryKnockbackRadius();
        int hit = 0;
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                entity -> entity != player && isEnemyOf(entity, player) && entity.distanceToSqr(player) <= radius * radius)) {
            double dx = player.getX() - entity.getX();
            double dz = player.getZ() - entity.getZ();
            if (dx * dx + dz * dz < 1.0E-4) {
                dx = player.getRandom().nextDouble() - 0.5;
                dz = player.getRandom().nextDouble() - 0.5;
            }
            entity.knockback(Config.sanctuaryKnockback(), dx, dz);
            entity.hurtMarked = true;
            hit++;
        }
        return hit;
    }

    private static boolean isEnemyOf(LivingEntity entity, Player player) {
        if (!entity.isAlive() || entity instanceof Player) {
            return false;
        }
        return entity instanceof Enemy || entity instanceof Mob mob && mob.getTarget() == player;
    }

    private static double horizontalDistanceSqr(LivingEntity a, LivingEntity b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }

    private static final class Channel {
        private final ServerPlayer player;
        private final Skill skill;
        @Nullable
        private final LivingEntity target;
        private final int duration;
        private final float healMultiplier;
        private final double beamRange;
        private int age;
        private int lostSight;
        @Nullable
        private Long animatedStackId;

        private Channel(ServerPlayer player, Skill skill, @Nullable LivingEntity target, int duration, float healMultiplier,
                double beamRange) {
            this.player = player;
            this.skill = skill;
            this.target = target;
            this.duration = duration;
            this.healMultiplier = healMultiplier;
            this.beamRange = beamRange;
        }
    }
}
