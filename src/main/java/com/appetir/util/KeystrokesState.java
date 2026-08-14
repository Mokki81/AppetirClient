package com.appetir.util;

import org.lwjgl.glfw.GLFW;

/**
 * Tracks pressed keys for Keystrokes HUD.
 */
public final class KeystrokesState {

    public static boolean w, a, s, d, space, shift, lmb, rmb;

    private KeystrokesState() {}

    public static void onKey(int key, int action) {
        boolean down = action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT;
        boolean up = action == GLFW.GLFW_RELEASE;

        if (!down && !up) return;
        boolean state = down;

        switch (key) {
            case GLFW.GLFW_KEY_W: w = state; break;
            case GLFW.GLFW_KEY_A: a = state; break;
            case GLFW.GLFW_KEY_S: s = state; break;
            case GLFW.GLFW_KEY_D: d = state; break;
            case GLFW.GLFW_KEY_SPACE: space = state; break;
            case GLFW.GLFW_KEY_LEFT_SHIFT:
            case GLFW.GLFW_KEY_RIGHT_SHIFT: shift = state; break;
            default: break;
        }
    }

    public static void onMouse(int button, int action) {
        boolean state = action == GLFW.GLFW_PRESS;
        if (action != GLFW.GLFW_PRESS && action != GLFW.GLFW_RELEASE) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) lmb = state;
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) rmb = state;
    }
}
