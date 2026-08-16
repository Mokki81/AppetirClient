package com.appetir.client;

import com.appetir.config.ConfigManager;
import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.util.NotificationManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Clean = explicit QOL whitelist only (performance-mod look).
 * Full  = everything.
 */
public final class ClientMode {

    public enum Mode {
        CLEAN("Clean", "Performance / QOL look"),
        FULL("Full", "All features");

        public final String display;
        public final String desc;

        Mode(String display, String desc) {
            this.display = display;
            this.desc = desc;
        }
    }

    /** Explicit whitelist for Clean mode — everything else is blocked. */
    private static final Set<String> CLEAN_WHITELIST;

    static {
        Set<String> s = new HashSet<>();
        // Render / visual QOL
        s.add("fullbright");
        s.add("nightvision");
        s.add("hud");
        s.add("keystrokes");
        s.add("norender");
        s.add("itemphysic");
        s.add("particles");
        s.add("worldparticles");
        s.add("aspectratio");
        s.add("customhand");
        s.add("glasshands");
        s.add("cosmetics");
        s.add("customworld");
        s.add("nametags");
        s.add("shulkerviewer");
        // Misc QOL
        s.add("optimization");
        s.add("clientsounds");
        s.add("itemscroller");
        s.add("antiafk");
        s.add("autoaccept");
        CLEAN_WHITELIST = Collections.unmodifiableSet(s);
    }

    private static Mode current = Mode.FULL;

    private ClientMode() {}

    public static Mode get() { return current; }
    public static boolean isClean() { return current == Mode.CLEAN; }
    public static boolean isFull() { return current == Mode.FULL; }

    public static void set(Mode mode) {
        if (mode == null || mode == current) return;
        current = mode;

        if (mode == Mode.CLEAN) {
            forceDisableRestricted();
            NotificationManager.push("Client Mode", "Clean — performance look");
        } else {
            NotificationManager.push("Client Mode", "Full — all modules");
        }

        ConfigManager cm = ConfigManager.getInstance();
        if (cm != null) cm.markDirty();
    }

    public static void toggle() {
        set(isClean() ? Mode.FULL : Mode.CLEAN);
    }

    public static void setRaw(Mode mode) {
        if (mode != null) current = mode;
    }

    public static boolean isCategoryVisible(Module.Category cat) {
        if (isFull()) return true;
        // In Clean only show categories that can contain whitelist modules
        return cat == Module.Category.RENDER
                || cat == Module.Category.MISC
                || cat == Module.Category.WORLD;
    }

    public static boolean isModuleAllowed(Module mod) {
        if (isFull()) return true;
        return CLEAN_WHITELIST.contains(mod.getName().toLowerCase(Locale.ROOT));
    }

    private static void forceDisableRestricted() {
        ModuleManager mm = ModuleManager.getInstance();
        if (mm == null) return;
        for (Module m : mm.getModules()) {
            if (!isModuleAllowed(m) && m.isEnabled()) {
                m.setEnabled(false);
            }
        }
    }

    public static String brandName() {
        return "Appetir";
    }

    public static String brandSubtitle() {
        return isClean() ? "Performance" : ("v" + com.appetir.AppetirClient.VERSION);
    }
}
