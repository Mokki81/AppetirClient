package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

public class Spider extends Module {

    private final NumberSetting speed = new NumberSetting("Speed", "Climb speed", 0.35, 0.1, 1.0, 0.05);

    public Spider() {
        super("Spider", "Лазание по стенам", Category.MOVEMENT);
        addSetting(speed);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (mc.player.horizontalCollision && mc.options.keyForward.isPressed()) {
            mc.player.setVelocity(mc.player.getVelocity().x, speed.get(), mc.player.getVelocity().z);
        }
    }

    public double getClimbSpeed() {
        return speed.get();
    }
}
