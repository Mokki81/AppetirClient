package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.HitBox;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Expand only living targets (not items/projectiles/boats/XP).
 * Never expand the local player.
 */
@Mixin(Entity.class)
public class HitBoxMixin {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void expandBoundingBox(CallbackInfoReturnable<Box> cir) {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;

        boolean hitboxEnabled = false;
        for (var m : mm.getModules()) {
            if (m instanceof HitBox && m.isEnabled()) {
                hitboxEnabled = true;
                break;
            }
        }
        if (!hitboxEnabled) return;

        Entity self = (Entity) (Object) this;
        if (!(self instanceof LivingEntity)) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && self == mc.player) return;

        float exp = HitBox.expansion;
        if (exp <= 0f) return;

        Box original = cir.getReturnValue();
        if (original == null) return;
        cir.setReturnValue(original.expand(exp));
    }
}
