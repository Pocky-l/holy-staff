package com.pockyl.holy_staff.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.pockyl.holy_staff.HolyStaff;
import com.pockyl.holy_staff.entity.BlessedGround;
import com.pockyl.holy_staff.item.HolyStaffItem;
import com.pockyl.holy_staff.registry.ModAttachments;
import com.pockyl.holy_staff.registry.ModItems;
import com.pockyl.holy_staff.skill.Channels;
import com.pockyl.holy_staff.skill.Healing;
import com.pockyl.holy_staff.skill.Skill;
import com.pockyl.holy_staff.skill.SkillCaster;

import java.util.UUID;

/**
 * In-game tests, run headless by {@code gradlew runGameTestServer}.
 * Tests use the 1x1x1 {@code empty} structure unless they need a prepared one. Every test has its own batch, so they
 * run one after another: area heals of one test would otherwise reach the mobs of a neighbouring test.
 */
@GameTestHolder(HolyStaff.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ModGameTests {
    private static final float EPSILON = 0.01F;

    private ModGameTests() {
    }

    @GameTest(template = "empty", batch = "modLoads")
    public static void modLoads(GameTestHelper helper) {
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "onlyFriendlyLivingEntitiesAreAllies")
    public static void onlyFriendlyLivingEntitiesAreAllies(GameTestHelper helper) {
        Mob cow = still(helper, EntityType.COW, new Vec3(0.5, 3, 0.5));
        Zombie zombie = still(helper, EntityType.ZOMBIE, new Vec3(2.5, 3, 0.5));
        ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, new Vec3(4.5, 3, 0.5));

        helper.assertTrue(Healing.canHeal(cow), "animals are healed");
        helper.assertFalse(Healing.canHeal(zombie), "monsters are not healed by default");
        helper.assertFalse(Healing.canHeal(stand), "armor stands are not healed");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "skillsAreSelectedOnTheStaffAndCycle")
    public static void skillsAreSelectedOnTheStaffAndCycle(GameTestHelper helper) {
        ServerPlayer player = caster(helper, new Vec3(0.5, 3, 0.5), Skill.BLESSED_GROUND);
        helper.assertTrue(HolyStaffItem.selected(new ItemStack(ModItems.HOLY_STAFF.get())) == Skill.BLESSED_GROUND,
                "a new staff starts with Blessed Ground");
        helper.assertTrue(Skill.SANCTUARY.cycle(1) == Skill.BLESSED_GROUND, "switching wraps around");
        helper.assertTrue(Skill.BLESSED_GROUND.cycle(-1) == Skill.SANCTUARY, "switching back wraps around");

        helper.assertTrue(HolyStaffItem.select(player, Skill.HOLY_BEAM), "a skill can be selected");
        helper.assertTrue(HolyStaffItem.selected(player.getMainHandItem()) == Skill.HOLY_BEAM, "the staff remembers it");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "skillsNeedTheStaffInHand")
    public static void skillsNeedTheStaffInHand(GameTestHelper helper) {
        ServerPlayer player = caster(helper, new Vec3(0.5, 3, 0.5), Skill.SANCTUARY);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        helper.assertFalse(SkillCaster.tryCast(player), "no staff, no cast");
        helper.assertFalse(Channels.isChannelling(player), "nothing started");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "blessedGroundFillsUpThenHealsEveryoneInside")
    public static void blessedGroundFillsUpThenHealsEveryoneInside(GameTestHelper helper) {
        ServerPlayer player = caster(helper, new Vec3(0.5, 3, 0.5), Skill.BLESSED_GROUND);
        player.setXRot(-90.0F);
        player.setHealth(4.0F);
        Mob cow = still(helper, EntityType.COW, new Vec3(2.0, 3, 0.5));
        cow.setHealth(1.0F);
        Mob far = still(helper, EntityType.COW, new Vec3(8.5, 3, 0.5));
        far.setHealth(1.0F);

        helper.assertTrue(SkillCaster.tryCast(player), "the cast succeeds");
        helper.assertFalse(SkillCaster.tryCast(player), "the second cast is blocked by the cooldown");
        helper.assertTrue(player.getHealth() == 4.0F, "nothing is healed while the circle fills up");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(BlessedGround.class, player.getBoundingBox().inflate(4)).size() == 1,
                "the circle is placed under the caster");
        // Default delay: 0.6 s = 12 ticks.
        helper.runAfterDelay(16, () -> {
            helper.assertTrue(near(player.getHealth(), 11.0F), "the caster inside is healed by 7, got " + player.getHealth());
            helper.assertTrue(near(cow.getHealth(), 8.0F), "the cow inside is healed by 7, got " + cow.getHealth());
            helper.assertTrue(far.getHealth() == 1.0F, "the cow outside is not healed");
            player.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "holyBeamChannelsHealsItsTargetAndCanBeCancelled")
    public static void holyBeamChannelsHealsItsTargetAndCanBeCancelled(GameTestHelper helper) {
        ServerPlayer player = caster(helper, new Vec3(0.5, 3, 0.5), Skill.HOLY_BEAM);
        Mob cow = still(helper, EntityType.COW, new Vec3(8.5, 3, 0.5));
        cow.setHealth(1.0F);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, cow.getBoundingBox().getCenter());

        helper.assertTrue(SkillCaster.tryCast(player), "the beam starts on the aimed ally");
        helper.assertTrue(Channels.current(player) == Skill.HOLY_BEAM, "the caster is channelling");
        helper.assertFalse(HolyStaffItem.select(player, Skill.SANCTUARY), "no switching while channelling");
        // 8 health per second, healed in steps of 2 every 5 ticks (first step right away).
        helper.runAfterDelay(11, () -> {
            helper.assertTrue(near(cow.getHealth(), 7.0F), "three heal steps after 11 ticks, got " + cow.getHealth());
            Channels.stop(player);
            helper.assertFalse(Channels.isChannelling(player), "the beam is cancelled");
        });
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(near(cow.getHealth(), 7.0F), "no heal after cancelling, got " + cow.getHealth());
            player.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "holyBeamNeedsATarget")
    public static void holyBeamNeedsATarget(GameTestHelper helper) {
        ServerPlayer player = caster(helper, new Vec3(0.5, 3, 0.5), Skill.HOLY_BEAM);
        player.setXRot(-90.0F);

        helper.assertFalse(SkillCaster.tryCast(player), "no ally aimed at, no beam");
        helper.assertFalse(Channels.isChannelling(player), "not channelling");
        helper.assertTrue(ModAttachments.cooldowns(player).isReady(Skill.HOLY_BEAM, helper.getLevel().getGameTime()),
                "a failed cast does not start the cooldown");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 120, batch = "sanctuaryThrowsEnemiesBackAndHealsInPulses")
    public static void sanctuaryThrowsEnemiesBackAndHealsInPulses(GameTestHelper helper) {
        ServerPlayer player = caster(helper, new Vec3(0.5, 3, 0.5), Skill.SANCTUARY);
        player.setHealth(2.0F);
        Mob cow = still(helper, EntityType.COW, new Vec3(4.5, 3, 0.5));
        cow.setHealth(1.0F);
        Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new Vec3(0.5, 3, 2.5));
        zombie.setNoGravity(true);

        helper.assertTrue(SkillCaster.tryCast(player), "the cast succeeds");
        helper.runAfterDelay(2, () -> helper.assertTrue(zombie.getDeltaMovement().z > 0.3, "the zombie is thrown away from the caster"));
        // 6 pulses of 2.5 (every 10 ticks over 3 s); the cow has only 10 max health.
        helper.runAfterDelay(65, () -> {
            helper.assertFalse(Channels.isChannelling(player), "the channel ends after its duration");
            helper.assertTrue(near(player.getHealth(), 17.0F), "six pulses healed the caster, got " + player.getHealth());
            helper.assertTrue(near(cow.getHealth(), 10.0F), "the cow is fully healed, got " + cow.getHealth());
            helper.assertFalse(SkillCaster.tryCast(player), "the 20 s cooldown is still running");
            player.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 60, batch = "creativeBlessedGroundHasNoCooldownAndHealsDouble")
    public static void creativeBlessedGroundHasNoCooldownAndHealsDouble(GameTestHelper helper) {
        ServerPlayer player = caster(helper, new Vec3(0.5, 3, 0.5), Skill.BLESSED_GROUND, ModItems.CREATIVE_HOLY_STAFF.get());
        player.setXRot(-90.0F);
        player.setHealth(2.0F);

        helper.assertTrue(SkillCaster.tryCast(player), "the cast succeeds");
        helper.assertTrue(ModAttachments.cooldowns(player).isReady(Skill.BLESSED_GROUND, helper.getLevel().getGameTime()),
                "the creative staff has no cooldown");
        helper.runAfterDelay(16, () -> {
            helper.assertTrue(near(player.getHealth(), 16.0F), "the caster is healed by 2 x 7, got " + player.getHealth());
            player.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40, batch = "creativeHolyBeamReachesTwiceAsFar")
    public static void creativeHolyBeamReachesTwiceAsFar(GameTestHelper helper) {
        Mob cow = still(helper, EntityType.COW, new Vec3(24.5, 3, 0.5));
        cow.setHealth(1.0F);
        ServerPlayer regular = caster(helper, new Vec3(0.5, 3, 0.5), Skill.HOLY_BEAM);
        regular.lookAt(EntityAnchorArgument.Anchor.EYES, cow.getBoundingBox().getCenter());
        helper.assertFalse(SkillCaster.tryCast(regular), "24 blocks is out of the regular beam's range");
        regular.discard();

        ServerPlayer creative = caster(helper, new Vec3(0.5, 3, 0.5), Skill.HOLY_BEAM, ModItems.CREATIVE_HOLY_STAFF.get());
        creative.lookAt(EntityAnchorArgument.Anchor.EYES, cow.getBoundingBox().getCenter());
        helper.assertTrue(SkillCaster.tryCast(creative), "the creative beam reaches 24 blocks");
        // The first step of 2 health is doubled.
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(near(cow.getHealth(), 5.0F), "the first heal step is doubled, got " + cow.getHealth());
            Channels.stop(creative);
            creative.discard();
            helper.succeed();
        });
    }

    private static boolean near(float actual, float expected) {
        return Math.abs(actual - expected) < EPSILON;
    }

    private static ServerPlayer caster(GameTestHelper helper, Vec3 relative, Skill skill) {
        return caster(helper, relative, skill, ModItems.HOLY_STAFF.get());
    }

    private static ServerPlayer caster(GameTestHelper helper, Vec3 relative, Skill skill, HolyStaffItem item) {
        ServerPlayer player = mockPlayer(helper);
        Vec3 pos = helper.absoluteVec(relative);
        player.moveTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        player.setNoGravity(true);
        ItemStack staff = new ItemStack(item);
        HolyStaffItem.setSelected(staff, skill);
        player.setItemInHand(InteractionHand.MAIN_HAND, staff);
        return player;
    }

    /**
     * The mock player of {@link GameTestHelper#makeMockServerPlayerInLevel()}, but with an embedded network channel:
     * Forge's login hooks need one, and the vanilla mock connection has none. Nothing on the other end announces this
     * mod's network channel, so the staff treats the player like a client without the mod and sends it no packets.
     */
    private static ServerPlayer mockPlayer(GameTestHelper helper) {
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "test-mock-player")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player);
        return player;
    }

    private static <T extends Mob> T still(GameTestHelper helper, EntityType<T> type, Vec3 relative) {
        T mob = helper.spawnWithNoFreeWill(type, relative);
        mob.setNoGravity(true);
        mob.setInvulnerable(true);
        return mob;
    }
}
