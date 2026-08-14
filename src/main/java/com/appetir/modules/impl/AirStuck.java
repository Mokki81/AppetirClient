package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;

public class AirStuck extends Module {

    private final BooleanSetting noClip = new BooleanSetting("NoClip", "Disable collision", true);

    public AirStuck() {
        super("AirStuck", "Зависание в воздухе", Category.MOVEMENT);
        addSetting(noClip);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.setVelocity(0, 0, 0);
        mc.player.velocityModified = true;
        if (noClip.get()) mc.player.noClip = true;
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.noClip = false;
    }
}
