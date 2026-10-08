package com.pockyl.holy_staff.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.pockyl.holy_staff.item.HolyStaffItem;
import com.pockyl.holy_staff.skill.Skill;

import java.util.function.Supplier;

/** Client to server: select the skill that the staff in the main hand casts on right click. */
public record SelectSkillPayload(Skill skill) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(skill.ordinal());
    }

    public static SelectSkillPayload decode(FriendlyByteBuf buf) {
        return new SelectSkillPayload(Skill.byId(buf.readVarInt()));
    }

    public static void handle(SelectSkillPayload payload, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            HolyStaffItem.select(player, payload.skill());
        }
    }
}
