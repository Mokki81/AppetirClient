package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NameTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class NameTagsMixin {

    @Inject(method = "render(Lnet/minecraft/entity/player/PlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL"))
    private void onRender(PlayerEntity player, float yaw, float tickDelta,
                          MatrixStack matrices, VertexConsumerProvider provider, int light, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || player == mc.player) return;

        NameTags mod = getMod();
        if (mod == null || !mod.isEnabled()) return;

        matrices.push();
        matrices.translate(0, player.getHeight() + 0.5, 0);
        matrices.multiply(mc.gameRenderer.getCamera().getRotation());
        float scale = 0.025f * mod.getScale();
        matrices.scale(-scale, -scale, scale);

        String name = player.getGameProfile().getName();
        String hp = mod.showHealth() ? String.format(" §c%.0f❤", player.getHealth()) : "";
        String dist = mod.showDistance() ? String.format(" §7%.0fm", mc.player.distanceTo(player)) : "";
        String arm = "";
        if (mod.showArmor()) {
            int armor = player.getArmor();
            arm = String.format(" §b%d⛨", armor);
        }
        String tag = name + hp + arm + dist;

        int tw = mc.textRenderer.getWidth(tag);
        mc.textRenderer.drawWithShadow(matrices, tag, -tw / 2f, -4, 0xFFFFFFFF);
        matrices.pop();
    }

    private NameTags getMod() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return null;
        for (var m : mm.getModules()) {
            if (m instanceof NameTags) return (NameTags) m;
        }
        return null;
    }
}
