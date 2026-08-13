package com.appetir.gui;

import com.appetir.config.ConfigManager;

/**
 * Theme system with animated gradient support.
 */
public class ThemeManager {

    public enum Theme {
        LIQUID    ("Liquid",     0xFF4A90D9, 0xFF7B68EE),
        BLOODY    ("Bloody",     0xFFCC2222, 0xFFFF6666),
        AMETHYST  ("Amethyst",   0xFF9B59B6, 0xFFD7BDE2),
        BANANA    ("Banana",     0xFFB8A000, 0xFFFFE066),
        RASPBERRY ("Raspberry",  0xFF8B1A4A, 0xFFE91E8C),
        TOXIC     ("Toxic",      0xFF2E7D32, 0xFF76FF03),
        ORANGE    ("Orange",     0xFFBF6000, 0xFFFF9800),
        CYAN      ("Cyan",       0xFF00BCD4, 0xFF00E5FF),
        GRADIENT  ("Gradient",   0xFF2196F3, 0xFF9C27B0),
        CUSTOM    ("Custom",     0xFF5B8CFF, 0xFF00E5FF);

        public final String name;
        public final int colorPrimary;
        public final int colorSecondary;

        Theme(String name, int primary, int secondary) {
            this.name = name;
            this.colorPrimary = primary;
            this.colorSecondary = secondary;
        }
    }

    private static Theme current = Theme.LIQUID;

    public static Theme getCurrent() {
        return current;
    }

    public static void setCurrent(Theme t) {
        if (t != null && t != current) {
            current = t;
            ConfigManager cm = ConfigManager.getInstance();
            if (cm != null) cm.saveQuiet();
        }
    }

    public static int primary() {
        return current.colorPrimary;
    }

    public static int secondary() {
        return current.colorSecondary;
    }

    public static int getAccentColor() {
        if (current == Theme.GRADIENT) {
            long time = System.currentTimeMillis();
            float t = (time % 4000L) / 4000.0f;
            float wave = t < 0.5f ? t * 2f : 2f - t * 2f;
            return lerpColor(Theme.GRADIENT.colorPrimary, Theme.GRADIENT.colorSecondary, wave);
        }
        return current.colorPrimary;
    }

    public static int getAccentColor(float alpha) {
        int c = getAccentColor();
        int a = Math.max(0, Math.min(255, (int) (alpha * 255)));
        return (c & 0x00FFFFFF) | (a << 24);
    }

    public static int lerpColor(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (((c1 >> 16) & 0xFF) * (1 - t) + ((c2 >> 16) & 0xFF) * t);
        int g = (int) (((c1 >>  8) & 0xFF) * (1 - t) + ((c2 >>  8) & 0xFF) * t);
        int b = (int) (((c1      ) & 0xFF) * (1 - t) + ((c2      ) & 0xFF) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
