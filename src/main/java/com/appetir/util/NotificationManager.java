package com.appetir.util;

import com.appetir.gui.ThemeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Premium slide-in notifications.
 */
public class NotificationManager {

    private static final List<Notification> notifications = new ArrayList<>();
    private static final long DURATION = 2800;

    public static void push(String title, String message) {
        notifications.add(0, new Notification(title, message, System.currentTimeMillis()));
        while (notifications.size() > 6) {
            notifications.remove(notifications.size() - 1);
        }
    }

    public static void pushModule(String name, boolean enabled) {
        push(name, enabled ? "Enabled" : "Disabled");
    }

    public static void render(MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        long now = System.currentTimeMillis();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        int y = screenH / 2 - 50;
        int accent = ThemeManager.getAccentColor();

        Iterator<Notification> it = notifications.iterator();
        while (it.hasNext()) {
            Notification n = it.next();
            long age = now - n.time;
            if (age > DURATION) {
                it.remove();
                continue;
            }

            // ease in / out
            float alpha;
            float slide;
            if (age < 250) {
                float t = age / 250f;
                alpha = t;
                slide = 1f - t * t; // from right
            } else if (age > DURATION - 350) {
                float t = (DURATION - age) / 350f;
                alpha = t;
                slide = 0f;
            } else {
                alpha = 1f;
                slide = 0f;
            }

            int aBg = (int) (alpha * 200);
            int aText = (int) (alpha * 255);

            String line1 = n.title;
            String line2 = n.message;
            int w = Math.max(mc.textRenderer.getWidth(line1), mc.textRenderer.getWidth(line2)) + 20;
            w = Math.max(w, 100);
            int h = 32;
            int x = (int) (screenW - w - 10 + slide * 40);

            // shadow
            fill(matrices, x + 2, y + 2, x + w + 2, y + h + 2, (aBg / 3) << 24);
            // body
            fill(matrices, x, y, x + w, y + h, (aBg << 24));
            // left accent bar
            fill(matrices, x, y, x + 3, y + h, (aText << 24) | (accent & 0x00FFFFFF));
            // top subtle line
            fill(matrices, x + 3, y, x + w, y + 1, ThemeManager.withAlpha(accent, alpha * 0.35f));

            // progress bar at bottom
            float progress = 1f - (age / (float) DURATION);
            int progW = (int) ((w - 3) * progress);
            fill(matrices, x + 3, y + h - 2, x + 3 + progW, y + h, ThemeManager.withAlpha(accent, alpha * 0.8f));

            mc.textRenderer.drawWithShadow(matrices, line1, x + 10, y + 6, (aText << 24) | 0xFFFFFF);
            int msgColor = line2.equals("Enabled") ? 0x55FF88 : 0xFF6B6B;
            mc.textRenderer.drawWithShadow(matrices, line2, x + 10, y + 17, (aText << 24) | msgColor);

            y += h + 6;
        }
    }

    private static void fill(MatrixStack m, int x1, int y1, int x2, int y2, int color) {
        net.minecraft.client.gui.DrawableHelper.fill(m, x1, y1, x2, y2, color);
    }

    private static class Notification {
        final String title, message;
        final long time;
        Notification(String title, String message, long time) {
            this.title = title;
            this.message = message;
            this.time = time;
        }
    }
}
