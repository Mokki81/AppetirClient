package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ClientTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient mc = (MinecraftClient) (Object) this;
        // Only tick modules when actually in a world with a player
        if (mc.player == null || mc.world == null) return;

        ModuleManager mm = ModuleManager.getInstance();
        if (mm != null) mm.onTick();
    }
}
