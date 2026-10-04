package com.pockyl.holy_staff.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.pockyl.holy_staff.HolyStaff;

/** Server to client: an entity was healed by a skill; the client shows a floating number. */
public record HealPopupPayload(int entityId, float amount, int skill) implements CustomPacketPayload {
    public static final Type<HealPopupPayload> TYPE = new Type<>(HolyStaff.id("heal_popup"));

    public static final StreamCodec<ByteBuf, HealPopupPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HealPopupPayload::entityId,
            ByteBufCodecs.FLOAT, HealPopupPayload::amount,
            ByteBufCodecs.VAR_INT, HealPopupPayload::skill,
            HealPopupPayload::new);

    @Override
    public Type<HealPopupPayload> type() {
        return TYPE;
    }
}
