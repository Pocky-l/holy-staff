package com.pockyl.holy_staff.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.skill.SkillCooldowns;

import java.util.function.Supplier;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HolyStaff.MOD_ID);

    /** Skill cooldowns of a player. Transient: cooldowns reset on relog, which is harmless for a few seconds of waiting. */
    public static final Supplier<AttachmentType<SkillCooldowns>> COOLDOWNS = ATTACHMENT_TYPES.register("cooldowns",
            () -> AttachmentType.builder(SkillCooldowns::new).build());

    private ModAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
