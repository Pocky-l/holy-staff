package com.pockyl.holy_staff.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.item.HolyStaffItem;
import com.pockyl.holy_staff.network.ModNetwork;
import com.pockyl.holy_staff.network.SelectSkillPayload;
import com.pockyl.holy_staff.network.StopChannelPayload;
import com.pockyl.holy_staff.registry.ModSounds;
import com.pockyl.holy_staff.skill.Skill;

/**
 * Staff input besides right click (which casts through the vanilla item use): left click, sneak + mouse wheel or
 * the optional next-skill key (unbound by default) switch the selected skill; a new click of either mouse button cancels Holy Beam. Attacking and
 * block breaking are suppressed while holding the staff.
 */
@Mod.EventBusSubscriber(modid = HolyStaff.MOD_ID, value = Dist.CLIENT)
public final class ClientInputHandler {
    private static boolean attackHeldLastTick;
    private static boolean useHeldLastTick;
    private static boolean pressHandledThisTick;

    private ClientInputHandler() {
    }

    static boolean holdsStaff(LocalPlayer player) {
        return player.getMainHandItem().getItem() instanceof HolyStaffItem;
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || event.getHand() != InteractionHand.MAIN_HAND || !holdsStaff(player)) {
            return;
        }
        Skill channel = ClientChannels.skillOf(player);
        if (event.isUseItem()) {
            if (channel == null) {
                return;
            }
            // While channelling, right click does not cast; a fresh click cancels the beam.
            event.setCanceled(true);
            event.setSwingHand(false);
            if (channel == Skill.HOLY_BEAM && !useHeldLastTick && !pressHandledThisTick) {
                pressHandledThisTick = true;
                ModNetwork.sendToServer(StopChannelPayload.INSTANCE);
            }
            return;
        }
        if (!event.isAttack()) {
            return;
        }
        event.setCanceled(true);
        event.setSwingHand(false);
        // Holding the button on a block fires this every tick (continued mining); only a fresh click counts.
        if (attackHeldLastTick || pressHandledThisTick) {
            return;
        }
        pressHandledThisTick = true;
        if (channel == Skill.HOLY_BEAM) {
            ModNetwork.sendToServer(StopChannelPayload.INSTANCE);
        } else if (channel == null) {
            switchSkill(player, 1);
        }
    }

    @SubscribeEvent
    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null && minecraft.screen == null && player.isShiftKeyDown() && holdsStaff(player) && event.getScrollDelta() != 0) {
            event.setCanceled(true);
            if (ClientChannels.skillOf(player) == null) {
                switchSkill(player, event.getScrollDelta() > 0 ? -1 : 1);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        while (ModKeyMappings.NEXT_SKILL.consumeClick()) {
            if (player != null && minecraft.screen == null && holdsStaff(player) && ClientChannels.skillOf(player) == null) {
                switchSkill(player, 1);
            }
        }
        attackHeldLastTick = minecraft.options.keyAttack.isDown();
        useHeldLastTick = minecraft.options.keyUse.isDown();
        pressHandledThisTick = false;
    }

    private static void switchSkill(LocalPlayer player, int steps) {
        ItemStack stack = player.getMainHandItem();
        Skill next = HolyStaffItem.selected(stack).cycle(steps);
        // Shown at once; the server confirms by syncing the same item data.
        HolyStaffItem.setSelected(stack, next);
        ModNetwork.sendToServer(new SelectSkillPayload(next));
        SkillHud.onSkillSwitched(next);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.SKILL_SWITCH.get(), 1.0F, 0.6F));
    }
}
