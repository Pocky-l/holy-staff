package com.pockyl.holy_staff.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import com.pockyl.holy_staff.network.CooldownPayload;
import com.pockyl.holy_staff.registry.ModAttachments;
import com.pockyl.holy_staff.skill.Skill;

/** Mirrors the cooldowns the server starts into the local player's copy, used by the HUD and by item use. */
public final class ClientCooldowns {
    private ClientCooldowns() {
    }

    public static void handle(CooldownPayload payload) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            ModAttachments.cooldowns(player).start(Skill.byId(payload.skill()), player.level().getGameTime(), payload.ticks());
        }
    }
}
