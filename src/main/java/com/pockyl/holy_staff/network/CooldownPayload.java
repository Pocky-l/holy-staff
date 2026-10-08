package com.pockyl.holy_staff.network;

import net.minecraft.network.FriendlyByteBuf;

/** Server to client: a skill of this player went on cooldown for the given number of ticks. */
public record CooldownPayload(int skill, int ticks) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(skill);
        buf.writeVarInt(ticks);
    }

    public static CooldownPayload decode(FriendlyByteBuf buf) {
        return new CooldownPayload(buf.readVarInt(), buf.readVarInt());
    }
}
