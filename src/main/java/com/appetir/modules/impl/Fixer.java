package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;

public class Fixer extends Module {
    private int tick=0;
    public Fixer() { super("Fixer","Исправляет различные баги",Category.MISC); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null) return;
        tick++;
        if (mc.player.isInsideWall()&&tick%10==0)
            mc.player.setPosition(mc.player.getX(),mc.player.getY()+0.1,mc.player.getZ());
        float yaw=mc.player.yaw, pitch=mc.player.pitch;
        if (Float.isNaN(yaw)||Float.isInfinite(yaw)) mc.player.yaw=0;
        if (Float.isNaN(pitch)||Float.isInfinite(pitch)) mc.player.pitch=0;
    }
}
