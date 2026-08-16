package com.appetir.modules.impl;

import com.appetir.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoEat extends Module {

    private int prevSlot = -1;
    private int foodSlot = -1;
    private boolean startedUse;
    private int idleTicks;
    private int startHunger;
    private int startCount;

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
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            if (mc.player.isUsingItem()) {
                startedUse = true;
                idleTicks = 0;
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
            mc.player.inventory.selectedSlot = i;
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            if (mc.player.isUsingItem()) {
                startedUse = true;
                idleTicks = 0;
            } else {
                // didn't start — restore slot immediately
                restoreSlot();
            }
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
