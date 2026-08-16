package com.appetir.util;

import com.appetir.modules.Module;
import org.lwjgl.glfw.GLFW;

/**
 * Global keybind listening state (Neverlose-style).
 */
public final class BindManager {

    private static Module listening = null;

    private BindManager() {}

    public static boolean isListening() {
        return listening != null;
    }

    public static Module getListening() {
        return listening;
    }

    public static void startListening(Module module) {
        listening = module;
    }

    public static void cancel() {
        listening = null;
    }

    /** Keys used by the client itself — cannot be module binds. */
    public static boolean isReserved(int key) {
        return key == GLFW.GLFW_KEY_RIGHT_SHIFT
                || key == GLFW.GLFW_KEY_RIGHT_CONTROL
                || key == GLFW.GLFW_KEY_RIGHT_ALT
                || key == GLFW.GLFW_KEY_INSERT
                || key == GLFW.GLFW_KEY_ESCAPE;
    }

    public static boolean onKey(int key) {
        if (listening == null) return false;

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            listening = null;
            return true;
        }

        if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
            listening.setKey(-1);
            NotificationManager.push(listening.getName(), "Unbound");
            listening = null;
            return true;
        }

        if (isReserved(key)) {
            NotificationManager.push("Bind", "Key reserved by client");
            return true;
        }

        listening.setKey(key);
        NotificationManager.push(listening.getName(), "Bound: " + KeyUtil.getKeyName(key));
        listening = null;
        return true;
    }
}
