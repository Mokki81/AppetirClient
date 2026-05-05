package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoPush;
import net.minecraft.entity.Entity;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class NoPushMixin {

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void onPush(Entity entity, CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        if (self != MinecraftClient.getInstance().player) return;
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;
        boolean enabled = mm.getModules().stream()
            .filter(m -> m instanceof NoPush)
            .anyMatch(m -> m.isEnabled());
        if (enabled) ci.cancel();
    }
}
