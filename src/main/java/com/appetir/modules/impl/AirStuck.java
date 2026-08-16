package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;

public class AirStuck extends Module {

    private final BooleanSetting noClip = new BooleanSetting("NoClip", "Disable collision", true);
    private boolean savedNoClip;
    private boolean stateSaved;

    public AirStuck() {
        super("AirStuck", "Зависание в воздухе", Category.MOVEMENT);
        addSetting(noClip);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        savedNoClip = mc.player.noClip;
        stateSaved = true;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.setVelocity(0, 0, 0);
        mc.player.velocityModified = true;
        // Always set both ways so toggling setting mid-enable works
        mc.player.noClip = noClip.get();
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && stateSaved) {
            mc.player.noClip = savedNoClip;
        } else if (mc.player != null) {
            mc.player.noClip = false;
        }
        stateSaved = false;
    }
}
