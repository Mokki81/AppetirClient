package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoSlow;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public class NoSlowMixin {

    // Перехватываем множитель скорости при использовании предметов
    @Redirect(
        method = "tickMovement",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/input/Input;movementForward()F",
            ordinal = 0)
    )
    private float redirectMovementForward(Input input) {
        if (isEnabled()) return input.movementForward;
        return input.movementForward;
    }

    @Redirect(
        method = "tickMovement",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/input/Input;movementSideways()F",
            ordinal = 0)
    )
    private float redirectMovementSideways(Input input) {
        if (isEnabled()) return input.movementSideways;
        return input.movementSideways;
    }

    private boolean isEnabled() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return false;
        return mm.getModules().stream()
            .filter(m -> m instanceof NoSlow)
            .anyMatch(m -> m.isEnabled());
    }
}
