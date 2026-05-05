package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.CustomHand;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class CustomHandMixin {

    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("HEAD"))
    private void onRenderItem(float tickDelta, MatrixStack matrices, Object provider, Object player, int light, CallbackInfo ci) {
        if (!isEnabled()) return;
        matrices.scale(CustomHand.scaleX, CustomHand.scaleY, CustomHand.scaleZ);
        matrices.translate(CustomHand.offsetX, CustomHand.offsetY, CustomHand.offsetZ);
    }

    private boolean isEnabled() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return false;
        return mm.getModules().stream()
            .filter(m -> m instanceof CustomHand)
            .anyMatch(m -> m.isEnabled());
    }
}
