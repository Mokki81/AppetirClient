package com.appetir.modules.impl;

import com.appetir.modules.Module;
import com.appetir.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;

public class NoSlow extends Module {

    private final BooleanSetting items = new BooleanSetting("Items", "No slow while using items", true);
    private final BooleanSetting soulSand = new BooleanSetting("SoulSand", "No soul sand slow", true);

    public NoSlow() {
        super("NoSlow", "Убирает замедление", Category.MOVEMENT);
        addSetting(items);
        addSetting(soulSand);
    }

    public boolean items() { return isEnabled() && items.get(); }
    public boolean soulSand() { return isEnabled() && soulSand.get(); }

    @Override
    public void onTick() {
        // Actual cancel of item-use slowdown is in NoSlowMixin.
        // Here we can correct velocity if needed.
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (items.get() && mc.player.isUsingItem() && (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0)) {
            // slight compensation — mixin does the heavy lifting
        }
    }
}
