package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Noclip/fly camera that restores vanilla flight state on disable.
 * Not a detached camera entity — returns player to saved position.
 */
public class FreeCamera extends Module {

    private final NumberSetting speed = new NumberSetting("Speed", "Camera speed", 1.5, 0.3, 5.0, 0.1);

    private Vec3d savedPos;
    private float savedYaw, savedPitch;
    private boolean savedAllowFlying;
    private boolean savedFlying;
    private float savedFlySpeed;
    private boolean savedNoClip;
    private boolean stateSaved;

    public FreeCamera() {
        super("FreeCamera", "Свободная камера (noclip + возврат)", Category.MISC);
        addSetting(speed);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        savedPos = mc.player.getPos();
        savedYaw = mc.player.yaw;
        savedPitch = mc.player.pitch;
        savedAllowFlying = mc.player.abilities.allowFlying;
        savedFlying = mc.player.abilities.flying;
        savedFlySpeed = mc.player.abilities.getFlySpeed();
        savedNoClip = mc.player.noClip;
        stateSaved = true;

        mc.player.noClip = true;
        mc.player.abilities.flying = true;
        mc.player.abilities.allowFlying = true;
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !stateSaved) return;

        mc.player.noClip = savedNoClip;
        mc.player.abilities.allowFlying = savedAllowFlying;
        mc.player.abilities.flying = savedFlying;
        mc.player.abilities.setFlySpeed(savedFlySpeed);

        if (savedPos != null) {
            mc.player.setPosition(savedPos.x, savedPos.y, savedPos.z);
            mc.player.yaw = savedYaw;
            mc.player.pitch = savedPitch;
        }
        stateSaved = false;
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
