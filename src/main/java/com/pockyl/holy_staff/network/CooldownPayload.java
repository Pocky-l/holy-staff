package com.pockyl.holy_staff.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.registry.ModAttachments;
import com.pockyl.holy_staff.skill.Skill;

/** Server to client: a skill of this player went on cooldown for the given number of ticks. */
public record CooldownPayload(int skill, int ticks) implements CustomPacketPayload {
    public static final Type<CooldownPayload> TYPE = new Type<>(HolyStaff.id("cooldown"));

    public static final StreamCodec<ByteBuf, CooldownPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CooldownPayload::skill,
            ByteBufCodecs.VAR_INT, CooldownPayload::ticks,
            CooldownPayload::new);

    @Override
    public Type<CooldownPayload> type() {
        return TYPE;
    }

    // Uses only common classes, so it is safe to register on both sides.
    public static void handle(CooldownPayload payload, IPayloadContext context) {
        long now = context.player().level().getGameTime();
        context.player().getData(ModAttachments.COOLDOWNS).start(Skill.byId(payload.skill()), now, payload.ticks());
    }
}
