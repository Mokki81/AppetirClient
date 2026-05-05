package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.GlassHands;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class GlassHandsMixin {

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    private void onRenderFirstPerson(Object player, float tickDelta, float pitch,
                                     Object hand, float swingProgress,
                                     Object item, float equipProgress,
                                     MatrixStack matrices, Object provider,
                                     int light, CallbackInfo ci) {
        if (isEnabled()) ci.cancel();
    }

    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
            at = @At("HEAD"), cancellable = true)
    private void onRenderArm(float tickDelta, MatrixStack matrices, Object provider,
                              Object player, int light, CallbackInfo ci) {
        if (isEnabled()) ci.cancel();
    }

    private boolean isEnabled() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return false;
        return mm.getModules().stream()
            .filter(m -> m instanceof GlassHands)
            .anyMatch(m -> m.isEnabled());
    }
}
