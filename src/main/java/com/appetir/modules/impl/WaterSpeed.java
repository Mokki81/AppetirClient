package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;

public class WaterSpeed extends Module {
    public WaterSpeed() { super("WaterSpeed","Увеличивает скорость в воде",Category.MOVEMENT); }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player==null||!mc.player.isTouchingWater()) return;
        if (mc.options.keyForward.isPressed()) {
            double yaw = Math.toRadians(mc.player.getYaw());
            mc.player.addVelocity(-Math.sin(yaw)*0.08, 0, Math.cos(yaw)*0.08);
        }
        if (mc.options.keyJump.isPressed()) mc.player.addVelocity(0,0.08,0);
    }
}
