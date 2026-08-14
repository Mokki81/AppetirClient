package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

public class Fullbright extends Module {

    private final NumberSetting gamma = new NumberSetting("Gamma", "Brightness level", 16.0, 1.0, 16.0, 0.5);
    private double savedGamma = 1.0;

    public Fullbright() {
        super("Fullbright", "Полная яркость", Category.RENDER);
        addSetting(gamma);
    }

    @Override
    public void onEnable() {
        savedGamma = MinecraftClient.getInstance().options.gamma;
        apply();
    }

    @Override
    public void onTick() {
        apply();
    }

    @Override
    public void onDisable() {
        MinecraftClient.getInstance().options.gamma = savedGamma;
    }

    private void apply() {
        MinecraftClient.getInstance().options.gamma = gamma.get();
    }
}
