package com.pockyl.holy_staff.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.pockyl.holy_staff.registry.ModEntities;
import com.pockyl.holy_staff.registry.ModParticles;
import com.pockyl.holy_staff.registry.ModSounds;
import com.pockyl.holy_staff.skill.Healing;
import com.pockyl.holy_staff.skill.Skill;

/**
 * Blessed Ground: a circle that fills up from its centre for {@code delay} ticks, then bursts and heals every ally
 * standing in it once, then fades out.
 */
public final class BlessedGround extends Entity {
    /** Ticks the burst stays visible after healing. */
    public static final int FADE_TICKS = 10;
    private static final double HEIGHT_BELOW = 1.0;
    private static final double HEIGHT_ABOVE = 2.5;

    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(BlessedGround.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(BlessedGround.class, EntityDataSerializers.INT);

    private float heal;

    public BlessedGround(EntityType<? extends BlessedGround> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public BlessedGround(Level level, Vec3 pos, float radius, int delay, float heal) {
        this(ModEntities.BLESSED_GROUND.get(), level);
        setPos(pos);
        entityData.set(RADIUS, radius);
        entityData.set(DELAY, delay);
        this.heal = heal;
        refreshDimensions();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 3.0F);
        builder.define(DELAY, 12);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (RADIUS.equals(key)) {
            refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(radius() * 2, 0.1F);
    }

    public float radius() {
        return entityData.get(RADIUS);
    }

    public int delay() {
        return entityData.get(DELAY);
    }

    /** 0..1 fill of the circle before the burst. */
    public float fill(float partialTick) {
        int delay = delay();
        return delay <= 0 ? 1.0F : Mth.clamp((tickCount + partialTick) / delay, 0.0F, 1.0F);
    }

    /** Ticks since the burst (negative before it). */
    public float sinceBurst(float partialTick) {
        return tickCount + partialTick - delay();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            if (tickCount == delay()) {
                burstParticles();
            } else if (tickCount < delay()) {
                fillParticles();
            }
            return;
        }
        if (tickCount == delay()) {
            burst();
        }
        if (tickCount >= delay() + FADE_TICKS) {
            discard();
        }
    }

    private void burst() {
        float radius = radius();
        AABB area = new AABB(getX() - radius, getY() - HEIGHT_BELOW, getZ() - radius,
                getX() + radius, getY() + HEIGHT_ABOVE, getZ() + radius);
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, area, Healing::canHeal)) {
            double dx = entity.getX() - getX();
            double dz = entity.getZ() - getZ();
            if (dx * dx + dz * dz <= radius * radius) {
                Healing.heal(entity, heal, Skill.BLESSED_GROUND);
            }
        }
        level().playSound(null, getX(), getY(), getZ(), ModSounds.BLESSED_BURST.get(), SoundSource.PLAYERS, 1.3F, 1.0F);
    }

    /** Light motes rising from the edge of the filling light. */
    private void fillParticles() {
        double edge = radius() * fill(0);
        for (int i = 0; i < 3; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            level().addParticle(ModParticles.HOLY_GLOW.get(), getX() + Math.cos(angle) * edge, getY() + 0.1, getZ() + Math.sin(angle) * edge,
                    0, 0.03 + random.nextDouble() * 0.04, 0);
        }
    }

    private void burstParticles() {
        float radius = radius();
        int count = Math.round(radius * 10);
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = Math.sqrt(random.nextDouble()) * radius;
            level().addParticle(i % 3 == 0 ? ModParticles.HOLY_SPARKLE.get() : ModParticles.HOLY_GLOW.get(),
                    getX() + Math.cos(angle) * distance, getY() + 0.1, getZ() + Math.sin(angle) * distance,
                    0, 0.08 + random.nextDouble() * 0.12, 0);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double max = 64.0 * getViewScale();
        return distance < max * max;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(RADIUS, tag.getFloat("Radius"));
        entityData.set(DELAY, tag.getInt("Delay"));
        tickCount = tag.getInt("Age");
        heal = tag.getFloat("Heal");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Radius", radius());
        tag.putInt("Delay", delay());
        tag.putInt("Age", tickCount);
        tag.putFloat("Heal", heal);
    }
}
