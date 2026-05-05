package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.AspectRatio;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Window.class)
public class AspectRatioMixin {

    @Inject(method = "getFramebufferWidth", at = @At("RETURN"), cancellable = true)
    private void onGetWidth(CallbackInfoReturnable<Integer> cir) {
        if (!isEnabled()) return;
        int h = ((Window)(Object)this).getFramebufferHeight();
        cir.setReturnValue((int)(h * AspectRatio.ratio));
    }

    private boolean isEnabled() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return false;
        return mm.getModules().stream()
            .filter(m -> m instanceof AspectRatio)
            .anyMatch(m -> m.isEnabled());
    }
}
