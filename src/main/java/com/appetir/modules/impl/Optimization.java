package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Limits FPS when window is unfocused via options — no Thread.sleep on game thread.
 */
public class Optimization extends Module {

    private final BooleanSetting unfocusedFps = new BooleanSetting("UnfocusedFPS", "Limit FPS when unfocused", true);
    private final NumberSetting unfocusedLimit = new NumberSetting("FPS Limit", "FPS when unfocused", 30, 5, 60, 5);

    private int savedMaxFps = -1;
    private boolean limited;

    public Optimization() {
        super("Optimization", "Оптимизация FPS", Category.MISC);
        addSetting(unfocusedFps);
        addSetting(unfocusedLimit);
    }

    @Override
    public void onDisable() {
        restoreFps();
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.options == null) return;

        if (!unfocusedFps.get()) {
            restoreFps();
            return;
        }

        if (!mc.isWindowFocused()) {
            if (!limited) {
                savedMaxFps = mc.options.maxFps;
                limited = true;
            }
            int limit = Math.max(5, unfocusedLimit.getInt());
            if (mc.options.maxFps != limit) {
                mc.options.maxFps = limit;
            }
        } else {
            restoreFps();
        }
    }

    private void restoreFps() {
        if (!limited) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.options != null && savedMaxFps > 0) {
            mc.options.maxFps = savedMaxFps;
        }
        limited = false;
        savedMaxFps = -1;
    }
}
