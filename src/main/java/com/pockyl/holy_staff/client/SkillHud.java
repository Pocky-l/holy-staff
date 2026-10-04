package com.pockyl.holy_staff.client;

import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.Nullable;

import com.pockyl.holy_staff.Config;
import com.pockyl.holy_staff.item.HolyStaffItem;
import com.pockyl.holy_staff.registry.ModAttachments;
import com.pockyl.holy_staff.skill.Skill;
import com.pockyl.holy_staff.skill.SkillCooldowns;

/**
 * While the staff is held: the three skills next to the hotbar (the selected one raised and framed in gold, cooldown
 * sweeps and seconds left, control hints), the name of a newly selected skill, and a cast bar while channelling.
 */
public final class SkillHud implements LayeredDraw.Layer {
    private static final int SLOT = 22;
    private static final int GAP = 3;
    private static final int GAP_TO_HOTBAR = 8;
    private static final int SELECTED_RAISE = 3;
    private static final int READY_FLASH_TICKS = 8;
    private static final int BACKGROUND = 0xA0101018;
    private static final int COOLDOWN_SHADE = 0xB0000000;
    private static final int GOLD = 0xFFFFD86A;
    private static final int TIMER_COLOR = 0xFFFFFF;
    private static final int CAST_BAR_WIDTH = 120;
    private static final int CAST_BAR_Y = 68;
    private static final int SWITCH_NAME_TICKS = 40;

    @Nullable
    private static Skill switchedTo;
    private static long switchedAt;

    static void onSkillSwitched(Skill skill) {
        LocalPlayer player = Minecraft.getInstance().player;
        switchedTo = skill;
        switchedAt = player == null ? 0 : player.level().getGameTime();
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator() || !Config.showSkillHud()
                || !ClientInputHandler.holdsStaff(player)) {
            return;
        }

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        SkillCooldowns cooldowns = player.getData(ModAttachments.COOLDOWNS);
        Skill selected = HolyStaffItem.selected(player.getMainHandItem());
        float now = player.level().getGameTime() + partialTick;
        int count = Skill.values().length;
        int width = count * SLOT + (count - 1) * GAP;
        int center = graphics.guiWidth() / 2;
        // Stay clear of the off-hand slot (opposite the main arm) and of the hotbar attack indicator.
        int gap = GAP_TO_HOTBAR + (minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR ? 22 : 0);
        boolean rightSide = player.getMainArm() == HumanoidArm.RIGHT;
        int x0 = rightSide ? center + 91 + gap : center - 91 - gap - width;
        int baseY = graphics.guiHeight() - SLOT - 1;
        Font font = minecraft.font;

        for (Skill skill : Skill.values()) {
            boolean isSelected = skill == selected;
            int x = x0 + skill.ordinal() * (SLOT + GAP);
            int y = baseY - (isSelected ? SELECTED_RAISE : 0);
            float remaining = Math.max(0, cooldowns.readyAt(skill) - now);
            float sinceReady = now - cooldowns.readyAt(skill);
            boolean ready = remaining <= 0;

            graphics.fill(x, y, x + SLOT, y + SLOT, isSelected ? 0xC0302810 : BACKGROUND);
            graphics.blit(skill.icon(), x + 3, y + 3, 0, 0, 16, 16, 16, 16);
            if (!isSelected) {
                graphics.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, 0x50000000);
            }

            if (!ready && cooldowns.total(skill) > 0) {
                // Shade shrinks from the top as the cooldown runs out, like vanilla item cooldowns.
                float fraction = Mth.clamp(remaining / cooldowns.total(skill), 0, 1);
                int shadeTop = y + 1 + Math.round((SLOT - 2) * (1 - fraction));
                graphics.fill(x + 1, shadeTop, x + SLOT - 1, y + SLOT - 1, COOLDOWN_SHADE);
                String seconds = remaining >= 200 ? String.valueOf((int) Math.ceil(remaining / 20))
                        : String.format("%.1f", remaining / 20);
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 200);
                graphics.drawCenteredString(font, seconds, x + SLOT / 2 + 1, y + (SLOT - 8) / 2, TIMER_COLOR);
                graphics.pose().popPose();
            }

            int border = isSelected ? GOLD : ready ? 0xFF000000 | darker(skill.color()) : 0xFF404040;
            if (ready && sinceReady < READY_FLASH_TICKS && cooldowns.total(skill) > 0) {
                border = 0xFFFFFFFF;
                graphics.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, 0x40FFFFFF);
            }
            graphics.renderOutline(x, y, SLOT, SLOT, border);
            if (isSelected) {
                graphics.renderOutline(x - 1, y - 1, SLOT + 2, SLOT + 2, 0x80FFD86A);
                drawSmall(graphics, font, Component.translatable("hud.holy_staff.cast"), x + SLOT / 2, y - 7, GOLD);
            }
        }

        Component hint = Component.translatable("hud.holy_staff.switch");
        int hintX = rightSide ? x0 + width + 4 + font.width(hint) / 4 : x0 - 4 - font.width(hint) / 4;
        drawSmall(graphics, font, hint, hintX, baseY + SLOT / 2 - 2, 0xFF8C8C8C);

        drawSwitchName(graphics, font, player, partialTick);
        drawCastBar(graphics, font, player, partialTick);
    }

    private static int darker(int rgb) {
        return ((rgb >> 1) & 0x7F7F7F);
    }

    private static void drawSwitchName(GuiGraphics graphics, Font font, LocalPlayer player, float partialTick) {
        if (switchedTo == null || ClientChannels.get(player) != null) {
            return;
        }
        float age = player.level().getGameTime() - switchedAt + partialTick;
        if (age < 0 || age > SWITCH_NAME_TICKS) {
            return;
        }
        int alpha = Mth.clamp(Math.round(255 * Math.min(1, (SWITCH_NAME_TICKS - age) / 10.0F)), 8, 255);
        Component name = Component.translatable(switchedTo.translationKey());
        graphics.drawCenteredString(font, name, graphics.guiWidth() / 2, graphics.guiHeight() - CAST_BAR_Y - 4,
                (alpha << 24) | switchedTo.color());
    }

    private static void drawCastBar(GuiGraphics graphics, Font font, LocalPlayer player, float partialTick) {
        ClientChannels.State state = ClientChannels.get(player);
        if (state == null || state.duration() <= 0) {
            return;
        }
        float progress = Mth.clamp(state.age(player.level().getGameTime(), partialTick) / state.duration(), 0, 1);
        int x = (graphics.guiWidth() - CAST_BAR_WIDTH) / 2;
        int y = graphics.guiHeight() - CAST_BAR_Y;
        int color = 0xFF000000 | state.skill().color();
        graphics.fill(x - 1, y - 1, x + CAST_BAR_WIDTH + 1, y + 5, 0xC0000000);
        graphics.fill(x, y, x + Math.round(CAST_BAR_WIDTH * (1 - progress)), y + 4, color);
        Component name = Component.translatable(state.skill().translationKey());
        if (state.skill() == Skill.HOLY_BEAM) {
            name = Component.translatable("hud.holy_staff.cancel_hint", name);
        }
        graphics.drawCenteredString(font, name, graphics.guiWidth() / 2, y - 11, color);
    }

    private static void drawSmall(GuiGraphics graphics, Font font, Component text, int centerX, int y, int color) {
        float scale = 0.5F;
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, y, 200);
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(font, text, -font.width(text) / 2, 0, color, true);
        graphics.pose().popPose();
    }
}
