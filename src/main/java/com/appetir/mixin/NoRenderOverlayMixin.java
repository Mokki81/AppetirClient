package com.appetir.mixin;

import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class NoRenderOverlayMixin {

    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void cancelFire(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        NoRender mod = getMod();
        if (mod != null && mod.noFire()) ci.cancel();
    }

    private static NoRender getMod() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return null;
        for (Module m : mm.getModules()) {
            if (m instanceof NoRender) return (NoRender) m;
        }
        return null;
    }
}
