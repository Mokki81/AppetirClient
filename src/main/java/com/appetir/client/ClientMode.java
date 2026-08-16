package com.appetir.client;

import com.appetir.config.ConfigManager;
import com.appetir.modules.Module;
import com.appetir.modules.ModuleManager;
import com.appetir.util.NotificationManager;

/**
 * Clean = looks like a performance / QOL client (combat & movement cheats hidden + forced off).
 * Full  = all modules visible.
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

    private static Mode current = Mode.FULL;

    private ClientMode() {}

    public static Mode get() {
        return current;
    }

    public static boolean isClean() {
        return current == Mode.CLEAN;
    }

    public static boolean isFull() {
        return current == Mode.FULL;
    }

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

    /** Categories hidden in Clean mode */
    public static boolean isCategoryVisible(Module.Category cat) {
        if (isFull()) return true;
        return cat == Module.Category.RENDER
                || cat == Module.Category.MISC
                || cat == Module.Category.WORLD;
    }

    /** Modules that stay allowed in Clean (QOL / visual / utility). */
    public static boolean isModuleAllowed(Module mod) {
        if (isFull()) return true;
        if (mod.getCategory() == Module.Category.COMBAT) return false;
        if (mod.getCategory() == Module.Category.MOVEMENT) return false;

        // Misc exceptions that look "cheat-y"
        String n = mod.getName();
        if (n.equalsIgnoreCase("FreeCamera")) return false;
        if (n.equalsIgnoreCase("TapeMouse")) return false;
        if (n.equalsIgnoreCase("AirStuck")) return false;

        return true;
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
        return isClean() ? "Appetir" : "Appetir";
    }

    public static String brandSubtitle() {
        return isClean() ? "Performance" : ("v" + com.appetir.AppetirClient.VERSION);
    }
}
