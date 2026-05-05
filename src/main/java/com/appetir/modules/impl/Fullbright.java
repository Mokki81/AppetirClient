package com.appetir.modules.impl;
import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
public class Fullbright extends Module {
    private float savedGamma = 1.0f;
    public Fullbright() { super("Fullbright", "Полная яркость без факелов", Category.RENDER); }
    @Override public void onEnable() {
        savedGamma = MinecraftClient.getInstance().options.gamma;
        MinecraftClient.getInstance().options.gamma = 16.0f;
    }
    @Override public void onDisable() {
        MinecraftClient.getInstance().options.gamma = savedGamma;
    }
}
