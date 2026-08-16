package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Eats via interactionManager. Tracks real use state, not fixed 32-tick timer.
 */
public class AutoEat extends Module {

    private int prevSlot = -1;
    private int foodSlot = -1;
    private boolean startedUse;
    private int idleTicks; // ticks since interact without isUsingItem
    private int startHunger;

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

        // Finish path: we started use — wait until not using or hunger improved
        if (startedUse) {
            if (mc.player.isUsingItem()) {
                idleTicks = 0;
                return;
            }
            // Not using — either finished or never started
            idleTicks++;
            boolean hungerUp = mc.player.getHungerManager().getFoodLevel() > startHunger;
            if (hungerUp || idleTicks > 5) {
                stopIfOurs();
                restoreSlot();
            }
            return;
        }

        if (mc.player.getHungerManager().getFoodLevel() >= 17) return;
        if (mc.player.isUsingItem()) return;
        // Don't steal slot if player already holding food and might use it
        ItemStack held = mc.player.getMainHandStack();
        if (held.getItem().isFood()) {
            // Use current slot only
            startHunger = mc.player.getHungerManager().getFoodLevel();
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            startedUse = true;
            idleTicks = 0;
            foodSlot = mc.player.inventory.selectedSlot;
            prevSlot = -1; // didn't change slot
            return;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.inventory.getStack(i);
            if (!s.getItem().isFood()) continue;

            prevSlot = mc.player.inventory.selectedSlot;
            foodSlot = i;
            mc.player.inventory.selectedSlot = i;
            startHunger = mc.player.getHungerManager().getFoodLevel();
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            startedUse = true;
            idleTicks = 0;
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
        idleTicks = 0;
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
