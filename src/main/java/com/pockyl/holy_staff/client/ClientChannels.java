package com.pockyl.holy_staff.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import org.jetbrains.annotations.Nullable;

import com.pockyl.holy_staff.Config;
import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.network.ChannelPayload;
import com.pockyl.holy_staff.skill.Skill;

import java.util.HashMap;
import java.util.Map;

/**
 * Channels of players seen by this client, from {@link ChannelPayload}. Also applies the movement restriction of the
 * local caster: slow and no jumping during Holy Beam, rooted during Sanctuary.
 */
@EventBusSubscriber(modid = HolyStaff.MOD_ID, value = Dist.CLIENT)
public final class ClientChannels {
    /** Safety margin after the expected end, in case the stop packet was lost. */
    private static final int EXPIRY_GRACE = 20;
    private static final Map<Integer, State> STATES = new HashMap<>();

    private ClientChannels() {
    }

    public static void handle(ChannelPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        if (payload.skill() < 0) {
            STATES.remove(payload.entityId());
        } else {
            Skill skill = Skill.byId(payload.skill());
            State previous = STATES.put(payload.entityId(), new State(skill, payload.targetId(), minecraft.level.getGameTime(),
                    payload.duration()));
            if (skill == Skill.HOLY_BEAM && (previous == null || previous.skill() != skill)
                    && minecraft.level.getEntity(payload.entityId()) instanceof Player player) {
                minecraft.getSoundManager().play(new BeamSoundInstance(player));
            }
        }
    }

    @Nullable
    public static State get(@Nullable Entity entity) {
        return entity == null ? null : STATES.get(entity.getId());
    }

    @Nullable
    public static Skill skillOf(@Nullable Entity entity) {
        State state = get(entity);
        return state == null ? null : state.skill();
    }

    static Map<Integer, State> all() {
        return STATES;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            STATES.clear();
            return;
        }
        long now = minecraft.level.getGameTime();
        STATES.entrySet().removeIf(entry -> now > entry.getValue().start() + entry.getValue().duration() + EXPIRY_GRACE
                || minecraft.level.getEntity(entry.getKey()) == null);
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }
        Skill skill = skillOf(player);
        if (skill == null) {
            return;
        }
        Input input = event.getInput();
        float multiplier = skill == Skill.SANCTUARY ? 0.0F : Config.beamSlowdown();
        input.forwardImpulse *= multiplier;
        input.leftImpulse *= multiplier;
        input.jumping = false;
        player.setSprinting(false);
    }

    /** A running channel. {@code start} is in client game time. */
    public record State(Skill skill, int targetId, long start, int duration) {
        public float age(long gameTime, float partialTick) {
            return gameTime - start + partialTick;
        }

        /** 0..1 visibility: quick fade in and out at both ends. */
        public float alpha(long gameTime, float partialTick) {
            float age = age(gameTime, partialTick);
            return Mth.clamp(Math.min(age / 4.0F, (duration - age) / 4.0F + 1.0F), 0.0F, 1.0F);
        }
    }
}
