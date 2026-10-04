package com.pockyl.holy_staff.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import com.pockyl.holy_staff.HolyStaff;

/** Simple glowing 3D shapes of the skill visuals: ground decals, light walls and beams. Positions relative to the pose. */
public final class HolyShapes {
    public static final ResourceLocation RING = HolyStaff.id("textures/entity/ring.png");
    public static final ResourceLocation DISC = HolyStaff.id("textures/entity/disc.png");
    public static final ResourceLocation COLUMN = HolyStaff.id("textures/entity/column.png");
    public static final ResourceLocation BEAM = HolyStaff.id("textures/entity/beam.png");
    public static final ResourceLocation RUNES = HolyStaff.id("textures/entity/runes.png");
    public static final ResourceLocation FLARE = HolyStaff.id("textures/entity/flare.png");
    public static final ResourceLocation RAYS = HolyStaff.id("textures/entity/rays.png");
    public static final ResourceLocation DOME = HolyStaff.id("textures/entity/dome.png");

    private HolyShapes() {
    }

    /** A flat square on the XZ plane, centred on the origin. */
    public static void flat(PoseStack poseStack, VertexConsumer consumer, float radius, int rgb, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        int a = alpha(alpha);
        vertex(consumer, pose, -radius, 0, -radius, 0, 0, rgb, a, 0, 1, 0);
        vertex(consumer, pose, -radius, 0, radius, 0, 1, rgb, a, 0, 1, 0);
        vertex(consumer, pose, radius, 0, radius, 1, 1, rgb, a, 0, 1, 0);
        vertex(consumer, pose, radius, 0, -radius, 1, 0, rgb, a, 0, 1, 0);
    }

    /** An open cylinder wall standing on the XZ plane; the texture runs bottom (v=1) to top (v=0). */
    public static void cylinder(PoseStack poseStack, VertexConsumer consumer, float radius, float height, int segments, int rgb, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        int a = alpha(alpha);
        for (int i = 0; i < segments; i++) {
            float a0 = Mth.TWO_PI * i / segments;
            float a1 = Mth.TWO_PI * (i + 1) / segments;
            float x0 = Mth.cos(a0) * radius;
            float z0 = Mth.sin(a0) * radius;
            float x1 = Mth.cos(a1) * radius;
            float z1 = Mth.sin(a1) * radius;
            float u0 = (float) i / segments;
            float u1 = (float) (i + 1) / segments;
            float nx = Mth.cos((a0 + a1) / 2);
            float nz = Mth.sin((a0 + a1) / 2);
            vertex(consumer, pose, x0, 0, z0, u0, 1, rgb, a, nx, 0, nz);
            vertex(consumer, pose, x1, 0, z1, u1, 1, rgb, a, nx, 0, nz);
            vertex(consumer, pose, x1, height, z1, u1, 0, rgb, a, nx, 0, nz);
            vertex(consumer, pose, x0, height, z0, u0, 0, rgb, a, nx, 0, nz);
        }
    }

    /** A square facing the camera, centred on the origin (used for glows and flares). */
    public static void billboard(PoseStack poseStack, VertexConsumer consumer, Quaternionf cameraRotation, float size, float spin,
            int rgb, float alpha) {
        poseStack.pushPose();
        poseStack.mulPose(cameraRotation);
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
        PoseStack.Pose pose = poseStack.last();
        int a = alpha(alpha);
        vertex(consumer, pose, -size, -size, 0, 0, 1, rgb, a, 0, 0, 1);
        vertex(consumer, pose, size, -size, 0, 1, 1, rgb, a, 0, 0, 1);
        vertex(consumer, pose, size, size, 0, 1, 0, rgb, a, 0, 0, 1);
        vertex(consumer, pose, -size, size, 0, 0, 0, rgb, a, 0, 0, 1);
        poseStack.popPose();
    }

    /** A hemisphere standing on the XZ plane; the texture runs from the ground (v=1) to the top (v=0). */
    public static void dome(PoseStack poseStack, VertexConsumer consumer, float radius, int rings, int segments, int rgb, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        int a = alpha(alpha);
        for (int r = 0; r < rings; r++) {
            float p0 = Mth.HALF_PI * r / rings;
            float p1 = Mth.HALF_PI * (r + 1) / rings;
            float y0 = Mth.sin(p0) * radius;
            float y1 = Mth.sin(p1) * radius;
            float r0 = Mth.cos(p0) * radius;
            float r1 = Mth.cos(p1) * radius;
            float v0 = 1 - (float) r / rings;
            float v1 = 1 - (float) (r + 1) / rings;
            for (int i = 0; i < segments; i++) {
                float a0 = Mth.TWO_PI * i / segments;
                float a1 = Mth.TWO_PI * (i + 1) / segments;
                float u0 = (float) i / segments;
                float u1 = (float) (i + 1) / segments;
                float c0 = Mth.cos(a0);
                float s0 = Mth.sin(a0);
                float c1 = Mth.cos(a1);
                float s1 = Mth.sin(a1);
                vertex(consumer, pose, c0 * r0, y0, s0 * r0, u0, v0, rgb, a, c0, 0, s0);
                vertex(consumer, pose, c1 * r0, y0, s1 * r0, u1, v0, rgb, a, c1, 0, s1);
                vertex(consumer, pose, c1 * r1, y1, s1 * r1, u1, v1, rgb, a, c1, 0, s1);
                vertex(consumer, pose, c0 * r1, y1, s0 * r1, u0, v1, rgb, a, c0, 0, s0);
            }
        }
    }

    /**
     * A flat ribbon from one point to another that always faces the camera (both relative to the pose origin).
     * The texture's v axis runs across the ribbon, so a soft-edged gradient gives a round-looking beam.
     */
    public static void ribbon(PoseStack poseStack, VertexConsumer consumer, Vec3 from, Vec3 to, Vec3 camera, float width,
            int rgb, float alphaFrom, float alphaTo) {
        Vec3 axis = to.subtract(from);
        Vec3 toCamera = camera.subtract(from.add(to).scale(0.5));
        Vec3 side = axis.cross(toCamera);
        if (side.lengthSqr() < 1.0E-8) {
            return;
        }
        side = side.normalize().scale(width / 2);
        PoseStack.Pose pose = poseStack.last();
        int a0 = alpha(alphaFrom);
        int a1 = alpha(alphaTo);
        vertex(consumer, pose, (float) (from.x - side.x), (float) (from.y - side.y), (float) (from.z - side.z), 0, 0, rgb, a0, 0, 1, 0);
        vertex(consumer, pose, (float) (from.x + side.x), (float) (from.y + side.y), (float) (from.z + side.z), 0, 1, rgb, a0, 0, 1, 0);
        vertex(consumer, pose, (float) (to.x + side.x), (float) (to.y + side.y), (float) (to.z + side.z), 1, 1, rgb, a1, 0, 1, 0);
        vertex(consumer, pose, (float) (to.x - side.x), (float) (to.y - side.y), (float) (to.z - side.z), 1, 0, rgb, a1, 0, 1, 0);
    }

    private static int alpha(float alpha) {
        return Mth.clamp(Math.round(alpha * 255), 0, 255);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int rgb,
            int alpha, float nx, float ny, float nz) {
        consumer.addVertex(pose, x, y, z)
                .setColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }
}
