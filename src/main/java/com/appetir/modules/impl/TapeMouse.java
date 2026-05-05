package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class TapeMouse extends Module {

    public TapeMouse() {
        super("TapeMouse", "Закрепление мыши", Category.MISC);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        GLFW.glfwSetInputMode(mc.getWindow().getHandle(),
            GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        GLFW.glfwSetInputMode(mc.getWindow().getHandle(),
            GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
    }
}
