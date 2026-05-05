package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoRender;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class NoRenderMixin {

    @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType,
                                   float viewDistance, boolean thickFog, CallbackInfo ci) {
        if (!isEnabled()) return;
        if (NoRender.noFog) ci.cancel();
    }

    private static boolean isEnabled() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return false;
        return mm.getModules().stream()
            .filter(m -> m instanceof NoRender)
            .anyMatch(m -> m.isEnabled());
    }
}
