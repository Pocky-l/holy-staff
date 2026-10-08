package com.pockyl.holy_staff.network;

import net.minecraft.network.FriendlyByteBuf;

/** Server to client: an entity was healed by a skill; the client shows a floating number. */
public record HealPopupPayload(int entityId, float amount, int skill) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeFloat(amount);
        buf.writeVarInt(skill);
    }

    public static HealPopupPayload decode(FriendlyByteBuf buf) {
        return new HealPopupPayload(buf.readVarInt(), buf.readFloat(), buf.readVarInt());
    }
}
