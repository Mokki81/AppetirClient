package com.appetir.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class RenderUtil {

    public static void drawEntityBox(MatrixStack matrices, VertexConsumerProvider provider,
                                     Entity entity, float tickDelta, int color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();

        double x = entity.prevX + (entity.getX() - entity.prevX) * tickDelta - cam.x;
        double y = entity.prevY + (entity.getY() - entity.prevY) * tickDelta - cam.y;
        double z = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta - cam.z;

        Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());

        matrices.push();
        matrices.translate(x, y, z);
        VertexConsumer vc = provider.getBuffer(RenderLayer.getLines());
        drawBox(matrices, vc, box, color);
        matrices.pop();
    }

    public static void drawBlockBox(MatrixStack matrices, VertexConsumerProvider provider,
                                    double bx, double by, double bz, int color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        matrices.push();
        matrices.translate(bx - cam.x, by - cam.y, bz - cam.z);
        VertexConsumer vc = provider.getBuffer(RenderLayer.getLines());
        drawBox(matrices, vc, new Box(0, 0, 0, 1, 1, 1), color);
        matrices.pop();
    }

    public static void drawLine(MatrixStack matrices, VertexConsumerProvider provider,
                                double tx, double ty, double tz, int color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        float r = ((color>>16)&0xFF)/255f, g = ((color>>8)&0xFF)/255f, b = ((color)&0xFF)/255f;
        matrices.push();
        VertexConsumer vc = provider.getBuffer(RenderLayer.getLines());
        Matrix4f mat = matrices.peek().getModel();
        double ex=tx-cam.x, ey=ty-cam.y, ez=tz-cam.z;
        float len = (float)Math.sqrt(ex*ex+ey*ey+ez*ez);
        if (len>0) {
            vc.vertex(mat,0,0,0).color(r,g,b,1f).normal((float)(ex/len),(float)(ey/len),(float)(ez/len)).next();
            vc.vertex(mat,(float)ex,(float)ey,(float)ez).color(r,g,b,1f).normal((float)(ex/len),(float)(ey/len),(float)(ez/len)).next();
        }
        matrices.pop();
    }

    private static void drawBox(MatrixStack matrices, VertexConsumer vc, Box box, int color) {
        float r=((color>>16)&0xFF)/255f, g=((color>>8)&0xFF)/255f,
              b=((color)&0xFF)/255f,   a=((color>>24)&0xFF)/255f;
        Matrix4f mat = matrices.peek().getModel();
        float x1=(float)box.minX,y1=(float)box.minY,z1=(float)box.minZ;
        float x2=(float)box.maxX,y2=(float)box.maxY,z2=(float)box.maxZ;
        // Bottom
        ln(vc,mat,x1,y1,z1,x2,y1,z1,r,g,b,a); ln(vc,mat,x2,y1,z1,x2,y1,z2,r,g,b,a);
        ln(vc,mat,x2,y1,z2,x1,y1,z2,r,g,b,a); ln(vc,mat,x1,y1,z2,x1,y1,z1,r,g,b,a);
        // Top
        ln(vc,mat,x1,y2,z1,x2,y2,z1,r,g,b,a); ln(vc,mat,x2,y2,z1,x2,y2,z2,r,g,b,a);
        ln(vc,mat,x2,y2,z2,x1,y2,z2,r,g,b,a); ln(vc,mat,x1,y2,z2,x1,y2,z1,r,g,b,a);
        // Verticals
        ln(vc,mat,x1,y1,z1,x1,y2,z1,r,g,b,a); ln(vc,mat,x2,y1,z1,x2,y2,z1,r,g,b,a);
        ln(vc,mat,x2,y1,z2,x2,y2,z2,r,g,b,a); ln(vc,mat,x1,y1,z2,x1,y2,z2,r,g,b,a);
    }

    private static void ln(VertexConsumer vc, Matrix4f mat,
                            float x1,float y1,float z1,float x2,float y2,float z2,
                            float r,float g,float b,float a) {
        float nx=x2-x1,ny=y2-y1,nz=z2-z1;
        float len=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);
        if (len==0) return;
        vc.vertex(mat,x1,y1,z1).color(r,g,b,a).normal(nx/len,ny/len,nz/len).next();
        vc.vertex(mat,x2,y2,z2).color(r,g,b,a).normal(nx/len,ny/len,nz/len).next();
    }
}
