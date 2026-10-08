package com.pockyl.holy_staff.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;

public final class ModKeyMappings {
    public static final String CATEGORY = "key.categories.holy_staff";

    /**
     * Optional key that switches the staff in hand to the next skill. Unbound by default (owner: R must not switch);
     * left click and sneak + mouse wheel always do.
     */
    public static final KeyMapping NEXT_SKILL = new KeyMapping("key.holy_staff.next_skill", KeyConflictContext.IN_GAME,
            InputConstants.UNKNOWN, CATEGORY);

    private ModKeyMappings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(NEXT_SKILL);
    }
}
