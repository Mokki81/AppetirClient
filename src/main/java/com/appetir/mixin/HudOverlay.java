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

        NotificationManager.render(matrices);

        if (!AppetirClient.hudVisible) return;

        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;

        int accent = ThemeManager.getAccentColor();
        int accentDim = ThemeManager.getAccentColor(0.45f);

        // ── Premium watermark ────────────────────────────────────
        String name = AppetirClient.NAME;
        String ver = " v" + AppetirClient.VERSION;
        mc.textRenderer.drawWithShadow(matrices, name, 5, 5, accent);
        mc.textRenderer.drawWithShadow(matrices, ver, 5 + mc.textRenderer.getWidth(name), 5, 0xFF888899);

        // thin accent underline under watermark
        int uw = mc.textRenderer.getWidth(name + ver);
        fill(matrices, 5, 15, 5 + uw, 16, accentDim);

        // FPS line
        String fps = mc.fpsDebugString.split(" ")[0] + " fps";
        mc.textRenderer.drawWithShadow(matrices, fps, 5, 18, 0xFF777788);

        // ── Arraylist ────────────────────────────────────────────
        List<Module> enabled = mm.getEnabled().stream()
                .sorted(Comparator.comparingInt((Module m) -> -mc.textRenderer.getWidth(m.getName())))
                .collect(Collectors.toList());

        int screenW = mc.getWindow().getScaledWidth();
        int y = 4;
        int i = 0;

        for (Module mod : enabled) {
            String text = mod.getName();
            int width = mc.textRenderer.getWidth(text);
            int x = screenW - width - 8;

            // soft background plate
            fill(matrices, x - 5, y - 1, screenW - 1, y + 11, 0x99000000);

            // gradient-ish right bar (accent)
            float t = enabled.size() <= 1 ? 0f : (float) i / (enabled.size() - 1);
            int barColor = ThemeManager.lerpColor(accent, ThemeManager.secondary(), t * 0.6f);
            fill(matrices, screenW - 2, y - 1, screenW, y + 11, barColor);

            // left micro accent
            fill(matrices, x - 5, y - 1, x - 4, y + 11, ThemeManager.withAlpha(barColor, 0.7f));

            mc.textRenderer.drawWithShadow(matrices, text, x, y + 1, 0xFFFFFFFF);
            y += 12;
            i++;
        }
    }

    private void fill(MatrixStack m, int x1, int y1, int x2, int y2, int color) {
        net.minecraft.client.gui.DrawableHelper.fill(m, x1, y1, x2, y2, color);
    }
}
