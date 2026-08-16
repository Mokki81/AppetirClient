package com.appetir.gui;

import com.appetir.config.ConfigManager;

/**
 * Themes + ClickGUI layout styles (Appetir / Catlean-inspired / Compact).
 */
public class ThemeManager {

    public enum Theme {
        LIQUID    ("Liquid",     0xFF4DA3FF, 0xFF7B68EE),
        AURORA    ("Aurora",     0xFF00E5A8, 0xFF4DA3FF),
        BLOODY    ("Bloody",     0xFFFF2D55, 0xFFFF6B6B),
        AMETHYST  ("Amethyst",   0xFFB388FF, 0xFFE040FB),
        SUNSET    ("Sunset",     0xFFFF6B35, 0xFFFFB347),
        TOXIC     ("Toxic",      0xFF39FF14, 0xFF00C853),
        ICE       ("Ice",        0xFF80D8FF, 0xFF2979FF),
        GOLD      ("Gold",       0xFFFFD700, 0xFFFFA000),
        ROSE      ("Rose",       0xFFFF4D6D, 0xFFFF8FAB),
        GRADIENT  ("Gradient",   0xFF4DA3FF, 0xFFE040FB),
        CATLEAN   ("CatLean",    0xFF7C5CFF, 0xFF00E5C0); // purple→cyan inspired

        public final String name;
        public final int colorPrimary;
        public final int colorSecondary;

        Theme(String name, int primary, int secondary) {
            this.name = name;
            this.colorPrimary = primary;
            this.colorSecondary = secondary;
        }
    }

    /** ClickGUI layout style */
    public enum GuiStyle {
        APPETIR("Appetir", "Sidebar + panel"),
        CATLEAN("CatLean", "Wide cards, soft glow"),
        COMPACT("Compact", "Dense list");

        public final String name;
        public final String desc;
        GuiStyle(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
    }

    private static Theme current = Theme.AURORA;
    private static GuiStyle guiStyle = GuiStyle.APPETIR;

    public static Theme getCurrent() { return current; }
    public static GuiStyle getGuiStyle() { return guiStyle; }

    public static void setCurrent(Theme t) {
        if (t != null && t != current) {
            current = t;
            ConfigManager cm = ConfigManager.getInstance();
            if (cm != null) cm.saveQuiet();
        }
    }

    public static void setGuiStyle(GuiStyle s) {
        if (s != null && s != guiStyle) {
            guiStyle = s;
            ConfigManager cm = ConfigManager.getInstance();
            if (cm != null) cm.saveQuiet();
        }
    }

    public static void cycleGuiStyle() {
        GuiStyle[] all = GuiStyle.values();
        int i = (guiStyle.ordinal() + 1) % all.length;
        setGuiStyle(all[i]);
    }

    public static int primary() { return current.colorPrimary; }
    public static int secondary() { return current.colorSecondary; }

    public static int getAccentColor() {
        long time = System.currentTimeMillis();
        float t = (time % 3500L) / 3500.0f;
        float wave = t < 0.5f ? t * 2f : 2f - t * 2f;
        return lerpColor(current.colorPrimary, current.colorSecondary, wave * 0.55f);
    }

    public static int getAccentColor(float alpha) {
        int c = getAccentColor();
        int a = Math.max(0, Math.min(255, (int) (alpha * 255)));
        return (c & 0x00FFFFFF) | (a << 24);
    }

    public static int withAlpha(int color, float alpha) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255)));
        return (color & 0x00FFFFFF) | (a << 24);
    }

    public static int lerpColor(int c1, int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (((c1 >> 16) & 0xFF) * (1 - t) + ((c2 >> 16) & 0xFF) * t);
        int g = (int) (((c1 >>  8) & 0xFF) * (1 - t) + ((c2 >>  8) & 0xFF) * t);
        int b = (int) (((c1      ) & 0xFF) * (1 - t) + ((c2      ) & 0xFF) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
