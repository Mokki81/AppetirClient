package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;

public class CustomWorld extends Module {

    public static long customTime = 6000L; // полдень
    public static boolean lockTime = true;
    public static float fogStart = 0.0f;
    public static float fogEnd   = 512.0f;

    public CustomWorld() {
        super("CustomWorld", "Кастомизация мира", Category.RENDER);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;
        if (lockTime) {
            // Принудительно удерживаем время
            mc.world.setTimeOfDay(customTime);
        }
    }
}
