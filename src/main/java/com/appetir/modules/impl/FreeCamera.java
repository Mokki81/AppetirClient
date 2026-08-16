package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;

/**
 * Noclip camera — restores position only if same world (#31).
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
    private RegistryKey<World> savedWorld;

    public FreeCamera() {
        super("FreeCamera", "Свободная камера (noclip + возврат)", Category.MISC);
        addSetting(speed);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        savedPos = mc.player.getPos();
        savedYaw = mc.player.yaw;
        savedPitch = mc.player.pitch;
        savedAllowFlying = mc.player.abilities.allowFlying;
        savedFlying = mc.player.abilities.flying;
        savedFlySpeed = mc.player.abilities.getFlySpeed();
        savedNoClip = mc.player.noClip;
        savedWorld = mc.world.getRegistryKey();
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

        // #31: only restore position in the same dimension/world
        boolean sameWorld = mc.world != null
                && savedWorld != null
                && mc.world.getRegistryKey().equals(savedWorld);

        if (sameWorld && savedPos != null) {
            mc.player.setPosition(savedPos.x, savedPos.y, savedPos.z);
            mc.player.yaw = savedYaw;
            mc.player.pitch = savedPitch;
        }
        stateSaved = false;
        savedWorld = null;
        savedPos = null;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        // World changed while enabled — drop restore, keep flying only
        if (stateSaved && mc.world != null && savedWorld != null
                && !mc.world.getRegistryKey().equals(savedWorld)) {
            stateSaved = false;
            savedPos = null;
            savedWorld = null;
        }
        mc.player.noClip = true;
        mc.player.abilities.flying = true;
        mc.player.abilities.setFlySpeed((float) (speed.get() * 0.05));
    }
}
