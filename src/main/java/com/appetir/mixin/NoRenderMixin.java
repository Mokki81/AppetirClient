package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoRender;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class NoRenderMixin {

    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType,
                                   float viewDistance, boolean thickFog, CallbackInfo ci) {
        NoRender mod = getMod();
        if (mod != null && mod.noFog()) {
            ci.cancel();
        }
    }

    private static NoRender getMod() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return null;
        for (var m : mm.getModules()) {
            if (m instanceof NoRender) return (NoRender) m;
        }
        return null;
    }
}
