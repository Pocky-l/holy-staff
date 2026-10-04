package com.pockyl.holy_staff.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.item.HolyStaffItem;
import com.pockyl.holy_staff.skill.Skill;

/** Client to server: select the skill that the staff in the main hand casts on right click. */
public record SelectSkillPayload(Skill skill) implements CustomPacketPayload {
    public static final Type<SelectSkillPayload> TYPE = new Type<>(HolyStaff.id("select_skill"));

    public static final StreamCodec<ByteBuf, SelectSkillPayload> STREAM_CODEC = StreamCodec.composite(
            Skill.STREAM_CODEC, SelectSkillPayload::skill,
            SelectSkillPayload::new);

    @Override
    public Type<SelectSkillPayload> type() {
        return TYPE;
    }

    public static void handle(SelectSkillPayload payload, IPayloadContext context) {
        HolyStaffItem.select(context.player(), payload.skill());
    }
}
