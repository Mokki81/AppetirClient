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
        Arrows arrows = null;
        Projectiles projectiles = null;
        boolean blockOn = false;

        for (var m : mm.getModules()) {
            if (m instanceof ESP && m.isEnabled()) esp = (ESP) m;
            if (m instanceof Arrows && m.isEnabled()) arrows = (Arrows) m;
            if (m instanceof Projectiles && m.isEnabled()) projectiles = (Projectiles) m;
            if (m instanceof BlockESP && m.isEnabled()) blockOn = true;
        }

        if (esp == null && arrows == null && projectiles == null && !blockOn) return;

        VertexConsumerProvider.Immediate provider = mc.getBufferBuilders().getEntityVertexConsumers();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        int accent = ThemeManager.getAccentColor();
        int friendColor = 0xBB55FF88;
        int enemyColor = (0xBB << 24) | (accent & 0x00FFFFFF);
        int mobColor = 0xBBFF4444;
        int projColor = 0xBBFFAA00;

        double espRangeSq = esp != null ? esp.getRange() * esp.getRange() : 0;
        double arrowsRangeSq = arrows != null ? arrows.getRange() * arrows.getRange() : 0;

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            double distSq = mc.player.squaredDistanceTo(e);

            if (esp != null && distSq <= espRangeSq) {
                if (e instanceof PlayerEntity) {
                    if (esp.showPlayers()) {
                        boolean isFriend = FriendManager.getInstance() != null
                                && FriendManager.getInstance().isFriendReadOnly((PlayerEntity) e);
                        if (!(isFriend && !esp.showFriends())) {
                            int c = isFriend ? friendColor : enemyColor;
                            RenderUtil.drawEntityBox(matrices, provider, e, tickDelta, c);
                        }
                    }
                } else if (e instanceof HostileEntity && esp.showMobs()) {
                    RenderUtil.drawEntityBox(matrices, provider, e, tickDelta, mobColor);
                }
            }

            if (arrows != null && distSq <= arrowsRangeSq) {
                boolean ok = arrows.playersOnly() ? (e instanceof PlayerEntity) : true;
                if (ok && e instanceof PlayerEntity) {
                    RenderUtil.drawLine(matrices, provider, e.getX(), e.getEyeY(), e.getZ(), 0xBBFFFF00);
                }
            }

            if (projectiles != null) {
                boolean draw = false;
                if (e instanceof ArrowEntity && projectiles.showArrows()) draw = true;
                else if (e instanceof EnderPearlEntity && projectiles.showPearls()) draw = true;
                else if (e instanceof SnowballEntity && projectiles.showSnowballs()) draw = true;
                if (draw) {
                    RenderUtil.drawEntityBox(matrices, provider, e, tickDelta, projColor);
                }
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
