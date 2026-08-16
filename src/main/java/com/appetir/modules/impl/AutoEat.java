package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Eats via interactionManager — does not own keyUse.
 */
public class AutoEat extends Module {

    private int cooldown = 0;
    private int prevSlot = -1;
    private int foodSlot = -1;
    private boolean startedUse;

    public AutoEat() {
        super("AutoEat", "Автоматически ест еду", Category.MISC);
    }

    @Override
    public void onDisable() {
        stopIfOurs();
        restoreSlot();
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            stopIfOurs();
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            if (cooldown == 0) {
                stopIfOurs();
                restoreSlot();
            }
            return;
        }

        if (mc.player.getHungerManager().getFoodLevel() >= 17) {
            stopIfOurs();
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
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            startedUse = true;
            cooldown = 32;
            return;
        }
    }

    private void stopIfOurs() {
        if (!startedUse) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.isUsingItem()) {
            mc.player.stopUsingItem();
        }
        startedUse = false;
    }

    private void restoreSlot() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            prevSlot = -1;
            foodSlot = -1;
            return;
        }
        if (prevSlot >= 0 && foodSlot >= 0 && mc.player.inventory.selectedSlot == foodSlot) {
            mc.player.inventory.selectedSlot = prevSlot;
        }
        prevSlot = -1;
        foodSlot = -1;
    }
}
