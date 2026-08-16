package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.CustomHand;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class CustomHandMixin {

    @Inject(
            method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("HEAD")
    )
    private void onRenderItemHead(float tickDelta, MatrixStack matrices,
                                  VertexConsumerProvider.Immediate provider,
                                  ClientPlayerEntity player, int light, CallbackInfo ci) {
        if (!isEnabled()) return;
        matrices.push();
        matrices.scale(CustomHand.scaleX, CustomHand.scaleY, CustomHand.scaleZ);
        matrices.translate(CustomHand.offsetX, CustomHand.offsetY, CustomHand.offsetZ);
        if (CustomHand.rotationX != 0f) {
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(CustomHand.rotationX));
        }
        if (CustomHand.rotationY != 0f) {
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(CustomHand.rotationY));
        }
    }

    @Inject(
            method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("RETURN")
    )
    private void onRenderItemReturn(float tickDelta, MatrixStack matrices,
                                    VertexConsumerProvider.Immediate provider,
                                    ClientPlayerEntity player, int light, CallbackInfo ci) {
        if (!isEnabled()) return;
        matrices.pop();
    }

    private boolean isEnabled() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return false;
        for (var m : mm.getModules()) {
            if (m instanceof CustomHand && m.isEnabled()) return true;
        }
        return false;
    }
}
