package com.pockyl.holy_staff.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;

import com.pockyl.holy_staff.skill.Skill;

/**
 * Body and staff poses. Holding: the staff arm is held forward like a mage ready to cast, the staff upright.
 * Holy Beam: the arm points at the target with the crystal ahead. Sanctuary: both hands lean on the staff planted in
 * the ground in front of the caster. The arm poses are Forge enum extensions of {@link HumanoidModel.ArmPose}.
 *
 * <p>Angles: an item-space rotation about X adds to the display rotation (models/item/holy_staff.json, third person
 * X = {@value #DISPLAY_PITCH} degrees), while raising the arm forward by A tilts the item back by A. The renderer
 * rotates the staff by {@link #staffPitch} to keep it where each pose wants it.
 */
public final class StaffPoses {
    public static final HumanoidModel.ArmPose HOLD = HumanoidModel.ArmPose.create("HOLY_STAFF_HOLD", false, StaffPoses::poseHold);
    public static final HumanoidModel.ArmPose BEAM = HumanoidModel.ArmPose.create("HOLY_STAFF_BEAM", false, StaffPoses::poseBeam);
    public static final HumanoidModel.ArmPose PLANTED = HumanoidModel.ArmPose.create("HOLY_STAFF_PLANTED", true, StaffPoses::posePlanted);

    /** Third-person display pitch of the item model, in degrees. Keep in sync with holy_staff.json. */
    static final float DISPLAY_PITCH = 27.0F;
    private static final float HOLD_ARM_PITCH = -0.75F;
    /** How much of the vanilla walking swing remains in the holding pose. */
    private static final float HOLD_SWING_KEPT = 0.2F;
    private static final float BEAM_ARM_PITCH = -1.35F;
    private static final float PLANTED_MAIN_PITCH = -0.55F;
    private static final float PLANTED_OTHER_PITCH = -0.8F;
    private static final float PLANTED_OTHER_YAW = 0.55F;
    /** Pivot of the GeckoLib item model inside the item renderer space (GeoItemRenderer translates by this). */
    private static final float PIVOT_X = 0.5F;
    private static final float PIVOT_Y = 0.51F;
    private static final float PIVOT_Z = 0.5F;
    /** How far the planted staff slides down through the hand, in blocks of item space (16 model pixels each). */
    private static final float PLANTED_SLIDE = 0.5F;

    private StaffPoses() {
    }

    public static HumanoidModel.ArmPose armPose(LivingEntity entity) {
        Skill skill = ClientChannels.skillOf(entity);
        if (skill == Skill.HOLY_BEAM) {
            return BEAM;
        }
        if (skill == Skill.SANCTUARY) {
            return PLANTED;
        }
        return HOLD;
    }

    private static ModelPart arm(HumanoidModel<?> model, HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
    }

    /** Arm pitch of the holding pose without the walking swing: forward, following half of the head pitch. */
    private static float holdPitch(HumanoidModel<?> model) {
        return HOLD_ARM_PITCH + model.head.xRot * 0.5F;
    }

    private static void poseHold(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        ModelPart part = arm(model, arm);
        part.xRot = Mth.lerp(1 - HOLD_SWING_KEPT, part.xRot, holdPitch(model));
        part.yRot = arm == HumanoidArm.RIGHT ? -0.15F : 0.15F;
    }

    private static void poseBeam(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        ModelPart part = arm(model, arm);
        part.xRot = BEAM_ARM_PITCH + model.head.xRot * 0.8F;
        part.yRot = model.head.yRot + (arm == HumanoidArm.RIGHT ? -0.1F : 0.1F);
        part.zRot = 0;
    }

    private static void posePlanted(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        boolean right = arm == HumanoidArm.RIGHT;
        ModelPart main = arm(model, arm);
        ModelPart other = right ? model.leftArm : model.rightArm;
        main.xRot = PLANTED_MAIN_PITCH;
        main.yRot = right ? -0.1F : 0.1F;
        main.zRot = 0;
        // The free hand reaches across to rest on the staff as well.
        other.xRot = PLANTED_OTHER_PITCH;
        other.yRot = right ? PLANTED_OTHER_YAW : -PLANTED_OTHER_YAW;
        other.zRot = 0;
    }

    /**
     * Extra item-space pitch (degrees) for a pose, relative to the display pitch. Derived from: final staff angle =
     * -(display + extra) - 90 - armRaise, where -180 is straight up and -90 points along the arm.
     */
    private static float staffPitch(Skill skill) {
        if (skill == Skill.HOLY_BEAM) {
            // Along the arm: display + extra = -90.
            return -90.0F - DISPLAY_PITCH;
        }
        // Planted: upright (-175) with the arm raised by PLANTED_MAIN_PITCH.
        float raise = -PLANTED_MAIN_PITCH * Mth.RAD_TO_DEG;
        return 175.0F - 90.0F - raise - DISPLAY_PITCH;
    }

    /** Re-orients the staff inside the item renderer space (display transform already applied) during a channel. */
    public static void applyItemTransform(PoseStack poseStack, ItemDisplayContext context, Skill skill) {
        boolean firstPerson = context.firstPerson();
        poseStack.translate(PIVOT_X, PIVOT_Y, PIVOT_Z);
        if (skill == Skill.HOLY_BEAM) {
            if (firstPerson) {
                // Point the staff into the screen, crystal towards the crosshair.
                poseStack.mulPose(Axis.XP.rotationDegrees(-60.0F));
                poseStack.translate(0, 0.25F, 0);
            } else {
                poseStack.mulPose(Axis.XP.rotationDegrees(staffPitch(skill)));
            }
        } else if (skill == Skill.SANCTUARY) {
            if (firstPerson) {
                poseStack.translate(0, -0.8F, -0.3F);
            } else {
                poseStack.mulPose(Axis.XP.rotationDegrees(staffPitch(skill)));
                poseStack.translate(0, -PLANTED_SLIDE, 0);
            }
        }
        poseStack.translate(-PIVOT_X, -PIVOT_Y, -PIVOT_Z);
    }
}
