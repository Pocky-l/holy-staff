package com.pockyl.holy_staff.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import com.pockyl.holy_staff.skill.Channels;
import com.pockyl.holy_staff.skill.Skill;

import java.util.function.Supplier;

/** Client to server: the player cancels the Holy Beam. Sanctuary cannot be cancelled. */
public record StopChannelPayload() {
    public static final StopChannelPayload INSTANCE = new StopChannelPayload();

    public void encode(FriendlyByteBuf buf) {
    }

    public static StopChannelPayload decode(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    public static void handle(StopChannelPayload payload, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null && Channels.current(player) == Skill.HOLY_BEAM) {
            Channels.stop(player);
        }
    }
}
