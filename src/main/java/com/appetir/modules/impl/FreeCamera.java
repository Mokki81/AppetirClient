package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class FreeCamera extends Module {

    private final NumberSetting speed = new NumberSetting("Speed", "Camera speed", 1.5, 0.3, 5.0, 0.1);

    private Vec3d savedPos;
    private float savedYaw, savedPitch;

    public FreeCamera() {
        super("FreeCamera", "Свободная камера (noclip)", Category.MISC);
        addSetting(speed);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        savedPos = mc.player.getPos();
        savedYaw = mc.player.yaw;
        savedPitch = mc.player.pitch;
        mc.player.noClip = true;
        mc.player.abilities.flying = true;
        mc.player.abilities.allowFlying = true;
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.noClip = false;
        mc.player.abilities.flying = false;
        mc.player.abilities.allowFlying = false;
        if (savedPos != null) {
            mc.player.setPosition(savedPos.x, savedPos.y, savedPos.z);
            mc.player.yaw = savedYaw;
            mc.player.pitch = savedPitch;
        }
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.noClip = true;
        mc.player.abilities.flying = true;
        mc.player.abilities.setFlySpeed((float) (speed.get() * 0.05));
    }
}
