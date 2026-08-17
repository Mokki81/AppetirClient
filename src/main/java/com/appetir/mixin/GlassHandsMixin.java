package com.appetir.mixin;

import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.GlassHands;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class GlassHandsMixin {

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void onRenderFirstPerson(AbstractClientPlayerEntity player, float tickDelta, float pitch,
                                     Hand hand, float swingProgress,
                                     ItemStack item, float equipProgress,
                                     MatrixStack matrices, VertexConsumerProvider provider,
                                     int light, CallbackInfo ci) {
        if (isEnabled()) ci.cancel();
    }

    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("HEAD"), cancellable = true)
    private void onRenderArm(float tickDelta, MatrixStack matrices,
                             VertexConsumerProvider.Immediate provider,
                             ClientPlayerEntity player, int light, CallbackInfo ci) {
        if (isEnabled()) ci.cancel();
    }

    private boolean isEnabled() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return false;
        for (Module m : mm.getModules()) {
            if (m instanceof GlassHands && m.isEnabled()) return true;
        }
        return false;
    }
}
