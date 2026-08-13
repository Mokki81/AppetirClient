package com.appetir.util;

import com.appetir.gui.ThemeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * On-screen notifications (module toggle, etc.).
 */
public class NotificationManager {

    private static final List<Notification> notifications = new ArrayList<>();

    public static void push(String title, String message) {
        notifications.add(0, new Notification(title, message, System.currentTimeMillis()));
        if (notifications.size() > 8) {
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
        int y = mc.getWindow().getScaledHeight() / 2 - 40;
        int screenW = mc.getWindow().getScaledWidth();
        int accent = ThemeManager.getAccentColor();

        Iterator<Notification> it = notifications.iterator();
        while (it.hasNext()) {
            Notification n = it.next();
            long age = now - n.time;
            if (age > 2500) {
                it.remove();
                continue;
            }

            float alpha = age < 200 ? age / 200f : (age > 2200 ? (2500 - age) / 300f : 1f);
            alpha = Math.max(0f, Math.min(1f, alpha));

            int a = (int) (alpha * 220);
            int bg = (a << 24) | 0x000000;
            int textA = (int) (alpha * 255);

            String line1 = n.title;
            String line2 = n.message;
            int w = Math.max(mc.textRenderer.getWidth(line1), mc.textRenderer.getWidth(line2)) + 16;
            int x = screenW - w - 8;

            // background
            fill(matrices, x, y, x + w, y + 28, bg);
            // accent bar
            fill(matrices, x, y, x + 2, y + 28, (textA << 24) | (accent & 0x00FFFFFF));

            mc.textRenderer.drawWithShadow(matrices, line1, x + 8, y + 5, (textA << 24) | 0xFFFFFF);
            mc.textRenderer.drawWithShadow(matrices, line2, x + 8, y + 15,
                    (textA << 24) | (line2.equals("Enabled") ? 0x55FF55 : 0xFF5555));

            y += 32;
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
