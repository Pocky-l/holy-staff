package com.pockyl.holy_staff.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.skill.Channels;
import com.pockyl.holy_staff.skill.Skill;

/** Client to server: the player cancels the Holy Beam. Sanctuary cannot be cancelled. */
public record StopChannelPayload() implements CustomPacketPayload {
    public static final Type<StopChannelPayload> TYPE = new Type<>(HolyStaff.id("stop_channel"));
    public static final StopChannelPayload INSTANCE = new StopChannelPayload();

    public static final StreamCodec<ByteBuf, StopChannelPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<StopChannelPayload> type() {
        return TYPE;
    }

    public static void handle(StopChannelPayload payload, IPayloadContext context) {
        if (Channels.current(context.player()) == Skill.HOLY_BEAM) {
            Channels.stop(context.player());
        }
    }
}
