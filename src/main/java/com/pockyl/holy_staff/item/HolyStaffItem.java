package com.pockyl.holy_staff.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import com.pockyl.holy_staff.Config;
import com.pockyl.holy_staff.client.HolyStaffClient;
import com.pockyl.holy_staff.registry.ModAttachments;
import com.pockyl.holy_staff.registry.ModDataComponents;
import com.pockyl.holy_staff.skill.Channels;
import com.pockyl.holy_staff.skill.Skill;
import com.pockyl.holy_staff.skill.SkillCaster;

import java.util.List;
import java.util.function.Consumer;

/**
 * The Holy Staff. Right click casts the selected skill through the vanilla "use item" flow, so blocks like chests
 * still open on a plain right click. The selected skill is stored on the stack and switched on the client with left
 * click or sneak + mouse wheel ({@code ClientInputHandler}).
 */
public final class HolyStaffItem extends Item implements GeoItem {
    public static final String CAST_CONTROLLER = "cast";
    public static final String CAST_ANIMATION = "cast";

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final float CREATIVE_HEAL_MULTIPLIER = 2.0F;
    private static final double CREATIVE_BEAM_RANGE_MULTIPLIER = 2.0;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final boolean creative;

    public HolyStaffItem(Properties properties, boolean creative) {
        super(properties);
        this.creative = creative;
        GeoItem.registerSyncedAnimatable(this);
    }

    /** The Creative Holy Staff: no cooldowns, stronger heals and a longer beam. */
    public boolean isCreative() {
        return creative;
    }

    public int cooldown(Skill skill) {
        return creative ? 0 : skill.cooldown();
    }

    public float healMultiplier() {
        return creative ? CREATIVE_HEAL_MULTIPLIER : 1.0F;
    }

    public double beamRange() {
        return Config.beamRange() * (creative ? CREATIVE_BEAM_RANGE_MULTIPLIER : 1.0);
    }

    public static Skill selected(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? Skill.BLESSED_GROUND : Skill.byName(tag.getString(ModDataComponents.SELECTED_SKILL), Skill.BLESSED_GROUND);
    }

    /** Stores the selected skill on the stack, without any checks. */
    public static void setSelected(ItemStack stack, Skill skill) {
        stack.getOrCreateTag().putString(ModDataComponents.SELECTED_SKILL, skill.getSerializedName());
    }

    /** Selects the skill on the staff in the main hand. Not allowed while channelling. */
    public static boolean select(Player player, Skill skill) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof HolyStaffItem) || Channels.isChannelling(player)) {
            return false;
        }
        setSelected(stack, skill);
        return true;
    }

    /** Looping animation while a channelled skill runs. */
    public static String channelAnimation(Skill skill) {
        return skill == Skill.SANCTUARY ? "channel_sanctuary" : "channel_beam";
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        // Consume (no swing) while on cooldown, so the off-hand item is not used instead when right click is held.
        if (level.isClientSide()) {
            Skill skill = selected(stack);
            boolean ready = ModAttachments.cooldowns(player).isReady(skill, level.getGameTime());
            boolean swing = ready && !skill.isChannelled();
            return swing ? InteractionResultHolder.success(stack) : InteractionResultHolder.consume(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            SkillCaster.tryCast(serverPlayer);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        // GeckoLib ids and skill changes update the NBT tag; that must not bob the item.
        return slotChanged || !newStack.is(this);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.holy_staff.description").withStyle(ChatFormatting.GRAY));
        if (creative) {
            tooltip.add(Component.translatable("tooltip.holy_staff.creative").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        Skill selected = selected(stack);
        for (Skill skill : Skill.values()) {
            boolean active = skill == selected;
            tooltip.add(Component.literal(active ? "> " : "  ")
                    .append(Component.translatable(skill.translationKey()))
                    .withStyle(active ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
            if (active) {
                tooltip.add(Component.literal("   ").append(Component.translatable(skill.translationKey() + ".description"))
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        tooltip.add(Component.translatable("tooltip.holy_staff.controls").withStyle(ChatFormatting.DARK_AQUA));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 0, state -> state.setAndContinue(IDLE)));
        AnimationController<HolyStaffItem> cast = new AnimationController<>(this, CAST_CONTROLLER, 2, state -> PlayState.STOP);
        cast.triggerableAnim(CAST_ANIMATION, RawAnimation.begin().thenPlay(CAST_ANIMATION));
        for (Skill skill : List.of(Skill.HOLY_BEAM, Skill.SANCTUARY)) {
            cast.triggerableAnim(channelAnimation(skill), RawAnimation.begin().thenLoop(channelAnimation(skill)));
        }
        controllers.add(cast);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // Forge only invokes this on the physical client, so the client classes are never loaded on a dedicated server.
    // It runs inside the Item constructor: the extensions must read the creative flag lazily.
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(HolyStaffClient.itemExtensions(this));
    }
}
