package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.ModeSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class Fly extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Fly mode", "Creative", "Creative", "Motion", "Jetpack");
    private final NumberSetting speed = new NumberSetting("Speed", "Fly speed", 1.2, 0.3, 5.0, 0.1);
    private final NumberSetting vertical = new NumberSetting("Vertical", "Up/down speed", 0.8, 0.1, 3.0, 0.1);

    public Fly() {
        super("Fly", "Позволяет летать", Category.MOVEMENT);
        addSetting(mode);
        addSetting(speed);
        addSetting(vertical);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mode.is("Creative")) {
            mc.player.abilities.allowFlying = true;
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.abilities.allowFlying = false;
            mc.player.abilities.flying = false;
        }
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String m = mode.get();
        double s = speed.get();
        double v = vertical.get();

        if (m.equals("Creative")) {
            mc.player.abilities.allowFlying = true;
            mc.player.abilities.setFlySpeed((float) (s * 0.05));
            return;
        }

        if (m.equals("Jetpack")) {
            if (mc.options.keyJump.isPressed()) {
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x, v * 0.4, vel.z);
            }
            return;
        }

        // Motion mode
        mc.player.abilities.flying = false;
        Vec3d vel = mc.player.getVelocity();
        double motionY = 0;

        if (mc.options.keyJump.isPressed()) motionY = v * 0.4;
        else if (mc.options.keySneak.isPressed()) motionY = -v * 0.4;
        else motionY = 0;

        float yaw = mc.player.yaw * 0.017453292f;
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        if (forward == 0 && strafe == 0) {
            mc.player.setVelocity(0, motionY, 0);
            return;
        }

        if (forward != 0) {
            if (strafe > 0) yaw += (forward > 0 ? -45 : 45) * 0.017453292f;
            else if (strafe < 0) yaw += (forward > 0 ? 45 : -45) * 0.017453292f;
            strafe = 0;
            forward = forward > 0 ? 1 : -1;
        }

        double mx = forward * s * 0.35 * -Math.sin(yaw) + strafe * s * 0.35 * Math.cos(yaw);
        double mz = forward * s * 0.35 * Math.cos(yaw) + strafe * s * 0.35 * Math.sin(yaw);
        mc.player.setVelocity(mx, motionY, mz);
    }
}
