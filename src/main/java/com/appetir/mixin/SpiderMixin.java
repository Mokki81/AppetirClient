package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.Spider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class SpiderMixin {

    @Inject(method = "isClimbing", at = @At("RETURN"), cancellable = true)
    private void onIsClimbing(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null||self!=mc.player||!isEnabled()) return;
        if (mc.options.keyForward.isPressed() && self.horizontalCollision)
            cir.setReturnValue(true);
    }

    private boolean isEnabled() {
        ModuleManager mm=ModuleManager.getInstance();
        if (mm==null) return false;
        return mm.getModules().stream().filter(m->m instanceof Spider).anyMatch(m->m.isEnabled());
    }
}
