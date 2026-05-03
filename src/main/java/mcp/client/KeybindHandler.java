package mcp.client;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class KeybindHandler {
    private static boolean wasPressed = false;
    private static boolean hudVisible = false;

    public static void onKeyInput(int key, boolean pressed) {
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            if (pressed && !wasPressed) {
                hudVisible = !hudVisible;
            }
            wasPressed = pressed;
        }
    }

    public static boolean isHudVisible() {
        return hudVisible;
    }
}