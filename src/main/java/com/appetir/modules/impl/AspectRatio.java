package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.NumberSetting;
import com.appetir.util.NotificationManager;

/**
 * Stub — framebuffer-width override was unsafe and removed.
 * Enabling shows a notice; no global FB mutation.
 */
public class AspectRatio extends Module {

    public static float ratio = 16.0f / 9.0f;

    private final NumberSetting ratioSetting = new NumberSetting("Ratio", "Target aspect (display only)", 1.78, 0.5, 3.0, 0.01);

    public AspectRatio() {
        super("AspectRatio", "Соотношение сторон (stub — безопасный)", Category.RENDER);
        addSetting(ratioSetting);
    }

    @Override
    public void onEnable() {
        ratio = ratioSetting.getFloat();
        NotificationManager.push("AspectRatio", "Framebuffer override disabled (unsafe)");
    }
}
