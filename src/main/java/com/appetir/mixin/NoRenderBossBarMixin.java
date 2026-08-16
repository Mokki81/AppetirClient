package com.appetir.mixin;

import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.NoRender;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public class NoRenderBossBarMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cancelBossBar(MatrixStack matrices, CallbackInfo ci) {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;
        for (var m : mm.getModules()) {
            if (m instanceof NoRender && ((NoRender) m).noBossBar()) {
                ci.cancel();
                return;
            }
        }
    }
}
