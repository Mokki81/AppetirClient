package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;

public class FreeCamera extends Module {
    public FreeCamera() { super("FreeCamera","Свободная камера",Category.MISC); }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null) return;
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().allowFlying = true;
        mc.player.noClip = true;
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null) return;
        mc.player.noClip = false;
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().allowFlying = false;
    }
}
