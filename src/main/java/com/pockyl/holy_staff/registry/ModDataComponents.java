package com.pockyl.holy_staff.registry;

/**
 * Keys of the item data of the staff. Minecraft 1.20.1 has no data components, so the data lives in the stack's NBT
 * tag, which is saved with the stack and synced to clients like any other item data.
 */
public final class ModDataComponents {
    /** The skill a staff casts on right click, stored by its serialized name. */
    public static final String SELECTED_SKILL = "SelectedSkill";

    private ModDataComponents() {
    }
}
