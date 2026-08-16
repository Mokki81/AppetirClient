package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoEat extends Module {

    private int prevSlot = -1;
    private int foodSlot = -1;
    private boolean startedUse;
    private int idleTicks;
    private int startHunger;
    private int startCount;
    private Item startedItem;

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

        if (startedUse) {
            if (mc.player.isUsingItem()) {
                // Still ours only if same hand item
                ItemStack active = mc.player.getActiveItem();
                if (startedItem != null && !active.isEmpty() && active.getItem() != startedItem) {
                    // User switched to another use — release ownership, don't stop
                    startedUse = false;
                    startedItem = null;
                    restoreSlot();
                    return;
                }
                idleTicks = 0;
                return;
            }
            idleTicks++;
            boolean hungerUp = mc.player.getHungerManager().getFoodLevel() > startHunger;
            boolean stackDown = false;
            if (foodSlot >= 0) {
                ItemStack s = mc.player.inventory.getStack(foodSlot);
                stackDown = s.getCount() < startCount;
            }
            if (hungerUp || stackDown || idleTicks > 8) {
                stopIfOurs();
                restoreSlot();
            }
            return;
        }

        if (mc.player.getHungerManager().getFoodLevel() >= 17) return;
        if (mc.player.isUsingItem()) return;

        ItemStack held = mc.player.getMainHandStack();
        if (held.getItem().isFood()) {
            startHunger = mc.player.getHungerManager().getFoodLevel();
            startCount = held.getCount();
            foodSlot = mc.player.inventory.selectedSlot;
            prevSlot = -1;
            startedItem = held.getItem();
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            if (mc.player.isUsingItem()) {
                startedUse = true;
                idleTicks = 0;
            } else {
                startedItem = null;
            }
            return;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.inventory.getStack(i);
            if (!s.getItem().isFood()) continue;

            prevSlot = mc.player.inventory.selectedSlot;
            foodSlot = i;
            startCount = s.getCount();
            startHunger = mc.player.getHungerManager().getFoodLevel();
            startedItem = s.getItem();
            mc.player.inventory.selectedSlot = i;
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            if (mc.player.isUsingItem()) {
                startedUse = true;
                idleTicks = 0;
            } else {
                startedItem = null;
                restoreSlot();
            }
            return;
        }
    }

    private void stopIfOurs() {
        if (!startedUse) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.isUsingItem()) {
            ItemStack active = mc.player.getActiveItem();
            // Only stop if still the item we started
            if (startedItem == null || active.isEmpty() || active.getItem() == startedItem) {
                mc.player.stopUsingItem();
            }
        }
        startedUse = false;
        startedItem = null;
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
