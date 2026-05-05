package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.*;
import com.appetir.util.RenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, float tickDelta, long limitTime,
                          boolean renderBlockOutline, Camera camera,
                          GameRenderer gameRenderer, LightmapTextureManager lightmap,
                          Matrix4f projMatrix, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;

        boolean espOn  = isOn(mm, ESP.class);
        boolean arrows = isOn(mm, Arrows.class);
        boolean proj   = isOn(mm, Projectiles.class);
        boolean blockE = isOn(mm, BlockESP.class);

        if (!espOn && !arrows && !proj && !blockE) return;

        VertexConsumerProvider.Immediate provider = mc.getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;

            if (espOn && e instanceof PlayerEntity) {
                int c = (0xBB << 24) | (com.appetir.gui.ThemeManager.getAccentColor() & 0x00FFFFFF);
                RenderUtil.drawEntityBox(matrices, provider, e, tickDelta, c);
            }
            if (arrows && e instanceof PlayerEntity) {
                RenderUtil.drawLine(matrices, provider, e.getX(), e.getEyeY(), e.getZ(), 0xBBFFFF00);
            }
            if (proj && (e instanceof ArrowEntity || e instanceof SnowballEntity)) {
                RenderUtil.drawEntityBox(matrices, provider, e, tickDelta, 0xBBFF6600);
            }
        }

        if (blockE) {
            for (net.minecraft.util.math.BlockPos pos : BlockESP.interestingBlocks)
                RenderUtil.drawBlockBox(matrices, provider, pos.getX(), pos.getY(), pos.getZ(), 0xBBFFAA00);
        }

        provider.draw(RenderLayer.getLines());
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

    private boolean isOn(ModuleManager mm, Class<?> c) {
        return mm.getModules().stream().filter(c::isInstance).anyMatch(m -> m.isEnabled());
    }
}
