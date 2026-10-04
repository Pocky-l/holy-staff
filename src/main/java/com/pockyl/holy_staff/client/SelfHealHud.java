package com.pockyl.holy_staff.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import com.pockyl.holy_staff.HolyStaff;

import java.util.ArrayList;
import java.util.List;

/** Heals received by the local player: green numbers popping up right next to the health bar. */
@EventBusSubscriber(modid = HolyStaff.MOD_ID, value = Dist.CLIENT)
public final class SelfHealHud implements LayeredDraw.Layer {
    private static final int LIFETIME = 30;
    private static final int MAX_POPUPS = 8;
    private static final float RISE = 16.0F;
    private static final int GREEN = 0x55FF55;
    private static final int OUTLINE = 0x0B3D0B;
    /** Vanilla layout: hearts start 91 px left of the centre, 39 px above the bottom, 8 px per heart. */
    private static final int HEARTS_LEFT = 91;
    private static final int HEARTS_BOTTOM = 39;
    private static final int HEARTS_WIDTH = 81;
    private static final RandomSource RANDOM = RandomSource.create();
    private static final List<Popup> POPUPS = new ArrayList<>();

    static void add(String text, float amount) {
        if (POPUPS.size() >= MAX_POPUPS) {
            POPUPS.removeFirst();
        }
        POPUPS.add(new Popup(text, 1.0F + Math.min(10.0F, amount) * 0.05F, RANDOM.nextInt(7) - 3));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level == null) {
            POPUPS.clear();
            return;
        }
        POPUPS.removeIf(popup -> ++popup.age >= LIFETIME);
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (POPUPS.isEmpty() || minecraft.options.hideGui || minecraft.player == null || minecraft.player.isSpectator()) {
            return;
        }
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        Font font = minecraft.font;
        int anchorX = graphics.guiWidth() / 2 - HEARTS_LEFT + HEARTS_WIDTH + 3;
        int anchorY = graphics.guiHeight() - HEARTS_BOTTOM;
        for (Popup popup : POPUPS) {
            float age = popup.age + partialTick;
            float life = age / LIFETIME;
            float rise = RISE * (1 - (1 - life) * (1 - life));
            float pop = age < 4 ? 1.6F - 0.6F * (age / 4) : 1.0F;
            float alpha = life < 0.6F ? 1.0F : Mth.clamp((1 - life) / 0.4F, 0, 1);
            int a = Math.max(8, Math.round(alpha * 255)) << 24;
            float scale = pop * popup.size;

            graphics.pose().pushPose();
            graphics.pose().translate(anchorX + popup.jitter, anchorY - rise, 300);
            graphics.pose().scale(scale, scale, 1);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx != 0 || dy != 0) {
                        graphics.drawString(font, popup.text, dx, dy, a | OUTLINE, false);
                    }
                }
            }
            graphics.drawString(font, popup.text, 0, 0, a | GREEN, false);
            graphics.pose().popPose();
        }
    }

    private static final class Popup {
        private final String text;
        private final float size;
        private final int jitter;
        private int age;

        private Popup(String text, float size, int jitter) {
            this.text = text;
            this.size = size;
            this.jitter = jitter;
        }
    }
}
