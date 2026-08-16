package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class TapeMouse extends Module {

    private int previousCursorMode = GLFW.GLFW_CURSOR_NORMAL;
    private boolean modeSaved;

    public TapeMouse() {
        super("TapeMouse", "Закрепление мыши", Category.MISC);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getWindow() == null) return;
        long handle = mc.getWindow().getHandle();
        previousCursorMode = GLFW.glfwGetInputMode(handle, GLFW.GLFW_CURSOR);
        modeSaved = true;
        GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getWindow() == null || !modeSaved) return;
        long handle = mc.getWindow().getHandle();
        // If a screen is open, prefer NORMAL; else restore previous
        int restore = mc.currentScreen != null
                ? GLFW.GLFW_CURSOR_NORMAL
                : previousCursorMode;
        GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, restore);
        modeSaved = false;
    }
}
