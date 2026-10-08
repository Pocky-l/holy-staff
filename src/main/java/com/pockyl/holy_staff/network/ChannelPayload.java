package com.pockyl.holy_staff.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Server to client: a player started channelling a skill (with an optional target), or stopped ({@code skill} -1).
 * Drives the beam and sanctuary visuals, arm poses and the movement restriction of the caster.
 */
public record ChannelPayload(int entityId, int skill, int targetId, int duration) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(skill);
        buf.writeVarInt(targetId);
        buf.writeVarInt(duration);
    }

    public static ChannelPayload decode(FriendlyByteBuf buf) {
        return new ChannelPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }
}
