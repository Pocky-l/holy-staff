package com.pockyl.holy_staff.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import com.pockyl.holy_staff.HolyStaff;

@Mod(value = HolyStaff.MOD_ID, dist = Dist.CLIENT)
public final class HolyStaffClient {
    public HolyStaffClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
