package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.HitBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class HitBoxMixin {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void expandBoundingBox(CallbackInfoReturnable<Box> cir) {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;

        // Найти модуль HitBox
        boolean hitboxEnabled = mm.getModules().stream()
            .filter(m -> m instanceof HitBox)
            .anyMatch(m -> m.isEnabled());

        if (!hitboxEnabled) return;

        Entity self = (Entity)(Object)this;
        // Не расширяем хитбокс игрока
        if (self instanceof PlayerEntity) return;

        float exp = HitBox.expansion;
        Box original = cir.getReturnValue();
        cir.setReturnValue(original.expand(exp));
    }
}
