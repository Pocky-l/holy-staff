package com.pockyl.holy_staff.event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.item.HolyStaffItem;
import com.pockyl.holy_staff.skill.Healing;

/**
 * While the staff is in the main hand, right clicking a healable mob heals it instead of interacting with it
 * (trading, sitting pets, ...), and the staff never attacks.
 */
@EventBusSubscriber(modid = HolyStaff.MOD_ID)
public final class StaffInteractionEvents {
    private StaffInteractionEvents() {
    }

    // PASS lets the client fall through to using the item, which casts the right-click skill on the aimed mob.
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (shouldSkip(event.getEntity(), event.getHand(), event.getTarget())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (shouldSkip(event.getEntity(), event.getHand(), event.getTarget())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (holdsStaff(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private static boolean shouldSkip(Player player, InteractionHand hand, Entity target) {
        return hand == InteractionHand.MAIN_HAND && target instanceof LivingEntity living && Healing.canHeal(living) && holdsStaff(player);
    }

    private static boolean holdsStaff(Player player) {
        return player.getMainHandItem().getItem() instanceof HolyStaffItem;
    }
}
