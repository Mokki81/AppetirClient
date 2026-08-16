package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoSlow;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Undo item-use movement slowdown after the full tickMovement pass.
 * Clamp after scale so we never produce >1.0 input spikes.
 */
@Mixin(ClientPlayerEntity.class)
public class NoSlowMixin {

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void onTickMovementTail(CallbackInfo ci) {
        if (!isNoSlowItems()) return;

        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (!player.isUsingItem()) return;

        Input input = player.input;
        // Vanilla applies ~0.2 while using an item; restore and clamp to legal range.
        input.movementSideways = MathHelper.clamp(input.movementSideways * 5.0f, -1.0f, 1.0f);
        input.movementForward = MathHelper.clamp(input.movementForward * 5.0f, -1.0f, 1.0f);
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
