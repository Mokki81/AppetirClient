package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class WaterSpeed extends Module {

    private final NumberSetting speed = new NumberSetting("Speed", "Target horizontal speed", 0.25, 0.05, 0.6, 0.01);
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

        double target = speed.get();
        double yaw = Math.toRadians(mc.player.yaw);
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        Vec3d vel = mc.player.getVelocity();
        double y = vel.y;

        if (forward != 0 || strafe != 0) {
            if (forward != 0) {
                if (strafe > 0) yaw += (forward > 0 ? -0.785 : 0.785);
                else if (strafe < 0) yaw += (forward > 0 ? 0.785 : -0.785);
                strafe = 0;
                forward = forward > 0 ? 1 : -1;
            }
            double mx = forward * target * -Math.sin(yaw) + strafe * target * Math.cos(yaw);
            double mz = forward * target * Math.cos(yaw) + strafe * target * Math.sin(yaw);
            // Set horizontal velocity to target — no accumulation
            mc.player.setVelocity(mx, y, mz);
            vel = mc.player.getVelocity();
            y = vel.y;
        }

        if (vertical.get()) {
            if (mc.options.keyJump.isPressed()) {
                mc.player.setVelocity(vel.x, Math.max(y, target), vel.z);
            } else if (mc.options.keySneak.isPressed()) {
                mc.player.setVelocity(vel.x, Math.min(y, -target), vel.z);
            }
        }
    }
}
