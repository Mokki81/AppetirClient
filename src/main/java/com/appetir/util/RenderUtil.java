package com.appetir.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public final class RenderUtil {

    private RenderUtil() {}

    public static void drawEntityBox(MatrixStack matrices, VertexConsumerProvider provider,
                                     Entity e, float tickDelta, int argb) {
        drawEntityBox(matrices, provider, e, tickDelta, argb, false, 1.5f);
    }

    public static void drawEntityBox(MatrixStack matrices, VertexConsumerProvider provider,
                                     Entity e, float tickDelta, int argb, boolean fill, float lineWidth) {
        double x = MathHelper.lerp(tickDelta, e.lastRenderX, e.getX()) - e.getX();
        double y = MathHelper.lerp(tickDelta, e.lastRenderY, e.getY()) - e.getY();
        double z = MathHelper.lerp(tickDelta, e.lastRenderZ, e.getZ()) - e.getZ();

        Box box = e.getBoundingBox().offset(-e.getX(), -e.getY(), -e.getZ()).offset(x, y, z);

        matrices.push();
        Matrix4f mat = matrices.peek().getModel();

        float a = ((argb >> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;

        if (fill) {
            drawFilledBox(mat, box, r, g, b, a * 0.25f);
        }
        drawOutlinedBox(mat, box, r, g, b, a, lineWidth);

        matrices.pop();
    }

    /** Simple stick-figure skeleton for players (CatLean-inspired). */
    public static void drawSkeleton(MatrixStack matrices, Entity e, float tickDelta, int argb) {
        if (!(e instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) e;

        double ix = MathHelper.lerp(tickDelta, e.lastRenderX, e.getX());
        double iy = MathHelper.lerp(tickDelta, e.lastRenderY, e.getY());
        double iz = MathHelper.lerp(tickDelta, e.lastRenderZ, e.getZ());

        float h = le.getHeight();
        float w = le.getWidth();

        // Body points relative to entity feet
        Vec3d feet = new Vec3d(ix, iy, iz);
        Vec3d pelvis = feet.add(0, h * 0.45, 0);
        Vec3d chest = feet.add(0, h * 0.72, 0);
        Vec3d head = feet.add(0, h * 0.92, 0);
        Vec3d lShoulder = chest.add(-w * 0.35, 0, 0);
        Vec3d rShoulder = chest.add(w * 0.35, 0, 0);
        Vec3d lHand = lShoulder.add(0, -h * 0.28, 0);
        Vec3d rHand = rShoulder.add(0, -h * 0.28, 0);
        Vec3d lFoot = feet.add(-w * 0.2, 0, 0);
        Vec3d rFoot = feet.add(w * 0.2, 0, 0);

        float a = ((argb >> 24) & 0xFF) / 255f;
        float rr = ((argb >> 16) & 0xFF) / 255f;
        float gg = ((argb >> 8) & 0xFF) / 255f;
        float bb = (argb & 0xFF) / 255f;

        matrices.push();
        // Convert to camera-relative later via drawLine world coords helper
        drawWorldLine(matrices, pelvis, chest, rr, gg, bb, a);
        drawWorldLine(matrices, chest, head, rr, gg, bb, a);
        drawWorldLine(matrices, chest, lShoulder, rr, gg, bb, a);
        drawWorldLine(matrices, chest, rShoulder, rr, gg, bb, a);
        drawWorldLine(matrices, lShoulder, lHand, rr, gg, bb, a);
        drawWorldLine(matrices, rShoulder, rHand, rr, gg, bb, a);
        drawWorldLine(matrices, pelvis, lFoot, rr, gg, bb, a);
        drawWorldLine(matrices, pelvis, rFoot, rr, gg, bb, a);
        matrices.pop();
    }

    public static void drawLine(MatrixStack matrices, VertexConsumerProvider provider,
                                double x, double y, double z, int argb) {
        // Vertical marker from eyes upward — used by Arrows module
        float a = ((argb >> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        matrices.push();
        drawWorldLine(matrices, new Vec3d(x, y, z), new Vec3d(x, y + 0.6, z), r, g, b, a);
        matrices.pop();
    }

    public static void drawBlockBox(MatrixStack matrices, VertexConsumerProvider provider,
                                    int x, int y, int z, int argb) {
        Box box = new Box(x, y, z, x + 1, y + 1, z + 1);
        matrices.push();
        Matrix4f mat = matrices.peek().getModel();
        float a = ((argb >> 24) & 0xFF) / 255f;
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        drawOutlinedBox(mat, box, r, g, b, a, 1.5f);
        matrices.pop();
    }

    private static void drawWorldLine(MatrixStack matrices, Vec3d a, Vec3d b,
                                      float r, float g, float bl, float alpha) {
        // Caller must have set up render state; we draw in absolute world space
        // offset by camera is handled by WorldRenderer matrix stack typically.
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);
        buf.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        Matrix4f mat = matrices.peek().getModel();
        buf.vertex(mat, (float) a.x, (float) a.y, (float) a.z).color(r, g, bl, alpha).next();
        buf.vertex(mat, (float) b.x, (float) b.y, (float) b.z).color(r, g, bl, alpha).next();
        tess.draw();
        RenderSystem.enableTexture();
    }

    private static void drawOutlinedBox(Matrix4f mat, Box box, float r, float g, float b, float a, float width) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(width);
        buf.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);

        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;

        // bottom
        line(buf, mat, x1, y1, z1, x2, y1, z1, r, g, b, a);
        line(buf, mat, x2, y1, z1, x2, y1, z2, r, g, b, a);
        line(buf, mat, x2, y1, z2, x1, y1, z2, r, g, b, a);
        line(buf, mat, x1, y1, z2, x1, y1, z1, r, g, b, a);
        // top
        line(buf, mat, x1, y2, z1, x2, y2, z1, r, g, b, a);
        line(buf, mat, x2, y2, z1, x2, y2, z2, r, g, b, a);
        line(buf, mat, x2, y2, z2, x1, y2, z2, r, g, b, a);
        line(buf, mat, x1, y2, z2, x1, y2, z1, r, g, b, a);
        // pillars
        line(buf, mat, x1, y1, z1, x1, y2, z1, r, g, b, a);
        line(buf, mat, x2, y1, z1, x2, y2, z1, r, g, b, a);
        line(buf, mat, x2, y1, z2, x2, y2, z2, r, g, b, a);
        line(buf, mat, x1, y1, z2, x1, y2, z2, r, g, b, a);

        tess.draw();
        RenderSystem.enableTexture();
    }

    private static void drawFilledBox(Matrix4f mat, Box box, float r, float g, float b, float a) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        buf.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);

        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;

        // 6 faces
        quad(buf, mat, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, r, g, b, a);
        quad(buf, mat, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, r, g, b, a);
        quad(buf, mat, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, r, g, b, a);
        quad(buf, mat, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, r, g, b, a);
        quad(buf, mat, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, r, g, b, a);
        quad(buf, mat, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, r, g, b, a);

        tess.draw();
        RenderSystem.enableTexture();
    }

    private static void line(BufferBuilder buf, Matrix4f mat,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float r, float g, float b, float a) {
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a).next();
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a).next();
    }

    private static void quad(BufferBuilder buf, Matrix4f mat,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float r, float g, float b, float a) {
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a).next();
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a).next();
        buf.vertex(mat, x3, y3, z3).color(r, g, b, a).next();
        buf.vertex(mat, x4, y4, z4).color(r, g, b, a).next();
    }
}
