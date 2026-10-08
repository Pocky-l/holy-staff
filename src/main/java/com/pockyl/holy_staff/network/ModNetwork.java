package com.pockyl.holy_staff.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.client.ClientChannels;
import com.pockyl.holy_staff.client.ClientCooldowns;
import com.pockyl.holy_staff.client.ClientSkillEffects;
import com.pockyl.holy_staff.client.HealNumbers;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "3";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(HolyStaff.id("main"), () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private ModNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(StopChannelPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(StopChannelPayload::encode)
                .decoder(StopChannelPayload::decode)
                .consumerMainThread(StopChannelPayload::handle)
                .add();
        CHANNEL.messageBuilder(SelectSkillPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SelectSkillPayload::encode)
                .decoder(SelectSkillPayload::decode)
                .consumerMainThread(SelectSkillPayload::handle)
                .add();
        // Client-bound handlers live in client code; the lambdas only resolve it when a packet arrives on a client.
        CHANNEL.messageBuilder(CooldownPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CooldownPayload::encode)
                .decoder(CooldownPayload::decode)
                .consumerMainThread((payload, context) -> ClientCooldowns.handle(payload))
                .add();
        CHANNEL.messageBuilder(HealPopupPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(HealPopupPayload::encode)
                .decoder(HealPopupPayload::decode)
                .consumerMainThread((payload, context) -> HealNumbers.handle(payload))
                .add();
        CHANNEL.messageBuilder(SkillFxPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SkillFxPayload::encode)
                .decoder(SkillFxPayload::decode)
                .consumerMainThread((payload, context) -> ClientSkillEffects.handle(payload))
                .add();
        CHANNEL.messageBuilder(ChannelPayload.class, id, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ChannelPayload::encode)
                .decoder(ChannelPayload::decode)
                .consumerMainThread((payload, context) -> ClientChannels.handle(payload))
                .add();
    }

    /** Whether the player is a real client with this mod (fake and game-test players are not). */
    public static boolean hasMod(ServerPlayer player) {
        // Game-test players have no open network channel; Forge's channel lookup would fail on them.
        return player.connection != null && player.connection.isAcceptingMessages()
                && CHANNEL.isRemotePresent(player.connection.connection);
    }

    /** Sends to the player if their client has this mod (fake and test players do not). */
    public static boolean sendToPlayer(ServerPlayer player, Object payload) {
        if (!hasMod(player)) {
            return false;
        }
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
        return true;
    }

    /**
     * Sends to the entity itself (if it is a player) and to every player close enough to see it. Clients that do not
     * know the entity simply ignore the packet.
     */
    public static void sendToNearby(Entity entity, Object payload) {
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

    /** Sends a client-to-server packet; only called from client code. */
    public static void sendToServer(Object payload) {
        CHANNEL.sendToServer(payload);
    }
}
