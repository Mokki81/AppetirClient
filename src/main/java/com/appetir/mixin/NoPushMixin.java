package com.appetir.mixin;

import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoPush;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class NoPushMixin {

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void onPush(Entity entity, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || self != mc.player) return;
        if (!(entity instanceof PlayerEntity)) return;

        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;
        for (Module m : mm.getModules()) {
            if (m instanceof NoPush && m.isEnabled()) {
                ci.cancel();
                return;
            }
        }
    }
}
