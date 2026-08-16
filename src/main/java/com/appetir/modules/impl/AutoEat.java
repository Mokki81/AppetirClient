package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

public class AutoEat extends Module {

    private int cooldown = 0;
    private boolean holdingUse;
    private int prevSlot = -1;
    private int foodSlot = -1;

    public AutoEat() {
        super("AutoEat", "Автоматически ест еду", Category.MISC);
    }

    @Override
    public void onDisable() {
        releaseUse();
        restoreSlot();
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null) {
            releaseUse();
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            if (cooldown == 0) {
                releaseUse();
                restoreSlot();
            }
            return;
        }

        if (mc.player.getHungerManager().getFoodLevel() >= 17) {
            releaseUse();
            restoreSlot();
            return;
        }

        if (mc.player.isUsingItem()) return;

        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.inventory.getStack(i);
            if (!s.getItem().isFood()) continue;

            if (prevSlot < 0) prevSlot = mc.player.inventory.selectedSlot;
            foodSlot = i;
            mc.player.inventory.selectedSlot = i;
            mc.options.keyUse.setPressed(true);
            holdingUse = true;
            cooldown = 32;
            return;
        }

        releaseUse();
    }

    private void releaseUse() {
        if (!holdingUse) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) mc.options.keyUse.setPressed(false);
        holdingUse = false;
    }

    private void restoreSlot() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            prevSlot = -1;
            foodSlot = -1;
            return;
        }
        // Only restore if still on the food slot we switched to
        if (prevSlot >= 0 && foodSlot >= 0 && mc.player.inventory.selectedSlot == foodSlot) {
            mc.player.inventory.selectedSlot = prevSlot;
        }
        prevSlot = -1;
        foodSlot = -1;
    }
}
