package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;

public class Optimization extends Module {

    public Optimization() {
        super("Optimization", "Оптимизация производительности", Category.MISC);
        setEnabled(true); // включена по умолчанию
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Ограничиваем FPS в фоне
        if (!mc.isWindowFocused()) {
            try { Thread.sleep(5); } catch (InterruptedException ignored) {}
        }
    }
}
