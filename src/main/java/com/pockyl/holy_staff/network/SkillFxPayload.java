package com.pockyl.holy_staff.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.joml.Vector3f;

import com.pockyl.holy_staff.HolyStaff;

/** Server to client: one-off visual effect of a cast skill at a position (e.g. light streaming from the staff). */
public record SkillFxPayload(int skill, int casterId, Vector3f pos) implements CustomPacketPayload {
    public static final Type<SkillFxPayload> TYPE = new Type<>(HolyStaff.id("skill_fx"));

    public static final StreamCodec<ByteBuf, SkillFxPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SkillFxPayload::skill,
            ByteBufCodecs.VAR_INT, SkillFxPayload::casterId,
            ByteBufCodecs.VECTOR3F, SkillFxPayload::pos,
            SkillFxPayload::new);

    @Override
    public Type<SkillFxPayload> type() {
        return TYPE;
    }
}
