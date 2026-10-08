package com.pockyl.holy_staff.registry;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.Nullable;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.skill.SkillCooldowns;

/** Per-player data, attached to every player as a Forge capability. */
public final class ModAttachments {
    /** Skill cooldowns of a player. Transient: cooldowns reset on relog, which is harmless for a few seconds of waiting. */
    public static final Capability<SkillCooldowns> COOLDOWNS = CapabilityManager.get(new CapabilityToken<>() {
    });

    private ModAttachments() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.register(SkillCooldowns.class));
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, ModAttachments::attach);
    }

    /** The cooldowns of the player on the side the player object belongs to. */
    public static SkillCooldowns cooldowns(Player player) {
        // A player whose capabilities were already invalidated (removed after death) has nothing left to cast.
        return player.getCapability(COOLDOWNS).orElseGet(SkillCooldowns::new);
    }

    private static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(HolyStaff.id("cooldowns"), new CooldownsProvider());
        }
    }

    private static final class CooldownsProvider implements ICapabilityProvider {
        private final LazyOptional<SkillCooldowns> cooldowns = LazyOptional.of(SkillCooldowns::new);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
            return COOLDOWNS.orEmpty(capability, cooldowns);
        }
    }
}
