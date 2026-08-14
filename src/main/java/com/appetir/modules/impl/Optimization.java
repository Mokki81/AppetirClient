package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

public class Optimization extends Module {

    private final BooleanSetting unfocusedFps = new BooleanSetting("UnfocusedFPS", "Limit FPS when unfocused", true);
    private final NumberSetting unfocusedLimit = new NumberSetting("FPS Limit", "FPS when unfocused", 30, 5, 60, 5);

    public Optimization() {
        super("Optimization", "Оптимизация FPS", Category.MISC);
        addSetting(unfocusedFps);
        addSetting(unfocusedLimit);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        if (unfocusedFps.get() && !mc.isWindowFocused()) {
            // Soft yield — don't freeze the client hard
            try {
                int limit = unfocusedLimit.getInt();
                if (limit > 0) Thread.sleep(Math.max(1, 1000 / limit / 2));
            } catch (InterruptedException ignored) {}
        }
    }
}
