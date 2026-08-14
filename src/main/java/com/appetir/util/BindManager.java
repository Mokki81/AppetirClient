package com.appetir.util;

import com.appetir.modules.Module;
import org.lwjgl.glfw.GLFW;

/**
 * Global keybind listening state (Neverlose-style).
 * When listening != null, next key press assigns the bind.
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

    /**
     * @return true if the key was consumed by bind system
     */
    public static boolean onKey(int key) {
        if (listening == null) return false;

        // ESC — cancel
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            listening = null;
            return true;
        }

        // DEL / BACKSPACE — unbind
        if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
            listening.setKey(-1);
            NotificationManager.push(listening.getName(), "Unbound");
            listening = null;
            return true;
        }

        // Don't bind reserved keys
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT
                || key == GLFW.GLFW_KEY_RIGHT_CONTROL
                || key == GLFW.GLFW_KEY_RIGHT_ALT) {
            return true;
        }

        listening.setKey(key);
        NotificationManager.push(listening.getName(), "Bound: " + KeyUtil.getKeyName(key));
        listening = null;
        return true;
    }
}
