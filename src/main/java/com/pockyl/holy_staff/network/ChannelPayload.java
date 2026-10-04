package com.pockyl.holy_staff.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.pockyl.holy_staff.HolyStaff;

/**
 * Server to client: a player started channelling a skill (with an optional target), or stopped ({@code skill} -1).
 * Drives the beam and sanctuary visuals, arm poses and the movement restriction of the caster.
 */
public record ChannelPayload(int entityId, int skill, int targetId, int duration) implements CustomPacketPayload {
    public static final Type<ChannelPayload> TYPE = new Type<>(HolyStaff.id("channel"));

    public static final StreamCodec<ByteBuf, ChannelPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ChannelPayload::entityId,
            ByteBufCodecs.VAR_INT, ChannelPayload::skill,
            ByteBufCodecs.VAR_INT, ChannelPayload::targetId,
            ByteBufCodecs.VAR_INT, ChannelPayload::duration,
            ChannelPayload::new);

    @Override
    public Type<ChannelPayload> type() {
        return TYPE;
    }
}
