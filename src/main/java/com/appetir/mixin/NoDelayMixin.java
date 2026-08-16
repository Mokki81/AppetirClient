package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoDelay;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Real NoDelay: zero block-breaking cooldown each tick.
 * (Previously incorrectly modified reach distance.)
 */
@Mixin(ClientPlayerInteractionManager.class)
public class NoDelayMixin {

    @Shadow private int blockBreakingCooldown;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!isEnabled()) return;
        this.blockBreakingCooldown = 0;
    }

    private boolean isEnabled() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return false;
        for (var m : mm.getModules()) {
            if (m instanceof NoDelay && m.isEnabled()) return true;
        }
        return false;
    }
}
