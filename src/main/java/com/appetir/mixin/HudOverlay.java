package com.appetir.mixin;

import com.appetir.AppetirClient;
import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InGameHud.class)
public class HudOverlay {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (!AppetirClient.hudVisible) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;

        // Заголовок клиента
        mc.textRenderer.drawWithShadow(matrices,
                AppetirClient.NAME + " " + AppetirClient.VERSION,
                4, 4, 0xFF55FFFF);

        // Список активных модулей справа
        List<Module> modules = mm.getModules();
        int y = 4;
        int screenW = mc.getWindow().getScaledWidth();
        for (Module mod : modules) {
            if (!mod.isEnabled()) continue;
            String text = mod.getName();
            int x = screenW - mc.textRenderer.getWidth(text) - 4;
            mc.textRenderer.drawWithShadow(matrices, text, x, y, 0xFFFFFF55);
            y += 10;
        }
    }
}
