package com.appetir.mixin;

import com.appetir.AppetirClient;
import com.appetir.gui.ThemeManager;
import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.util.NotificationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(InGameHud.class)
public class HudOverlay {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.debugEnabled) return;

        // Always render notifications
        NotificationManager.render(matrices);

        if (!AppetirClient.hudVisible) return;

        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;

        int accent = ThemeManager.getAccentColor();

        // Watermark
        String watermark = AppetirClient.NAME + " §7v" + AppetirClient.VERSION;
        mc.textRenderer.drawWithShadow(matrices, watermark, 4, 4, accent);

        String fps = mc.fpsDebugString.split(" ")[0] + " fps";
        mc.textRenderer.drawWithShadow(matrices, fps, 4, 15, 0xFFAAAAAA);

        // Arraylist
        List<Module> enabled = mm.getEnabled().stream()
                .sorted(Comparator.comparingInt((Module m) -> -mc.textRenderer.getWidth(m.getName())))
                .collect(Collectors.toList());

        int screenW = mc.getWindow().getScaledWidth();
        int y = 4;

        for (Module mod : enabled) {
            String text = mod.getName();
            int width = mc.textRenderer.getWidth(text);
            int x = screenW - width - 6;

            fill(matrices, x - 3, y - 1, screenW - 2, y + 10, 0x66000000);
            fill(matrices, screenW - 2, y - 1, screenW, y + 10, accent);
            mc.textRenderer.drawWithShadow(matrices, text, x, y, 0xFFFFFFFF);
            y += 11;
        }
    }

    private void fill(MatrixStack m, int x1, int y1, int x2, int y2, int color) {
        net.minecraft.client.gui.DrawableHelper.fill(m, x1, y1, x2, y2, color);
    }
}
