package com.appetir.mixin;

import com.appetir.AppetirClient;
import com.appetir.gui.ThemeManager;
import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.modules.impl.Keystrokes;
import com.appetir.util.KeystrokesState;
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

        // Watermark
        String name = AppetirClient.NAME;
        String ver = " v" + AppetirClient.VERSION;
        mc.textRenderer.drawWithShadow(matrices, name, 5, 5, accent);
        mc.textRenderer.drawWithShadow(matrices, ver, 5 + mc.textRenderer.getWidth(name), 5, 0xFF888899);
        int uw = mc.textRenderer.getWidth(name + ver);
        fill(matrices, 5, 15, 5 + uw, 16, accentDim);

        String fps = mc.fpsDebugString.split(" ")[0] + " fps";
        mc.textRenderer.drawWithShadow(matrices, fps, 5, 18, 0xFF777788);

        // Arraylist
        List<Module> enabled = mm.getEnabled().stream()
                .filter(m -> !(m instanceof Keystrokes))
                .sorted(Comparator.comparingInt((Module m) -> -mc.textRenderer.getWidth(m.getName())))
                .collect(Collectors.toList());

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        int y = 4;
        int i = 0;

        for (Module mod : enabled) {
            String text = mod.getName();
            int width = mc.textRenderer.getWidth(text);
            int x = screenW - width - 8;

            fill(matrices, x - 5, y - 1, screenW - 1, y + 11, 0x99000000);
            float t = enabled.size() <= 1 ? 0f : (float) i / (enabled.size() - 1);
            int barColor = ThemeManager.lerpColor(accent, ThemeManager.secondary(), t * 0.6f);
            fill(matrices, screenW - 2, y - 1, screenW, y + 11, barColor);
            fill(matrices, x - 5, y - 1, x - 4, y + 11, ThemeManager.withAlpha(barColor, 0.7f));
            mc.textRenderer.drawWithShadow(matrices, text, x, y + 1, 0xFFFFFFFF);
            y += 12;
            i++;
        }

        // Keystrokes
        Module ksMod = mm.getByName("Keystrokes");
        if (ksMod instanceof Keystrokes && ksMod.isEnabled()) {
            renderKeystrokes(matrices, mc, (Keystrokes) ksMod, accent, screenH);
        }
    }

    private void renderKeystrokes(MatrixStack m, MinecraftClient mc, Keystrokes ks, int accent, int screenH) {
        int size = ks.getSize();
        int gap = 3;
        int baseX = ks.getX();
        int baseY = screenH - ks.getY() - size * 3 - gap * 2 - (ks.showSpace() ? size + gap : 0) - (ks.showMouse() ? size + gap : 0);

        // W
        drawKey(m, mc, baseX + size + gap, baseY, size, "W", KeystrokesState.w, accent);
        // A S D
        drawKey(m, mc, baseX, baseY + size + gap, size, "A", KeystrokesState.a, accent);
        drawKey(m, mc, baseX + size + gap, baseY + size + gap, size, "S", KeystrokesState.s, accent);
        drawKey(m, mc, baseX + (size + gap) * 2, baseY + size + gap, size, "D", KeystrokesState.d, accent);

        int row = baseY + (size + gap) * 2;

        if (ks.showSpace()) {
            int spaceW = size * 3 + gap * 2;
            drawKey(m, mc, baseX, row, spaceW, size, "SPACE", KeystrokesState.space, accent);
            row += size + gap;
        }

        if (ks.showMouse()) {
            int half = (size * 3 + gap * 2 - gap) / 2;
            drawKey(m, mc, baseX, row, half, size, "LMB", KeystrokesState.lmb, accent);
            drawKey(m, mc, baseX + half + gap, row, half, size, "RMB", KeystrokesState.rmb, accent);
        }
    }

    private void drawKey(MatrixStack m, MinecraftClient mc, int x, int y, int s, String label, boolean pressed, int accent) {
        drawKey(m, mc, x, y, s, s, label, pressed, accent);
    }

    private void drawKey(MatrixStack m, MinecraftClient mc, int x, int y, int w, int h, String label, boolean pressed, int accent) {
        if (pressed) {
            fill(m, x, y, x + w, y + h, ThemeManager.withAlpha(accent, 0.85f));
            fill(m, x, y, x + w, y + 1, 0x44FFFFFF);
        } else {
            fill(m, x, y, x + w, y + h, 0xAA0A0A14);
            fill(m, x, y, x + w, y + 1, 0x22FFFFFF);
        }
        // border
        int border = pressed ? accent : 0x44FFFFFF;
        fill(m, x, y, x + w, y + 1, border);
        fill(m, x, y + h - 1, x + w, y + h, border);
        fill(m, x, y, x + 1, y + h, border);
        fill(m, x + w - 1, y, x + w, y + h, border);

        int tw = mc.textRenderer.getWidth(label);
        int tx = x + (w - tw) / 2;
        int ty = y + (h - 8) / 2;
        mc.textRenderer.drawWithShadow(m, label, tx, ty, pressed ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    private void fill(MatrixStack m, int x1, int y1, int x2, int y2, int color) {
        net.minecraft.client.gui.DrawableHelper.fill(m, x1, y1, x2, y2, color);
    }
}
