package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.ModeSetting;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Speed mode", "Strafe", "Strafe", "Vanilla", "BHop");
    private final NumberSetting speed = new NumberSetting("Speed", "Movement multiplier", 1.2, 1.0, 3.0, 0.05);

    public Speed() {
        super("Speed", "Увеличивает скорость передвижения", Category.MOVEMENT);
        addSetting(mode);
        addSetting(speed);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.isFallFlying() || mc.player.isTouchingWater()) return;
        if (!isMoving(mc)) return;

        double s = speed.get();
        String m = mode.get();

        if (m.equals("Vanilla")) {
            mc.player.abilities.setWalkSpeed((float) (0.1f * s));
            return;
        }

        if (m.equals("BHop") && mc.player.isOnGround()) {
            mc.player.jump();
        }

        // Strafe / BHop boost
        Vec3d v = mc.player.getVelocity();
        float yaw = mc.player.yaw * 0.017453292f;
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;

        if (forward == 0 && strafe == 0) return;

        if (forward != 0) {
            if (strafe > 0) yaw += (forward > 0 ? -45 : 45) * 0.017453292f;
            else if (strafe < 0) yaw += (forward > 0 ? 45 : -45) * 0.017453292f;
            strafe = 0;
            forward = forward > 0 ? 1 : -1;
        }

        double mx = forward * s * 0.28 * -Math.sin(yaw) + strafe * s * 0.28 * Math.cos(yaw);
        double mz = forward * s * 0.28 * Math.cos(yaw) + strafe * s * 0.28 * Math.sin(yaw);

        mc.player.setVelocity(mx, v.y, mz);
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.abilities.setWalkSpeed(0.1f);
        }
    }

    private boolean isMoving(MinecraftClient mc) {
        return mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
    }
}
