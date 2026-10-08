package com.pockyl.holy_staff.network;

import net.minecraft.network.FriendlyByteBuf;
import org.joml.Vector3f;

/** Server to client: one-off visual effect of a cast skill at a position (e.g. light streaming from the staff). */
public record SkillFxPayload(int skill, int casterId, Vector3f pos) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(skill);
        buf.writeVarInt(casterId);
        buf.writeFloat(pos.x());
        buf.writeFloat(pos.y());
        buf.writeFloat(pos.z());
    }

    public static SkillFxPayload decode(FriendlyByteBuf buf) {
        return new SkillFxPayload(buf.readVarInt(), buf.readVarInt(), new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat()));
    }
}
