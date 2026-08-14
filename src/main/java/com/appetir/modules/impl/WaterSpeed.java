package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

public class WaterSpeed extends Module {

    private final NumberSetting speed = new NumberSetting("Speed", "Water boost", 0.1, 0.02, 0.4, 0.01);
    private final BooleanSetting vertical = new BooleanSetting("Vertical", "Boost up/down", true);

    public WaterSpeed() {
        super("WaterSpeed", "Увеличивает скорость в воде", Category.MOVEMENT);
        addSetting(speed);
        addSetting(vertical);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !mc.player.isTouchingWater()) return;

        double s = speed.get();
        double yaw = Math.toRadians(mc.player.yaw);

        if (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0) {
            double forward = mc.player.input.movementForward;
            double strafe = mc.player.input.movementSideways;
            if (forward != 0) {
                if (strafe > 0) yaw += (forward > 0 ? -0.785 : 0.785);
                else if (strafe < 0) yaw += (forward > 0 ? 0.785 : -0.785);
                strafe = 0;
                forward = forward > 0 ? 1 : -1;
            }
            double mx = forward * s * -Math.sin(yaw) + strafe * s * Math.cos(yaw);
            double mz = forward * s * Math.cos(yaw) + strafe * s * Math.sin(yaw);
            mc.player.addVelocity(mx, 0, mz);
        }

        if (vertical.get()) {
            if (mc.options.keyJump.isPressed()) mc.player.addVelocity(0, s, 0);
            if (mc.options.keySneak.isPressed()) mc.player.addVelocity(0, -s, 0);
        }
    }
}
