package com.appetir.mixin;

import com.appetir.friends.FriendManager;
import com.appetir.gui.ThemeManager;
import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.*;
import com.appetir.util.RenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.math.Matrix4f;
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

        ESP esp = null;
        boolean arrowsOn = false;
        boolean projOn = false;
        boolean blockOn = false;

        for (var m : mm.getModules()) {
            if (m instanceof ESP && m.isEnabled()) esp = (ESP) m;
            if (m instanceof Arrows && m.isEnabled()) arrowsOn = true;
            if (m instanceof Projectiles && m.isEnabled()) projOn = true;
            if (m instanceof BlockESP && m.isEnabled()) blockOn = true;
        }

        if (esp == null && !arrowsOn && !projOn && !blockOn) return;

        VertexConsumerProvider.Immediate provider = mc.getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        int accent = ThemeManager.getAccentColor();
        int friendColor = 0xBB55FF88;
        int enemyColor = (0xBB << 24) | (accent & 0x00FFFFFF);
        int mobColor = 0xBBFF4444;
        int projColor = 0xBBFFAA00;

        double maxRange = esp != null ? esp.getRange() : 64;
        double maxRangeSq = maxRange * maxRange;

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (mc.player.squaredDistanceTo(e) > maxRangeSq) continue;

            if (esp != null) {
                if (e instanceof PlayerEntity) {
                    if (!esp.showPlayers()) continue;
                    boolean isFriend = FriendManager.getInstance() != null
                            && FriendManager.getInstance().isFriend((PlayerEntity) e);
                    if (isFriend && !esp.showFriends()) continue;
                    int c = isFriend ? friendColor : enemyColor;
                    RenderUtil.drawEntityBox(matrices, provider, e, tickDelta, c);
                } else if (e instanceof HostileEntity && esp.showMobs()) {
                    RenderUtil.drawEntityBox(matrices, provider, e, tickDelta, mobColor);
                }
            }

            if (arrowsOn && e instanceof PlayerEntity) {
                RenderUtil.drawLine(matrices, provider, e.getX(), e.getEyeY(), e.getZ(), 0xBBFFFF00);
            }

            if (projOn && (e instanceof ArrowEntity || e instanceof SnowballEntity || e instanceof EnderPearlEntity)) {
                RenderUtil.drawEntityBox(matrices, provider, e, tickDelta, projColor);
            }
        }

        if (blockOn) {
            for (net.minecraft.util.math.BlockPos pos : BlockESP.interestingBlocks) {
                RenderUtil.drawBlockBox(matrices, provider, pos.getX(), pos.getY(), pos.getZ(), 0xBBFFAA00);
            }
        }

        provider.draw(RenderLayer.getLines());
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }
}
