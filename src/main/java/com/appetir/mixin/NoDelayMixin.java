package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoDelay;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class NoDelayMixin {

    @Inject(method = "getReachDistance", at = @At("RETURN"), cancellable = true)
    private void onGetReach(CallbackInfoReturnable<Float> cir) {
        // NoDelay также слегка увеличивает дальность взаимодействия
        if (isEnabled()) cir.setReturnValue(cir.getReturnValue() + 0.5f);
    }

    private boolean isEnabled() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return false;
        return mm.getModules().stream()
            .filter(m -> m instanceof NoDelay)
            .anyMatch(m -> m.isEnabled());
    }
}
