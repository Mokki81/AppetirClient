package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoSlow;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class NoSlowMixin {

    /**
     * After vanilla applies item-use slowdown (multiplies movement by 0.2),
     * restore full movement if NoSlow is enabled.
     */
    @Inject(method = "tickMovement", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z",
            shift = At.Shift.AFTER
    ))
    private void onTickMovement(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (!isNoSlowItems()) return;
        if (!player.isUsingItem()) return;

        Input input = player.input;
        // Vanilla multiplies by 0.2 while using item — restore
        input.movementSideways /= 0.2f;
        input.movementForward /= 0.2f;
    }

    private boolean isNoSlowItems() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return false;
        for (var m : mm.getModules()) {
            if (m instanceof NoSlow && m.isEnabled()) {
                return ((NoSlow) m).items();
            }
        }
        return false;
    }
}
