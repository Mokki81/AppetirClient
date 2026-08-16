package com.appetir.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import org.lwjgl.glfw.GLFW;

/**
 * Soft ownership of a KeyBinding: only clear if we pressed it and the physical
 * input is not still held (avoids releasing the user's real RMB/keys).
 */
public final class KeyOwnership {

    private KeyOwnership() {}

    public static boolean pressUseIfFree(MinecraftClient mc) {
        if (mc == null || mc.options == null) return false;
        KeyBinding use = mc.options.keyUse;
        if (use.isPressed()) {
            // Already down (user or another owner) — don't claim
            return false;
        }
        use.setPressed(true);
        return true;
    }

    public static void releaseUseIfOwned(MinecraftClient mc, boolean owned) {
        if (!owned || mc == null || mc.options == null) return;
        if (isPhysicalRightMouseDown(mc)) {
            // User still holding RMB — leave keyUse pressed
            return;
        }
        mc.options.keyUse.setPressed(false);
    }

    public static boolean isPhysicalRightMouseDown(MinecraftClient mc) {
        try {
            long handle = mc.getWindow().getHandle();
            return GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        } catch (Exception e) {
            return false;
        }
    }
}
