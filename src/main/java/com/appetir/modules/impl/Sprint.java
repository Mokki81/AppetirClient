package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;

public class Sprint extends Module {

    private final BooleanSetting omni = new BooleanSetting("Omni", "Sprint in all directions", true);
    private final BooleanSetting keep = new BooleanSetting("Keep", "Keep sprinting while using items", true);

    public Sprint() {
        super("Sprint", "Автоматический спринт", Category.MOVEMENT);
        addSetting(omni);
        addSetting(keep);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (mc.player.isSneaking() || mc.player.isTouchingWater() || mc.player.isSubmergedInWater()) return;

        boolean moving = omni.get()
                ? (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0)
                : mc.player.input.movementForward > 0;

        if (moving) {
            if (keep.get() || !mc.player.isUsingItem()) {
                mc.player.setSprinting(true);
            }
        }
    }
}
