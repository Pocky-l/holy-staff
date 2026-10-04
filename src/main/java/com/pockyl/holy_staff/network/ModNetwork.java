package com.pockyl.holy_staff.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import com.pockyl.holy_staff.client.ClientChannels;
import com.pockyl.holy_staff.client.ClientSkillEffects;
import com.pockyl.holy_staff.client.HealNumbers;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "3";

    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(StopChannelPayload.TYPE, StopChannelPayload.STREAM_CODEC, StopChannelPayload::handle);
        registrar.playToServer(SelectSkillPayload.TYPE, SelectSkillPayload.STREAM_CODEC, SelectSkillPayload::handle);
        registrar.playToClient(CooldownPayload.TYPE, CooldownPayload.STREAM_CODEC, CooldownPayload::handle);
        // Client-bound handlers live in client code; the lambdas only resolve it when a packet arrives on a client.
        registrar.playToClient(HealPopupPayload.TYPE, HealPopupPayload.STREAM_CODEC,
                (payload, context) -> HealNumbers.handle(payload));
        registrar.playToClient(SkillFxPayload.TYPE, SkillFxPayload.STREAM_CODEC,
                (payload, context) -> ClientSkillEffects.handle(payload));
        registrar.playToClient(ChannelPayload.TYPE, ChannelPayload.STREAM_CODEC,
                (payload, context) -> ClientChannels.handle(payload));
    }

    /** Whether the player is a real client with this mod (fake and game-test players are not). */
    public static boolean hasMod(ServerPlayer player) {
        return player.connection != null && player.connection.hasChannel(CooldownPayload.TYPE);
    }

    /** Sends to the player if their client has this mod (fake and test players do not). */
    public static boolean sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        if (player.connection == null || !player.connection.hasChannel(payload)) {
            return false;
        }
        PacketDistributor.sendToPlayer(player, payload);
        return true;
    }

    /**
     * Sends to the entity itself (if it is a player) and to every player close enough to see it. Clients that do not
     * know the entity simply ignore the packet.
     */
    public static void sendToNearby(Entity entity, CustomPacketPayload payload) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        double range = Math.max(4, entity.getType().clientTrackingRange()) * 16.0;
        for (ServerPlayer player : level.players()) {
            if (player == entity || player.distanceToSqr(entity) <= range * range) {
                sendToPlayer(player, payload);
            }
        }
    }
}
